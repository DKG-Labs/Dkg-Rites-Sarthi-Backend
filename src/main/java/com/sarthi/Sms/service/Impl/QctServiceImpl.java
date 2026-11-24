package com.sarthi.Sms.service.Impl;

import com.sarthi.Sms.constant.RailMillAppConstant;
import com.sarthi.Sms.dto.sms.common.DutyStatusResDto;
import com.sarthi.Sms.dto.sms.common.EndDutyReqDto;
import com.sarthi.Sms.dto.sms.common.ReportReqDto;
import com.sarthi.Sms.dto.sms.common.StartDutyReqDto;
import com.sarthi.Sms.dto.sms.common.StartDutyResDto;
import com.sarthi.Sms.dto.qct.*;
import com.sarthi.Sms.entity.qct.*;
import com.sarthi.Sms.entity.rolling.RollingDutyEntity;
import com.sarthi.Sms.exception.SmsErrorDetails;
import com.sarthi.Sms.exception.SmsInvalidArgumentException;
import com.sarthi.Sms.exception.SmsResourceNotFoundException;
import com.sarthi.Sms.repository.qct.*;
import com.sarthi.Sms.service.QctService;
import com.sarthi.Sms.service.SmsService;
import com.sarthi.Sms.util.RailMillCommonUtils;
import com.sarthi.Sms.util.DutyEnum;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.sarthi.Sms.util.RailMillCommonUtils;
import com.sarthi.Sms.util.DutyEnum;
import com.sarthi.Sms.service.DutySequenceService;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class QctServiceImpl implements QctService {
    @Autowired
    private QctDutyRepository qctDutyRepository;

    @Autowired
    private DutySequenceService dutySequenceService;

    @Autowired
    private QctMasterRepository qmr;

    @Autowired
    private QctDetailRepository qctDetailRepository;

    @Autowired
    private QctTestingMasterRepository qctTestingMasterRepository;

    @Autowired
    private QctTestingDetailRepository qctTestingDetailRepository;

    @Autowired
    private QctFatigueMasterRepository qfmr;

    @Autowired
    private QctFatigueDetailRepository qfdr;

    @Autowired
    private QctResidualMasterRepository qrmr;

    @Autowired
    private QctFcgrMasterRepository qfgrm;

    @Autowired
    private QctFractureToughnessMasterReopsitory qftmr;

    @Autowired
    private QctCenterLineMasterRepository qclmr;

    @Autowired
    private SmsService ss;

    @Override
    @Transactional
    public StartDutyResDto startDuty(String authorizationHeader, StartDutyReqDto req) {
        QctDutyEntity qctDutyEntity = new QctDutyEntity();
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
        String dutyId = dutySequenceService.generateDutyId(DutyEnum.QCT, date);
        qctDutyEntity.setDutyId(dutyId);
        qctDutyEntity.setUserId(userId);
        qctDutyEntity.setDate(date);
        qctDutyEntity.setStartTime(LocalDateTime.now());
        qctDutyEntity.setShift(req.getShift());

        // Save duty in the database
        qctDutyRepository.save(qctDutyEntity);

        // Prepare response
        StartDutyResDto startDutyResDto = new StartDutyResDto();
        startDutyResDto.setDate(RailMillCommonUtils.convertDateToString(qctDutyEntity.getDate()));
        startDutyResDto.setDutyId(qctDutyEntity.getDutyId());
        startDutyResDto.setStartTime(RailMillCommonUtils.extractTime(qctDutyEntity.getStartTime()));
        startDutyResDto.setShift(qctDutyEntity.getShift());

        return startDutyResDto;
    }

    @Override
    public DutyStatusResDto checkDutyStatus(String authorizationHeader) {
        String userId = RailMillCommonUtils.getUserIdFromAuthHeader(authorizationHeader);
        String dutyId = qctDutyRepository.checkDutyStatus(userId);

        DutyStatusResDto dutyProgressResDto = new DutyStatusResDto();
        dutyProgressResDto.setDutyId(dutyId);
        dutyProgressResDto.setIsDutyInProgess(dutyId != null);

        return dutyProgressResDto;
    }

    @Override
    @Transactional
    public void endDuty(EndDutyReqDto req) {
        QctDutyEntity qctDutyEntity = qctDutyRepository.findById(req.getDutyId())
                .orElseThrow(() -> new SmsResourceNotFoundException(
                        new SmsErrorDetails(RailMillAppConstant.ERROR_CODE_RESOURCE,
                                RailMillAppConstant.ERROR_TYPE_CODE_RESOURCE,
                                RailMillAppConstant.ERROR_TYPE_RESOURCE,
                                "No duties found for the provided duty ID.")));

        // Check if duty has already ended
        if (Objects.nonNull(qctDutyEntity.getEndTime())) {
            throw new SmsInvalidArgumentException(new SmsErrorDetails(
                    RailMillAppConstant.ERROR_CODE_VALIDATION,
                    RailMillAppConstant.ERROR_TYPE_CODE_VALIDATION,
                    RailMillAppConstant.ERROR_TYPE_VALIDATION,
                    "Duty has already ended."));
        }

        // Set end time and remarks
        qctDutyEntity.setEndTime(LocalDateTime.now());
        qctDutyEntity.setShiftRemarks(req.getShiftRemarks());

        // Save the updated duty entity
        qctDutyRepository.save(qctDutyEntity);
    }

    @Override
    public StartDutyResDto getOngoingDutyDtls(String ah) {
        String userId = RailMillCommonUtils.getUserIdFromAuthHeader(ah);
        Optional<QctDutyEntity> rdeOpt = qctDutyRepository.getOngoingDutyDtls(userId);

        StartDutyResDto sdrd = new StartDutyResDto();
        if (rdeOpt.isPresent()) {
            QctDutyEntity rde = rdeOpt.get();
            sdrd.setDate(RailMillCommonUtils.convertDateToString(rde.getDate()));
            sdrd.setShift(rde.getShift());
            // sdrd.setMill(rde.getMill());
            // sdrd.setRailGrade(rde.getRailGrade());
            // sdrd.setRailSection(rde.getRailSection());
            sdrd.setDutyId(rde.getDutyId());
            sdrd.setStartTime(RailMillCommonUtils.extractTime(rde.getStartTime()));
        }

        return sdrd;
    }

    // @Override
    // @Transactional
    // public void saveQctInspection(String authorizationHeader,
    // SaveQctInspectionReqDto requestDto) {
    // // Extract user ID from authorization header
    // String userId = RailMillCommonUtils.getUserIdFromAuthHeader(authorizationHeader);

    // // Validate duty ID
    // QctDutyEntity dutyEntity = qctDutyRepository.findById(requestDto.getDutyId())
    // .orElseThrow(() -> new SmsInvalidArgumentException(new SmsErrorDetails(
    // RailMillAppConstant.ERROR_CODE_VALIDATION,
    // RailMillAppConstant.ERROR_TYPE_CODE_VALIDATION,
    // RailMillAppConstant.ERROR_TYPE_VALIDATION,
    // "Invalid Duty ID provided."
    // )));

    // if (!dutyEntity.getUserId().equals(userId)) {
    // throw new SmsInvalidArgumentException(new SmsErrorDetails(
    // RailMillAppConstant.ERROR_CODE_VALIDATION,
    // RailMillAppConstant.ERROR_TYPE_CODE_VALIDATION,
    // RailMillAppConstant.ERROR_TYPE_VALIDATION,
    // "You are not authorized to add QCT inspection for this duty ID."
    // ));
    // }

    // // Save QCT master
    // QctMasterEntity master = new QctMasterEntity();
    // QctMasterId masterkey =new QctMasterId();
    // masterkey.setDutyId(requestDto.getDutyId());
    // masterkey.setQctType(requestDto.getQctType());

    // master.setId(masterkey);
    // master.setMill(requestDto.getMill());
    // master.setRailSection(requestDto.getRailSection());
    // master.setRailGrade(requestDto.getRailGrade());
    // master.setMonthYear(RailMillCommonUtils.convertStringToDateObject(requestDto.getMonthYear()));

    // master.setNoOfSamples(requestDto.getNoOfSamples());
    // master.setRemark(requestDto.getRemark());

    // master = qctMasterRepository.save(master);

    // // Save QCT detail records based on the number of samples
    // for (int i = 0; i < requestDto.getNoOfSamples(); i++) {
    // QctDetailDto detailDto = requestDto.getDetails().get(i);

    // QctDetailId detailKey = new QctDetailId();
    // detailKey.setHeatNo(detailDto.getHeatNo());
    // detailKey.setStrandNo(detailDto.getStrandNo());
    // detailKey.setSampleId(detailDto.getSampleId());
    // detailKey.setQctType(detailDto.getQctType());

    // QctDetailEntity detail = new QctDetailEntity();
    // detail.setDutyId(requestDto.getDutyId());
    // detail.setId(detailKey);
    // detail.setCreatedAt(LocalDateTime.now());

    // qctDetailRepository.save(detail);
    // }
    // }

    // @Override
    // @Transactional
    // public GetQctInspectionResDto getQctInspection(String dutyId) {
    // // Validate duty ID
    // QctDutyEntity dutyEntity = qctDutyRepository.findById(dutyId)
    // .orElseThrow(() -> new SmsInvalidArgumentException(new SmsErrorDetails(
    // RailMillAppConstant.ERROR_CODE_VALIDATION,
    // RailMillAppConstant.ERROR_TYPE_CODE_VALIDATION,
    // RailMillAppConstant.ERROR_TYPE_VALIDATION,
    // "Invalid Duty ID provided."
    // )));

    // // Fetch QCT Master

    // QctMasterEntity masterEntity = qctMasterRepository.findByIdDutyId(dutyId)
    // .orElseThrow(() -> new SmsResourceNotFoundException(new SmsErrorDetails(
    // RailMillAppConstant.ERROR_CODE_RESOURCE,
    // RailMillAppConstant.ERROR_TYPE_CODE_RESOURCE,
    // RailMillAppConstant.ERROR_TYPE_RESOURCE,
    // "No QCT Master data found for the provided Duty ID."
    // )));

    // // Fetch QCT Details
    // List<QctDetailEntity> detailEntities =
    // qctDetailRepository.findByDutyId(dutyId);

    // // Map QCT Master to Response DTO
    // GetQctInspectionResDto responseDto = new GetQctInspectionResDto();
    // responseDto.setDutyId(masterEntity.getId().getDutyId());
    // responseDto.setMill(masterEntity.getMill());
    // responseDto.setRailSection(masterEntity.getRailSection());
    // responseDto.setRailGrade(masterEntity.getRailGrade());
    // responseDto.setMonthYear(RailMillCommonUtils.convertDateToString(masterEntity.getMonthYear()));
    // responseDto.setQctType(masterEntity.getId().getQctType());
    // responseDto.setNoOfSamples(masterEntity.getNoOfSamples());
    // responseDto.setRemark(masterEntity.getRemark());

    // // Map QCT Details to Response DTO
    // List<QctDetailDto> detailDtos = detailEntities.stream().map(detail -> {
    // QctDetailDto detailDto = new QctDetailDto();
    // detailDto.setHeatNo(detail.getId().getHeatNo());
    // detailDto.setStrandNo(detail.getId().getStrandNo());
    // detailDto.setSampleId(detail.getId().getSampleId());
    // detailDto.setQctType(detail.getId().getQctType());
    // detailDto.setCreatedAt(RailMillCommonUtils.extractTime(detail.getCreatedAt()));
    // return detailDto;
    // }).collect(Collectors.toList());

    // responseDto.setDetails(detailDtos);

    // return responseDto;
    // }

    // @Override
    // @Transactional
    // public List<GetQctInspectionResDto> getAllQctInspections() {
    // // Fetch all QCT Master records
    // List<QctMasterEntity> masterEntities = qctMasterRepository.findAll();

    // // Map each QCT Master record to its response DTO
    // List<GetQctInspectionResDto> responseDtos =
    // masterEntities.stream().map(masterEntity -> {
    // // Fetch corresponding QCT Detail records
    // List<QctDetailEntity> detailEntities =
    // qctDetailRepository.findByDutyId(masterEntity.getId().getDutyId());

    // // Map QCT Master to Response DTO
    // GetQctInspectionResDto responseDto = new GetQctInspectionResDto();
    // responseDto.setDutyId(masterEntity.getId().getDutyId());
    // responseDto.setMill(masterEntity.getMill());
    // responseDto.setRailSection(masterEntity.getRailSection());
    // responseDto.setRailGrade(masterEntity.getRailGrade());
    // responseDto.setMonthYear(RailMillCommonUtils.convertDateToString(masterEntity.getMonthYear()));
    // responseDto.setQctType(masterEntity.getId().getQctType());
    // responseDto.setNoOfSamples(masterEntity.getNoOfSamples());
    // responseDto.setRemark(masterEntity.getRemark());

    // // Map QCT Details to Response DTO
    // List<QctDetailDto> detailDtos = detailEntities.stream().map(detail -> {
    // QctDetailDto detailDto = new QctDetailDto();
    // detailDto.setHeatNo(detail.getId().getHeatNo());
    // detailDto.setStrandNo(detail.getId().getStrandNo());
    // detailDto.setSampleId(detail.getId().getSampleId());
    // detailDto.setQctType(detail.getId().getQctType());
    // detailDto.setCreatedAt(RailMillCommonUtils.extractTime(detail.getCreatedAt()));
    // return detailDto;
    // }).collect(Collectors.toList());

    // responseDto.setDetails(detailDtos);

    // return responseDto;
    // }).collect(Collectors.toList());

    // return responseDtos;
    // }

    // @Override
    // @Transactional
    // public void saveQctTestingMaster(String authorizationHeader,
    // QctTestingMasterDto requestDto) {
    // // Extract user ID from authorization header
    // String userId = RailMillCommonUtils.getUserIdFromAuthHeader(authorizationHeader);

    // // Validate duty ID
    // QctDutyEntity dutyEntity = qctDutyRepository.findById(requestDto.getDutyId())
    // .orElseThrow(() -> new SmsInvalidArgumentException(new SmsErrorDetails(
    // RailMillAppConstant.ERROR_CODE_VALIDATION,
    // RailMillAppConstant.ERROR_TYPE_CODE_VALIDATION,
    // RailMillAppConstant.ERROR_TYPE_VALIDATION,
    // "Invalid Duty ID provided."
    // )));

    // // Map DTO to Entity
    // QctTestingMasterEntity entity = new QctTestingMasterEntity();
    // QctTestingMasterId id = new QctTestingMasterId();
    // id.setDutyId(requestDto.getDutyId());
    // id.setQctType(requestDto.getQctType());

    // entity.setId(id);
    // entity.setStrain(requestDto.getStrain());
    // entity.setFrequencyHz(requestDto.getFrequencyHz());
    // entity.setStress(requestDto.getStress());
    // entity.setCycleEndured(requestDto.getCycleEndured());
    // entity.setRemarks(requestDto.getRemarks());
    // entity.setNumberOfSamplesReportedAsKq(requestDto.getNumberOfSamplesReportedAsKq());
    // entity.setMeanOfValuesKq(requestDto.getMeanOfValuesKq());

    // // Save Entity
    // qctTestingMasterRepository.save(entity);
    // }

    // @Override
    // @Transactional
    // public void saveQctTestingDetail(String authorizationHeader,
    // QctTestingDetailDto requestDto) {
    // // Validate if the related QCT Testing Master exists
    // QctTestingMasterId masterId = new QctTestingMasterId();
    // masterId.setDutyId(requestDto.getDutyId());
    // masterId.setQctType(requestDto.getQctType());

    // qctTestingMasterRepository.findById(masterId)
    // .orElseThrow(() -> new SmsInvalidArgumentException(new SmsErrorDetails(
    // RailMillAppConstant.ERROR_CODE_VALIDATION,
    // RailMillAppConstant.ERROR_TYPE_CODE_VALIDATION,
    // RailMillAppConstant.ERROR_TYPE_VALIDATION,
    // "Invalid Duty ID or QCT Type provided."
    // )));

    // // Map DTO to Entity
    // QctTestingDetailId detailId = new QctTestingDetailId();
    // detailId.setHeatNo(requestDto.getHeatNo());
    // detailId.setStrandNo(requestDto.getStrandNo());
    // detailId.setSampleId(requestDto.getSampleId());
    // detailId.setQctType(requestDto.getQctType());

    // QctTestingDetailEntity entity = new QctTestingDetailEntity();
    // entity.setId(detailId);
    // entity.setDutyId(requestDto.getDutyId());

    // entity.setFatiqueResult(requestDto.getFatiqueResult());
    // entity.setStressMpa(requestDto.getStressMpa());
    // entity.setNatureOfStress(requestDto.getNatureOfStress());
    // entity.setFcgrDaDn10(requestDto.getFcgrDaDn10());
    // entity.setFcgrDaDn13_5(requestDto.getFcgrDaDn13_5());
    // entity.setValueReportedAs(requestDto.getValueReportedAs());
    // entity.setValueOfReportedParameter(requestDto.getValueOfReportedParameter());
    // entity.setNoOfHardness(requestDto.getNoOfHardness());
    // entity.setMeanRsh(requestDto.getMeanRsh());
    // entity.setMxVariation(requestDto.getMxVariation());

    // // Save Entity
    // qctTestingDetailRepository.save(entity);
    // }

    @Override
    @Transactional
    public void saveNewTestSample(QctNewTestSampleDto req) {

        // Ensure the provided duty ID exists in qct_duty table
        ensureQctDutyExists(req.getDutyId());

        // Validate that heat number has reached "Bloom Cutting" stage in SMS
        ss.validateHeatBloomStage(req.getHeatNo());

        QctMasterEntity qme = new QctMasterEntity();
        qme.setCreatedAt(LocalDateTime.now());
        qme.setDutyId(req.getDutyId());
        qme.setHeatNo(req.getHeatNo());
        qme.setQctType(req.getQctType());
        qme.setSampleId(req.getSampleId());
        qme.setStrandNo(req.getStrandNo());
        qme.setMill(req.getMill());
        qme.setRailGrade(req.getRailGrade());
        qme.setRailSection(req.getRailSection());

        qmr.save(qme);
    }

    /**
     * Ensures that the provided duty ID exists in qct_duty table
     * Creates the duty record if it doesn't exist
     */
    private void ensureQctDutyExists(String dutyId) {
        try {
            // Check if duty already exists
            Optional<QctDutyEntity> existingDuty = qctDutyRepository.findByDutyId(dutyId);

            if (existingDuty.isEmpty()) {
                // Create the duty record with provided duty ID
                QctDutyEntity qctDutyEntity = new QctDutyEntity();
                qctDutyEntity.setDutyId(dutyId);
                qctDutyEntity.setUserId("SYSTEM"); // System-generated duty
                qctDutyEntity.setDate(LocalDate.now());
                qctDutyEntity.setStartTime(LocalDateTime.now());
                qctDutyEntity.setShift("A"); // Default shift

                qctDutyRepository.save(qctDutyEntity);
                System.out.println("Created QCT duty record for provided duty ID: " + dutyId);
            }
        } catch (Exception e) {
            System.out.println("Warning: Could not ensure QCT duty exists for ID: " + dutyId + ". Error: " + e.getMessage());
            // Continue execution - let the foreign key constraint handle it
        }
    }

    @Transactional
    @Override
    public void updateTestSample(QctNewTestSampleDto req) {
        // Validate that heat number has reached "Bloom Cutting" stage in SMS
        ss.validateHeatBloomStage(req.getHeatNo());

        QctMasterEntity qme = qmr.findById(req.getQctId()).orElseThrow(() -> new SmsInvalidArgumentException(new SmsErrorDetails(
            RailMillAppConstant.ERROR_CODE_VALIDATION,
            RailMillAppConstant.ERROR_TYPE_CODE_VALIDATION,
            RailMillAppConstant.ERROR_TYPE_VALIDATION,
            "Invalid QCT ID provided."
        )));

        qme.setHeatNo(req.getHeatNo());
        qme.setMill(req.getMill());
        qme.setRailGrade(req.getRailGrade());
        qme.setRailSection(req.getRailSection());
        qme.setStrandNo(req.getStrandNo());
        qme.setSampleId(req.getSampleId());
        qme.setQctType(req.getQctType());
        qme.setDutyId(req.getDutyId());
        qmr.save(qme);
    }

    @Transactional
    @Override
    public void deleteSample(Integer qctId) {
        qmr.deleteById(qctId);
    }

    @Override
    @Transactional
    public void saveFatigue(QctFatigueDto req) {
        QctFatigueMasterEntity qfme = new QctFatigueMasterEntity();
        qfme.setCycleEndured(req.getCyclesEndured());
        qfme.setDutyId(req.getDutyId());
        qfme.setFrequency(req.getFrequency());
        qfme.setRemarks(req.getRemarks());
        qfmr.save(qfme);

        for (QctFatigueHeatDtlDto subReq : req.getDtlList()) {
            QctFatigueDetailEntity qfde = new QctFatigueDetailEntity();
            qfde.setDutyId(req.getDutyId());
            qfde.setIsBroken(subReq.getIsBroken());
            qfde.setQctId(subReq.getId());

            qfdr.save(qfde);
        }
    }

    @Transactional
    @Override
    public void saveResidual(QctResidualDto req) {

        for (QctResidualDtlDto subReq : req.getDtlList()) {
            QctResidualMasterEntity qrme = new QctResidualMasterEntity();
            qrme.setDutyId(req.getDutyId());
            qrme.setQctId(subReq.getId());
            qrme.setRemarks(req.getRemarks());
            qrme.setStress(subReq.getStress());
            qrme.setStressNature(subReq.getStressNature());

            qrmr.save(qrme);

        }
    }

    @Override
    @Transactional
    public void saveFcgr(QctFcgrDto req) {
        for (QctFcgrDtlDto subReq : req.getDtlList()) {
            QctFcgrMasterEntity qfme = new QctFcgrMasterEntity();
            qfme.setMpam10(subReq.getMpam10());
            qfme.setMpam13(subReq.getMpam13());
            qfme.setQctDuty(req.getDutyId());
            qfme.setQctId(subReq.getId());
            qfme.setRemarks(req.getRemarks());

            qfgrm.save(qfme);
        }
    }

    @Override
    public List<PendingTestSummaryDto> getPendingTestSummary() {
        List<Object[]> results = qmr.findPendingTests();
        return results.stream().map(row -> new PendingTestSummaryDto(
                (String) row[0], // qctType
                (String) row[1], // mill
                (String) row[2], // railGrade
                (String) row[3], // railSection
                (String) row[4], // sampleMonth
                String.valueOf(row[5]) // sampleCount (convert from Number to String)
        )).collect(Collectors.toList());
    }

    @Override
    public List<QctDtlResDto> getQctDtlByDutyId(String dutyId) {
        List<Object[]> res = qmr.findQctDtlByDutyId(dutyId);

        return res.stream()
                .map(result -> new QctDtlResDto(
                        (String) result[0], // heatNo
                        (Integer) result[1], // strandNo
                        (String) result[2], // sampleId
                        (String) result[3], // mill
                        (String) result[4], // railGrade
                        (String) result[5], // railSection
                        (String) result[6], // sampleMonth
                        (String) result[7], // qctType
                        (Integer) result[8] // qctId
                ))
                .collect(Collectors.toList()); // Collect the results into a List
    }

    @Override
    public QctDtlResDto getQctDtlByQctId(Integer qctId) {
        List<Object[]> result = qmr.findQctDtlByQctId(qctId);

        if(result.size() < 1){
            throw new SmsResourceNotFoundException(
                new SmsErrorDetails(RailMillAppConstant.ERROR_CODE_RESOURCE,
                        RailMillAppConstant.ERROR_TYPE_CODE_RESOURCE,
                        RailMillAppConstant.ERROR_TYPE_RESOURCE,
                        "No detail found for the provided id."));
        }

        Object[] resObj = result.get(0);

        return new QctDtlResDto(
                (String) resObj[0], // heatNo
                (Integer) resObj[1], // strandNo
                (String) resObj[2], // sampleId
                (String) resObj[3], // mill
                (String) resObj[4], // railGrade
                (String) resObj[5], // railSection
                (String) resObj[6], // sampleMonth
                (String) resObj[7], // qctType
                (Integer) qctId // qctId
        );
    }

    @Override
    @Transactional
    public void saveFractureToughness(QctFractureToughnessDto req){
        for(QctFractureToughnessDtlDto subReq : req.getDtlList()){
            QctFractureToughnessMasterEntity qftme = new QctFractureToughnessMasterEntity();
            qftme.setDutyId(req.getDutyId());
            qftme.setParameter(subReq.getParameter());
            qftme.setValue(subReq.getValue());
            qftme.setQctId(subReq.getId());
            qftme.setRemarks(req.getRemarks());

            qftmr.save(qftme);

        }
    }
    @Override
    public void saveCenterLine(QctCenterLineDto req){
        for(QctCenterLineDtlDto subReq : req.getDtlList()){
            QctCenterLineMasterEntity qclme = new QctCenterLineMasterEntity();
            qclme.setHardnessReadingCount(subReq.getHardnessReadingCount());
            qclme.setMaxVariation(subReq.getMaxVariation());
            qclme.setMeanRsh(subReq.getMeanRsh());
            qclme.setDutyId(req.getDutyId());
            qclme.setRemarks(req.getRemarks());
            qclme.setQctId(subReq.getId());
            // qftme.setDutyId(req.getDutyId());
            // qftme.setParameter(subReq.getParameter());
            // qftme.setValue(subReq.getValue());
            // qftme.setQctId(subReq.getId());
            // qftme.setRemarks(req.getRemarks());

            qclmr.save(qclme);

        }
    }

    @Override
    public List<QctRecordDto> getQctRecord(ReportReqDto req){
        String mmYYYY = LocalDate.parse(req.getStartDate(), DateTimeFormatter.ofPattern("dd/MM/yyyy")).format(DateTimeFormatter.ofPattern("MM-yyyy"));
        List<Object[]> results = qmr.findQctRecord(mmYYYY);


        List<QctRecordDto> dtos = new ArrayList<>();

        for (Object[] result : results) {
            QctRecordDto dto = new QctRecordDto();
            dto.setMill((String) result[0]);
            dto.setRailGrade((String) result[1]);
            dto.setRailSection((String) result[2]);
            dto.setHeatNo((String) result[3]);
            dto.setStrandNo((Integer) result[4]);
            dto.setSampleId((String) result[5]);
            dto.setFatigueInfo((String) result[6]);
            dto.setResidualInfo((String) result[7]);
            dto.setFcgrInfo((String) result[8]);
            dto.setFractureToughnessInfo((String) result[9]);
            dto.setCenterlineInfo((String) result[10]);
            dtos.add(dto);
        }

        return dtos;

    }
}
