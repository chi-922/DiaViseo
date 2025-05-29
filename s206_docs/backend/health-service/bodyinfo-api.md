# BodyInfo API 명세

**Health Service 내 BodyInfo 모듈은 체성분 정보 관리, OCR 자동 입력, 신체 변화 추적을 담당하는 서비스입니다.**

---

## 📌 API 개요

### Base URL
```
http://health-service/api/bodies
```

### BodyInfo 모듈의 역할
- **체성분 정보 관리**: 체중, 체지방률, 근육량, 신장 정보 CRUD
- **OCR 자동 입력**: Tesseract 기반 인바디 결과지 자동 인식
- **BMI/BMR 계산**: 해리스-베네딕트 공식 기반 신체지수 계산
- **변화 추적**: 일별/주별/월별 체성분 변화 통계
- **User Service 연동**: 체성분 변경 시 자동 동기화

### 공통 응답 형식
```json
{
  "timestamp": "2025-05-26T10:30:00",
  "status": "OK", 
  "message": "성공 메시지",
  "data": { /* 실제 데이터 */ }
}
```

### 공통 HTTP 상태코드
| 상태코드 | 설명 | 응답 예시 |
|---------|------|----------|
| **200** | 성공 | `"message": "요청 성공"` |
| **201** | 생성 성공 | `"message": "체성분 데이터 등록 성공"` |
| **400** | 잘못된 요청 | `"message": "체중은 양수여야 합니다."` |
| **404** | 데이터 없음 | `"message": "해당 날짜의 체성분 정보가 없습니다."` |
| **408** | 타임아웃 | `"message": "처리 시간이 초과되었습니다."` |
| **500** | 서버 오류 | `"message": "서버 처리 중 오류가 발생했습니다."` |

---

## 🏥 체성분 정보 관리 API

### 1. 체성분 정보 등록

#### `POST /api/bodies`

**Request Headers**
```http
X-USER-ID: {userId}
Content-Type: application/json
```

**Request Body**
```json
{
  "weight": 70.5,
  "bodyFat": 15.2,
  "muscleMass": 32.1,
  "height": 175.0,
  "measurementDate": "2025-05-26"
}
```

**처리 과정**
1. **입력값 검증**: 체중(양수), 체지방률(0-100%), 근육량(0-1000kg) 등
2. **BodyInfo 엔티티 생성**: inputType = MANUAL로 설정
3. **BMI/BMR 계산**: User Service에서 사용자 정보 조회 후 계산
4. **User Service 동기화**: 체성분 정보 자동 전송

**Response (성공)**
```json
{
  "timestamp": "2025-05-26T10:30:00",
  "status": "CREATED",
  "message": "체성분 데이터 등록 성공",
  "data": {
    "bodyId": 1,
    "userId": 123,
    "height": 175.0,
    "weight": 70.5,
    "bodyFat": 15.2,
    "muscleMass": 32.1,
    "measurementDate": "2025-05-26",
    "bmi": 23.02,
    "bmr": 1687.5,
    "createdAt": "2025-05-26T10:30:00"
  }
}
```

---

### 2. 체성분 정보 조회 (최신)

특정 날짜 이전의 가장 최근 체성분 정보를 조회합니다.

#### `GET /api/bodies`

**Query Parameters**
| 파라미터 | 타입 | 필수 | 기본값 | 설명 |
|---------|------|------|-------|------|
| `date` | LocalDate | ❌ | 오늘 | 조회 기준 날짜 (YYYY-MM-DD) |

**Response (성공)**
```json
{
  "timestamp": "2025-05-26T15:00:00",
  "status": "OK",
  "message": "유저 체성분 정보 목록 요청이 성공적으로 반환 처리됐습니다.",
  "data": [
    {
      "bodyId": 1,
      "userId": 123,
      "height": 175.0,
      "weight": 70.5,
      "bodyFat": 15.2,
      "muscleMass": 32.1,
      "measurementDate": "2025-05-26",
      "bmi": 23.02,
      "bmr": 1687.5,
      "createdAt": "2025-05-26T10:30:00"
    }
  ]
}
```

**Response (데이터 없음 - 초기 응답)**
```json
{
  "data": [
    {
      "bodyId": null,
      "userId": 123,
      "height": 175.0,
      "weight": 70.0,
      "bodyFat": 0.0,
      "muscleMass": 0.0,
      "measurementDate": null,
      "bmi": 22.86,
      "bmr": 1650.0,
      "createdAt": null
    }
  ]
}
```

