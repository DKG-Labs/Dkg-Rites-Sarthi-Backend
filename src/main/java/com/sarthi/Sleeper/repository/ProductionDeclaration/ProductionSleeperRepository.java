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

  /*  @Query("""
SELECT COUNT(s.id)
FROM ProductionSleeper s
JOIN s.benchGroup b
JOIN b.chamber c
JOIN c.declaration d
WHERE d.id = :batchId
""")
    Long countByBatchId(Long batchId);
*/
  @Query(value = """
SELECT COUNT(*)
FROM production_sleeper s
JOIN production_bench_group b ON s.bench_group_id = b.id
JOIN production_stress_chamber c ON b.chamber_id = c.id
JOIN production_declaration d ON c.declaration_id = d.id
WHERE d.id = :batchId
AND s.sleeper_no NOT IN (
    SELECT ds.sleeper_no
    FROM demoulding_defective_sleepers ds
    JOIN demoulding_inspection di 
        ON ds.inspection_id = di.id
    WHERE di.batch_no COLLATE utf8mb4_unicode_ci = d.batch_number COLLATE utf8mb4_unicode_ci
)
""", nativeQuery = true)
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

   /* @Query("""
SELECT s.sleeperNo 
FROM ProductionSleeper s
WHERE s.gang.declaration.batchNumber = :batchNo
AND :benchNo BETWEEN s.gang.gangFrom AND s.gang.gangTo
AND s.gang.sleeperType = :sleeperType
""")
    List<String> findLongLineSleepers(String batchNo, Integer benchNo, String sleeperType); */
   @Query("""
SELECT s.sleeperNo 
FROM ProductionSleeper s
WHERE s.gang.declaration.batchNumber = :batchNo
AND :benchNo BETWEEN s.gang.gangFrom AND s.gang.gangTo
AND s.gang.sleeperType = :sleeperType
AND s.sleeperNo LIKE CONCAT(:benchNo, '%')
""")
   List<String> findLongLineSleepers(String batchNo, Integer benchNo, String sleeperType);

    @Query("""
SELECT s
FROM ProductionSleeper s
JOIN s.gang g
WHERE g.declaration.id = :batchId
""")
    List<ProductionSleeper> getSleepersFromGang(Long batchId);

    @Query("""
SELECT DISTINCT g.sleeperType
FROM ProductionLongLineGang g
WHERE g.declaration.id = :batchId
""")
    String getLongLineSleeperType(Long batchId);
}
