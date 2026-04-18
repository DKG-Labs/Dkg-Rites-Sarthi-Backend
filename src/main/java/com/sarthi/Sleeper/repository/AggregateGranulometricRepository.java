package com.sarthi.Sleeper.repository;

import com.sarthi.Sleeper.entity.Aggregate.AggregateGranulometricTest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.List;

@Repository
public interface AggregateGranulometricRepository extends JpaRepository<AggregateGranulometricTest, Long> {
    Optional<AggregateGranulometricTest> findByRequestId(Long requestId);
    List<AggregateGranulometricTest> findAllByTypeOfTesting(String typeOfTesting);
}
