package com.sarthi.SRailPad.repository.scrap;

import com.sarthi.SRailPad.entity.scrap.RailScrapVerification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

/**
 * Scrap verifications plus the live Rail Pad rejection / production queries that feed the Rejection Basket.
 * Plant ids are compared without the leading ':' and product types with UPPER(TRIM(..)).
 */
@Repository
public interface RailScrapVerificationRepository extends JpaRepository<RailScrapVerification, Long> {

    // ---------- Scrap verifications ----------

    @Query(value = """
            SELECT * FROM rail_scrap_verification
            WHERE plant_id = :plantId AND UPPER(TRIM(product_type)) = :productKey AND status = 'ACTIVE'
            ORDER BY scrapping_date, created_date
            """, nativeQuery = true)
    List<RailScrapVerification> findActive(@Param("plantId") String plantId, @Param("productKey") String productKey);

    @Query(value = """
            SELECT COALESCE(SUM(qty_scrapped), 0) FROM rail_scrap_verification
            WHERE plant_id = :plantId AND UPPER(TRIM(product_type)) = :productKey AND status = 'ACTIVE'
            """, nativeQuery = true)
    Long sumScrapped(@Param("plantId") String plantId, @Param("productKey") String productKey);

    /** plant_id, product key, display product type, total scrapped, last scrapping date */
    @Query(value = """
            SELECT plant_id, UPPER(TRIM(product_type)), MAX(product_type),
                   COALESCE(SUM(qty_scrapped), 0), MAX(scrapping_date)
            FROM rail_scrap_verification
            WHERE status = 'ACTIVE'
            GROUP BY plant_id, UPPER(TRIM(product_type))
            """, nativeQuery = true)
    List<Object[]> sumScrappedGrouped();

    @Query(value = """
            SELECT MAX(CAST(SUBSTRING_INDEX(verification_no, '-', -1) AS UNSIGNED))
            FROM rail_scrap_verification WHERE verification_no LIKE :prefix
            """, nativeQuery = true)
    Long maxSequenceForPrefix(@Param("prefix") String prefix);

    /** Locks the plant row so two scrap verifications for the same plant can't both pass the pending check */
    @Query(value = """
            SELECT id FROM rail_vendor_plant WHERE REPLACE(plant_id, ':', '') = :plantId FOR UPDATE
            """, nativeQuery = true)
    List<Long> lockPlant(@Param("plantId") String plantId);

    // ---------- Process rejections (Process IE production verification) ----------

    /** plant_id, product key, display product type, total rejected, rejected since :since */
    @Query(value = """
            SELECT REPLACE(v.production_unit, ':', ''), UPPER(TRIM(r.product_type)), MAX(r.product_type),
                   COALESCE(SUM(r.rejected_qty), 0),
                   COALESCE(SUM(CASE WHEN v.casting_date >= :since THEN r.rejected_qty ELSE 0 END), 0)
            FROM rail_ie_production_rejection r
            JOIN rail_ie_production_verification v ON v.id = r.verification_id
            WHERE r.rejected_qty > 0 AND r.product_type IS NOT NULL
            GROUP BY REPLACE(v.production_unit, ':', ''), UPPER(TRIM(r.product_type))
            """, nativeQuery = true)
    List<Object[]> sumProcessRejectedGrouped(@Param("since") LocalDate since);

    /** date, shift, verification id, request id, product type, batch no, reason, qty, IE id, IE name, created date */
    @Query(value = """
            SELECT v.casting_date, v.shift, v.id, v.request_id, r.product_type, r.batch_no, r.reason,
                   r.rejected_qty, v.created_by, um.full_name, v.created_date
            FROM rail_ie_production_rejection r
            JOIN rail_ie_production_verification v ON v.id = r.verification_id
            LEFT JOIN user_master um ON um.userid = v.created_by
            WHERE REPLACE(v.production_unit, ':', '') = :plantId
              AND UPPER(TRIM(r.product_type)) = :productKey
              AND r.rejected_qty > 0
            ORDER BY v.casting_date, v.created_date
            """, nativeQuery = true)
    List<Object[]> findProcessRejections(@Param("plantId") String plantId, @Param("productKey") String productKey);

