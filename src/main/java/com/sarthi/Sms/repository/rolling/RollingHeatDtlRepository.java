package com.sarthi.Sms.repository.rolling;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.sarthi.Sms.entity.rolling.RollingHeatDtlEntity;
import com.sarthi.Sms.entity.rolling.RollingHeatDtlId;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.List;



@Repository
public interface RollingHeatDtlRepository extends JpaRepository<RollingHeatDtlEntity, RollingHeatDtlId>{
    Optional<RollingHeatDtlEntity> findByRollingHeatDtlId(RollingHeatDtlId rollingHeatDtlId);
    List<RollingHeatDtlEntity> findByDutyId(String dutyId);
    Optional<RollingHeatDtlEntity> findByDutyIdAndRollingHeatDtlId(String dutyId, RollingHeatDtlId rollingHeatDtlId);

    // Check if any heat exists
    boolean existsByRollingHeatDtlId_HeatNo(String heatNo);

    // Delete all records by heatNo
    @Modifying
    @Transactional
    void deleteByRollingHeatDtlId_HeatNo(String heatNo);
}
