package com.sarthi.Sleeper.repository;

import com.sarthi.Sleeper.entity.Compaction.CompactionScada;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CompactionScadaRepository extends JpaRepository<CompactionScada, Long> {
    @Query("""
    SELECT s FROM CompactionScada s
    WHERE s.compaction.id IN :ids
""")
    List<CompactionScada> findByCompactionIds(List<Long> ids);
}
