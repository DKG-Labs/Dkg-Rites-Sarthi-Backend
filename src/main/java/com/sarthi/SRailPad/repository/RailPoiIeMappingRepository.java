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

    List<RailPoiIeMapping> findByIeUserId(Integer ieUserId);

    @org.springframework.data.jpa.repository.Modifying
    @org.springframework.data.jpa.repository.Query("UPDATE RailPoiIeMapping p SET p.ieUserId = :newUserId WHERE p.plantId = :plantId AND p.ieUserId = :oldUserId")
    void updateIeUserIdByPlantId(@org.springframework.data.repository.query.Param("plantId") String plantId, 
                                 @org.springframework.data.repository.query.Param("oldUserId") Integer oldUserId, 
                                 @org.springframework.data.repository.query.Param("newUserId") Integer newUserId);
}
