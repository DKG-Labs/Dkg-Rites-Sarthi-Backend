package com.sarthi.Sleeper.repository;

import com.sarthi.Sleeper.entity.AggregatesInventory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AggregatesInventoryRepository extends JpaRepository<AggregatesInventory, Long> {
}
