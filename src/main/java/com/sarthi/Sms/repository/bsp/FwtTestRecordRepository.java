package com.sarthi.Sms.repository.bsp;

import java.time.LocalDate;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.sarthi.Sms.entity.bsp.FwtTestRecord;
public interface FwtTestRecordRepository extends JpaRepository<FwtTestRecord, Long> {
    Optional<FwtTestRecord> findTopByOrderByCreatedAtDesc();
    Page<FwtTestRecord> findByCreatedAtBetween(LocalDate startDate, LocalDate endDate, Pageable pageable);
}
