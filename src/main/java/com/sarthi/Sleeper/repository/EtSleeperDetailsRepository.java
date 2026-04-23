package com.sarthi.Sleeper.repository;

import com.sarthi.Sleeper.entity.EtSleeperDetails;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EtSleeperDetailsRepository extends JpaRepository<EtSleeperDetails, Long> {
    List<EtSleeperDetails> findByEt_BatchNumber(String batchNumber);
}
