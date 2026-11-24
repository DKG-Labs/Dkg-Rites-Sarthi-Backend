package com.sarthi.Sms.repository.welding;

import com.sarthi.Sms.dto.welding.WeldingInspectionResponseDto;
import com.sarthi.Sms.entity.welding.WeldingDetailEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

import java.util.Optional;

@Repository
public interface WeldingDetailRepository extends JpaRepository<WeldingDetailEntity, String> {

    @Query("""
        SELECT new com.sarthi.Sms.dto.welding.WeldingInspectionResponseDto(
            m.inspectionMasterId, d.date, d.shift, m.panelId, i.railId1, i.railId2, r.panelDecision
        )
        FROM WeldingInspectionDutyEntity d
        JOIN WeldingDutyRelationEntity r ON d.dutyId = r.id.dutyId
        JOIN WeldingMasterEntity m ON r.id.inspectionMasterId = m.inspectionMasterId
        JOIN WeldingDetailEntity i ON m.inspectionMasterId = i.inspectionMasterId
        WHERE r.panelDecision IN ('Hold', 'Refinish','hold', 'refinish')
        """)
    List<WeldingInspectionResponseDto> findInspectionDetailsByPanelDecision();

    Optional<WeldingDetailEntity> findByJointNo(String jointNo);

    List<WeldingDetailEntity> findByInspectionMasterId(Integer inspectionMasterId);
}
