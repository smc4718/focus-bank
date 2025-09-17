package com.pyj.focusbank.dao;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Mapper
public interface RankingMapper {

    /**
     * 이번 주(월~오늘) 랭킹 TOP N 조회.
     *
     * @param monday 이번 주 월요일
     * @param today  오늘 날짜
     * @param limit  상위 N명
     * @return SELECT 결과 (컬럼명 → 값) Map 리스트
     */
    List<Map<String, Object>> selectWeeklyRanking(
            @Param("monday") LocalDate monday,
            @Param("today") LocalDate today,
            @Param("limit") int limit
    );

    /**
     * 전체 누적 랭킹 TOP N 조회.
     *
     * @param limit 상위 N명
     * @return SELECT 결과 (컬럼명 → 값) Map 리스트
     */
    List<Map<String, Object>> selectOverallRanking(@Param("limit") int limit);
}