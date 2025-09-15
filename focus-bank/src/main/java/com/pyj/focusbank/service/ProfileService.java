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
     * 닉네임으로 프로필 조회.
     * <p>사유 코드(reason) 체크 용도 및 내부 비즈니스 검증에 사용됩니다.</p>
     * @param nickname 닉네임 문자열
     * @return 존재하면 ProfileDto를 Optional로 감싸 반환, 없으면 Optional.empty()
     */
    Optional<ProfileDto> findByNickname(String nickname);

    /**
     * 닉네임 사용 가능 여부 확인.
     * <p>실제로는 닉네임 자체의 중복 여부를 확인하며,
     * 태그는 시스템이 자동 부여하기 때문에 사용자는 신경 쓸 필요가 없습니다.
     * <p>사전 유효성 검사(포맷/길이)도 함께 수행
     *
     * @param nickname 검사할 닉네임(원본 문자열)
     * @return 사용 가능하면 true, 사용 불가(유효성 실패 또는 이미 동일 닉네임이 존재)면 false
     */
    boolean isNicknameAvailable(String nickname);

    /**
     * 닉네임 유효성 검사.
     * <p>정책: 2~16자, 한글/영문/숫자/밑줄(_)만 허용.</p>
     *
     * @param nickname 검사할 닉네임
     * @return true = 유효 / false = 유효하지 않음
     */
    boolean isValidNickname(String nickname); // 🔹 추가

    /**
     * 닉네임 생성/변경(Upsert) 처리.
     * <p>처리 순서:
     * <ol>
     *     <li>닉네임 유효성 검사 (길이, 금지어 등)</li>
     *     <li>중복 검사 (이미 같은 닉네임이 존재하면 실패)</li>
     *     <li>해당 anonId가 최초라면 INSERT + 랜덤 태그 부여</li>
     *     <li>이미 존재하면 UPDATE (닉네임만 변경, 태그는 유지)</li>
     * </ol>
     *
     * @param anonId 대상 사용자 익명 식별자
     * @param nickname 설정할 닉네임(원본 문자열)
     * @return 최종 저장된 ProfileDto
     * @throws IllegalArgumentException 닉네임 유효성 실패
     * @throws IllegalStateException 닉네임 중복(다른 사람이 이미 사용 중)
     */
    ProfileDto upsertNickname(String anonId, String nickname);
}