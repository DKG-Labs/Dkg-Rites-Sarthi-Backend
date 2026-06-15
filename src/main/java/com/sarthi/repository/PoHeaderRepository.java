package com.sarthi.repository;

import com.sarthi.Sleeper.dto.RlyProjection;
import com.sarthi.dto.reports.PoInspection1stLevelStatusDto;
import com.sarthi.entity.PoHeader;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PoHeaderRepository extends JpaRepository<PoHeader, Long> {



	/**
	 * Find PO Header by PO Number.
	 */
	Optional<PoHeader> findByPoNo(String poNo);

	List<PoHeader> findByPoNoIn(List<String> poNos);

	/**
	 * Find PO Header by PO Number with items eagerly loaded (JOIN FETCH).
	 * Use this when item data is needed to avoid LazyInitializationException.
	 */
	@Query("SELECT DISTINCT h FROM PoHeader h LEFT JOIN FETCH h.items WHERE h.poNo = :poNo")
	Optional<PoHeader> findByPoNoWithItems(@Param("poNo") String poNo);



    boolean existsByPoKey(String poKey);

    List<PoHeader> findByVendorCode(String vendorCode);

    // @Query("""
    // select distinct h
    // from PoHeader h
    // left join fetch h.items i
    // where h.vendorCode = :vendorCode
    // """)
    // List<PoHeader> findAllByVendorCodeWithItems(String vendorCode);
    @Query("""
                select distinct h
                from PoHeader h
                left join fetch h.items i
                where h.vendorCode = :vendorCode
            """)
    List<PoHeader> findAllByVendorCodeWithItems(String vendorCode);

	@Query("""
       select distinct h
       from PoHeader h
       left join fetch h.items i
       where h.vendorCode = :vendorCode
       and h.itemCatDescr = :itemCatDescr
       """)
	List<PoHeader> findAllByVendorCodeAndItemCatDescrWithItems(String vendorCode, String itemCatDescr);

    /*
     * @Query("""
     * SELECT new com.sarthi.dto.reports.PoInspection1stLevelStatusDto(
     * 0,
     * ph.rlyShortName,
     * ph.poNo,
     * ph.poDate,
     * ph.vendorDetails,
     * ph.inspectingAgency,
     * SUM(pi.qty),
     * null,
     * null,
     * null,
     * null,
     * null,
     * ph.poStatus
     * )
     * FROM PoHeader ph
     * JOIN ph.items pi
     * GROUP BY
     * ph.rlyShortName,
     * ph.poNo,
     * ph.poDate,
     * ph.vendorDetails,
     * ph.inspectingAgency,
     * ph.poStatus
     * """)
     * List<PoInspection1stLevelStatusDto> fetchPoInspectionStatus();
     */
    /*
     * @Query("""
     * SELECT new com.sarthi.dto.reports.PoInspection1stLevelStatusDto(
     * 0,
     * ph.rlyShortName,
     * ph.poNo,
     * ph.poDate,
     * ph.vendorDetails,
     * ph.inspectingAgency,
     * SUM(pi.qty),
     * null,
     * null,
     * null,
     * null,
     * null,
     * ph.poStatus
     * )
     * FROM PoHeader ph
     * JOIN ph.items pi
     * GROUP BY
     * ph.rlyShortName,
     * ph.poNo,
     * ph.poDate,
     * ph.vendorDetails,
     * ph.inspectingAgency,
     * ph.poStatus
     * """)
     * List<PoInspection1stLevelStatusDto> fetchPoInspectionStatus();
     */
    @Query("""
            SELECT new com.sarthi.dto.reports.PoInspection1stLevelStatusDto(
                0,
                ph.rlyShortName,
                ph.poNo,
                ph.poDate,
                ph.vendorDetails,

                COALESCE(ie.rio, ph.inspectingAgency),

                SUM(pi.qty),
                null,
                null,
                null,
                null,
                null,
                ph.poStatus,
                ph.itemCatDescr
            )
            FROM PoHeader ph
            JOIN ph.items pi

            LEFT JOIN PincodePoIMapping ppm
                ON ppm.vendorCode = ph.vendorCode

            LEFT JOIN IEFieldsMapping ie
                ON ie.pinCode = ppm.pinCode

            GROUP BY
                ph.rlyShortName,
                ph.poNo,
                ph.poDate,
                ph.vendorDetails,
                COALESCE(ie.rio, ph.inspectingAgency),
                ph.poStatus,
                ph.itemCatDescr
            """)
    List<PoInspection1stLevelStatusDto> fetchPoInspectionStatus();

    @Query("SELECT COUNT(ph.poNo) FROM PoHeader ph")
    long countTotalPo();

    @Query("SELECT COUNT(ph.poNo) FROM PoHeader ph WHERE ph.itemCatDescr = :itemCatDescr")
    long countPoByItemCatDescr(@Param("itemCatDescr") String itemCatDescr);

    @Query("""
    SELECT DISTINCT p.rlyCd AS rlyCd, p.rlyShortName AS rlyShortName
    FROM PoHeader p
    WHERE p.rlyCd IS NOT NULL
""")
    List<RlyProjection> getUniqueRlyList();

    @Query("""
SELECT p
FROM PoHeader p
WHERE p.poDate BETWEEN :startDate AND :endDate
AND UPPER(p.itemCatDescr) = 'ELASTIC RAIL CLIPS'
""")
    List<PoHeader> findElasticRailClipsPoHeaders(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

	@Query("SELECT DISTINCT h FROM PoHeader h LEFT JOIN FETCH h.items WHERE h.itemCatDescr IS NOT NULL AND LOWER(h.itemCatDescr) LIKE '%rail pad%'")
	List<PoHeader> findRailPadPoHeadersWithItems();

	@Query(value = """
        SELECT 
            ph.rly_short_name AS rly,
            ph.po_no AS poNo,
            COALESCE(ph.firm_details, SUBSTRING_INDEX(ph.vendor_details, '~', 1)) AS manufacturer,
            (SELECT SUM(pi.qty) FROM po_item pi WHERE pi.po_header_id = ph.id) AS poQty,
            (SELECT MIN(pi.uom) FROM po_item pi WHERE pi.po_header_id = ph.id) AS uom,
            COALESCE((
                SELECT SUM(r.accepted_qty)
                FROM rail_final_inspection_lot_results r
                JOIN rail_inspection_call ic ON CONVERT(r.call_no USING utf8mb4) COLLATE utf8mb4_unicode_ci = CONVERT(ic.call_no USING utf8mb4) COLLATE utf8mb4_unicode_ci
                WHERE CONVERT((CASE WHEN ic.po_no LIKE '%/%' THEN SUBSTRING_INDEX(ic.po_no, '/', 1) ELSE ic.po_no END) USING utf8mb4) COLLATE utf8mb4_unicode_ci = CONVERT(ph.po_no USING utf8mb4) COLLATE utf8mb4_unicode_ci
                  AND r.date_of_inspection BETWEEN :startDate AND :endDate
            ), 0) AS dispatchedMonthly,
            COALESCE((
                SELECT SUM(r.accepted_qty)
                FROM rail_final_inspection_lot_results r
                JOIN rail_inspection_call ic ON CONVERT(r.call_no USING utf8mb4) COLLATE utf8mb4_unicode_ci = CONVERT(ic.call_no USING utf8mb4) COLLATE utf8mb4_unicode_ci
                WHERE CONVERT((CASE WHEN ic.po_no LIKE '%/%' THEN SUBSTRING_INDEX(ic.po_no, '/', 1) ELSE ic.po_no END) USING utf8mb4) COLLATE utf8mb4_unicode_ci = CONVERT(ph.po_no USING utf8mb4) COLLATE utf8mb4_unicode_ci
            ), 0) AS totalDispatched
        FROM po_header ph
        LEFT JOIN pincode_poi_mapping p ON CONVERT(p.vendor_code USING utf8mb4) COLLATE utf8mb4_unicode_ci = CONVERT(ph.vendor_code USING utf8mb4) COLLATE utf8mb4_unicode_ci OR CONVERT(p.company_name USING utf8mb4) COLLATE utf8mb4_unicode_ci = CONVERT(ph.firm_details USING utf8mb4) COLLATE utf8mb4_unicode_ci
        LEFT JOIN ie_pincode_poi_mapping ipm ON CONVERT(ipm.poi_code USING utf8mb4) COLLATE utf8mb4_unicode_ci = CONVERT(p.poi_code USING utf8mb4) COLLATE utf8mb4_unicode_ci AND ipm.ie_type = 'PRIMARY'
        LEFT JOIN ie_profile ip ON CONVERT(ip.employee_code USING utf8mb4) COLLATE utf8mb4_unicode_ci = CONVERT(ipm.employee_code USING utf8mb4) COLLATE utf8mb4_unicode_ci
        WHERE LOWER(ph.item_cat_descr) LIKE '%rail pad%'
          AND (:zone IS NULL OR :zone = '' OR CONVERT(ph.rly_short_name USING utf8mb4) COLLATE utf8mb4_unicode_ci = CONVERT(:zone USING utf8mb4) COLLATE utf8mb4_unicode_ci)
          AND (:vendor IS NULL OR :vendor = '' OR CONVERT(ph.firm_details USING utf8mb4) COLLATE utf8mb4_unicode_ci = CONVERT(:vendor USING utf8mb4) COLLATE utf8mb4_unicode_ci OR CONVERT(SUBSTRING_INDEX(ph.vendor_details, '~', 1) USING utf8mb4) COLLATE utf8mb4_unicode_ci = CONVERT(:vendor USING utf8mb4) COLLATE utf8mb4_unicode_ci)
          AND (:rio IS NULL OR :rio = '' OR CONVERT(UPPER(ip.rio) USING utf8mb4) COLLATE utf8mb4_unicode_ci = CONVERT(UPPER(:rio) USING utf8mb4) COLLATE utf8mb4_unicode_ci)
        GROUP BY ph.id, ph.po_no, ph.rly_short_name, ph.firm_details, ph.vendor_details
        ORDER BY ph.po_date DESC, ph.id DESC
    """, nativeQuery = true)
    List<Object[]> fetchRailPadMonthlyProgress(
        @Param("startDate") java.time.LocalDate startDate,
        @Param("endDate") java.time.LocalDate endDate,
        @Param("rio") String rio,
        @Param("zone") String zone,
        @Param("vendor") String vendor);

	@Query("SELECT DISTINCT h.firmDetails, h.vendorCode FROM PoHeader h WHERE h.itemCatDescr IS NOT NULL AND LOWER(h.itemCatDescr) LIKE '%rail pad%' ORDER BY h.firmDetails ASC")
	List<Object[]> findDistinctRailPadVendors();

    List<PoHeader> findByCaseNoIsNull();
}
