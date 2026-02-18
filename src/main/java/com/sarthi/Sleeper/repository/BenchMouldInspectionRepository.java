package com.sarthi.Sleeper.repository;

import com.sarthi.Sleeper.entity.BenchMouldInspection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BenchMouldInspectionRepository extends JpaRepository<BenchMouldInspection, Long> {


}
