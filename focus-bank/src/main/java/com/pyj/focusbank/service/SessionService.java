package com.pyj.focusbank.service;

import com.pyj.focusbank.dto.FocusSessionDto;

import java.time.LocalDate;
import java.util.List;

public interface SessionService {

    /**
     * 특정 날짜의 세션 목록 조회
     * @param anonId 익명 사용자 ID
     * @param date 조회할 날짜 (yyyy-MM-dd)
     * @return 세션 목록
     */
    List<FocusSessionDto> getSessionsForDate(String anonId, LocalDate date);

    /**
     * 집중 시작 (입금)
     * @param anonId 익명 사용자 ID
     * @return 새로 시작된 세션 정보
     */
    FocusSessionDto startFocus(String anonId);

    /**
     * 집중 종료 (정산)
     * @param sessionId 종료할 세션 ID
     * @return 종료된 세션 정보
     */
    FocusSessionDto endFocus(Long sessionId);
}