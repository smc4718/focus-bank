package com.pyj.focusbank.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RankingDto {
    private int rank;            // 순위
    private String anonId;       // 사용자 식별자
    private String displayName;  // 닉네임#태그 or anon-XXXX
    private int seconds;         // 집중 시간(초)
}