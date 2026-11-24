package com.sarthi.Sms.repository.welding;

import com.sarthi.Sms.entity.welding.WeldingDutyRelationEntity;
import com.sarthi.Sms.entity.welding.WeldingDutyInspectionRelationKey;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface WeldingDutyRelationRepository extends JpaRepository<WeldingDutyRelationEntity, WeldingDutyInspectionRelationKey> {

    // Query using the composite key parts
    Optional<WeldingDutyRelationEntity> findById_DutyIdAndId_InspectionMasterId(String dutyId, Integer inspectionMasterId);
}
