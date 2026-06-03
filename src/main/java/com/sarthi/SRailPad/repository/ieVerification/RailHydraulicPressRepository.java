package com.sarthi.SRailPad.repository.ieVerification;

import com.sarthi.SRailPad.entity.ieVerification.RailHydraulicPress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface RailHydraulicPressRepository extends JpaRepository<RailHydraulicPress, Long> {
    List<RailHydraulicPress> findAllByPlantIdAndShiftAndCastingDate(String plantId, String shift, LocalDate castingDate);
    List<RailHydraulicPress> findAllByPlantIdOrderByCreatedDateDesc(String plantId);
}
