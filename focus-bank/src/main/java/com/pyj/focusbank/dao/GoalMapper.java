package com.pyj.focusbank.dao;

import com.pyj.focusbank.dto.UserGoalDto;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;

@Mapper
public interface GoalMapper {

    /**
     * 오늘 기준 활성 목표 1건 조회
     * - 조건: effective_from <= today
     * - 정렬: 최신 effective_from DESC
     */
    UserGoalDto selectActiveGoal(@Param("anonId") String anonId,
                                 @Param("periodType") String periodType,
                                 @Param("today") LocalDate today);

    /**
     * daily_aggregate에서 기간(from~to) 합계(초)
     */
    Integer sumTotalSecondsBetween(@Param("anonId") String anonId,
                                   @Param("fromDate") LocalDate fromDate,
                                   @Param("toDate") LocalDate toDate);

    /**
     * 목표 저장 (업서트)
     * - 같은 (anonId, periodType, effectiveFrom)이면 target_seconds만 갱신
     */
    int upsertGoal(UserGoalDto goal);

    /** 익명 사용자 row 자동 생성 (없으면 삽입, 있으면 무시) */
    int ensureAnonymousUser(@Param("anonId") String anonId);
}