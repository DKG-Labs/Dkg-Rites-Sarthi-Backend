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
LEFT JOIN s.benchGroup b
LEFT JOIN b.chamber c
LEFT JOIN s.gang g
WHERE (c.declaration.id = :batchId OR g.declaration.id = :batchId)
""")
    List<ProductionSleeper> getSleepersByBatch(Long batchId);

    @Query("""
SELECT s
FROM ProductionSleeper s
LEFT JOIN s.benchGroup b
LEFT JOIN b.chamber c
LEFT JOIN s.gang g
WHERE (c.declaration.id = :batchId AND b.sleeperType = :sleeperType)
   OR (g.declaration.id = :batchId AND g.sleeperType = :sleeperType)
""")
   List<ProductionSleeper> getSleepersByBatchAndType(Long batchId, String sleeperType);

    @Query("""
SELECT COUNT(s.id)
FROM ProductionSleeper s
LEFT JOIN s.benchGroup b
LEFT JOIN b.chamber c
LEFT JOIN s.gang g
WHERE (c.declaration.id = :batchId OR g.declaration.id = :batchId)
""")
    Long countByBatchId(Long batchId);

    @Query("""
SELECT COUNT(s.id)
FROM ProductionSleeper s
LEFT JOIN s.benchGroup b
LEFT JOIN b.chamber c
LEFT JOIN s.gang g
WHERE (c.declaration.id = :batchId AND b.sleeperType = :sleeperType)
   OR (g.declaration.id = :batchId AND g.sleeperType = :sleeperType)
""")
    Long countByBatchIdAndType(Long batchId, String sleeperType);

@Query("""
SELECT DISTINCT COALESCE(b.sleeperType, g.sleeperType)
FROM ProductionSleeper s
LEFT JOIN s.benchGroup b
LEFT JOIN b.chamber c
LEFT JOIN s.gang g
WHERE (c.declaration.id = :batchId OR g.declaration.id = :batchId)
""")
List<String> getSleeperTypeByBatch(Long batchId);
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
