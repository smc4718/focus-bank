package com.pyj.focusbank.dao;

import com.pyj.focusbank.dto.DailySummaryDto;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface ReportMapper {

    /**
     * 특정 anonId, 특정 날짜의 집중 요약 조회
     *
     * @param anonId 익명 사용자 ID
     * @param targetDate 대상 날짜 (YYYY-MM-DD)
     * @return DailySummaryDto (없으면 null)
     */
    DailySummaryDto findDailySummary(@Param("anonId") String anonId,
                                     @Param("targetDate") String targetDate);
}