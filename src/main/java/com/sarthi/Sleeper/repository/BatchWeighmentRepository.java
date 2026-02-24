package com.sarthi.Sleeper.repository;

import com.sarthi.Sleeper.entity.BatchWeighment.BatchWeighment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BatchWeighmentRepository extends JpaRepository<BatchWeighment, Long> {
}
