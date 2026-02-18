package com.sarthi.Sleeper.repository;


import com.sarthi.Sleeper.entity.MoistureAnalysisEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MoistureAnalysisEntryRepository extends JpaRepository<MoistureAnalysisEntry, Long> {
}
