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
     * 이번 주(월~오늘) TOP N 랭킹.
     * <p>LocalDate.now(KST)로 오늘을 계산하고, ISO 기준으로 monday를 구해 Mapper에 전달합니다.</p>
     */
    @Override
    public List<RankingDto> getWeeklyRanking(int limit) {
        // 오늘(현지: KST) 및 이번 주 월요일 계산
        LocalDate today = LocalDate.now(KST);
        LocalDate monday = today.with(DayOfWeek.MONDAY);

        // Mapper에서 결과를 받아와 DTO로 변환
        List<Map<String, Object>> rows = rankingMapper.selectWeeklyRanking(monday, today, limit);
        return mapToRankingDto(rows);
    }

    /**
     * 전체 누적 TOP N 랭킹.
     */
    @Override
    public List<RankingDto> getOverallRanking(int limit) {
        List<Map<String, Object>> rows = rankingMapper.selectOverallRanking(limit);
        return mapToRankingDto(rows);
    }

    /**
     * Mapper 결과(Map 리스트)를 {@link RankingDto} 리스트로 매핑한다.
     *
     * <p>닉네임/태그가 모두 존재하면 '닉네임#태그'로, 아니면 anon-XXXX 형식으로 표시명을 만든다.</p>
     *
     * @param rows select 결과 (각 Map은 컬럼명 → 값)
     * @return 정렬 + 순위(rank) 부여된 DTO 리스트
     */
    private List<RankingDto> mapToRankingDto(List<Map<String, Object>> rows) {
        List<RankingDto> list = new ArrayList<>();
        int rank = 1;

        for (Map<String, Object> row : rows) {
            // anon_id (ULID)
            String anonId = (String) row.get("anon_id");

            // SUM(total_seconds) → Number로 받아 int로 캐스팅 (NULL 방어)
            Number secNum = (Number) row.get("total_seconds");
            int seconds = secNum != null ? secNum.intValue() : 0;

            // 닉네임/태그 (LEFT JOIN이므로 null일 수 있음)
            String nickname = (String) row.get("nickname");
            String tag = (String) row.get("nickname_tag");

            // 표시명 결정: 닉네임이 있으면 "닉네임#태그", 없으면 "anon-XXXX"
            String displayName = (nickname != null && tag != null)
                    ? (nickname + "#" + tag)
                    : maskAnon(anonId);

            list.add(new RankingDto(rank++, anonId, displayName, seconds));
        }
        return list;
    }

    /**
     * 익명 마스킹 ('anon-XXXX') 유틸.
     *
     * @param id ULID 등 식별자
     * @return 뒤 4자리를 대문자로 노출한 마스킹 문자열
     */
    private String maskAnon(String id) {
        if (id == null || id.length() < 4) return "anon-****";
        return "anon-" + id.substring(id.length() - 4).toUpperCase();
    }
}