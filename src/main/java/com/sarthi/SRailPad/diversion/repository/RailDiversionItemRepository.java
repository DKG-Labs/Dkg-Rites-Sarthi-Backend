package com.sarthi.SRailPad.diversion.repository;

import com.sarthi.SRailPad.diversion.entity.RailDiversionItem;
import com.sarthi.enums.DiversionRequestStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface RailDiversionItemRepository extends JpaRepository<RailDiversionItem, Long> {

    /**
     * Qty already diverted from a source IC, per lot / batch, by active requests (exact IC match, not LIKE).
     * Row: [lotNo, batchNo, sumDiversionQty]. excludeRequestId skips the request being edited.
     */
    @Query("""
           SELECT i.lotNo, i.batchNo, COALESCE(SUM(i.diversionQty), 0)
           FROM RailDiversionItem i JOIN i.diversionRequest r
           WHERE r.sourceIcNo = :sourceIcNo AND r.status IN :statuses
             AND (:excludeRequestId IS NULL OR r.id <> :excludeRequestId)
           GROUP BY i.lotNo, i.batchNo
           """)
    List<Object[]> sumDivertedBySourceIc(@Param("sourceIcNo") String sourceIcNo,
                                         @Param("statuses") Collection<DiversionRequestStatus> statuses,
                                         @Param("excludeRequestId") Long excludeRequestId);

    /** Qty diverted from a source call per batch by active requests. Row: [batchNo, sum]. */
    @Query("""
           SELECT i.batchNo, COALESCE(SUM(i.diversionQty), 0)
           FROM RailDiversionItem i JOIN i.diversionRequest r
           WHERE r.sourceCallNo = :sourceCallNo AND r.status IN :statuses AND i.batchNo IS NOT NULL
           GROUP BY i.batchNo
           """)
    List<Object[]> sumDivertedByBatchForSourceCall(@Param("sourceCallNo") String sourceCallNo,
                                                   @Param("statuses") Collection<DiversionRequestStatus> statuses);

    /** Total diverted per source IC for a vendor (bulk, for the eligible list). Row: [sourceIcNo, sum]. */
    @Query("""
           SELECT r.sourceIcNo, COALESCE(SUM(i.diversionQty), 0)
           FROM RailDiversionItem i JOIN i.diversionRequest r
           WHERE r.vendorCode = :vendorCode AND r.status IN :statuses
           GROUP BY r.sourceIcNo
           """)
    List<Object[]> sumDivertedByVendor(@Param("vendorCode") String vendorCode,
                                       @Param("statuses") Collection<DiversionRequestStatus> statuses);
}
