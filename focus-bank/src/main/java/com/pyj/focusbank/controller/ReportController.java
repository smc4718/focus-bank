package com.pyj.focusbank.controller;

import com.pyj.focusbank.dto.DailySummaryDto;
import com.pyj.focusbank.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
}