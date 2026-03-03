package com.sarthi.Sleeper.repository;

import com.sarthi.Sleeper.entity.Cement.CementReceipt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CementReceiptRepository extends JpaRepository<CementReceipt, Long> {
}
