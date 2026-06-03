package com.sarthi.SRailPad.repository.ieVerification;

import com.sarthi.SRailPad.entity.ieVerification.RailRawMaterialWeighment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface RailRawMaterialWeighmentRepository extends JpaRepository<RailRawMaterialWeighment, Long> {
    List<RailRawMaterialWeighment> findAllByPlantIdAndShiftAndCastingDate(String plantId, String shift, LocalDate castingDate);
    List<RailRawMaterialWeighment> findAllByPlantIdOrderByCreatedDateDesc(String plantId);
}
