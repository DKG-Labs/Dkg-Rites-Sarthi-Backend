package com.sarthi.Sleeper.repository;

import com.sarthi.Sleeper.entity.Aggregate.AggregateFlakinessTest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AggregateFlakinessRepository extends JpaRepository<AggregateFlakinessTest, Long> {
    Optional<AggregateFlakinessTest> findByRequestId(Long requestId);
}
