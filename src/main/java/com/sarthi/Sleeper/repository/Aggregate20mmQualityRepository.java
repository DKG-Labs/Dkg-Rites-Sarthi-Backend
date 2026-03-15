package com.sarthi.Sleeper.repository;

import com.sarthi.Sleeper.entity.Aggregate.Aggregate20mmQuality;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface Aggregate20mmQualityRepository extends JpaRepository<Aggregate20mmQuality, Long> {
}
