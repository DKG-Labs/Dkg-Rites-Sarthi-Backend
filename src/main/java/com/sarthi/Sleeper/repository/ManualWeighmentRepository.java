package com.sarthi.Sleeper.repository;

import com.sarthi.Sleeper.entity.BatchWeighment.ManualWeighment;
import org.springframework.data.jpa.repository.JpaRepository;
import org. springframework.stereotype.Repository;

@Repository
public interface ManualWeighmentRepository extends JpaRepository<ManualWeighment, Long> {
}
