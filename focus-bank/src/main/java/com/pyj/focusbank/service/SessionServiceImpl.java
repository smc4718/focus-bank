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
     * 특정 날짜의 세션 목록 조회
     * 1. anonId 유효성 검사 (null 또는 빈 문자열일 경우 예외 발생)
     * 2. 지정된 anonId와 날짜(date)에 해당하는 focus_session 목록을 DB에서 조회
     * 3. 조회 결과가 없으면 빈 리스트 반환
     *
     * @param anonId 익명 사용자 ID
     * @param date 조회할 날짜 (yyyy-MM-dd)
     * @return 해당 날짜의 세션 목록 (없으면 빈 리스트)
     * @throws IllegalArgumentException anonId가 null 또는 빈 문자열일 경우
     */
    @Override
    public List<FocusSessionDto> getSessionsForDate(String anonId, LocalDate date) {
        // 1. anonId 유효성 검사 → null 또는 공백이면 예외 발생
        if (anonId == null || anonId.trim().isEmpty()) {
            throw new IllegalArgumentException("anonId 값이 비어있습니다.");
        }

        // 2. 앞뒤 공백 제거 후 Mapper 호출 → 특정 날짜의 세션 목록 조회
        return sessionMapper.findByAnonAndDate(anonId.trim(), date);
    }


    /**
     * 집중 시작(입금) 처리
     * 1. anonId 유효성 검사 (null 또는 빈 문자열일 경우 예외 발생)
     * 2. 부모 테이블(anonymous_user)에 anonId 존재 여부 확인 후 없으면 생성
     * 3. focus_session 테이블에 새로운 세션(집중 시작) 기록
     * 4. 방금 생성된 열린 세션 정보를 조회하여 반환
     *
     * @param anonId 익명 사용자 ID
     * @return 새로 생성된 세션 정보 (열린 상태)
     * @throws IllegalArgumentException anonId가 null 또는 빈 문자열일 경우
     * @throws IllegalStateException anonymous_user에 anonId가 존재하지 않을 경우
     */
    @Transactional
    @Override
    public FocusSessionDto startFocus(String anonId) {
        // 1. anonId 유효성 검사 → null 또는 공백이면 예외 발생
        if (anonId == null || anonId.trim().isEmpty()) {
            throw new IllegalArgumentException("anonId 값이 비어있습니다.");
        }

        // 2. 앞뒤 공백 제거
        anonId = anonId.trim();

        // 3. anonymous_user 테이블에 anonId 없으면 삽입 (있으면 무시)
        sessionMapper.ensureAnonUser(anonId);

        // 4. focus_session 테이블에 새로운 세션 삽입
        int affected = sessionMapper.startFocus(anonId);

        // 5. 삽입 결과가 없으면 FK 제약 등으로 부모가 없는 경우 → 예외 발생
        if (affected == 0) {
            throw new IllegalStateException("anonymous_user에 해당 anonId가 없습니다: " + anonId);
        }

        // 6. 방금 열린 세션을 다시 DB에서 조회 후 반환
        return sessionMapper.findOpenByAnon(anonId);
    }


    /**
     * 집중 종료(정산) 처리
     * 1. focus_session 테이블에서 해당 세션의 종료 시각과 집중 시간(duration_sec)을 업데이트
     * 2. 업데이트 결과가 없으면 이미 종료되었거나 존재하지 않는 세션이므로 null 반환
     * 3. 업데이트 성공 시, 종료된 세션 정보를 기반으로 일별 집계(daily_aggregate) 테이블 갱신
     * 4. 종료된 세션의 상세 정보를 다시 조회해서 반환
     *
     * @param sessionId 종료할 세션 ID
     * @return 종료된 세션 정보 (없거나 이미 종료된 경우 null)
     */
    @Transactional
    @Override
    public FocusSessionDto endFocus(Long sessionId) {
        // 1. focus_session 테이블에서 해당 세션 종료 처리
        //    → ended_at을 NOW()로 업데이트, duration_sec 계산
        int updated = sessionMapper.endFocus(sessionId);

        // 2. 업데이트된 행이 없으면 (updated == 0)
        //    → 이미 종료됐거나 존재하지 않는 세션이므로 null 반환
        if (updated == 0) {
            return null;
        }

        // 3. 정상적으로 종료된 경우 → daily_aggregate 테이블에 해당 세션 반영
        sessionMapper.updateDailyAggregate(sessionId);

        // 4. 종료된 세션 정보를 다시 DB에서 조회하여 반환
        return sessionMapper.findById(sessionId);
    }
}