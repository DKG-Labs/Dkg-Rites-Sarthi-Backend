package com.sarthi.Sms.repository.sms;


import com.sarthi.Sms.entity.sms.DutyHeatRelationIdSms2;
import com.sarthi.Sms.entity.sms.DutyHeatSms2Entity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface DutyHeatSms2Repository extends JpaRepository<DutyHeatSms2Entity, DutyHeatRelationIdSms2> {
    @Modifying
    @Query("DELETE FROM DutyHeatSms2Entity d WHERE d.dutyHeatRelationId.heatNo = :heatNo")
    void deleteByHeatNo(@Param("heatNo") String heatNo);
}
