package com.pyj.focusbank.service;

import com.pyj.focusbank.dao.GoalMapper;
import com.pyj.focusbank.dto.GoalProgressDto;
import com.pyj.focusbank.dto.GoalSaveRequest;
import com.pyj.focusbank.dto.UserGoalDto;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Objects;

/**
 * 목표 진행률 계산 + 목표 저장 서비스
 * - 기간 계산은 KST 기준
 * - 주차 계산은 ISO 기준(월요일 시작)
 */
@Service
@RequiredArgsConstructor
public class GoalServiceImpl implements GoalService {

    private final GoalMapper goalMapper;
    private static final ZoneId KST = ZoneId.of("Asia/Seoul");

    @Transactional(readOnly = true)
    @Override
    public GoalProgressDto getProgress(String anonId, String periodType) {
        anonId = Objects.requireNonNull(anonId, "anonId required").trim();
        periodType = Objects.requireNonNull(periodType, "periodType required").trim().toUpperCase();

        final LocalDate today = LocalDate.now(KST);

        // 기간 from/to 계산
        LocalDate from;
        switch (periodType) {
            case "DAILY":
                from = today;
                break;
            case "WEEKLY":
                from = today.with(DayOfWeek.MONDAY);
                break;
            case "MONTHLY":
                from = today.withDayOfMonth(1);
                break;
            default:
                periodType = "DAILY";
                from = today;
        }
        LocalDate to = today;

        // 목표 조회
        UserGoalDto goal = goalMapper.selectActiveGoal(anonId, periodType, today);

        // 달성치 합계
        int achieved = goalMapper.sumTotalSecondsBetween(anonId, from, to);

        if (goal == null) {
            // 목표 미설정
            return new GoalProgressDto(null, achieved, null);
        }

        // 진행률 계산 (최대 1.0)
        double progress = goal.getTargetSeconds() != null && goal.getTargetSeconds() > 0
                ? Math.min(1.0, (double) achieved / goal.getTargetSeconds())
                : 0.0;

        return new GoalProgressDto(goal.getTargetSeconds(), achieved, progress);
    }

    @Transactional
    @Override
    public UserGoalDto saveGoal(String anonId, GoalSaveRequest req) {
        anonId = Objects.requireNonNull(anonId, "anonId required").trim();
        Objects.requireNonNull(req, "request required");

        String periodType = Objects.requireNonNull(req.getPeriodType(), "periodType required")
                .trim().toUpperCase();
        Integer targetSeconds = Objects.requireNonNull(req.getTargetSeconds(), "targetSeconds required");

        // 간단 검증 (세부 조건은 정책에 맞게 조정)
        if (!(periodType.equals("DAILY") || periodType.equals("WEEKLY") || periodType.equals("MONTHLY"))) {
            throw new IllegalArgumentException("periodType must be DAILY|WEEKLY|MONTHLY");
        }
        if (targetSeconds <= 0) {
            throw new IllegalArgumentException("targetSeconds must be positive");
        }

        // effectiveFrom 기본값 = 오늘(KST)
        final LocalDate today = LocalDate.now(KST);
        String effectiveFrom = (req.getEffectiveFrom() == null || req.getEffectiveFrom().isBlank())
                ? today.toString()
                : req.getEffectiveFrom().trim();

        UserGoalDto goal = new UserGoalDto();
        goal.setAnonId(anonId);
        goal.setPeriodType(periodType);
        goal.setTargetSeconds(targetSeconds);
        goal.setEffectiveFrom(effectiveFrom);

        // 업서트 (중복이면 target_seconds 갱신)
        try {
            goalMapper.upsertGoal(goal);
        } catch (DuplicateKeyException e) {
            // UNIQUE 제약 위반 시(이론상 ON DUPLICATE로 처리되므로 거의 없음) 방어
            goalMapper.upsertGoal(goal);
        }

        // 저장/갱신 후 최신 목표 반환
        return goalMapper.selectActiveGoal(anonId, periodType, today);
    }

    @Transactional(readOnly = true)
    @Override
    public UserGoalDto getActiveGoal(String anonId, String periodType) {
        anonId = Objects.requireNonNull(anonId, "anonId required").trim();
        periodType = Objects.requireNonNull(periodType, "periodType required").trim().toUpperCase();
        final LocalDate today = LocalDate.now(KST);
        return goalMapper.selectActiveGoal(anonId, periodType, today);
    }
}