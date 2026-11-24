package com.sarthi.Sms.repository.bsp;

import com.sarthi.Sms.entity.bsp.MacroTestRecord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.Optional;

public interface MacroTestRecordRepository extends JpaRepository<MacroTestRecord, Long> {
    Optional<MacroTestRecord> findTopByOrderByCreatedAtDesc();
    Page<MacroTestRecord> findByCreatedAtBetween(LocalDate startDate, LocalDate endDate, Pageable pageable);
}
