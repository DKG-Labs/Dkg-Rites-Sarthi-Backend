package com.sarthi.Sleeper.repository.FinalInspectionRepository;

import com.sarthi.Sleeper.entity.FinalInspection.WaterCubeSampleDeclaration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WaterCubeSampleRepository extends JpaRepository<WaterCubeSampleDeclaration, Long> {

    List<WaterCubeSampleDeclaration> findByCreatedBy(Long createdBy);

    @Query("SELECT w FROM WaterCubeSampleDeclaration w WHERE NOT EXISTS (" +
            "SELECT 1 FROM WaterCubeStrengthTest t " +
            "WHERE t.waterCubeSampleDeclaration.id = w.id)")
    List<WaterCubeSampleDeclaration> findAllNotTested();
}
