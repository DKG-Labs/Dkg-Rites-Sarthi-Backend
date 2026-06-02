package com.sarthi.repository;

import com.sarthi.entity.IeVendorCalibrationInspectionDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IeVendorCalibrationInspectionDetailRepository extends JpaRepository<IeVendorCalibrationInspectionDetail, Long> {
}
