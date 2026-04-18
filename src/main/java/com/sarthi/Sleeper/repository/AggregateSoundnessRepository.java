package com.sarthi.Sleeper.repository;

import com.sarthi.Sleeper.entity.Aggregate.AggregateSoundnessTest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.List;

@Repository
public interface AggregateSoundnessRepository extends JpaRepository<AggregateSoundnessTest, Long> {
    Optional<AggregateSoundnessTest> findByRequestId(Long requestId);
    List<AggregateSoundnessTest> findAllByTypeOfTesting(String typeOfTesting);
}
