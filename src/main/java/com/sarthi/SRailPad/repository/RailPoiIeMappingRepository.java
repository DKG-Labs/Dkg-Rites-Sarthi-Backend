package com.sarthi.SRailPad.repository;

import com.sarthi.SRailPad.entity.raipadMapping.RailPoiIeMapping;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RailPoiIeMappingRepository extends JpaRepository<RailPoiIeMapping, Long> {
    List<RailPoiIeMapping> findByPoiCodeAndPlantId(String poiCode, String plantId);

    boolean existsByPoiCodeAndPlantIdAndIeUserIdAndIeType(String poiCode, String plantId, int intExact, String processIe);

    @org.springframework.data.jpa.repository.Query("SELECT p FROM RailPoiIeMapping p WHERE p.poiCode = :poiCode AND p.plantId = :plantId AND UPPER(REPLACE(p.ieType, ' ', '_')) = UPPER(REPLACE(:ieType, ' ', '_'))")
    List<RailPoiIeMapping> findByPoiCodeAndPlantIdAndIeType(@org.springframework.data.repository.query.Param("poiCode") String poiCode, 
                                                            @org.springframework.data.repository.query.Param("plantId") String plantId, 
                                                            @org.springframework.data.repository.query.Param("ieType") String ieType);

    List<RailPoiIeMapping> findByIeUserId(Integer ieUserId);

    @org.springframework.data.jpa.repository.Modifying
    @org.springframework.data.jpa.repository.Query("UPDATE RailPoiIeMapping p SET p.ieUserId = :newUserId WHERE p.plantId = :plantId AND p.ieUserId = :oldUserId")
    void updateIeUserIdByPlantId(@org.springframework.data.repository.query.Param("plantId") String plantId, 
                                 @org.springframework.data.repository.query.Param("oldUserId") Integer oldUserId, 
                                 @org.springframework.data.repository.query.Param("newUserId") Integer newUserId);

    @org.springframework.data.jpa.repository.Query(value = "SELECT DISTINCT CONCAT(um.employee_code, ' - ', um.username) " +
            "FROM rail_poi_ie_mapping rpm " +
            "JOIN user_master um ON um.userid = rpm.ie_user_id " +
            "WHERE rpm.poi_code = :poiCode AND rpm.plant_id = :plantId AND UPPER(REPLACE(rpm.ie_type, ' ', '_')) = 'MAIN_IE'", nativeQuery = true)
    List<String> findIeEmpCodeWithNameAndPlantId(
            @org.springframework.data.repository.query.Param("poiCode") String poiCode, 
            @org.springframework.data.repository.query.Param("plantId") String plantId
    );

    Optional<RailPoiIeMapping> findByPlantIdAndIeType(String plantId, String mainIe);
}
