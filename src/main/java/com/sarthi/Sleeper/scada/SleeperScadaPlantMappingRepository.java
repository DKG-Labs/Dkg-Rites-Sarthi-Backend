package com.sarthi.Sleeper.scada;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SleeperScadaPlantMappingRepository extends JpaRepository<SleeperScadaPlantMapping, Long> {

    List<SleeperScadaPlantMapping> findByActiveTrueOrderByVendorShortNameAscPlantShortNameAsc();

    Optional<SleeperScadaPlantMapping> findByVendorShortNameIgnoreCaseAndPlantShortNameIgnoreCase(String vendorShortName, String plantShortName);
}
