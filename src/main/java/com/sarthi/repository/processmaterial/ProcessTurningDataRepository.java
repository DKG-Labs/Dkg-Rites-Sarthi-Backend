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
AND (:lineNo IS NULL OR p.lineNo = :lineNo)
AND p.createdAt BETWEEN :startDate AND :endDate
""")
    List<Object[]> getTurningSumByDate(
            @Param("callNo") String callNo,
            @Param("lotNo") String lotNo,
            @Param("shift") String shift,
            @Param("lineNo") String lineNo,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate")   LocalDateTime endDate
    );

    /**
     * Returns latest 30 rows of dia_1, dia_2, dia_3 for a given inspection_call_no,
     * ordered by id descending (most recent first).
     */
    @Query(value = """
        SELECT dia_1, dia_2, dia_3
        FROM process_turning_data
        WHERE inspection_call_no = :callNo
          AND (dia_1 IS NOT NULL OR dia_2 IS NOT NULL OR dia_3 IS NOT NULL)
        ORDER BY id DESC
        LIMIT 30
        """, nativeQuery = true)
    List<Object[]> findLatest30DiaByCallNo(@Param("callNo") String callNo);

    /**
     * Returns diameter measurements (dia_1, dia_2, dia_3) for a specific company name and unit address
     * for the latest 30 days from the last record entered, ordered chronologically (by id ascending).
     */
    @Query(value = """
        SELECT p.dia_1, p.dia_2, p.dia_3
        FROM process_turning_data p
        JOIN inspection_calls ic ON p.inspection_call_no = ic.ic_number
        WHERE ic.company_name = :companyName
          AND ic.unit_address = :unitAddress
          AND (p.dia_1 IS NOT NULL OR p.dia_2 IS NOT NULL OR p.dia_3 IS NOT NULL)
          AND p.created_at >= (
              SELECT DATE_SUB(MAX(p2.created_at), INTERVAL 30 DAY)
              FROM process_turning_data p2
              JOIN inspection_calls ic2 ON p2.inspection_call_no = ic2.ic_number
              WHERE ic2.company_name = :companyName
                AND ic2.unit_address = :unitAddress
          )
        ORDER BY p.id ASC
        """, nativeQuery = true)
    List<Object[]> findDiaByCompanyAndUnitForLatest30Days(
            @Param("companyName") String companyName,
            @Param("unitAddress") String unitAddress);

}


