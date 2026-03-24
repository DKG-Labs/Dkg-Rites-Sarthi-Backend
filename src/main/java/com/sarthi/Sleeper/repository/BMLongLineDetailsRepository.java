package com.sarthi.Sleeper.repository;

import com.sarthi.Sleeper.entity.BenchMouldLongAndStress.BMLongLineDetails;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BMLongLineDetailsRepository extends JpaRepository<BMLongLineDetails, Long> {
    List<BMLongLineDetails> findByBmMasterId(Long id);

    void deleteByBmMasterId(Long id);
}
