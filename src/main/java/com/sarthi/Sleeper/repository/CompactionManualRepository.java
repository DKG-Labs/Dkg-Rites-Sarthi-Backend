package com.sarthi.Sleeper.repository;

import com.sarthi.Sleeper.entity.Compaction.CompactionManual;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CompactionManualRepository extends JpaRepository<CompactionManual, Long> {
    @Query("""
    SELECT m FROM CompactionManual m
    WHERE m.compaction.id IN :ids
""")
    List<CompactionManual> findByCompactionIds(List<Long> ids);
}
