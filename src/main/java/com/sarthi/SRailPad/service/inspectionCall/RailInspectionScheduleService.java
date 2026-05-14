package com.sarthi.SRailPad.service.inspectionCall;

import com.sarthi.SRailPad.entity.inspectionCall.RailInspectionSchedule;
import com.sarthi.SRailPad.repository.inspectionCall.RailInspectionScheduleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class RailInspectionScheduleService {

    private final RailInspectionScheduleRepository repository;

    public RailInspectionSchedule schedule(RailInspectionSchedule schedule) {
        return repository.save(schedule);
    }

    public RailInspectionSchedule reschedule(RailInspectionSchedule schedule) {
        Optional<RailInspectionSchedule> existing = repository.findByCallNo(schedule.getCallNo());
        if (existing.isPresent()) {
            RailInspectionSchedule current = existing.get();
            current.setScheduleDate(schedule.getScheduleDate());
            current.setReason(schedule.getReason());
            current.setUpdatedBy(schedule.getUpdatedBy());
            return repository.save(current);
        }
        return repository.save(schedule);
    }

    public Optional<RailInspectionSchedule> getByCallNo(String callNo) {
        return repository.findByCallNo(callNo);
    }

    public List<RailInspectionSchedule> getAll() {
        return repository.findAll();
    }

    public long getCountByDate(LocalDate date) {
        return repository.countByScheduleDate(date);
    }

    public void deleteByCallNo(String callNo) {
        repository.findByCallNo(callNo).ifPresent(repository::delete);
    }
}
