package com.pyj.focusbank.controller;

import com.pyj.focusbank.dto.DailySummaryDto;
import com.pyj.focusbank.dto.FocusAggDto;
import com.pyj.focusbank.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/reports")
public class ReportController {

    private final ReportService reportService;

    /**
     * 일별 집중 시간 요약 조회
     * GET /api/reports/summary?date=YYYY-MM-DD
     * 헤더: X-ANON-ID
     */
    @GetMapping("/summary")
    public ResponseEntity<DailySummaryDto> getSummary( @RequestHeader("X-ANON-ID") String anonId,
                                                       @RequestParam("date") String date) {
        DailySummaryDto dto = reportService.getDailySummary(anonId, date);
        return ResponseEntity.ok(dto);
    }


    /**
     * 주간 집중 리포트 조회
     * GET /api/reports/weekly?weeks=12
     * Header: X-ANON-ID
     *
     * 응답: [
     *   { "period": "2025-W35", "totalSeconds": 7200, "dayCount": 3, "avgSecPerDay": 2400 },
     *   ...
     * ]
     */
    @GetMapping("/weekly")
    public ResponseEntity<List<FocusAggDto>> getWeekly(
            @RequestHeader("X-ANON-ID") String anonId,
            @RequestParam(value = "weeks", defaultValue = "12") int weeks) {

        List<FocusAggDto> report = reportService.getWeeklyReport(anonId, weeks);
        return ResponseEntity.ok(report);
    }


    /**
     * 월간 집중 리포트 조회
     * GET /api/reports/monthly?months=12
     * Header: X-ANON-ID
     *
     * 응답: [
     *   { "period": "2025-08", "totalSeconds": 14400, "dayCount": 10, "avgSecPerDay": 1440 },
     *   ...
     * ]
     */
    @GetMapping("/monthly")
    public ResponseEntity<List<FocusAggDto>> getMonthly(
            @RequestHeader("X-ANON-ID") String anonId,
            @RequestParam(value = "months", defaultValue = "12") int months) {

        List<FocusAggDto> report = reportService.getMonthlyReport(anonId, months);
        return ResponseEntity.ok(report);
    }
}