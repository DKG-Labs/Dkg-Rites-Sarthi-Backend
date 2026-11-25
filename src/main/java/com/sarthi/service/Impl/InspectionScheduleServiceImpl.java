package com.sarthi.service.Impl;

import com.sarthi.constant.AppConstant;
import com.sarthi.dto.InspectionScheduleDto;
import com.sarthi.entity.InspectionSchedule;
import com.sarthi.entity.PoHeader;
import com.sarthi.entity.PoItem;
import com.sarthi.entity.UserMaster;
import com.sarthi.entity.rawmaterial.InspectionCall;
import com.sarthi.exception.BusinessException;
import com.sarthi.exception.ErrorDetails;
import com.sarthi.exception.InvalidInputException;
import com.sarthi.repository.IeProfileRepository;
import com.sarthi.repository.InspectionScheduleRepository;
import com.sarthi.repository.PoHeaderRepository;
import com.sarthi.repository.PoItemRepository;
import com.sarthi.repository.RioUserRepository;
import com.sarthi.repository.UserMasterRepository;
import com.sarthi.repository.UserRoleMasterRepository;
import com.sarthi.repository.rawmaterial.InspectionCallRepository;
import com.sarthi.service.InspectionScheduleService;
import com.sarthi.util.NotificationService;
import com.sarthi.util.RioUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service implementation for Inspection Schedule operations.
 */
@Service
public class InspectionScheduleServiceImpl implements InspectionScheduleService {

    private static final Logger logger = LoggerFactory.getLogger(InspectionScheduleServiceImpl.class);
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @Autowired
    private InspectionScheduleRepository scheduleRepository;
    @Autowired
    private NotificationService notificationService;
    @Autowired(required = false)
    private InspectionCallRepository inspectionCallRepository;
    @Autowired(required = false)
    private PoHeaderRepository poHeaderRepository;
    @Autowired(required = false)
    private PoItemRepository poItemRepository;
    @Autowired(required = false)
    private UserMasterRepository userMasterRepository;
    @Autowired(required = false)
    private UserRoleMasterRepository userRoleMasterRepository;
    @Autowired(required = false)
    private IeProfileRepository ieProfileRepository;
    @Autowired(required = false)
    private RioUserRepository rioUserRepository;

