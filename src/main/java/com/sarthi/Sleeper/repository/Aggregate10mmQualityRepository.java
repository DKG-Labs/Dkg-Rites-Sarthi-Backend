package com.sarthi.Sleeper.repository;

import com.sarthi.Sleeper.entity.Aggregate.Aggregate10mmQuality;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface Aggregate10mmQualityRepository extends JpaRepository<Aggregate10mmQuality, Long> {
}
