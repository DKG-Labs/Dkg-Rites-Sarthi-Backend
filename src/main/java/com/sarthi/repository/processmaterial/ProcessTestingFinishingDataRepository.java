package com.sarthi.repository.processmaterial;

import com.sarthi.entity.processmaterial.ProcessTestingFinishingData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ProcessTestingFinishingDataRepository extends JpaRepository<ProcessTestingFinishingData, Long> {

    List<ProcessTestingFinishingData> findByInspectionCallNo(String inspectionCallNo);

    List<ProcessTestingFinishingData> findByInspectionCallNoAndPoNoAndLineNo(
            String inspectionCallNo, String poNo, String lineNo);

    List<ProcessTestingFinishingData> findByInspectionCallNoAndShift(String inspectionCallNo, String shift);

    Optional<ProcessTestingFinishingData> findByInspectionCallNoAndShiftAndHourIndex(
            String inspectionCallNo, String shift, Integer hourIndex);

    List<ProcessTestingFinishingData> findByInspectionCallNoAndShiftAndLotNoAndCreatedBy(
            String inspectionCallNo, String shift, String lotNo, String createdBy);

    void deleteByInspectionCallNo(String inspectionCallNo);

    @Query("""
SELECT
COALESCE(SUM(p.toeLoadRejected),0),
COALESCE(SUM(p.weightRejected),0),
COALESCE(SUM(p.paintIdentificationRejected),0),
COALESCE(SUM(p.ercCoatingRejected),0)
FROM ProcessTestingFinishingData p
WHERE p.inspectionCallNo = :callNo
AND p.lotNo = :lotNo
AND p.shift = :shift
AND (:lineNo IS NULL OR p.lineNo = :lineNo)
AND p.createdAt BETWEEN :startDate AND :endDate
""")
    List<Object[]> getTestingFinishingSumByDate(
            @Param("callNo") String callNo,
            @Param("lotNo") String lotNo,
            @Param("shift") String shift,
            @Param("lineNo") String lineNo,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate")   LocalDateTime endDate
    );

}
