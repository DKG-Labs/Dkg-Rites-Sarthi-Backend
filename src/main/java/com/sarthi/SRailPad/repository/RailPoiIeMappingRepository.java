package com.sarthi.SRailPad.repository;

import com.sarthi.SRailPad.entity.raipadMapping.RailPoiIeMapping;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RailPoiIeMappingRepository extends JpaRepository<RailPoiIeMapping, Long> {
    List<RailPoiIeMapping> findByPoiCodeAndPlantId(String poiCode, String plantId);

    boolean existsByPoiCodeAndPlantIdAndIeUserIdAndIeType(String poiCode, String plantId, int intExact, String processIe);

    List<RailPoiIeMapping> findByPoiCodeAndPlantIdAndIeType(String poiCode, String plantId, String mainIe);
}
