package com.pyj.focusbank.controller;

import com.pyj.focusbank.dto.FocusSessionDto;
import com.pyj.focusbank.service.SessionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/sessions")
public class SessionController {

    private final SessionService sessionService;

    /**
     * 집중 시작(입금)
     * 헤더: X-ANON-ID: <ULID>
     */
    @PostMapping("/deposit")
    public ResponseEntity<FocusSessionDto> deposit(@RequestHeader("X-ANON-ID") String anonId) {
        FocusSessionDto session = sessionService.startFocus(anonId);
        return ResponseEntity.ok(session);
    }

    /**
     * 집중 종료(출금)
     * 쿼리파라미터: sessionId
     */
    @PostMapping("/withdraw")
    public ResponseEntity<FocusSessionDto> withdraw(@RequestParam("sessionId") Long sessionId) {
        FocusSessionDto session = sessionService.endFocus(sessionId);
        return ResponseEntity.ok(session);
    }
}