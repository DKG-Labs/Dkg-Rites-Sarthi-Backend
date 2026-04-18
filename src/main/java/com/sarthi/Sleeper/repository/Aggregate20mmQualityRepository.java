package com.sarthi.Sleeper.repository;

import com.sarthi.Sleeper.entity.Aggregate.Aggregate20mmQuality;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface Aggregate20mmQualityRepository extends JpaRepository<Aggregate20mmQuality, Long> {
    Optional<Aggregate20mmQuality> findByRequestId(Long requestId);
    List<Aggregate20mmQuality> findAllByTypeOfTesting(String typeOfTesting);
}
