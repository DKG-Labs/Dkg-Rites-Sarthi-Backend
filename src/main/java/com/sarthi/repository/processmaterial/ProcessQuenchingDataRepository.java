package com.sarthi.repository.processmaterial;

import com.sarthi.entity.processmaterial.ProcessQuenchingData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ProcessQuenchingDataRepository extends JpaRepository<ProcessQuenchingData, Long> {

    List<ProcessQuenchingData> findByInspectionCallNo(String inspectionCallNo);

    List<ProcessQuenchingData> findByInspectionCallNoAndPoNoAndLineNo(
            String inspectionCallNo, String poNo, String lineNo);

    List<ProcessQuenchingData> findByInspectionCallNoAndShift(String inspectionCallNo, String shift);

    Optional<ProcessQuenchingData> findByInspectionCallNoAndShiftAndHourIndex(
            String inspectionCallNo, String shift, Integer hourIndex);

    List<ProcessQuenchingData> findByInspectionCallNoAndShiftAndLotNoAndCreatedBy(
            String inspectionCallNo, String shift, String lotNo, String createdBy);

    void deleteByInspectionCallNo(String inspectionCallNo);

 /*   @Query("""
            SELECT COALESCE(SUM(p.boxGaugeRejected),0)
            FROM ProcessQuenchingData p
            WHERE p.inspectionCallNo = :callNo
            AND p.lotNo = :lotNo
            AND p.shift = :shift
            AND DATE(p.createdAt) = :date
            """)
    Integer getQuenchingBoxGaugeSum(
            String callNo,
            String lotNo,
            String shift,
            LocalDate date
    ); */
 @Query("""
        SELECT COALESCE(SUM(p.boxGaugeRejected),0)
        FROM ProcessQuenchingData p
        WHERE p.inspectionCallNo = :callNo
        AND p.lotNo = :lotNo
        AND p.shift = :shift
        AND (:lineNo IS NULL OR p.lineNo = :lineNo)
        AND p.createdAt BETWEEN :startDate AND :endDate
        """)
 Integer getQuenchingBoxGaugeSum(
         @Param("callNo") String callNo,
         @Param("lotNo") String lotNo,
         @Param("shift") String shift,
         @Param("lineNo") String lineNo,
         @Param("startDate") LocalDateTime startDate,
         @Param("endDate") LocalDateTime endDate
 );

    @Query("""
            SELECT
            COALESCE(SUM(p.flatBearingAreaRejected),0),
            COALESCE(SUM(p.fallingGaugeRejected),0)
            FROM ProcessQuenchingData p
            WHERE p.inspectionCallNo = :callNo
            AND p.lotNo = :lotNo
            AND p.shift = :shift
            AND DATE(p.createdAt) = :date
            """)
    Object[] getQuenchingDimensionalSum(
            String callNo,
            String lotNo,
            String shift,
            LocalDate date
    );

    @Query("""
        SELECT COALESCE(SUM(p.flatBearingAreaRejected),0)
        FROM ProcessQuenchingData p
        WHERE p.inspectionCallNo = :callNo
        AND p.lotNo = :lotNo
        AND p.shift = :shift
        AND (:lineNo IS NULL OR p.lineNo = :lineNo)
        AND p.createdAt BETWEEN :startDate AND :endDate
        """)
    Integer getQuenchingFlatBearingSum(
            @Param("callNo") String callNo,
            @Param("lotNo") String lotNo,
            @Param("shift") String shift,
            @Param("lineNo") String lineNo,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );

    @Query("""
        SELECT COALESCE(SUM(p.fallingGaugeRejected),0)
        FROM ProcessQuenchingData p
        WHERE p.inspectionCallNo = :callNo
        AND p.lotNo = :lotNo
        AND p.shift = :shift
        AND (:lineNo IS NULL OR p.lineNo = :lineNo)
        AND p.createdAt BETWEEN :startDate AND :endDate
        """)
    Integer getQuenchingFallingGaugeSum(
            @Param("callNo") String callNo,
            @Param("lotNo") String lotNo,
            @Param("shift") String shift,
            @Param("lineNo") String lineNo,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );

    @Query(value = """
            SELECT COALESCE(SUM(quenching_hardness_rejected),0)
            FROM process_quenching_data
            WHERE inspection_call_no = :callId
            AND lot_no = :lotNumber
            AND shift = :shift
            AND (:lineNo IS NULL OR line_no = :lineNo)
            AND created_at BETWEEN :startDate AND :endDate
            """, nativeQuery = true)
    Integer getQuenchingHardnessSum(
            @Param("callId") String callId,
            @Param("lotNumber") String lotNumber,
            @Param("shift") String shift,
            @Param("lineNo") String lineNo,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

}