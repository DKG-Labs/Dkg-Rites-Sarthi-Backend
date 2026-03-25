package com.sarthi.Sleeper.repository;

import com.sarthi.Sleeper.entity.BenchMouldLongAndStress.BMStressDetails;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BMStressDetailsRepository extends JpaRepository<BMStressDetails, Long> {
    List<BMStressDetails> findByBmMasterId(Long id);

    void deleteByBmMasterId(Long id);

    @Query("SELECT b FROM BMStressDetails b WHERE b.benchNumber IN :benchNumbers")
    List<BMStressDetails> findByBenchNumbers(@Param("benchNumbers") List<Integer> benchNumbers);
}
