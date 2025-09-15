package com.pyj.focusbank.dao;

import com.pyj.focusbank.dto.FocusSessionDto;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;
import java.util.List;

@Mapper
public interface SessionMapper {

    /**
     * 특정 사용자(anonId)의 진행 중인 세션 1개 조회.
     *
     * @param anonId 익명 사용자 ID
     * @return FocusSessionDto (없으면 null)
     */
    FocusSessionDto findActiveSession(@Param("anonId") String anonId);

    /**
     * 특정 날짜의 세션 목록 조회
     * @param anonId 익명 사용자 ID
     * @param date 조회할 날짜 (yyyy-MM-dd)
     * @return 해당 날짜의 세션 목록
     */
    List<FocusSessionDto> findByAnonAndDate(@Param("anonId") String anonId,
                                            @Param("date") LocalDate date);

    /**
     * 열린(종료되지 않은) 세션 조회
     * @param anonId 익명 사용자 ID
     * @return FocusSessionDto (없으면 null)
     */
    FocusSessionDto findOpenByAnon(@Param("anonId") String anonId);

    /**
     * 익명 사용자 보장 (없으면 삽입, 있으면 무시)
     * @param anonId 익명 사용자 ID
     * @return 영향받은 행 수
     */
    int ensureAnonUser(@Param("anonId") String anonId);

    /**
     * 집중 시작 (세션 생성)
     * @param anonId 익명 사용자 ID
     * @return 삽입된 행 수
     */
    int startFocus(@Param("anonId") String anonId);

    /**
     * 집중 종료 (세션 종료 처리)
     * @param sessionId 세션 ID
     * @return 업데이트된 행 수 (없으면 이미 종료된 세션)
     */
    int endFocus(@Param("sessionId") Long sessionId);

    /**
     * 종료된 세션을 기반으로 일일 집계 반영
     * @param sessionId 세션 ID
     * @return 업데이트된 행 수
     */
    int updateDailyAggregate(@Param("sessionId") Long sessionId);

    /**
     * ID로 세션 단건 조회
     * @param sessionId 세션 ID
     * @return FocusSessionDto (없으면 null)
     */
    FocusSessionDto findById(@Param("sessionId") Long sessionId);
}