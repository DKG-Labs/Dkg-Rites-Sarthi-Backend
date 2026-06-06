package com.sarthi.SRailPad.repository.ieVerification;

import com.sarthi.SRailPad.entity.ieVerification.RailFinalInspectionLotResults;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface RailFinalInspectionLotResultsRepository extends JpaRepository<RailFinalInspectionLotResults, Long> {
    Optional<RailFinalInspectionLotResults> findByCallNoAndLotNo(String callNo, String lotNo);
    List<RailFinalInspectionLotResults> findAllByCallNo(String callNo);
    List<RailFinalInspectionLotResults> findAllByPlantIdAndShiftAndDateOfInspection(String plantId, String shift, LocalDate dateOfInspection);
}
