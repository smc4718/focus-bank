package com.pyj.focusbank.controller;

import com.pyj.focusbank.dto.FocusSessionDto;
import com.pyj.focusbank.service.SessionService;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/sessions")
public class SessionController {

    private final SessionService sessionService;

    /**
     * 특정 날짜의 세션 목록 조회
     * @param anonId 익명 사용자 ID (헤더)
     * @param date 조회할 날짜 (yyyy-MM-dd)
     * @return 해당 날짜에 기록된 세션 목록
     */
    @GetMapping
    public List<FocusSessionDto> listSessions(
            @RequestHeader("X-ANON-ID") String anonId,              // 요청 헤더에서 익명 사용자 ID 추출
            @RequestParam("date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date // 쿼리 파라미터(date) 파싱
    ) {
        // Service 계층에 위임하여 특정 날짜의 세션 목록 조회
        return sessionService.getSessionsForDate(anonId, date);
    }

    /**
     * 집중 시작(입금)
     * 헤더: X-ANON-ID: <ULID or anon-****>
     */
    @PostMapping("/deposit")
    public ResponseEntity<FocusSessionDto> deposit(@RequestHeader("X-ANON-ID") String anonId) {
        // 1. 방어 로직: anonId가 null이거나 공백이면 400 Bad Request 응답
        if (anonId == null || anonId.trim().isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        // 2. 서비스 호출 → 새로운 세션 시작
        FocusSessionDto session = sessionService.startFocus(anonId);

        // 3. 201 Created 상태 코드 + Location 헤더 설정
        HttpHeaders headers = new HttpHeaders();
        headers.setLocation(URI.create("/api/sessions/" + session.getSessionId()));

        // 4. 생성된 세션 정보를 응답 본문으로 반환
        return new ResponseEntity<>(session, headers, HttpStatus.CREATED);
    }

    /**
     * 집중 종료(정산)
     * 쿼리파라미터: sessionId
     */
    @PostMapping("/settle")
    public ResponseEntity<FocusSessionDto> settle(@RequestParam("sessionId") Long sessionId) {
        // 1. 서비스 호출 → 세션 종료 시도
        FocusSessionDto session = sessionService.endFocus(sessionId);

        // 2. 종료 결과가 null이면 이미 종료됐거나 없는 세션 → 409 Conflict
        if (session == null) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }

        // 3. 정상적으로 종료된 세션 정보 반환 (200 OK)
        return ResponseEntity.ok(session);
    }


    /*
       아래는 공통 예외 핸들링
    */

    // 잘못된 입력값 처리 (예: anonId null/빈 문자열 등)
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ProblemDetail> handleBadRequest(IllegalArgumentException ex) {
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, ex.getMessage());
        pd.setTitle("Bad Request"); // 에러 제목
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(pd);
    }

    // 비즈니스 규칙 위반 처리 (예: 이미 열린/종료된 세션, 부모 없음 등)
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ProblemDetail> handleConflict(IllegalStateException ex) {
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, ex.getMessage());
        pd.setTitle("Conflict"); // 에러 제목
        return ResponseEntity.status(HttpStatus.CONFLICT).body(pd);
    }

    // DB 무결성 위반 처리 (예: FK 제약 위반)
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ProblemDetail> handleFK(DataIntegrityViolationException ex) {
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(
                HttpStatus.CONFLICT,
                "데이터 무결성 위반: FK 제약을 확인하세요."
        );
        pd.setTitle("Integrity Violation");
        pd.setProperty("hint", "anonymous_user에 anonId가 존재해야 합니다."); // 추가 힌트 제공
        return ResponseEntity.status(HttpStatus.CONFLICT).body(pd);
    }

    // 기타 처리되지 않은 예외 처리 (fallback)
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleEtc(Exception ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of(
                        "error", "internal_server_error",
                        "message", ex.getMessage() // 예외 메시지 그대로 전달
                ));
    }
}