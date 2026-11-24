package com.sarthi.Sms.service.Impl;

import java.math.BigDecimal;
import java.sql.Date;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import com.sarthi.Sms.dto.sms.common.DutyStatusResDto;
import com.sarthi.Sms.dto.sms.common.EndDutyReqDto;
import com.sarthi.Sms.dto.sms.common.ReportReqDto;
import com.sarthi.Sms.dto.sms.common.StartDutyReqDto;
import com.sarthi.Sms.dto.sms.common.StartDutyResDto;
import com.sarthi.Sms.dto.ndt.CalibrationListDto;
import com.sarthi.Sms.dto.ndt.NdtCalibrationDto;
import com.sarthi.Sms.dto.ndt.NdtCalibrationListResponseDto;
import com.sarthi.Sms.dto.ndt.NdtCalibrationResponseDto;
import com.sarthi.Sms.dto.ndt.NdtReportDto;
import com.sarthi.Sms.dto.ndt.NdtShiftSummaryReqDto;
import com.sarthi.Sms.entity.ndt.NdtCalibrationEntity;
import com.sarthi.Sms.entity.ndt.NdtDutyClbPrevDataEntity;
import com.sarthi.Sms.repository.ndt.NdtCalibrationRepository;
import com.sarthi.Sms.repository.ndt.NdtDutyClbPrevDataRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sarthi.Sms.constant.RailMillAppConstant;
import com.sarthi.Sms.entity.ndt.NdtDutyEntity;
import com.sarthi.Sms.entity.ndt.NdtShiftSummaryEntity;
import com.sarthi.Sms.entity.rolling.RollingDutyEntity;
import com.sarthi.Sms.exception.SmsErrorDetails;
import com.sarthi.Sms.exception.SmsInvalidArgumentException;
import com.sarthi.Sms.exception.SmsResourceNotFoundException;
import com.sarthi.Sms.repository.ndt.NdtDutyRepository;
import com.sarthi.Sms.repository.ndt.NdtShiftSummaryRepository;
import com.sarthi.Sms.service.NdtService;
import com.sarthi.Sms.util.RailMillCommonUtils;
import com.sarthi.Sms.util.DutyEnum;
import com.sarthi.Sms.service.DutySequenceService;
@Service
public class NdtServiceImpl implements NdtService {

    @Autowired
    private NdtDutyRepository ndtDutyRepository;

    @Autowired
    private DutySequenceService dutySequenceService;

    @Autowired
    private NdtCalibrationRepository ndtCalibrationRepository;

    @Autowired
    private NdtShiftSummaryRepository nssr;

    @Autowired
    private NdtDutyClbPrevDataRepository ndcpdr;

    @Override
    @Transactional
    public StartDutyResDto startDuty(String authorizationHeader, StartDutyReqDto req) {
        NdtDutyEntity ndtDutyEntity = new NdtDutyEntity();
        String userId = RailMillCommonUtils.getUserIdFromAuthHeader(authorizationHeader);

        // Check if a duty is already in progress
        DutyStatusResDto dutyStatus = checkDutyStatus(authorizationHeader);
        if (dutyStatus.getIsDutyInProgess()) {
            throw new SmsInvalidArgumentException(new SmsErrorDetails(
                    RailMillAppConstant.USER_ALREADY_EXISTS,
                    RailMillAppConstant.USER_ALREADY_EXISTS,
                    RailMillAppConstant.ERROR_TYPE_VALIDATION,
                    "Cannot start new duty. Previous duty is already in progress"));
        }

        // Generate duty ID and other details
        LocalDate date = RailMillCommonUtils.convertStringToDateObject(req.getStartDate());
        String dutyId = dutySequenceService.generateDutyId(DutyEnum.NDT, date);
        ndtDutyEntity.setDutyId(dutyId);
        ndtDutyEntity.setUserId(userId);
        ndtDutyEntity.setDate(date);
        ndtDutyEntity.setStartTime(LocalDateTime.now());
        ndtDutyEntity.setNdt(req.getNdt());
        ndtDutyEntity.setRailGrade(req.getRailGrade());
        ndtDutyEntity.setShift(req.getShift());
        ndtDutyEntity.setMill(req.getMill());
        ndtDutyEntity.setRailSection(req.getRailSection());

        // Save duty in the database
        ndtDutyEntity = ndtDutyRepository.save(ndtDutyEntity);

        // Prepare response
        StartDutyResDto startDutyResDto = new StartDutyResDto();
        startDutyResDto.setDutyId(ndtDutyEntity.getDutyId());
        startDutyResDto.setStartTime(RailMillCommonUtils.extractTime(ndtDutyEntity.getStartTime()));
        startDutyResDto.setShift(ndtDutyEntity.getShift());
        startDutyResDto.setNdt(ndtDutyEntity.getNdt());
        startDutyResDto.setRailGrade(ndtDutyEntity.getRailGrade());
        startDutyResDto.setRailSection(ndtDutyEntity.getRailSection());
        startDutyResDto.setMill(ndtDutyEntity.getMill());
        startDutyResDto.setDate(RailMillCommonUtils.convertDateToString(ndtDutyEntity.getDate()));
        return startDutyResDto;
    }

