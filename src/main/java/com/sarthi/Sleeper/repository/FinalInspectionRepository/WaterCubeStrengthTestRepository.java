package com.sarthi.Sleeper.repository.FinalInspectionRepository;

import com.sarthi.Sleeper.dto.SleeperDashboardDtos.WaterProjection;
import com.sarthi.Sleeper.entity.FinalInspection.WaterCubeStrengthTest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WaterCubeStrengthTestRepository extends JpaRepository<WaterCubeStrengthTest, Long> {

    @EntityGraph(attributePaths = {"details", "waterCubeSampleDeclaration"})
    List<WaterCubeStrengthTest> findAll();

    // plantId must already be normalised (colons/spaces removed, lower-case) or null
    @EntityGraph(attributePaths = {"details", "waterCubeSampleDeclaration"})
    @Query("SELECT w FROM WaterCubeStrengthTest w " +
            "WHERE (:plantId IS NULL OR LOWER(REPLACE(REPLACE(w.plantId, ':', ''), ' ', '')) = :plantId) " +
            "AND (:fromDate IS NULL OR w.createdDate >= :fromDate) " +
            "ORDER BY w.id ASC")
    List<WaterCubeStrengthTest> findForList(
            @org.springframework.data.repository.query.Param("plantId") String plantId,
            @org.springframework.data.repository.query.Param("fromDate") java.time.LocalDateTime fromDate);

    // Page the ids first (a collection fetch can't be paged in the database), then load those rows
    @Query(value = "SELECT w.id FROM WaterCubeStrengthTest w " +
            "WHERE (:plantId IS NULL OR LOWER(REPLACE(REPLACE(w.plantId, ':', ''), ' ', '')) = :plantId) " +
            "AND (:fromDate IS NULL OR w.createdDate >= :fromDate) " +
            "ORDER BY w.id DESC",
            countQuery = "SELECT COUNT(w) FROM WaterCubeStrengthTest w " +
            "WHERE (:plantId IS NULL OR LOWER(REPLACE(REPLACE(w.plantId, ':', ''), ' ', '')) = :plantId) " +
            "AND (:fromDate IS NULL OR w.createdDate >= :fromDate)")
    org.springframework.data.domain.Page<Long> findIdsForPage(
            @org.springframework.data.repository.query.Param("plantId") String plantId,
            @org.springframework.data.repository.query.Param("fromDate") java.time.LocalDateTime fromDate,
            org.springframework.data.domain.Pageable pageable);

    @EntityGraph(attributePaths = {"details", "waterCubeSampleDeclaration"})
    List<WaterCubeStrengthTest> findByIdIn(java.util.Collection<Long> ids);

    // Only what the verified-batches list needs, oldest first so a later test overrides an earlier one
    interface WaterCubeConditionView {
        Long getProductionDeclarationId();
        Boolean getCondition2();
        Integer getMrSamplesRequired();
    }

    @Query("SELECT w.waterCubeSampleDeclaration.productionDeclarationId AS productionDeclarationId, " +
            "w.condition2 AS condition2, w.mrSamplesRequired AS mrSamplesRequired " +
            "FROM WaterCubeStrengthTest w " +
            "WHERE w.waterCubeSampleDeclaration.productionDeclarationId IS NOT NULL " +
            "ORDER BY w.id ASC")
    List<WaterCubeConditionView> findConditionViews();

    @EntityGraph(attributePaths = {"details", "waterCubeSampleDeclaration"})
    List<WaterCubeStrengthTest> findByCreatedBy(Long createdBy);

    @EntityGraph(attributePaths = {"details", "waterCubeSampleDeclaration"})
    List<WaterCubeStrengthTest> findByWaterCubeSampleDeclarationId(Long declarationId);

    @Override
    @EntityGraph(attributePaths = {"details", "waterCubeSampleDeclaration"})
    Optional<WaterCubeStrengthTest> findById(Long id);

    @Query("""
SELECT COUNT(w) > 0 
FROM WaterCubeStrengthTest w 
WHERE w.batchNumber = :batchNo
""")
    boolean existsWaterCube(String batchNo);

    @Query("""
SELECT COUNT(w) > 0 
FROM WaterCubeStrengthTest w 
WHERE (TRIM(w.batchNumber) = TRIM(:batchNo) 
    OR TRIM(w.batchNumber) = TRIM(REPLACE(:batchNo, 'B-', '')) 
    OR CONCAT('B-', TRIM(w.batchNumber)) = TRIM(:batchNo))
AND UPPER(TRIM(w.finalTestResult)) LIKE 'PASS%'
""")
    boolean hasPassedWaterCube(@org.springframework.data.repository.query.Param("batchNo") String batchNo);

    @Query(value = """
        SELECT DISTINCT TRIM(batch_number)
        FROM water_cube_strength_test
        WHERE UPPER(TRIM(final_test_result)) LIKE 'PASS%'
    """, nativeQuery = true)
    List<String> findAllPassedBatchNumbers();

    @Query(value = """
        SELECT DISTINCT TRIM(batch_number)
        FROM water_cube_strength_test
        WHERE (batch_number IN (:batchNumbers) 
           OR TRIM(batch_number) IN (:batchNumbers)
           OR REPLACE(batch_number, 'B-', '') IN (:batchNumbers)
           OR CONCAT('B-', batch_number) IN (:batchNumbers)
           OR REPLACE(batch_number, 'W-', '') IN (:batchNumbers)
           OR CONCAT('W-', batch_number) IN (:batchNumbers)
           OR REPLACE(batch_number, 'w-', '') IN (:batchNumbers)
           OR CONCAT('w-', batch_number) IN (:batchNumbers))
          AND UPPER(TRIM(final_test_result)) LIKE 'PASS%'
    """, nativeQuery = true)
    List<String> findPassedBatchNumbersIn(@org.springframework.data.repository.query.Param("batchNumbers") java.util.Collection<String> batchNumbers);

    @Query(value = """
        SELECT *
        FROM water_cube_strength_test
        WHERE batch_number IN (:batchNumbers)
    """, nativeQuery = true)
    List<WaterCubeStrengthTest> findByBatchNumbersIn(@org.springframework.data.repository.query.Param("batchNumbers") java.util.Collection<String> batchNumbers);

    @Query("SELECT DISTINCT w.batchNumber FROM WaterCubeStrengthTest w")
    List<String> findAllBatchNumbers();

    @Query(value = """
    SELECT wc.created_date AS createdDate,
           AVG(wcd.strength_nmm2) AS avgStrength
    FROM water_cube_strength_test wc
    JOIN water_cube_strength_detail wcd 
         ON wc.id = wcd.strength_test_id
    WHERE wc.batch_number = :batchNo
    GROUP BY wc.created_date
    LIMIT 1
""", nativeQuery = true)
    WaterProjection getWaterData(String batchNo);
}
