package com.sarthi.Sms.repository.dashboard;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.sarthi.Sms.entity.dashboard.DimensionalInspectionEntity;
@Repository
public interface AiSystemRepository extends JpaRepository<DimensionalInspectionEntity, Integer> {

    // Fast and simple queries following the same pattern as existing services
    @Query("SELECT DISTINCT d.railId FROM DimensionalInspectionEntity d WHERE (d.inspectedAt BETWEEN :startDate AND :endDate OR d.inspectedAt IS NULL)")
    List<String> findDimensionalRailIdsBetweenDates(
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate
    );

    @Query("SELECT DISTINCT s.railId FROM SurfaceInspectionEntity s WHERE s.createdAt BETWEEN :startDate AND :endDate")
    List<String> findSurfaceRailIdsBetweenDates(
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate
    );

    @Query("SELECT DISTINCT d.railId FROM DimensionalInspectionEntity d WHERE d.inspectedAt BETWEEN :startDate AND :endDate AND d.shift = :shift")
    List<String> findDimensionalRailIdsByDateAndShift(
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate,
        @Param("shift") String shift
    );

    @Query("SELECT DISTINCT s.railId FROM SurfaceInspectionEntity s WHERE s.createdAt BETWEEN :startDate AND :endDate AND s.shift = :shift")
    List<String> findSurfaceRailIdsByDateAndShift(
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate,
        @Param("shift") String shift
    );

    // Paginated versions for AI Dashboard
    @Query("SELECT DISTINCT d.railId FROM DimensionalInspectionEntity d WHERE d.inspectedAt BETWEEN :startDate AND :endDate ORDER BY d.inspectedAt DESC")
    Page<String> findDimensionalRailIdsBetweenDatesPageable(
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate,
        Pageable pageable
    );

    @Query("SELECT DISTINCT s.railId FROM SurfaceInspectionEntity s WHERE s.createdAt BETWEEN :startDate AND :endDate ORDER BY s.createdAt DESC")
    Page<String> findSurfaceRailIdsBetweenDatesPageable(
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate,
        Pageable pageable
    );

    @Query("SELECT DISTINCT d.railId FROM DimensionalInspectionEntity d WHERE d.inspectedAt BETWEEN :startDate AND :endDate AND d.shift = :shift ORDER BY d.inspectedAt DESC")
    Page<String> findDimensionalRailIdsByDateAndShiftPageable(
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate,
        @Param("shift") String shift,
        Pageable pageable
    );

    @Query("SELECT DISTINCT s.railId FROM SurfaceInspectionEntity s WHERE s.createdAt BETWEEN :startDate AND :endDate AND s.shift = :shift ORDER BY s.createdAt DESC")
    Page<String> findSurfaceRailIdsByDateAndShiftPageable(
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate,
        @Param("shift") String shift,
        Pageable pageable
    );

}