    @Override
    @Transactional
    public void endDuty(EndDutyReqDto req) {
        NdtDutyEntity ndtDutyEntity = ndtDutyRepository.findById(req.getDutyId())
                .orElseThrow(() -> new SmsResourceNotFoundException(
                        new SmsErrorDetails(RailMillAppConstant.ERROR_CODE_RESOURCE,
                                RailMillAppConstant.ERROR_TYPE_CODE_RESOURCE,
                                RailMillAppConstant.ERROR_TYPE_RESOURCE,
                                "No duties found for the provided duty ID.")));

        // Check if duty has already ended
        if (Objects.nonNull(ndtDutyEntity.getEndTime())) {
            throw new SmsInvalidArgumentException(new SmsErrorDetails(
                    RailMillAppConstant.ERROR_CODE_VALIDATION,
                    RailMillAppConstant.ERROR_TYPE_CODE_VALIDATION,
                    RailMillAppConstant.ERROR_TYPE_VALIDATION,
                    "Duty has already ended."));
        }

        // Set end time and remarks
        ndtDutyEntity.setEndTime(LocalDateTime.now());
        ndtDutyEntity.setShiftRemarks(req.getShiftRemarks());

        // Save the updated duty entity
        ndtDutyRepository.save(ndtDutyEntity);
    }

    @Override
    public DutyStatusResDto checkDutyStatus(String authorizationHeader) {
        String userId = RailMillCommonUtils.getUserIdFromAuthHeader(authorizationHeader);
        String dutyId = ndtDutyRepository.checkDutyStatus(userId);

        DutyStatusResDto dutyProgressResDto = new DutyStatusResDto();
        dutyProgressResDto.setDutyId(dutyId);
        dutyProgressResDto.setIsDutyInProgess(dutyId != null);

        return dutyProgressResDto;
    }

    @Override
    @Transactional
    public void saveCalibrationList(String authorizationHeader, NdtCalibrationDto requestDto) {
        // Extract user ID from the authorization header
        String userId = RailMillCommonUtils.getUserIdFromAuthHeader(authorizationHeader);

        // Validate duty ID belongs to the user
        NdtDutyEntity ndtDutyEntity = ndtDutyRepository.findById(requestDto.getDutyId())
                .orElseThrow(() -> new SmsInvalidArgumentException(new SmsErrorDetails(
                        RailMillAppConstant.ERROR_CODE_VALIDATION,
                        RailMillAppConstant.ERROR_TYPE_CODE_VALIDATION,
                        RailMillAppConstant.ERROR_TYPE_VALIDATION,
                        "Invalid Duty ID provided.")));

        if (!ndtDutyEntity.getUserId().equals(userId)) {
            throw new SmsInvalidArgumentException(new SmsErrorDetails(
                    RailMillAppConstant.ERROR_CODE_VALIDATION,
                    RailMillAppConstant.ERROR_TYPE_CODE_VALIDATION,
                    RailMillAppConstant.ERROR_TYPE_VALIDATION,
                    "You are not authorized to add calibration for this duty ID."));
        }

        // Iterate through the calibration list and save each item
        List<NdtCalibrationEntity> calibrationList = new ArrayList<>();
        for (CalibrationListDto calibrationItem : requestDto.getCalibrationList()) {
            // Create a new NdtCalibrationEntity for each item
            NdtCalibrationEntity calibrationEntity = new NdtCalibrationEntity();
            calibrationEntity.setDutyId(requestDto.getDutyId());
            calibrationEntity.setCalibrationCompletionTime(RailMillCommonUtils.convertStringToTimeObj(calibrationItem.getCalibrationCompletionTime()));
            calibrationEntity.setCalibrationSpeed(calibrationItem.getCalibrationSpeed());
            calibrationEntity.setRclRepName(calibrationItem.getRclRepName());
            calibrationEntity.setCalibrationRoundRemarks(calibrationItem.getCalibrationRoundRemarks());
            calibrationEntity.setUt(calibrationItem.getUt());
            calibrationEntity.setCt(calibrationItem.getCt());
            calibrationEntity.setFmg(calibrationItem.getFmg());
            calibrationEntity.setOsiris(calibrationItem.getOsiris());
            calibrationList.add(calibrationEntity);


            // Save the entity to the database
        }
        List<NdtCalibrationEntity> savedList = ndtCalibrationRepository.saveAll(calibrationList);

        ndcpdr.deleteByDutyId(requestDto.getDutyId());

        // Map saved calibrations to prev-records and save
        List<NdtDutyClbPrevDataEntity> newPrevData = savedList.stream()
            .map(cal -> new NdtDutyClbPrevDataEntity(null, requestDto.getDutyId(), cal.getCalibrationId()))
            .collect(Collectors.toList());

        ndcpdr.saveAll(newPrevData);
    }

