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
WHERE c.declaration.id = :batchId
""")
    List<ProductionSleeper> getSleepersByBatchViaChamber(@Param("batchId") Long batchId);

    @Query("""
SELECT s
FROM ProductionSleeper s
JOIN s.gang g
WHERE g.declaration.id = :batchId
""")
    List<ProductionSleeper> getSleepersByBatchViaGang(@Param("batchId") Long batchId);

    // Split instead of LEFT JOIN + OR (full scan); merged by id so each sleeper appears once, as before,
    // and returned in id order - the order the old single query produced (it scanned production_sleeper
    // by primary key), so screens listing these sleepers keep the same order.
    default List<ProductionSleeper> getSleepersByBatch(Long batchId) {
        java.util.TreeMap<Long, ProductionSleeper> merged = new java.util.TreeMap<>();
        for (ProductionSleeper s : getSleepersByBatchViaChamber(batchId)) merged.putIfAbsent(s.getId(), s);
        for (ProductionSleeper s : getSleepersByBatchViaGang(batchId)) merged.putIfAbsent(s.getId(), s);
        return new java.util.ArrayList<>(merged.values());
    }

    // Split into two index-friendly branches (chamber / gang) instead of LEFT JOIN + OR, which forced a full scan.
    // benchGroup / gang are JOIN FETCHed: callers read their sleeperType per sleeper, and lazy loading did one
    // DB round trip per bench group / gang (completed-batches took 13+ min for ~200 batches).
    @Query("""
SELECT s, c.declaration.id
FROM ProductionSleeper s
JOIN FETCH s.benchGroup b
JOIN b.chamber c
WHERE c.declaration.id IN :batchIds
""")
    List<Object[]> getSleepersWithBatchIdViaChamber(@Param("batchIds") java.util.Collection<Long> batchIds);

    @Query("""
SELECT s, g.declaration.id
FROM ProductionSleeper s
JOIN FETCH s.gang g
WHERE g.declaration.id IN :batchIds
""")
    List<Object[]> getSleepersWithBatchIdViaGang(@Param("batchIds") java.util.Collection<Long> batchIds);

    /** Same result as the old COALESCE(chamber, gang) query: chamber wins if a sleeper has both. */
    default List<Object[]> getSleepersWithBatchIdByBatchIds(java.util.Collection<Long> batchIds) {
        List<Object[]> result = new java.util.ArrayList<>(getSleepersWithBatchIdViaChamber(batchIds));
        java.util.Set<Long> seen = new java.util.HashSet<>();
        for (Object[] row : result) {
            seen.add(((ProductionSleeper) row[0]).getId());
        }
        for (Object[] row : getSleepersWithBatchIdViaGang(batchIds)) {
            if (seen.add(((ProductionSleeper) row[0]).getId())) {
                result.add(row);
            }
        }
        return result;
    }

    @Query("""
SELECT s
FROM ProductionSleeper s
JOIN s.benchGroup b
JOIN b.chamber c
WHERE c.declaration.id = :batchId AND b.sleeperType = :sleeperType
""")
    List<ProductionSleeper> getSleepersByBatchAndTypeViaChamber(@Param("batchId") Long batchId, @Param("sleeperType") String sleeperType);

    @Query("""
SELECT s
FROM ProductionSleeper s
JOIN s.gang g
WHERE g.declaration.id = :batchId AND g.sleeperType = :sleeperType
""")
    List<ProductionSleeper> getSleepersByBatchAndTypeViaGang(@Param("batchId") Long batchId, @Param("sleeperType") String sleeperType);

    default List<ProductionSleeper> getSleepersByBatchAndType(Long batchId, String sleeperType) {
        java.util.LinkedHashMap<Long, ProductionSleeper> merged = new java.util.LinkedHashMap<>();
        for (ProductionSleeper s : getSleepersByBatchAndTypeViaChamber(batchId, sleeperType)) merged.putIfAbsent(s.getId(), s);
        for (ProductionSleeper s : getSleepersByBatchAndTypeViaGang(batchId, sleeperType)) merged.putIfAbsent(s.getId(), s);
        return new java.util.ArrayList<>(merged.values());
    }

    // Two index-friendly branches instead of LEFT JOIN + OR (full scan of production_sleeper).
    // UNION (not UNION ALL) de-duplicates sleeper ids, so the count matches the old OR exactly.
    @Query(value = """
    SELECT COUNT(*) FROM (
        SELECT s.id
        FROM production_sleeper s
        JOIN production_bench_group b ON b.id = s.bench_group_id
        JOIN production_stress_chamber c ON c.id = b.chamber_id
        WHERE c.declaration_id = :batchId
        UNION
        SELECT s.id
        FROM production_sleeper s
        JOIN production_longline_gang g ON g.id = s.gang_id
        WHERE g.declaration_id = :batchId
    ) t
    """, nativeQuery = true)
    Long countByBatchId(@Param("batchId") Long batchId);

    // Two index-friendly branches instead of LEFT JOIN + (c IN ... OR g IN ...), which forced a full scan.
    // Note: a sleeper linked to BOTH a chamber and a gang is counted in both; the old COALESCE counted chamber only.
    @Query(value = """
    SELECT t.declaration_id, SUM(t.cnt)
    FROM (
        SELECT c.declaration_id AS declaration_id, COUNT(s.id) AS cnt
        FROM production_sleeper s
        JOIN production_bench_group b ON b.id = s.bench_group_id
        JOIN production_stress_chamber c ON c.id = b.chamber_id
        WHERE c.declaration_id IN (:batchIds)
        GROUP BY c.declaration_id
        UNION ALL
        SELECT g.declaration_id AS declaration_id, COUNT(s.id) AS cnt
        FROM production_sleeper s
        JOIN production_longline_gang g ON g.id = s.gang_id
        WHERE g.declaration_id IN (:batchIds)
        GROUP BY g.declaration_id
    ) t
    GROUP BY t.declaration_id
    """, nativeQuery = true)
    List<Object[]> countSleepersByBatchIds(@Param("batchIds") List<Long> batchIds);

    // Same split as countByBatchId; UNION de-duplicates so the count matches the old OR exactly.
    @Query(value = """
    SELECT COUNT(*) FROM (
        SELECT s.id
        FROM production_sleeper s
        JOIN production_bench_group b ON b.id = s.bench_group_id
        JOIN production_stress_chamber c ON c.id = b.chamber_id
        WHERE c.declaration_id = :batchId AND b.sleeper_type = :sleeperType
        UNION
        SELECT s.id
        FROM production_sleeper s
        JOIN production_longline_gang g ON g.id = s.gang_id
        WHERE g.declaration_id = :batchId AND g.sleeper_type = :sleeperType
    ) t
    """, nativeQuery = true)
    Long countByBatchIdAndType(@Param("batchId") Long batchId, @Param("sleeperType") String sleeperType);

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
