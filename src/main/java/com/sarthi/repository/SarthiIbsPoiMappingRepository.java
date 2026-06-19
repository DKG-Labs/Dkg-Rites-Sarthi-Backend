package com.sarthi.repository;

import com.sarthi.entity.IBS.SarthiIbsPoiMapping;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SarthiIbsPoiMappingRepository extends JpaRepository<SarthiIbsPoiMapping, Long> {
}
