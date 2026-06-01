package com.sarthi.repository;

import com.sarthi.entity.IeVendorCalibrationInspection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IeVendorCalibrationInspectionRepository extends JpaRepository<IeVendorCalibrationInspection, Long> {

}
