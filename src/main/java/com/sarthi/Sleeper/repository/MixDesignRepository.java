package com.sarthi.Sleeper.repository;

import com.sarthi.Sleeper.entity.MixDesign;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MixDesignRepository extends JpaRepository<MixDesign, Long> {
}
