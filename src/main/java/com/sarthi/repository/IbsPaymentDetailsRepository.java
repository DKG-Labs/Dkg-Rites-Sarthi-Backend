package com.sarthi.repository;

import com.sarthi.entity.IBS.IbsPaymentDetails;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IbsPaymentDetailsRepository extends JpaRepository<IbsPaymentDetails, Long> {
    boolean existsByMerTxnId(String merTxnId);
}
