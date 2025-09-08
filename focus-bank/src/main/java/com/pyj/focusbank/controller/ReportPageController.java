package com.pyj.focusbank.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;

/**
 * 리포트 화면 컨트롤러
 * - /reports 진입 시 reports.html 뷰를 반환
 * - API는 ReportController(@RestController)에서 별도 제공
 */
@Controller
@RequiredArgsConstructor
public class ReportPageController {

    /**
     * 리포트 페이지 진입
     * GET /reports
     * Header: X-ANON-ID
     *
     * - 헤더로 받은 anonId를 템플릿에 전달
     * - reports.html에서는 JS fetch 요청 시 이 anonId를 다시 헤더에 넣어 API 호출
     */
    @GetMapping("/reports")
    public String reportsPage(@RequestHeader("X-ANON-ID") String anonId, Model model) {
        model.addAttribute("anonId", anonId);
        return "reports"; // src/main/resources/templates/reports.html
    }
}