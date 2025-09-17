package com.pyj.focusbank.service;

import com.pyj.focusbank.dao.RankingMapper;
import com.pyj.focusbank.dto.RankingDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class RankingServiceImpl implements RankingService {

    private final RankingMapper rankingMapper;

    /** 타임존 (KST) */
    private static final ZoneId KST = ZoneId.of("Asia/Seoul");

    /**
     * 이번 주(월~오늘) TOP N 랭킹 조회
     */
    @Override
    public List<RankingDto> getWeeklyRanking(int limit) {
        LocalDate today = LocalDate.now(KST);
        LocalDate monday = today.with(DayOfWeek.MONDAY);

        // Mapper에서 Map 리스트 조회 후 DTO 변환
        List<Map<String, Object>> rows = rankingMapper.selectWeeklyRanking(monday, today, limit);
        return mapToRankingDto(rows);
    }

    /**
     * 전체 누적 TOP N 랭킹 조회
     */
    @Override
    public List<RankingDto> getOverallRanking(int limit) {
        List<Map<String, Object>> rows = rankingMapper.selectOverallRanking(limit);
        return mapToRankingDto(rows);
    }

    /**
     * Map 리스트 → RankingDto 리스트 변환
     * 닉네임/태그가 있으면 "닉네임#태그", 없으면 "anon-XXXX" 표시
     */
    private List<RankingDto> mapToRankingDto(List<Map<String, Object>> rows) {
        List<RankingDto> list = new ArrayList<>();
        int rank = 1;

        for (Map<String, Object> row : rows) {
            String anonId = (String) row.get("anon_id");

            Number secNum = (Number) row.get("total_seconds");
            int seconds = (secNum != null) ? secNum.intValue() : 0;

            String nickname = (String) row.get("nickname");
            String tag = (String) row.get("nickname_tag");

            String displayName;
            if (nickname != null && tag != null) {
                displayName = nickname + "#" + tag;
            } else {
                displayName = maskAnon(anonId);
            }

            list.add(new RankingDto(rank++, anonId, displayName, seconds));
        }
        return list;
    }

    /**
     * 익명 마스킹 유틸 ('anon-XXXX')
     */
    private String maskAnon(String id) {
        if (id == null || id.length() < 4) return "anon-****";
        return "anon-" + id.substring(id.length() - 4).toUpperCase();
    }
}