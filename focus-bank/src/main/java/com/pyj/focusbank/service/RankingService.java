package com.pyj.focusbank.service;

import com.pyj.focusbank.dto.RankingDto;

import java.util.List;

public interface RankingService {

    /**
     * 이번 주(월요일~오늘) 랭킹 TOP N을 반환한다.
     *
     * @param limit 상위 N명
     * @return 정렬된 {@link RankingDto} 리스트
     */
    List<RankingDto> getWeeklyRanking(int limit);

    /**
     * 전체 누적 랭킹 TOP N을 반환한다.
     *
     * @param limit 상위 N명
     * @return 정렬된 {@link RankingDto} 리스트
     */
    List<RankingDto> getOverallRanking(int limit);
}