    @Override
    public StartDutyResDto getOngoingDutyDtls(String ah) {
        String userId = RailMillCommonUtils.getUserIdFromAuthHeader(ah);
        Optional<NdtDutyEntity> rdeOpt = ndtDutyRepository.getOngoingDutyDtls(userId);

        StartDutyResDto sdrd = new StartDutyResDto();
        if (rdeOpt.isPresent()) {
            NdtDutyEntity rde = rdeOpt.get();
            sdrd.setDutyId(rde.getDutyId());
            sdrd.setStartTime(RailMillCommonUtils.extractTime(rde.getStartTime()));
            sdrd.setShift(rde.getShift());
            sdrd.setDate(RailMillCommonUtils.convertDateToString(rde.getDate()));
            sdrd.setMill(rde.getMill());
            sdrd.setRailGrade(rde.getRailGrade());
            sdrd.setRailSection(rde.getRailSection());
            sdrd.setNdt(rde.getNdt());
        }

        return sdrd;
    }

    @Override
    @Transactional
    public NdtCalibrationListResponseDto getCalibrationsByDutyId(String dutyId, Boolean prevFilled) {
        List<NdtCalibrationEntity> entities = new ArrayList<>();
        if(prevFilled){
            List<NdtDutyClbPrevDataEntity> prevData = ndcpdr.findByDutyId(dutyId);
            List<Integer> clbIds = prevData.stream().map(NdtDutyClbPrevDataEntity::getCalibrationId).collect(Collectors.toList());
            entities = ndtCalibrationRepository.findByCalibrationIdIn(clbIds);
        }
        else{
            entities = ndtCalibrationRepository.findByDutyId(dutyId);
        }

        // Map entities to response DTOs
        List<NdtCalibrationResponseDto> calibrations = entities.stream().map(entity -> {
            NdtCalibrationResponseDto dto = new NdtCalibrationResponseDto();
            dto.setCalibrationId(entity.getCalibrationId());
            dto.setDutyId(entity.getDutyId());
            dto.setCalibrationCompletionTime(RailMillCommonUtils.convertTimeToString(entity.getCalibrationCompletionTime()));
            dto.setCalibrationSpeed(entity.getCalibrationSpeed());
            dto.setRclRepName(entity.getRclRepName());
            dto.setCalibrationRoundRemarks(entity.getCalibrationRoundRemarks());
            dto.setCt(entity.getCt());
            dto.setUt(entity.getUt());
            dto.setFmg(entity.getFmg());
            dto.setOsiris(entity.getOsiris());

            return dto;
        }).collect(Collectors.toList());

        // Prepare response
        NdtCalibrationListResponseDto response = new NdtCalibrationListResponseDto();
        response.setDutyId(dutyId);
        response.setCalibrations(calibrations);

        return response;
    }

