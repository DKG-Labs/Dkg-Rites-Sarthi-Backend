package com.sarthi.SRailPad.repository.ieVerification;

import com.sarthi.SRailPad.entity.ieVerification.RailMixingKneaderMill;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface RailMixingKneaderMillRepository extends JpaRepository<RailMixingKneaderMill, Long> {
    List<RailMixingKneaderMill> findAllByPlantIdAndShiftAndCastingDate(String plantId, String shift, LocalDate castingDate);
    List<RailMixingKneaderMill> findAllByPlantIdOrderByCreatedDateDesc(String plantId);
}
