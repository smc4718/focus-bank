# 📑 API 명세서

> 모든 요청은 필요 시 `X-ANON-ID: <ULID>` 헤더를 포함합니다.  
> 응답은 JSON 형식이며, 성공/실패 시 적절한 HTTP 상태 코드를 반환합니다.

---

## 세션 (Session)

### POST `/api/sessions/deposit`
세션 시작(입금). 진행 중 세션이 있으면 `409 Conflict`.

**Headers**
- `X-ANON-ID`: 익명 사용자 ID

**Response 200**
```json
{
  "sessionId": 123,
  "anonId": "01HW...",
  "startedAt": "2025-09-24T10:12:00",
  "endedAt": null,
  "durationSec": null,
  "createdAt": "2025-09-24T10:12:00"
}
```

**Response 409**
```json
{ "error": "conflict", "message": "이미 진행 중인 세션이 있습니다." }
```

---

### POST `/api/sessions/settle?sessionId={id}`
세션 종료(정산).

**Response 200**
```json
{
  "sessionId": 123,
  "anonId": "01HW...",
  "startedAt": "2025-09-24T10:12:00",
  "endedAt": "2025-09-24T10:42:30",
  "durationSec": 1830,
  "createdAt": "2025-09-24T10:12:00"
}
```

**Response 409**
```json
{ "error": "conflict", "message": "이미 종료된 세션입니다." }
```

---

### GET `/api/sessions/active`
진행 중 세션 조회. 없으면 `204 No Content`.

**Response 200**
```json
{
  "sessionId": 123,
  "anonId": "01HW...",
  "startedAt": "2025-09-24T10:12:00",
  "endedAt": null,
  "durationSec": null,
  "createdAt": "2025-09-24T10:12:00"
}
```

---

### GET `/api/sessions?date=YYYY-MM-DD`
해당 날짜의 세션 목록.

**Response 200**
```json
[
  { "sessionId": 1, "startedAt": "...", "endedAt": "...", "durationSec": 1200 },
  { "sessionId": 2, "startedAt": "...", "endedAt": null }
]
```

---

## 리포트 (Report)

### GET `/api/reports/summary?date=YYYY-MM-DD`
일별 합계 조회.

**Response**
```json
{ "targetDate": "2025-09-24", "totalSec": 5400 }
```

### GET `/api/reports/weekly?weeks=N`
주간 집계(N주).

**Response**
```json
[
  { "period": "2025-W38", "totalSeconds": 7200, "dayCount": 3, "avgSecPerDay": 2400 }
]
```

### GET `/api/reports/monthly?months=N`
월간 집계(N개월).

**Response**
```json
[
  { "period": "2025-09", "totalSeconds": 14400, "dayCount": 10, "avgSecPerDay": 1440 }
]
```

---

## 목표 (Goal)

### POST `/api/goals`
목표 저장(업서트).

**Headers**
- `X-ANON-ID`

**Request Body**
```json
{ "periodType": "DAILY", "targetSeconds": 7200, "effectiveFrom": "2025-09-24" }
```

**Response 201/200**
```json
{ "goalId": 3, "anonId": "01HW...", "periodType": "DAILY", "targetSeconds": 7200, "effectiveFrom": "2025-09-24" }
```

---

### GET `/api/goals/current?period=DAILY|WEEKLY|MONTHLY`
오늘 기준 활성 목표 조회.

**Response**
```json
{ "goalId": 3, "anonId": "01HW...", "periodType": "DAILY", "targetSeconds": 7200, "effectiveFrom": "2025-09-24" }
```

---

### GET `/api/goals/progress?period=daily|weekly|monthly`
목표 진행률 조회.

**Response**
```json
{ "targetSeconds": 7200, "achievedSeconds": 3600, "progress": 0.5 }
```

---

## 랭킹 (Ranking)

### GET `/api/rankings/weekly?limit=10`
주간 랭킹 TOP N.

**Response**
```json
[ { "rank": 1, "anonId": "01HW...", "displayName": "닉#1234", "seconds": 14400 } ]
```

---

### GET `/api/rankings/overall?limit=10`
전체 누적 랭킹 TOP N.

**Response**
```json
[ { "rank": 1, "anonId": "01HW...", "displayName": "닉#1234", "seconds": 360000 } ]
```

---

## 프로필 (Profile)

### GET `/api/profile`
프로필 조회.

**Response**
```json
{
  "anonId": "01HW...",
  "nickname": "집중장인",
  "nicknameTag": "0420",
  "createdAt": "...",
  "updatedAt": "..."
}
```

---

### POST `/api/profile/nickname`
닉네임 저장.

**Request**
```json
{ "nickname": "집중장인" }
```

**Response**
```json
{
  "anonId": "01HW...",
  "nickname": "집중장인",
  "nicknameTag": "0420",
  "createdAt": "...",
  "updatedAt": "..."
}
```
