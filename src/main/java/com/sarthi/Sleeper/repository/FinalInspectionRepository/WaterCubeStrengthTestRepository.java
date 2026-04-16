package com.sarthi.Sleeper.repository.FinalInspectionRepository;

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
}
