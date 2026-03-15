package com.sarthi.Sleeper.repository;

import com.sarthi.Sleeper.entity.Aggregate.AggregateSoundnessTest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AggregateSoundnessRepository extends JpaRepository<AggregateSoundnessTest, Long> {
}
