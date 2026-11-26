package com.sarthi.SRailPad.diversion.repository;

import com.sarthi.SRailPad.diversion.entity.RailDiversionRequest;
import com.sarthi.enums.DiversionRequestStatus;
import com.sarthi.enums.DiversionType;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface RailDiversionRequestRepository extends JpaRepository<RailDiversionRequest, Long> {

    Optional<RailDiversionRequest> findByRequestNo(String requestNo);

    boolean existsByRequestNo(String requestNo);

    /** Lot / batch numbers of a request's items: [lotNo, batchNo]. */
    @Query("SELECT i.lotNo, i.batchNo FROM RailDiversionItem i WHERE i.diversionRequest.requestNo = :requestNo ORDER BY i.id")
    List<Object[]> findItemLotsByRequestNo(@Param("requestNo") String requestNo);

    List<RailDiversionRequest> findByVendorCodeAndStatusNotOrderByCreatedDateDesc(String vendorCode, DiversionRequestStatus status);

    List<RailDiversionRequest> findByCmUserIdAndStatusOrderByCreatedDateDesc(String cmUserId, DiversionRequestStatus status);

    List<RailDiversionRequest> findByCmUserIdAndStatusInOrderByCreatedDateDesc(String cmUserId, Collection<DiversionRequestStatus> statuses);

    List<RailDiversionRequest> findByStatusInOrderByCreatedDateDesc(Collection<DiversionRequestStatus> statuses);

    List<RailDiversionRequest> findByRioIdAndStatusInOrderByCreatedDateDesc(String rioId, Collection<DiversionRequestStatus> statuses);

    /** Locks every active request on a source IC while a new one is validated (stops parallel over-diversion). */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT r FROM RailDiversionRequest r WHERE r.sourceIcNo = :sourceIcNo AND r.status IN :statuses")
    List<RailDiversionRequest> lockActiveBySourceIc(@Param("sourceIcNo") String sourceIcNo,
                                                    @Param("statuses") Collection<DiversionRequestStatus> statuses);

    /** Source ICs that already have an active COMPLETE diversion. */
    @Query("SELECT DISTINCT r.sourceIcNo FROM RailDiversionRequest r WHERE r.diversionType = :type AND r.status IN :statuses AND r.vendorCode = :vendorCode")
    List<String> findSourceIcsWithDiversionType(@Param("vendorCode") String vendorCode,
                                                @Param("type") DiversionType type,
                                                @Param("statuses") Collection<DiversionRequestStatus> statuses);

    /**
     * Final-stage qty approved for diversion away from a source PO serial (approval creates the diverted-passed call).
     * PO serial compared without leading zeros (001 = 1).
     */
    @Query(value = """
            SELECT COALESCE(SUM(r.total_diversion_qty), 0) FROM rail_material_diversion_request r
            WHERE r.stage = 'FINAL'
              AND r.status IN ('CM_APPROVE_DIVERSION', 'SBU_HEAD_APPROVE_DIVERSION', 'BASKET_CREATED', 'COMPLETED')
              AND TRIM(r.source_po_no) = :poNo
              AND TRIM(LEADING '0' FROM TRIM(r.source_po_sr_no)) = TRIM(LEADING '0' FROM TRIM(:poSrNo))
            """, nativeQuery = true)
    java.math.BigDecimal sumApprovedFinalDivertedFromSource(@Param("poNo") String poNo, @Param("poSrNo") String poSrNo);

    /** Source PO serials that have an active COMPLETE diversion (fresh calls hide these). */
    @Query("SELECT DISTINCT r.sourcePoSrNo FROM RailDiversionRequest r WHERE r.vendorCode = :vendorCode AND r.diversionType = :type AND r.status IN :statuses")
    List<String> findCompleteSourceSrNos(@Param("vendorCode") String vendorCode,
                                         @Param("type") DiversionType type,
                                         @Param("statuses") Collection<DiversionRequestStatus> statuses);
}
