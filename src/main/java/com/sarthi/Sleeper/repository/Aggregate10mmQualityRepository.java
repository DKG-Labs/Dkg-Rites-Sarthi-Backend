package com.sarthi.Sleeper.repository;

import com.sarthi.Sleeper.entity.Aggregate.Aggregate10mmQuality;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface Aggregate10mmQualityRepository extends JpaRepository<Aggregate10mmQuality, Long> {
    Optional<Aggregate10mmQuality> findByRequestId(Long requestId);
}
