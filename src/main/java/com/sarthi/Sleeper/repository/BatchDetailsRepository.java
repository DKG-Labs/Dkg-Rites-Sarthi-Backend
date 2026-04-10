package com.sarthi.Sleeper.repository;

import com.sarthi.Sleeper.entity.BatchWeighment.BatchDetails;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BatchDetailsRepository extends JpaRepository<BatchDetails, Long> {
   // List<BatchDetails> findByBatchIds(List<Long> ids);

    @Query("SELECT bd FROM BatchDetails bd WHERE bd.batchWeighment.id IN :ids")
    List<BatchDetails> findByBatchIds(List<Long> ids);
}
