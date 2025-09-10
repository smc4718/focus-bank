package com.pyj.focusbank.dto;

import lombok.*;

/**
 * 목표 진행률 조회 응답 DTO
 * - targetSeconds: 목표(초) (없으면 null => 미설정)
 * - achievedSeconds: 현재까지 달성(초)
 * - progress: 0.0~1.0 (목표 없으면 null)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GoalProgressDto {
    private Integer targetSeconds;    // null = 미설정
    private Integer achievedSeconds;  // 달성치(초)
    private Double progress;          // 0~1 (null=미설정)
}