package com.sarthi.SRailPad.repository.inspectionCall;

import com.sarthi.SRailPad.entity.inspectionCall.RailInspectionSchedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;

@Repository
public interface RailInspectionScheduleRepository extends JpaRepository<RailInspectionSchedule, Long> {
    Optional<RailInspectionSchedule> findByCallNo(String callNo);
    long countByScheduleDate(LocalDate scheduleDate);
}
