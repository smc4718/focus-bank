# Focus Bank (디지털 집중력 은행) --

## 프로젝트 개요
- 집중 시간을 마치 "돈처럼 저축"하는 개념
- 로그인/회원가입 없이 세션 시작/종료 버튼만으로 기록
- 일/주/월 단위 자동 집계 & 리더보드 제공
- 하루 목표/스트릭 확인 기능
- QR 기반 기기 연동 (로그인 대체)

## 기술 스택
- Java 17, Spring Boot 3.5.5
- MariaDB, MyBatis
- Thymeleaf + JavaScript
- Spring Boot Actuator
- (추가 예정) Redis, Scheduler/Batch, JWT, ZXing, Prometheus/Grafana

## DB 스키마 초안 (변동 예정)
- anonymous_user: 익명 사용자
- focus_session: 집중 세션 기록
- daily_aggregate: 일별 총합
- user_goal, user_streak: 목표/스트릭 관리
- (추가 예정) weekly/monthly 집계, leaderboard

## 개발 로드맵 (4주)
- Week 1: 프로젝트 세팅, DB 구축, 세션 API, index.html UI 뼈대
- Week 2: 집계 자동화, 랭킹 보드
- Week 3: 목표/스트릭, QR 연동, Actuator
- Week 4: Redis, 모니터링, 최종 문서화
