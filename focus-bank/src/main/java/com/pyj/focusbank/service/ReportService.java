package com.pyj.focusbank.service;

import com.pyj.focusbank.dto.DailySummaryDto;
import com.pyj.focusbank.dto.FocusAggDto;

import java.util.List;

public interface ReportService {

    /**
     * 특정 날짜의 집중 요약 조회
     *
     * @param anonId 익명 사용자 ID
     * @param targetDate 대상 날짜 (YYYY-MM-DD)
     * @return DailySummaryDto (없으면 totalSec=0)
     */
    DailySummaryDto getDailySummary(String anonId, String targetDate);

    /**
     * 최근 N주 리포트를 반환.
     * - 기간 계산은 Asia/Seoul(KST) 기준으로 이루어집니다.
     * - (N-1)주 전 "월요일"부터 오늘까지를 포함합니다.
     *
     * @param anonId 익명 사용자 식별자
     * @param weeks  최근 몇 주(기본 12주 권장)
     * @return 주차(period="YYYY-Www")별 집계 결과
     */
    List<FocusAggDto> getWeeklyReport(String anonId, int weeks);

    /**
     * 최근 N개월 리포트를 반환.
     * - 기간 계산은 KST 기준으로 이루어집니다.
     * - (N-1)개월 전 "1일"부터 오늘까지를 포함합니다.
     *
     * @param anonId 익명 사용자 식별자
     * @param months 최근 몇 개월(기본 12개월 권장)
     * @return 월(period="YYYY-MM")별 집계 결과
     */
    List<FocusAggDto> getMonthlyReport(String anonId, int months);
}