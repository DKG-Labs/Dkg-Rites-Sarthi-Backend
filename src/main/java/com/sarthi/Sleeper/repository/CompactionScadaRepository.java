package com.sarthi.Sleeper.repository;

import com.sarthi.Sleeper.entity.Compaction.CompactionScada;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CompactionScadaRepository extends JpaRepository<CompactionScada, Long> {
}
