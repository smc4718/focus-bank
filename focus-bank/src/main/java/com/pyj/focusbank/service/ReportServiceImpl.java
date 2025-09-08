package com.pyj.focusbank.service;

import com.pyj.focusbank.dao.ReportMapper;
import com.pyj.focusbank.dto.DailySummaryDto;
import com.pyj.focusbank.dto.FocusAggDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Objects;

/**
 * 리포트(일/주/월) 조회
 * - 타임존은 KST(Asia/Seoul) 기준으로 기간을 계산함.
 * - DB는 UTC로 저장하더라도 일/주/월 경계(자정, 월요일, 1일)는 KST 기준이 자연스럽다는 가정.
 */
@Service
@RequiredArgsConstructor
public class ReportServiceImpl implements ReportService {

    private final ReportMapper reportMapper;

    // 한국 시간대(서버가 UTC여도 통계 표시는 KST 기준으로)
    private static final ZoneId KST = ZoneId.of("Asia/Seoul");

    // 운영 상한선(무제한 조회 방지 및 과도한 차트 라벨 방지)
    private static final int MAX_WEEKS = 52;   // 최대 1년치 주간
    private static final int MAX_MONTHS = 24;  // 최대 2년치 월간

    /**
     * 특정 날짜의 일별 요약 조회.
     * - 존재하지 않으면 totalSeconds=0 으로 기본값 반환.
     * - targetDate는 'YYYY-MM-DD' 문자열로 가정(daily_aggregate.target_date와 동일 스키마).
     */
    @Transactional(readOnly = true)
    @Override
    public DailySummaryDto getDailySummary(String anonId, String targetDate) {
        // 필수 파라미터 가드
        anonId = Objects.requireNonNull(anonId, "anonId required").trim();
        targetDate = Objects.requireNonNull(targetDate, "targetDate required").trim();

        // Mapper에서 해당 일자의 누적 요약을 조회
        DailySummaryDto dto = reportMapper.findDailySummary(anonId, targetDate);

        // 집계가 없으면 기본값(0초)으로 반환하여 클라이언트 단 분기 최소화
        if (dto == null) {
            return new DailySummaryDto(targetDate, 0L);
        }
        return dto;
    }

    /**
     * 최근 N주 리포트.
     * - 포함 범위: (N-1)주 전 "월요일 00:00" ~ 오늘(포함).
     * - 라벨링/그룹핑은 Mapper XML에서 YEARWEEK(..., 3)로 ISO 주차 사용(월요일 시작).
     */
    @Transactional(readOnly = true)
    @Override
    public List<FocusAggDto> getWeeklyReport(String anonId, int weeks) {
        // 기본값/상한값 보정: 1~MAX_WEEKS 범위로 클램프
        if (weeks <= 0) weeks = 12;
        if (weeks > MAX_WEEKS) weeks = MAX_WEEKS;

        // 필수 파라미터 가드
        anonId = Objects.requireNonNull(anonId, "anonId required").trim();

        // 오늘 날짜(KST 기준).
        LocalDate today = LocalDate.now(KST);

        // (N-1)주 전의 "월요일"부터 ~ 오늘까지(포함)
        // ex) weeks=12 -> 11주 전 월요일 ~ 오늘
        LocalDate from = today.minusWeeks(Math.max(weeks - 1, 0))
                .with(java.time.DayOfWeek.MONDAY);
        LocalDate to = today;

        // 혹시라도 from이 to보다 뒤가 되는 이변 방지(로컬 타임존/서머타임 엣지케이스 등)
        if (from.isAfter(to)) {
            from = to;
        }

        return reportMapper.selectWeeklyAgg(anonId, from, to);
    }

    /**
     * 최근 N개월 리포트.
     * - 포함 범위: (N-1)개월 전 "해당 월의 1일 00:00" ~ 오늘(포함).
     * - 라벨링/그룹핑은 Mapper XML에서 DATE_FORMAT(..., '%Y-%m') 사용.
     */
    @Transactional(readOnly = true)
    @Override
    public List<FocusAggDto> getMonthlyReport(String anonId, int months) {
        // 기본값/상한값 보정: 1~MAX_MONTHS 범위로 클램프
        if (months <= 0) months = 12;
        if (months > MAX_MONTHS) months = MAX_MONTHS;

        // 필수 파라미터 가드
        anonId = Objects.requireNonNull(anonId, "anonId required").trim();

        LocalDate today = LocalDate.now(KST);

        // (N-1)개월 전의 해당 월 1일 ~ 오늘(포함)
        LocalDate from = today.withDayOfMonth(1)
                .minusMonths(Math.max(months - 1, 0));
        LocalDate to = today;

        if (from.isAfter(to)) {
            from = to;
        }

        return reportMapper.selectMonthlyAgg(anonId, from, to);
    }
}