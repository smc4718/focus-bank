// 파일: src/main/java/com/pyj/focusbank/dto/ProfileDto.java
package com.pyj.focusbank.dto;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * user_profile 한 행을 그대로 담는 DTO.
 */
@Data
public class ProfileDto {
    private String anonId;            // 익명ID
    private String nickname;          // 닉네임
    private LocalDateTime createdAt;  // 닉네임 생성일
    private LocalDateTime updatedAt;  // 닉네임 수정일
}