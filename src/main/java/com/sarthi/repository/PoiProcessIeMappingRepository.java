package com.sarthi.repository;

import com.sarthi.entity.PoiProcessIeMapping;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PoiProcessIeMappingRepository extends JpaRepository<PoiProcessIeMapping, Long> {
    @Query("""
    SELECT u.id
    FROM PoiProcessIeMapping p
    JOIN UserMaster u 
        ON u.employeeCode = p.employeeCode
    WHERE p.poiCode = :poiCode
""")
    List<Long> findUserIdsByPoiCode(@Param("poiCode") String poiCode);

    @org.springframework.data.jpa.repository.Modifying
    @Query("UPDATE PoiProcessIeMapping p SET p.employeeCode = :newEmpCode WHERE p.poiCode = :poiCode AND p.employeeCode = :oldEmpCode")
    int updateEmployeeCodeByPoiCode(String poiCode, String oldEmpCode, String newEmpCode);

    boolean existsByPoiCodeAndEmployeeCode(String poiCode, String employeeCode);

    List<PoiProcessIeMapping> findByPoiCode(String poiCode);

    @Query("SELECT DISTINCT p.poiCode FROM PoiProcessIeMapping p WHERE p.employeeCode = :employeeCode")
    List<String> findDistinctPoiCodesByEmployeeCode(@Param("employeeCode") String employeeCode);

    @Query(value = """
        SELECT DISTINCT pim.poi_code AS poiCode, ppm.company_name AS companyName, ppm.unit_name AS unitName
        FROM poi_process_ie_mapping pim
        LEFT JOIN pincode_poi_mapping ppm ON ppm.poi_code = pim.poi_code
        WHERE pim.employee_code = :employeeCode
    """, nativeQuery = true)
    List<Object[]> findPlantInfoByEmployeeCode(@Param("employeeCode") String employeeCode);
}
