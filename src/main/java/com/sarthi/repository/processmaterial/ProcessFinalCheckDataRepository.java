package com.sarthi.repository.processmaterial;

import com.sarthi.entity.processmaterial.ProcessFinalCheckData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ProcessFinalCheckDataRepository extends JpaRepository<ProcessFinalCheckData, Long> {

    List<ProcessFinalCheckData> findByInspectionCallNo(String inspectionCallNo);

    List<ProcessFinalCheckData> findByInspectionCallNoAndPoNoAndLineNo(
            String inspectionCallNo, String poNo, String lineNo);

    List<ProcessFinalCheckData> findByInspectionCallNoAndShift(String inspectionCallNo, String shift);

    Optional<ProcessFinalCheckData> findByInspectionCallNoAndShiftAndHourIndex(
            String inspectionCallNo, String shift, Integer hourIndex);

    List<ProcessFinalCheckData> findByInspectionCallNoAndShiftAndLotNoAndCreatedBy(
            String inspectionCallNo, String shift, String lotNo, String createdBy);

    void deleteByInspectionCallNo(String inspectionCallNo);

    @Query("""
SELECT
COALESCE(SUM(p.surfaceDefectRejected),0),
COALESCE(SUM(p.markingRejected),0)
FROM ProcessFinalCheckData p
WHERE p.inspectionCallNo = :callNo
AND p.lotNo = :lotNo
AND p.shift = :shift
AND (:lineNo IS NULL OR p.lineNo = :lineNo)
AND p.createdAt BETWEEN :startDate AND :endDate
""")
    List<Object[]> getVisualDefectsSumByDate(
            @Param("callNo") String callNo,
            @Param("lotNo") String lotNo,
            @Param("shift") String shift,
            @Param("lineNo") String lineNo,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate")   LocalDateTime endDate
    );

    @Query("""
        SELECT COALESCE(SUM(p.embossingDefectRejected),0)
        FROM ProcessFinalCheckData p
        WHERE p.inspectionCallNo = :callNo
        AND p.lotNo = :lotNo
        AND p.shift = :shift
        AND (:lineNo IS NULL OR p.lineNo = :lineNo)
        AND p.createdAt BETWEEN :startDate AND :endDate
        """)
    Integer getFinalEmbossingSumByDate(
            @Param("callNo") String callNo,
            @Param("lotNo") String lotNo,
            @Param("shift") String shift,
            @Param("lineNo") String lineNo,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );

    @Query(value = """
SELECT COALESCE(SUM(tempering_hardness_rejected),0)
FROM process_final_check_data
WHERE inspection_call_no = :callId
AND lot_no = :lotNumber
AND shift = :shift
AND (:lineNo IS NULL OR line_no = :lineNo)
AND created_at BETWEEN :startDate AND :endDate
""", nativeQuery = true)
    Integer getTemperingHardnessSumByDate(
            @Param("callId") String callId,
            @Param("lotNumber") String lotNumber,
            @Param("shift") String shift,
            @Param("lineNo") String lineNo,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    @Query("""
        SELECT COALESCE(SUM(p.boxGaugeRejected),0)
        FROM ProcessFinalCheckData p
        WHERE p.inspectionCallNo = :callNo
        AND p.lotNo = :lotNo
        AND p.shift = :shift
        AND (:lineNo IS NULL OR p.lineNo = :lineNo)
        AND p.createdAt BETWEEN :startDate AND :endDate
        """)
    Integer getFinalBoxGaugeSum(
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
            FROM ProcessFinalCheckData p
            WHERE p.inspectionCallNo = :callNo
            AND p.lotNo = :lotNo
            AND p.shift = :shift
            AND DATE(p.createdAt) = :date
            """)
    Object[] getFinalDimensionalSum(
            String callNo,
            String lotNo,
            String shift,
            LocalDate date
    );

    @Query("""
        SELECT COALESCE(SUM(p.flatBearingAreaRejected),0)
        FROM ProcessFinalCheckData p
        WHERE p.inspectionCallNo = :callNo
        AND p.lotNo = :lotNo
        AND p.shift = :shift
        AND (:lineNo IS NULL OR p.lineNo = :lineNo)
        AND p.createdAt BETWEEN :startDate AND :endDate
        """)
    Integer getFinalFlatBearingSum(
            @Param("callNo") String callNo,
            @Param("lotNo") String lotNo,
            @Param("shift") String shift,
            @Param("lineNo") String lineNo,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );

    @Query("""
        SELECT COALESCE(SUM(p.fallingGaugeRejected),0)
        FROM ProcessFinalCheckData p
        WHERE p.inspectionCallNo = :callNo
        AND p.lotNo = :lotNo
        AND p.shift = :shift
        AND (:lineNo IS NULL OR p.lineNo = :lineNo)
        AND p.createdAt BETWEEN :startDate AND :endDate
        """)
    Integer getFinalFallingGaugeSum(
            @Param("callNo") String callNo,
            @Param("lotNo") String lotNo,
            @Param("shift") String shift,
            @Param("lineNo") String lineNo,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );
}