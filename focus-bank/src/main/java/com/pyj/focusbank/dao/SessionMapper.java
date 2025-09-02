package com.pyj.focusbank.dao;

import com.pyj.focusbank.dto.FocusSessionDto;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface SessionMapper {

    // 열린(종료되지 않은) 세션 조회
    FocusSessionDto findOpenByAnon(@Param("anonId") String anonId);

    // 집중 시작 (입금 시작)
    int startFocus(@Param("anonId") String anonId);

    // 집중 종료 (출금 완료)
    int endFocus(@Param("sessionId") Long sessionId);

    // 종료 직후 일일 집계 반영
    int updateDailyAggregate(@Param("sessionId") Long sessionId);

    // ID로 세션 조회
    FocusSessionDto findById(@Param("sessionId") Long sessionId);
}