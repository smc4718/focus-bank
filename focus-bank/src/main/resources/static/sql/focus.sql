-- DROP
DROP TABLE IF EXISTS user_goal;
DROP TABLE IF EXISTS daily_aggregate;
DROP TABLE IF EXISTS focus_session;
DROP TABLE IF EXISTS anonymous_user;

-- 익명 사용자focusbank
CREATE TABLE anonymous_user (
  anon_id      CHAR(26)    NOT NULL,           					   -- 익명 사용자 식별자 (ULID 26자리)
  created_at   DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP, -- 생성 시각
  updated_at   DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP  -- 최종 수정 시각
                                ON UPDATE CURRENT_TIMESTAMP,
  nickname     VARCHAR(24)     NULL,             				-- 닉네임
  nickname_tag CHAR(4) 		    NULL DEFAULT NULL,								-- 닉네임 충돌 방지용 태그 (#1234)
  PRIMARY KEY (anon_id),
  UNIQUE KEY uq_nickname (nickname, nickname_tag) 					-- 닉네임+태그 조합 유니크
) ENGINE=InnoDB DEFAULT CHARSET=UTF8MB4;


-- 집중 세션(입금/출금)
CREATE TABLE focus_session (
  session_id 	BIGINT 	NOT NULL AUTO_INCREMENT, 				-- 세션 고유 ID
  anon_id      CHAR(26) NOT NULL,                 				-- 어떤 사용자의 세션인지(FK)
  started_at 	DATETIME NOT NULL,              					-- 세션 시작 시각 (입금 시작)
  ended_at 		DATETIME 	 NULL,                    			-- 세션 종료 시각 (출금 완료, NULL이면 진행 중)
  duration_sec INT 			 NULL,                     		-- 집중 시간(초) = 종료 시 계산
  created_at 	DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP, -- 레코드 생성 시각
  PRIMARY KEY (session_id),
  CONSTRAINT fk_fs_user FOREIGN KEY (anon_id)
    REFERENCES anonymous_user(anon_id)
    ON UPDATE CASCADE ON DELETE RESTRICT,
  KEY idx_fs_user_started (anon_id, started_at), -- 사용자별 시작 시각 조회 최적화
  KEY idx_fs_user_ended (anon_id, ended_at)      -- 사용자별 종료 시각 조회 최적화
) ENGINE=InnoDB DEFAULT CHARSET=UTF8MB4;


-- 일별 집계
CREATE TABLE daily_aggregate (
  target_date 	 DATE 	 NOT NULL,              				-- 집계 대상 날짜
  anon_id 		 CHAR(26) NOT NULL,              				-- 사용자 ID (FK)
  total_seconds INT 		 NOT NULL DEFAULT 0,   					-- 해당 날짜 총 집중 시간(초)
  updated_at 	 DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
                         ON UPDATE CURRENT_TIMESTAMP, 		-- 마지막 갱신 시각
  PRIMARY KEY (target_date, anon_id),         					-- 날짜+사용자 복합 PK (하루 1행)
  CONSTRAINT fk_da_user FOREIGN KEY (anon_id)
    REFERENCES anonymous_user(anon_id)
    ON UPDATE CASCADE ON DELETE RESTRICT,
  KEY idx_da_date_total (target_date, total_seconds DESC) 	-- 날짜별 랭킹 조회 최적화
) ENGINE=InnoDB DEFAULT CHARSET=UTF8MB4;       


-- 사용자 목표 테이블 (목표 진행률 기능용)
-- 목표가 아직 없다면 이 테이블은 비어 있어도 OK (조회 시 "미설정"으로 응답됨)
CREATE TABLE user_goal (
  goal_id         BIGINT   AUTO_INCREMENT PRIMARY KEY, -- 목표 고유 ID
  anon_id         CHAR(26) 									NOT NULL, -- 익명 사용자 ULID (FK)
  period_type     ENUM('DAILY','WEEKLY','MONTHLY') 	NOT NULL, -- 목표 기간
  target_seconds  INT 											NOT NULL, -- 목표 시간(초)
  effective_from  DATE 											NOT NULL, -- 이 날짜부터 유효
  created_at      DATETIME 									NOT NULL DEFAULT CURRENT_TIMESTAMP, -- 생성 시각

  CONSTRAINT fk_goal_user FOREIGN KEY (anon_id)
    REFERENCES anonymous_user(anon_id)
    ON UPDATE CASCADE ON DELETE RESTRICT,

  -- 같은 사용자 × 기간 × 시작일은 1건만 허용
  CONSTRAINT uq_goal_unique UNIQUE (anon_id, period_type, effective_from),

  -- 조회 최적화 인덱스: 오늘 기준 최신 목표 찾기
  KEY idx_goal_user_period (anon_id, period_type, effective_from)
) ENGINE=InnoDB DEFAULT CHARSET=UTF8MB4;


