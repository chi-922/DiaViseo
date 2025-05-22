package com.s206.health.bodyinfo.controller;

import com.s206.common.dto.ResponseDto;
import com.s206.health.bodyinfo.dto.request.BodyInfoCreateRequest;
import com.s206.health.bodyinfo.dto.request.BodyInfoPatchRequest;
import com.s206.health.bodyinfo.dto.response.BodyInfoProjection;
import com.s206.health.bodyinfo.dto.response.BodyInfoResponse;
import com.s206.health.bodyinfo.dto.response.MonthlyAverageBodyInfoResponse;
import com.s206.health.bodyinfo.dto.response.WeeklyAverageBodyInfoResponse;
import com.s206.health.bodyinfo.service.BodyInfoService;
import com.s206.health.bodyinfo.service.InBodyOcrService;
import com.s206.health.client.UserClient;
import com.s206.health.client.dto.request.BodyCompositionRequest;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.format.annotation.DateTimeFormat.ISO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/bodies")
public class BodyInfoController {

	private final BodyInfoService bodyInfoService;
	private final InBodyOcrService inBodyOcrService;
	private final UserClient userClient;

	@PostMapping
	public ResponseEntity<ResponseDto<BodyInfoResponse>> create(
			@RequestHeader("X-USER-ID") Integer userId, @Valid @RequestBody
			BodyInfoCreateRequest request) {
		BodyInfoResponse response = bodyInfoService.create(userId, request);
		BodyCompositionRequest bodyCompositionRequest = new BodyCompositionRequest(userId,
				response.getHeight(), response.getWeight());
		userClient.updateBodyComposition(bodyCompositionRequest);
		return ResponseEntity.status(HttpStatus.CREATED)
				.body(ResponseDto.success(HttpStatus.CREATED, "체성분 데이터 등록 성공", response));
	}

	@PostMapping("/ocr")
	public ResponseEntity<ResponseDto<BodyInfoCreateRequest>> extractFromOcr(
			@RequestHeader("X-USER-ID") Integer userId,
			@RequestParam("image") MultipartFile imageFile) {

		try {
			// 비동기 OCR 처리
			CompletableFuture<BodyInfoCreateRequest> future =
					inBodyOcrService.extractBodyInfoFromImageAsync(imageFile);

			// 결과 대기 (타임아웃 30초)
			BodyInfoCreateRequest extractedData = future.get(30, TimeUnit.SECONDS);

			return ResponseEntity.ok()
					.body(ResponseDto.success(HttpStatus.OK, "OCR 정보 추출 성공", extractedData));

		} catch (TimeoutException e) {
			log.error("OCR 처리 시간 초과", e);
			return ResponseEntity.status(HttpStatus.REQUEST_TIMEOUT)
					.body(ResponseDto.error(HttpStatus.REQUEST_TIMEOUT, "처리 시간이 초과되었습니다."));
		} catch (Exception e) {
			log.error("OCR 처리 중 오류 발생", e);
			return ResponseEntity.status(HttpStatus.BAD_REQUEST)
					.body(ResponseDto.error(HttpStatus.BAD_REQUEST,
							"이미지 처리 중 오류가 발생했습니다: " + e.getMessage()));
		}
	}

	// 필요시 OCR 결과를 확인하고 수정한 후 저장하는 새로운 엔드포인트
	@PostMapping("/ocr/confirm")
	public ResponseEntity<ResponseDto<BodyInfoResponse>> confirmOcrData(
			@RequestHeader("X-USER-ID") Integer userId,
			@Valid @RequestBody BodyInfoCreateRequest request) {

		BodyInfoResponse response = bodyInfoService.create(userId, request);
		return ResponseEntity.status(HttpStatus.CREATED)
				.body(ResponseDto.success(HttpStatus.CREATED, "OCR 데이터 확인 후 체성분 데이터 등록 성공",
						response));
	}

