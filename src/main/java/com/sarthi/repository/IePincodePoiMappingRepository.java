package com.sarthi.repository;

import com.sarthi.entity.IePincodePoiMapping;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface IePincodePoiMappingRepository extends JpaRepository<IePincodePoiMapping, Long> {
   // Optional<Integer> findPrimaryIe(String pinCode, String product, String poiCode, String rio);

   // Optional<Integer> findSecondaryIe(String pinCode, String product, String poiCode, String rio);


    @Query("""
    SELECT m.employeeCode
    FROM IePincodePoiMapping m
    WHERE (m.poiCode = :poiCode OR TRIM(LEADING ':' FROM m.poiCode) = TRIM(LEADING ':' FROM :poiCode))
      AND (
          (TRIM(m.pinCode) = TRIM(:pinCode) AND TRIM(UPPER(m.product)) = TRIM(UPPER(:product)))
          OR (:pinCode IS NOT NULL)
      )
      AND UPPER(TRIM(m.ieType)) = 'PRIMARY'
    ORDER BY CASE WHEN TRIM(m.pinCode) = TRIM(:pinCode) THEN 0 ELSE 1 END
""")
    List<String> findPrimaryIe(
            @Param("pinCode") String pinCode,
            @Param("product") String product,
            @Param("poiCode") String poiCode
    );

    @Query("""
    SELECT m.employeeCode
    FROM IePincodePoiMapping m
    WHERE (m.poiCode = :poiCode OR TRIM(LEADING ':' FROM m.poiCode) = TRIM(LEADING ':' FROM :poiCode))
      AND (
          (TRIM(m.pinCode) = TRIM(:pinCode) AND TRIM(UPPER(m.product)) = TRIM(UPPER(:product)))
          OR (:pinCode IS NOT NULL)
      )
      AND UPPER(TRIM(m.ieType)) = 'SECONDARY'
    ORDER BY CASE WHEN TRIM(m.pinCode) = TRIM(:pinCode) THEN 0 ELSE 1 END
""")
    List<String> findSecondaryIe(
            @Param("pinCode") String pinCode,
            @Param("product") String product,
            @Param("poiCode") String poiCode
    );


    List<IePincodePoiMapping> findByPoiCode(String placeOfInspection);

    @Query(value = """
        SELECT DISTINCT CONCAT(um.employee_code, ' - ', um.username)
        FROM ie_pincode_poi_mapping ipm
        JOIN user_master um ON um.employee_code COLLATE utf8mb4_unicode_ci = ipm.employee_code COLLATE utf8mb4_unicode_ci
        WHERE :poiCode IS NULL OR :poiCode = ''
           OR ipm.poi_code = :poiCode
           OR TRIM(LEADING ':' FROM ipm.poi_code) = TRIM(LEADING ':' FROM :poiCode)
           OR ipm.poi_code IN (
               SELECT ppm.poi_code FROM pincode_poi_mapping ppm 
               WHERE ppm.poi_code = :poiCode OR TRIM(LEADING ':' FROM ppm.poi_code) = TRIM(LEADING ':' FROM :poiCode)
           )
    """, nativeQuery = true)
    List<String> findIeEmpCodeWithName(@Param("poiCode") String poiCode);

    @org.springframework.data.jpa.repository.Modifying
    @Query("UPDATE IePincodePoiMapping m SET m.employeeCode = :newEmpCode WHERE m.poiCode = :poiCode AND m.employeeCode = :oldEmpCode")
    int updateEmployeeCodeByPoiCode(String poiCode, String oldEmpCode, String newEmpCode);

    boolean existsByPoiCodeAndEmployeeCode(String poiCode, String employeeCode);
}
