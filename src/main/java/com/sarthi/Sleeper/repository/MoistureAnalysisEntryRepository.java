package com.sarthi.Sleeper.repository;


import com.sarthi.Sleeper.entity.MoistureAnalysisEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface MoistureAnalysisEntryRepository extends JpaRepository<MoistureAnalysisEntry, Long> {

    @EntityGraph(attributePaths = {"sections"})
    List<MoistureAnalysisEntry> findAll();

    List<MoistureAnalysisEntry> findTop5ByOrderByIdDesc();
}
