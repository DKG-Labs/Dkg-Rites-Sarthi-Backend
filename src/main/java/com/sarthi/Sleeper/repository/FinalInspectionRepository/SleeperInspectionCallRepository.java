package com.sarthi.Sleeper.repository.FinalInspectionRepository;

import com.sarthi.Sleeper.dto.SleeperDashboardDtos.Level1Projection;
import com.sarthi.Sleeper.entity.FinalInspection.SleeperInspectionCall;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface SleeperInspectionCallRepository extends JpaRepository<SleeperInspectionCall, Long> {
    long countByCreatedAtBetween(LocalDateTime start, LocalDateTime end);
    List<SleeperInspectionCall> findByCreatedBy(Long createdBy);

    Optional<SleeperInspectionCall> findByCallNo(String callNo);

    @Query("""
SELECT DISTINCT s.sleeperId
FROM SleeperInspectionCallBatch b
JOIN b.goodSleepers s
""")
    List<Long> findAllGoodSleeperIds();

    @Query("""
SELECT DISTINCT s.sleeperId
FROM SleeperInspectionCallBatch b
JOIN b.badSleepers s
""")
    List<Long> findAllBadSleeperIds();

    @Query("""
    SELECT c 
    FROM SleeperInspectionCall c
    WHERE c.poNo = :poNo
      AND c.srNo = :srNo
""")
    List<SleeperInspectionCall> getCalls(String poNo, String srNo);


    @Query(value = """

SELECT 
    ph.rly_short_name AS rly,
    ph.po_no AS poNo,
    ph.po_date AS poDate,
    ph.firm_details AS vendor,
    ph.region_code AS region,

    -- 🔹 PO QTY
    (SELECT SUM(pi.qty) 
     FROM po_item pi 
     WHERE pi.po_header_id = ph.id) AS poQty,

    -- 🔹 ACCEPTED
    (SELECT COALESCE(SUM(ibs.total_accepted),0)
     FROM ie_batch_summary ibs
     JOIN sleeper_inspection_call sic 
          ON ibs.call_no = sic.call_no
     WHERE sic.po_no = ph.po_no) AS accQty,

    -- 🔹 REJECTED
    (SELECT COALESCE(SUM(ibs.total_rejected),0)
     FROM ie_batch_summary ibs
     JOIN sleeper_inspection_call sic 
          ON ibs.call_no = sic.call_no
     WHERE sic.po_no = ph.po_no) AS totalRejected,

    -- 🔹 OFFERED
    (SELECT COALESCE(SUM(ibs.total_offered),0)
     FROM ie_batch_summary ibs
     JOIN sleeper_inspection_call sic 
          ON ibs.call_no = sic.call_no
     WHERE sic.po_no = ph.po_no) AS totalOffered

FROM po_header ph

WHERE ph.item_cat_descr = 'PSC Mainline Sleeper'
  AND DATE(ph.po_date) BETWEEN :startDate AND :endDate

ORDER BY ph.po_date DESC

""", nativeQuery = true)
    List<Level1Projection> getLevel1Data(LocalDate startDate, LocalDate endDate);

}
