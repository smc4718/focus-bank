package com.pyj.focusbank.dto;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FocusAggDto {
    private String period;          // "2025-W36" 또는 "2025-09"
    private long totalSeconds;      // 합계(초)
    private long dayCount;          // 해당 기간에 기록이 있는 일수
    private long avgSecPerDay;      // 일당 평균(초)
}
