package com.sarthi.repository.processmaterial;

import com.sarthi.entity.processmaterial.ProcessTemperingData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ProcessTemperingDataRepository extends JpaRepository<ProcessTemperingData, Long> {

    List<ProcessTemperingData> findByInspectionCallNo(String inspectionCallNo);

    List<ProcessTemperingData> findByInspectionCallNoAndPoNoAndLineNo(
            String inspectionCallNo, String poNo, String lineNo);

    List<ProcessTemperingData> findByInspectionCallNoAndShift(String inspectionCallNo, String shift);

    Optional<ProcessTemperingData> findByInspectionCallNoAndShiftAndHourIndex(
            String inspectionCallNo, String shift, Integer hourIndex);

    List<ProcessTemperingData> findByInspectionCallNoAndShiftAndLotNoAndCreatedBy(
            String inspectionCallNo, String shift, String lotNo, String createdBy);

    void deleteByInspectionCallNo(String inspectionCallNo);

    @Query(value = """
            SELECT 
            COALESCE(SUM(tempering_temperature_rejected),0),
            COALESCE(SUM(tempering_duration_rejected),0)
            FROM process_tempering_data
            WHERE inspection_call_no = :callId
            AND lot_no = :lotNumber
            AND shift = :shift
            AND created_at BETWEEN :startDate AND :endDate
            """, nativeQuery = true)
    List<Object[]> getTemperingSumByDate(
            String callId,
            String lotNumber,
            String shift,
            LocalDateTime startDate,
            LocalDateTime endDate);
}
