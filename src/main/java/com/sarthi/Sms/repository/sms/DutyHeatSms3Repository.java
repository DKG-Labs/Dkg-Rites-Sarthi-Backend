package com.sarthi.Sms.repository.sms;


import com.sarthi.Sms.entity.sms.DutyHeatRelationIdSms3;
import com.sarthi.Sms.entity.sms.DutyHeatSms3Entity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DutyHeatSms3Repository extends JpaRepository<DutyHeatSms3Entity, DutyHeatRelationIdSms3> {
    
}
