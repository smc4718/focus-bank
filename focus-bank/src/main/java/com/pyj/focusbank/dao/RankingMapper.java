package com.pyj.focusbank.dao;

import com.pyj.focusbank.dto.RankingDto;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;
import java.util.List;

/**
 * 랭킹 조회 Mapper 인터페이스.
 *
 * <p>RankingMapper.xml과 연결되어,
 * 주간 랭킹 및 전체 랭킹을 RankingDto 리스트로 반환한다.</p>
 */
@Mapper
public interface RankingMapper {

    /**
     * 이번 주(월~오늘) 랭킹 TOP N 조회.
     *
     * @param monday 이번 주 월요일
     * @param today  오늘 날짜
     * @param limit  상위 N명
     * @return 정렬된 RankingDto 리스트
     */
    List<RankingDto> selectWeeklyRanking(
            @Param("monday") LocalDate monday,
            @Param("today") LocalDate today,
            @Param("limit") int limit
    );

    /**
     * 전체 누적 랭킹 TOP N 조회.
     *
     * @param limit 상위 N명
     * @return 정렬된 RankingDto 리스트
     */
    List<RankingDto> selectOverallRanking(@Param("limit") int limit);
}