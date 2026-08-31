package com.sarthi.Sleeper.repository;

import com.sarthi.Sleeper.entity.MomentOfResistance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface MomentOfResistanceRepository extends JpaRepository<MomentOfResistance, Long> {
    @Query("""
    SELECT m FROM MomentOfResistance m
    LEFT JOIN MomentOfResistanceTest t
        ON m.batchNumber = t.batchNumber AND (t.testResult = 'Pass' OR t.testResult = 'Fail')
    WHERE (m.plantId = :plantId OR REPLACE(m.plantId, ':', '') = REPLACE(:plantId, ':', '') OR :plantId IS NULL)
    AND (m.vendorCode = :vendorCode OR REPLACE(m.vendorCode, ':', '') = REPLACE(:vendorCode, ':', '') OR :vendorCode IS NULL)
    AND m.shift = :shift
    AND m.createdBy = :createdBy
    AND m.createdDate BETWEEN :startOfDay AND :endOfDay
    AND t.batchNumber IS NULL
""")
    List<MomentOfResistance> findByDateExcludingTested(
            String plantId,
            String vendorCode,
            String shift,
            int createdBy,
            LocalDateTime startOfDay,
            LocalDateTime endOfDay
    );
}
