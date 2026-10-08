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
    SELECT DISTINCT t.company_name FROM (
        SELECT CONVERT(vp.company_name USING utf8mb4) COLLATE utf8mb4_unicode_ci AS company_name
        FROM vendor_plant vp
        WHERE (:zone IS NULL OR :zone = '' OR :zone = 'all'
               OR UPPER(TRIM(CONVERT(vp.zonal_railway USING utf8mb4) COLLATE utf8mb4_unicode_ci)) = UPPER(TRIM(CONVERT(:zone USING utf8mb4) COLLATE utf8mb4_unicode_ci)))
          AND vp.company_name IS NOT NULL AND TRIM(CONVERT(vp.company_name USING utf8mb4) COLLATE utf8mb4_unicode_ci) <> ''

        UNION ALL

        SELECT COALESCE(
            CONVERT(vp.company_name USING utf8mb4) COLLATE utf8mb4_unicode_ci,
            CONVERT(ph.vendor_details USING utf8mb4) COLLATE utf8mb4_unicode_ci,
            CONVERT(ph.firm_details USING utf8mb4) COLLATE utf8mb4_unicode_ci
        ) AS company_name
        FROM po_header ph
        LEFT JOIN vendor_plant vp ON (
            CONVERT(vp.vendor_code USING utf8mb4) COLLATE utf8mb4_unicode_ci = CONVERT(ph.vendor_code USING utf8mb4) COLLATE utf8mb4_unicode_ci 
            OR CONVERT(vp.vendor_code USING utf8mb4) COLLATE utf8mb4_unicode_ci = CONVERT(CONCAT(':', ph.vendor_code) USING utf8mb4) COLLATE utf8mb4_unicode_ci 
            OR CONVERT(REPLACE(COALESCE(CONVERT(vp.vendor_code USING utf8mb4) COLLATE utf8mb4_unicode_ci, ''), ':', '') USING utf8mb4) COLLATE utf8mb4_unicode_ci = CONVERT(REPLACE(COALESCE(CONVERT(ph.vendor_code USING utf8mb4) COLLATE utf8mb4_unicode_ci, ''), ':', '') USING utf8mb4) COLLATE utf8mb4_unicode_ci
            OR UPPER(TRIM(CONVERT(vp.company_name USING utf8mb4) COLLATE utf8mb4_unicode_ci)) = UPPER(TRIM(CONVERT(ph.vendor_details USING utf8mb4) COLLATE utf8mb4_unicode_ci))
            OR UPPER(TRIM(CONVERT(vp.company_name USING utf8mb4) COLLATE utf8mb4_unicode_ci)) = UPPER(TRIM(CONVERT(ph.firm_details USING utf8mb4) COLLATE utf8mb4_unicode_ci))
        )
        WHERE LOWER(TRIM(CONVERT(ph.item_cat_descr USING utf8mb4) COLLATE utf8mb4_unicode_ci)) LIKE '%sleeper%'
          AND (:zone IS NULL OR :zone = '' OR :zone = 'all'
               OR UPPER(TRIM(CONVERT(ph.rly_short_name USING utf8mb4) COLLATE utf8mb4_unicode_ci)) = UPPER(TRIM(CONVERT(:zone USING utf8mb4) COLLATE utf8mb4_unicode_ci))
               OR UPPER(TRIM(CONVERT(ph.rly_cd USING utf8mb4) COLLATE utf8mb4_unicode_ci)) = UPPER(TRIM(CONVERT(:zone USING utf8mb4) COLLATE utf8mb4_unicode_ci)))
    ) t
    WHERE t.company_name IS NOT NULL AND TRIM(t.company_name) <> ''
    ORDER BY t.company_name ASC
    """, nativeQuery = true)
    List<String> findDistinctCompanyNamesByZone(@Param("zone") String zone);

    @Query(value = """
    SELECT DISTINCT t.plant_id FROM (
        SELECT CONVERT(vp.plant_id USING utf8mb4) COLLATE utf8mb4_unicode_ci AS plant_id
        FROM vendor_plant vp
        WHERE (:zone IS NULL OR :zone = '' OR :zone = 'all'
               OR UPPER(TRIM(CONVERT(vp.zonal_railway USING utf8mb4) COLLATE utf8mb4_unicode_ci)) = UPPER(TRIM(CONVERT(:zone USING utf8mb4) COLLATE utf8mb4_unicode_ci)))
          AND (:companyName IS NULL OR :companyName = '' OR :companyName = 'all'
               OR UPPER(TRIM(CONVERT(vp.company_name USING utf8mb4) COLLATE utf8mb4_unicode_ci)) = UPPER(TRIM(CONVERT(:companyName USING utf8mb4) COLLATE utf8mb4_unicode_ci)) 
               OR UPPER(TRIM(CONVERT(vp.vendor_code USING utf8mb4) COLLATE utf8mb4_unicode_ci)) = UPPER(TRIM(CONVERT(:companyName USING utf8mb4) COLLATE utf8mb4_unicode_ci)) 
               OR UPPER(TRIM(CONVERT(vp.plant_id USING utf8mb4) COLLATE utf8mb4_unicode_ci)) = UPPER(TRIM(CONVERT(:companyName USING utf8mb4) COLLATE utf8mb4_unicode_ci))
               OR CONVERT(REPLACE(COALESCE(CONVERT(vp.plant_id USING utf8mb4) COLLATE utf8mb4_unicode_ci, ''), ':', '') USING utf8mb4) COLLATE utf8mb4_unicode_ci = CONVERT(REPLACE(COALESCE(CONVERT(:companyName USING utf8mb4) COLLATE utf8mb4_unicode_ci, ''), ':', '') USING utf8mb4) COLLATE utf8mb4_unicode_ci
               OR REPLACE(REPLACE(REPLACE(CONVERT(COALESCE(vp.company_name, '') USING utf8mb4) COLLATE utf8mb4_unicode_ci, ' ', ''), '.', ''), '+', '') LIKE CONCAT('%', REPLACE(REPLACE(REPLACE(CONVERT(:companyName USING utf8mb4) COLLATE utf8mb4_unicode_ci, ' ', ''), '.', ''), '+', ''), '%')
               OR REPLACE(REPLACE(REPLACE(CONVERT(:companyName USING utf8mb4) COLLATE utf8mb4_unicode_ci, ' ', ''), '.', ''), '+', '') LIKE CONCAT('%', REPLACE(REPLACE(REPLACE(CONVERT(COALESCE(vp.company_name, '') USING utf8mb4) COLLATE utf8mb4_unicode_ci, ' ', ''), '.', ''), '+', ''), '%'))
          AND vp.plant_id IS NOT NULL AND TRIM(CONVERT(vp.plant_id USING utf8mb4) COLLATE utf8mb4_unicode_ci) <> ''

        UNION ALL

        SELECT CONVERT(vp.plant_id USING utf8mb4) COLLATE utf8mb4_unicode_ci AS plant_id
        FROM po_header ph
        JOIN vendor_plant vp ON (
            CONVERT(vp.vendor_code USING utf8mb4) COLLATE utf8mb4_unicode_ci = CONVERT(ph.vendor_code USING utf8mb4) COLLATE utf8mb4_unicode_ci 
            OR CONVERT(vp.vendor_code USING utf8mb4) COLLATE utf8mb4_unicode_ci = CONVERT(CONCAT(':', ph.vendor_code) USING utf8mb4) COLLATE utf8mb4_unicode_ci 
            OR CONVERT(REPLACE(COALESCE(CONVERT(vp.vendor_code USING utf8mb4) COLLATE utf8mb4_unicode_ci, ''), ':', '') USING utf8mb4) COLLATE utf8mb4_unicode_ci = CONVERT(REPLACE(COALESCE(CONVERT(ph.vendor_code USING utf8mb4) COLLATE utf8mb4_unicode_ci, ''), ':', '') USING utf8mb4) COLLATE utf8mb4_unicode_ci
            OR UPPER(TRIM(CONVERT(vp.company_name USING utf8mb4) COLLATE utf8mb4_unicode_ci)) = UPPER(TRIM(CONVERT(ph.vendor_details USING utf8mb4) COLLATE utf8mb4_unicode_ci))
            OR UPPER(TRIM(CONVERT(vp.company_name USING utf8mb4) COLLATE utf8mb4_unicode_ci)) = UPPER(TRIM(CONVERT(ph.firm_details USING utf8mb4) COLLATE utf8mb4_unicode_ci))
        )
        WHERE LOWER(TRIM(CONVERT(ph.item_cat_descr USING utf8mb4) COLLATE utf8mb4_unicode_ci)) LIKE '%sleeper%'
          AND (:zone IS NULL OR :zone = '' OR :zone = 'all'
               OR UPPER(TRIM(CONVERT(ph.rly_short_name USING utf8mb4) COLLATE utf8mb4_unicode_ci)) = UPPER(TRIM(CONVERT(:zone USING utf8mb4) COLLATE utf8mb4_unicode_ci))
               OR UPPER(TRIM(CONVERT(ph.rly_cd USING utf8mb4) COLLATE utf8mb4_unicode_ci)) = UPPER(TRIM(CONVERT(:zone USING utf8mb4) COLLATE utf8mb4_unicode_ci)))
          AND (:companyName IS NULL OR :companyName = '' OR :companyName = 'all'
               OR UPPER(TRIM(CONVERT(vp.company_name USING utf8mb4) COLLATE utf8mb4_unicode_ci)) = UPPER(TRIM(CONVERT(:companyName USING utf8mb4) COLLATE utf8mb4_unicode_ci))
               OR UPPER(TRIM(CONVERT(ph.vendor_details USING utf8mb4) COLLATE utf8mb4_unicode_ci)) = UPPER(TRIM(CONVERT(:companyName USING utf8mb4) COLLATE utf8mb4_unicode_ci))
               OR UPPER(TRIM(CONVERT(ph.firm_details USING utf8mb4) COLLATE utf8mb4_unicode_ci)) = UPPER(TRIM(CONVERT(:companyName USING utf8mb4) COLLATE utf8mb4_unicode_ci))
               OR UPPER(TRIM(CONVERT(vp.vendor_code USING utf8mb4) COLLATE utf8mb4_unicode_ci)) = UPPER(TRIM(CONVERT(:companyName USING utf8mb4) COLLATE utf8mb4_unicode_ci))
               OR UPPER(TRIM(CONVERT(ph.vendor_code USING utf8mb4) COLLATE utf8mb4_unicode_ci)) = UPPER(TRIM(CONVERT(:companyName USING utf8mb4) COLLATE utf8mb4_unicode_ci))
               OR UPPER(TRIM(CONVERT(vp.plant_id USING utf8mb4) COLLATE utf8mb4_unicode_ci)) = UPPER(TRIM(CONVERT(:companyName USING utf8mb4) COLLATE utf8mb4_unicode_ci))
               OR CONVERT(REPLACE(COALESCE(CONVERT(vp.plant_id USING utf8mb4) COLLATE utf8mb4_unicode_ci, ''), ':', '') USING utf8mb4) COLLATE utf8mb4_unicode_ci = CONVERT(REPLACE(COALESCE(CONVERT(:companyName USING utf8mb4) COLLATE utf8mb4_unicode_ci, ''), ':', '') USING utf8mb4) COLLATE utf8mb4_unicode_ci
               OR REPLACE(REPLACE(REPLACE(CONVERT(COALESCE(ph.vendor_details, '') USING utf8mb4) COLLATE utf8mb4_unicode_ci, ' ', ''), '.', ''), '+', '') LIKE CONCAT('%', REPLACE(REPLACE(REPLACE(CONVERT(:companyName USING utf8mb4) COLLATE utf8mb4_unicode_ci, ' ', ''), '.', ''), '+', ''), '%')
               OR REPLACE(REPLACE(REPLACE(CONVERT(:companyName USING utf8mb4) COLLATE utf8mb4_unicode_ci, ' ', ''), '.', ''), '+', '') LIKE CONCAT('%', REPLACE(REPLACE(REPLACE(CONVERT(COALESCE(ph.vendor_details, '') USING utf8mb4) COLLATE utf8mb4_unicode_ci, ' ', ''), '.', ''), '+', ''), '%')
               OR REPLACE(REPLACE(REPLACE(CONVERT(COALESCE(ph.firm_details, '') USING utf8mb4) COLLATE utf8mb4_unicode_ci, ' ', ''), '.', ''), '+', '') LIKE CONCAT('%', REPLACE(REPLACE(REPLACE(CONVERT(:companyName USING utf8mb4) COLLATE utf8mb4_unicode_ci, ' ', ''), '.', ''), '+', ''), '%')
               OR REPLACE(REPLACE(REPLACE(CONVERT(:companyName USING utf8mb4) COLLATE utf8mb4_unicode_ci, ' ', ''), '.', ''), '+', '') LIKE CONCAT('%', REPLACE(REPLACE(REPLACE(CONVERT(COALESCE(ph.firm_details, '') USING utf8mb4) COLLATE utf8mb4_unicode_ci, ' ', ''), '.', ''), '+', ''), '%'))
          AND vp.plant_id IS NOT NULL AND TRIM(CONVERT(vp.plant_id USING utf8mb4) COLLATE utf8mb4_unicode_ci) <> ''

        UNION ALL

        SELECT CONVERT(sic.plant_id USING utf8mb4) COLLATE utf8mb4_unicode_ci AS plant_id
        FROM sleeper_inspection_call sic
        JOIN po_header ph ON (
            CONVERT(ph.po_no USING utf8mb4) COLLATE utf8mb4_unicode_ci = CONVERT(sic.po_no USING utf8mb4) COLLATE utf8mb4_unicode_ci 
            OR CONVERT(ph.po_no USING utf8mb4) COLLATE utf8mb4_unicode_ci = CONVERT(SUBSTRING_INDEX(sic.po_no, '/', 1) USING utf8mb4) COLLATE utf8mb4_unicode_ci
        )
        LEFT JOIN vendor_plant vp ON (
            CONVERT(vp.plant_id USING utf8mb4) COLLATE utf8mb4_unicode_ci = CONVERT(sic.plant_id USING utf8mb4) COLLATE utf8mb4_unicode_ci
            OR CONVERT(REPLACE(COALESCE(CONVERT(vp.plant_id USING utf8mb4) COLLATE utf8mb4_unicode_ci, ''), ':', '') USING utf8mb4) COLLATE utf8mb4_unicode_ci = CONVERT(REPLACE(COALESCE(CONVERT(sic.plant_id USING utf8mb4) COLLATE utf8mb4_unicode_ci, ''), ':', '') USING utf8mb4) COLLATE utf8mb4_unicode_ci
        )
        WHERE (:zone IS NULL OR :zone = '' OR :zone = 'all'
               OR UPPER(TRIM(CONVERT(ph.rly_short_name USING utf8mb4) COLLATE utf8mb4_unicode_ci)) = UPPER(TRIM(CONVERT(:zone USING utf8mb4) COLLATE utf8mb4_unicode_ci))
               OR UPPER(TRIM(CONVERT(ph.rly_cd USING utf8mb4) COLLATE utf8mb4_unicode_ci)) = UPPER(TRIM(CONVERT(:zone USING utf8mb4) COLLATE utf8mb4_unicode_ci)))
          AND (:companyName IS NULL OR :companyName = '' OR :companyName = 'all'
               OR UPPER(TRIM(COALESCE(CONVERT(vp.company_name USING utf8mb4) COLLATE utf8mb4_unicode_ci, ''))) = UPPER(TRIM(CONVERT(:companyName USING utf8mb4) COLLATE utf8mb4_unicode_ci))
               OR UPPER(TRIM(COALESCE(CONVERT(ph.vendor_details USING utf8mb4) COLLATE utf8mb4_unicode_ci, ''))) = UPPER(TRIM(CONVERT(:companyName USING utf8mb4) COLLATE utf8mb4_unicode_ci))
               OR UPPER(TRIM(COALESCE(CONVERT(ph.firm_details USING utf8mb4) COLLATE utf8mb4_unicode_ci, ''))) = UPPER(TRIM(CONVERT(:companyName USING utf8mb4) COLLATE utf8mb4_unicode_ci))
               OR UPPER(TRIM(COALESCE(CONVERT(ph.vendor_code USING utf8mb4) COLLATE utf8mb4_unicode_ci, ''))) = UPPER(TRIM(CONVERT(:companyName USING utf8mb4) COLLATE utf8mb4_unicode_ci))
               OR UPPER(TRIM(COALESCE(CONVERT(sic.plant_id USING utf8mb4) COLLATE utf8mb4_unicode_ci, ''))) = UPPER(TRIM(CONVERT(:companyName USING utf8mb4) COLLATE utf8mb4_unicode_ci))
               OR CONVERT(REPLACE(COALESCE(CONVERT(sic.plant_id USING utf8mb4) COLLATE utf8mb4_unicode_ci, ''), ':', '') USING utf8mb4) COLLATE utf8mb4_unicode_ci = CONVERT(REPLACE(COALESCE(CONVERT(:companyName USING utf8mb4) COLLATE utf8mb4_unicode_ci, ''), ':', '') USING utf8mb4) COLLATE utf8mb4_unicode_ci
               OR REPLACE(REPLACE(REPLACE(CONVERT(COALESCE(ph.vendor_details, '') USING utf8mb4) COLLATE utf8mb4_unicode_ci, ' ', ''), '.', ''), '+', '') LIKE CONCAT('%', REPLACE(REPLACE(REPLACE(CONVERT(:companyName USING utf8mb4) COLLATE utf8mb4_unicode_ci, ' ', ''), '.', ''), '+', ''), '%')
               OR REPLACE(REPLACE(REPLACE(CONVERT(:companyName USING utf8mb4) COLLATE utf8mb4_unicode_ci, ' ', ''), '.', ''), '+', '') LIKE CONCAT('%', REPLACE(REPLACE(REPLACE(CONVERT(COALESCE(ph.vendor_details, '') USING utf8mb4) COLLATE utf8mb4_unicode_ci, ' ', ''), '.', ''), '+', ''), '%')
               OR REPLACE(REPLACE(REPLACE(CONVERT(COALESCE(ph.firm_details, '') USING utf8mb4) COLLATE utf8mb4_unicode_ci, ' ', ''), '.', ''), '+', '') LIKE CONCAT('%', REPLACE(REPLACE(REPLACE(CONVERT(:companyName USING utf8mb4) COLLATE utf8mb4_unicode_ci, ' ', ''), '.', ''), '+', ''), '%')
               OR REPLACE(REPLACE(REPLACE(CONVERT(:companyName USING utf8mb4) COLLATE utf8mb4_unicode_ci, ' ', ''), '.', ''), '+', '') LIKE CONCAT('%', REPLACE(REPLACE(REPLACE(CONVERT(COALESCE(ph.firm_details, '') USING utf8mb4) COLLATE utf8mb4_unicode_ci, ' ', ''), '.', ''), '+', ''), '%'))
          AND sic.plant_id IS NOT NULL AND TRIM(CONVERT(sic.plant_id USING utf8mb4) COLLATE utf8mb4_unicode_ci) <> ''
    ) t
    WHERE t.plant_id IS NOT NULL AND TRIM(t.plant_id) <> ''
    """, nativeQuery = true)
    List<String> findPlantIdsByCompanyAndZone(@Param("companyName") String companyName, @Param("zone") String zone);
}
