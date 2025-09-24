# DB 설계 (요약)

> 실제 스키마는 `src/main/resources/static/sql/focus.sql` 참조

## 주요 테이블

### `anonymous_user`
| Column        | Type        | Note                       |
|---------------|-------------|----------------------------|
| anon_id (PK)  | CHAR(26)    | ULID, 사용자 식별자        |
| nickname      | VARCHAR(24) | 닉네임(옵션)               |
| nickname_tag  | CHAR(4)     | 닉네임 태그(중복 방지)     |
| created_at    | DATETIME    | 생성 시 자동 기록 (DEFAULT CURRENT_TIMESTAMP)|
| updated_at    | DATETIME    | 수정 시 자동 반영 (ON UPDATE CURRENT_TIMESTAMP) |

### `focus_session`
| Column        | Type      | Note                                  |
|---------------|-----------|---------------------------------------|
| session_id PK | BIGINT    |                                       |
| anon_id (FK)  | CHAR(26)  | → anonymous_user                      |
| started_at    | DATETIME  | 입금 시작                              |
| ended_at      | DATETIME  | 정산 완료 (null=진행중)                |
| duration_sec  | INT       | 종료 시 계산                           |
| created_at    | DATETIME  |생성 시 자동 기록 (DEFAULT CURRENT_TIMESTAMP)|

인덱스: `(anon_id, started_at)` 등

### `daily_aggregate`
| Column        | Type      | Note                       |
|---------------|-----------|----------------------------|
| anon_id (PK)  | CHAR(26)  |                            |
| target_datePK | DATE      | YYYY-MM-DD                 |
| total_sec     | BIGINT    | 해당 날짜 총 초             |
| created_at    | DATETIME  |생성 시 자동 기록 (DEFAULT CURRENT_TIMESTAMP)|

### `user_goal`
| Column         | Type      | Note                                         |
|----------------|-----------|----------------------------------------------|
| goal_id (PK)   | BIGINT    |                                              |
| anon_id (FK)   | CHAR(26)  | → anonymous_user                             |
| period_type    | CHAR(7)   | DAILY/WEEKLY/MONTHLY                         |
| target_seconds | INT       | 목표(초)                                     |
| effective_from | DATE      | 적용 시작일                                  |
| created_at     | DATETIME  |생성 시 자동 기록 (DEFAULT CURRENT_TIMESTAMP) |

Unique: `(anon_id, period_type, effective_from)` <br/>

-> 사용자가 같은 기간에 중복 목표를 못 넣도록 보장
---

## 관계
- `focus_session.anon_id` → `anonymous_user.anon_id`
- `user_goal.anon_id`     → `anonymous_user.anon_id`

집계 전략:
- 세션 종료 시(`settle`) 트리거 서비스에서 `daily_aggregate` 갱신
- 리포트는 집계 테이블 기반으로 빠르게 조회
