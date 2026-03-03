package com.sarthi.Sleeper.repository;

import com.sarthi.Sleeper.entity.VendorHtsWire.HtsWire;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface HtsWireRepository extends JpaRepository<HtsWire, Long> {
}
