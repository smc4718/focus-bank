// 파일: src/main/java/com/pyj/focusbank/dto/ProfileDto.java
package com.pyj.focusbank.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProfileDto {
    private String anonId;           // 익명 사용자 ID
    private String nickname;         // 닉네임
    private String nicknameTag;      // 닉네임 태그 (#1234 같은 4자리)
    private LocalDateTime createdAt; // 닉네임 생성일
    private LocalDateTime updatedAt; // 닉네임 수정일
}
