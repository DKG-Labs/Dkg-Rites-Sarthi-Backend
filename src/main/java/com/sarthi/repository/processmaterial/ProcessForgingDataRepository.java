package com.sarthi.repository.processmaterial;

import com.sarthi.entity.processmaterial.ProcessForgingData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ProcessForgingDataRepository extends JpaRepository<ProcessForgingData, Long> {

    List<ProcessForgingData> findByInspectionCallNo(String inspectionCallNo);

    List<ProcessForgingData> findByInspectionCallNoAndPoNoAndLineNo(
            String inspectionCallNo, String poNo, String lineNo);

    List<ProcessForgingData> findByInspectionCallNoAndShift(String inspectionCallNo, String shift);

    Optional<ProcessForgingData> findByInspectionCallNoAndShiftAndHourIndex(
            String inspectionCallNo, String shift, Integer hourIndex);

    void deleteByInspectionCallNo(String inspectionCallNo);

    Optional<ProcessForgingData> findByInspectionCallNoAndLotNoAndShift(String callId, String lotNumber, String shift);

    @Query("""
SELECT
COALESCE(SUM(p.forgingTempRejected),0),
COALESCE(SUM(p.forgingStabilisationRejectionRejected),0),
COALESCE(SUM(p.improperForgingRejected),0),
COALESCE(SUM(p.forgingDefectRejected),0)
FROM ProcessForgingData p
WHERE p.inspectionCallNo = :callNo
AND p.lotNo = :lotNo
AND p.shift = :shift
AND p.createdAt BETWEEN :startDate AND :endDate
""")
    List<Object[]> getForgingSumByDate(
            @Param("callNo") String callNo,
            @Param("lotNo") String lotNo,
            @Param("shift") String shift,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate")   LocalDateTime endDate
    );




    @Query("""
        SELECT COALESCE(SUM(p.embossingDefectRejected),0)
        FROM ProcessForgingData p
        WHERE p.inspectionCallNo = :callNo
        AND p.lotNo = :lotNo
        AND p.shift = :shift
        AND p.createdAt BETWEEN :startDate AND :endDate
        """)
    Integer getForgingEmbossingSumByDate(
            @Param("callNo") String callNo,
            @Param("lotNo") String lotNo,
            @Param("shift") String shift,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );
}

