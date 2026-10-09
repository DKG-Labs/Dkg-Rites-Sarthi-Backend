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

    @EntityGraph(attributePaths = {"items"})
    List<ErcDiversionRequest> findByCmUserIdAndStatusInOrderByCreatedDateDesc(String cmUserId, List<DiversionRequestStatus> statuses);

    @EntityGraph(attributePaths = {"items"})
    List<ErcDiversionRequest> findByRioIdAndStatusInOrderByCreatedDateDesc(String rioId, List<DiversionRequestStatus> statuses);

    @EntityGraph(attributePaths = {"items"})
    @Query("SELECT r FROM ErcDiversionRequest r WHERE r.vendorCode = :vendorCode AND (:status IS NULL OR r.status = :status) ORDER BY r.createdDate DESC")
    List<ErcDiversionRequest> findByVendorAndOptionalStatus(@Param("vendorCode") String vendorCode, @Param("status") DiversionRequestStatus status);

    @Query("SELECT COUNT(r) FROM ErcDiversionRequest r WHERE r.stage = :stage AND YEAR(r.createdDate) = :year")
    long countByStageAndYear(@Param("stage") DiversionStage stage, @Param("year") int year);
}
