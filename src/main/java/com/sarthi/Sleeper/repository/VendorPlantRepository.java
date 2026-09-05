package com.sarthi.Sleeper.repository;

import com.sarthi.Sleeper.dto.SleeperDashboardDtos.PlantProjection;
import com.sarthi.Sleeper.entity.VendorPlant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.sarthi.Sleeper.dto.SleeperDashboardDtos.PlantDTO;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface VendorPlantRepository extends JpaRepository<VendorPlant, Long> {
    List<VendorPlant> findByVendorCode(String vendorCode);

    Optional<VendorPlant> findByPlantId(String plantId);

    @Query("""
    SELECT v FROM VendorPlant v 
    WHERE v.plantId = :plantId 
       OR REPLACE(COALESCE(v.plantId, ''), ':', '') = REPLACE(:plantId, ':', '')
       OR LOWER(v.plantId) LIKE LOWER(CONCAT('%', :plantId, '%'))
    """)
    List<VendorPlant> findMatchingPlants(@Param("plantId") String plantId);

    Optional<VendorPlant> findByCompanyNameAndPlantName(String companyName, String plantName);


    @Query("""
    SELECT DISTINCT m.plantId 
    FROM SleeperPoiIeMapping m
    WHERE m.ieUserId = :userId
""")
    List<String> findPlantIdsByUserId(@Param("userId") Integer userId);

    List<VendorPlant> findByVendorId(Long vendorCode);
    @Query(value = """
    SELECT plant_name AS plantName,
           plant_id AS plantId
    FROM vendor_plant
    WHERE vendor_id = :vendorCode
""", nativeQuery = true)
    List<PlantProjection> getPlants(String vendorCode);

    @Query("""
    SELECT DISTINCT v.plantId
    FROM VendorPlant v
    WHERE v.vendorId = :vendorCode
    ORDER BY v.plantId
    """)
    List<String> findPlantIdsByVendorCode(String vendorCode);

    @Query("SELECT DISTINCT v.companyName FROM VendorPlant v WHERE v.companyName IS NOT NULL AND v.companyName <> '' ORDER BY v.companyName")
    List<String> findDistinctCompanyNames();

    @Query("SELECT DISTINCT new com.sarthi.Sleeper.dto.SleeperDashboardDtos.PlantDTO(v.plantName, v.plantId) FROM VendorPlant v WHERE v.companyName = :companyName AND v.plantId IS NOT NULL AND v.plantId <> '' ORDER BY v.plantName")
    List<PlantDTO> findPlantsByCompanyName(@Param("companyName") String companyName);

    @Query("select vp.vendorCode from VendorPlant vp where vp.plantId = :plantId")
    Optional<String> findVendorCodeByPlantId(@Param("plantId") String plantId);

    @Query(value = """
    SELECT zonal_railway
    FROM vendor_plant
    WHERE plant_id = :plantId
      AND zonal_railway IS NOT NULL
    LIMIT 1
    """, nativeQuery = true)
    String findZonalRailwayByPlantId(@Param("plantId") String plantId);

    @Query(value = """
    SELECT DISTINCT company_name FROM (
        SELECT vp.company_name AS company_name
        FROM vendor_plant vp
        WHERE (:zone IS NULL OR :zone = '' OR :zone = 'all'
               OR UPPER(TRIM(vp.zonal_railway)) = UPPER(TRIM(:zone)))
          AND vp.company_name IS NOT NULL AND vp.company_name <> ''

        UNION

        SELECT COALESCE(vp.company_name, ph.vendor_details, ph.firm_details) AS company_name
        FROM po_header ph
        LEFT JOIN vendor_plant vp ON (
            vp.vendor_code = ph.vendor_code 
            OR vp.vendor_code = CONCAT(':', ph.vendor_code) 
            OR REPLACE(COALESCE(vp.vendor_code, ''), ':', '') = REPLACE(COALESCE(ph.vendor_code, ''), ':', '')
            OR UPPER(TRIM(vp.company_name)) = UPPER(TRIM(ph.vendor_details))
            OR UPPER(TRIM(vp.company_name)) = UPPER(TRIM(ph.firm_details))
        )
        WHERE LOWER(TRIM(ph.item_cat_descr)) LIKE '%sleeper%'
          AND (:zone IS NULL OR :zone = '' OR :zone = 'all'
               OR UPPER(TRIM(ph.rly_short_name)) = UPPER(TRIM(:zone))
               OR UPPER(TRIM(ph.rly_cd)) = UPPER(TRIM(:zone)))
          AND COALESCE(vp.company_name, ph.vendor_details, ph.firm_details) IS NOT NULL
          AND COALESCE(vp.company_name, ph.vendor_details, ph.firm_details) <> ''
    ) t
    ORDER BY company_name ASC
    """, nativeQuery = true)
    List<String> findDistinctCompanyNamesByZone(@Param("zone") String zone);

    @Query(value = """
    SELECT DISTINCT plant_id FROM (
        SELECT vp.plant_id AS plant_id
        FROM vendor_plant vp
        WHERE (:zone IS NULL OR :zone = '' OR :zone = 'all')
          AND (:companyName IS NULL OR :companyName = '' OR :companyName = 'all'
               OR UPPER(TRIM(vp.company_name)) = UPPER(TRIM(:companyName)) 
               OR UPPER(TRIM(vp.vendor_code)) = UPPER(TRIM(:companyName)) 
               OR UPPER(TRIM(vp.plant_id)) = UPPER(TRIM(:companyName))
               OR REPLACE(COALESCE(vp.plant_id, ''), ':', '') = REPLACE(:companyName, ':', ''))
          AND vp.plant_id IS NOT NULL AND vp.plant_id <> ''

        UNION

        SELECT vp.plant_id AS plant_id
        FROM po_header ph
        JOIN vendor_plant vp ON (
            vp.vendor_code = ph.vendor_code 
            OR vp.vendor_code = CONCAT(':', ph.vendor_code) 
            OR REPLACE(COALESCE(vp.vendor_code, ''), ':', '') = REPLACE(COALESCE(ph.vendor_code, ''), ':', '')
            OR UPPER(TRIM(vp.company_name)) = UPPER(TRIM(ph.vendor_details))
            OR UPPER(TRIM(vp.company_name)) = UPPER(TRIM(ph.firm_details))
        )
        WHERE LOWER(TRIM(ph.item_cat_descr)) LIKE '%sleeper%'
          AND (:zone IS NULL OR :zone = '' OR :zone = 'all'
               OR UPPER(TRIM(ph.rly_short_name)) = UPPER(TRIM(:zone))
               OR UPPER(TRIM(ph.rly_cd)) = UPPER(TRIM(:zone)))
          AND (:companyName IS NULL OR :companyName = '' OR :companyName = 'all'
               OR UPPER(TRIM(vp.company_name)) = UPPER(TRIM(:companyName))
               OR UPPER(TRIM(ph.vendor_details)) = UPPER(TRIM(:companyName))
               OR UPPER(TRIM(ph.firm_details)) = UPPER(TRIM(:companyName))
               OR UPPER(TRIM(vp.vendor_code)) = UPPER(TRIM(:companyName))
               OR UPPER(TRIM(ph.vendor_code)) = UPPER(TRIM(:companyName))
               OR UPPER(TRIM(vp.plant_id)) = UPPER(TRIM(:companyName))
               OR REPLACE(COALESCE(vp.plant_id, ''), ':', '') = REPLACE(:companyName, ':', ''))
          AND vp.plant_id IS NOT NULL AND vp.plant_id <> ''

        UNION

        SELECT sic.plant_id AS plant_id
        FROM sleeper_inspection_call sic
        JOIN po_header ph ON (
            ph.po_no COLLATE utf8mb4_unicode_ci = sic.po_no COLLATE utf8mb4_unicode_ci 
            OR ph.po_no COLLATE utf8mb4_unicode_ci = SUBSTRING_INDEX(sic.po_no, '/', 1) COLLATE utf8mb4_unicode_ci
        )
        LEFT JOIN vendor_plant vp ON (
            vp.plant_id COLLATE utf8mb4_unicode_ci = sic.plant_id COLLATE utf8mb4_unicode_ci
            OR REPLACE(COALESCE(vp.plant_id, ''), ':', '') COLLATE utf8mb4_unicode_ci = REPLACE(COALESCE(sic.plant_id, ''), ':', '') COLLATE utf8mb4_unicode_ci
        )
        WHERE (:zone IS NULL OR :zone = '' OR :zone = 'all'
               OR UPPER(TRIM(ph.rly_short_name)) = UPPER(TRIM(:zone))
               OR UPPER(TRIM(ph.rly_cd)) = UPPER(TRIM(:zone)))
          AND (:companyName IS NULL OR :companyName = '' OR :companyName = 'all'
               OR UPPER(TRIM(COALESCE(vp.company_name, ''))) = UPPER(TRIM(:companyName))
               OR UPPER(TRIM(COALESCE(ph.vendor_details, ''))) = UPPER(TRIM(:companyName))
               OR UPPER(TRIM(COALESCE(ph.firm_details, ''))) = UPPER(TRIM(:companyName))
               OR UPPER(TRIM(COALESCE(ph.vendor_code, ''))) = UPPER(TRIM(:companyName))
               OR UPPER(TRIM(COALESCE(sic.plant_id, ''))) = UPPER(TRIM(:companyName))
               OR REPLACE(COALESCE(sic.plant_id, ''), ':', '') = REPLACE(:companyName, ':', ''))
          AND sic.plant_id IS NOT NULL AND sic.plant_id <> ''
    ) t
    """, nativeQuery = true)
    List<String> findPlantIdsByCompanyAndZone(@Param("companyName") String companyName, @Param("zone") String zone);
}
