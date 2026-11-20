package com.sarthi.SRailPad.diversion.repository;

import com.sarthi.SRailPad.diversion.entity.RailDivertedPassedBasket;
import com.sarthi.enums.BasketStatus;
import com.sarthi.enums.DiversionStage;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface RailDivertedPassedBasketRepository extends JpaRepository<RailDivertedPassedBasket, Long> {

    List<RailDivertedPassedBasket> findByVendorCodeOrderByCreatedDateDesc(String vendorCode);

    List<RailDivertedPassedBasket> findByDiversionRequestId(Long requestId);

    List<RailDivertedPassedBasket> findByVendorCodeAndTargetPoNoAndTargetPoSrNoAndStageAndStatusIn(
            String vendorCode, String targetPoNo, String targetPoSrNo, DiversionStage stage, Collection<BasketStatus> statuses);

    /** Basket rows behind one diverted-passed call, locked for reserve / release. */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT b FROM RailDivertedPassedBasket b WHERE b.divertedCallNo = :callNo ORDER BY b.id")
    List<RailDivertedPassedBasket> lockByDivertedCallNo(@Param("callNo") String callNo);

    @Query("SELECT b FROM RailDivertedPassedBasket b WHERE b.divertedCallNo IN :callNos")
    List<RailDivertedPassedBasket> findByDivertedCallNoIn(@Param("callNos") Collection<String> callNos);
}
