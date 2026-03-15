package com.sarthi.Sleeper.repository.FinalInspectionRepository;


import com.sarthi.Sleeper.entity.FinalInspection.MorSampleDeclaration;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface MorSampleRepository extends JpaRepository<MorSampleDeclaration, Long> {
    boolean existsByWaterCubeStrengthTestId(Long waterCubeStrengthTestId);
    List<MorSampleDeclaration> findByStatus(String status);
    List<MorSampleDeclaration> findByStatusAndCreatedBy(String status, Long createdBy);
}
