package com.pyj.focusbank.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 목표 정보 DTO
 * - user_goal 테이블 매핑
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GoalDto {
    private Long goalId;
    private String anonId;         // 사용자 ULID
    private String periodType;     // DAILY / WEEKLY / MONTHLY
    private Integer targetSeconds; // 목표 시간(초)
    private String effectiveFrom;  // 적용 시작일 (YYYY-MM-DD)
    private String createdAt;      // 생성 시각
}