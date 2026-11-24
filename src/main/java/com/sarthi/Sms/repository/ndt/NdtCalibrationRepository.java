package com.sarthi.Sms.repository.ndt;

import com.sarthi.Sms.entity.ndt.NdtCalibrationEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface NdtCalibrationRepository extends JpaRepository<NdtCalibrationEntity, Integer> {
    List<NdtCalibrationEntity> findByDutyId(String dutyId);

    List<NdtCalibrationEntity> findByCalibrationIdIn(List<Integer> clbId);
}
