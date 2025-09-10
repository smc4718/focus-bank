package com.pyj.focusbank.dto;

import lombok.*;

import java.time.LocalDate;

/**
 * user_goal 테이블 매핑 DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserGoalDto {
    private Long goalId;           // 목표 PK
    private String anonId;         // 사용자 ULID
    private String periodType;     // DAILY / WEEKLY / MONTHLY
    private Integer targetSeconds; // 목표 시간(초)
    private String effectiveFrom;  // 적용 시작일 (YYYY-MM-DD)
    private String createdAt;      // 생성 시각
}