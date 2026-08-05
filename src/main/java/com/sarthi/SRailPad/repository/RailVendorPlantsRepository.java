package com.sarthi.SRailPad.repository;

import com.sarthi.SRailPad.entity.raipadMapping.RailVendorPlants;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RailVendorPlantsRepository extends JpaRepository<RailVendorPlants, Long> {
    @Query("""
select r.vendorId
from RailVendorPlants r
where r.vendorCode = :vendorCode
""")
    Optional<Long> findVendorUserIdByVendorCode(
            @Param("vendorCode") String vendorCode
    );

    Optional<RailVendorPlants> findByPlantId(String plantId);
    java.util.List<RailVendorPlants> findByVendorCode(String vendorCode);
    boolean existsByPlantId(String plantId);
    boolean existsByVendorCode(String vendorCode);

    @Query("SELECT DISTINCT r.plantId FROM RailVendorPlants r WHERE r.vendorCode IN :vendorCodes AND r.plantId IS NOT NULL")
    java.util.List<String> findDistinctPlantIdsByVendorCodeIn(@Param("vendorCodes") java.util.List<String> vendorCodes);

    @Query(value = """
        SELECT 
            rvp.plant_name AS plantName,
            COALESCE(ifm.rio, 'N/A') AS rio,
            COALESCE(prod.prod_qty, 0) AS production,
            COALESCE(fin.accept_qty, 0) AS acceptance,
            COALESCE(proc.proc_rej_qty, 0) AS processRejection,
            COALESCE(fin.final_rej_qty, 0) AS finalRejection
        FROM rail_vendor_plant rvp
        LEFT JOIN railpad_pincode_poi_mapping ppm ON CONVERT(ppm.vendor_code USING utf8mb4) COLLATE utf8mb4_unicode_ci = CONVERT(rvp.vendor_code USING utf8mb4) COLLATE utf8mb4_unicode_ci
        LEFT JOIN ie_fields_mapping ifm ON CONVERT(ifm.pin_code USING utf8mb4) COLLATE utf8mb4_unicode_ci = CONVERT(ppm.pin_code USING utf8mb4) COLLATE utf8mb4_unicode_ci AND ifm.product = 'Rail Pad'
        LEFT JOIN (
            SELECT rpd.plant_id, SUM(COALESCE(rpb.quantity, 0)) AS prod_qty
            FROM rail_production_declaration rpd
            JOIN rail_production_product rpp ON rpp.declaration_id = rpd.id
            JOIN rail_production_batch rpb ON rpb.product_id = rpp.id
            WHERE (:startDate IS NULL OR rpd.production_date >= :startDate)
              AND (:endDate IS NULL OR rpd.production_date <= :endDate)
            GROUP BY rpd.plant_id
        ) prod ON CONVERT(prod.plant_id USING utf8mb4) COLLATE utf8mb4_unicode_ci = CONVERT(rvp.plant_id USING utf8mb4) COLLATE utf8mb4_unicode_ci
        LEFT JOIN (
            SELECT v.production_unit AS plant_id, SUM(COALESCE(re.rejected_qty, 0)) AS proc_rej_qty
            FROM rail_ie_production_verification v
            JOIN rail_ie_production_rejection re ON re.verification_id = v.id
            WHERE (:startDate IS NULL OR v.casting_date >= :startDate)
              AND (:endDate IS NULL OR v.casting_date <= :endDate)
            GROUP BY v.production_unit
        ) proc ON CONVERT(proc.plant_id USING utf8mb4) COLLATE utf8mb4_unicode_ci = CONVERT(rvp.plant_id USING utf8mb4) COLLATE utf8mb4_unicode_ci
        LEFT JOIN (
            SELECT r.plant_id, SUM(COALESCE(r.accepted_qty, 0)) AS accept_qty, SUM(COALESCE(r.rejected_qty, 0)) AS final_rej_qty
            FROM rail_final_inspection_lot_results r
            WHERE (:startDate IS NULL OR r.date_of_inspection >= :startDate)
              AND (:endDate IS NULL OR r.date_of_inspection <= :endDate)
              AND (:zone IS NULL OR :zone = '' OR 
                   CONVERT((CASE 
                       WHEN r.rly_po_sr_no LIKE '%/%' THEN SUBSTRING_INDEX(r.rly_po_sr_no, '/', 1) 
                       ELSE r.rly_po_sr_no 
                   END) USING utf8mb4) COLLATE utf8mb4_unicode_ci = :zone)
            GROUP BY r.plant_id
        ) fin ON CONVERT(fin.plant_id USING utf8mb4) COLLATE utf8mb4_unicode_ci = CONVERT(rvp.plant_id USING utf8mb4) COLLATE utf8mb4_unicode_ci
        WHERE (:rio IS NULL OR :rio = '' OR CONVERT(ifm.rio USING utf8mb4) COLLATE utf8mb4_unicode_ci = :rio)
          AND (:vendor IS NULL OR :vendor = '' OR CONVERT(rvp.company_name USING utf8mb4) COLLATE utf8mb4_unicode_ci = :vendor)
        ORDER BY rvp.plant_name
    """, nativeQuery = true)
    java.util.List<Object[]> fetchRailPadMonthlyAnalysis(
        @Param("startDate") java.time.LocalDate startDate,
        @Param("endDate") java.time.LocalDate endDate,
        @Param("rio") String rio,
        @Param("zone") String zone,
        @Param("vendor") String vendor);
}

