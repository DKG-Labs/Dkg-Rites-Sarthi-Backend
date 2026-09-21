package com.sarthi.Sms.repository.sms;


import com.sarthi.Sms.entity.sms.DutyHeatRelationIdSms3;
import com.sarthi.Sms.entity.sms.DutyHeatSms3Entity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface DutyHeatSms3Repository extends JpaRepository<DutyHeatSms3Entity, DutyHeatRelationIdSms3> {
    @Modifying
    @Query("DELETE FROM DutyHeatSms3Entity d WHERE d.dutyHeatRelationId.heatNo = :heatNo")
    void deleteByHeatNo(@Param("heatNo") String heatNo);
}
