package com.pyj.focusbank.service;

import com.pyj.focusbank.dto.DailySummaryDto;

public interface ReportService {

    /**
     * 특정 날짜의 집중 요약 조회
     *
     * @param anonId 익명 사용자 ID
     * @param targetDate 대상 날짜 (YYYY-MM-DD)
     * @return DailySummaryDto (없으면 totalSec=0)
     */
    DailySummaryDto getDailySummary(String anonId, String targetDate);
}