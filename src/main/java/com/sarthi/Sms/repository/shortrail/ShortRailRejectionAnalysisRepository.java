package com.sarthi.Sms.repository.shortrail;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.sarthi.Sms.entity.shortrail.ShortRailRejectionAnalysisEntity;
@Repository
public interface ShortRailRejectionAnalysisRepository extends JpaRepository<ShortRailRejectionAnalysisEntity, Long> {
    List<ShortRailRejectionAnalysisEntity> findByDutyId(String dutyId);

    List<ShortRailRejectionAnalysisEntity> findByDutyIdAndRailGradeInspectedAndRailSectionInspected(
        String dutyId, 
        String railGradeInspected, 
        String railSectionInspected
    );
}
