package com.sarthi.Sleeper.repository;

import com.sarthi.Sleeper.entity.Aggregate.AggregateGranulometricTest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AggregateGranulometricRepository extends JpaRepository<AggregateGranulometricTest, Long> {
    Optional<AggregateGranulometricTest> findByRequestId(Long requestId);
}
