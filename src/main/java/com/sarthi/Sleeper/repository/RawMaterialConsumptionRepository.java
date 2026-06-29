package com.sarthi.Sleeper.repository;

import com.sarthi.Sleeper.entity.RawMaterialConsumption;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@Repository
public interface RawMaterialConsumptionRepository extends JpaRepository<RawMaterialConsumption, Long> {
    Page<RawMaterialConsumption> findByPlantIdAndRawMaterial(String plantId, String rawMaterial, Pageable pageable);
    Page<RawMaterialConsumption> findByPlantId(String plantId, Pageable pageable);
    Page<RawMaterialConsumption> findByPlantIdAndRawMaterialAndStatusIn(String plantId, String rawMaterial, List<String> statuses, Pageable pageable);
    List<RawMaterialConsumption> findByPlantIdAndRawMaterialAndStatusIn(String plantId, String rawMaterial, List<String> statuses);
}
