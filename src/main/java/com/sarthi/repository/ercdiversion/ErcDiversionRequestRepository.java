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

    @Query("SELECT r.railwayPermissionDocUrl FROM ErcDiversionRequest r WHERE r.id = :id")
    Optional<String> findRailwayDocUrlById(@Param("id") Long id);

    @Query("SELECT DISTINCT r.sourcePoSrNo FROM ErcDiversionRequest r " +
           "WHERE r.vendorCode = :vendorCode " +
           "AND r.diversionType = com.sarthi.enums.DiversionType.COMPLETE " +
           "AND r.status NOT IN (com.sarthi.enums.DiversionRequestStatus.DRAFT, " +
           "com.sarthi.enums.DiversionRequestStatus.CM_RETURN_DIVERSION, " +
           "com.sarthi.enums.DiversionRequestStatus.SBU_HEAD_RETURN_DIVERSION)")
    List<String> findCompleteSourceSrNos(@Param("vendorCode") String vendorCode);
}
