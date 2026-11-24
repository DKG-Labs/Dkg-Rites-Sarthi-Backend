package com.sarthi.Sms.repository.bsp;

import com.sarthi.Sms.entity.bsp.ChemTestRecord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.Optional;

public interface ChemTestRecordRepository extends JpaRepository<ChemTestRecord, Long> {
    Optional<ChemTestRecord> findTopByOrderByCreatedAtDesc();
    Page<ChemTestRecord> findByCreatedAtBetween(LocalDate startDate, LocalDate endDate, Pageable pageable);
}