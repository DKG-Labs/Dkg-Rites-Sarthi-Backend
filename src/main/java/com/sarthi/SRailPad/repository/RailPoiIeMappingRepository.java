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

    @org.springframework.data.jpa.repository.Query(value = """
SELECT rpm.* 
FROM rail_poi_ie_mapping rpm 
WHERE (rpm.poi_code = :poiCode OR :poiCode IS NULL) 
  AND (rpm.plant_id = :plantId OR rpm.plant_id = CONCAT(':', REPLACE(:plantId, ':', '')) OR rpm.plant_id = REPLACE(:plantId, ':', '') OR :plantId IS NULL) 
  AND (rpm.ie_type = :ieType OR LOWER(rpm.ie_type) = LOWER(:ieType) OR UPPER(REPLACE(rpm.ie_type, ' ', '_')) = UPPER(REPLACE(:ieType, ' ', '_')))
""", nativeQuery = true)
    List<RailPoiIeMapping> findByPoiCodeAndPlantIdAndIeType(@org.springframework.data.repository.query.Param("poiCode") String poiCode, 
                                                            @org.springframework.data.repository.query.Param("plantId") String plantId, 
                                                            @org.springframework.data.repository.query.Param("ieType") String ieType);

    List<RailPoiIeMapping> findByIeUserId(Integer ieUserId);

    @org.springframework.data.jpa.repository.Query("SELECT DISTINCT p.poiCode FROM RailPoiIeMapping p WHERE p.ieUserId = :ieUserId AND p.poiCode IS NOT NULL")
    List<String> findDistinctPoiCodesByIeUserId(@org.springframework.data.repository.query.Param("ieUserId") Integer ieUserId);

    @org.springframework.data.jpa.repository.Modifying
    @org.springframework.data.jpa.repository.Query(value = """
UPDATE rail_poi_ie_mapping 
SET ie_user_id = :newUserId 
WHERE (REPLACE(plant_id, ':', '') = REPLACE(:plantId, ':', '')) 
  AND ie_user_id = :oldUserId
""", nativeQuery = true)
    void updateIeUserIdByPlantId(@org.springframework.data.repository.query.Param("plantId") String plantId, 
                                 @org.springframework.data.repository.query.Param("oldUserId") Integer oldUserId, 
                                 @org.springframework.data.repository.query.Param("newUserId") Integer newUserId);

    @org.springframework.data.jpa.repository.Query(value = """
SELECT DISTINCT CONCAT(um.employee_code, ' - ', um.username) 
FROM rail_poi_ie_mapping rpm 
JOIN user_master um ON um.userid = rpm.ie_user_id 
WHERE (rpm.poi_code = :poiCode OR :poiCode IS NULL) 
  AND (rpm.plant_id = :plantId OR REPLACE(rpm.plant_id, ':', '') = REPLACE(:plantId, ':', '') OR :plantId IS NULL) 
  AND (UPPER(REPLACE(rpm.ie_type, ' ', '_')) = 'MAIN_IE' OR UPPER(rpm.ie_type) = 'MAIN IE')
""", nativeQuery = true)
    List<String> findIeEmpCodeWithNameAndPlantId(
            @org.springframework.data.repository.query.Param("poiCode") String poiCode, 
            @org.springframework.data.repository.query.Param("plantId") String plantId
    );

    @org.springframework.data.jpa.repository.Query(value = """
SELECT * FROM rail_poi_ie_mapping 
WHERE REPLACE(plant_id, ':', '') = REPLACE(:plantId, ':', '') 
  AND UPPER(REPLACE(ie_type, ' ', '_')) = UPPER(REPLACE(:ieType, ' ', '_')) 
LIMIT 1
""", nativeQuery = true)
    Optional<RailPoiIeMapping> findByPlantIdAndIeType(@org.springframework.data.repository.query.Param("plantId") String plantId, 
                                                      @org.springframework.data.repository.query.Param("ieType") String ieType);

    @org.springframework.data.jpa.repository.Query("""
SELECT COUNT(m) > 0 
FROM RailPoiIeMapping m 
WHERE (m.plantId = :plantId OR m.plantId = :cleanPlantId OR m.plantId = :colonPlantId OR LOWER(REPLACE(m.plantId, ':', '')) = LOWER(REPLACE(:plantId, ':', '')) OR LOWER(REPLACE(m.plantId, ':', '')) = LOWER(REPLACE(:cleanPlantId, ':', '')) OR (:poiCode IS NOT NULL AND m.poiCode = :poiCode)) 
  AND (UPPER(REPLACE(m.ieType, ' ', '_')) = 'MAIN_IE' OR UPPER(m.ieType) = 'MAIN IE' OR UPPER(m.ieType) LIKE '%MAIN%')
""")
    boolean hasMainIeMapping(
            @org.springframework.data.repository.query.Param("plantId") String plantId,
            @org.springframework.data.repository.query.Param("cleanPlantId") String cleanPlantId,
            @org.springframework.data.repository.query.Param("colonPlantId") String colonPlantId,
            @org.springframework.data.repository.query.Param("poiCode") String poiCode
    );

    @org.springframework.data.jpa.repository.Query("""
SELECT COUNT(m) > 0 
FROM RailPoiIeMapping m 
WHERE (m.plantId = :plantId OR m.plantId = :cleanPlantId OR m.plantId = :colonPlantId OR LOWER(REPLACE(m.plantId, ':', '')) = LOWER(REPLACE(:plantId, ':', '')) OR LOWER(REPLACE(m.plantId, ':', '')) = LOWER(REPLACE(:cleanPlantId, ':', '')) OR (:poiCode IS NOT NULL AND m.poiCode = :poiCode)) 
  AND (UPPER(REPLACE(m.ieType, ' ', '_')) = 'PROCESS_IE' OR UPPER(m.ieType) = 'PROCESS IE' OR UPPER(m.ieType) LIKE '%PROCESS%')
""")
    boolean hasProcessIeMapping(
            @org.springframework.data.repository.query.Param("plantId") String plantId,
            @org.springframework.data.repository.query.Param("cleanPlantId") String cleanPlantId,
            @org.springframework.data.repository.query.Param("colonPlantId") String colonPlantId,
            @org.springframework.data.repository.query.Param("poiCode") String poiCode
    );
    @org.springframework.data.jpa.repository.Query(value = """
SELECT DISTINCT COALESCE(NULLIF(TRIM(um.FULL_NAME), ''), um.username) 
FROM rail_poi_ie_mapping rpm 
JOIN user_master um ON um.userid = rpm.ie_user_id 
WHERE (REPLACE(rpm.plant_id, ':', '') = REPLACE(:plantId, ':', '') OR (:poiCode IS NOT NULL AND rpm.poi_code = :poiCode)) 
  AND (UPPER(REPLACE(rpm.ie_type, ' ', '_')) = 'MAIN_IE' OR UPPER(rpm.ie_type) = 'MAIN IE')
""", nativeQuery = true)
    List<String> findMainIeNamesByPlantId(
            @org.springframework.data.repository.query.Param("plantId") String plantId,
            @org.springframework.data.repository.query.Param("poiCode") String poiCode
    );
}
