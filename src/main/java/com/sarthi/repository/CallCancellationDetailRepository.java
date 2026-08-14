package com.sarthi.repository;

import com.sarthi.entity.CallCancellationDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CallCancellationDetailRepository extends JpaRepository<CallCancellationDetail, Long> {
    Optional<CallCancellationDetail> findByCallNumber(String callNumber);
    List<CallCancellationDetail> findByVendorCode(String vendorCode);
}
