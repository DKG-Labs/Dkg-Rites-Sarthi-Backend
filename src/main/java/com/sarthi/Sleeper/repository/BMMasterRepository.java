package com.sarthi.Sleeper.repository;

import com.sarthi.Sleeper.entity.BenchMouldLongAndStress.BMMaster;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BMMasterRepository extends JpaRepository<BMMaster, Long> {
    BMMaster findById(BMMaster bmMaster);
}
