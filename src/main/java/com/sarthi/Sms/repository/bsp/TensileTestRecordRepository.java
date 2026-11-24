package com.sarthi.Sms.repository.bsp;

import com.sarthi.Sms.entity.bsp.TensileTestRecord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.Optional;

public interface TensileTestRecordRepository extends JpaRepository<TensileTestRecord, Long> {
    Optional<TensileTestRecord> findTopByOrderByCreatedAtDesc();
    Page<TensileTestRecord> findByCreatedAtBetween(LocalDate startDate, LocalDate endDate, Pageable pageable);
}
