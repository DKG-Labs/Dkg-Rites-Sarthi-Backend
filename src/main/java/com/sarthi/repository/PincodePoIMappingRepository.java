package com.sarthi.repository;

import com.sarthi.dto.UnitDetailsDTO;
import com.sarthi.dto.UnitDto;
import com.sarthi.entity.PincodePoIMapping;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PincodePoIMappingRepository extends JpaRepository<PincodePoIMapping, Long> {
    boolean existsByPinCodeAndPoiCode(String pinCode, String poiCode);

    Optional<PincodePoIMapping> findByPoiCode(String placeOfInspection);

    // @Query("SELECT DISTINCT p.companyName FROM PincodePoIMapping p")
    // List<String> findDistinctCompanyNames();

    // @Query("""
    // SELECT DISTINCT p.companyName
    // FROM PincodePoIMapping p
    // WHERE p.poiCode IS NOT NULL
    // AND p.poiCode <> ''
    // """)
    // List<String> findDistinctCompanyNames();

    @Query("""
                SELECT DISTINCT p.companyName
                FROM PincodePoIMapping p
                WHERE p.poiCode IN ('POI1', 'POI31','POI32','POI33')
            """)
    List<String> findDistinctCompanyNames();

    @Query("""
                SELECT DISTINCT p.companyName
                FROM PincodePoIMapping p
                WHERE p.vendorCode = :vendorCode
            """)
    List<String> findDistinctCompanyNamesByVendorCode(
            @Param("vendorCode") String vendorCode);

    @Query("""
                SELECT DISTINCT new com.sarthi.dto.UnitDto(p.unitName)
                FROM PincodePoIMapping p
                WHERE p.companyName = :companyName
            """)
    List<UnitDto> findUnitsByCompany(@Param("companyName") String companyName);

    @Query("""
                SELECT new com.sarthi.dto.UnitDetailsDTO(
                    p.address,
                    p.poiCode,
                    p.pinCode
                )
                FROM PincodePoIMapping p
                WHERE p.companyName = :companyName
                  AND p.unitName = :unitName
            """)
    Optional<UnitDetailsDTO> findUnitDetails(
            @Param("companyName") String companyName,
            @Param("unitName") String unitName);

    @Query("SELECT DISTINCT p.companyName FROM PincodePoIMapping p WHERE p.poiCode IS NOT NULL AND p.poiCode <> ''")
    List<String> findAllDistinctCompanyNames();

    @Query("SELECT DISTINCT p.unitName FROM PincodePoIMapping p WHERE p.companyName = :companyName AND p.poiCode IS NOT NULL AND p.poiCode <> ''")
    List<String> findUnitNamesByCompanyName(@Param("companyName") String companyName);

    Optional<PincodePoIMapping> findByCompanyNameAndUnitName(String companyName, String unitName);

    @Query(value = """
SELECT DISTINCT
    ppm.company_name,
    ppm.unit_name,
    ipm.employee_code,
    ip.rio
FROM pincode_poi_mapping ppm
JOIN ie_pincode_poi_mapping ipm 
     ON ppm.pin_code = ipm.pin_code
     AND ppm.poi_code = ipm.poi_code
JOIN ie_profile ip 
     ON ip.employee_code = ipm.employee_code
ORDER BY ppm.company_name, ppm.unit_name
""", nativeQuery = true)
    List<Object[]> findAllCompanyUnitIe();

    @Query(value = """
           SELECT
                           ppm.company_name,
                           ppm.unit_name,
                           GROUP_CONCAT(DISTINCT um.employee_code ORDER BY um.employee_code) AS employeeCode
                       FROM pincode_poi_mapping ppm
                       JOIN ie_poi_mapping ipm
                           ON ppm.poi_code = ipm.poi_code
                       JOIN process_ie_users piu
                           ON piu.ie_user_id = ipm.ie_user_id
                              OR piu.process_user_id = ipm.ie_user_id
                       JOIN user_master um
                           ON um.userid = piu.ie_user_id
                              OR um.userid = piu.process_user_id
                       GROUP BY ppm.company_name, ppm.unit_name
                       ORDER BY ppm.company_name;
""", nativeQuery = true)
    List<Object[]> findCompanyUnitEmployees();
}
