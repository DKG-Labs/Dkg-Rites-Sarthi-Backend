package com.sarthi.repository;

import com.sarthi.entity.CallCancellationDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

@Repository
public interface CallCancellationDetailRepository extends JpaRepository<CallCancellationDetail, Long> {
    Optional<CallCancellationDetail> findByCallNumber(String callNumber);
    List<CallCancellationDetail> findByVendorCode(String vendorCode);

    @Query("SELECT c FROM CallCancellationDetail c WHERE c.callNumber IN :callNumbers")
    List<CallCancellationDetail> findByCallNumberIn(@Param("callNumbers") Collection<String> callNumbers);
}
