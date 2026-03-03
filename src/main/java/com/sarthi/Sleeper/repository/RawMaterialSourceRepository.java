package com.sarthi.Sleeper.repository;

import com.sarthi.Sleeper.entity.RawMaterialSource;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RawMaterialSourceRepository extends JpaRepository<RawMaterialSource, Long> {
}
