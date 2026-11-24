package com.sarthi.Sms.repository.ndt;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.sarthi.Sms.entity.ndt.NdtDutyClbPrevDataEntity;
@Repository
public interface NdtDutyClbPrevDataRepository extends JpaRepository<NdtDutyClbPrevDataEntity, Integer> {
    // Add custom query methods here if needed

    void deleteByDutyId(String dutyId);

    List<NdtDutyClbPrevDataEntity> findByDutyId(String dutyId);
}

