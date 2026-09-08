package com.sarthi.Sleeper.repository.FinalInspectionRepository;

import com.sarthi.Sleeper.entity.FinalInspection.SleeperCallCancellationDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SleeperCallCancellationDetailRepository extends JpaRepository<SleeperCallCancellationDetail, Long> {

    Optional<SleeperCallCancellationDetail> findByCallNumber(String callNumber);

    List<SleeperCallCancellationDetail> findByVendorCode(String vendorCode);
}
