package com.pyj.focusbank.controller;

import com.pyj.focusbank.dto.ProfileDto;
import com.pyj.focusbank.service.ProfileService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/profile")
public class ProfileController {

    private final ProfileService profileService;

    /**
     * 내 프로필 조회.
     *
     * <p>헤더 X-ANON-ID로 본인의 anonId를 전달해야 하며,
     *  DB에 프로필이 없으면 204 No Content를 반환합니다.
     *
     * @param anonId 요청 헤더의 익명 식별자
     * @return 200 OK + ProfileDto 또는 204 No Content
     */
    @GetMapping // GET /api/profile
    public ResponseEntity<ProfileDto> getMyProfile(
            @RequestHeader("X-ANON-ID") String anonId
    ) {
        // 서비스에서 조회 시도
        return profileService.findByAnonId(anonId) // Optional<ProfileDto>
                .map(ResponseEntity::ok)           // 있으면 200 OK
                .orElseGet(() -> ResponseEntity.noContent().build()); // 없으면 204
    }

    /**
     * 닉네임 사용 가능 여부 확인(프론트의 실시간 중복 체크용).
     *
     * <p>유효성 실패도 available=false로 반환합니다.
     *
     * @param nickname 확인할 닉네임(쿼리 파라미터)
     * @return {"available": true/false}
     */
    @GetMapping("/check") // GET /api/profile/check?nickname=xxx
    public Map<String, Boolean> checkNickname(
            @RequestParam("nickname") String nickname // 검사 대상 닉네임
    ) {
        // 서비스로 위임하여 사용 가능 여부 판단
        boolean available = profileService.isNicknameAvailable(nickname); // true/false
        // 단순 JSON 형태로 반환
        return Map.of("available", available); // {"available": ...}
    }

    /**
     * 닉네임 생성/변경(Upsert).
     *
     * <p>헤더 X-ANON-ID를 기준으로 본인의 프로필을 INSERT 또는 UPDATE 합니다.
     * <br/>에러 처리:
     * <ul>
     *   <li>유효성 실패: 400 Bad Request</li>
     *   <li>중복(다른 유저가 사용 중): 409 Conflict</li>
     * </ul>
     *
     * @param anonId 요청 헤더의 익명 식별자
     * @param body   {"nickname": "원하는닉네임"}
     * @return 저장된 최종 ProfileDto
     */
    @PostMapping("/nickname") // POST /api/profile/nickname
    public ProfileDto setNickname(
            @RequestHeader("X-ANON-ID") String anonId, // 나의 anonId
            @RequestBody NicknameRequest body           // 요청 바디
    ) {
        try { // 예외 → 상태코드 변환
            // 서비스 호출로 upsert 수행
            return profileService.upsertNickname(anonId, body.nickname); // 최종 DTO 반환
        } catch (IllegalArgumentException e) { // 유효성 실패
            // 400 Bad Request로 변환하여 응답
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage()); // 400
        } catch (IllegalStateException e) { // 중복 등 비즈니스 충돌
            // 409 Conflict로 변환하여 응답
            throw new ResponseStatusException(HttpStatus.CONFLICT, e.getMessage()); // 409
        }
    }

    /**
     * 닉네임 요청 DTO
     *
     * - 클라이언트에서 JSON 형태로 넘어온 닉네임 값을
     *   자바 객체로 변환하기 위해 사용한다.
     * - 예: { "nickname": "홍길동" } → NicknameRequest.nickname = "홍길동"
     */
    @Data
    public static class NicknameRequest {

        /**
         * 사용자가 지정하고자 하는 닉네임
         * - null/빈문자 검증은 Controller나 Service에서 수행
         * - 중복 불가 조건이 있으므로 저장 전 중복체크 필요
         */
        private String nickname;
    }
}