package com.pyj.focusbank.service;

import com.pyj.focusbank.dto.FocusSessionDto;

public interface SessionService {

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