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
        AND (:zonalRailway IS NULL OR :zonalRailway = '' OR CONVERT(ph.rly_short_name USING utf8mb4) COLLATE utf8mb4_unicode_ci = CONVERT(:zonalRailway USING utf8mb4) COLLATE utf8mb4_unicode_ci OR CONVERT(ph.rly_cd USING utf8mb4) COLLATE utf8mb4_unicode_ci = CONVERT(:zonalRailway USING utf8mb4) COLLATE utf8mb4_unicode_ci)
        AND (:vendorPlantCode IS NULL OR :vendorPlantCode = '' OR 
             CONVERT(ph.vendor_code USING utf8mb4) COLLATE utf8mb4_unicode_ci = CONVERT(:vendorPlantCode USING utf8mb4) COLLATE utf8mb4_unicode_ci OR 
             CONVERT(ph.vendor_code USING utf8mb4) COLLATE utf8mb4_unicode_ci = CONVERT(CONCAT(':', :vendorPlantCode) USING utf8mb4) COLLATE utf8mb4_unicode_ci OR 
             CONVERT(ph.vendor_code USING utf8mb4) COLLATE utf8mb4_unicode_ci = CONVERT(SUBSTRING_INDEX(:vendorPlantCode, '/', 1) USING utf8mb4) COLLATE utf8mb4_unicode_ci OR 
             CONVERT(ph.vendor_code USING utf8mb4) COLLATE utf8mb4_unicode_ci = CONVERT(CONCAT(':', SUBSTRING_INDEX(:vendorPlantCode, '/', 1)) USING utf8mb4) COLLATE utf8mb4_unicode_ci OR 
             CONVERT(ph.vendor_code USING utf8mb4) COLLATE utf8mb4_unicode_ci IN (SELECT CONVERT(rvp.vendor_code USING utf8mb4) COLLATE utf8mb4_unicode_ci FROM rail_vendor_plant rvp WHERE CONVERT(rvp.plant_id USING utf8mb4) COLLATE utf8mb4_unicode_ci = CONVERT(:vendorPlantCode USING utf8mb4) COLLATE utf8mb4_unicode_ci OR CONVERT(rvp.vendor_code USING utf8mb4) COLLATE utf8mb4_unicode_ci = CONVERT(:vendorPlantCode USING utf8mb4) COLLATE utf8mb4_unicode_ci OR CONVERT(rvp.company_name USING utf8mb4) COLLATE utf8mb4_unicode_ci LIKE CONCAT('%', CONVERT(:vendorPlantCode USING utf8mb4) COLLATE utf8mb4_unicode_ci, '%') OR CONVERT(rvp.plant_name USING utf8mb4) COLLATE utf8mb4_unicode_ci LIKE CONCAT('%', CONVERT(:vendorPlantCode USING utf8mb4) COLLATE utf8mb4_unicode_ci, '%')) OR 
             CONVERT(ph.vendor_code USING utf8mb4) COLLATE utf8mb4_unicode_ci IN (SELECT CONVERT(ppm.vendor_code USING utf8mb4) COLLATE utf8mb4_unicode_ci FROM railpad_pincode_poi_mapping ppm WHERE CONVERT(ppm.poi_code USING utf8mb4) COLLATE utf8mb4_unicode_ci = CONVERT(:vendorPlantCode USING utf8mb4) COLLATE utf8mb4_unicode_ci OR CONVERT(ppm.company_name USING utf8mb4) COLLATE utf8mb4_unicode_ci LIKE CONCAT('%', CONVERT(:vendorPlantCode USING utf8mb4) COLLATE utf8mb4_unicode_ci, '%')) OR 
             CONVERT(ph.vendor_code USING utf8mb4) COLLATE utf8mb4_unicode_ci IN (SELECT CONVERT(ppm.vendor_code USING utf8mb4) COLLATE utf8mb4_unicode_ci FROM pincode_poi_mapping ppm WHERE CONVERT(ppm.poi_code USING utf8mb4) COLLATE utf8mb4_unicode_ci = CONVERT(:vendorPlantCode USING utf8mb4) COLLATE utf8mb4_unicode_ci OR CONVERT(ppm.company_name USING utf8mb4) COLLATE utf8mb4_unicode_ci LIKE CONCAT('%', CONVERT(:vendorPlantCode USING utf8mb4) COLLATE utf8mb4_unicode_ci, '%')) OR
             CONVERT(ph.vendor_code USING utf8mb4) COLLATE utf8mb4_unicode_ci IN (SELECT CONVERT(vp.vendor_code USING utf8mb4) COLLATE utf8mb4_unicode_ci FROM vendor_plant vp WHERE CONVERT(vp.company_name USING utf8mb4) COLLATE utf8mb4_unicode_ci LIKE CONCAT('%', CONVERT(:vendorPlantCode USING utf8mb4) COLLATE utf8mb4_unicode_ci, '%') OR CONVERT(vp.plant_id USING utf8mb4) COLLATE utf8mb4_unicode_ci = CONVERT(:vendorPlantCode USING utf8mb4) COLLATE utf8mb4_unicode_ci OR CONVERT(vp.vendor_code USING utf8mb4) COLLATE utf8mb4_unicode_ci = CONVERT(:vendorPlantCode USING utf8mb4) COLLATE utf8mb4_unicode_ci) OR
             CONVERT(ph.vendor_details USING utf8mb4) COLLATE utf8mb4_unicode_ci LIKE CONCAT('%', CONVERT(:vendorPlantCode USING utf8mb4) COLLATE utf8mb4_unicode_ci, '%') OR
             CONVERT(ph.firm_details USING utf8mb4) COLLATE utf8mb4_unicode_ci LIKE CONCAT('%', CONVERT(:vendorPlantCode USING utf8mb4) COLLATE utf8mb4_unicode_ci, '%')
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
        AND (:zonalRailway IS NULL OR :zonalRailway = '' OR CONVERT(ph.rly_short_name USING utf8mb4) COLLATE utf8mb4_unicode_ci = CONVERT(:zonalRailway USING utf8mb4) COLLATE utf8mb4_unicode_ci OR CONVERT(ph.rly_cd USING utf8mb4) COLLATE utf8mb4_unicode_ci = CONVERT(:zonalRailway USING utf8mb4) COLLATE utf8mb4_unicode_ci)
        AND (:vendorPlantCode IS NULL OR :vendorPlantCode = '' OR 
             CONVERT(ph.vendor_code USING utf8mb4) COLLATE utf8mb4_unicode_ci = CONVERT(:vendorPlantCode USING utf8mb4) COLLATE utf8mb4_unicode_ci OR 
             CONVERT(ph.vendor_code USING utf8mb4) COLLATE utf8mb4_unicode_ci = CONVERT(CONCAT(':', :vendorPlantCode) USING utf8mb4) COLLATE utf8mb4_unicode_ci OR 
             CONVERT(ph.vendor_code USING utf8mb4) COLLATE utf8mb4_unicode_ci = CONVERT(SUBSTRING_INDEX(:vendorPlantCode, '/', 1) USING utf8mb4) COLLATE utf8mb4_unicode_ci OR 
             CONVERT(ph.vendor_code USING utf8mb4) COLLATE utf8mb4_unicode_ci = CONVERT(CONCAT(':', SUBSTRING_INDEX(:vendorPlantCode, '/', 1)) USING utf8mb4) COLLATE utf8mb4_unicode_ci OR 
             CONVERT(ph.vendor_code USING utf8mb4) COLLATE utf8mb4_unicode_ci IN (SELECT CONVERT(rvp.vendor_code USING utf8mb4) COLLATE utf8mb4_unicode_ci FROM rail_vendor_plant rvp WHERE CONVERT(rvp.plant_id USING utf8mb4) COLLATE utf8mb4_unicode_ci = CONVERT(:vendorPlantCode USING utf8mb4) COLLATE utf8mb4_unicode_ci OR CONVERT(rvp.vendor_code USING utf8mb4) COLLATE utf8mb4_unicode_ci = CONVERT(:vendorPlantCode USING utf8mb4) COLLATE utf8mb4_unicode_ci OR CONVERT(rvp.company_name USING utf8mb4) COLLATE utf8mb4_unicode_ci LIKE CONCAT('%', CONVERT(:vendorPlantCode USING utf8mb4) COLLATE utf8mb4_unicode_ci, '%') OR CONVERT(rvp.plant_name USING utf8mb4) COLLATE utf8mb4_unicode_ci LIKE CONCAT('%', CONVERT(:vendorPlantCode USING utf8mb4) COLLATE utf8mb4_unicode_ci, '%')) OR 
             CONVERT(ph.vendor_code USING utf8mb4) COLLATE utf8mb4_unicode_ci IN (SELECT CONVERT(ppm.vendor_code USING utf8mb4) COLLATE utf8mb4_unicode_ci FROM railpad_pincode_poi_mapping ppm WHERE CONVERT(ppm.poi_code USING utf8mb4) COLLATE utf8mb4_unicode_ci = CONVERT(:vendorPlantCode USING utf8mb4) COLLATE utf8mb4_unicode_ci OR CONVERT(ppm.company_name USING utf8mb4) COLLATE utf8mb4_unicode_ci LIKE CONCAT('%', CONVERT(:vendorPlantCode USING utf8mb4) COLLATE utf8mb4_unicode_ci, '%')) OR 
             CONVERT(ph.vendor_code USING utf8mb4) COLLATE utf8mb4_unicode_ci IN (SELECT CONVERT(ppm.vendor_code USING utf8mb4) COLLATE utf8mb4_unicode_ci FROM pincode_poi_mapping ppm WHERE CONVERT(ppm.poi_code USING utf8mb4) COLLATE utf8mb4_unicode_ci = CONVERT(:vendorPlantCode USING utf8mb4) COLLATE utf8mb4_unicode_ci OR CONVERT(ppm.company_name USING utf8mb4) COLLATE utf8mb4_unicode_ci LIKE CONCAT('%', CONVERT(:vendorPlantCode USING utf8mb4) COLLATE utf8mb4_unicode_ci, '%')) OR
             CONVERT(ph.vendor_code USING utf8mb4) COLLATE utf8mb4_unicode_ci IN (SELECT CONVERT(vp.vendor_code USING utf8mb4) COLLATE utf8mb4_unicode_ci FROM vendor_plant vp WHERE CONVERT(vp.company_name USING utf8mb4) COLLATE utf8mb4_unicode_ci LIKE CONCAT('%', CONVERT(:vendorPlantCode USING utf8mb4) COLLATE utf8mb4_unicode_ci, '%') OR CONVERT(vp.plant_id USING utf8mb4) COLLATE utf8mb4_unicode_ci = CONVERT(:vendorPlantCode USING utf8mb4) COLLATE utf8mb4_unicode_ci OR CONVERT(vp.vendor_code USING utf8mb4) COLLATE utf8mb4_unicode_ci = CONVERT(:vendorPlantCode USING utf8mb4) COLLATE utf8mb4_unicode_ci) OR
             CONVERT(ph.vendor_details USING utf8mb4) COLLATE utf8mb4_unicode_ci LIKE CONCAT('%', CONVERT(:vendorPlantCode USING utf8mb4) COLLATE utf8mb4_unicode_ci, '%') OR
             CONVERT(ph.firm_details USING utf8mb4) COLLATE utf8mb4_unicode_ci LIKE CONCAT('%', CONVERT(:vendorPlantCode USING utf8mb4) COLLATE utf8mb4_unicode_ci, '%')
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
        AND (:zonalRailway IS NULL OR :zonalRailway = '' OR CONVERT(ph.rly_short_name USING utf8mb4) COLLATE utf8mb4_unicode_ci = CONVERT(:zonalRailway USING utf8mb4) COLLATE utf8mb4_unicode_ci OR CONVERT(ph.rly_cd USING utf8mb4) COLLATE utf8mb4_unicode_ci = CONVERT(:zonalRailway USING utf8mb4) COLLATE utf8mb4_unicode_ci)
        AND (:vendorPlantCode IS NULL OR :vendorPlantCode = '' OR 
             CONVERT(ph.vendor_code USING utf8mb4) COLLATE utf8mb4_unicode_ci = CONVERT(:vendorPlantCode USING utf8mb4) COLLATE utf8mb4_unicode_ci OR 
             CONVERT(ph.vendor_code USING utf8mb4) COLLATE utf8mb4_unicode_ci = CONVERT(CONCAT(':', :vendorPlantCode) USING utf8mb4) COLLATE utf8mb4_unicode_ci OR 
             CONVERT(ph.vendor_code USING utf8mb4) COLLATE utf8mb4_unicode_ci = CONVERT(SUBSTRING_INDEX(:vendorPlantCode, '/', 1) USING utf8mb4) COLLATE utf8mb4_unicode_ci OR 
             CONVERT(ph.vendor_code USING utf8mb4) COLLATE utf8mb4_unicode_ci = CONVERT(CONCAT(':', SUBSTRING_INDEX(:vendorPlantCode, '/', 1)) USING utf8mb4) COLLATE utf8mb4_unicode_ci OR 
             CONVERT(ph.vendor_code USING utf8mb4) COLLATE utf8mb4_unicode_ci IN (SELECT CONVERT(rvp.vendor_code USING utf8mb4) COLLATE utf8mb4_unicode_ci FROM rail_vendor_plant rvp WHERE CONVERT(rvp.plant_id USING utf8mb4) COLLATE utf8mb4_unicode_ci = CONVERT(:vendorPlantCode USING utf8mb4) COLLATE utf8mb4_unicode_ci OR CONVERT(rvp.vendor_code USING utf8mb4) COLLATE utf8mb4_unicode_ci = CONVERT(:vendorPlantCode USING utf8mb4) COLLATE utf8mb4_unicode_ci OR CONVERT(rvp.company_name USING utf8mb4) COLLATE utf8mb4_unicode_ci LIKE CONCAT('%', CONVERT(:vendorPlantCode USING utf8mb4) COLLATE utf8mb4_unicode_ci, '%') OR CONVERT(rvp.plant_name USING utf8mb4) COLLATE utf8mb4_unicode_ci LIKE CONCAT('%', CONVERT(:vendorPlantCode USING utf8mb4) COLLATE utf8mb4_unicode_ci, '%')) OR 
             CONVERT(ph.vendor_code USING utf8mb4) COLLATE utf8mb4_unicode_ci IN (SELECT CONVERT(ppm.vendor_code USING utf8mb4) COLLATE utf8mb4_unicode_ci FROM railpad_pincode_poi_mapping ppm WHERE CONVERT(ppm.poi_code USING utf8mb4) COLLATE utf8mb4_unicode_ci = CONVERT(:vendorPlantCode USING utf8mb4) COLLATE utf8mb4_unicode_ci OR CONVERT(ppm.company_name USING utf8mb4) COLLATE utf8mb4_unicode_ci LIKE CONCAT('%', CONVERT(:vendorPlantCode USING utf8mb4) COLLATE utf8mb4_unicode_ci, '%')) OR 
             CONVERT(ph.vendor_code USING utf8mb4) COLLATE utf8mb4_unicode_ci IN (SELECT CONVERT(ppm.vendor_code USING utf8mb4) COLLATE utf8mb4_unicode_ci FROM pincode_poi_mapping ppm WHERE CONVERT(ppm.poi_code USING utf8mb4) COLLATE utf8mb4_unicode_ci = CONVERT(:vendorPlantCode USING utf8mb4) COLLATE utf8mb4_unicode_ci OR CONVERT(ppm.company_name USING utf8mb4) COLLATE utf8mb4_unicode_ci LIKE CONCAT('%', CONVERT(:vendorPlantCode USING utf8mb4) COLLATE utf8mb4_unicode_ci, '%')) OR
             CONVERT(ph.vendor_code USING utf8mb4) COLLATE utf8mb4_unicode_ci IN (SELECT CONVERT(vp.vendor_code USING utf8mb4) COLLATE utf8mb4_unicode_ci FROM vendor_plant vp WHERE CONVERT(vp.company_name USING utf8mb4) COLLATE utf8mb4_unicode_ci LIKE CONCAT('%', CONVERT(:vendorPlantCode USING utf8mb4) COLLATE utf8mb4_unicode_ci, '%') OR CONVERT(vp.plant_id USING utf8mb4) COLLATE utf8mb4_unicode_ci = CONVERT(:vendorPlantCode USING utf8mb4) COLLATE utf8mb4_unicode_ci OR CONVERT(vp.vendor_code USING utf8mb4) COLLATE utf8mb4_unicode_ci = CONVERT(:vendorPlantCode USING utf8mb4) COLLATE utf8mb4_unicode_ci) OR
             CONVERT(ph.vendor_details USING utf8mb4) COLLATE utf8mb4_unicode_ci LIKE CONCAT('%', CONVERT(:vendorPlantCode USING utf8mb4) COLLATE utf8mb4_unicode_ci, '%') OR
             CONVERT(ph.firm_details USING utf8mb4) COLLATE utf8mb4_unicode_ci LIKE CONCAT('%', CONVERT(:vendorPlantCode USING utf8mb4) COLLATE utf8mb4_unicode_ci, '%')
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
            ph.vendor_details AS vendorDetails,
            SUM(pi.qty) AS poQuantity,
            pi.uom AS uom
        FROM po_item pi
        JOIN po_header ph ON pi.po_header_id = ph.id
        WHERE (LOWER(ph.item_cat_descr) = LOWER(:itemCatDescr) OR LOWER(ph.item_cat_descr) LIKE CONCAT('%', LOWER(:itemCatDescr), '%') OR (LOWER(:itemCatDescr) LIKE '%rail%pad%' AND (LOWER(ph.item_cat_descr) LIKE '%rail%pad%' OR LOWER(ph.item_cat_descr) LIKE '%railpad%')))
        AND (:vCode IS NULL OR :vCode = '' OR 
             CONVERT(ph.vendor_code USING utf8mb4) COLLATE utf8mb4_unicode_ci = CONVERT(:vCode USING utf8mb4) COLLATE utf8mb4_unicode_ci OR 
             CONVERT(ph.vendor_code USING utf8mb4) COLLATE utf8mb4_unicode_ci = CONVERT(CONCAT(':', :vCode) USING utf8mb4) COLLATE utf8mb4_unicode_ci OR 
             CONVERT(ph.vendor_code USING utf8mb4) COLLATE utf8mb4_unicode_ci = CONVERT(SUBSTRING_INDEX(:vCode, '/', 1) USING utf8mb4) COLLATE utf8mb4_unicode_ci OR 
             CONVERT(ph.vendor_code USING utf8mb4) COLLATE utf8mb4_unicode_ci = CONVERT(CONCAT(':', SUBSTRING_INDEX(:vCode, '/', 1)) USING utf8mb4) COLLATE utf8mb4_unicode_ci OR 
             CONVERT(ph.vendor_code USING utf8mb4) COLLATE utf8mb4_unicode_ci IN (SELECT CONVERT(rvp.vendor_code USING utf8mb4) COLLATE utf8mb4_unicode_ci FROM rail_vendor_plant rvp WHERE CONVERT(rvp.plant_id USING utf8mb4) COLLATE utf8mb4_unicode_ci = CONVERT(:vCode USING utf8mb4) COLLATE utf8mb4_unicode_ci OR CONVERT(rvp.vendor_code USING utf8mb4) COLLATE utf8mb4_unicode_ci = CONVERT(:vCode USING utf8mb4) COLLATE utf8mb4_unicode_ci OR CONVERT(rvp.company_name USING utf8mb4) COLLATE utf8mb4_unicode_ci LIKE CONCAT('%', CONVERT(:vCode USING utf8mb4) COLLATE utf8mb4_unicode_ci, '%') OR CONVERT(rvp.plant_name USING utf8mb4) COLLATE utf8mb4_unicode_ci LIKE CONCAT('%', CONVERT(:vCode USING utf8mb4) COLLATE utf8mb4_unicode_ci, '%')) OR 
             CONVERT(ph.vendor_code USING utf8mb4) COLLATE utf8mb4_unicode_ci IN (SELECT CONVERT(ppm.vendor_code USING utf8mb4) COLLATE utf8mb4_unicode_ci FROM railpad_pincode_poi_mapping ppm WHERE CONVERT(ppm.poi_code USING utf8mb4) COLLATE utf8mb4_unicode_ci = CONVERT(:vCode USING utf8mb4) COLLATE utf8mb4_unicode_ci OR CONVERT(ppm.company_name USING utf8mb4) COLLATE utf8mb4_unicode_ci LIKE CONCAT('%', CONVERT(:vCode USING utf8mb4) COLLATE utf8mb4_unicode_ci, '%')) OR 
             CONVERT(ph.vendor_code USING utf8mb4) COLLATE utf8mb4_unicode_ci IN (SELECT CONVERT(ppm.vendor_code USING utf8mb4) COLLATE utf8mb4_unicode_ci FROM pincode_poi_mapping ppm WHERE CONVERT(ppm.poi_code USING utf8mb4) COLLATE utf8mb4_unicode_ci = CONVERT(:vCode USING utf8mb4) COLLATE utf8mb4_unicode_ci OR CONVERT(ppm.company_name USING utf8mb4) COLLATE utf8mb4_unicode_ci LIKE CONCAT('%', CONVERT(:vCode USING utf8mb4) COLLATE utf8mb4_unicode_ci, '%')) OR
             CONVERT(ph.vendor_code USING utf8mb4) COLLATE utf8mb4_unicode_ci IN (SELECT CONVERT(vp.vendor_code USING utf8mb4) COLLATE utf8mb4_unicode_ci FROM vendor_plant vp WHERE CONVERT(vp.company_name USING utf8mb4) COLLATE utf8mb4_unicode_ci LIKE CONCAT('%', CONVERT(:vCode USING utf8mb4) COLLATE utf8mb4_unicode_ci, '%') OR CONVERT(vp.plant_id USING utf8mb4) COLLATE utf8mb4_unicode_ci = CONVERT(:vCode USING utf8mb4) COLLATE utf8mb4_unicode_ci OR CONVERT(vp.vendor_code USING utf8mb4) COLLATE utf8mb4_unicode_ci = CONVERT(:vCode USING utf8mb4) COLLATE utf8mb4_unicode_ci) OR
             CONVERT(ph.vendor_details USING utf8mb4) COLLATE utf8mb4_unicode_ci LIKE CONCAT('%', CONVERT(:vCode USING utf8mb4) COLLATE utf8mb4_unicode_ci, '%') OR
             CONVERT(ph.firm_details USING utf8mb4) COLLATE utf8mb4_unicode_ci LIKE CONCAT('%', CONVERT(:vCode USING utf8mb4) COLLATE utf8mb4_unicode_ci, '%')
        )
        AND (:zCode IS NULL OR :zCode = '' OR CONVERT(ph.rly_short_name USING utf8mb4) COLLATE utf8mb4_unicode_ci = CONVERT(:zCode USING utf8mb4) COLLATE utf8mb4_unicode_ci OR CONVERT(ph.rly_cd USING utf8mb4) COLLATE utf8mb4_unicode_ci = CONVERT(:zCode USING utf8mb4) COLLATE utf8mb4_unicode_ci)
        AND (:startDate IS NULL OR :endDate IS NULL OR ph.po_date BETWEEN :startDate AND :endDate)
        GROUP BY ph.id, ph.po_no, ph.rly_short_name, ph.po_date, ph.vendor_details, pi.uom
        ORDER BY ph.po_date DESC
    """, nativeQuery = true)
    List<Object[]> fetchPoIssuedDetailsRaw(
        @Param("itemCatDescr") String itemCatDescr,
        @Param("vCode") String vCode,
        @Param("zCode") String zCode,
        @Param("startDate") java.time.LocalDateTime startDate,
        @Param("endDate") java.time.LocalDateTime endDate
    );

    @Query(value = """
        SELECT 
            (CASE WHEN ic.po_no LIKE '%/%' THEN SUBSTRING_INDEX(ic.po_no, '/', 1) ELSE ic.po_no END) AS poNo,
            SUM(COALESCE(r.accepted_qty, 0)) AS totalAccepted
        FROM rail_final_inspection_lot_results r
        JOIN rail_inspection_call ic ON CONVERT(r.call_no USING utf8mb4) COLLATE utf8mb4_unicode_ci = CONVERT(ic.call_no USING utf8mb4) COLLATE utf8mb4_unicode_ci
        WHERE (CASE WHEN ic.po_no LIKE '%/%' THEN SUBSTRING_INDEX(ic.po_no, '/', 1) ELSE ic.po_no END) IN (:poNos)
        GROUP BY (CASE WHEN ic.po_no LIKE '%/%' THEN SUBSTRING_INDEX(ic.po_no, '/', 1) ELSE ic.po_no END)
    """, nativeQuery = true)
    List<Object[]> findRailpadAcceptedQtyByPoNos(@Param("poNos") List<String> poNos);

    @Query(value = """
        SELECT 
            (CASE WHEN f.po_no LIKE '%/%' THEN SUBSTRING_INDEX(f.po_no, '/', 1) ELSE f.po_no END) AS poNo,
            SUM(COALESCE(f.qty_now_passed, 0)) AS totalAccepted
        FROM final_cumulative_results f
        WHERE (CASE WHEN f.po_no LIKE '%/%' THEN SUBSTRING_INDEX(f.po_no, '/', 1) ELSE f.po_no END) IN (:poNos)
           OR f.po_no IN (:poNos)
        GROUP BY (CASE WHEN f.po_no LIKE '%/%' THEN SUBSTRING_INDEX(f.po_no, '/', 1) ELSE f.po_no END)
    """, nativeQuery = true)
    List<Object[]> findErcAcceptedQtyByPoNos(@Param("poNos") List<String> poNos);

    @Query(value = """
        SELECT 
            (CASE WHEN sic.po_no LIKE '%/%' THEN SUBSTRING_INDEX(sic.po_no, '/', 1) ELSE sic.po_no END) AS poNo,
            SUM(COALESCE(ibs.total_accepted, 0)) AS totalAccepted
        FROM ie_batch_summary ibs
        JOIN sleeper_inspection_call sic ON CONVERT(ibs.call_no USING utf8mb4) COLLATE utf8mb4_unicode_ci = CONVERT(sic.call_no USING utf8mb4) COLLATE utf8mb4_unicode_ci
        WHERE (CASE WHEN sic.po_no LIKE '%/%' THEN SUBSTRING_INDEX(sic.po_no, '/', 1) ELSE sic.po_no END) IN (:poNos)
        GROUP BY (CASE WHEN sic.po_no LIKE '%/%' THEN SUBSTRING_INDEX(sic.po_no, '/', 1) ELSE sic.po_no END)
    """, nativeQuery = true)
    List<Object[]> findSleeperAcceptedQtyByPoNos(@Param("poNos") List<String> poNos);

    @Query(value = """
        SELECT 
            (CASE WHEN fci.rly_po_no LIKE '%/%' THEN SUBSTRING_INDEX(fci.rly_po_no, '/', 1) ELSE fci.rly_po_no END) AS poNo,
            SUM(COALESCE(fci.accepted_qty, 0)) AS totalAccepted
        FROM final_call_inspection_header fci
        WHERE (CASE WHEN fci.rly_po_no LIKE '%/%' THEN SUBSTRING_INDEX(fci.rly_po_no, '/', 1) ELSE fci.rly_po_no END) IN (:poNos)
           OR fci.rly_po_no IN (:poNos)
        GROUP BY (CASE WHEN fci.rly_po_no LIKE '%/%' THEN SUBSTRING_INDEX(fci.rly_po_no, '/', 1) ELSE fci.rly_po_no END)
    """, nativeQuery = true)
    List<Object[]> findGeneralAcceptedQtyByPoNos(@Param("poNos") List<String> poNos);

    List<PoItem> findByPoHeader(PoHeader poHeader);

    Optional<PoItem> findByPoHeaderAndItemSrNo(
            PoHeader poHeader,
            String itemSrNo);
}
