package com.pyj.focusbank.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class FocusSessionDto {
    private Long sessionId;             // 고유 ID
    private String anonId;              // 어떤 사용자의 세션인지
    private LocalDateTime startedAt;    // 세션 시작 시간 (입금 시작)
    private LocalDateTime endedAt;      // 세션 종료 시간 (출금 완료)
    private Integer durationSec;        // 집중 시간(초) = 종료 시 계산
    private LocalDateTime createdAt;    // 레코드 생성 시각
}