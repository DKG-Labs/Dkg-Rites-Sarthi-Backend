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
           "AND r.status IN ('SUBMITTED_TO_CM', 'APPROVED_BY_CM', 'APPROVED_BY_SBU', 'BASKET_CREATED', 'COMPLETED')")
    BigDecimal sumDivertedQuantityForSource(
            @Param("sourceIcNo") String sourceIcNo,
            @Param("heatNo") String heatNo,
            @Param("tcNo") String tcNo,
            @Param("lotNo") String lotNo
    );

    @Query("SELECT r.sourceIcNo, COALESCE(SUM(i.diversionQty), 0) FROM ErcDiversionItem i " +
           "JOIN i.diversionRequest r " +
           "WHERE r.sourceIcNo IN :sourceIcNos " +
           "AND r.status IN ('SUBMITTED_TO_CM', 'APPROVED_BY_CM', 'APPROVED_BY_SBU', 'BASKET_CREATED', 'COMPLETED') " +
           "GROUP BY r.sourceIcNo")
    List<Object[]> sumDivertedQuantitiesBySourceIcNos(@Param("sourceIcNos") List<String> sourceIcNos);
}
