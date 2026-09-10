package com.sarthi.Sleeper.repository.ProductionDeclaration;

import com.sarthi.Sleeper.entity.ProductionDeclaration.ProductionSleeper;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductionSleeperRepository extends JpaRepository<ProductionSleeper, Long> {
    @Query(value = """
SELECT ps.*
FROM production_sleeper ps
WHERE ps.bench_group_id IN (
    SELECT bg.id FROM production_bench_group bg
    JOIN production_stress_chamber c ON c.id = bg.chamber_id
    WHERE c.declaration_id = :batchId
)
UNION ALL
SELECT ps.*
FROM production_sleeper ps
WHERE ps.gang_id IN (
    SELECT g.id FROM production_longline_gang g
    WHERE g.declaration_id = :batchId
)
""", nativeQuery = true)
    List<ProductionSleeper> getSleepersByBatch(@Param("batchId") Long batchId);

    @Query(value = """
SELECT ps.*
FROM production_sleeper ps
WHERE ps.bench_group_id IN (
    SELECT bg.id FROM production_bench_group bg
    JOIN production_stress_chamber c ON c.id = bg.chamber_id
    WHERE c.declaration_id = :batchId AND bg.sleeper_type = :sleeperType
)
UNION ALL
SELECT ps.*
FROM production_sleeper ps
WHERE ps.gang_id IN (
    SELECT g.id FROM production_longline_gang g
    WHERE g.declaration_id = :batchId AND g.sleeper_type = :sleeperType
)
""", nativeQuery = true)
   List<ProductionSleeper> getSleepersByBatchAndType(@Param("batchId") Long batchId, @Param("sleeperType") String sleeperType);

    @Query(value = """
SELECT (
    (SELECT COUNT(ps.id) FROM production_sleeper ps
     WHERE ps.bench_group_id IN (
         SELECT bg.id FROM production_bench_group bg
         JOIN production_stress_chamber c ON c.id = bg.chamber_id
         WHERE c.declaration_id = :batchId
     ))
    +
    (SELECT COUNT(ps.id) FROM production_sleeper ps
     WHERE ps.gang_id IN (
         SELECT g.id FROM production_longline_gang g
         WHERE g.declaration_id = :batchId
     ))
)
""", nativeQuery = true)
    Long countByBatchId(@Param("batchId") Long batchId);

    @Query(value = """
SELECT (
    (SELECT COUNT(ps.id) FROM production_sleeper ps
     WHERE ps.bench_group_id IN (
         SELECT bg.id FROM production_bench_group bg
         JOIN production_stress_chamber c ON c.id = bg.chamber_id
         WHERE c.declaration_id = :batchId AND bg.sleeper_type = :sleeperType
     ))
    +
    (SELECT COUNT(ps.id) FROM production_sleeper ps
     WHERE ps.gang_id IN (
         SELECT g.id FROM production_longline_gang g
         WHERE g.declaration_id = :batchId AND g.sleeper_type = :sleeperType
     ))
)
""", nativeQuery = true)
    Long countByBatchIdAndType(@Param("batchId") Long batchId, @Param("sleeperType") String sleeperType);

    @Query(value = """
SELECT DISTINCT bg.sleeper_type
FROM production_bench_group bg
JOIN production_stress_chamber c ON c.id = bg.chamber_id
WHERE c.declaration_id = :batchId
UNION
SELECT DISTINCT g.sleeper_type
FROM production_longline_gang g
WHERE g.declaration_id = :batchId
""", nativeQuery = true)
    List<String> getSleeperTypeByBatch(@Param("batchId") Long batchId);
    @Query("SELECT s.sleeperNo FROM ProductionSleeper s " +
            "WHERE s.benchGroup.chamber.declaration.batchNumber = :batchNo " +
            "AND s.benchGroup.benchNo = :benchNo " +
            "AND s.benchGroup.sleeperType = :sleeperType")
    List<String> findSleepers(@Param("batchNo") String batchNo,
                              @Param("benchNo") String benchNo,
                              @Param("sleeperType") String sleeperType);

   /* @Query("""
SELECT s.sleeperNo 
FROM ProductionSleeper s
WHERE s.gang.declaration.batchNumber = :batchNo
AND :benchNo BETWEEN s.gang.gangFrom AND s.gang.gangTo
AND s.gang.sleeperType = :sleeperType
""")
    List<String> findLongLineSleepers(String batchNo, Integer benchNo, String sleeperType); */
  /* @Query("""
SELECT s.sleeperNo
FROM ProductionSleeper s
WHERE s.gang.declaration.batchNumber = :batchNo
AND :benchNo BETWEEN s.gang.gangFrom AND s.gang.gangTo
AND s.gang.sleeperType = :sleeperType
AND s.sleeperNo LIKE CONCAT(:benchNo, '%')
""")
   List<String> findLongLineSleepers(String batchNo, Integer benchNo, String sleeperType);
*/
   @Query("""
SELECT s.sleeperNo 
FROM ProductionSleeper s
WHERE s.gang.declaration.batchNumber = :batchNo
           AND (
               (s.gang.gangFrom IS NOT NULL AND s.gang.gangTo IS NOT NULL
                   AND :benchNo BETWEEN s.gang.gangFrom AND s.gang.gangTo)
               OR
               (s.gang.gangNo IS NOT NULL AND s.gang.gangNo = :benchNo)
           )
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

//    @Query("""
//SELECT DISTINCT g.sleeperType
//FROM ProductionLongLineGang g
//WHERE g.declaration.id = :batchId
//""")
//    String getLongLineSleeperType(Long batchId);

    @Query("""
SELECT DISTINCT g.sleeperType
FROM ProductionLongLineGang g
WHERE g.declaration.id = :batchId
""")
    List<String> getLongLineSleeperType(Long batchId);

    @Query(value = "SELECT COUNT(id) FROM production_sleeper", nativeQuery = true)
    Long countBy();

    @Query("""
SELECT s
FROM ProductionSleeper s
JOIN s.gang g
WHERE g.declaration.id = :batchId
AND g.sleeperType = :sleeperType
""")
    List<ProductionSleeper> getSleepersFromGangAndType(Long batchId, String sleeperType);


}
