package com.sarthi.repository;

import com.sarthi.entity.VendorCalibrationDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VendorCalibrationDetailRepository extends JpaRepository<VendorCalibrationDetail, Long> {

    List<VendorCalibrationDetail> findByVendorCalibrationHeaderId(Long headerId);
}
