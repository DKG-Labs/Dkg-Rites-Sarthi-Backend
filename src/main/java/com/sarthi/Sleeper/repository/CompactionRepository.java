package com.sarthi.Sleeper.repository;

import com.sarthi.Sleeper.entity.Compaction.Compaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface CompactionRepository extends JpaRepository<Compaction , Long> {


    @Query("""
    SELECT c FROM Compaction c
    WHERE c.plantId = :plantId
    AND c.vendorCode = :vendorCode
    AND c.shift = :shift
    AND c.createdBy = :createdBy
    AND c.createdDate BETWEEN :startOfDay AND :endOfDay
""")
    List<Compaction> findByDate(
            String plantId,
            String vendorCode,
            String shift,
            int createdBy,
            LocalDateTime startOfDay,
            LocalDateTime endOfDay
    );


}
