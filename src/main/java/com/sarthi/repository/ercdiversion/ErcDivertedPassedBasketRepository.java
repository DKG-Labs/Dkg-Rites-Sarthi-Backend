package com.sarthi.repository.ercdiversion;

import com.sarthi.entity.ercdiversion.ErcDivertedPassedBasket;
import com.sarthi.enums.BasketStatus;
import com.sarthi.enums.DiversionStage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ErcDivertedPassedBasketRepository extends JpaRepository<ErcDivertedPassedBasket, Long> {

    List<ErcDivertedPassedBasket> findByVendorCodeAndStageAndStatus(String vendorCode, DiversionStage stage, BasketStatus status);

    @Query("SELECT b FROM ErcDivertedPassedBasket b " +
           "WHERE b.vendorCode = :vendorCode " +
           "AND b.stage = :stage " +
           "AND b.targetPoNo = :targetPoNo " +
           "AND b.targetPoSrNo = :targetPoSrNo " +
           "AND b.availableBalanceQty > 0 " +
           "AND b.status = 'AVAILABLE'")
    List<ErcDivertedPassedBasket> findAvailableBasketItems(
            @Param("vendorCode") String vendorCode,
            @Param("stage") DiversionStage stage,
            @Param("targetPoNo") String targetPoNo,
            @Param("targetPoSrNo") String targetPoSrNo
    );

    List<ErcDivertedPassedBasket> findByDiversionRequestId(Long diversionRequestId);
}
