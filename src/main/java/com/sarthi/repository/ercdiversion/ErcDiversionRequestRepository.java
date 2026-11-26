package com.sarthi.repository.ercdiversion;

import com.sarthi.entity.ercdiversion.ErcDiversionRequest;
import com.sarthi.enums.DiversionRequestStatus;
import com.sarthi.enums.DiversionStage;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ErcDiversionRequestRepository extends JpaRepository<ErcDiversionRequest, Long> {

    @EntityGraph(attributePaths = {"items"})
    Optional<ErcDiversionRequest> findByRequestNo(String requestNo);

    /** Batch version for list screens (one query instead of one per call). */
    List<ErcDiversionRequest> findByRequestNoIn(java.util.Collection<String> requestNos);

    @EntityGraph(attributePaths = {"items"})
    List<ErcDiversionRequest> findByVendorCodeOrderByCreatedDateDesc(String vendorCode);

    @Query("SELECT r.id, r.requestNo, r.vendorCode, r.companyId, r.companyName, r.plantId, r.plantName, " +
           "r.rioId, r.cmUserId, r.stage, r.diversionType, r.sourcePoNo, r.sourcePoSrNo, r.sourceIcNo, " +
           "r.sourceCallNo, r.sourceIcDate, r.targetPoNo, r.targetPoSrNo, r.totalDiversionQty, " +
           "r.unitOfMeasurement, r.reasonCode, r.reasonRemarks, r.railwayPermissionNo, " +
           "r.railwayPermissionDate, r.status, r.createdDate, r.createdBy, " +
           "r.diversionNo, r.sourceIeName, r.sourceIeUserId " +
           "FROM ErcDiversionRequest r WHERE r.cmUserId = :cmUserId AND r.status IN :statuses ORDER BY r.createdDate DESC")
    List<Object[]> findByCmUserIdAndStatusInLight(@Param("cmUserId") String cmUserId, @Param("statuses") List<DiversionRequestStatus> statuses);

    @Query("SELECT r.id, r.requestNo, r.vendorCode, r.companyId, r.companyName, r.plantId, r.plantName, " +
           "r.rioId, r.cmUserId, r.stage, r.diversionType, r.sourcePoNo, r.sourcePoSrNo, r.sourceIcNo, " +
           "r.sourceCallNo, r.sourceIcDate, r.targetPoNo, r.targetPoSrNo, r.totalDiversionQty, " +
           "r.unitOfMeasurement, r.reasonCode, r.reasonRemarks, r.railwayPermissionNo, " +
           "r.railwayPermissionDate, r.status, r.createdDate, r.createdBy, " +
           "r.diversionNo, r.sourceIeName, r.sourceIeUserId " +
           "FROM ErcDiversionRequest r WHERE r.rioId = :rioId AND r.status IN :statuses ORDER BY r.createdDate DESC")
    List<Object[]> findByRioIdAndStatusInLight(@Param("rioId") String rioId, @Param("statuses") List<DiversionRequestStatus> statuses);

    @EntityGraph(attributePaths = {"items"})
    List<ErcDiversionRequest> findByCmUserIdAndStatusInOrderByCreatedDateDesc(String cmUserId, List<DiversionRequestStatus> statuses);

    @EntityGraph(attributePaths = {"items"})
    List<ErcDiversionRequest> findByRioIdAndStatusInOrderByCreatedDateDesc(String rioId, List<DiversionRequestStatus> statuses);

    @Query("SELECT r.id, r.requestNo, r.vendorCode, r.companyId, r.companyName, r.plantId, r.plantName, " +
           "r.rioId, r.cmUserId, r.stage, r.diversionType, r.sourcePoNo, r.sourcePoSrNo, r.sourceIcNo, " +
           "r.sourceCallNo, r.sourceIcDate, r.targetPoNo, r.targetPoSrNo, r.totalDiversionQty, " +
           "r.unitOfMeasurement, r.reasonCode, r.reasonRemarks, r.railwayPermissionNo, " +
           "r.railwayPermissionDate, r.status, r.createdDate, r.createdBy, " +
           "r.diversionNo, r.sourceIeName, r.sourceIeUserId " +
           "FROM ErcDiversionRequest r WHERE r.vendorCode = :vendorCode AND r.status != com.sarthi.enums.DiversionRequestStatus.DELETED_BY_VENDOR AND (:status IS NULL OR r.status = :status) ORDER BY r.createdDate DESC")
    List<Object[]> findByVendorAndOptionalStatusLight(@Param("vendorCode") String vendorCode, @Param("status") DiversionRequestStatus status);

    @EntityGraph(attributePaths = {"items"})
    @Query("SELECT r FROM ErcDiversionRequest r WHERE r.vendorCode = :vendorCode AND (:status IS NULL OR r.status = :status) ORDER BY r.createdDate DESC")
    List<ErcDiversionRequest> findByVendorAndOptionalStatus(@Param("vendorCode") String vendorCode, @Param("status") DiversionRequestStatus status);

    @Query("SELECT COUNT(r) FROM ErcDiversionRequest r WHERE r.stage = :stage AND YEAR(r.createdDate) = :year")
    long countByStageAndYear(@Param("stage") DiversionStage stage, @Param("year") int year);

    @Query("SELECT COUNT(r) FROM ErcDiversionRequest r WHERE r.stage = :stage AND CAST(r.createdDate AS date) = :date")
    long countByStageAndCreatedDate(@Param("stage") DiversionStage stage, @Param("date") java.time.LocalDate date);

    boolean existsByRequestNo(String requestNo);

    boolean existsByDiversionNo(String diversionNo);

    @Query("SELECT COUNT(r) FROM ErcDiversionRequest r WHERE r.stage = :stage")
    long countByStage(@Param("stage") DiversionStage stage);

    @Query("SELECT DISTINCT r.sourceIcNo FROM ErcDiversionRequest r " +
           "WHERE r.sourceIcNo IN :icNumbers " +
           "AND r.status NOT IN (" +
           "com.sarthi.enums.DiversionRequestStatus.DRAFT, " +
           "com.sarthi.enums.DiversionRequestStatus.CM_RETURN_DIVERSION, " +
           "com.sarthi.enums.DiversionRequestStatus.SBU_HEAD_RETURN_DIVERSION, " +
           "com.sarthi.enums.DiversionRequestStatus.RETURNED_TO_VENDOR, " +
           "com.sarthi.enums.DiversionRequestStatus.DELETED_BY_VENDOR)")
    List<String> findDivertedSourceIcNos(@Param("icNumbers") List<String> icNumbers);

    /**
     * Source ICs whose material was diverted COMPLETELY (same statuses as above). A PARTIAL diversion leaves
     * material on the source PO serial, so its source IC must stay available for fresh Process calls.
     */
    @Query("SELECT DISTINCT r.sourceIcNo FROM ErcDiversionRequest r " +
           "WHERE r.sourceIcNo IN :icNumbers " +
           "AND r.diversionType = com.sarthi.enums.DiversionType.COMPLETE " +
           "AND r.status NOT IN (" +
           "com.sarthi.enums.DiversionRequestStatus.DRAFT, " +
           "com.sarthi.enums.DiversionRequestStatus.CM_RETURN_DIVERSION, " +
           "com.sarthi.enums.DiversionRequestStatus.SBU_HEAD_RETURN_DIVERSION, " +
           "com.sarthi.enums.DiversionRequestStatus.RETURNED_TO_VENDOR, " +
           "com.sarthi.enums.DiversionRequestStatus.DELETED_BY_VENDOR)")
    List<String> findCompletelyDivertedSourceIcNos(@Param("icNumbers") List<String> icNumbers);

    /**
     * Quantity diverted out of a source RM call, per heat: [UPPER(heat_no), SUM(diversion_qty)].
     * Same statuses as above (returned / draft / deleted requests do not count). One query per call.
     */
    @Query(value = "SELECT UPPER(TRIM(i.heat_no)) AS heat, SUM(COALESCE(i.diversion_qty, 0)) AS qty " +
           "FROM erc_material_diversion_item i " +
           "JOIN erc_material_diversion_request r ON r.id = i.diversion_request_id " +
           "WHERE (r.source_call_no = :callNo OR r.source_ic_no = :callNo " +
           "       OR r.source_ic_no LIKE CONCAT('%/', :callNo, '/%')) " +
           "AND r.status NOT IN ('DRAFT', 'CM_RETURN_DIVERSION', 'SBU_HEAD_RETURN_DIVERSION', " +
           "                     'RETURNED_TO_VENDOR', 'DELETED_BY_VENDOR') " +
           "AND i.heat_no IS NOT NULL " +
           "GROUP BY UPPER(TRIM(i.heat_no))", nativeQuery = true)
    List<Object[]> sumDivertedQtyByHeatForSourceCall(@Param("callNo") String callNo);

    @Query("SELECT r.railwayPermissionDocUrl FROM ErcDiversionRequest r WHERE r.id = :id")
    Optional<String> findRailwayDocUrlById(@Param("id") Long id);

    @Query("SELECT DISTINCT r.sourcePoSrNo FROM ErcDiversionRequest r " +
           "WHERE r.vendorCode = :vendorCode " +
           "AND r.diversionType = com.sarthi.enums.DiversionType.COMPLETE " +
           "AND r.status NOT IN (com.sarthi.enums.DiversionRequestStatus.DRAFT, " +
           "com.sarthi.enums.DiversionRequestStatus.CM_RETURN_DIVERSION, " +
           "com.sarthi.enums.DiversionRequestStatus.SBU_HEAD_RETURN_DIVERSION)")
    List<String> findCompleteSourceSrNos(@Param("vendorCode") String vendorCode);

    @Query("SELECT DISTINCT r.sourceIcNo FROM ErcDiversionRequest r " +
           "WHERE r.vendorCode = :vendorCode " +
           "AND r.diversionType = com.sarthi.enums.DiversionType.COMPLETE " +
           "AND r.status NOT IN (com.sarthi.enums.DiversionRequestStatus.DRAFT, " +
           "com.sarthi.enums.DiversionRequestStatus.CM_RETURN_DIVERSION, " +
           "com.sarthi.enums.DiversionRequestStatus.SBU_HEAD_RETURN_DIVERSION, " +
           "com.sarthi.enums.DiversionRequestStatus.RETURNED_TO_VENDOR, " +
           "com.sarthi.enums.DiversionRequestStatus.DELETED_BY_VENDOR)")
    List<String> findCompleteDivertedSourceIcNos(@Param("vendorCode") String vendorCode);
}
