package com.sarthi.Sms.repository.calibration;

import com.sarthi.Sms.entity.calibration.CalibrationDetailEntity;
import com.sarthi.Sms.entity.calibration.CalibrationDetailId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.sql.Date;
import java.util.List;
import java.util.Optional;
@Repository
public interface CalibrationDetailRepository extends JpaRepository<CalibrationDetailEntity, CalibrationDetailId> {
    List<CalibrationDetailEntity> findByIdDutyId(String dutyId); // Accessing the dutyId from the embedded key
    @Query("SELECT c FROM CalibrationDetailEntity c WHERE c.id.serialNumber = :serialNumber ORDER BY c.calibrationDate DESC LIMIT 1")
    Optional<CalibrationDetailEntity> findTopByIdSerialNumberOrderByCalibrationDateDesc(@Param("serialNumber") String serialNumber);
    // Or another approach to access using the embedded ID directly
    List<CalibrationDetailEntity> findById_SerialNumber(String serialNumber);
    @Query("""
           SELECT c
           FROM CalibrationDetailEntity c
           WHERE c.calibrationDate = (
               SELECT MAX(innerC.calibrationDate)
               FROM CalibrationDetailEntity innerC
               WHERE innerC.id.serialNumber = c.id.serialNumber
           )
           ORDER BY c.calibrationDate DESC
           """)
    List<CalibrationDetailEntity> findAllLatestCalibrations();

    List<CalibrationDetailEntity> findByInstrumentIn(List<String> instruments);
}
