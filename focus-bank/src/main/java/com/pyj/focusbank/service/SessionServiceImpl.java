package com.pyj.focusbank.service;

import com.pyj.focusbank.dao.SessionMapper;
import com.pyj.focusbank.dto.FocusSessionDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class SessionServiceImpl implements SessionService {

    private final SessionMapper sessionMapper;

    /**
     * 특정 날짜의 세션 목록 조회 구현
     * @param anonId 익명 사용자 ID
     * @param date 조회할 날짜 (yyyy-MM-dd)
     * @return 세션 목록
     */
    @Override
    public List<FocusSessionDto> getSessionsForDate(String anonId, LocalDate date) {
        return sessionMapper.findByAnonAndDate(anonId.trim(), date);
    }

    /**
     * 집중 시작 (입금)
     * 1. anonId 유효성 검사 + 공백 제거
     * 2. anonymous_user 테이블에 anonId 없으면 생성 (ensure)
     * 3. focus_session 테이블에 새 세션 기록 (부모 없으면 0행 → 예외)
     * 4. 방금 열린 세션 정보를 다시 조회해서 반환
     *
     * @param anonId 익명 사용자 ID
     * @return 새로 시작된 세션 정보
     */
    @Transactional
    @Override
    public FocusSessionDto startFocus(String anonId) {
        // 1) null/빈값 방어 + 앞뒤 공백 제거
        anonId = Objects.requireNonNull(anonId, "anonId required").trim();

        // 2) 부모 테이블(anonymous_user)에 anonId가 없으면 생성
        sessionMapper.ensureAnonUser(anonId);

        // 3) focus_session에 새 세션 기록
        int affected = sessionMapper.startFocus(anonId);
        if (affected == 0) {
            throw new IllegalStateException("anonymous_user에 anonId가 없습니다: " + anonId);
        }

        // 4) 방금 열린 세션을 다시 조회해서 리턴
        return sessionMapper.findOpenByAnon(anonId);
    }

    /**
     * 집중 종료 (정산)
     * 1. 세션 종료 시각/집중 시간(duration_sec) 업데이트
     * 2. 업데이트 성공 시 일별 집계 테이블 갱신
     * 3. 종료된 세션 정보를 조회해서 반환
     *
     * @param sessionId 종료할 세션 ID
     * @return 종료된 세션 정보
     */
    @Transactional
    @Override
    public FocusSessionDto endFocus(Long sessionId) {
        // 1) 세션 종료
        int updated = sessionMapper.endFocus(sessionId);

        // 2) 업데이트 없으면 이미 종료 or 없는 세션 → null 반환
        if (updated == 0) {
            return null;
        }

        // 3) 종료된 세션 기준으로 일별 집계 갱신
        sessionMapper.updateDailyAggregate(sessionId);

        // 4) 종료된 세션 정보 리턴
        return sessionMapper.findById(sessionId);
    }
}