-- 익명 사용자
DROP TABLE IF EXISTS anonymous_user;
CREATE TABLE anonymous_user (
  anon_id      CHAR(26)    NOT NULL,           					   -- 익명 사용자 식별자 (ULID 26자리)
  created_at   DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP, -- 생성 시각
  nickname     VARCHAR(24)     NULL,             					-- 닉네임
  nickname_tag CHAR(4) 		    NULL,   								-- 닉네임 충돌 방지용 태그 (#1234)
  PRIMARY KEY (anon_id),
  UNIQUE KEY uq_nickname (nickname, nickname_tag) 					-- 닉네임+태그 조합 유니크
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 집중 세션(입금/출금)
DROP TABLE IF EXISTS focus_session;
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 일별 집계
DROP TABLE IF EXISTS daily_aggregate;
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;       

