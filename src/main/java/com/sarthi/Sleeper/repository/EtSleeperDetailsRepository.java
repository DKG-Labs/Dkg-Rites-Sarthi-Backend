package com.sarthi.Sleeper.repository;

import com.sarthi.Sleeper.entity.EtSleeperDetails;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EtSleeperDetailsRepository extends JpaRepository<EtSleeperDetails, Long> {
    List<EtSleeperDetails> findByEt_BatchNumber(String batchNumber);

    @org.springframework.data.jpa.repository.Query("SELECT e FROM EtSleeperDetails e WHERE e.et.batchNumber IN :batchNumbers")
    List<EtSleeperDetails> findByEt_BatchNumberIn(@org.springframework.data.repository.query.Param("batchNumbers") java.util.Collection<String> batchNumbers);
}
