package com.sarthi.SRailPad.service.inspectionCall;

import com.sarthi.SRailPad.entity.inspectionCall.RailInspectionCall;
import com.sarthi.SRailPad.entity.inspectionCall.RailInspectionSchedule;
import com.sarthi.SRailPad.repository.inspectionCall.RailInspectionCallRepository;
import com.sarthi.SRailPad.repository.inspectionCall.RailInspectionScheduleRepository;
import com.sarthi.entity.PoHeader;
import com.sarthi.entity.PoItem;
import com.sarthi.entity.UserMaster;
import com.sarthi.repository.PoHeaderRepository;
import com.sarthi.repository.PoItemRepository;
import com.sarthi.repository.RioUserRepository;
import com.sarthi.repository.UserMasterRepository;
import com.sarthi.repository.UserRoleMasterRepository;
import com.sarthi.util.RioUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class RailInspectionScheduleService {

    private final RailInspectionScheduleRepository repository;

    @Autowired(required = false)
    private RailInspectionCallRepository railInspectionCallRepository;
    @Autowired(required = false)
    private PoHeaderRepository poHeaderRepository;
    @Autowired(required = false)
    private PoItemRepository poItemRepository;
    @Autowired(required = false)
    private UserMasterRepository userMasterRepository;
    @Autowired(required = false)
    private UserRoleMasterRepository userRoleMasterRepository;
    @Autowired(required = false)
    private RioUserRepository rioUserRepository;
    @Autowired(required = false)
    private JdbcTemplate jdbcTemplate;

    private void validateRailScheduleRio(String callNo, String actorStr) {
        if (callNo == null || callNo.isBlank() || railInspectionCallRepository == null || poHeaderRepository == null) {
            return;
        }
        RailInspectionCall ic = railInspectionCallRepository.findByCallNo(callNo.trim()).orElse(null);
        if (ic == null || ic.getPoNo() == null || ic.getPoNo().isBlank()) {
            return;
        }
        PoHeader poHeader = poHeaderRepository.findFirstByPoNoOrL5PoNo(ic.getPoNo().trim()).orElse(null);
        String caseNo = poHeader != null ? poHeader.getCaseNo() : null;
        if ((caseNo == null || caseNo.trim().isEmpty()) && poHeader != null && poItemRepository != null) {
            List<PoItem> items = poItemRepository.findByPoHeader_Id(poHeader.getId());
            if (items != null) {
                String targetSr = ic.getPoSr() != null ? ic.getPoSr().trim() : "";
                caseNo = items.stream()
                        .filter(i -> i.getItemSrNo() != null && i.getItemSrNo().trim().equalsIgnoreCase(targetSr))
                        .map(PoItem::getCaseNo)
                        .filter(c -> c != null && !c.trim().isEmpty())
                        .findFirst()
                        .orElse(items.stream()
                                .map(PoItem::getCaseNo)
                                .filter(c -> c != null && !c.trim().isEmpty())
                                .findFirst()
                                .orElse(null));
            }
        }
        UserMaster actorUser = null;
        if (actorStr != null && !actorStr.isBlank() && userMasterRepository != null) {
            try {
                actorUser = userMasterRepository.findByUserId(Integer.valueOf(actorStr.trim())).orElse(null);
            } catch (NumberFormatException ignored) {
                actorUser = userMasterRepository.findFirstByEmployeeCode(actorStr.trim()).orElse(null);
            }
        }
        RioUtil.validateIeCaseRioAccess(
                RioUtil.getAuthenticatedUser(),
                actorUser,
                userRoleMasterRepository,
                jdbcTemplate,
                rioUserRepository,
                caseNo,
                callNo
        );
    }

    public RailInspectionSchedule schedule(RailInspectionSchedule schedule) {
        validateRailScheduleRio(schedule.getCallNo(), schedule.getCreatedBy());
        return repository.save(schedule);
    }

    public RailInspectionSchedule reschedule(RailInspectionSchedule schedule) {
        String actor = (schedule.getUpdatedBy() != null && !schedule.getUpdatedBy().isBlank())
                ? schedule.getUpdatedBy() : schedule.getCreatedBy();
        validateRailScheduleRio(schedule.getCallNo(), actor);
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
