package com.sarthi.repository.processmaterial;

import com.sarthi.entity.processmaterial.ProcessTurningData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ProcessTurningDataRepository extends JpaRepository<ProcessTurningData, Long> {

    List<ProcessTurningData> findByInspectionCallNo(String inspectionCallNo);

    List<ProcessTurningData> findByInspectionCallNoAndPoNoAndLineNo(
            String inspectionCallNo, String poNo, String lineNo);

    List<ProcessTurningData> findByInspectionCallNoAndShift(String inspectionCallNo, String shift);

    Optional<ProcessTurningData> findByInspectionCallNoAndShiftAndHourIndex(
            String inspectionCallNo, String shift, Integer hourIndex);

    List<ProcessTurningData> findByInspectionCallNoAndShiftAndLotNoAndCreatedBy(
            String inspectionCallNo, String shift, String lotNo, String createdBy);

    void deleteByInspectionCallNo(String inspectionCallNo);

    Optional<ProcessTurningData> findByInspectionCallNoAndLotNoAndShift(String callId, String lotNumber, String shift);

    @Query("""
SELECT
COALESCE(SUM(p.parallelLengthRejected),0),
COALESCE(SUM(p.fullTurningLengthRejected),0),
COALESCE(SUM(p.turningDiaRejected),0)
FROM ProcessTurningData p
WHERE p.inspectionCallNo = :callNo
AND p.lotNo = :lotNo
AND p.shift = :shift
AND p.createdAt BETWEEN :startDate AND :endDate
""")
    List<Object[]> getTurningSumByDate(
            @Param("callNo") String callNo,
            @Param("lotNo") String lotNo,
            @Param("shift") String shift,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate")   LocalDateTime endDate
    );



}

