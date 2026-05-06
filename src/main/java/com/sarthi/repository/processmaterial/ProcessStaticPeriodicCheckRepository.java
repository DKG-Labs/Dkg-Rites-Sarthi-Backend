package com.sarthi.repository.processmaterial;

import com.sarthi.entity.processmaterial.ProcessStaticPeriodicCheck;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProcessStaticPeriodicCheckRepository extends JpaRepository<ProcessStaticPeriodicCheck, Long> {

    List<ProcessStaticPeriodicCheck> findByInspectionCallNo(String inspectionCallNo);

    Optional<ProcessStaticPeriodicCheck> findByInspectionCallNoAndPoNoAndLineNo(
            String inspectionCallNo, String poNo, String lineNo);

    Optional<ProcessStaticPeriodicCheck> findByInspectionCallNoAndShiftAndLineNoAndLotNoAndCreatedByAndDateOfInspection(
            String inspectionCallNo, String shift, String lineNo, String lotNo, String createdBy, java.time.LocalDate dateOfInspection);

    // Fallback: find most recent record for a call + line (for older data without shift/lot/date)
    Optional<ProcessStaticPeriodicCheck> findFirstByInspectionCallNoAndLineNoOrderByCreatedAtDesc(
            String inspectionCallNo, String lineNo);

    void deleteByInspectionCallNo(String inspectionCallNo);
}

