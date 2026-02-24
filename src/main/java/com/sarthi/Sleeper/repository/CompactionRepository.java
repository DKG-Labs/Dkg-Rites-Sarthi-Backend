package com.sarthi.Sleeper.repository;

import com.sarthi.Sleeper.entity.Compaction.Compaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CompactionRepository extends JpaRepository<Compaction , Long> {
}
