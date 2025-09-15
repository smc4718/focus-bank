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

    @Override
    public Optional<ProfileDto> findByAnonId(String anonId) {
        // 널 체크 및 트리밍
        Objects.requireNonNull(anonId, "anonId must not be null"); // 필수 파라미터 검증
        // 매퍼로 조회 수행
        ProfileDto dto = userProfileMapper.findByAnonId(anonId); // 존재하면 DTO, 없으면 null
        // Optional로 감싸서 반환
        return Optional.ofNullable(dto); // Optional 리턴
    }

    @Override
    public boolean isNicknameAvailable(String nickname) {
        // 닉네임 정규화(트림) 및 유효성 검사
        String normalized = normalize(nickname); // 앞뒤 공백 제거
        if (!isValidNickname(normalized)) { // 포맷/길이 검사
            return false; // 유효성 실패면 사용 불가
        }
        // DB에서 동일 닉네임 존재 여부 확인
        ProfileDto exists = userProfileMapper.findByNickname(normalized); // 닉네임 중복 조회
        // 없으면 사용 가능(true), 있으면 사용 불가(false)
        return (exists == null); // null이면 사용 가능
    }

    @Override
    @Transactional // 쓰기 작업은 readOnly=false 트랜잭션
    public ProfileDto upsertNickname(String anonId, String nickname) {
        // 필수값 검증
        Objects.requireNonNull(anonId, "anonId must not be null"); // anonId 필수
        // 닉네임 정규화
        String normalized = normalize(nickname); // 닉네임 트림
        // 유효성 검사(길이/문자셋)
        if (!isValidNickname(normalized)) { // 검증 실패 시
            throw new IllegalArgumentException("닉네임 형식이 올바르지 않습니다. (2~16자, 한글/영문/숫자/밑줄)"); // 400 취지
        }

        // 현재 닉네임이 DB에 존재하는지 확인
        ProfileDto nickOwner = userProfileMapper.findByNickname(normalized); // 같은 닉네임의 주인
        if (nickOwner != null && !anonId.equals(nickOwner.getAnonId())) { // 다른 사람이 이미 사용 중?
            // 다른 anonId가 쓴 닉네임이면 충돌
            throw new IllegalStateException("이미 사용 중인 닉네임입니다."); // 409 취지
        }

        // 내 프로필 존재 여부 확인
        ProfileDto mine = userProfileMapper.findByAnonId(anonId); // 내 레코드 조회
        if (mine == null) { // 없으면 INSERT
            // 새 DTO를 구성(선택: createdAt/updatedAt 세팅)
            ProfileDto created = new ProfileDto(); // 빈 DTO 생성
            created.setAnonId(anonId);             // anonId 세팅
            created.setNickname(normalized);       // 닉네임 세팅
            // created.setCreatedAt(OffsetDateTime.now()); // 컬럼이 트리거/DEFAULT면 생략 가능
            // DB에 INSERT 실행
            userProfileMapper.insert(anonId, normalized); // 1행 삽입
            // 최종 상태를 다시 조회해서 반환(생성 시점의 DB 값 보장)
            return userProfileMapper.findByAnonId(anonId); // 새 상태 반환
        } else { // 있으면 UPDATE
            // 닉네임이 동일하면 그대로 반환(멱등 처리)
            if (normalized.equals(mine.getNickname())) { // 변경 없음
                return mine; // 현재 상태 그대로 반환
            }
            // 닉네임만 업데이트
            userProfileMapper.updateNickname(anonId, normalized); // UPDATE 1행
            // 최종 상태 조회 후 반환
            return userProfileMapper.findByAnonId(anonId); // 변경 반영 상태 반환
        }
    }

    // ===== 내부 유틸 =====

    /**
     * 닉네임 정규화: null 방지 및 trim.
     */
    private String normalize(String nickname) {
        return nickname == null ? "" : nickname.trim(); // null이면 빈 문자열, 아니면 trim
    }

    /**
     * 닉네임 유효성 검사.
     * <p>정책: 2~16자, 한글/영문/숫자/밑줄만 허용(공백/특수문자 불가).
     */
    private boolean isValidNickname(String nickname) {
        if (nickname.isEmpty()) return false; // 빈 문자열 불가
        if (nickname.length() < 2 || nickname.length() > 16) return false; // 길이 제한
        // 정규식: 한글 가-힣, 영문 대소문자, 숫자, 밑줄(_)만 허용
        return nickname.matches("^[A-Za-z0-9가-힣_]{2,16}$"); // 패턴 일치 여부
    }
}