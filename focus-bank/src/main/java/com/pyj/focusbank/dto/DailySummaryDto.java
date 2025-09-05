package com.pyj.focusbank.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 일별 집중 시간 요약 DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DailySummaryDto {
    /** 대상 날짜 (YYYY-MM-DD) */
    private String targetDate;

    /** 총 집중 시간 (초 단위) */
    private Long totalSec;
}