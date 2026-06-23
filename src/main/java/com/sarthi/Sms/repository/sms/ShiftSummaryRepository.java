package com.sarthi.Sms.repository.sms;


import com.sarthi.Sms.entity.sms.ShiftSummaryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;


@Repository
public interface ShiftSummaryRepository extends JpaRepository<ShiftSummaryEntity, String> {
    Optional<ShiftSummaryEntity> findByDutyId(String dutyId);
}
