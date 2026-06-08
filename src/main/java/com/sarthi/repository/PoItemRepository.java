package com.sarthi.repository;

import com.sarthi.dto.PoInspection2ndLevelSerialStatusDto;
import com.sarthi.entity.PoItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

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

    @Query("SELECT SUM(pi.qty) FROM PoItem pi JOIN pi.poHeader ph WHERE ph.itemCatDescr = :itemCatDescr AND pi.uom = 'Set'")
    Long sumQtyByItemCatDescrAndUomSet(@Param("itemCatDescr") String itemCatDescr);

    @Query("SELECT SUM(pi.qty) FROM PoItem pi JOIN pi.poHeader ph WHERE ph.itemCatDescr = :itemCatDescr AND pi.uom IN ('Mt', 'Mts', 'Mts.')")
    Double sumQtyByItemCatDescrAndUomMt(@Param("itemCatDescr") String itemCatDescr);

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
        GROUP BY ph.poNo
    """)
    List<com.sarthi.dto.reports.PoIssuedDetailDto> getPoIssuedDetails(@Param("itemCatDescr") String itemCatDescr);
}
