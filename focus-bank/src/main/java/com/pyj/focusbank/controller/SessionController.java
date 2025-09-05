package com.pyj.focusbank.controller;

import com.pyj.focusbank.dto.FocusSessionDto;
import com.pyj.focusbank.service.SessionService;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/sessions")
public class SessionController {

    private final SessionService sessionService;

    /**
     * 집중 시작(입금)
     * 헤더: X-ANON-ID: <ULID or anon-****>
     */
    @PostMapping("/deposit")
    public ResponseEntity<FocusSessionDto> deposit(@RequestHeader("X-ANON-ID") String anonId) {
        // 방어: 공백/빈값 거르기 (서비스에서도 trim 하지만 여기서 1차 차단)
        if (anonId == null || anonId.trim().isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        FocusSessionDto session = sessionService.startFocus(anonId);

        // 201 Created + Location 헤더(optional)
        HttpHeaders headers = new HttpHeaders();
        headers.setLocation(URI.create("/api/sessions/" + session.getSessionId()));
        return new ResponseEntity<>(session, headers, HttpStatus.CREATED);
    }

    /**
     * 집중 종료(정산)
     * 쿼리파라미터: sessionId
     */
    @PostMapping("/settle")
    public ResponseEntity<FocusSessionDto> settle(@RequestParam("sessionId") Long sessionId) {
        FocusSessionDto session = sessionService.endFocus(sessionId);
        if (session == null) {
            // 이미 종료됐거나 없는 세션
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
        return ResponseEntity.ok(session);
    }


    /*
       아래는 공통 예외 핸들링
    */

    // 잘못된 입력값 등
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ProblemDetail> handleBadRequest(IllegalArgumentException ex) {
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, ex.getMessage());
        pd.setTitle("Bad Request");
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(pd);
    }

    // 비즈니스 룰 위반(부모 없음, 이미 열린/종료된 세션 등)
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ProblemDetail> handleConflict(IllegalStateException ex) {
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, ex.getMessage());
        pd.setTitle("Conflict");
        return ResponseEntity.status(HttpStatus.CONFLICT).body(pd);
    }

    // DB 무결성 위반(FK 등) — 정상 흐름에선 안 나와야 하지만 방어
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ProblemDetail> handleFK(DataIntegrityViolationException ex) {
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(
                HttpStatus.CONFLICT,
                "데이터 무결성 위반: FK 제약을 확인하세요."
        );
        pd.setTitle("Integrity Violation");
        pd.setProperty("hint", "anonymous_user에 anonId가 존재해야 합니다.");
        return ResponseEntity.status(HttpStatus.CONFLICT).body(pd);
    }

    // 예외 미처리 fallback
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleEtc(Exception ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "internal_server_error", "message", ex.getMessage()));
    }
}