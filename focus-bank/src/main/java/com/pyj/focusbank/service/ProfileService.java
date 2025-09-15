package com.pyj.focusbank.service;

import com.pyj.focusbank.dto.ProfileDto;
import java.util.Optional;

public interface ProfileService {

    /**
     * anonId로 프로필 조회.
     * @param anonId 익명 식별자
     * @return 존재하면 ProfileDto를 Optional로 감싸서 반환, 없으면 Optional.empty()
     */
    Optional<ProfileDto> findByAnonId(String anonId);

    /**
     * 닉네임 사용 가능 여부 확인.
     * <p>사전 유효성 검사(포맷/길이)도 함께 수행합니다.
     * @param nickname 검사할 닉네임(원본 문자열)
     * @return 사용 가능하면 true, 사용 불가(유효성 실패 또는 중복)면 false
     */
    boolean isNicknameAvailable(String nickname);

    /**
     * 닉네임 생성/변경(Upsert) 처리.
     * <p>유효성 검사 → 중복 검사 → 존재 여부에 따라 insert/update.
     * <p>중복일 때는 IllegalStateException(또는 하위 구현체에서 다른 예외) 발생을 권장합니다.
     *
     * @param anonId 대상 사용자 익명 식별자
     * @param nickname 설정할 닉네임(원본 문자열)
     * @return 최종 저장된 ProfileDto
     * @throws IllegalArgumentException 닉네임 유효성 실패
     * @throws IllegalStateException 닉네임 중복(다른 사람이 이미 사용 중)
     */
    ProfileDto upsertNickname(String anonId, String nickname);
}