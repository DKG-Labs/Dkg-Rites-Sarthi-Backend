package com.sarthi.Sms.repository.sms;


import com.sarthi.Sms.entity.sms.DutyHeatRelationIdSms2;
import com.sarthi.Sms.entity.sms.DutyHeatSms2Entity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DutyHeatSms2Repository extends JpaRepository<DutyHeatSms2Entity, DutyHeatRelationIdSms2> {
    
}
