package com.sarthi.Sleeper.repository;

import com.sarthi.Sleeper.entity.AdmixtureInventory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AdmixtureInventoryRepository extends JpaRepository<AdmixtureInventory, Long> {
}
