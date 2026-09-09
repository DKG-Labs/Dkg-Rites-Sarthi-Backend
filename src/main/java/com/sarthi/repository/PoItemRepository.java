package com.sarthi.repository;

import com.sarthi.dto.PoInspection2ndLevelSerialStatusDto;
import com.sarthi.entity.PoHeader;
import com.sarthi.entity.PoItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface PoItemRepository extends JpaRepository<PoItem, Long> {

    /**
     * Find a specific PO item by PO number and item serial number.
     */
    Optional<PoItem> findByPoHeader_PoNoAndItemSrNo(String poNo, String itemSrNo);

    Optional<PoItem> findFirstByPoHeader_PoNoAndItemSrNo(String poNo, String itemSrNo);

    /**
     * Fetch PO items by PO header id.
     */
    List<PoItem> findByPoHeader_Id(Long poHeaderId);

    List<PoItem> findByPoHeader_IdIn(List<Long> poHeaderIds);

    @Query("""
                SELECT new com.sarthi.dto.PoInspection2ndLevelSerialStatusDto(
                    0,
                    pi.itemSrNo,
                    pi.consigneeDetail,
                    pi.deliveryDate,
                    pi.extendedDeliveryDate,
                    pi.qty,
                    (pi.qty - COALESCE(pi.qtyCancelled, 0)),
                    0,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null
                )
                FROM PoItem pi
                JOIN pi.poHeader ph
                WHERE ph.poNo = :poNo
                ORDER BY pi.itemSrNo
            """)
    List<PoInspection2ndLevelSerialStatusDto> fetchSerialStatusByPoNo(@Param("poNo") String poNo);

    @Query("SELECT SUM(pi.qty) FROM PoItem pi WHERE pi.uom = 'Nos.'")
    Long sumQtyByUomNos();

    @Query("SELECT SUM(pi.qty) FROM PoItem pi WHERE pi.uom IN ('Mt', 'Mts', 'Mts.')")
    Double sumQtyByUomMt();

    @Query("SELECT SUM(pi.qty) FROM PoItem pi JOIN pi.poHeader ph WHERE ph.itemCatDescr = :itemCatDescr AND pi.uom = 'Nos.'")
    Long sumQtyByItemCatDescrAndUomNos(@Param("itemCatDescr") String itemCatDescr);

    @Query(value = """
        SELECT SUM(pi.qty) 
        FROM po_item pi 
        JOIN po_header ph ON pi.po_header_id = ph.id
        WHERE (LOWER(ph.item_cat_descr) = LOWER(:itemCatDescr) OR LOWER(ph.item_cat_descr) LIKE CONCAT('%', LOWER(:itemCatDescr), '%') OR (LOWER(:itemCatDescr) LIKE '%rail%pad%' AND (LOWER(ph.item_cat_descr) LIKE '%rail%pad%' OR LOWER(ph.item_cat_descr) LIKE '%railpad%'))) AND pi.uom = 'Nos.'
        AND (:startDate IS NULL OR :startDate = '' OR :endDate IS NULL OR :endDate = '' OR ph.po_date BETWEEN :startDate AND :endDate)
        AND (:zonalRailway IS NULL OR :zonalRailway = '' OR ph.rly_short_name = :zonalRailway OR ph.rly_cd = :zonalRailway)
        AND (:vendorPlantCode IS NULL OR :vendorPlantCode = '' OR 
             ph.vendor_code = :vendorPlantCode OR 
             ph.vendor_code = CONCAT(':', :vendorPlantCode) OR 
             ph.vendor_code = SUBSTRING_INDEX(:vendorPlantCode, '/', 1) OR 
             ph.vendor_code = CONCAT(':', SUBSTRING_INDEX(:vendorPlantCode, '/', 1)) OR 
             ph.vendor_code IN (SELECT rvp.vendor_code FROM rail_vendor_plant rvp WHERE rvp.plant_id = :vendorPlantCode OR rvp.vendor_code = :vendorPlantCode OR rvp.company_name LIKE CONCAT('%', :vendorPlantCode, '%') OR rvp.plant_name LIKE CONCAT('%', :vendorPlantCode, '%')) OR 
             ph.vendor_code IN (SELECT ppm.vendor_code FROM railpad_pincode_poi_mapping ppm WHERE ppm.poi_code = :vendorPlantCode OR ppm.company_name LIKE CONCAT('%', :vendorPlantCode, '%')) OR 
             ph.vendor_code IN (SELECT ppm.vendor_code FROM pincode_poi_mapping ppm WHERE ppm.poi_code = :vendorPlantCode OR ppm.company_name LIKE CONCAT('%', :vendorPlantCode, '%')) OR
             ph.vendor_code IN (SELECT vp.vendor_code FROM vendor_plant vp WHERE vp.company_name LIKE CONCAT('%', :vendorPlantCode, '%') OR vp.plant_id = :vendorPlantCode OR vp.vendor_code = :vendorPlantCode) OR
             ph.vendor_details LIKE CONCAT('%', :vendorPlantCode, '%') OR
             ph.firm_details LIKE CONCAT('%', :vendorPlantCode, '%')
        )
    """, nativeQuery = true)
    Long sumFilteredQtyByItemCatDescrAndUomNos(
            @Param("itemCatDescr") String itemCatDescr,
            @Param("startDate") String startDate,
            @Param("endDate") String endDate,
            @Param("vendorPlantCode") String vendorPlantCode,
            @Param("zonalRailway") String zonalRailway);

    @Query(value = """
        SELECT SUM(pi.qty) 
        FROM po_item pi 
        JOIN po_header ph ON pi.po_header_id = ph.id
        WHERE (LOWER(ph.item_cat_descr) = LOWER(:itemCatDescr) OR LOWER(ph.item_cat_descr) LIKE CONCAT('%', LOWER(:itemCatDescr), '%') OR (LOWER(:itemCatDescr) LIKE '%rail%pad%' AND (LOWER(ph.item_cat_descr) LIKE '%rail%pad%' OR LOWER(ph.item_cat_descr) LIKE '%railpad%'))) AND pi.uom = 'Set'
        AND (:startDate IS NULL OR :startDate = '' OR :endDate IS NULL OR :endDate = '' OR ph.po_date BETWEEN :startDate AND :endDate)
        AND (:zonalRailway IS NULL OR :zonalRailway = '' OR ph.rly_short_name = :zonalRailway OR ph.rly_cd = :zonalRailway)
        AND (:vendorPlantCode IS NULL OR :vendorPlantCode = '' OR 
             ph.vendor_code = :vendorPlantCode OR 
             ph.vendor_code = CONCAT(':', :vendorPlantCode) OR 
             ph.vendor_code = SUBSTRING_INDEX(:vendorPlantCode, '/', 1) OR 
             ph.vendor_code = CONCAT(':', SUBSTRING_INDEX(:vendorPlantCode, '/', 1)) OR 
             ph.vendor_code IN (SELECT rvp.vendor_code FROM rail_vendor_plant rvp WHERE rvp.plant_id = :vendorPlantCode OR rvp.vendor_code = :vendorPlantCode OR rvp.company_name LIKE CONCAT('%', :vendorPlantCode, '%') OR rvp.plant_name LIKE CONCAT('%', :vendorPlantCode, '%')) OR 
             ph.vendor_code IN (SELECT ppm.vendor_code FROM railpad_pincode_poi_mapping ppm WHERE ppm.poi_code = :vendorPlantCode OR ppm.company_name LIKE CONCAT('%', :vendorPlantCode, '%')) OR 
             ph.vendor_code IN (SELECT ppm.vendor_code FROM pincode_poi_mapping ppm WHERE ppm.poi_code = :vendorPlantCode OR ppm.company_name LIKE CONCAT('%', :vendorPlantCode, '%')) OR
             ph.vendor_code IN (SELECT vp.vendor_code FROM vendor_plant vp WHERE vp.company_name LIKE CONCAT('%', :vendorPlantCode, '%') OR vp.plant_id = :vendorPlantCode OR vp.vendor_code = :vendorPlantCode) OR
             ph.vendor_details LIKE CONCAT('%', :vendorPlantCode, '%') OR
             ph.firm_details LIKE CONCAT('%', :vendorPlantCode, '%')
        )
    """, nativeQuery = true)
    Long sumFilteredQtyByItemCatDescrAndUomSet(
            @Param("itemCatDescr") String itemCatDescr,
            @Param("startDate") String startDate,
            @Param("endDate") String endDate,
            @Param("vendorPlantCode") String vendorPlantCode,
            @Param("zonalRailway") String zonalRailway);

    @Query("SELECT SUM(pi.qty) FROM PoItem pi JOIN pi.poHeader ph WHERE ph.itemCatDescr = :itemCatDescr AND pi.uom = 'Set'")
    Long sumQtyByItemCatDescrAndUomSet(@Param("itemCatDescr") String itemCatDescr);

    @Query("SELECT SUM(pi.qty) FROM PoItem pi JOIN pi.poHeader ph WHERE ph.itemCatDescr = :itemCatDescr AND pi.uom IN ('Mt', 'Mts', 'Mts.')")
    Double sumQtyByItemCatDescrAndUomMt(@Param("itemCatDescr") String itemCatDescr);

    @Query(value = """
        SELECT SUM(pi.qty) 
        FROM po_item pi 
        JOIN po_header ph ON pi.po_header_id = ph.id
        WHERE (LOWER(ph.item_cat_descr) = LOWER(:itemCatDescr) OR LOWER(ph.item_cat_descr) LIKE CONCAT('%', LOWER(:itemCatDescr), '%') OR (LOWER(:itemCatDescr) LIKE '%rail%pad%' AND (LOWER(ph.item_cat_descr) LIKE '%rail%pad%' OR LOWER(ph.item_cat_descr) LIKE '%railpad%'))) AND pi.uom IN ('Mt', 'Mts', 'Mts.')
        AND (:startDate IS NULL OR :startDate = '' OR :endDate IS NULL OR :endDate = '' OR ph.po_date BETWEEN :startDate AND :endDate)
        AND (:zonalRailway IS NULL OR :zonalRailway = '' OR ph.rly_short_name = :zonalRailway OR ph.rly_cd = :zonalRailway)
        AND (:vendorPlantCode IS NULL OR :vendorPlantCode = '' OR 
             ph.vendor_code = :vendorPlantCode OR 
             ph.vendor_code = CONCAT(':', :vendorPlantCode) OR 
             ph.vendor_code = SUBSTRING_INDEX(:vendorPlantCode, '/', 1) OR 
             ph.vendor_code = CONCAT(':', SUBSTRING_INDEX(:vendorPlantCode, '/', 1)) OR 
             ph.vendor_code IN (SELECT rvp.vendor_code FROM rail_vendor_plant rvp WHERE rvp.plant_id = :vendorPlantCode OR rvp.vendor_code = :vendorPlantCode OR rvp.company_name LIKE CONCAT('%', :vendorPlantCode, '%') OR rvp.plant_name LIKE CONCAT('%', :vendorPlantCode, '%')) OR 
             ph.vendor_code IN (SELECT ppm.vendor_code FROM railpad_pincode_poi_mapping ppm WHERE ppm.poi_code = :vendorPlantCode OR ppm.company_name LIKE CONCAT('%', :vendorPlantCode, '%')) OR 
             ph.vendor_code IN (SELECT ppm.vendor_code FROM pincode_poi_mapping ppm WHERE ppm.poi_code = :vendorPlantCode OR ppm.company_name LIKE CONCAT('%', :vendorPlantCode, '%')) OR
             ph.vendor_code IN (SELECT vp.vendor_code FROM vendor_plant vp WHERE vp.company_name LIKE CONCAT('%', :vendorPlantCode, '%') OR vp.plant_id = :vendorPlantCode OR vp.vendor_code = :vendorPlantCode) OR
             ph.vendor_details LIKE CONCAT('%', :vendorPlantCode, '%') OR
             ph.firm_details LIKE CONCAT('%', :vendorPlantCode, '%')
        )
    """, nativeQuery = true)
    Double sumFilteredQtyByItemCatDescrAndUomMt(
            @Param("itemCatDescr") String itemCatDescr,
            @Param("startDate") String startDate,
            @Param("endDate") String endDate,
            @Param("vendorPlantCode") String vendorPlantCode,
            @Param("zonalRailway") String zonalRailway);

    @Query(value = """
        SELECT 
            ph.rly_short_name AS rlyShortName,
            ph.po_no AS poNo,
            ph.po_date AS poDate,
            COALESCE(
                NULLIF(CASE 
                    WHEN ph.vendor_details IS NOT NULL AND ph.vendor_details != '' AND ph.vendor_details NOT REGEXP '^[0-9: /-]+$' 
                    THEN TRIM(SUBSTRING_INDEX(ph.vendor_details, '~', 1)) 
                    ELSE NULL 
                END, ''),
                NULLIF(CASE 
                    WHEN ph.firm_details IS NOT NULL AND ph.firm_details != '' AND ph.firm_details NOT REGEXP '^[0-9: /-]+$' 
                    THEN TRIM(ph.firm_details) 
                    ELSE NULL 
                END, ''),
                (SELECT rvp.company_name FROM rail_vendor_plant rvp 
                 WHERE rvp.vendor_code = ph.vendor_code 
                    OR rvp.plant_id = ph.vendor_code 
                 LIMIT 1),
                (SELECT ppm.company_name FROM railpad_pincode_poi_mapping ppm 
                 WHERE ppm.vendor_code = ph.vendor_code 
                    OR ppm.poi_code = ph.vendor_code 
                 LIMIT 1),
                (SELECT vp.company_name FROM vendor_plant vp 
                 WHERE vp.vendor_code = ph.vendor_code 
                    OR vp.plant_id = ph.vendor_code 
                 LIMIT 1),
                NULLIF(TRIM(SUBSTRING_INDEX(ph.vendor_details, '~', 1)), ''),
                ph.vendor_code
            ) AS vendorDetails,
            SUM(pi.qty) AS poQuantity,
            pi.uom AS uom,
            COALESCE((
                SELECT SUM(r.accepted_qty)
                FROM rail_final_inspection_lot_results r
                JOIN rail_inspection_call ic ON r.call_no = ic.call_no
                WHERE (CASE WHEN ic.po_no LIKE '%/%' THEN SUBSTRING_INDEX(ic.po_no, '/', 1) ELSE ic.po_no END) = ph.po_no
            ), 
            (
                SELECT SUM(f.qty_now_passed)
                FROM final_cumulative_results f
                WHERE f.po_no = ph.po_no
            ),
            (
                SELECT SUM(ibs.total_accepted)
                FROM ie_batch_summary ibs
                JOIN sleeper_inspection_call sic ON ibs.call_no = sic.call_no
                WHERE (CASE WHEN sic.po_no LIKE '%/%' THEN SUBSTRING_INDEX(sic.po_no, '/', 1) ELSE sic.po_no END) = ph.po_no
            ),
            (
                SELECT SUM(fci.accepted_qty)
                FROM final_call_inspection_header fci
                WHERE (CASE WHEN fci.rly_po_no LIKE '%/%' THEN SUBSTRING_INDEX(fci.rly_po_no, '/', 1) ELSE fci.rly_po_no END) = ph.po_no
            ), 0) AS acceptedQtyAfterFinalInspection
        FROM po_item pi
        JOIN po_header ph ON pi.po_header_id = ph.id
        WHERE (
            LOWER(ph.item_cat_descr) = LOWER(:itemCatDescr) 
            OR LOWER(ph.item_cat_descr) LIKE CONCAT('%', LOWER(:itemCatDescr), '%') 
            OR (LOWER(:itemCatDescr) LIKE '%rail%pad%' AND (LOWER(ph.item_cat_descr) LIKE '%rail%pad%' OR LOWER(ph.item_cat_descr) LIKE '%railpad%'))
        )
        AND (:vCode IS NULL OR :vCode = '' OR 
             ph.vendor_code = :vCode OR 
             ph.vendor_code = CONCAT(':', :vCode) OR 
             ph.vendor_code = SUBSTRING_INDEX(:vCode, '/', 1) OR 
             ph.vendor_code = CONCAT(':', SUBSTRING_INDEX(:vCode, '/', 1)) OR 
             ph.vendor_code IN (SELECT rvp.vendor_code FROM rail_vendor_plant rvp WHERE rvp.plant_id = :vCode OR rvp.vendor_code = :vCode OR rvp.company_name LIKE CONCAT('%', :vCode, '%') OR rvp.plant_name LIKE CONCAT('%', :vCode, '%')) OR 
             ph.vendor_code IN (SELECT ppm.vendor_code FROM railpad_pincode_poi_mapping ppm WHERE ppm.poi_code = :vCode OR ppm.company_name LIKE CONCAT('%', :vCode, '%')) OR 
             ph.vendor_code IN (SELECT ppm.vendor_code FROM pincode_poi_mapping ppm WHERE ppm.poi_code = :vCode OR ppm.company_name LIKE CONCAT('%', :vCode, '%')) OR
             ph.vendor_code IN (SELECT vp.vendor_code FROM vendor_plant vp WHERE vp.company_name LIKE CONCAT('%', :vCode, '%') OR vp.plant_id = :vCode OR vp.vendor_code = :vCode) OR
             ph.vendor_details LIKE CONCAT('%', :vCode, '%') OR
             ph.firm_details LIKE CONCAT('%', :vCode, '%')
        )
        AND (:zCode IS NULL OR :zCode = '' OR ph.rly_short_name = :zCode OR ph.rly_cd = :zCode)
        AND (:startDate IS NULL OR :endDate IS NULL OR ph.po_date BETWEEN :startDate AND :endDate)
        GROUP BY ph.id, ph.po_no, ph.rly_short_name, ph.po_date, ph.vendor_details, ph.firm_details, ph.vendor_code, pi.uom
        ORDER BY ph.po_date DESC
    """, nativeQuery = true)
    List<Object[]> fetchPoIssuedDetailsRaw(
        @Param("itemCatDescr") String itemCatDescr,
        @Param("vCode") String vCode,
        @Param("zCode") String zCode,
        @Param("startDate") java.time.LocalDateTime startDate,
        @Param("endDate") java.time.LocalDateTime endDate
    );



    List<PoItem> findByPoHeader(PoHeader poHeader);

    Optional<PoItem> findByPoHeaderAndItemSrNo(
            PoHeader poHeader,
            String itemSrNo);
}
