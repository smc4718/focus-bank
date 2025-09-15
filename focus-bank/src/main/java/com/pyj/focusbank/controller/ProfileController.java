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

/**
 * 프로필(익명 사용자) 컨트롤러.
 *
 * <p>주요 역할:
 * <ul>
 *   <li>내 프로필 조회 (GET /api/profile)</li>
 *   <li>닉네임 사용 가능 여부 확인(사유 코드 포함) (GET /api/profile/check)</li>
 *   <li>닉네임 생성/변경(Upsert) (POST /api/profile/nickname)</li>
 * </ul>
 *
 * <p>인증/식별은 간단히 헤더 {@code X-ANON-ID} 로 식별합니다.
 * 실제 서비스에서는 인증 토큰을 통해 anonId를 매핑하는 방식으로 대체하는 것이 바람직합니다.
 *
 * <p>오류 응답 전략:
 * <ul>
 *   <li>유효성 실패 → 400 Bad Request (reason: {@code invalid-format})</li>
 *   <li>중복 충돌 → 409 Conflict (reason: {@code duplicate})</li>
 * </ul>
 *
 * <p>프론트엔드는 {@code reason} 코드를 기반으로 적절한 한국어 메시지를 보여줄 수 있습니다.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/profile")
public class ProfileController {

    /** 비즈니스 로직(유효성/중복검사/Upsert)을 담당하는 서비스 빈 */
    private final ProfileService profileService;

    /**
     * 내 프로필 조회 API.
     *
     * <p>요청 헤더 {@code X-ANON-ID} 로 본인의 익명 식별자를 전달해야 합니다.
     * 해당 사용자의 프로필이 DB에 없다면 {@code 204 No Content} 를 반환합니다.
     *
     * <pre>
     * 요청:
     *   GET /api/profile
     *   X-ANON-ID: 01H...ULID...
     *
     * 응답:
     *   200 OK + ProfileDto(JSON)
     *   204 No Content (미존재)
     * </pre>
     *
     * @param anonId 요청 헤더의 익명 식별자(필수)
     * @return 존재 시 {@code 200 OK + ProfileDto}, 미존재 시 {@code 204 No Content}
     */
    @GetMapping // GET /api/profile
    public ResponseEntity<ProfileDto> getMyProfile(
            @RequestHeader("X-ANON-ID") String anonId // 헤더에서 anonId 추출
    ) {
        // 서비스로 조회를 위임하고 Optional을 그대로 응답에 매핑
        return profileService.findByAnonId(anonId)     // Optional<ProfileDto>
                .map(ResponseEntity::ok)               // 값이 있으면 200 OK
                .orElseGet(() -> ResponseEntity.noContent().build()); // 없으면 204
    }

    /**
     * 닉네임 사용 가능 여부 확인 API.
     *
     * <p>프론트엔드에서 실시간 중복/유효성 체크할 때 사용합니다.</p>
     *
     * @param nickname 검사할 닉네임 (쿼리 파라미터)
     * @return JSON 형태의 응답 맵
     *   - available: true/false
     *   - reason: "invalid-format" | "duplicate" (실패 시에만 포함)
     */
    @GetMapping("/check")
    public Map<String, Object> checkNickname(
            @RequestParam("nickname") String nickname
    ) {
        // 1) 입력 정규화: null 방지 + 앞뒤 공백 제거
        String normalized = nickname == null ? "" : nickname.trim();

        // 2) 유효성 검사: 길이, 허용 문자(한글/영문/숫자/밑줄) 확인
        if (!profileService.isValidNickname(normalized)) {
            // 형식이 잘못된 경우 → 사용 불가 + 사유코드 "invalid-format"
            return Map.of(
                    "available", false,
                    "reason", "invalid-format"
            );
        }

        // 3) 중복 검사: 이미 다른 사용자가 동일 닉네임을 쓰고 있는지 확인
        if (!profileService.isNicknameAvailable(normalized)) {
            // 중복된 경우 → 사용 불가 + 사유코드 "duplicate"
            return Map.of(
                    "available", false,
                    "reason", "duplicate"
            );
        }

        // 4) 모든 검사를 통과한 경우 → 사용 가능
        return Map.of("available", true);
    }

    /**
     * 닉네임 생성/변경(Upsert) API.
     *
     * 헤더 {@code X-ANON-ID} 기준으로 자신의 프로필을 INSERT 또는 UPDATE 합니다.
     * - 최초 요청: INSERT (태그 자동 부여)
     * - 이후 변경: UPDATE (태그는 고정, 닉네임만 변경)
     *
     * 성공 시 200 OK + ProfileDto(JSON),
     * 실패 시 400(형식 오류), 409(중복) 상태코드 반환.
     *
     * @param anonId 요청 헤더의 익명 식별자
     * @param body   {"nickname": "..."} 형식의 요청 바디
     * @return 저장된 최종 ProfileDto
     */
    @PostMapping("/nickname")
    public ProfileDto setNickname(
            @RequestHeader("X-ANON-ID") String anonId,
            @RequestBody NicknameRequest body
    ) {
        try {
            // 서비스 호출로 upsert 수행 (유효성/중복 검사는 서비스 내부에서 처리)
            return profileService.upsertNickname(anonId, body.nickname);

        } catch (IllegalArgumentException e) {
            // 유효성 실패 → 400 + reason 코드 포함된 메시지로 변환
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "invalid-format: " + e.getMessage()
            );

        } catch (IllegalStateException e) {
            // 중복 충돌 → 409 + reason 코드 포함된 메시지로 변환
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "duplicate: " + e.getMessage()
            );
        }
    }

    /**
     * 닉네임 요청 바디 DTO.
     *
     * <p>클라이언트가 보낸 JSON 의 {@code nickname} 필드를
     * 자바 객체로 역직렬화하기 위한 용도입니다.
     *
     * <pre>
     * 예) { "nickname": "홍길동" }
     * </pre>
     */
    @Data
    public static class NicknameRequest {
        /**
         * 사용자가 지정하고자 하는 닉네임(원본 문자열).
         * <ul>
         *   <li>null/빈문자/공백 처리 → 컨트롤러/서비스에서 정규화 및 검증</li>
         *   <li>중복 불가 조건 적용 → 저장 전 중복 검사</li>
         * </ul>
         */
        private String nickname;
    }
}