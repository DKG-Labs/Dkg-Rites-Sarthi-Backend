package com.sarthi.Sms.repository.sms;


import com.sarthi.Sms.entity.sms.DutyHeatRelationEntity;
import com.sarthi.Sms.entity.sms.DutyHeatRelationIdSms2;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;


@Repository
public interface DutyHeatRelationRepository extends JpaRepository<DutyHeatRelationEntity, DutyHeatRelationIdSms2> {
    List<DutyHeatRelationEntity> findByDutyHeatRelationIdDutyId(String dutyId);
    Optional<DutyHeatRelationEntity> findByDutyHeatRelationId(DutyHeatRelationIdSms2 dutyHeatRelationId);
}
