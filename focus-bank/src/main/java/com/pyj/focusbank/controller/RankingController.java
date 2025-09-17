package com.pyj.focusbank.controller;

import com.pyj.focusbank.dto.RankingDto;
import com.pyj.focusbank.service.RankingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/rankings")
@RequiredArgsConstructor
public class RankingController {

    private final RankingService rankingService;

    /**
     * 이번 주(월~오늘) 랭킹 TOP N을 반환한다.
     *
     * @param limit 상위 N명 (미지정 시 10)
     * @return 정렬된 랭킹 리스트(JSON)
     */
    @GetMapping("/weekly")
    public ResponseEntity<List<RankingDto>> weekly(@RequestParam(defaultValue = "10") int limit) {
        // 간단한 방어: 1~100 사이만 허용 (필요 시 조정)
        int safeLimit = Math.min(Math.max(limit, 1), 100);
        return ResponseEntity.ok(rankingService.getWeeklyRanking(safeLimit));
    }

    /**
     * 전체 누적 랭킹 TOP N을 반환한다.
     *
     * @param limit 상위 N명 (미지정 시 10)
     * @return 정렬된 랭킹 리스트(JSON)
     */
    @GetMapping("/overall")
    public ResponseEntity<List<RankingDto>> overall(@RequestParam(defaultValue = "10") int limit) {
        int safeLimit = Math.min(Math.max(limit, 1), 100);
        return ResponseEntity.ok(rankingService.getOverallRanking(safeLimit));
    }
}