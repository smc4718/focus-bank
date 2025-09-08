package com.pyj.focusbank.dao;

import com.pyj.focusbank.dto.DailySummaryDto;
import com.pyj.focusbank.dto.FocusAggDto;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;
import java.util.List;

@Mapper
public interface ReportMapper {

    /**
     * 특정 anonId, 특정 날짜의 집중 요약 조회
     *
     * @param anonId 익명 사용자 ID
     * @param targetDate 대상 날짜 (YYYY-MM-DD)
     * @return DailySummaryDto (없으면 null)
     */
    DailySummaryDto findDailySummary(@Param("anonId") String anonId,
                                     @Param("targetDate") String targetDate);


    /**
     * 주간(ISO 기준: 월요일 시작) 단위 집계 결과를 반환합니다.
     *
     * @param anonId   익명 사용자 식별자 (ULID 26자리)
     * @param fromDate 집계 시작일(포함). daily_aggregate.target_date 기준
     * @param toDate   집계 종료일(포함). daily_aggregate.target_date 기준
     * @return 기간 내 각 주(period="YYYY-Www")별 합계/평균 등의 정보 목록
     *
     * <주의>
     * - SQL에서는 YEARWEEK(target_date, 3)를 사용하여 ISO 주차로 그룹핑.
     * - daily_aggregate에 없는 날짜는 결과에 나타나지 않으므로, 프론트에서 0 보간이 필요할 수 있음.
     */
    List<FocusAggDto> selectWeeklyAgg(@Param("anonId") String anonId,
                                      @Param("fromDate") LocalDate fromDate,
                                      @Param("toDate") LocalDate toDate);


    /**
     * 월 단위 집계 결과를 반환합니다.
     *
     * @param anonId   익명 사용자 식별자
     * @param fromDate 집계 시작일(포함)
     * @param toDate   집계 종료일(포함)
     * @return 기간 내 각 월(period="YYYY-MM")별 합계/평균 등의 정보 목록
     *
     * <주의>
     * - SQL에서는 DATE_FORMAT(target_date, '%Y-%m')으로 월 라벨을 생성/그룹핑합니다.
     */
    List<FocusAggDto> selectMonthlyAgg(@Param("anonId") String anonId,
                                       @Param("fromDate") LocalDate fromDate,
                                       @Param("toDate") LocalDate toDate);
}