---

### 3. 특정 날짜 체성분 조회

#### `GET /api/bodies/date`

**Query Parameters**
| 파라미터 | 타입 | 필수 | 설명 |
|---------|------|------|------|
| `date` | LocalDate | ✅ | 조회할 날짜 (YYYY-MM-DD) |

**Response (성공)**
```json
{
  "timestamp": "2025-05-26T16:00:00",
  "status": "OK",
  "message": "유저 체성분 정보 조회가 성공적으로 처리됐습니다.",
  "data": {
    "bodyId": 1,
    "userId": 123,
    "height": 175.0,
    "weight": 70.5,
    "bodyFat": 15.2,
    "muscleMass": 32.1,
    "measurementDate": "2025-05-26",
    "bmi": 23.02,
    "bmr": 1687.5,
    "createdAt": "2025-05-26T10:30:00"
  }
}
```

---

### 4. 체성분 정보 수정

#### `PATCH /api/bodies/{bodyId}`

**Request Body**
```json
{
  "weight": 69.8,
  "bodyFat": 14.5,
  "muscleMass": 33.2,
  "height": 175.0,
  "measurementDate": "2025-05-26"
}
```

**Response (성공)**
```json
{
  "timestamp": "2025-05-26T17:00:00",
  "status": "OK",
  "message": "체성분 정보가 성공적으로 수정되었습니다.",
  "data": {
    "bodyId": 1,
    "userId": 123,
    "height": 175.0,
    "weight": 69.8,
    "bodyFat": 14.5,
    "muscleMass": 33.2,
    "measurementDate": "2025-05-26",
    "bmi": 22.81,
    "bmr": 1675.3,
    "createdAt": "2025-05-26T10:30:00"
  }
}
```

---

### 5. 체성분 정보 삭제

#### `DELETE /api/bodies/{bodyId}`

**Response (성공)**
```json
{
  "timestamp": "2025-05-26T18:00:00",
  "status": "OK",
  "message": "체성분 정보가 성공적으로 삭제되었습니다.",
  "data": null
}
```

---

## 📷 OCR 자동 입력 API

### 6. OCR 데이터 추출

인바디 결과지 이미지에서 체성분 데이터를 자동 추출합니다.

#### `POST /api/bodies/ocr` (multipart/form-data)

**Request Parameters**
```
image (File): 인바디 결과지 이미지 파일
```

**처리 과정**
1. **이미지 업로드**: MultipartFile 수신
2. **비동기 OCR 처리**: Tesseract 엔진으로 텍스트 추출 (30초 타임아웃)
3. **데이터 파싱**: 정규식으로 체중, 체지방률, 근육량, 키, 측정일 추출
4. **결과 반환**: BodyInfoCreateRequest 형태로 반환

**Response (성공)**
```json
{
  "timestamp": "2025-05-26T19:00:00",
  "status": "OK",
  "message": "OCR 정보 추출 성공",
  "data": {
    "weight": 70.5,
    "bodyFat": 15.2,
    "muscleMass": 32.1,
    "height": 175.0,
    "measurementDate": "2025-05-26"
  }
}
```

**에러 응답**
| 상태코드 | 에러 케이스 | 응답 메시지 |
|---------|-----------|-----------|
| **408** | 처리 시간 초과 | `"처리 시간이 초과되었습니다."` |
| **400** | 이미지 처리 실패 | `"이미지 처리 중 오류가 발생했습니다."` |

---

### 7. OCR 데이터 확인 후 저장

OCR 결과를 확인/수정한 후 최종 저장합니다.

#### `POST /api/bodies/ocr/confirm`

**Request Body**
```json
{
  "weight": 70.5,
  "bodyFat": 15.2,
  "muscleMass": 32.1,
  "height": 175.0,
  "measurementDate": "2025-05-26"
}
```

**Response (성공)**
```json
{
  "timestamp": "2025-05-26T20:00:00",
  "status": "CREATED",
  "message": "OCR 데이터 확인 후 체성분 데이터 등록 성공",
  "data": {
    "bodyId": 2,
    "userId": 123,
    "height": 175.0,
    "weight": 70.5,
    "bodyFat": 15.2,
    "muscleMass": 32.1,
    "measurementDate": "2025-05-26",
    "bmi": 23.02,
    "bmr": 1687.5,
    "createdAt": "2025-05-26T20:00:00"
  }
}
```

