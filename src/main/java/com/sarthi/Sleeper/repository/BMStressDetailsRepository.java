package com.sarthi.Sleeper.repository;

import com.sarthi.Sleeper.entity.BenchMouldLongAndStress.BMStressDetails;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BMStressDetailsRepository extends JpaRepository<BMStressDetails, Long> {
    List<BMStressDetails> findByBmMasterId(Long id);

    void deleteByBmMasterId(Long id);
}
