package com.sarthi.Sleeper.repository.FinalInspectionRepository;

import com.sarthi.Sleeper.entity.FinalInspection.WaterCubeStrengthTest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WaterCubeStrengthRepository extends JpaRepository<WaterCubeStrengthTest, Long> {
    List<WaterCubeStrengthTest> findByCreatedBy(Long createdBy);
    Optional<WaterCubeStrengthTest> findByWaterCubeSampleDeclarationId(Long declarationId);
    List<WaterCubeStrengthTest> findByFinalTestResult(String finalTestResult);
}