    private void validateScheduleIeRio(String callNo, String actorStr) {
        if (callNo == null || callNo.isBlank() || inspectionCallRepository == null || poHeaderRepository == null) {
            return;
        }
        InspectionCall ic = inspectionCallRepository.findByIcNumber(callNo.trim()).orElse(null);
        if (ic == null || ic.getPoNo() == null || ic.getPoNo().isBlank()) {
            return;
        }
        PoHeader ph = poHeaderRepository.findFirstByPoNoOrL5PoNo(ic.getPoNo().trim()).orElse(null);
        String caseNo = ph != null ? ph.getCaseNo() : null;
        if ((caseNo == null || caseNo.trim().isEmpty()) && ph != null && poItemRepository != null && ic.getPoSerialNo() != null) {
            List<PoItem> items = poItemRepository.findByPoHeader_Id(ph.getId());
            if (items != null) {
                caseNo = items.stream()
                        .filter(i -> i.getItemSrNo() != null && i.getItemSrNo().trim().equalsIgnoreCase(ic.getPoSerialNo().trim()))
                        .map(PoItem::getCaseNo)
                        .filter(c -> c != null && !c.trim().isEmpty())
                        .findFirst()
                        .orElse(null);
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
                ieProfileRepository,
                rioUserRepository,
                caseNo,
                callNo
        );
    }

    @Override
    @Transactional
    public InspectionScheduleDto scheduleInspection(InspectionScheduleDto scheduleDto) {
        logger.info("Scheduling inspection for call: {}", scheduleDto.getCallNo());

        if (scheduleDto.getCallNo() == null || scheduleDto.getCallNo().isEmpty()) {
            throw new InvalidInputException(new ErrorDetails(
                    AppConstant.INVALID_INPUT, AppConstant.ERROR_TYPE_CODE_VALIDATION,
                    AppConstant.ERROR_TYPE_VALIDATION, "Call number is required"));
        }

        if (scheduleDto.getScheduleDate() == null || scheduleDto.getScheduleDate().isEmpty()) {
            throw new InvalidInputException(new ErrorDetails(
                    AppConstant.INVALID_INPUT, AppConstant.ERROR_TYPE_CODE_VALIDATION,
                    AppConstant.ERROR_TYPE_VALIDATION, "Schedule date is required"));
        }

        // Check if already scheduled
        if (scheduleRepository.existsByCallNo(scheduleDto.getCallNo())) {
            throw new BusinessException(new ErrorDetails(
                    AppConstant.RECORD_ALREADY_EXIST, AppConstant.ERROR_TYPE_CODE_VALIDATION,
                    AppConstant.ERROR_TYPE_VALIDATION,
                    "Inspection already scheduled for call: " + scheduleDto.getCallNo()));
        }

        validateScheduleIeRio(scheduleDto.getCallNo(), scheduleDto.getCreatedBy());

        InspectionSchedule schedule = new InspectionSchedule();
        schedule.setCallNo(scheduleDto.getCallNo());
        schedule.setScheduleDate(LocalDate.parse(scheduleDto.getScheduleDate(), DATE_FORMATTER));
        schedule.setReason(scheduleDto.getReason());
        schedule.setStatus("Scheduled");
        schedule.setCreatedBy(scheduleDto.getCreatedBy());

        InspectionSchedule saved = scheduleRepository.save(schedule);
        logger.info("Inspection scheduled successfully for call: {}", scheduleDto.getCallNo());
        String productType = "ERC";
        notificationService.sendInspectionScheduledNotification(
                productType,
                scheduleDto.getCallNo(),
                Integer.valueOf(scheduleDto.getCreatedBy())
        );
        return mapToDto(saved);
    }

    @Override
    @Transactional
    public InspectionScheduleDto rescheduleInspection(InspectionScheduleDto scheduleDto) {
        logger.info("Rescheduling inspection for call: {}", scheduleDto.getCallNo());

        InspectionSchedule schedule = scheduleRepository.findByCallNo(scheduleDto.getCallNo())
                .orElseThrow(() -> new BusinessException(new ErrorDetails(
                        AppConstant.NO_RECORD_FOUND, AppConstant.ERROR_TYPE_CODE_VALIDATION,
                        AppConstant.ERROR_TYPE_VALIDATION,
                        "No schedule found for call: " + scheduleDto.getCallNo())));

        String actorStr = scheduleDto.getUpdatedBy() != null && !scheduleDto.getUpdatedBy().isBlank()
                ? scheduleDto.getUpdatedBy() : scheduleDto.getCreatedBy();
        validateScheduleIeRio(scheduleDto.getCallNo(), actorStr);

        schedule.setScheduleDate(LocalDate.parse(scheduleDto.getScheduleDate(), DATE_FORMATTER));
        schedule.setReason(scheduleDto.getReason());
        schedule.setStatus("Rescheduled");
        schedule.setUpdatedBy(scheduleDto.getUpdatedBy());

        InspectionSchedule saved = scheduleRepository.save(schedule);
        logger.info("Inspection rescheduled successfully for call: {}", scheduleDto.getCallNo());

        return mapToDto(saved);
    }

    @Override
    public InspectionScheduleDto getScheduleByCallNo(String callNo) {
        logger.info("Fetching schedule for call: {}", callNo);

        return scheduleRepository.findByCallNo(callNo)
                .map(this::mapToDto)
                .orElse(null);
    }

    @Override
    public List<InspectionScheduleDto> getAllSchedules() {
        logger.info("Fetching all schedules");
        return scheduleRepository.findAllByOrderByScheduleDateDesc()
                .stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    public long getScheduleCountByDate(String date) {
        LocalDate scheduleDate = LocalDate.parse(date, DATE_FORMATTER);
        return scheduleRepository.countByScheduleDate(scheduleDate);
    }

    @Override
    @Transactional
    public void deleteScheduleByCallNo(String callNo) {
        logger.info("Deleting schedule for call: {}", callNo);
        scheduleRepository.deleteByCallNo(callNo);
    }

    private InspectionScheduleDto mapToDto(InspectionSchedule entity) {
        InspectionScheduleDto dto = new InspectionScheduleDto();
        dto.setId(entity.getId());
        dto.setCallNo(entity.getCallNo());
        dto.setScheduleDate(entity.getScheduleDate() != null ? entity.getScheduleDate().toString() : null);
        dto.setReason(entity.getReason());
        dto.setStatus(entity.getStatus());
        dto.setCreatedBy(entity.getCreatedBy());
        dto.setCreatedAt(entity.getCreatedAt() != null ? entity.getCreatedAt().toString() : null);
        dto.setUpdatedBy(entity.getUpdatedBy());
        dto.setUpdatedAt(entity.getUpdatedAt() != null ? entity.getUpdatedAt().toString() : null);
        return dto;
    }
}

