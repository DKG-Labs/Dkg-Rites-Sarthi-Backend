package com.sarthi.Sleeper.repository;

import com.sarthi.Sleeper.entity.DowelInventory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DowelInventoryRepository extends JpaRepository<DowelInventory, Long> {
}
