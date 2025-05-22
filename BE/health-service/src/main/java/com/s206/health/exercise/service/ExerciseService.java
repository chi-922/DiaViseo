package com.s206.health.exercise.service;

import com.s206.common.exception.types.BadRequestException;
import com.s206.common.exception.types.NotFoundException;
import com.s206.health.exercise.dto.request.ExerciseCreateRequest;
import com.s206.health.exercise.dto.request.ExerciseUpdateRequest;
import com.s206.health.exercise.dto.response.ExerciseCategoryResponse;
import com.s206.health.exercise.dto.response.ExerciseListResponse;
import com.s206.health.exercise.dto.response.ExerciseTypeDetailResponse;
import com.s206.health.exercise.dto.response.ExerciseTypeResponse;
import com.s206.health.exercise.entity.Exercise;
import com.s206.health.exercise.entity.ExerciseCategory;
import com.s206.health.exercise.entity.ExerciseType;
import com.s206.health.exercise.favorite.entity.FavoriteExercise;
import com.s206.health.exercise.favorite.repository.FavoriteExerciseRepository;
import com.s206.health.exercise.mapper.ExerciseMapper;
import com.s206.health.exercise.repository.ExerciseCategoryRepository;
import com.s206.health.exercise.repository.ExerciseRepository;
import com.s206.health.exercise.repository.ExerciseTypeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cglib.core.Local;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ExerciseService {

    private final ExerciseRepository exerciseRepository;
    private final ExerciseTypeRepository exerciseTypeRepository;
    private final ExerciseCategoryRepository exerciseCategoryRepository;
    private final ExerciseMapper exerciseMapper;
    private final FavoriteExerciseRepository favoriteExerciseRepository;

    // 특정 사용자의 운동 기록 전체 조회
    @Transactional(readOnly = true)
    public List<ExerciseListResponse> getAllExercisesByUser(Integer userId) {
        List<Exercise> exercises = exerciseRepository.findByUserIdAndIsDeletedFalseOrderByExerciseDateDesc(userId);

        // 모든 운동 유형 정보를 가져와서 Map 으로 변환 (ID -> ExerciseType)
        Map<Integer, ExerciseType> exerciseTypeMap = exerciseTypeRepository.findAll().stream()
                .collect(Collectors.toMap(ExerciseType::getExerciseTypeId, type -> type));

        // 모든 운동 카테고리 정보를 가져와서 Map 으로 변환 (ID -> ExerciseCategory)
        Map<Integer, ExerciseCategory> exerciseCategoryMap = exerciseCategoryRepository.findAll().stream()
                .collect(Collectors.toMap(ExerciseCategory::getExerciseCategoryId, category->category));

        // 운동 기록과 운동 유형, 카테고리 정보를 조합하여 응답 DTO 생성
        return exercises.stream()
                .map(exercise -> {
                    // 운동 유형 및 카테고리 정보 조회
                    ExerciseType exerciseType = exerciseTypeMap.get(exercise.getExerciseTypeId());
                    ExerciseCategory exerciseCategory = null;

                    if (exerciseType != null) {
                        exerciseCategory = exerciseCategoryMap.get(exerciseType.getExerciseCategoryId());
                    }

                    // 매퍼를 사용하여 DTO 변환
                    return exerciseMapper.toListResponse(exercise, exerciseType, exerciseCategory, exercise.getExerciseCalorie());
                })
                .collect(Collectors.toList());
    }

    // 특정 사용자의 운동 기록 상세 조회
    @Transactional(readOnly = true)
    public ExerciseListResponse getExerciseById(Integer userId, Integer exerciseId) {
        // 1. 운동 기록 조회 (해당 사용자 확인)
        Exercise exercise = exerciseRepository.findByExerciseIdAndUserId(exerciseId, userId)
                .orElseThrow(() -> new NotFoundException("해당 사용자의 운동 기록을 찾을 수 없습니다."));

        // 삭제된 운동 기록인 경우 예외 발생
        if (exercise.getIsDeleted()) {
            throw new BadRequestException("삭제된 운동 기록입니다.");
        }

        // 2. 운동 종류 정보 조회
        ExerciseType exerciseType = exerciseTypeRepository.findById(exercise.getExerciseTypeId())
                .orElseThrow(() -> new NotFoundException("존재하지 않는 운동 종류입니다."));

        // 3. 운동 카테고리 정보 조회
        ExerciseCategory exerciseCategory = exerciseCategoryRepository.findById(exerciseType.getExerciseCategoryId())
                .orElseThrow(() -> new NotFoundException("존재하지 않는 운동 카테고리입니다."));

        // 4. 매퍼를 사용하여 응답 DTO 생성 및 반환
        return exerciseMapper.toListResponse(exercise, exerciseType, exerciseCategory, exercise.getExerciseCalorie());
    }

    // 운동 기록 생성
    @Transactional
    public ExerciseListResponse createExercise(Integer userId, ExerciseCreateRequest request) {
        // 날짜가 null 인 경우 현재 시간으로 설정
        LocalDateTime exerciseDate = request.getExerciseDate();
        if (exerciseDate == null) {
            exerciseDate = LocalDateTime.now();
        }


        // 1. 운동 종류 존재 여부 확인
        ExerciseType exerciseType = exerciseTypeRepository.findByExerciseNumberAndIsDeletedFalse(request.getExerciseNumber())
                .orElseThrow(() -> new NotFoundException("존재하지 않는 운동 종류입니다."));

        // 2. 칼로리 계산
        Integer totalCalorie;
        if (request.getExerciseCalorie() != null) {
            totalCalorie = request.getExerciseCalorie();
        } else {
            totalCalorie = exerciseType.getExerciseCalorie() * request.getExerciseTime();
        }

        Exercise exercise = Exercise.builder()
                .userId(userId)  // 경로 변수의 userId 사용
                .exerciseTypeId(exerciseType.getExerciseTypeId())
                .exerciseDate(exerciseDate)
                .exerciseTime(request.getExerciseTime())
                .exerciseCalorie(totalCalorie)
                .healthConnectUuid(request.getHealthConnectUuid())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .isDeleted(false)
                .build();

        // 3. 운동 기록 저장
        Exercise savedExercise = exerciseRepository.save(exercise);

        // 4. 카테고리 정보 조회
        ExerciseCategory exerciseCategory = exerciseCategoryRepository.findById(exerciseType.getExerciseCategoryId())
                .orElse(null);

        // 5. 매퍼를 사용하여 응답 DTO 생성 및 반환
        return exerciseMapper.toListResponse(savedExercise, exerciseType, exerciseCategory, totalCalorie);
    }

    // 운동 기록 리스트 저장
    @Transactional
    public List<ExerciseListResponse> createExercises(Integer userId, List<ExerciseCreateRequest> requests) {
        // 현재부터 40일 전 날짜 계산
        LocalDateTime fortyDaysAgo = LocalDateTime.now().minusDays(40);

        // healthConnectUuid가 있는 요청들만 추출
        Set<String> uuidsToCheck = requests.stream()
                .filter(req -> req.getHealthConnectUuid() != null)
                .map(ExerciseCreateRequest::getHealthConnectUuid)
                .collect(Collectors.toSet());

        // 중복 체크할 UUID가 있는 경우에만 조회
        Set<String> existingUuids = new HashSet<>();
        if (!uuidsToCheck.isEmpty()) {
            existingUuids.addAll(exerciseRepository.findExistingUuids(uuidsToCheck, fortyDaysAgo));
        }

        // 중복되지 않은 요청만 처리하여 저장
        return requests.stream()
                .filter(request -> {
                    // healthConnectUuid가 없거나, 있어도 DB에 없는 경우만 저장
                    return request.getHealthConnectUuid() == null ||
                            !existingUuids.contains(request.getHealthConnectUuid());
                })
                .map(request -> createExercise(userId, request))
                .collect(Collectors.toList());
    }

    // 운동 기록 수정
    @Transactional
    public ExerciseListResponse updateExercise(Integer userId, Integer exerciseId, ExerciseUpdateRequest request) {
        // 1. 운동 기록 조회 (해당 사용자 확인)
        Exercise exercise = exerciseRepository.findByExerciseIdAndUserId(exerciseId, userId)
                .orElseThrow(() -> new NotFoundException("해당 사용자의 운동 기록을 찾을 수 없습니다."));

        // 삭제된 운동 기록인 경우 예외
        if (exercise.getIsDeleted()) {
            throw new BadRequestException("삭제된 운동 기록입니다.");
        }

        // 2. 기존 운동 종류 정보 조회 (exerciseTypeId는 변경하지 않음)
        ExerciseType exerciseType = exerciseTypeRepository.findById(exercise.getExerciseTypeId())
                .orElseThrow(() -> new NotFoundException("존재하지 않는 운동 종류입니다."));

        // 3. 칼로리 계산
        Integer totalCalorie;
        if (request.getExerciseCalorie() != null) {
            totalCalorie = request.getExerciseCalorie();
        } else {
            totalCalorie = exerciseType.getExerciseCalorie() * request.getExerciseTime();
        }

        Exercise updatedExercise = Exercise.builder()
                .exerciseId(exercise.getExerciseId())
                .userId(exercise.getUserId())  // 기존 userId 유지
                .exerciseTypeId(exercise.getExerciseTypeId())  // 기존 exerciseTypeId 유지 (변경하지 않음)
                .exerciseDate(request.getExerciseDate())
                .exerciseTime(request.getExerciseTime())
                .exerciseCalorie(totalCalorie)
                .createdAt(exercise.getCreatedAt())
                .updatedAt(LocalDateTime.now())
                .deletedAt(exercise.getDeletedAt())
                .isDeleted(exercise.getIsDeleted())
                .build();

        // 4. 변경 사항 저장
        Exercise savedExercise = exerciseRepository.save(updatedExercise);

        // 5. 카테고리 정보 조회
        ExerciseCategory exerciseCategory = exerciseCategoryRepository.findById(exerciseType.getExerciseCategoryId())
                .orElse(null);

        // 6. 매퍼를 사용하여 응답 DTO 생성 및 반환
        return exerciseMapper.toListResponse(savedExercise, exerciseType, exerciseCategory, totalCalorie);
    }

    // 운동 기록 삭제
    @Transactional
    public void deleteExercise(Integer userId, Integer exerciseId) {
        // 1. 운동 기록 조회 (해당 사용자 확인)
        Exercise exercise = exerciseRepository.findByExerciseIdAndUserId(exerciseId, userId)
                .orElseThrow(() -> new NotFoundException("해당 사용자의 운동 기록을 찾을 수 없습니다."));

        // 이미 삭제된 운동 기록인 경우 예외 발생
        if (exercise.getIsDeleted()) {
            throw new BadRequestException("이미 삭제된 운동 기록입니다.");
        }

        // 2. 소프트 삭제 처리
        Exercise deletedExercise = Exercise.builder()
                .exerciseId(exercise.getExerciseId())
                .userId(exercise.getUserId())
                .exerciseTypeId(exercise.getExerciseTypeId())
                .exerciseDate(exercise.getExerciseDate())
                .exerciseTime(exercise.getExerciseTime())
                .exerciseCalorie(exercise.getExerciseCalorie())
                .createdAt(exercise.getCreatedAt())
                .updatedAt(LocalDateTime.now())
                .deletedAt(LocalDateTime.now())
                .isDeleted(true)
                .build();

        // 3. 운동 기록 저장
        exerciseRepository.save(deletedExercise);
    }

    // 운동 카테고리 전체 조회
    @Transactional(readOnly = true)
    public List<ExerciseCategoryResponse> getAllCategories() {
        List<ExerciseCategory> categories = exerciseCategoryRepository.findByIsDeletedFalse();

        return categories.stream()
                .map(category -> ExerciseCategoryResponse.builder()
                        .exerciseCategoryId(category.getExerciseCategoryId())
                        .exerciseCategoryName(category.getExerciseCategoryName())
                        .createdAt(category.getCreatedAt())
                        .build())
                .collect(Collectors.toList());
    }

    // 카테고리별 운동 조회
    @Transactional(readOnly = true)
    public List<ExerciseTypeResponse> getExercisesByCategory(Integer exerciseCategoryId, Integer userId) {
        // 해당 카테고리의 운동 타입 조회
        List<ExerciseType> exerciseTypes = exerciseTypeRepository
                .findByExerciseCategoryIdAndIsDeletedFalse(exerciseCategoryId);

        // 사용자의 즐겨찾기 목록 조회
        List<FavoriteExercise> favorites = favoriteExerciseRepository.findAllByUserId(userId);

        // 즐겨찾기한 운동 타입 ID 목록 생성
        Set<Integer> favoriteExerciseTypeIds = favorites.stream()
                .map(favorite -> favorite.getExerciseType().getExerciseTypeId())
                .collect(Collectors.toSet());

        return exerciseTypes.stream()
                .map(exerciseType -> ExerciseTypeResponse.builder()
                        .exerciseTypeId(exerciseType.getExerciseTypeId())
                        .exerciseCategoryId(exerciseType.getExerciseCategoryId())
                        .exerciseNumber(exerciseType.getExerciseNumber())
                        .exerciseName(exerciseType.getExerciseName())
                        .exerciseEnglishName(exerciseType.getExerciseEnglishName())
                        .exerciseCalorie(exerciseType.getExerciseCalorie())
                        .createdAt(exerciseType.getCreatedAt())
                        .isFavorite(favoriteExerciseTypeIds.contains(exerciseType.getExerciseTypeId()))
                        .build())
                .collect(Collectors.toList());
    }

    // 특정 운동 상세 조회
    @Transactional(readOnly = true)
    public ExerciseTypeDetailResponse getExerciseTypeDetail(Integer exerciseNumber, Integer userId) {
        // 1. 운동 타입 정보 조회
        ExerciseType exerciseType = exerciseTypeRepository.findByExerciseNumberAndIsDeletedFalse(exerciseNumber)
                .orElseThrow(() -> new NotFoundException("존재하지 않는 운동 입니다."));

        // 2. 운동 카테고리 정보 조회
        ExerciseCategory exerciseCategory = exerciseCategoryRepository.findById(exerciseType.getExerciseCategoryId())
                .orElseThrow(() -> new NotFoundException("존재하지 않는 운동 카테고리입니다."));

        // 3. 즐겨찾기 여부 확인
        boolean isFavorite = favoriteExerciseRepository
                .findByUserIdAndExerciseTypeExerciseTypeId(userId, exerciseType.getExerciseTypeId())
                .isPresent();

        // 4. 응답 DTO
        return ExerciseTypeDetailResponse.fromEntity(
                exerciseType,
                exerciseCategory.getExerciseCategoryName(),
                isFavorite
        );
    }

    // 최근에 한 운동 조회
    @Transactional(readOnly = true)
    public List<ExerciseTypeResponse> getLatestExercises(Integer userId, int limit) {
        // 1. 사용자의 모든 운동 기록을 내림차순 조회
        List<Exercise> allExercises = exerciseRepository.findByUserIdAndIsDeletedFalseOrderByExerciseDateDesc(userId);

        // 2. exerciseTypeId를 기준으로 중복 제거하여 최신 운동 타입 ID 목록 추출
        List<Integer> uniqueExerciseTypeIds = allExercises.stream()
                .map(Exercise::getExerciseTypeId)
                .distinct()
                .limit(limit)
                .collect(Collectors.toList());

        // 3. 해당 운동 타입 정보 가져오기
        List<ExerciseType> exerciseTypes = exerciseTypeRepository.findAllById(uniqueExerciseTypeIds);

        // 4. 응답 DTO 생성
        Map<Integer, ExerciseTypeResponse> responseMap = new LinkedHashMap<>();

        // 운동 타입 ID 목록을 순회하며 응답 객체 생성
        for (Integer typeId : uniqueExerciseTypeIds) {
            // 운동 타입 찾기
            exerciseTypes.stream()
                    .filter(type -> type.getExerciseTypeId().equals(typeId))
                    .findFirst() // 일치하는 첫 번째 항목 선택
                    .ifPresent(exerciseType -> { // 해당 운동 타입이 존재하는 경우에만 처리
                        ExerciseTypeResponse response = ExerciseTypeResponse.builder()
                                .exerciseTypeId(exerciseType.getExerciseTypeId())
                                .exerciseCategoryId(exerciseType.getExerciseCategoryId())
                                .exerciseName(exerciseType.getExerciseName())
                                .exerciseEnglishName(exerciseType.getExerciseEnglishName())
                                .exerciseCalorie(exerciseType.getExerciseCalorie())
                                .exerciseNumber(exerciseType.getExerciseNumber())
                                .createdAt(exerciseType.getCreatedAt())
                                .build();

                        // 생성된 응답 객체를 맵에 저장 (키: 운동 타입 ID)
                        responseMap.put(typeId, response);
                    });
        }

        return new ArrayList<>(responseMap.values());
    }
}
