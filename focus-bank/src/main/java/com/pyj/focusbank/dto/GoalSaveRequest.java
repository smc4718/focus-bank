package com.pyj.focusbank.dto;

import lombok.*;

/**
 * 목표 저장 요청 바디
 * - effectiveFrom 생략 시 오늘(KST)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GoalSaveRequest {
    private String periodType;     // DAILY | WEEKLY | MONTHLY
    private Integer targetSeconds;
    private String effectiveFrom;  // YYYY-MM-DD (optional)
}