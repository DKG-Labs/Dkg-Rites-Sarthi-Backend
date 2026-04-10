package com.sarthi.Sleeper.repository;

import com.sarthi.Sleeper.entity.BatchWeighment.ScadaWeighment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ScadaWeighmentRepository extends JpaRepository<ScadaWeighment, Long> {
    @Query("SELECT s FROM ScadaWeighment s WHERE s.batchWeighment.id IN :ids")
    List<ScadaWeighment> findByBatchIds(List<Long> ids);
}