    @Override
    public void saveShiftSummary(NdtShiftSummaryReqDto req) {
        // Create a new entity object
        NdtShiftSummaryEntity sse = new NdtShiftSummaryEntity();

        // Set the values from the DTO to the entity
        sse.setDutyId(req.getDutyId());
        sse.setRollingTableSpeed(req.getRollingTableSpeed());
        sse.setTotalRailsInspected(req.getTotalRailsInspected());
        sse.setTotalUtMarkedRails(req.getTotalUtMarkedRails());
        sse.setTotalEctMarkedRails(req.getTotalEctMarkedRails());
        sse.setBodyStraightnessResultA(req.getBodyStraightnessResultA());
        sse.setBodyStraightnessResultA1(req.getBodyStraightnessResultA1());
        sse.setBodyStraightnessResultMarked(req.getBodyStraightnessResultMarked());
        sse.setTotalProfileMarked(req.getTotalProfileMarked());
        sse.setRailLength(req.getRailLength());
        sse.setTotalUtOkRails(req.getTotalUtOkRails());
        sse.setTotalUtSuspectedRails(req.getTotalUtSuspectedRails());
        sse.setTotalUtRejectedRails(req.getTotalUtRejectedRails());
        sse.setTotalRecycledRails(req.getTotalRecycledRails());
        sse.setEctSuspectedRails(req.getEctSuspectedRails());
        sse.setRemarks(req.getRemarks());

        // Save the entity to the repository
        nssr.save(sse);
    }

    @Override
    public NdtShiftSummaryReqDto getShiftSummary(String dutyId){
        Optional<NdtShiftSummaryEntity> sseOpt = nssr.findById(dutyId);
        NdtShiftSummaryReqDto sse = new NdtShiftSummaryReqDto();
        if(sseOpt.isPresent()){
            NdtShiftSummaryEntity sseEntity = sseOpt.get();
            sse.setDutyId(sseEntity.getDutyId());
            sse.setRollingTableSpeed(sseEntity.getRollingTableSpeed());
            sse.setTotalRailsInspected(sseEntity.getTotalRailsInspected());
            sse.setTotalUtMarkedRails(sseEntity.getTotalUtMarkedRails());
            sse.setTotalEctMarkedRails(sseEntity.getTotalEctMarkedRails());
            sse.setBodyStraightnessResultA(sseEntity.getBodyStraightnessResultA());
            sse.setBodyStraightnessResultA1(sseEntity.getBodyStraightnessResultA1());
            sse.setBodyStraightnessResultMarked(sseEntity.getBodyStraightnessResultMarked());
            sse.setTotalProfileMarked(sseEntity.getTotalProfileMarked());
            sse.setRailLength(sseEntity.getRailLength());
            sse.setTotalUtOkRails(sseEntity.getTotalUtOkRails());
            sse.setTotalUtSuspectedRails(sseEntity.getTotalUtSuspectedRails());
            sse.setTotalUtRejectedRails(sseEntity.getTotalUtRejectedRails());
            sse.setTotalRecycledRails(sseEntity.getTotalRecycledRails());
            sse.setEctSuspectedRails(sseEntity.getEctSuspectedRails());
            sse.setRemarks(sseEntity.getRemarks());
        }
        return sse;
    }

    @Override
    public List<NdtReportDto> getNdtReport(ReportReqDto req){
        LocalDate startDate = RailMillCommonUtils.convertStringToDateObject(req.getStartDate());
        LocalDate endDate = RailMillCommonUtils.convertStringToDateObject(req.getEndDate());

        List<Object[]> objRes = nssr.getNdtReport(startDate, endDate);

        return objRes.stream().map(result -> {
            NdtReportDto nrd = new NdtReportDto();
            nrd.setDate((Date) result[0]);
            nrd.setNdt((String) result[1]);
            nrd.setRailGrade((String) result[2]);
            nrd.setRailSection((String) result[3]);
            nrd.setTotalRailsInspected(result[4] != null ? ((Integer) result[4]).intValue() : 0);
            nrd.setTotalUtMarkedRails(result[5] != null ? ((Integer) result[5]).intValue() : 0);
            nrd.setTotalUtSuspectedRails(result[6] != null ? ((Integer) result[6]).intValue() : 0);
            nrd.setTotalRecycledRails(result[7] != null ? ((Integer) result[7]).intValue() : 0);
            nrd.setTotalUtRejectedRails(result[8] != null ? ((Integer) result[8]).intValue() : 0);
            nrd.setEctSuspectedRails(result[9] != null ? ((Integer) result[9]).intValue() : 0);
            nrd.setBodyStraightnessResultA((Integer) result[10]);
            nrd.setBodyStraightnessResultA1((Integer) result[11]);
            nrd.setBodyStraightnessResultMarked((Integer) result[12]);
            nrd.setTotalProfileMarked(result[13] != null ? ((Integer) result[13]).intValue() : 0);
            nrd.setRailLength((Integer) result[14]);

            return nrd;
        }).collect(Collectors.toList());
    }

}
