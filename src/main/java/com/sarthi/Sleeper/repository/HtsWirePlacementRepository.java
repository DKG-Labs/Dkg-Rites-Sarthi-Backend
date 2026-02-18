package com.sarthi.Sleeper.repository;


import com.sarthi.Sleeper.entity.HtsWirePlacement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface HtsWirePlacementRepository extends JpaRepository<HtsWirePlacement, Long> {


}
