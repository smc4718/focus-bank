package com.pyj.focusbank.controller;

import com.pyj.focusbank.dto.GoalProgressDto;
import com.pyj.focusbank.dto.GoalSaveRequest;
import com.pyj.focusbank.dto.UserGoalDto;
import com.pyj.focusbank.service.GoalService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 목표 관리 API
 * - POST   /api/goals           : 목표 설정(업서트)
 * - GET    /api/goals/current   : 오늘 기준 활성 목표 조회
 * - GET    /api/goals/progress  : 목표 진행률 조회
 */
@RestController
@RequestMapping("/api/goals")
@RequiredArgsConstructor
public class GoalController {

    private final GoalService goalService;

    /**
     * 목표 설정(업서트)
     * 헤더: X-ANON-ID
     * 바디: { "periodType": "DAILY", "targetSeconds": 7200, "effectiveFrom": "2025-09-10"(선택) }
     */
    @PostMapping
    public ResponseEntity<UserGoalDto> saveGoal(
            @RequestHeader("X-ANON-ID") String anonId,
            @RequestBody GoalSaveRequest body) {

        UserGoalDto saved = goalService.saveGoal(anonId, body);
        return ResponseEntity.status(201).body(saved);
    }

    /** 오늘 기준 활성 목표 조회 */
    @GetMapping("/current")
    public ResponseEntity<UserGoalDto> getCurrentGoal(
            @RequestHeader("X-ANON-ID") String anonId,
            @RequestParam("period") String periodType) {

        return ResponseEntity.ok(goalService.getActiveGoal(anonId, periodType));
    }

    /** 목표 진행률 조회 */
    @GetMapping("/progress")
    public ResponseEntity<GoalProgressDto> getProgress(
            @RequestHeader("X-ANON-ID") String anonId,
            @RequestParam("period") String periodType) {

        return ResponseEntity.ok(goalService.getProgress(anonId, periodType));
    }
}