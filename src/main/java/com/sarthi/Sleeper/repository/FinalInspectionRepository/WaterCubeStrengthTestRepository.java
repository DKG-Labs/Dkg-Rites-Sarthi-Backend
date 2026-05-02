package com.sarthi.Sleeper.repository.FinalInspectionRepository;

import com.sarthi.Sleeper.dto.SleeperDashboardDtos.WaterProjection;
import com.sarthi.Sleeper.entity.FinalInspection.WaterCubeStrengthTest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WaterCubeStrengthTestRepository extends JpaRepository<WaterCubeStrengthTest, Long> {

    List<WaterCubeStrengthTest> findByCreatedBy(Long createdBy);

    List<WaterCubeStrengthTest> findByWaterCubeSampleDeclarationId(Long declarationId);

    @Query("""
SELECT COUNT(w) > 0 
FROM WaterCubeStrengthTest w 
WHERE w.batchNumber = :batchNo
""")
    boolean existsWaterCube(String batchNo);

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
