package com.sarthi.repository.ercdiversion;

import com.sarthi.entity.ercdiversion.ErcDiversionItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface ErcDiversionItemRepository extends JpaRepository<ErcDiversionItem, Long> {

    List<ErcDiversionItem> findByDiversionRequestId(Long diversionRequestId);

    @Query("SELECT COALESCE(SUM(i.diversionQty), 0) FROM ErcDiversionItem i " +
           "JOIN i.diversionRequest r " +
           "WHERE r.sourceIcNo = :sourceIcNo " +
           "AND (:heatNo IS NULL OR i.heatNo = :heatNo) " +
           "AND (:tcNo IS NULL OR i.tcNo = :tcNo) " +
           "AND (:lotNo IS NULL OR i.lotNo = :lotNo) " +
           "AND r.status NOT IN (" +
           "com.sarthi.enums.DiversionRequestStatus.DRAFT, " +
           "com.sarthi.enums.DiversionRequestStatus.CM_RETURN_DIVERSION, " +
           "com.sarthi.enums.DiversionRequestStatus.SBU_HEAD_RETURN_DIVERSION, " +
           "com.sarthi.enums.DiversionRequestStatus.RETURNED_TO_VENDOR, " +
           "com.sarthi.enums.DiversionRequestStatus.DELETED_BY_VENDOR)")
    BigDecimal sumDivertedQuantityForSource(
            @Param("sourceIcNo") String sourceIcNo,
            @Param("heatNo") String heatNo,
            @Param("tcNo") String tcNo,
            @Param("lotNo") String lotNo
    );

    @Query("SELECT r.sourceIcNo, COALESCE(SUM(i.diversionQty), 0) FROM ErcDiversionItem i " +
           "JOIN i.diversionRequest r " +
           "WHERE r.sourceIcNo IN :sourceIcNos " +
           "AND r.status NOT IN (" +
           "com.sarthi.enums.DiversionRequestStatus.DRAFT, " +
           "com.sarthi.enums.DiversionRequestStatus.CM_RETURN_DIVERSION, " +
           "com.sarthi.enums.DiversionRequestStatus.SBU_HEAD_RETURN_DIVERSION, " +
           "com.sarthi.enums.DiversionRequestStatus.RETURNED_TO_VENDOR, " +
           "com.sarthi.enums.DiversionRequestStatus.DELETED_BY_VENDOR) " +
           "GROUP BY r.sourceIcNo")
    List<Object[]> sumDivertedQuantitiesBySourceIcNos(@Param("sourceIcNos") List<String> sourceIcNos);
}
