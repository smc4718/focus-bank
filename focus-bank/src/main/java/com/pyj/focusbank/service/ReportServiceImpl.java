package com.pyj.focusbank.service;

import com.pyj.focusbank.dao.ReportMapper;
import com.pyj.focusbank.dto.DailySummaryDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

@Service
@RequiredArgsConstructor
public class ReportServiceImpl implements ReportService {

    private final ReportMapper reportMapper;

    /**
     * 특정 날짜 집중 요약 조회
     */
    @Transactional(readOnly = true)
    @Override
    public DailySummaryDto getDailySummary(String anonId, String targetDate) {
        anonId = Objects.requireNonNull(anonId, "anonId required").trim();
        targetDate = Objects.requireNonNull(targetDate, "targetDate required").trim();

        DailySummaryDto dto = reportMapper.findDailySummary(anonId, targetDate);
        if (dto == null) {
            // 집계가 없으면 기본값 리턴
            return new DailySummaryDto(targetDate, 0L);
        }
        return dto;
    }
}