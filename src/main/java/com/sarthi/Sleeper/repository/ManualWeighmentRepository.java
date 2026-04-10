package com.sarthi.Sleeper.repository;

import com.sarthi.Sleeper.entity.BatchWeighment.ManualWeighment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org. springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ManualWeighmentRepository extends JpaRepository<ManualWeighment, Long> {
 //   List<ManualWeighment> findByBatchIds(List<Long> ids);

    @Query("SELECT m FROM ManualWeighment m WHERE m.batchWeighment.id IN :ids")
    List<ManualWeighment> findByBatchIds(List<Long> ids);
}