---

## 📊 변화 추적 및 통계 API

### 8. 주간 체성분 데이터

최근 7일간 일별 체성분 변화를 조회합니다.

#### `GET /api/bodies/weekly`

**Query Parameters**
| 파라미터 | 타입 | 필수 | 기본값 | 설명 |
|---------|------|------|-------|------|
| `endDate` | LocalDate | ❌ | 오늘 | 종료 날짜 |

**Response (성공)**
```json
{
  "timestamp": "2025-05-26T21:00:00",
  "status": "OK",
  "message": "주간 체성분 정보 조회가 성공적으로 처리됐습니다.",
  "data": [
    {
      "measurementDate": "2025-05-20",
      "weight": 71.2,
      "muscleMass": 31.8,
      "bodyFat": 15.5,
      "height": 175.0
    },
    {
      "measurementDate": "2025-05-21",
      "weight": 0.0,
      "muscleMass": 0.0,
      "bodyFat": 0.0,
      "height": 0.0
    },
    {
      "measurementDate": "2025-05-26",
      "weight": 70.5,
      "muscleMass": 32.1,
      "bodyFat": 15.2,
      "height": 175.0
    }
  ]
}
```

---

### 9. 주별 평균 체성분

최근 7주간 주별 평균 체성분을 조회합니다.

#### `GET /api/bodies/weekly-avg`

**Response (성공)**
```json
{
  "timestamp": "2025-05-26T22:00:00",
  "status": "OK",
  "message": "7주간 주별 평균 체성분 정보 조회가 성공적으로 처리됐습니다.",
  "data": [
    {
      "weekLabel": 1,
      "avgWeight": 71.50,
      "avgMuscleMass": 31.20,
      "avgBodyFat": 16.10,
      "avgHeight": 175.00
    },
    {
      "weekLabel": 2,
      "avgWeight": 71.20,
      "avgMuscleMass": 31.50,
      "avgBodyFat": 15.80,
      "avgHeight": 175.00
    },
    {
      "weekLabel": 7,
      "avgWeight": 70.50,
      "avgMuscleMass": 32.10,
      "avgBodyFat": 15.20,
      "avgHeight": 175.00
    }
  ]
}
```

---

### 10. 월별 평균 체성분

최근 7개월간 월별 평균 체성분을 조회합니다.

#### `GET /api/bodies/monthly-avg`

**Response (성공)**
```json
{
  "timestamp": "2025-05-26T23:00:00",
  "status": "OK",
  "message": "7개월간 월별 평균 체성분 정보 조회가 성공적으로 처리됐습니다.",
  "data": [
    {
      "monthIndex": 1,
      "avgWeight": 72.80,
      "avgMuscleMass": 30.50,
      "avgBodyFat": 17.20,
      "avgHeight": 175.00
    },
    {
      "monthIndex": 7,
      "avgWeight": 70.30,
      "avgMuscleMass": 32.40,
      "avgBodyFat": 14.80,
      "avgHeight": 175.00
    }
  ]
}
```

---

## 🔧 주요 특징

### OCR 처리 특징
- **Tesseract 4.0+ 엔진**: 고성능 OCR 처리
- **비동기 처리**: 30초 타임아웃으로 안정성 확보
- **정규식 파싱**: 체중, 체지방률, 근육량, 키, 측정일 자동 추출
- **OS별 최적화**: Windows/Linux 환경별 자동 경로 설정

### 계산 공식
```
BMI = 체중(kg) ÷ 신장(m)²

BMR (남성) = 88.362 + (13.397 × 체중) + (4.799 × 신장) - (5.677 × 나이)
BMR (여성) = 447.593 + (9.247 × 체중) + (3.098 × 신장) - (4.330 × 나이)
```

### 데이터 검증
- **체중**: 0-500kg
- **체지방률**: 0-100%
- **근육량**: 0-1000kg
- **신장**: 0-1000cm
- **측정일**: 현재 또는 과거 날짜만 허용

### 통계 처리
- **빈 데이터 처리**: 데이터가 없는 날짜는 0으로 채움
- **0값 제외 평균**: 실제 측정값만으로 평균 계산
- **소수점 처리**: BigDecimal 사용으로 정확한 계산