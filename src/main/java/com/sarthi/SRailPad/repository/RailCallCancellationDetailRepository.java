package com.sarthi.SRailPad.repository;

import com.sarthi.SRailPad.entity.RailCallCancellationDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RailCallCancellationDetailRepository extends JpaRepository<RailCallCancellationDetail, Long> {
    Optional<RailCallCancellationDetail> findByCallNumber(String callNumber);
    List<RailCallCancellationDetail> findByVendorCode(String vendorCode);
}
