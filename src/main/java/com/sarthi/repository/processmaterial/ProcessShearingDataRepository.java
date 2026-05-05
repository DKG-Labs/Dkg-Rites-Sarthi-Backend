package com.sarthi.repository.processmaterial;

import com.sarthi.entity.processmaterial.ProcessShearingData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ProcessShearingDataRepository extends JpaRepository<ProcessShearingData, Long> {

    List<ProcessShearingData> findByInspectionCallNo(String inspectionCallNo);

    List<ProcessShearingData> findByInspectionCallNoAndPoNoAndLineNo(
            String inspectionCallNo, String poNo, String lineNo);

    List<ProcessShearingData> findByInspectionCallNoAndShift(String inspectionCallNo, String shift);

    Optional<ProcessShearingData> findByInspectionCallNoAndShiftAndHourIndex(
            String inspectionCallNo, String shift, Integer hourIndex);

    List<ProcessShearingData> findByInspectionCallNoAndShiftAndLotNoAndCreatedBy(
            String inspectionCallNo, String shift, String lotNo, String createdBy);

    void deleteByInspectionCallNo(String inspectionCallNo);

/*
    @Query("""
SELECT
COALESCE(SUM(p.lengthCutBarRejected),0),
COALESCE(SUM(p.improperDiaRejected),0),
COALESCE(SUM(p.sharpEdgesRejected),0),
COALESCE(SUM(p.crackedEdgesRejected),0)
FROM ProcessShearingData p
WHERE p.inspectionCallNo = :callNo
AND p.lotNo = :lotNo
AND p.shift = :shift
AND DATE(p.createdAt) = :date
""")
    Object[] getShearingSumByDate(
            @Param("callNo") String callNo,
            @Param("lotNo") String lotNo,
            @Param("shift") String shift,
            @Param("date") LocalDate date
    );*/
@Query("""
SELECT
COALESCE(SUM(p.lengthCutBarRejected),0),
COALESCE(SUM(p.improperDiaRejected),0),
COALESCE(SUM(p.sharpEdgesRejected),0),
COALESCE(SUM(p.crackedEdgesRejected),0)
FROM ProcessShearingData p
WHERE p.inspectionCallNo = :callNo
AND p.lotNo = :lotNo
AND p.shift = :shift
AND p.createdAt BETWEEN :startDate AND :endDate
""")
List<Object[]> getShearingSumByDate(
        @Param("callNo") String callNo,
        @Param("lotNo") String lotNo,
        @Param("shift") String shift,
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate")   LocalDateTime endDate
);


}