    // ---------- Final rejections (Main IE: lots with overall_status REJECTED) ----------

    /** plant_id, product key, display product type, total rejected, rejected since :since */
    @Query(value = """
            SELECT REPLACE(l.plant_id, ':', ''), UPPER(TRIM(l.railpad_type)), MAX(l.railpad_type),
                   COALESCE(SUM(COALESCE(NULLIF(l.rejected_qty, 0), l.offered_qty, 0)), 0),
                   COALESCE(SUM(CASE WHEN l.date_of_inspection >= :since
                                     THEN COALESCE(NULLIF(l.rejected_qty, 0), l.offered_qty, 0) ELSE 0 END), 0)
            FROM rail_final_inspection_lot_results l
            WHERE UPPER(l.overall_status) = 'REJECTED' AND l.plant_id IS NOT NULL AND l.railpad_type IS NOT NULL
            GROUP BY REPLACE(l.plant_id, ':', ''), UPPER(TRIM(l.railpad_type))
            """, nativeQuery = true)
    List<Object[]> sumFinalRejectedGrouped(@Param("since") LocalDate since);

    /**
     * date, shift, lot result id, call no, lot no, product type, qty, IE id, IE name, created date,
     * failed sections, weight test status, remarks
     */
    @Query(value = """
            SELECT l.date_of_inspection, l.shift, l.id, l.call_no, l.lot_no, l.railpad_type,
                   COALESCE(NULLIF(l.rejected_qty, 0), l.offered_qty, 0),
                   l.created_by, um.full_name, l.created_date,
                   (SELECT GROUP_CONCAT(DISTINCT s.section_name ORDER BY s.section_name SEPARATOR ', ')
                      FROM rail_final_inspection_section_results s
                     WHERE s.lot_result_id = l.id AND UPPER(s.status) IN ('FAIL', 'FAILED', 'REJECTED')),
                   (SELECT MAX(w.weight_status) FROM rail_final_weight_test w
                     WHERE CONVERT(w.call_no USING utf8mb4) COLLATE utf8mb4_unicode_ci
                           = CONVERT(l.call_no USING utf8mb4) COLLATE utf8mb4_unicode_ci
                       AND CONVERT(w.lot_no USING utf8mb4) COLLATE utf8mb4_unicode_ci
                           = CONVERT(l.lot_no USING utf8mb4) COLLATE utf8mb4_unicode_ci
                       AND UPPER(w.weight_status) = 'REJECTED'),
                   l.remarks
            FROM rail_final_inspection_lot_results l
            LEFT JOIN user_master um ON um.userid = l.created_by
            WHERE UPPER(l.overall_status) = 'REJECTED'
              AND REPLACE(l.plant_id, ':', '') = :plantId
              AND UPPER(TRIM(l.railpad_type)) = :productKey
            ORDER BY l.date_of_inspection, l.created_date
            """, nativeQuery = true)
    List<Object[]> findFinalRejections(@Param("plantId") String plantId, @Param("productKey") String productKey);

    // ---------- Production (IE-verified) ----------

    /** plant_id, product key, display product type, quantity produced since :since */
    @Query(value = """
            SELECT REPLACE(v.production_unit, ':', ''), UPPER(TRIM(i.product_type)), MAX(i.product_type),
                   COALESCE(SUM(i.quantity_produced), 0)
            FROM rail_ie_production_info i
            JOIN rail_ie_production_verification v ON v.id = i.verification_id
            WHERE v.casting_date >= :since AND i.product_type IS NOT NULL
            GROUP BY REPLACE(v.production_unit, ':', ''), UPPER(TRIM(i.product_type))
            """, nativeQuery = true)
    List<Object[]> sumProductionGrouped(@Param("since") LocalDate since);
}
