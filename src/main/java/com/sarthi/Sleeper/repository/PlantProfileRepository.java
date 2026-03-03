package com.sarthi.Sleeper.repository;

import com.sarthi.Sleeper.entity.PlantProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PlantProfileRepository extends JpaRepository<PlantProfile, Long> {
}
