# Alert Service API 명세

**Alert Service는 사용자에게 운동·식단 관련 정보를 푸시 알림 형태로 전달하는 마이크로서비스입니다.**

---

## 📌 API 개요

### Base URL
```
http://alert-service/api
```

### Alert Service의 역할
- **FCM 토큰 관리**: 사용자 디바이스의 Firebase Cloud Messaging 토큰 등록 및 갱신
- **알림 조회 및 관리**: 사용자의 알림 읽음 처리 및 삭제
- **알림 전송**: MQ 기반의 비동기 푸시 알림 발송 처리
- **알림 저장**: 알림 이력 저장 및 상태 업데이트

### 공통 응답 형식
모든 API는 Common Module의 `ResponseDto` 형식을 사용합니다.

```json
{
  "timestamp": "2025-05-28T13:00:00",
  "status": "OK", 
  "message": "성공 메시지",
  "data": { /* 실제 데이터 */ }
}
```

### 공통 HTTP 상태코드
| 상태코드 | 설명 | 응답 예시 |
|---------|------|----------|
| **200** | 성공 | `"message": "요청 성공"` |
| **400** | 잘못된 요청 | `"message": "필수 파라미터가 누락되었습니다."` |
| **401** | 인증 실패 | `"message": "유효하지 않은 토큰입니다."` |
| **500** | 서버 오류 | `"message": "서버 오류가 발생했습니다."` |

---

## 📲 1. FCM 토큰 등록 API

#### `POST /api/users/fcm-token`

사용자의 FCM(Firebase Cloud Messaging) 토큰을 등록하거나 갱신합니다.

**Headers**
```http
Authorization: Bearer {accessToken}
Content-Type: application/json
```

**Body**
```json
{
  "token": "dgl42ys1csj...DeviceToken"
}
```

**Response**
```json
{
  "timestamp": "2025-05-28T13:00:00",
  "status": "OK",
  "message": "FCM 토큰 등록 성공",
  "data": null
}
```

**에러 응답**
| 상태 코드 | 에러 코드 | 설명 |
|-----------|-----------|------|
| **400**   | BAD_REQUEST | 요청 본문이 유효하지 않은 경우 |
| **401**   | UNAUTHORIZED | 인증되지 않은 사용자 |
| **500**   | INTERNAL_SERVER_ERROR | 서버 내부 오류 발생 |

---

## 📬 2. 알림 목록 조회 API

#### `GET /api/alert`

사용자의 알림 목록을 페이징 처리하여 조회합니다.

**Headers**
```http
Authorization: Bearer {accessToken}
```

**Query Parameters**

| 파라미터 | 타입     | 필수 | 기본값       | 설명                         |
|----------|----------|------|--------------|------------------------------|
| `page`   | Integer  | ❌   | `0`          | 페이지 번호 (0부터 시작)     |
| `size`   | Integer  | ❌   | `20`         | 한 페이지당 항목 수          |
| `sort`   | String   | ❌   | `sentAt,desc`| 정렬 기준 (필드, 방향)       |

**Response**
```json
{
  "timestamp": "2025-05-28T13:00:00",
  "status": "OK",
  "message": "알림 목록 조회 성공",
  "data": {
    "content": [...],
    "pageable": {...},
    "totalPages": 1,
    "totalElements": 4,
    ...
  }
}
```

---

## ✅ 3. 단건 알림 읽음 처리 API

#### `PATCH /api/alert/{notificationId}/read`

특정 알림을 읽음 처리합니다.

**Headers**
```http
Authorization: Bearer {accessToken}
```

**Path Params**
- `notificationId`: 읽음 처리할 알림 ID

**Response**
```json
{
  "timestamp": "2025-05-15T00:48:36.604377",
  "status": "OK",
  "message": "읽음 처리 됐습니다",
  "data": null
}
```

**에러 응답**
| 상태 코드 | 메시지 | 설명 |
|-----------|--------|------|
| **403**   | 해당 알림에 접근 권한이 없습니다 | 다른 사용자의 알림 접근 |
| **404**   | 알림을 찾을 수 없습니다 | 알림이 존재하지 않음 |

---

## ✅ 4. 전체 알림 읽음 처리 API

#### `PATCH /api/alert/read-all`

사용자의 모든 읽지 않은 알림을 읽음 처리합니다.

**Headers**
```http
Authorization: Bearer {accessToken}
```

**Response**
```json
{
  "timestamp": "2025-05-15T00:59:58.875251",
  "status": "OK",
  "message": "읽음 처리 됐습니다",
  "data": null
}
```

---

## 🗑️ 5. 단건 알림 삭제 API

#### `DELETE /api/alert/{notificationId}/delete`

특정 알림을 삭제합니다. 사용자 본인의 알림만 삭제할 수 있습니다.

**Headers**
```http
Authorization: Bearer {accessToken}
```

**Path Params**
- `notificationId`: 삭제할 알림 ID

**Response**
```json
{
  "timestamp": "2025-05-15T09:37:24.3718851",
  "status": "OK",
  "message": "알림이 삭제되었습니다",
  "data": null
}
```

**에러 응답**
- 403 Forbidden
```json
{
  "status": "FORBIDDEN",
  "message": "다른 사용자의 알림입니다.",
  "timestamp": "2023-08-10T14:30:45.123"
}
```

- 404 Not Found
```json
{
  "status": "NOT_FOUND",
  "message": "알림이 존재하지 않습니다.",
  "timestamp": "2023-08-10T14:30:45.123"
}
```
