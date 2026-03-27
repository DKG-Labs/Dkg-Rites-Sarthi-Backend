package com.sarthi.Sleeper.repository.ProductionDeclaration;

import com.sarthi.Sleeper.entity.ProductionDeclaration.ProductionSleeper;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductionSleeperRepository extends JpaRepository<ProductionSleeper, Long> {
    @Query("""
SELECT s
FROM ProductionSleeper s
JOIN s.benchGroup b
JOIN b.chamber c
JOIN c.declaration d
WHERE d.id = :batchId
""")
    List<ProductionSleeper> getSleepersByBatch(Long batchId);

    @Query("""
SELECT COUNT(s.id)
FROM ProductionSleeper s
JOIN s.benchGroup b
JOIN b.chamber c
JOIN c.declaration d
WHERE d.id = :batchId
""")
    Long countByBatchId(Long batchId);

    @Query("""
SELECT DISTINCT b.sleeperType
FROM ProductionBenchGroup b
JOIN b.chamber c
JOIN c.declaration d
WHERE d.id = :batchId
""")
    String getSleeperTypeByBatch(Long batchId);

    @Query("SELECT s.sleeperNo FROM ProductionSleeper s " +
            "WHERE s.benchGroup.chamber.declaration.batchNumber = :batchNo " +
            "AND s.benchGroup.benchNo = :benchNo " +
            "AND s.benchGroup.sleeperType = :sleeperType")
    List<String> findSleepers(@Param("batchNo") String batchNo,
                              @Param("benchNo") Integer benchNo,
                              @Param("sleeperType") String sleeperType);
}
