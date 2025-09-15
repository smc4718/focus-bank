package com.pyj.focusbank.service;

import com.pyj.focusbank.dto.FocusSessionDto;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface SessionService {

    /**
     * 진행 중인 세션 조회.
     *
     * <p>조건:
     * - 해당 사용자의 anonId
     * - 아직 종료되지 않은 세션(endedAt == null)</p>
     *
     * @param anonId 익명 사용자 ID
     * @return 진행 중인 세션이 있으면 Optional.of(dto), 없으면 Optional.empty()
     */
    Optional<FocusSessionDto> getActiveSession(String anonId);

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