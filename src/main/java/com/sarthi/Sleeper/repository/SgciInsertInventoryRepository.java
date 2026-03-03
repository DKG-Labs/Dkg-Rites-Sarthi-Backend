package com.sarthi.Sleeper.repository;

import com.sarthi.Sleeper.entity.SgciInsertInventory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SgciInsertInventoryRepository extends JpaRepository<SgciInsertInventory, Long> {
}
