package com.pyj.focusbank.dao;

import com.pyj.focusbank.dto.ProfileDto;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

@Mapper
public interface UserProfileMapper {

    /**
     * anonId로 사용자 1명 조회.
     * @param anonId 익명 식별자(필수)
     * @return 해당 사용자가 있으면 ProfileDto, 없으면 null
     */
    ProfileDto findByAnonId(@Param("anonId") String anonId);

    /**
     * 닉네임으로 사용자 1명 조회(중복 체크 용도).
     * @param nickname 닉네임
     * @return 닉네임이 이미 존재하면 ProfileDto, 없으면 null
     */
    ProfileDto findByNickname(@Param("nickname") String nickname);

    /**
     * 새 프로필 생성.
     * @param anonId 익명 식별자(필수)
     * @param nickname 닉네임(nullable 가능: 정책에 따라 null 허용 여부 결정)
     * @return 삽입된 행 수(정상 1)
     */
    int insert(@Param("anonId") String anonId, @Param("nickname") String nickname);

    /**
     * 기존 사용자의 닉네임만 수정.
     * @param anonId 대상 사용자 anonId
     * @param nickname 새 닉네임
     * @return 변경된 행 수(정상 1, 없으면 0)
     */
    int updateNickname(@Param("anonId") String anonId, @Param("nickname") String nickname);
}