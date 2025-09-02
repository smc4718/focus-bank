package com.pyj.focusbank.service;

import com.pyj.focusbank.dao.SessionMapper;
import com.pyj.focusbank.dto.FocusSessionDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SessionServiceImpl implements SessionService {

    private final SessionMapper sessionMapper;

    /**
     * 집중 시작 (입금)
     * - 사용자의 열린 세션이 있으면 예외
     * - 없으면 새로 시작하고, 방금 열린 세션을 조회해 반환
     */
    @Override
    @Transactional
    public FocusSessionDto startFocus(String anonId) {
        FocusSessionDto open = sessionMapper.findOpenByAnon(anonId);
        if (open != null) {
            throw new IllegalStateException("이미 진행 중인 집중이 있어요.");
        }
        sessionMapper.startFocus(anonId);
        // 방금 열린 세션 재조회(ended_at IS NULL)
        return sessionMapper.findOpenByAnon(anonId);
    }

    /**
     * 집중 종료 (출금)
     * - 종료 가능한 세션이 없으면 예외
     * - 종료 후 일일 집계 갱신
     * - 종료된 세션 정보를 반환
     */
    @Override
    @Transactional
    public FocusSessionDto endFocus(Long sessionId) {
        int updated = sessionMapper.endFocus(sessionId);
        if (updated == 0) {
            throw new IllegalStateException("종료할 수 있는 세션이 없어요.");
        }
        sessionMapper.updateDailyAggregate(sessionId);
        return sessionMapper.findById(sessionId);
    }
}