	@GetMapping
	public ResponseEntity<ResponseDto<List<BodyInfoResponse>>> findLatestByDate(
			@RequestHeader("X-USER-ID") Integer userId,
			@RequestParam(name = "date", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {

		// date 파라미터가 없는 경우 현재 날짜로 설정
		LocalDate targetDate = (date != null) ? date : LocalDate.now();

		List<BodyInfoResponse> response = bodyInfoService.findLatestByUserIdAndDate(userId, targetDate);

		return ResponseEntity.ok(
				ResponseDto.success(HttpStatus.OK,
						"유저 체성분 정보 목록 요청이 성공적으로 반환 처리됐습니다.",
						response));
	}

	@PatchMapping("/{bodyId}")
	public ResponseEntity<ResponseDto<BodyInfoResponse>> patchBodyInfo(
			@RequestHeader("X-USER-ID") Integer userId,
			@PathVariable("bodyId") Integer bodyId,
			@Valid @RequestBody BodyInfoPatchRequest request
	) {
		BodyInfoResponse response = bodyInfoService.updateBodyInfo(userId, bodyId, request);
		return ResponseEntity.ok(
				ResponseDto.success(HttpStatus.OK, "체성분 정보가 성공적으로 수정되었습니다.", response));
	}

	@DeleteMapping("/{bodyId}")
	public ResponseEntity<ResponseDto<Void>> deleteBodyInfo(
			@RequestHeader("X-USER-ID") Integer userId,
			@PathVariable Integer bodyId) {
		bodyInfoService.deleteBodyInfo(userId, bodyId);
		return ResponseEntity.ok(
				ResponseDto.success(HttpStatus.OK, "체성분 정보가 성공적으로 삭제되었습니다.", null));
	}

	@GetMapping("/date")
	public ResponseEntity<ResponseDto<BodyInfoResponse>> findByDate(
			@RequestHeader("X-USER-ID") Integer userId,
			@RequestParam @DateTimeFormat(iso = ISO.DATE) LocalDate date) {
		try {
			// 현재 날짜 체크
			LocalDate today = LocalDate.now();
			if (date.isAfter(today)) {
				return ResponseEntity.badRequest()
						.body(ResponseDto.error(HttpStatus.BAD_REQUEST, "미래 날짜는 입력할 수 없습니다."));
			}

			// 너무 오래된 날짜 체크
			if (date.isBefore(LocalDate.of(2000, 1, 1))) {
				return ResponseEntity.badRequest()
						.body(ResponseDto.error(HttpStatus.BAD_REQUEST,
								"2000년 1월 1일 이전 날짜는 입력할 수 없습니다."));
			}

			BodyInfoResponse response = bodyInfoService.findByUserIdAndDate(userId, date);
			// 데이터가 없을 경우 처리
			if (response == null) {
				return ResponseEntity.ok(
						ResponseDto.success(HttpStatus.OK, "해당 날짜의 체성분 정보가 없습니다.", null));
			}

			return ResponseEntity.ok(
					ResponseDto.success(HttpStatus.OK, "유저 체성분 정보 조회가 성공적으로 처리됐습니다.", response));
		} catch (Exception e) {
			log.error("체성분 정보 조회 중 오류 발생: {}", e.getMessage(), e);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(ResponseDto.error(HttpStatus.INTERNAL_SERVER_ERROR,
							"서버 처리 중 오류가 발생했습니다."));
		}
	}

	@GetMapping("/weekly")
	public ResponseEntity<ResponseDto<List<BodyInfoProjection>>> getWeeklyBodyInfo(
			@RequestHeader("X-USER-ID") Integer userId,
			@RequestParam(required = false) @DateTimeFormat(iso = ISO.DATE) LocalDate endDate) {

		List<BodyInfoProjection> response = bodyInfoService.getWeeklyBodyInfo(userId, endDate);
		return ResponseEntity.ok(
				ResponseDto.success(HttpStatus.OK, "주간 체성분 정보 조회가 성공적으로 처리됐습니다.", response));
	}

	@GetMapping("/weekly-avg")
	public ResponseEntity<ResponseDto<List<WeeklyAverageBodyInfoResponse>>> getWeeklyAverages(
			@RequestHeader("X-USER-ID") Integer userId,
			@RequestParam(required = false) @DateTimeFormat(iso = ISO.DATE) LocalDate endDate
	) {
		if (endDate == null) {
			endDate = LocalDate.now();
		}

		List<WeeklyAverageBodyInfoResponse> response = bodyInfoService.getWeeklyAverages(userId,
				endDate);

		return ResponseEntity.ok(
				ResponseDto.success(HttpStatus.OK, "7주간 주별 평균 체성분 정보 조회가 성공적으로 처리됐습니다.", response));
	}

	@GetMapping("/monthly-avg")
	public ResponseEntity<ResponseDto<List<MonthlyAverageBodyInfoResponse>>> getMonthlyAverages(
			@RequestHeader("X-USER-ID") Integer userId,
			@RequestParam(required = false) @DateTimeFormat(iso = ISO.DATE) LocalDate endDate
	) {
		if (endDate == null) {
			endDate = LocalDate.now();
		}

		List<MonthlyAverageBodyInfoResponse> response = bodyInfoService.getMonthlyAverages(userId,
				endDate);

		return ResponseEntity.ok(
				ResponseDto.success(HttpStatus.OK, "7개월간 월별 평균 체성분 정보 조회가 성공적으로 처리됐습니다.",
						response));
	}
}
