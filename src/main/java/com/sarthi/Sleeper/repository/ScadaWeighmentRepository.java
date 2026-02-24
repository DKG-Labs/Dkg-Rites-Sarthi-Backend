package com.sarthi.Sleeper.repository;

import com.sarthi.Sleeper.entity.BatchWeighment.ScadaWeighment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ScadaWeighmentRepository extends JpaRepository<ScadaWeighment, Long> {
}
