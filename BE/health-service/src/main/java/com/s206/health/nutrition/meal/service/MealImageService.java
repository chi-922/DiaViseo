package com.s206.health.nutrition.meal.service;

import io.minio.*;
import io.minio.http.Method;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@Service
public class MealImageService {

    @Autowired
    private MinioClient minioClient;

    @Value("${spring.minio.endpoint}")
    private String endpoint;

    @Value("${spring.minio.external-endpoint:#{null}}")
    private String externalEndpoint;

    @Value("${spring.minio.bucket.name}")
    private String bucketName;

    // 버킷이 존재하는지 확인하고 없으면 생성
    public void checkBucket() {
        try {
            boolean bucketExists = minioClient.bucketExists(BucketExistsArgs.builder()
                    .bucket(bucketName)
                    .build());
            if (!bucketExists) {
                minioClient.makeBucket(MakeBucketArgs.builder()
                        .bucket(bucketName)
                        .build());
            }
        } catch (Exception e) {
            throw new RuntimeException("버킷 생성 중 오류가 발생했습니다 :" + e.getMessage(), e);
        }
    }

    // 식단 이미지를 업로드
    public String uploadMealImage(MultipartFile file) {
        checkBucket();

        try {
            // 유니크한 파일명 생성 (충돌 방지)
            String originalFilename = file.getOriginalFilename();
            String extension = originalFilename != null && originalFilename.contains(".") ?
                    originalFilename.substring(originalFilename.lastIndexOf(".")) : ".jpg";
            String objectName = "meal/" + UUID.randomUUID() + extension;

            // 파일 확장자에 따른 MIME 타입 결정
            String contentType;
            if (extension.equalsIgnoreCase(".jpg") || extension.equalsIgnoreCase(".jpeg")) {
                contentType = "image/jpeg";
            } else if (extension.equalsIgnoreCase(".png")) {
                contentType = "image/png";
            } else if (extension.equalsIgnoreCase(".gif")) {
                contentType = "image/gif";
            } else if (extension.equalsIgnoreCase(".webp")) {
                contentType = "image/webp";
            } else {
                contentType = file.getContentType();
            }

            // MinIO에 파일 업로드 (명시적 Content-Type 설정)
            minioClient.putObject(PutObjectArgs.builder()
                    .bucket(bucketName)
                    .object(objectName)
                    .stream(file.getInputStream(), file.getSize(), -1)
                    .contentType(contentType != null ? contentType : "image/jpeg") // 기본값 설정
                    .build());

            return objectName;
        } catch (Exception e) {
            throw new RuntimeException("이미지 업로드 중 오류가 발생했습니다: " + e.getMessage(), e);
        }
    }


    // 이미지 URL 생성
    public String getMealImageUrl(String objectName) {
        if (objectName == null || objectName.isEmpty()) {
            return null;
        }

        // meal/ 접두사 자동 추가
        if (!objectName.startsWith("meal/")) {
            objectName = "meal/" + objectName;
        }

        // 외부 엔드포인트가 설정된 경우 해당 값 사용, 아니면 기본 엔드포인트 사용
        String baseUrl = externalEndpoint != null && !externalEndpoint.isEmpty() ? externalEndpoint : endpoint;

        // 슬래시 처리
        if (baseUrl.endsWith("/")) {
            baseUrl = baseUrl.substring(0, baseUrl.length() - 1);
        }

        // 직접 URL 생성
        return String.format("%s/%s/%s", baseUrl, bucketName, objectName);
    }


    // 이미지 파일을 삭제
    public void deleteMealImage(String objectName) {
        if (objectName == null || objectName.isEmpty()) {
            return; // 빈 URL이면 무시
        }

        try {
            minioClient.removeObject(RemoveObjectArgs.builder()
                    .bucket(bucketName)
                    .object(objectName)
                    .build());
        } catch (Exception e) {
            throw new RuntimeException("이미지 삭제 중 오류가 발생했습니다: " + e.getMessage(), e);
        }
    }
}
