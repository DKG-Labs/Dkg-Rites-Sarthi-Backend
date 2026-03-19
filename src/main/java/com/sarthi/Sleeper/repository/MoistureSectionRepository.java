package com.sarthi.Sleeper.repository;

import com.sarthi.Sleeper.entity.MoistureSection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MoistureSectionRepository extends JpaRepository<MoistureSection, Long> {
}
