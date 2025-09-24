# Focus Bank — 디지털 집중력 은행

집중 시간을 **입금(시작)** / **정산(종료)** 하여 누적하고, 
**목표 진행률**, **일/주/월 리포트**, **랭킹**, **QR로 세션 연동**을 제공하는 웹 애플리케이션입니다.

---

## ✨ 주요 기능
- **세션 기록**
  - 입금(시작) / 정산(종료)로 집중 시간 기록
  - 진행 중 세션 실시간 타이머 표시
- **리포트**
  - 오늘 합계, 주/월 합계
  - 각 회차(오늘) 막대 차트
- **목표 관리**
  - 일간/주간/월간 목표 설정(업서트)
  - 목표 진행률 조회
- **랭킹**
  - 주간 / 전체 누적 TOP N
- **프로필(익명)**
  - `X-ANON-ID` 기반, 닉네임 + 태그 지원
- **QR 코드로 세션 연동**
  - 현재 기기의 익명 ID가 포함된 URL을 QR로 생성 → 다른 기기에서 **동일 세션** 이어서 사용

---

## 🧱 기술 스택
- **Backend**: Java 17, Spring Boot 3.x, MyBatis
- **DB**: MariaDB
- **Frontend**: Thymeleaf + Vanilla JS (Chart.js)
- **Build**: Gradle
- **기타**: Lombok, HikariCP

> 실제 버전은 `build.gradle`, `application.yml`에 정의되어 있습니다.

---

## 📁 폴더 구조 (요약)
```
focus-bank/
  ├─ build.gradle
  ├─ src/
  │  ├─ main/
  │  │  ├─ java/com/pyj/focusbank/
  │  │  │  ├─ controller/        # REST 컨트롤러
  │  │  │  ├─ service/           # 서비스 계층
  │  │  │  ├─ dao/               # MyBatis Mapper 인터페이스
  │  │  │  └─ dto/               # 요청/응답 DTO
  │  │  └─ resources/
  │  │     ├─ mapper/*.xml       # MyBatis 매퍼 XML
  │  │     ├─ templates/index.html
  │  │     ├─ application.yml / application-dev.yml
  │  │     └─ static/sql/focus.sql  # 스키마 & 인덱스
  │  └─ test/java/...
  └─ README.md (repo 루트)
```

---

## ▶️ 실행 방법 (로컬 개발)

1) **DB 준비 (MariaDB)**  
```sql
-- 데이터베이스 생성 (예시)
CREATE DATABASE focusbank DEFAULT CHARACTER SET utf8mb4;
CREATE USER 'fb'@'%' IDENTIFIED BY '1735';
GRANT ALL PRIVILEGES ON focusbank.* TO 'fb'@'%';
FLUSH PRIVILEGES;
```

2) **스키마 생성**  
`src/main/resources/static/sql/focus.sql`를 실행해 테이블을 생성합니다.

3) **환경 설정**  
`src/main/resources/application-dev.yml` (기본 active: `dev`)
```yaml
spring:
  datasource:
    url: jdbc:mariadb://localhost:3306/focusbank?useUnicode=true&characterEncoding=utf8
    username: fb
    password: 1735
```
> 운영 환경에서는 **환경 변수/Secret**로 계정 정보를 관리하세요.

4) **애플리케이션 실행**
```bash
./gradlew bootRun
# 또는
./gradlew build && java -jar build/libs/*.jar
```

5) **접속**  
브라우저에서 `http://localhost:8080`

---

## 🔐 클라이언트 식별 (X-ANON-ID)

- 최초 접속 시 프론트에서 **ULID** 생성 후 `localStorage`에 저장
- 모든 API 호출 시 `X-ANON-ID` 헤더로 전송
- 서버는 이 값을 기준으로 `anonymous_user`를 식별/관리

---

## 🔗 API 빠른 안내

> 전체 상세는 [`docs/API.md`](./docs/API.md) 참고

- `POST /api/sessions/deposit` — 세션 시작(입금)  
- `POST /api/sessions/settle?sessionId=...` — 세션 종료(정산)  
- `GET  /api/sessions/active` — 진행 중 세션 조회 (없으면 204)  
- `GET  /api/sessions?date=YYYY-MM-DD` — 해당 날짜의 세션 목록  
- `GET  /api/reports/summary?date=YYYY-MM-DD` — 일일 합계  
- `GET  /api/reports/weekly?weeks=N` — 주간 집계(N주)  
- `GET  /api/reports/monthly?months=N` — 월간 집계(N개월)  
- `POST /api/goals` — 목표 저장(업서트)  
- `GET  /api/goals/current?period=DAILY|WEEKLY|MONTHLY` — 활성 목표 조회  
- `GET  /api/goals/progress?period=daily|weekly|monthly` — 목표 진행률  
- `GET  /api/rankings/weekly?limit=N` — 주간 랭킹  
- `GET  /api/rankings/overall?limit=N` — 전체 누적 랭킹  
- `GET  /api/profile` — 프로필 조회  
- `POST /api/profile/nickname` — 닉네임 저장

---

## 🧩 아키텍처 개요

- **Controller → Service → DAO(MyBatis) → DB** 레이어드 구조
- 트랜잭션은 **Service** 레이어에서 관리
- 집계 테이블(`daily_aggregate`)을 사용해 조회 성능 최적화

자세한 구조/흐름도는 [`docs/ARCHITECTURE.md`](./docs/ARCHITECTURE.md) 참고.

---

## 🗄️ DB 개요

핵심 테이블:

- `anonymous_user(anon_id, nickname, nickname_tag, created_at, updated_at)`
- `focus_session(session_id, anon_id, started_at, ended_at, duration_sec, created_at)`
- `daily_aggregate(anon_id, target_date, total_sec, created_at)`
- `user_goal(goal_id, anon_id, period_type, target_seconds, effective_from, created_at)`

상세 스키마/인덱스는 [`docs/DB.md`](./docs/DB.md) 참고.

---

## ✅ 품질/보안 체크리스트

- [ ] DB 계정/비밀번호 환경 변수로 분리
- [ ] RestController 예외 처리(ProblemDetail) 표준화
- [ ] 입력값 검증(닉네임 규칙, 목표 값 범위) 강화
- [ ] CORS/HTTPS/Reverse Proxy 설정 (운영 시)
- [ ] 부하테스트/지표(Actuator/Prometheus) 추가

---

## 🛣️ 향후 개선 아이디어
- Streak(연속 달성일) 표시, 더 풍부한 리포트 시각화
- 기록 내보내기(CSV/PDF), 모바일 PWA 지원
- 실시간 공동 랭킹/친구 초대

