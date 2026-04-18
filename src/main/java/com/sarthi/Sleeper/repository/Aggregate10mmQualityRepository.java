package com.sarthi.Sleeper.repository;

import com.sarthi.Sleeper.entity.Aggregate.Aggregate10mmQuality;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.List;

@Repository
public interface Aggregate10mmQualityRepository extends JpaRepository<Aggregate10mmQuality, Long> {
    Optional<Aggregate10mmQuality> findByRequestId(Long requestId);
    List<Aggregate10mmQuality> findAllByTypeOfTesting(String typeOfTesting);
}
