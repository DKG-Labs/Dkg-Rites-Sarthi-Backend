package com.sarthi.Sms.repository.dashboard;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.sarthi.Sms.entity.dashboard.DimensionalInspectionEntity;
@Repository
public interface DimensionalInspectionRepository extends JpaRepository<DimensionalInspectionEntity, Integer>{

    @Query("SELECT e.railId FROM DimensionalInspectionEntity e WHERE e.inspectedAt BETWEEN :startDate AND :endDate")
    List<Object[]> findBetweenStartDtAndEndDt(@Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate);

    @Query("SELECT e.railId FROM DimensionalInspectionEntity e WHERE e.inspectedAt BETWEEN :startDate AND :endDate AND e.shift = :shift")
    List<Object[]> findByStartDateAndShift(@Param("startDate") LocalDateTime startDate,@Param("endDate") LocalDateTime endDate ,@Param("shift") String shift);

   @Query("SELECT e FROM DimensionalInspectionEntity e WHERE e.railId =:railId")
    List<DimensionalInspectionEntity> findDtlByRailId(@Param("railId") String railId);
}