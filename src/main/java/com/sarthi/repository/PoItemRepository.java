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

    /**
     * Fetch PO items by PO header id.
     */
    List<PoItem> findByPoHeader_Id(Long poHeaderId);

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
        WHERE ph.item_cat_descr = :itemCatDescr AND pi.uom = 'Nos.'
        AND (:startDate = '' OR :endDate = '' OR ph.po_date BETWEEN :startDate AND :endDate)
        AND (:zonalRailway IS NULL OR :zonalRailway = '' OR ph.rly_short_name = :zonalRailway)
        AND (:vendorPlantCode IS NULL OR :vendorPlantCode = '' OR ph.vendor_code IN (
            SELECT ppm.vendor_code FROM pincode_poi_mapping ppm WHERE ppm.poi_code = :vendorPlantCode
        ))
    """, nativeQuery = true)
    Long sumFilteredQtyByItemCatDescrAndUomNos(
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
        WHERE ph.item_cat_descr = :itemCatDescr AND pi.uom IN ('Mt', 'Mts', 'Mts.')
        AND (:startDate = '' OR :endDate = '' OR ph.po_date BETWEEN :startDate AND :endDate)
        AND (:zonalRailway IS NULL OR :zonalRailway = '' OR ph.rly_short_name = :zonalRailway)
        AND (:vendorPlantCode IS NULL OR :vendorPlantCode = '' OR ph.vendor_code IN (
            SELECT ppm.vendor_code FROM pincode_poi_mapping ppm WHERE ppm.poi_code = :vendorPlantCode
        ))
    """, nativeQuery = true)
    Double sumFilteredQtyByItemCatDescrAndUomMt(
            @Param("itemCatDescr") String itemCatDescr,
            @Param("startDate") String startDate,
            @Param("endDate") String endDate,
            @Param("vendorPlantCode") String vendorPlantCode,
            @Param("zonalRailway") String zonalRailway);

    @Query("""
        SELECT new com.sarthi.dto.reports.PoIssuedDetailDto(
            MAX(ph.rlyShortName),
            ph.poNo,
            MAX(ph.poDate),
            MAX(ph.vendorDetails),
            SUM(pi.qty),
            MAX(pi.uom),
            (SELECT COALESCE(SUM(f.qtyNowPassed), 0L) FROM FinalCumulativeResults f WHERE f.poNo = ph.poNo),
            0L
        )
        FROM PoItem pi
        JOIN pi.poHeader ph
        WHERE ph.itemCatDescr = :itemCatDescr
        AND (:vCode = '' OR EXISTS (
            SELECT 1 FROM PincodePoIMapping map 
            WHERE ph.vendorCode = map.vendorCode 
            AND map.poiCode = :vCode
        ))
        AND (:zCode = '' OR ph.rlyShortName = :zCode)
        AND (ph.poDate BETWEEN :startDate AND :endDate)
        GROUP BY ph.poNo
    """)
    List<com.sarthi.dto.reports.PoIssuedDetailDto> getPoIssuedDetails(
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
