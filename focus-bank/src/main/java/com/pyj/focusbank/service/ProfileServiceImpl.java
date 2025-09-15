package com.pyj.focusbank.service;

import com.pyj.focusbank.dao.UserProfileMapper;
import com.pyj.focusbank.dto.ProfileDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Objects;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProfileServiceImpl implements ProfileService {

    private final UserProfileMapper userProfileMapper;

    /**
     * anonId로 프로필을 조회한다.
     *
     * @param anonId 익명 식별자(ULID 26자리 등)
     * @return 존재하면 ProfileDto, 없으면 Optional.empty()
     * @throws NullPointerException anonId가 null인 경우
     */
    @Override
    public Optional<ProfileDto> findByAnonId(String anonId) {
        // (1) 필수 파라미터 검증: null 금지
        Objects.requireNonNull(anonId, "anonId must not be null");

        // (2) 매퍼 호출: 존재 시 DTO, 없으면 null
        ProfileDto dto = userProfileMapper.findByAnonId(anonId);

        // (3) Optional로 감싸서 반환(호출 측에서 NPE 없이 처리)
        return Optional.ofNullable(dto);
    }

    /**
     * 닉네임으로 프로필을 조회한다. (중복 검사 등에서 사용)
     *
     * @param nickname 조회할 닉네임(원본)
     * @return 존재하면 ProfileDto, 없으면 Optional.empty()
     */
    @Override
    public Optional<ProfileDto> findByNickname(String nickname) {
        // (1) null-safe 정규화: null → "" 로, 공백 제거
        String normalized = normalize(nickname);

        // (2) 빈 문자열이라면 조회 의미 없음 → empty 반환
        if (normalized.isEmpty()) return Optional.empty();

        // (3) 매퍼 호출: 있으면 DTO, 없으면 null
        ProfileDto dto = userProfileMapper.findByNickname(normalized);

        // (4) Optional로 래핑
        return Optional.ofNullable(dto);
    }

    /**
     * 닉네임 사용 가능 여부(유효성 + 중복)를 확인한다.
     *
     * @param nickname 검사할 닉네임(원본)
     * @return 사용 가능하면 true, 아니면 false
     */
    @Override
    public boolean isNicknameAvailable(String nickname) {
        // (1) 정규화: 앞/뒤 공백 제거
        String normalized = normalize(nickname);

        // (2) 유효성 검사: 길이/문자셋 정책 위반 시 사용 불가
        if (!isValidNickname(normalized)) {
            return false;
        }

        // (3) 중복 검사: 동일 닉네임이 이미 존재하면 사용 불가
        ProfileDto exists = userProfileMapper.findByNickname(normalized);

        // (4) null(없음)이면 사용 가능, 있으면 불가
        return (exists == null);
    }

    /**
     * 닉네임 Upsert(생성/변경) 처리.
     *
     * <p>동작 규칙:
     * <ul>
     *   <li>닉네임 없으면 INSERT (태그 4자리 자동 부여)</li>
     *   <li>이미 있으면 UPDATE (태그는 고정, 닉네임만 변경)</li>
     * </ul>
     *
     * @param anonId  익명 식별자
     * @param nickname 설정하고자 하는 닉네임(원본)
     * @return 최종 저장된 ProfileDto(INSERT/UPDATE 이후 DB에서 재조회)
     * @throws NullPointerException anonId가 null인 경우
     * @throws IllegalArgumentException 닉네임 유효성 실패 시
     * @throws IllegalStateException 닉네임 중복(타 유저가 사용 중) 시
     */
    @Override
    @Transactional // 트랜잭션 readOnly=false
    public ProfileDto upsertNickname(String anonId, String nickname) {
        // ===== 1) 필수값 검증 =====
        Objects.requireNonNull(anonId, "anonId must not be null"); // anonId는 반드시 필요

        // ===== 2) 닉네임 정규화 및 유효성 검사 =====
        String normalized = normalize(nickname); // 앞뒤 공백 제거
        if (!isValidNickname(normalized)) {      // 길이/문자셋 정책 위반 확인
            throw new IllegalArgumentException("닉네임 형식이 올바르지 않습니다. (2~16자, 한글/영문/숫자/밑줄)");
        }

        // ===== 3) 닉네임 중복 검사 =====
        // 동일 닉네임을 가진 다른 사용자가 존재하는지 확인
        ProfileDto nickOwner = userProfileMapper.findByNickname(normalized);
        if (nickOwner != null && !anonId.equals(nickOwner.getAnonId())) {
            // 다른 사람이 이미 사용중 → 중복
            throw new IllegalStateException("이미 사용 중인 닉네임입니다.");
        }

        // ===== 4) 내 프로필 존재 여부 확인 =====
        ProfileDto mine = userProfileMapper.findByAnonId(anonId);

        if (mine == null) {
            // ---------- [INSERT] 최초 등록 ----------
            // 시스템이 4자리 태그 자동 생성 (예: "0042")
            String nicknameTag = generateTag();

            // DB에 새 사용자 삽입 (닉네임 + 태그)
            userProfileMapper.insert(anonId, normalized, nicknameTag);

            // 최종 상태를 DB에서 다시 조회해서 반환 (DB 값 기준으로 일원화)
            return userProfileMapper.findByAnonId(anonId);

        } else {
            // ---------- [UPDATE] 기존 사용자 ----------
            // 닉네임이 동일하면 변경 불필요 → 현재 상태 그대로 반환(멱등성)
            if (normalized.equals(mine.getNickname())) {
                return mine;
            }

            // 닉네임만 업데이트 (태그는 고정/불변)
            userProfileMapper.updateNickname(anonId, normalized);

            // 변경된 최신 상태를 DB에서 재조회 후 반환
            return userProfileMapper.findByAnonId(anonId);
        }
    }

    /**
     * 시스템 자동 태그 생성기.
     *
     * <p>0001 ~ 9999 범위의 4자리 숫자 문자열을 랜덤 생성한다.
     * 예: "0042", "9876"
     *
     * @return 4자리 문자열 태그
     */
    private String generateTag() {
        // (1) 1~9999 사이의 난수 생성
        int num = (int) (Math.random() * 9999) + 1;

        // (2) 항상 4자리로 0 패딩
        return String.format("%04d", num);
    }

    /**
     * 닉네임 유효성 검사.
     *
     * <p>정책:
     * <ul>
     *   <li>길이: 2~16자</li>
     *   <li>허용문자: 한글, 영문 대소문자, 숫자, 밑줄(_)</li>
     *   <li>공백/기타 특수문자 불가</li>
     * </ul>
     *
     * @param nickname 검사 대상 닉네임(원본 또는 정규화 문자열)
     * @return 정책에 부합하면 true, 아니면 false
     */
    @Override
    public boolean isValidNickname(String nickname) {
        // (1) null/빈 문자열 방지
        if (nickname == null || nickname.isEmpty()) return false;

        // (2) 길이 제한 확인
        if (nickname.length() < 2 || nickname.length() > 16) return false;

        // (3) 허용 문자셋 확인(정규식)
        return nickname.matches("^[A-Za-z0-9가-힣_]{2,16}$");
    }

    // ===================== 내부 유틸 =====================

    /**
     * 닉네임 정규화: null-safe trim.
     *
     * @param nickname 원본 닉네임(널 허용)
     * @return null → "" 로 치환, 그 외에는 앞/뒤 공백 제거
     */
    private String normalize(String nickname) {
        // (1) null이면 빈 문자열 반환
        // (2) 아니면 trim() 적용
        return nickname == null ? "" : nickname.trim();
    }
}