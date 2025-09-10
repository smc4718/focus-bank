package com.pyj.focusbank.service;

import com.pyj.focusbank.dto.GoalProgressDto;
import com.pyj.focusbank.dto.GoalSaveRequest;
import com.pyj.focusbank.dto.UserGoalDto;

/**
 * 목표 관리 서비스
 */
public interface GoalService {

    /**
     * 목표 진행률 조회
     * @param anonId     ULID 26자
     * @param periodType DAILY | WEEKLY | MONTHLY (대소문자 무관)
     */
    GoalProgressDto getProgress(String anonId, String periodType);

    /**
     * 목표 저장(업서트)
     * @param anonId ULID 26자
     * @param req    periodType, targetSeconds, effectiveFrom(optional)
     * @return 저장/갱신된 목표
     */
    UserGoalDto saveGoal(String anonId, GoalSaveRequest req);

    /**
     * 오늘 기준 활성 목표 조회 (프리필용)
     */
    UserGoalDto getActiveGoal(String anonId, String periodType);
}