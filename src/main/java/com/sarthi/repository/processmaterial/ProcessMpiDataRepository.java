package com.sarthi.repository.processmaterial;

import com.sarthi.entity.processmaterial.ProcessMpiData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProcessMpiDataRepository extends JpaRepository<ProcessMpiData, Long> {

    List<ProcessMpiData> findByInspectionCallNo(String inspectionCallNo);

    List<ProcessMpiData> findByInspectionCallNoAndPoNoAndLineNo(
            String inspectionCallNo, String poNo, String lineNo);

    List<ProcessMpiData> findByInspectionCallNoAndShift(String inspectionCallNo, String shift);

    Optional<ProcessMpiData> findByInspectionCallNoAndShiftAndHourIndex(
            String inspectionCallNo, String shift, Integer hourIndex);

    List<ProcessMpiData> findByInspectionCallNoAndShiftAndLotNoAndCreatedBy(
            String inspectionCallNo, String shift, String lotNo, String createdBy);

    void deleteByInspectionCallNo(String inspectionCallNo);
}

