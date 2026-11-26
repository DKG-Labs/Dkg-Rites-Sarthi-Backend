package com.sarthi.Sms.service.Impl;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.sarthi.Sms.dto.rolling.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.sarthi.Sms.constant.RailMillAppConstant;
import com.sarthi.Sms.dto.sms.common.DutyStatusResDto;
import com.sarthi.Sms.dto.sms.common.EndDutyReqDto;
import com.sarthi.Sms.dto.sms.common.StartDutyReqDto;
import com.sarthi.Sms.dto.sms.common.StartDutyResDto;
import com.sarthi.Sms.dto.rolling.ControlHeatReqDto;
import com.sarthi.Sms.dto.rolling.ControlReqDto;
import com.sarthi.Sms.dto.rolling.ControlSampleDtlReqDto;
import com.sarthi.Sms.dto.rolling.FinishingVerificationReqDto;
import com.sarthi.Sms.dto.rolling.HeatAcptTestDtlReqDto;
import com.sarthi.Sms.dto.rolling.HotStampingDto;
import com.sarthi.Sms.dto.rolling.HtRailDeleteDto;
import com.sarthi.Sms.dto.rolling.HtSequenceRailIdDto;
import com.sarthi.Sms.dto.rolling.HtSequenceReqDto;
import com.sarthi.Sms.dto.rolling.HtSequenceResDto;
import com.sarthi.Sms.dto.rolling.PreCamberingDto;
import com.sarthi.Sms.dto.rolling.RailRollingResDto;
import com.sarthi.Sms.dto.rolling.RollingHeatResDto;
import com.sarthi.Sms.dto.rolling.RollingVerificationDto;
import com.sarthi.Sms.dto.rolling.SaveBatchReqDto;
import com.sarthi.Sms.dto.rolling.SaveRailDtlsReqDto;
import com.sarthi.Sms.dto.rolling.SaveRetestSampleReqDto;
import com.sarthi.Sms.dto.rolling.TempObservationDto;
import com.sarthi.Sms.dto.rolling.TestSampleAcptDto;
import com.sarthi.Sms.dto.rolling.TestSampleAcptEntity;
import com.sarthi.Sms.dto.rolling.TestSampleAcptId;
import com.sarthi.Sms.dto.rolling.TestSampleDto;
import com.sarthi.Sms.dto.rolling.VerificationBloomYardDto;
import com.sarthi.Sms.dto.rolling.VerificationChargingDto;
import com.sarthi.Sms.entity.rolling.AcptTestRelationEntity;
import com.sarthi.Sms.entity.rolling.AcptTestRelationId;
import com.sarthi.Sms.entity.rolling.FinishingVerificationHotStampingEntity;
import com.sarthi.Sms.entity.rolling.RetestSample;
import com.sarthi.Sms.entity.rolling.FinishingVerificationPreCamberingEntity;
import com.sarthi.Sms.entity.rolling.FinishingVerificationStraignteningMachineEntity;
import com.sarthi.Sms.entity.rolling.HtSequenceEntity;
import com.sarthi.Sms.entity.rolling.HtSequenceHeatEntity;
import com.sarthi.Sms.entity.rolling.HtSequenceHeatRailEntity;
import com.sarthi.Sms.entity.rolling.RetestSample;
import com.sarthi.Sms.entity.rolling.RollingDutyEntity;
import com.sarthi.Sms.entity.rolling.RollingHeatDtlEntity;
import com.sarthi.Sms.entity.rolling.RollingHeatDtlId;
import com.sarthi.Sms.entity.rolling.TemperatureObservationEntity;
import com.sarthi.Sms.entity.rolling.TemperatureObservationId;
import com.sarthi.Sms.entity.rolling.VerificationBloomYardEntity;
import com.sarthi.Sms.entity.rolling.VerificationChargingEntity;
import com.sarthi.Sms.entity.rolling.HtSequenceEntity.BloomQuality;
import com.sarthi.Sms.exception.SmsErrorDetails;
import com.sarthi.Sms.exception.SmsInvalidArgumentException;
import com.sarthi.Sms.exception.SmsResourceNotFoundException;
import com.sarthi.Sms.repository.rolling.AcptTestRelationRepository;
import com.sarthi.Sms.repository.rolling.FinishingVerificationHotStampingRepository;
import com.sarthi.Sms.repository.rolling.FinishingVerificationPreCamberingRepository;
import com.sarthi.Sms.repository.rolling.FinishingVerificationStraighteningMachineRepository;
import com.sarthi.Sms.repository.rolling.HtSequenceHeatRailRepository;
import com.sarthi.Sms.repository.rolling.HtSequenceHeatRepository;
import com.sarthi.Sms.repository.rolling.HtSequenceRepository;
import com.sarthi.Sms.repository.rolling.RetestSampleRepository;
import com.sarthi.Sms.repository.rolling.RollingDutyRepository;
import com.sarthi.Sms.repository.rolling.RollingHeatDtlRepository;
import com.sarthi.Sms.repository.rolling.TemperatureObservationRepository;
import com.sarthi.Sms.repository.rolling.TestSampleAcptRepository;
import com.sarthi.Sms.repository.rolling.VerificationBloomYardRepository;
import com.sarthi.Sms.repository.rolling.VerificationChargingRepository;
import com.sarthi.Sms.service.DutySequenceService;
import com.sarthi.Sms.service.RollingService;
import com.sarthi.Sms.service.SmsService;
import com.sarthi.Sms.util.RailMillCommonUtils;
import com.sarthi.Sms.util.DutyEnum;
import com.sarthi.Sms.dto.sms.DeleteHeatReqDto;
import jakarta.transaction.Transactional;

@Service
public class RollingServiceImpl implements RollingService {

    @Autowired
    RollingDutyRepository rdr;

    @Autowired
    DutySequenceService dutySequenceService;

    @Autowired
    SmsService smsService;

    @Autowired
    com.sarthi.Sms.repository.sms.HeatDtlSms2Repository hds2r;

    @Autowired
    com.sarthi.Sms.repository.sms.HeatDtlSms3Repository hds3r;

    @Autowired
    RollingHeatDtlRepository rhdr;

    @Autowired
    VerificationBloomYardRepository vbyr;

    @Autowired
    VerificationChargingRepository vcr;

    @Autowired
    TemperatureObservationRepository tor;

    @Autowired
    FinishingVerificationHotStampingRepository fvhsr;

    @Autowired
    FinishingVerificationPreCamberingRepository fvpcr;

    @Autowired
    FinishingVerificationStraighteningMachineRepository fvsmr;

    @Autowired
    TestSampleAcptRepository tsar;

    @Autowired
    AcptTestRelationRepository atrr;

    @Autowired
    RetestSampleRepository rsr;

    @Autowired
    HtSequenceHeatRepository htshr;

    @Autowired
    HtSequenceHeatRailRepository htshrr;

    @Autowired
    HtSequenceRepository htsr;

    @Autowired
    RetestSampleRepository retestSampleRepository;

    @Override
    @Transactional
    public StartDutyResDto startDuty(String authHeader, StartDutyReqDto startDutyReqDto) {
        RollingDutyEntity rollingDutyEntity = new RollingDutyEntity();
        String userId = RailMillCommonUtils.getUserIdFromAuthHeader(authHeader);

        // check if any other duty is already in progess
        DutyStatusResDto dutyStatusResDto = checkDutyStatus(authHeader);
        if (dutyStatusResDto.getIsDutyInProgess()) {
            throw new SmsInvalidArgumentException(new SmsErrorDetails(RailMillAppConstant.USER_ALREADY_EXISTS,
                    RailMillAppConstant.USER_ALREADY_EXISTS, RailMillAppConstant.ERROR_TYPE_VALIDATION,
                    "Cannot start new duty for the user. Previous duty already in progress"));
        }

        LocalDate date = RailMillCommonUtils.convertStringToDateObject(startDutyReqDto.getStartDate());
        com.sarthi.Sms.util.RailMillDutyRules.validate(date, startDutyReqDto.getShift(),
                startDutyReqDto.getOrganisation(), startDutyReqDto.getMill());

        String dutyId = dutySequenceService.generateDutyId(DutyEnum.ROLLING, date);

        rollingDutyEntity.setDutyId(dutyId);
        rollingDutyEntity.setUserId(userId);
        rollingDutyEntity.setDate(date);
        rollingDutyEntity.setShift(startDutyReqDto.getShift());
        rollingDutyEntity.setMill(startDutyReqDto.getMill());
        rollingDutyEntity.setOrganisation(startDutyReqDto.getOrganisation().trim().toUpperCase());
        rollingDutyEntity.setRailGrade(startDutyReqDto.getRailGrade());
        rollingDutyEntity.setRailSection(startDutyReqDto.getRailSection());
        rollingDutyEntity.setStartTime(LocalDateTime.now());
        rollingDutyEntity = rdr.save(rollingDutyEntity);

        StartDutyResDto startDutyResDto = new StartDutyResDto();
        startDutyResDto.setDate(RailMillCommonUtils.convertDateToString(rollingDutyEntity.getDate()));
        startDutyResDto.setDutyId(rollingDutyEntity.getDutyId());
        startDutyResDto.setShift(rollingDutyEntity.getShift());
        startDutyResDto.setMill(rollingDutyEntity.getMill());
        startDutyResDto.setOrganisation(rollingDutyEntity.getOrganisation());
        startDutyResDto.setStartTime(RailMillCommonUtils.extractTime(rollingDutyEntity.getStartTime()));
        startDutyResDto.setRailGrade(rollingDutyEntity.getRailGrade());
        startDutyResDto.setRailSection(rollingDutyEntity.getRailSection());

        return startDutyResDto;
    }

    @Override
    @Transactional
    public void endDuty(EndDutyReqDto endDutyReqDto) {
        RollingDutyEntity rollingDutyEntity = rdr.findByDutyId(endDutyReqDto.getDutyId())
                .orElseThrow(() -> new SmsResourceNotFoundException(
                        new SmsErrorDetails(RailMillAppConstant.ERROR_CODE_RESOURCE,
                                RailMillAppConstant.ERROR_TYPE_CODE_RESOURCE,
                                RailMillAppConstant.ERROR_TYPE_RESOURCE,
                                "No duties found for the provided duty id.")));

        if (Objects.nonNull(rollingDutyEntity.getEndTime())) {
            throw new SmsInvalidArgumentException(
                    new SmsErrorDetails(
                            RailMillAppConstant.ERROR_CODE_VALIDATION,
                            RailMillAppConstant.ERROR_TYPE_CODE_VALIDATION,
                            RailMillAppConstant.ERROR_TYPE_VALIDATION,
                            "Duty already ended."));
        }
        rollingDutyEntity.setShiftRemarks(endDutyReqDto.getShiftRemarks());
        rollingDutyEntity.setEndTime(LocalDateTime.now());
        rdr.save(rollingDutyEntity);
    }

    @Override
    public DutyStatusResDto checkDutyStatus(String authHeader) {
        String userId = RailMillCommonUtils.getUserIdFromAuthHeader(authHeader);
        String dutyId = rdr.checkDutyStatus(userId);
        DutyStatusResDto dutyStatusResDto = new DutyStatusResDto();
        dutyStatusResDto.setDutyId(dutyId);
        dutyStatusResDto.setIsDutyInProgess(dutyId != null);
        return dutyStatusResDto;
    }

    @Override
    public StartDutyResDto getOngoingDutyDtls(String ah) {
        String userId = RailMillCommonUtils.getUserIdFromAuthHeader(ah);
        Optional<RollingDutyEntity> rdeOpt = rdr.getOngoingDutyDtls(userId);

        StartDutyResDto sdrd = new StartDutyResDto();
        if (rdeOpt.isPresent()) {
            RollingDutyEntity rde = rdeOpt.get();
            sdrd.setDate(RailMillCommonUtils.convertDateToString(rde.getDate()));
            sdrd.setShift(rde.getShift());
            sdrd.setMill(rde.getMill());
            sdrd.setOrganisation(rde.getOrganisation());
            sdrd.setRailGrade(rde.getRailGrade());
            sdrd.setRailSection(rde.getRailSection());
            sdrd.setDutyId(rde.getDutyId());
            sdrd.setStartTime(RailMillCommonUtils.extractTime(rde.getStartTime()));
        }

        return sdrd;
    }

    @Override
    public RailRollingResDto getRollingControlDtls(String dutyId) {
        // find ems functioning etc details from SmsDutyEntity
        RollingDutyEntity rollingDutyEntity = rdr.findByDutyId(dutyId)
                .orElseThrow(() -> new SmsResourceNotFoundException(
                        new SmsErrorDetails(RailMillAppConstant.ERROR_CODE_RESOURCE,
                                RailMillAppConstant.ERROR_TYPE_CODE_RESOURCE,
                                RailMillAppConstant.ERROR_TYPE_RESOURCE,
                                "No duties found for the provided duty id.")));

        List<RollingHeatDtlEntity> rollingHeatDtlEntity = rhdr.findByDutyId(dutyId);

        List<RollingHeatResDto> rollingHeatResDtoList = rollingHeatDtlEntity.stream()
                .map(res -> {
                    RollingHeatResDto rollingHeatResDto = new RollingHeatResDto();
                    rollingHeatResDto.setHeatNo(res.getRollingHeatDtlId().getHeatNo());
                    rollingHeatResDto.setSampleNo(res.getRollingHeatDtlId().getSampleNo());
                    rollingHeatResDto.setResult(res.getRemark());
                    rollingHeatResDto.setTiming(RailMillCommonUtils.convertTimeToString(res.getTiming()));
                    return rollingHeatResDto;
                }).collect(Collectors.toList());

        RailRollingResDto railRollingResDto = new RailRollingResDto();
        railRollingResDto.setBranding(rollingDutyEntity.getBranding());
        railRollingResDto.setMicrometerNo(rollingDutyEntity.getMicrometerNo());
        railRollingResDto
                .setMicrometerValidity(RailMillCommonUtils.convertDateToString(rollingDutyEntity.getMicrometerValidity()));
        railRollingResDto.setNoOfGauges(rollingDutyEntity.getNoOfGauges());
        railRollingResDto.setRailRollingHeatList(rollingHeatResDtoList);
        railRollingResDto.setVernierNo(rollingDutyEntity.getVernierNo());
        railRollingResDto.setVernierValidity(RailMillCommonUtils.convertDateToString(rollingDutyEntity.getVernierValidity()));
        railRollingResDto.setWeighingMachine(rollingDutyEntity.getWeighingMachine());
        railRollingResDto.setWeighingMachineValidity(
                RailMillCommonUtils.convertDateToString(rollingDutyEntity.getWeighingMachineValidity()));

        return railRollingResDto;
    }

    @Override
    public ControlHeatReqDto getControlSampleDtl(ControlSampleDtlReqDto req) {
        RollingHeatDtlEntity rhde = rhdr
                .findByRollingHeatDtlId(new RollingHeatDtlId(req.getHeatNo(), req.getSampleNo()))
                .orElseThrow(() -> new SmsResourceNotFoundException(
                        new SmsErrorDetails(RailMillAppConstant.ERROR_CODE_RESOURCE,
                                RailMillAppConstant.ERROR_TYPE_CODE_RESOURCE,
                                RailMillAppConstant.ERROR_TYPE_RESOURCE,
                                "No detail found for the provided heat number and sample number")));

        ControlHeatReqDto chrd = new ControlHeatReqDto();
        chrd.setDutyId(rhde.getDutyId());
        chrd.setHeatNo(rhde.getRollingHeatDtlId().getHeatNo());
        chrd.setSampleNo(rhde.getRollingHeatDtlId().getSampleNo());
        chrd.setTiming(RailMillCommonUtils.convertTimeToString(rhde.getTiming()));
        chrd.setSampleLocation(rhde.getSampleLocation());
        chrd.setHeight(rhde.getHeight());
        chrd.setHead(rhde.getHead());
        chrd.setFlange(rhde.getFlange());
        chrd.setWeb(rhde.getWeb());
        chrd.setAsy(rhde.getAsy());
        chrd.setFootToe(rhde.getFootToe());
        chrd.setFishingHeight(rhde.getFishingHeight());
        chrd.setWeight(rhde.getWeight());
        chrd.setCrownProfile(rhde.getCrownProfile());
        chrd.setFootFlatness(rhde.getFootFlatness());
        chrd.setRemark(rhde.getRemark());

        return chrd;
    }

    @Override
    @Transactional
    public void saveControlHeat(ControlHeatReqDto req) {

        // Validate that heat number has reached "Bloom Cutting" stage in SMS
        validateHeatBloomStage(req.getHeatNo());


        Optional<RollingHeatDtlEntity> rhdeOpt = rhdr.findById(new RollingHeatDtlId(req.getHeatNo(), req.getSampleNo()));

        if(rhdeOpt.isPresent()){
            throw new SmsInvalidArgumentException(
                        new SmsErrorDetails(
                                RailMillAppConstant.ERROR_CODE_VALIDATION,
                                RailMillAppConstant.ERROR_TYPE_CODE_VALIDATION,
                                RailMillAppConstant.ERROR_TYPE_VALIDATION,
                                "Rolling Control already done for the heat no. : " + req.getHeatNo()));
        }



        RollingHeatDtlEntity rhde = new RollingHeatDtlEntity();
        rhde.setDutyId(req.getDutyId());
        rhde.setRollingHeatDtlId(
                new RollingHeatDtlId(req.getHeatNo(),
                        req.getSampleNo()));
        rhde.setTiming(RailMillCommonUtils.convertStringToTimeObj(req.getTiming()));
        rhde.setSampleLocation(req.getSampleLocation());
        rhde.setHeight(req.getHeight());
        rhde.setHead(req.getHead());
        rhde.setFlange(req.getFlange());
        rhde.setWeb(req.getWeb());
        rhde.setAsy(req.getAsy());
        rhde.setFootToe(req.getFootToe());
        rhde.setFishingHeight(req.getFishingHeight());
        rhde.setWeight(req.getWeight());
        rhde.setCrownProfile(req.getCrownProfile());
        rhde.setFootFlatness(req.getFootFlatness());
        rhde.setRemark(req.getRemark());
        rhdr.save(rhde);
    }

    @Override
    @Transactional
    public void deleteControlHeat(ControlHeatReqDto req){
        rhdr.deleteById(new RollingHeatDtlId(req.getHeatNo(), req.getSampleNo()));
    }

    @Override
    @Transactional
    public void saveControlDtls(ControlReqDto req) {
        RollingDutyEntity rde = rdr.findByDutyId(req.getDutyId())
                .orElseThrow(() -> new SmsResourceNotFoundException(
                        new SmsErrorDetails(RailMillAppConstant.ERROR_CODE_RESOURCE,
                                RailMillAppConstant.ERROR_TYPE_CODE_RESOURCE,
                                RailMillAppConstant.ERROR_TYPE_RESOURCE,
                                "No duties found for the provided duty id.")));

        rde.setDutyId(req.getDutyId());
        rde.setMicrometerNo(req.getMicrometerNo());
        rde.setVernierNo(req.getVernierNo());
        rde.setWeighingMachine(req.getWeighingMachine());
        rde.setMicrometerValidity(RailMillCommonUtils.convertStringToDateObject(req.getMicrometerValidity()));
        rde.setVernierValidity(RailMillCommonUtils.convertStringToDateObject(req.getVernierValidity()));
        rde.setWeighingMachineValidity(RailMillCommonUtils.convertStringToDateObject(req.getWeighingMachineValidity()));
        rde.setBranding(req.getBranding());
        rde.setNoOfGauges(req.getNoOfGauges());

        rdr.save(rde);
    }

    @Override
    @Transactional
    public void saveRollingVerification(RollingVerificationDto req) {
        for (VerificationBloomYardDto vbydd : req.getBloomYardDtls()) {
            vbydd.setDutyId(req.getDutyId());
            vbydd.setRemarks(req.getRemarks());
            saveBloomYardDtls(vbydd);
        }
        for (VerificationChargingDto vcd : req.getChargingTableDtls()) {
            vcd.setDutyId(req.getDutyId());
            vcd.setRemarks(req.getRemarks());
            saveChargingDtls(vcd);
        }
        for (TempObservationDto tod : req.getTempObservationDtls()) {
            tod.setRemarks(req.getRemarks());
            tod.setDutyId(req.getDutyId());
            saveTempObservationDtls(tod);
        }
    }

    @Override
    @Transactional
    public void saveBloomYardDtls(VerificationBloomYardDto req) {
        String heatNo = req.getHeatNo();

        
        heatNo = String.format("%06d", Integer.parseInt(heatNo));
        Optional<VerificationBloomYardEntity> vbyeOpt = vbyr.findById(new BloomVerificationId(heatNo, req.getDutyId()));

        if(vbyeOpt.isPresent()){
                            throw new SmsInvalidArgumentException(
                        new SmsErrorDetails(
                                RailMillAppConstant.ERROR_CODE_VALIDATION,
                                RailMillAppConstant.ERROR_TYPE_CODE_VALIDATION,
                                RailMillAppConstant.ERROR_TYPE_VALIDATION,
                                "Bloom yard verification already completed for heat no. " + heatNo + " in this duty. Same heat can be used in different duties."));
        }

        // Validate that heat number has reached "Bloom Cutting" stage in SMS
        validateHeatBloomStage(heatNo);


        VerificationBloomYardEntity vbye = new VerificationBloomYardEntity();
        vbye.setId(new BloomVerificationId(heatNo, req.getDutyId()));
        vbye.setInternalTransferVerification(req.getInternalTransferVerification());
        vbye.setHeatRegisterVerification(req.getHeatRegisterVerification());
        vbye.setLength(req.getLength());
        vbye.setDefect(req.getDefect());
        vbye.setRemarks(req.getRemarks());

        vbyr.save(vbye);
    }
    @Override
    @Transactional
    public void saveChargingDtls(VerificationChargingDto req) {

        String heatNo = req.getHeatNo();
        heatNo = String.format("%06d", Integer.parseInt(heatNo));

        // Validate that heat number has reached "Bloom Cutting" stage in SMS
        validateHeatBloomStage(heatNo);

        Optional<VerificationChargingEntity> vceOpt = vcr.findById(heatNo);

        if(vceOpt.isPresent()){
                            throw new SmsInvalidArgumentException(
                        new SmsErrorDetails(
                                RailMillAppConstant.ERROR_CODE_VALIDATION,
                                RailMillAppConstant.ERROR_TYPE_CODE_VALIDATION,
                                RailMillAppConstant.ERROR_TYPE_VALIDATION,
                                "Charging Verification already done for the heat no. : " + heatNo));
        }

        VerificationChargingEntity vce = new VerificationChargingEntity();

        vce.setDutyId(req.getDutyId());
        vce.setHeatNo(heatNo);
        vce.setLength(req.getLength());
        vce.setDefect(req.getDefect());
        vce.setBloomTrackingVerification(req.getBloomTrackingVerification());
        vce.setHotBloomsDescaling(req.getHotBloomsDescaling());
        vce.setRemarks(req.getRemarks());

        vcr.save(vce);
    }

    @Override
    @Transactional
    public void saveTempObservationDtls(TempObservationDto req) {
        TemperatureObservationEntity toe = new TemperatureObservationEntity();

        toe.setFinishingTemp(req.getFinishingTemp());
        toe.setId(
                new TemperatureObservationId(req.getDutyId(), RailMillCommonUtils.convertStringToTimeObj(req.getCreatedAt())));
        toe.setSoakingZoneTemp(req.getSoakingZoneTemp());
        toe.setRemarks(req.getRemarks());

        tor.save(toe);
    }

    // @Override
    // public List<TempObservationReqResDto> getTempObservationDtls(String dutyId) {
    // List<TemperatureObservationEntity> toeLst = tor.findByIdDutyId(dutyId);

    // List<TempObservationReqResDto> res = toeLst.stream().map(toe -> {
    // TempObservationReqResDto torrd = new TempObservationReqResDto();
    // torrd.setCreatedAt(toe.getId().getCreatedAt());
    // torrd.setDutyId(toe.getId().getDutyId());
    // torrd.setFinishingTemp(toe.getFinishingTemp());
    // torrd.setSoakingZoneTemp(toe.getSoakingZoneTemp());

    // return torrd;
    // }).collect(Collectors.toList());

    // return res;

    // }

    @Override
    @Transactional
    public void saveFinishingVerification(FinishingVerificationReqDto req) {
        FinishingVerificationStraignteningMachineEntity fvsme = new FinishingVerificationStraignteningMachineEntity();

        for (HotStampingDto subReq : req.getHotStampingList()) {
            // Validate that heat number has reached "Bloom Cutting" stage in SMS
            validateHeatBloomStage(subReq.getHeatNo());
            FinishingVerificationHotStampingEntity fvhse = new FinishingVerificationHotStampingEntity();
            fvhse.setBrand(subReq.getBranding());
            fvhse.setDutyId(req.getDutyId());
            fvhse.setHeatNo(subReq.getHeatNo());
            fvhsr.save(fvhse);
        }

        for (PreCamberingDto subReq : req.getPreCamberingList()) {
            // Validate that heat number has reached "Bloom Cutting" stage in SMS
            validateHeatBloomStage(subReq.getHeatNo());
            FinishingVerificationPreCamberingEntity fvpce = new FinishingVerificationPreCamberingEntity();
            fvpce.setDutyId(req.getDutyId());
            fvpce.setHeatNo(subReq.getHeatNo());
            fvpce.setIsSatisfactory(subReq.getIsSatisfactory());
            fvpcr.save(fvpce);
        }

        fvsme.setDutyId(req.getDutyId());
        fvsme.setStraigtnessRemarks(req.getStraightnessRemarks());
        fvsme.setRemarks(req.getRemarks());

        fvsme.setSmR1Hrsm(req.getSmR1Hrsm());
        fvsme.setSmR4Hrsm(req.getSmR4Hrsm());
        fvsme.setSmR6Hrsm(req.getSmR6Hrsm());
        fvsme.setSmR8Hrsm(req.getSmR8Hrsm());
        fvsme.setSmR1Vrsm(req.getSmR1Vrsm());
        fvsme.setSmR4Vrsm(req.getSmR4Vrsm());
        fvsme.setSmR6Vrsm(req.getSmR6Vrsm());
        fvsme.setSmR8Vrsm(req.getSmR8Vrsm());
        fvsme.setSmIsSatisfactory(req.getSmIsSatisfactory());

        fvsme.setSmSrR1Hrsm(req.getSmSrR1Hrsm());
        fvsme.setSmSrR2Hrsm(req.getSmSrR2Hrsm());
        fvsme.setSmSrR3Hrsm(req.getSmSrR3Hrsm());
        fvsme.setSmSrR4Hrsm(req.getSmSrR4Hrsm());
        fvsme.setSmSrR1Vrsm(req.getSmSrR1Vrsm());
        fvsme.setSmSrR2Vrsm(req.getSmSrR2Vrsm());
        fvsme.setSmSrR3Vrsm(req.getSmSrR3Vrsm());
        fvsme.setSmSrR4Vrsm(req.getSmSrR4Vrsm());
        fvsme.setSmSrIsSatisfactory(req.getSmSrIsSatisfactory());

        fvsme.setSmLrR1Hrsm(req.getSmLrR1Hrsm());
        fvsme.setSmLrR2Hrsm(req.getSmLrR2Hrsm());
        fvsme.setSmLrR3Hrsm(req.getSmLrR3Hrsm());
        fvsme.setSmLrR4Hrsm(req.getSmLrR4Hrsm());
        fvsme.setSmLrR1Vrsm(req.getSmLrR1Vrsm());
        fvsme.setSmLrR2Vrsm(req.getSmLrR2Vrsm());
        fvsme.setSmLrR3Vrsm(req.getSmLrR3Vrsm());
        fvsme.setSmLrR4Vrsm(req.getSmLrR4Vrsm());
        fvsme.setSmLrIsSatisfactory(req.getSmLrIsSatisfactory());

        fvsmr.save(fvsme);
    }

    @Override
    public TestSampleAcptDto getHeatAcptTestDtls(HeatAcptTestDtlReqDto req) {

        List<TestSampleAcptEntity> tsaeList  = new ArrayList<>();
        if(Objects.nonNull(req.getSampleId()) && !req.getSampleId().isEmpty()){
            // System.out.println("NON NULL");
            tsaeList  = tsar.findBySampleId(req.getSampleId());

        }
        else{
            tsaeList = tsar.findDtl(req.getHeatNo(), req.getStrand(), req.getSampleType(), req.getSampleLot());
        }
        TestSampleAcptDto res = new TestSampleAcptDto();
        res.setSampleId(req.getSampleId());
        tsaeList.stream().forEach(tsae -> {
            if (tsae.getChemical()) {
                res.setChemical(true);
            }
            if (tsae.getN2()) {
                res.setN2(true);
            }
            if (tsae.getFwtSt()) {
                res.setFwtSt(true);
            }
            if (tsae.getMechanical()) {
                res.setMechanical(true);
            }
            if (tsae.getSp()) {
                res.setSp(true);
            }
            if (tsae.getIr()) {
                res.setIr(true);
            }
            if (tsae.getO2()) {
                res.setO2(true);
            }
            if (tsae.getFwtHs()) {
                res.setFwtHs(true);
            }
            if (tsae.getFwtStSr()) {
                res.setFwtStSr(true);
            }
            if (tsae.getTensileFoot()) {
                res.setTensileFoot(true);
            }
            if (tsae.getPh()) {
                res.setPh(true);
            }
            if (tsae.getTensile()) {
                res.setTensile(true);
            }
            if (tsae.getRsh()) {
                res.setRsh(true);
            }
            if (tsae.getMicro()) {
                res.setMicro(true);
            }
            if (tsae.getDecarb()) {
                res.setDecarb(true);
            }
        });

        // TestSampleAcptDto tsad = new TestSampleAcptDto();
        // if (tsaeOpt.isPresent()) {
        //     TestSampleAcptEntity tsae = tsaeOpt.get();
        //     tsad.setRailGrade(tsae.getRailGrade());
        //     tsad.setSampleType(tsae.getTsai().getSampleType());
        //     tsad.setSampleId(tsae.getTsai().getSampleId());
        //     tsad.setHeatNo(tsae.getTsai().getHeatNo());
        //     tsad.setStrand(tsae.getTsai().getStrand());
        //     tsad.setSampleLot(tsae.getTsai().getSampleLot());
        //     tsad.setChemical(tsae.getChemical());
        //     tsad.setN2(tsae.getN2());
        //     tsad.setFwtSt(tsae.getFwtSt());
        //     tsad.setMechanical(tsae.getMechanical());
        //     tsad.setSp(tsae.getSp());
        //     tsad.setIr(tsae.getIr());
        //     tsad.setO2(tsae.getO2());
        //     tsad.setFwtHs(tsae.getFwtHs());
        //     tsad.setFwtStSr(tsae.getFwtStSr());
        //     tsad.setTensileFoot(tsae.getTensileFoot());
        //     tsad.setPh(tsae.getPh());
        //     tsad.setTensile(tsae.getTensile());
        //     tsad.setRsh(tsae.getRsh());
        //     tsad.setMicro(tsae.getMicro());
        //     tsad.setDecarb(tsae.getDecarb());
        // }

        return res;
    }

    @Override
    @Transactional
    public void saveAcceptanceTestSample(TestSampleAcptDto req) {

        // Validate that heat number has reached "Bloom Cutting" stage in SMS
        validateHeatBloomStage(req.getHeatNo());

        // check if sample id ( rail id ) is present in HT Sequence
        if (req.getSampleId() != "" && Objects.nonNull(req.getSampleId())) {
            HtSequenceEntity htse = htsr.findById(req.getSampleId())
                    .orElseThrow(() -> new SmsResourceNotFoundException(
                            new SmsErrorDetails(RailMillAppConstant.ERROR_CODE_RESOURCE,
                                    RailMillAppConstant.ERROR_TYPE_CODE_RESOURCE,
                                    RailMillAppConstant.ERROR_TYPE_RESOURCE,
                                    "Sample ID not declared in HT Sequence.")));
        }

        // get previously marked tests for the sample id
        List<TestSampleAcptEntity> tsaeList = new ArrayList<>();
        if(Objects.nonNull(req.getSampleId()) && !req.getSampleId().isEmpty()){
            tsaeList = tsar.findBySampleId(req.getSampleId());
        }
        else{
            tsaeList = tsar.findDtl(req.getHeatNo(), req.getStrand(), req.getSampleType(), req.getSampleLot());
        }

        // validate whether there were any tests marked earlier and tried to be marked
        // again
        validateMarkedTests(req, tsaeList);

        // find the entity with composite key
        List<TestSampleAcptEntity> tsaeOpt = tsar.findByCompositeKey(
            req.getSampleId(), req.getHeatNo(), req.getSampleType(),
            req.getStrand(), req.getSampleLot());

        // if present mark the new true tests 
        if(tsaeOpt.size() > 0){
            TestSampleAcptEntity tsae = tsaeOpt.get(0);
            StringBuilder markedTests = new StringBuilder();
            updateTestSample(tsae, req, markedTests);
            tsae = tsar.save(tsae);

            // update / create relation entity
            Optional<AcptTestRelationEntity> atreOpt = atrr
                    .findByAtri(new AcptTestRelationId(req.getDutyId(), tsae.getId()));

                    if (atreOpt.isPresent()) {
                        String prevMarkedTest = atreOpt.get().getTestsMarked();
                        if (prevMarkedTest != null && !prevMarkedTest.isEmpty()) {
                            markedTests.append(", "); // Add a separator if necessary
                            markedTests.append(prevMarkedTest);
                        }
                        AcptTestRelationEntity atre = atreOpt.get();
                        atre.setTestsMarked(markedTests.toString());
                        atrr.save(atre);
                    } else {
                        AcptTestRelationEntity atre = new AcptTestRelationEntity();
                        atre.setAtri(new AcptTestRelationId(req.getDutyId(), tsae.getId()));
                        atre.setTestsMarked(markedTests.toString());
                        atrr.save(atre);
                    }

        }

        else{
            StringBuilder markedTests = new StringBuilder();
            TestSampleAcptEntity tsae = createNewTestSample(req, markedTests);
            tsae = tsar.save(tsae);
            AcptTestRelationEntity atre = new AcptTestRelationEntity();
            atre.setAtri(new AcptTestRelationId(req.getDutyId(), tsae.getId()));
            atre.setTestsMarked(markedTests.toString());
            atrr.save(atre);
        }



        // // check if there are tests for a sample id marked earlier and if yes, then
        // // check if the same test is being marked again
        // Optional<TestSampleAcptEntity> tsaeOpt = tsar
        //         .findByTsai(new TestSampleAcptId(req.getSampleId(), req.getHeatNo(), req.getSampleType(),
        //                 req.getStrand(), req.getSampleLot()));

        // if (tsaeOpt.isPresent()) {
        //     TestSampleAcptEntity tsae = tsaeOpt.get();

        //     // Check if any previously marked test is being marked again
        //     if (isPreviouslyMarked(tsae, req)) {
        //         throw new SmsInvalidArgumentException(
        //                 new SmsErrorDetails(
        //                         RailMillAppConstant.ERROR_CODE_VALIDATION,
        //                         RailMillAppConstant.ERROR_TYPE_CODE_VALIDATION,
        //                         RailMillAppConstant.ERROR_TYPE_VALIDATION,
        //                         "Some test which were marked earlier was tried to be marked again."));
        //     }

        //     // Update existing entity with new test results

        //     StringBuilder markedTests = new StringBuilder();
        //     updateTestSample(tsae, req, markedTests);
        //     tsae = tsar.save(tsae);

        //     Optional<AcptTestRelationEntity> atreOpt = atrr
        //             .findByAtri(new AcptTestRelationId(req.getDutyId(), tsae.getId()));

            // if (atreOpt.isPresent()) {
            //     String prevMarkedTest = atreOpt.get().getTestsMarked();
            //     if (prevMarkedTest != null && !prevMarkedTest.isEmpty()) {
            //         markedTests.append(", "); // Add a separator if necessary
            //         markedTests.append(prevMarkedTest);
            //     }

            //     AcptTestRelationEntity atre = atreOpt.get();
            //     atre.setTestsMarked(markedTests.toString());
            //     atrr.save(atre);
            // } else {
            //     AcptTestRelationEntity atre = new AcptTestRelationEntity();
            //     atre.setAtri(new AcptTestRelationId(req.getDutyId(), tsae.getId()));
            //     atre.setTestsMarked(markedTests.toString());
            //     atrr.save(atre);
            // }

        //     return;
        // // }
        // StringBuilder markedTests = new StringBuilder();
        // TestSampleAcptEntity tsae = createNewTestSample(req, markedTests);

        // tsae = tsar.save(tsae);

        // AcptTestRelationEntity atre = new AcptTestRelationEntity();
        // atre.setAtri(new AcptTestRelationId(req.getDutyId(), tsae.getId()));
        // atre.setTestsMarked(markedTests.toString());
        // atrr.save(atre);
    }

    private void validateMarkedTests(TestSampleAcptDto req, List<TestSampleAcptEntity> tsaeList){
        System.out.println("CALLED VALIDATE MARKED TESTS" + tsaeList.size());
        StringBuilder prevMarkedTests = new StringBuilder(); // to keep the tests which were previously marked

        // Iterate over each existing entity and check for conflicts with the boolean
        // fields
        for (TestSampleAcptEntity existingEntity : tsaeList) {
            System.out.println("REQUEST SP: " + existingEntity.getSp() + req.getSp());
            // Check for each field if it is marked true in both the entity and the dto
            if (existingEntity.getChemical() && req.getChemical()) {
                prevMarkedTests.append("Chemical,");
            }
            if (existingEntity.getN2() && req.getN2()) {
                prevMarkedTests.append("N2,");
            }
            if (existingEntity.getFwtSt() && req.getFwtSt()) {
                prevMarkedTests.append("FWT ST,");
            }
            if (existingEntity.getMechanical() && req.getMechanical()) {
                prevMarkedTests.append("Mechanical,");
            }
            if (existingEntity.getSp() && req.getSp()) {
                prevMarkedTests.append("SP,");
            }
            if (existingEntity.getIr() && req.getIr()) {
                prevMarkedTests.append("IR,");
            }
            if (existingEntity.getO2() && req.getO2()) {
                prevMarkedTests.append("O2,");
            }
            if (existingEntity.getFwtHs() && req.getFwtHs()) {
                prevMarkedTests.append("FWT HS,");
            }
            if (existingEntity.getFwtStSr() && req.getFwtStSr()) {
                prevMarkedTests.append("FWT ST SR,");
            }
            if (existingEntity.getTensileFoot() && req.getTensileFoot()) {
                prevMarkedTests.append("Tensile Foot,");
            }
            if (existingEntity.getMicro() && req.getMicro()) {
                prevMarkedTests.append("Micro,");
            }
            if (existingEntity.getDecarb() && req.getDecarb()) {
                prevMarkedTests.append("Decarb,");
            }
            if (existingEntity.getRsh() && req.getRsh()) {
                prevMarkedTests.append("RSH,");
            }
            if (existingEntity.getPh() && req.getPh()) {
                prevMarkedTests.append("PH,");
            }
            if (existingEntity.getTensile() && req.getTensile()) {
                prevMarkedTests.append("Tensile,");
            }
        }

        // If there are any conflicts, throw an exception with all the errors
        if (prevMarkedTests.length() > 0) {
            System.out.println("ERROR");
            prevMarkedTests.append(" tests are marked before. Please unmark them and save.");
            // throw new Exception(prevMarkedTests.toString().trim());

                            throw new SmsInvalidArgumentException(
                        new SmsErrorDetails(
                                RailMillAppConstant.ERROR_CODE_VALIDATION,
                                RailMillAppConstant.ERROR_TYPE_CODE_VALIDATION,
                                RailMillAppConstant.ERROR_TYPE_VALIDATION,
                                prevMarkedTests.toString().trim()));
        }

    }

    private boolean isPreviouslyMarked(TestSampleAcptEntity tsae, TestSampleAcptDto req) {
        return Boolean.TRUE.equals(tsae.getChemical()) && Boolean.TRUE.equals(req.getChemical()) ||
                Boolean.TRUE.equals(tsae.getDecarb()) && Boolean.TRUE.equals(req.getDecarb()) ||
                Boolean.TRUE.equals(tsae.getFwtHs()) && Boolean.TRUE.equals(req.getFwtHs()) ||
                Boolean.TRUE.equals(tsae.getFwtSt()) && Boolean.TRUE.equals(req.getFwtSt()) ||
                Boolean.TRUE.equals(tsae.getFwtStSr()) && Boolean.TRUE.equals(req.getFwtStSr()) ||
                Boolean.TRUE.equals(tsae.getIr()) && Boolean.TRUE.equals(req.getIr()) ||
                Boolean.TRUE.equals(tsae.getMechanical()) && Boolean.TRUE.equals(req.getMechanical()) ||
                Boolean.TRUE.equals(tsae.getMicro()) && Boolean.TRUE.equals(req.getMicro()) ||
                Boolean.TRUE.equals(tsae.getN2()) && Boolean.TRUE.equals(req.getN2()) ||
                Boolean.TRUE.equals(tsae.getO2()) && Boolean.TRUE.equals(req.getO2()) ||
                Boolean.TRUE.equals(tsae.getSp()) && Boolean.TRUE.equals(req.getSp()) ||
                Boolean.TRUE.equals(tsae.getRsh()) && Boolean.TRUE.equals(req.getRsh()) ||
                Boolean.TRUE.equals(tsae.getTensile()) && Boolean.TRUE.equals(req.getTensile()) ||
                Boolean.TRUE.equals(tsae.getPh()) && Boolean.TRUE.equals(req.getPh()) ||
                Boolean.TRUE.equals(tsae.getTensileFoot()) && Boolean.TRUE.equals(req.getTensileFoot());
    }

    private void updateTestSample(TestSampleAcptEntity tsae, TestSampleAcptDto req, StringBuilder markedTests) {
        if (Boolean.TRUE.equals(req.getChemical())) {
            tsae.setChemical(req.getChemical());
            if (markedTests.length() > 0)
                markedTests.append(", "); // Add comma if it's not the first item
            markedTests.append("Chemical");
        }
        if (Boolean.TRUE.equals(req.getDecarb())) {
            tsae.setDecarb(req.getDecarb());
            if (markedTests.length() > 0)
                markedTests.append(", "); // Add comma if it's not the first item
            markedTests.append("Decard");
        }
        if (Boolean.TRUE.equals(req.getFwtHs())) {
            tsae.setFwtHs(req.getFwtHs());
            if (markedTests.length() > 0)
                markedTests.append(", "); // Add comma if it's not the first item
            markedTests.append("FWT (HS)");
        }
        if (Boolean.TRUE.equals(req.getFwtSt())) {
            tsae.setFwtSt(req.getFwtSt());
            if (markedTests.length() > 0)
                markedTests.append(", "); // Add comma if it's not the first item
            markedTests.append("FWT (St.)");
        }
        if (Boolean.TRUE.equals(req.getFwtStSr())) {
            tsae.setFwtStSr(req.getFwtStSr());
            if (markedTests.length() > 0)
                markedTests.append(", "); // Add comma if it's not the first item
            markedTests.append("FWT (St.) - Sr");
        }
        if (Boolean.TRUE.equals(req.getIr())) {
            tsae.setIr(req.getIr());
            if (markedTests.length() > 0)
                markedTests.append(", "); // Add comma if it's not the first item
            markedTests.append("IR");
        }
        if (Boolean.TRUE.equals(req.getMechanical())) {
            tsae.setMechanical(req.getMechanical());
            if (markedTests.length() > 0)
                markedTests.append(", "); // Add comma if it's not the first item
            markedTests.append("Mechanical");
        }
        if (Boolean.TRUE.equals(req.getMicro())) {
            tsae.setMicro(req.getMicro());
            if (markedTests.length() > 0)
                markedTests.append(", "); // Add comma if it's not the first item
            markedTests.append("Micro");
        }
        if (Boolean.TRUE.equals(req.getN2())) {
            tsae.setN2(req.getN2());
            if (markedTests.length() > 0)
                markedTests.append(", "); // Add comma if it's not the first item
            markedTests.append("N2");
        }
        if (Boolean.TRUE.equals(req.getO2())) {
            tsae.setO2(req.getO2());
            if (markedTests.length() > 0)
                markedTests.append(", "); // Add comma if it's not the first item
            markedTests.append("O2");
        }
        if (Boolean.TRUE.equals(req.getSp())) {
            tsae.setSp(req.getSp());
            if (markedTests.length() > 0)
                markedTests.append(", "); // Add comma if it's not the first item
            markedTests.append("SP");
        }
        if (Boolean.TRUE.equals(req.getTensileFoot())) {
            tsae.setTensileFoot(req.getTensileFoot());
            if (markedTests.length() > 0)
                markedTests.append(", "); // Add comma if it's not the first item
            markedTests.append("Tensile Foot");
        }
        if (Boolean.TRUE.equals(req.getTensile())) {
            tsae.setTensile(req.getTensile());
            if (markedTests.length() > 0)
                markedTests.append(", "); // Add comma if it's not the first item
            markedTests.append("Tensile");
        }
        if (Boolean.TRUE.equals(req.getRsh())) {
            tsae.setRsh(req.getRsh());
            if (markedTests.length() > 0)
                markedTests.append(", "); // Add comma if it's not the first item
            markedTests.append("RSH");
        }
        if (Boolean.TRUE.equals(req.getPh())) {
            tsae.setPh(req.getPh());
            if (markedTests.length() > 0)
                markedTests.append(", "); // Add comma if it's not the first item
            markedTests.append("PH");
        }
    }

    private TestSampleAcptEntity createNewTestSample(TestSampleAcptDto req, StringBuilder markedTests) {

        // Check each field in the 'req' and append to the StringBuilder if the value is
        // true
        if (Objects.nonNull(req.getChemical()) && req.getChemical()) {
            markedTests.append("Chemical, ");
        }
        if (Objects.nonNull(req.getN2()) && req.getN2()) {
            markedTests.append("N2, ");
        }
        if (Objects.nonNull(req.getFwtSt()) && req.getFwtSt()) {
            markedTests.append("FWT (St), ");
        }
        if (Objects.nonNull(req.getMechanical()) && req.getMechanical()) {
            markedTests.append("Mechanical, ");
        }
        if (Objects.nonNull(req.getSp()) && req.getSp()) {
            markedTests.append("SP, ");
        }
        if (Objects.nonNull(req.getIr()) && req.getIr()) {
            markedTests.append("IR, ");
        }
        if (Objects.nonNull(req.getO2()) && req.getO2()) {
            markedTests.append("O2, ");
        }
        if (Objects.nonNull(req.getFwtHs()) && req.getFwtHs()) {
            markedTests.append("FWT (HS), ");
        }
        if (Objects.nonNull(req.getFwtStSr()) && req.getFwtStSr()) {
            markedTests.append("FWT (St) - SR, ");
        }
        if (Objects.nonNull(req.getTensileFoot()) && req.getTensileFoot()) {
            markedTests.append("Tensile Foot, ");
        }
        if (Objects.nonNull(req.getMicro()) && req.getMicro()) {
            markedTests.append("Micro, ");
        }
        if (Objects.nonNull(req.getDecarb()) && req.getDecarb()) {
            markedTests.append("Decarb, ");
        }
        if (Objects.nonNull(req.getRsh()) && req.getRsh()) {
            markedTests.append("RSH, ");
        }
        if (Objects.nonNull(req.getTensile()) && req.getTensile()) {
            markedTests.append("Tensile, ");
        }
        if (Objects.nonNull(req.getPh()) && req.getPh()) {
            markedTests.append("PH, ");
        }

        // Remove trailing comma and space
        if (markedTests.length() > 0) {
            markedTests.setLength(markedTests.length() - 2); // Removes ", " at the end
        }

        // Remove the trailing comma and space if there are any marked tests
        if (markedTests.length() > 0) {
            markedTests.setLength(markedTests.length() - 2); // Remove the last ", "
        }

        TestSampleAcptEntity tsae = new TestSampleAcptEntity();
        // Set individual fields directly (no more embedded tsai)
        System.out.println("Creating new test sample entity...");
        System.out.println("Setting Sample ID: " + req.getSampleId());
        System.out.println("Setting Heat No: " + req.getHeatNo());
        System.out.println("Setting Sample Type: " + req.getSampleType());
        System.out.println("Setting Strand: " + req.getStrand());
        System.out.println("Setting Sample Lot: " + req.getSampleLot());
        System.out.println("Setting Rail Grade: " + req.getRailGrade());

        tsae.setSampleId(req.getSampleId());
        tsae.setHeatNo(req.getHeatNo());
        tsae.setSampleType(req.getSampleType());
        tsae.setStrand(req.getStrand());
        tsae.setSampleLot(req.getSampleLot());
        tsae.setRailGrade(req.getRailGrade());
        tsae.setChemical(req.getChemical() != null ? req.getChemical() : false);
        tsae.setN2(req.getN2() != null ? req.getN2() : false);
        tsae.setFwtSt(req.getFwtSt() != null ? req.getFwtSt() : false);
        tsae.setMechanical(req.getMechanical() != null ? req.getMechanical() : false);
        tsae.setSp(req.getSp() != null ? req.getSp() : false);
        tsae.setIr(req.getIr() != null ? req.getIr() : false);
        tsae.setO2(req.getO2() != null ? req.getO2() : false);
        tsae.setFwtHs(req.getFwtHs() != null ? req.getFwtHs() : false);
        tsae.setFwtStSr(req.getFwtStSr() != null ? req.getFwtStSr() : false);
        tsae.setTensileFoot(req.getTensileFoot() != null ? req.getTensileFoot() : false);
        tsae.setMicro(req.getMicro() != null ? req.getMicro() : false);
        tsae.setRsh(req.getRsh() != null ? req.getRsh() : false);
        tsae.setTensile(req.getTensile() != null ? req.getTensile() : false);
        tsae.setPh(req.getPh() != null ? req.getPh() : false);
        tsae.setDecarb(req.getDecarb() != null ? req.getDecarb() : false);
        tsae.setCreatedAt(LocalDateTime.now());
        return tsae;
    }

    // @Override
    // @Transactional
    // public void saveTestingRailDtls(SaveRailDtlsReqDto req) {
    //     // Check if the Rail ID is associated with any heat
    //     List<TestSampleAcptEntity> tsae = tsar.findByTsaiSampleId(req.getRailId())
    //             .orElseThrow(() -> new SmsResourceNotFoundException(
    //                     new SmsErrorDetails(
    //                             RailMillAppConstant.ERROR_CODE_RESOURCE,
    //                             RailMillAppConstant.ERROR_TYPE_CODE_RESOURCE,
    //                             RailMillAppConstant.ERROR_TYPE_RESOURCE,
    //                             "Rail ID not registered with any heat in test sample marking.")));

    //     // Find an open batch
    //     Optional<HtSequenceHeatEntity> openBatchOpt = htshr.findByHtSequenceBatchCompleteFalse();

    //     HtSequenceHeatEntity htshe;
    //     if (openBatchOpt.isPresent()) {
    //         htshe = openBatchOpt.get();
    //         // Validate if the Rail ID belongs to the running batch
    //         if (!htshe.getHeatNo().equals(tsae.getTsai().getHeatNo())) {
    //             throw new SmsInvalidArgumentException(
    //                     new SmsErrorDetails(
    //                             RailMillAppConstant.ERROR_CODE_VALIDATION,
    //                             RailMillAppConstant.ERROR_TYPE_CODE_VALIDATION,
    //                             RailMillAppConstant.ERROR_TYPE_VALIDATION,
    //                             "Rail ID does not belong to the current running batch."));
    //         }
    //     } else {
    //         // Create a new batch for the Rail ID's heat
    //         htshe = new HtSequenceHeatEntity();
    //         htshe.setBatchCreateDutyId(req.getDutyId());
    //         htshe.setHeatNo(tsae.getTsai().getHeatNo());
    //         htshe.setHtSequenceBatchComplete(false);
    //         htshr.save(htshe);
    //     }

    //     // Save the rail details
    //     HtSequenceHeatRailEntity htshre = new HtSequenceHeatRailEntity();
    //     htshre.setDutyId(req.getDutyId());
    //     htshre.setBloomQuality(req.getBloomQuality());
    //     htshre.setHeatNo(tsae.getTsai().getHeatNo());
    //     htshre.setHtStatus(req.getHtStatus());
    //     htshre.setRailId(req.getRailId());
    //     htshre.setCreatedAt(LocalDateTime.now());
    //     htshrr.save(htshre);
    // }

    @Override
    @Transactional
    public void saveBatch(SaveBatchReqDto req) {
        // Find an open batch
        HtSequenceHeatEntity htshe = htshr.findByHtSequenceBatchCompleteFalse()
                .orElseThrow(() -> new SmsInvalidArgumentException(
                        new SmsErrorDetails(
                                RailMillAppConstant.ERROR_CODE_VALIDATION,
                                RailMillAppConstant.ERROR_TYPE_CODE_VALIDATION,
                                RailMillAppConstant.ERROR_TYPE_VALIDATION,
                                "No open batch found.")));

        htshe.setBatchEndDutyId(req.getDutyId());
        htshe.setHtSequenceBatchComplete(true);

        htshr.save(htshe);
    }

    // @Override
    // public HtSequenceResDto getHtSeqOpenBatchDtls() {
    //     // Find an open batch
    //     HtSequenceHeatEntity htshe = htshr.findByHtSequenceBatchCompleteFalse()
    //             .orElseThrow(() -> new SmsInvalidArgumentException(
    //                     new SmsErrorDetails(
    //                             RailMillAppConstant.ERROR_CODE_VALIDATION,
    //                             RailMillAppConstant.ERROR_TYPE_CODE_VALIDATION,
    //                             RailMillAppConstant.ERROR_TYPE_VALIDATION,
    //                             "No open batch found. Please start new batch.")));

    //     List<HtSequenceHeatRailEntity> htshreList = htshrr.findByHeatNoOrderByCreatedAtDesc(htshe.getHeatNo());

    //     HtSequenceResDto htsrd = new HtSequenceResDto();
    //     htsrd.setHeatNo(htshe.getHeatNo());

    //     List<HtSequenceRailIdDto> railIdDtlList = htshreList.stream().map(htshre -> {
    //         HtSequenceRailIdDto htsrid = new HtSequenceRailIdDto();
    //         htsrid.setBloomQuality(htshre.getBloomQuality());
    //         htsrid.setHtStatus(htshre.getHtStatus());
    //         htsrid.setRailId(htshre.getRailId());

    //         TestSampleAcptEntity tsae = tsar.findByTsaiSampleId(htshre.getRailId())
    //                 .orElseThrow(() -> new SmsResourceNotFoundException(
    //                         new SmsErrorDetails(
    //                                 RailMillAppConstant.ERROR_CODE_RESOURCE,
    //                                 RailMillAppConstant.ERROR_TYPE_CODE_RESOURCE,
    //                                 RailMillAppConstant.ERROR_TYPE_RESOURCE,
    //                                 "Rail ID not registered with any heat.")));

    //         // Dynamically populate test-marked list based on tsae's Boolean fields
    //         List<String> testMarkedList = convertMarkedTestToStringList(tsae);

    //         htsrid.setTestMarked(testMarkedList);
    //         return htsrid;
    //     }).collect(Collectors.toList());

    //     htsrd.setRailIdDtlList(railIdDtlList);
    //     return htsrd;
    // }

    @Override
    public List<String> getMarkedTestSamples(String railId) {
        // get the data from the test sample entity
        List<TestSampleAcptEntity> tsaeList = tsar.findBySampleId(railId);

        // // check if the heat number associated with tha above entity
        // // is associated with the heat number in current open batch

        // Optional<HtSequenceHeatEntity> htsheOpt =
        // htshr.findByHtSequenceBatchCompleteFalse();

        // // first check if there is any open batch
        // if (htsheOpt.isPresent()) {
        // HtSequenceHeatEntity htshe = htsheOpt.get();

        // // if rail id doesnt belong to current batch
        // if (!htshe.getHeatNo().equalsIgnoreCase(tsae.getTsai().getHeatNo())) {
        // throw new SmsInvalidArgumentException(new
        // SmsErrorDetails(RailMillAppConstant.ERROR_CODE_INVALID,
        // RailMillAppConstant.ERROR_TYPE_CODE_INVALID, RailMillAppConstant.ERROR_TYPE_INVALID,
        // "Rail ID does not belong to the current batch."));
        // }

        // // rail ID belongs to current batch but data already present in the response
        // List<HtSequenceHeatRailEntity> htshreList =
        // htshrr.findByHeatNo(htshe.getHeatNo());
        // boolean isRailIdPresent = htshreList.stream()
        // .anyMatch(record -> record.getRailId().equalsIgnoreCase(railId));

        // if(isRailIdPresent){
        // throw new SmsInvalidArgumentException(new
        // SmsErrorDetails(RailMillAppConstant.ERROR_CODE_INVALID,
        // RailMillAppConstant.ERROR_TYPE_CODE_INVALID, RailMillAppConstant.ERROR_TYPE_INVALID,
        // "Rail ID already present in the open batch. Cannot add again."));
        // }

        // List<String> markedTestList = convertMarkedTestToStringList(tsae);
        // return markedTestList;
        // }

        return convertMarkedTestToStringList(tsaeList);

        // if no open batch
    }

    // public List<String> convertMarkedTestToStringList(TestSampleAcptEntity tsae) {
    //     return Stream.of(
    //             new AbstractMap.SimpleEntry<>("Chemical", tsae.getChemical()),
    //             new AbstractMap.SimpleEntry<>("N2", tsae.getN2()),
    //             new AbstractMap.SimpleEntry<>("FWT ST", tsae.getFwtSt()),
    //             new AbstractMap.SimpleEntry<>("Mechanical", tsae.getMechanical()),
    //             new AbstractMap.SimpleEntry<>("SP", tsae.getSp()),
    //             new AbstractMap.SimpleEntry<>("IR", tsae.getIr()),
    //             new AbstractMap.SimpleEntry<>("O2", tsae.getO2()),
    //             new AbstractMap.SimpleEntry<>("FWT HS", tsae.getFwtHs()),
    //             new AbstractMap.SimpleEntry<>("FWT ST SR", tsae.getFwtStSr()),
    //             new AbstractMap.SimpleEntry<>("Tensile Foot", tsae.getTensileFoot()),
    //             new AbstractMap.SimpleEntry<>("Micro", tsae.getMicro()),
    //             new AbstractMap.SimpleEntry<>("Tensile", tsae.getTensile()),
    //             new AbstractMap.SimpleEntry<>("RSH", tsae.getRsh()),
    //             new AbstractMap.SimpleEntry<>("PH", tsae.getPh()),
    //             new AbstractMap.SimpleEntry<>("Decarb",
    //                     tsae.getDecarb()))
    //             .filter(AbstractMap.SimpleEntry::getValue)
    //             .map(AbstractMap.SimpleEntry::getKey)
    //             .collect(Collectors.toList());
    // }

    public List<String> convertMarkedTestToStringList(List<TestSampleAcptEntity> tsaeList) {
        // Initialize a list to hold the test names for all entities
    List<String> markedTests = new ArrayList<>();
    // Iterate over each TestSampleAcptEntity in the list
    for (TestSampleAcptEntity tsae : tsaeList) {
        // For each entity, create a list of marked tests and add it to the main list
        List<String> testsForEntity = Stream.of(
                new AbstractMap.SimpleEntry<>("Chemical", tsae.getChemical()),
                new AbstractMap.SimpleEntry<>("N2", tsae.getN2()),
                new AbstractMap.SimpleEntry<>("FWT ST", tsae.getFwtSt()),
                new AbstractMap.SimpleEntry<>("Mechanical", tsae.getMechanical()),
                new AbstractMap.SimpleEntry<>("SP", tsae.getSp()),
                new AbstractMap.SimpleEntry<>("IR", tsae.getIr()),
                new AbstractMap.SimpleEntry<>("O2", tsae.getO2()),
                new AbstractMap.SimpleEntry<>("FWT HS", tsae.getFwtHs()),
                new AbstractMap.SimpleEntry<>("FWT ST SR", tsae.getFwtStSr()),
                new AbstractMap.SimpleEntry<>("Tensile Foot", tsae.getTensileFoot()),
                new AbstractMap.SimpleEntry<>("Micro", tsae.getMicro()),
                new AbstractMap.SimpleEntry<>("Tensile", tsae.getTensile()),
                new AbstractMap.SimpleEntry<>("RSH", tsae.getRsh()),
                new AbstractMap.SimpleEntry<>("PH", tsae.getPh()),
                new AbstractMap.SimpleEntry<>("Decarb", tsae.getDecarb())
        )
        .filter(AbstractMap.SimpleEntry::getValue) // Keep only the ones marked as true
        .map(AbstractMap.SimpleEntry::getKey) // Get the test names
        .collect(Collectors.toList());

        // Add the tests for this entity to the overall list
        markedTests.addAll(testsForEntity);
    }

    // Return the final list of all marked tests
    return markedTests;
    }

    @Override
    @Transactional
    public void saveHtSequenceRail(HtSequenceReqDto req) {
        System.out.println("CALLEDD");
        Optional<HtSequenceEntity> htseOpt = htsr.findById(req.getRailId());

        if (htseOpt.isPresent()) {
            String errMsg = "";
            if (htseOpt.get().getBatchOpen()) {
                errMsg = "Rail ID already present in current batch. Cannot add again.";
            } else {
                errMsg = "Rail ID already present in a closed batch. Please provide other rail ID.";
            }
            throw new SmsInvalidArgumentException(
                    new SmsErrorDetails(
                            RailMillAppConstant.ERROR_CODE_VALIDATION,
                            RailMillAppConstant.ERROR_TYPE_CODE_VALIDATION,
                            RailMillAppConstant.ERROR_TYPE_VALIDATION,
                            errMsg));
        }

        HtSequenceEntity htse = new HtSequenceEntity();
        htse.setRailId(req.getRailId());
        htse.setBatchOpen(true);
        htse.setBloomQuality(BloomQuality.valueOf(req.getBloomQuality()));
        htse.setCreatedAt(LocalDateTime.now());
        htse.setCreatedBy(req.getDutyId());
        htse.setHtStatus(req.getHtStatus());

        htsr.save(htse);
    }

    @Override
    @Transactional
    public void updateHtSequenceRail(HtSequenceReqDto req) {
        System.out.println("CALLEDD");
        HtSequenceEntity htse = htsr.findById(req.getRailId())
                .orElseThrow(() -> new SmsResourceNotFoundException(
                        new SmsErrorDetails(RailMillAppConstant.ERROR_CODE_RESOURCE,
                                RailMillAppConstant.ERROR_TYPE_CODE_RESOURCE,
                                RailMillAppConstant.ERROR_TYPE_RESOURCE,
                                "Rail ID not found in the records. Cannot update.")));

        htse.setBloomQuality(BloomQuality.valueOf(req.getBloomQuality()));
        htse.setHtStatus(req.getHtStatus());
        htsr.save(htse);
    }

    // @Override
    // public List<HtSequenceRailIdDto> getHtSeqOpenBatchDtlsV2() {
    //     // Find an open batch
    //     List<HtSequenceEntity> htseList = htsr.findByBatchOpenTrue();

    //     if (htseList.isEmpty()) {
    //         throw new SmsInvalidArgumentException(
    //                 new SmsErrorDetails(
    //                         RailMillAppConstant.ERROR_CODE_VALIDATION,
    //                         RailMillAppConstant.ERROR_TYPE_CODE_VALIDATION,
    //                         RailMillAppConstant.ERROR_TYPE_VALIDATION,
    //                         "No open batch found. Please start new batch."));
    //     }

    //     return htseList.stream().map(htse -> {
    //         HtSequenceRailIdDto htsrid = new HtSequenceRailIdDto();
    //         htsrid.setBloomQuality(htse.getBloomQuality().name());
    //         htsrid.setHtStatus(htse.getHtStatus());
    //         htsrid.setRailId(htse.getRailId());
    //         return htsrid;
    //     }).collect(Collectors.toList());
    // }

    @Override
    public List<HtSequenceRailIdDto> getHtSeqOpenBatchDtlsV2() {
        List<Object[]> results = htsr.findOpenBatchDetailsWithTests();
        return results.stream().map(result -> {
            HtSequenceRailIdDto dto = new HtSequenceRailIdDto();
            dto.setRailId((String) result[0]);
            dto.setBloomQuality(result[1] != null ? result[1].toString() : null);
            dto.setHtStatus((Boolean) result[2]);
            dto.setCreatedAt((String) result[3]);
            
            String testsMarked = (String) result[4];
            if (testsMarked != null) {
                dto.setTestMarked(Arrays.asList(testsMarked.split(", ")));
            } else {
                dto.setTestMarked(new ArrayList<>());
            }
            return dto;
        }).collect(Collectors.toList());
    }

    public void saveBatchV2(SaveBatchReqDto req) {
        // Find an open batch
        List<HtSequenceEntity> htseList = htsr.findByBatchOpenTrue();

        if (htseList.isEmpty()) {
            throw new SmsInvalidArgumentException(
                    new SmsErrorDetails(
                            RailMillAppConstant.ERROR_CODE_VALIDATION,
                            RailMillAppConstant.ERROR_TYPE_CODE_VALIDATION,
                            RailMillAppConstant.ERROR_TYPE_VALIDATION,
                            "No open batch found. Please start new batch."));
        }

        htseList.forEach(htse -> {
            htse.setBatchEndDutyId(req.getDutyId());
            htse.setBatchOpen(false);
        });

        htsr.saveAll(htseList);
    }

    public List<TestSampleDto> getAllTestSamples() {
        List<Object[]> results = tsar.findAllTestSamples();
        return results.stream()
            .map(result -> {
                TestSampleDto dto = new TestSampleDto();
                dto.setHeatNumber((String) result[0]);
                // Handle Timestamp to LocalDateTime conversion
                if (result[1] instanceof java.sql.Timestamp) {
                    dto.setCreatedAt(((java.sql.Timestamp) result[1]));
                } else {
                    // dto.setCreatedAt((LocalDateTime) result[1]);
                }
                dto.setType((String) result[2]);
                dto.setRollingTestSampleId((Integer) result[3]); // Add the rolling_test_sample_id
                return dto;
            })
            .collect(Collectors.toList());
    }

    @Override
    public List<TestSampleDto> getTestSamplesByCurrentShift(String currentDutyId) {
        // Get the current duty details to extract shift and date
        Optional<RollingDutyEntity> currentDutyOpt = rdr.findByDutyId(currentDutyId);
        if (!currentDutyOpt.isPresent()) {
            throw new SmsResourceNotFoundException(new SmsErrorDetails(
                RailMillAppConstant.ERROR_CODE_RESOURCE,
                RailMillAppConstant.ERROR_TYPE_CODE_RESOURCE,
                RailMillAppConstant.ERROR_TYPE_RESOURCE,
                "Current duty not found with ID: " + currentDutyId
            ));
        }

        RollingDutyEntity currentDuty = currentDutyOpt.get();
        String currentShift = currentDuty.getShift();
        java.sql.Date currentDate = java.sql.Date.valueOf(currentDuty.getDate());

        // Get test samples filtered by current shift and date
        List<Object[]> results;
        try {
            results = tsar.findTestSamplesByShiftAndDate(currentShift, currentDate);
        } catch (Exception e) {
            // Fallback: return all test samples if shift-based filtering fails
            return getAllTestSamples();
        }

        return results.stream()
            .map(result -> {
                TestSampleDto dto = new TestSampleDto();
                dto.setHeatNumber((String) result[0]);
                // Handle Timestamp to LocalDateTime conversion
                if (result[1] instanceof java.sql.Timestamp) {
                    dto.setCreatedAt(((java.sql.Timestamp) result[1]));
                } else {
                    // dto.setCreatedAt((LocalDateTime) result[1]);
                }
                dto.setType((String) result[2]);
                dto.setRollingTestSampleId((Integer) result[3]); // Add the rolling_test_sample_id
                return dto;
            })
            .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void deleteTestSample(String heatNumber, String type) {
        if ("acceptance".equals(type)) {
            // Delete acceptance test samples
            List<TestSampleAcptEntity> acceptanceTests = tsar.findByHeatNo(heatNumber);
            if (!acceptanceTests.isEmpty()) {
                tsar.deleteAll(acceptanceTests);
            } else {
                throw new SmsResourceNotFoundException(new SmsErrorDetails(
                    RailMillAppConstant.ERROR_CODE_RESOURCE,
                    RailMillAppConstant.ERROR_TYPE_CODE_RESOURCE,
                    RailMillAppConstant.ERROR_TYPE_RESOURCE,
                    "Acceptance test sample not found for heat number: " + heatNumber
                ));
            }
        } else if ("retest".equals(type)) {
            // Delete retest samples using more efficient query
            List<RetestSample> retestSamples = rsr.findByHeatNumber(heatNumber);
            if (!retestSamples.isEmpty()) {
                System.out.println("DEBUG: Deleting " + retestSamples.size() + " retest samples for heat number: " + heatNumber);
                for (RetestSample rs : retestSamples) {
                    System.out.println("DEBUG: Deleting retest sample - Heat: " + rs.getHeatNumber() +
                                     ", Strand: " + rs.getStrandNumber() + ", TestType: " + rs.getTestType() +
                                     ", SampleLot: " + rs.getSampleLot());
                }
                rsr.deleteAll(retestSamples);
                System.out.println("DEBUG: Successfully deleted all retest samples for heat number: " + heatNumber);
            } else {
                System.out.println("DEBUG: No retest samples found for heat number: " + heatNumber);
                throw new SmsResourceNotFoundException(new SmsErrorDetails(
                    RailMillAppConstant.ERROR_CODE_RESOURCE,
                    RailMillAppConstant.ERROR_TYPE_CODE_RESOURCE,
                    RailMillAppConstant.ERROR_TYPE_RESOURCE,
                    "Retest sample not found for heat number: " + heatNumber
                ));
            }
        } else {
            throw new SmsInvalidArgumentException(new SmsErrorDetails(
                RailMillAppConstant.ERROR_CODE_VALIDATION,
                RailMillAppConstant.ERROR_TYPE_CODE_VALIDATION,
                RailMillAppConstant.ERROR_TYPE_VALIDATION,
                "Invalid test sample type: " + type
            ));
        }
    }



    // Helper method to convert strand number from Roman numerals to integers
    private Integer convertStrandToInteger(String strandNumber) {
        if (strandNumber == null) return null;

        try {
            // Try parsing as integer first (for backward compatibility)
            return Integer.parseInt(strandNumber);
        } catch (NumberFormatException e) {
            // Convert Roman numerals to integers
            switch (strandNumber.toUpperCase()) {
                case "I": return 1;
                case "II": return 2;
                case "III": return 3;
                case "IV": return 4;
                case "V": return 5;
                case "VI": return 6;
                default: return null; // Unknown strand format
            }
        }
    }

    @Override
    public TestSampleAcptDto getTestSampleById(Integer rollingTestSampleId, String type) {
        System.out.println("DEBUG: getTestSampleById called with ID=" + rollingTestSampleId + ", type=" + type);

        if ("acceptance".equals(type)) {
            Optional<TestSampleAcptEntity> entityOpt = tsar.findById(rollingTestSampleId);
            if (entityOpt.isPresent()) {
                TestSampleAcptEntity entity = entityOpt.get();
                TestSampleAcptDto dto = new TestSampleAcptDto();

                // Find the dutyId from the relation table
                Optional<AcptTestRelationEntity> relationOpt = atrr.findByAtri(
                    new AcptTestRelationId(null, entity.getId())
                );
                if (relationOpt.isPresent()) {
                    dto.setDutyId(relationOpt.get().getAtri().getDutyId());
                }

            // Map entity fields to DTO
            dto.setRailGrade(entity.getRailGrade());
            dto.setSampleType(entity.getSampleType());
            dto.setSampleId(entity.getSampleId() != null ? entity.getSampleId() : "N/A");
            dto.setHeatNo(entity.getHeatNo());
            dto.setStrand(entity.getStrand());
            dto.setSampleLot(entity.getSampleLot());
            dto.setChemical(entity.getChemical());
            dto.setN2(entity.getN2());
            dto.setFwtSt(entity.getFwtSt());
            dto.setMechanical(entity.getMechanical());
            dto.setSp(entity.getSp());
            dto.setIr(entity.getIr());
            dto.setO2(entity.getO2());
            dto.setFwtHs(entity.getFwtHs());
            dto.setFwtStSr(entity.getFwtStSr());
            dto.setTensileFoot(entity.getTensileFoot());
            dto.setMicro(entity.getMicro());
            dto.setDecarb(entity.getDecarb());
            dto.setRsh(entity.getRsh());
            dto.setPh(entity.getPh());
            dto.setTensile(entity.getTensile());
            return dto;
        } else {
            throw new SmsResourceNotFoundException(new SmsErrorDetails(
                RailMillAppConstant.ERROR_CODE_RESOURCE,
                RailMillAppConstant.ERROR_TYPE_CODE_RESOURCE,
                RailMillAppConstant.ERROR_TYPE_RESOURCE,
                "Acceptance test sample not found with ID: " + rollingTestSampleId
            ));
        }
    } else if ("retest".equals(type)) {
        System.out.println("DEBUG: Looking for retest sample with ID=" + rollingTestSampleId);
        Optional<RetestSample> retestOpt = rsr.findById(rollingTestSampleId);
        System.out.println("DEBUG: Retest sample found: " + retestOpt.isPresent());
        if (retestOpt.isPresent()) {
            RetestSample firstRetest = retestOpt.get();

            // Find all related retest samples for the same heat number, test type, and sample lot
            List<RetestSample> allRelatedRetests = rsr.findByHeatNumberAndTestTypeAndSampleLot(
                firstRetest.getHeatNumber(),
                firstRetest.getTestType(),
                firstRetest.getSampleLot()
            );

            System.out.println("DEBUG: Found " + allRelatedRetests.size() + " related retest samples");

            TestSampleAcptDto dto = new TestSampleAcptDto();

            // Map retest sample fields to DTO using the first retest sample
            // Note: For retest samples, we put testType in dutyId field for frontend compatibility
            dto.setDutyId(firstRetest.getTestType()); // Frontend expects testType in dutyId field
            dto.setRailGrade(firstRetest.getRailGrade());
            dto.setSampleId(firstRetest.getSampleId() != null ? firstRetest.getSampleId() : "N/A");
            dto.setHeatNo(firstRetest.getHeatNumber());
            dto.setSampleLot(firstRetest.getSampleLot());
            dto.setSampleType("retest"); // Set a default sample type for retest

            // For multiple retest samples, we'll put all strand numbers in a special format
            // that the frontend can parse to populate all retest sample dropdowns
            if (allRelatedRetests.size() == 1) {
                // Single retest sample - use the strand directly
                Integer strandValue = convertStrandToInteger(firstRetest.getStrandNumber());
                dto.setStrand(strandValue);
            } else {
                // Multiple retest samples - use the first one's strand for backward compatibility
                // The frontend will need to handle multiple strands differently
                Integer strandValue = convertStrandToInteger(firstRetest.getStrandNumber());
                dto.setStrand(strandValue);

                // Store all strand numbers in a custom field (we'll use the sampleId field to pass this info)
                StringBuilder allStrands = new StringBuilder();
                for (int i = 0; i < allRelatedRetests.size(); i++) {
                    if (i > 0) allStrands.append(",");
                    allStrands.append(allRelatedRetests.get(i).getStrandNumber());
                }
                // Store original sampleId and all strands info
                dto.setSampleId((firstRetest.getSampleId() != null ? firstRetest.getSampleId() : "N/A") +
                               "|STRANDS:" + allStrands.toString());
            }

            // For retest samples, we don't have individual test flags, so set defaults
            dto.setChemical(false);
            dto.setN2(false);
            dto.setFwtSt(false);
            dto.setMechanical(false);
            dto.setSp(false);
            dto.setIr(false);
            dto.setO2(false);
            dto.setFwtHs(false);
            dto.setFwtStSr(false);
            dto.setTensileFoot(false);
            dto.setMicro(false);
            dto.setDecarb(false);
            dto.setRsh(false);
            dto.setPh(false);
            dto.setTensile(false);

            return dto;
        } else {
            throw new SmsResourceNotFoundException(new SmsErrorDetails(
                RailMillAppConstant.ERROR_CODE_RESOURCE,
                RailMillAppConstant.ERROR_TYPE_CODE_RESOURCE,
                RailMillAppConstant.ERROR_TYPE_RESOURCE,
                "Retest sample not found with ID: " + rollingTestSampleId
            ));
        }
    } else {
        throw new IllegalArgumentException("Invalid type: " + type + ". Must be 'acceptance' or 'retest'");
    }
    }

    @Override
    @Transactional
    public void updateTestSample(TestSampleAcptDto req) {
        // Validate that heat number has reached "Bloom Cutting" stage in SMS
        validateHeatBloomStage(req.getHeatNo());

        // Find existing test sample by heat number
        List<TestSampleAcptEntity> existingTests = tsar.findByHeatNo(req.getHeatNo());
        if (!existingTests.isEmpty()) {
            TestSampleAcptEntity entity = existingTests.get(0); // Update the first one

            // Update all fields from the request
            entity.setRailGrade(req.getRailGrade());
            entity.setSampleType(req.getSampleType());
            entity.setSampleId(req.getSampleId());
            entity.setHeatNo(req.getHeatNo());
            entity.setStrand(req.getStrand());
            entity.setSampleLot(req.getSampleLot());
            entity.setChemical(req.getChemical() != null ? req.getChemical() : false);
            entity.setN2(req.getN2() != null ? req.getN2() : false);
            entity.setFwtSt(req.getFwtSt() != null ? req.getFwtSt() : false);
            entity.setMechanical(req.getMechanical() != null ? req.getMechanical() : false);
            entity.setSp(req.getSp() != null ? req.getSp() : false);
            entity.setIr(req.getIr() != null ? req.getIr() : false);
            entity.setO2(req.getO2() != null ? req.getO2() : false);
            entity.setFwtHs(req.getFwtHs() != null ? req.getFwtHs() : false);
            entity.setFwtStSr(req.getFwtStSr() != null ? req.getFwtStSr() : false);
            entity.setTensileFoot(req.getTensileFoot() != null ? req.getTensileFoot() : false);
            entity.setMicro(req.getMicro() != null ? req.getMicro() : false);
            entity.setDecarb(req.getDecarb() != null ? req.getDecarb() : false);
            entity.setRsh(req.getRsh() != null ? req.getRsh() : false);
            entity.setPh(req.getPh() != null ? req.getPh() : false);
            entity.setTensile(req.getTensile() != null ? req.getTensile() : false);
            tsar.save(entity);
        } else {
            throw new SmsResourceNotFoundException(new SmsErrorDetails(
                RailMillAppConstant.ERROR_CODE_RESOURCE,
                RailMillAppConstant.ERROR_TYPE_CODE_RESOURCE,
                RailMillAppConstant.ERROR_TYPE_RESOURCE,
                "Test sample not found for heat number: " + req.getHeatNo()
            ));
        }
    }

    @Override
    @Transactional
    public void updateRetestSample(SaveRetestSampleReqDto req) {
        // Find and update existing retest samples for this heat number
        List<RetestSample> existingRetestSamples = rsr.findByHeatNumber(req.getHeatNumber());

        if (!existingRetestSamples.isEmpty()) {
            // Delete existing retest samples for this heat number
            rsr.deleteAll(existingRetestSamples);
            System.out.println("DEBUG: Deleted " + existingRetestSamples.size() + " existing retest samples for heat: " + req.getHeatNumber());
        }

        // Create new retest samples with updated data
        for (String strandNumber : req.getStrandNumber()) {
            RetestSample retestSample = new RetestSample();
            retestSample.setHeatNumber(req.getHeatNumber());
            retestSample.setStrandNumber(strandNumber);
            retestSample.setTestType(req.getTestType());
            retestSample.setRailGrade(req.getRailGrade());
            retestSample.setSampleLot(req.getSampleLot());
            retestSample.setDutyId(req.getDutyId());
            retestSample.setSampleId(req.getSampleId());
            // createDate is set automatically in the entity
            rsr.save(retestSample);
        }
    }

    @Override
    @Transactional
    public void deleteHtRail(HtRailDeleteDto req){
        htsr.deleteById(req.getRailId());
    }

    @Override
    public void saveTestingRailDtls(SaveRailDtlsReqDto req) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'saveTestingRailDtls'");
    }

    @Override
    public HtSequenceResDto getHtSeqOpenBatchDtls() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getHtSeqOpenBatchDtls'");
    }

    @Override
    public void validateHeatForTesting(String heatNumber) {
        // Validate that heat number has reached "Bloom Cutting" stage in SMS
        validateHeatBloomStage(heatNumber);
    }

    /**
     * Validates that the heat number has reached "Bloom Cutting" stage in SMS
     * Checks SMS2 and SMS3 systems only (as per system architecture)
     */
    private void validateHeatBloomStage(String heatNumber) {
        System.out.println("DEBUG: Validating heat number: " + heatNumber + " in SMS2 and SMS3");

        boolean isBloomStageCompleted = false;
        String foundInSystem = "";

        // Check SMS2
        try {
            Optional<com.sarthi.Sms.entity.sms.HeatDtlSms2Entity> heatSms2Opt =
                hds2r.findByHeatNo(heatNumber);
            if (heatSms2Opt.isPresent() && "Bloom Cutting".equals(heatSms2Opt.get().getHeatStage())) {
                isBloomStageCompleted = true;
                foundInSystem = "SMS2";
                System.out.println("DEBUG: Heat found in SMS2 with Bloom Cutting stage");
            }
        } catch (Exception e) {
            System.out.println("DEBUG: Error checking SMS2: " + e.getMessage());
        }

        // Check SMS3 if not found in SMS2
        if (!isBloomStageCompleted) {
            try {
                Optional<com.sarthi.Sms.entity.sms.HeatDtlSms3Entity> heatSms3Opt =
                    hds3r.findByHeatNo(heatNumber);
                if (heatSms3Opt.isPresent() && "Bloom Cutting".equals(heatSms3Opt.get().getHeatStage())) {
                    isBloomStageCompleted = true;
                    foundInSystem = "SMS3";
                    System.out.println("DEBUG: Heat found in SMS3 with Bloom Cutting stage");
                }
            } catch (Exception e) {
                System.out.println("DEBUG: Error checking SMS3: " + e.getMessage());
            }
        }

        if (isBloomStageCompleted) {
            System.out.println("DEBUG: Heat validation PASSED - Bloom Cutting stage confirmed in " + foundInSystem);
            return;
        }

        // Only reject if heat is not found in either SMS2 or SMS3 with Bloom Cutting stage
        System.out.println("DEBUG: Heat validation FAILED - heat not found with Bloom Cutting stage in SMS2 or SMS3");
        throw new SmsInvalidArgumentException(
            new SmsErrorDetails(
                RailMillAppConstant.ERROR_CODE_VALIDATION,
                RailMillAppConstant.ERROR_TYPE_CODE_VALIDATION,
                RailMillAppConstant.ERROR_TYPE_VALIDATION,
                "Heat number " + heatNumber + " has not reached 'Bloom Cutting' stage in SMS. Please complete bloom stage before creating test samples."
            )
        );
    }



    // @Override
    // @Transactional
    // public void saveRetestSample(SaveRetestSampleReqDto req) {
    //     // Check if heat number exists in test_sample_acceptance table
    //     List<TestSampleAcptEntity> tsaeList = tsar.findByTsaiHeatNo(req.getHeatNumber());
    //     if (tsaeList.isEmpty()) {
    //         throw new SmsResourceNotFoundException(
    //             new SmsErrorDetails(
    //                 RailMillAppConstant.ERROR_CODE_RESOURCE,
    //                 RailMillAppConstant.ERROR_TYPE_CODE_RESOURCE,
    //                 RailMillAppConstant.ERROR_TYPE_RESOURCE,
    //                 "Heat number not found in test sample records."
    //             )
    //         );
    //     }

    //     // Process each strand number
    //     for (String stNo : req.getStrandNumber()) {
    //         // Check if retest sample already exists for this strand
    //         Optional<RetestSample> existingRetest = retestSampleRepository.findById(
    //             new RetestSampleId(req.getHeatNumber(), stNo, req.getTestType(), req.getSampleLot())
    //         );
    //         if (existingRetest.isPresent()) {
    //             throw new SmsInvalidArgumentException(
    //                 new SmsErrorDetails(
    //                     RailMillAppConstant.ERROR_CODE_VALIDATION,
    //                     RailMillAppConstant.ERROR_TYPE_CODE_VALIDATION,
    //                     RailMillAppConstant.ERROR_TYPE_VALIDATION,
    //                     "Retest sample already exists for heat number: " + req.getHeatNumber() + 
    //                     ", strand number: " + stNo + 
    //                     ", test type: " + req.getTestType()
    //                 )
    //             );
    //         }

    //         // Save new retest sample for this strand
    //         RetestSample retestSample = new RetestSample();
    //         retestSample.setHeatNumber(req.getHeatNumber());
    //         retestSample.setStrandNumber(stNo);
    //         retestSample.setTestType(req.getTestType());
    //         retestSample.setRailGrade(req.getRailGrade());
    //         retestSample.setSampleLot(req.getSampleLot());
    //         retestSample.setDutyId(req.getDutyId());
    //         retestSample.setCreateDate(LocalDateTime.now());

    //         retestSampleRepository.save(retestSample);
    //     }
    // }

    @Override
    @Transactional
    public void saveRetestSample(SaveRetestSampleReqDto req) {
        // Validate that heat number has reached "Bloom Cutting" stage in SMS
        validateHeatBloomStage(req.getHeatNumber());

        // Check HT sequence validation only for specific rail grades
        if ("1080HH".equals(req.getRailGrade()) || "R350HT".equals(req.getRailGrade())) {
            Optional<HtSequenceEntity> htSequence = htsr.findById(req.getSampleId());
            if (htSequence.isEmpty()) {
                throw new SmsResourceNotFoundException(
                    new SmsErrorDetails(
                        RailMillAppConstant.ERROR_CODE_RESOURCE,
                        RailMillAppConstant.ERROR_TYPE_CODE_RESOURCE,
                        RailMillAppConstant.ERROR_TYPE_RESOURCE,
                        "Sample ID not found in HT sequence records for rail grade " + req.getRailGrade()
                    )
                );
            }
        }

        // Rest of the existing validation and save logic
        List<TestSampleAcptEntity> tsaeList = tsar.findByHeatNo(req.getHeatNumber());
        if (tsaeList.isEmpty()) {
            throw new SmsResourceNotFoundException(
                new SmsErrorDetails(
                    RailMillAppConstant.ERROR_CODE_RESOURCE,
                    RailMillAppConstant.ERROR_TYPE_CODE_RESOURCE,
                    RailMillAppConstant.ERROR_TYPE_RESOURCE,
                    "Heat number not found in test sample records."
                )
            );
        }

        // Process each strand number
        for (String strandNumber : req.getStrandNumber()) {
            // Check if retest sample already exists for this combination using efficient query
            List<RetestSample> existingRetests = rsr.findByHeatNumberAndStrandNumberAndTestTypeAndSampleLot(
                req.getHeatNumber(),
                strandNumber,
                req.getTestType(),
                req.getSampleLot()
            );

            if (!existingRetests.isEmpty()) {
                System.out.println("DEBUG: Found existing retest sample - Heat: " + req.getHeatNumber() +
                                 ", Strand: " + strandNumber + ", TestType: " + req.getTestType() +
                                 ", SampleLot: " + req.getSampleLot());
                throw new SmsInvalidArgumentException(
                    new SmsErrorDetails(
                        RailMillAppConstant.ERROR_CODE_VALIDATION,
                        RailMillAppConstant.ERROR_TYPE_CODE_VALIDATION,
                        RailMillAppConstant.ERROR_TYPE_VALIDATION,
                        "Retest sample already exists for heat number: " + req.getHeatNumber() +
                        ", strand number: " + strandNumber +
                        ", test type: " + req.getTestType() +
                        ", sample lot: " + req.getSampleLot()
                    )
                );
            }

            RetestSample retestSample = new RetestSample();
            retestSample.setHeatNumber(req.getHeatNumber());
            retestSample.setStrandNumber(strandNumber);
            retestSample.setTestType(req.getTestType());
            retestSample.setRailGrade(req.getRailGrade());
            retestSample.setSampleLot(req.getSampleLot());
            retestSample.setDutyId(req.getDutyId());
            retestSample.setSampleId(req.getSampleId());
            retestSample.setCreateDate(LocalDateTime.now());

            rsr.save(retestSample);
        }
    }

    @Override
    public List<Integer> getStrandNoList(String heatNumber, String sampleLot) {
        List<TestSampleAcptEntity> tsaeList = tsar.findByHeatNoAndSampleLot(heatNumber, sampleLot);
        if (tsaeList.isEmpty()) {
            throw new SmsResourceNotFoundException(
                new SmsErrorDetails(
                    RailMillAppConstant.ERROR_CODE_RESOURCE,
                    RailMillAppConstant.ERROR_TYPE_CODE_RESOURCE,
                    RailMillAppConstant.ERROR_TYPE_RESOURCE,
                    "Heat number not found in test sample records."
                )
            );
        }

        return tsaeList.stream()
            .map(tsae -> tsae.getStrand())
            .collect(Collectors.toList());
    }
    @Override
    @Transactional
    public void deleteHeatDtl(DeleteHeatReqDto req) {
        String heatNo = req.getHeatNo();
        rhdr.deleteByRollingHeatDtlId_HeatNo(heatNo);
    }

    @Override
    public List<Object> checkExistingRetestSamples(String heatNumber) {
        List<RetestSample> retestSamples = rsr.findByHeatNumber(heatNumber);
        List<Object> result = new ArrayList<>();

        for (RetestSample rs : retestSamples) {
            Map<String, Object> sampleInfo = new HashMap<>();
            sampleInfo.put("retestSampleId", rs.getRetestSampleId());
            sampleInfo.put("heatNumber", rs.getHeatNumber());
            sampleInfo.put("strandNumber", rs.getStrandNumber());
            sampleInfo.put("testType", rs.getTestType());
            sampleInfo.put("sampleLot", rs.getSampleLot());
            sampleInfo.put("railGrade", rs.getRailGrade());
            sampleInfo.put("dutyId", rs.getDutyId());
            sampleInfo.put("sampleId", rs.getSampleId());
            sampleInfo.put("createDate", rs.getCreateDate());
            result.add(sampleInfo);
        }

        System.out.println("DEBUG: Found " + result.size() + " retest samples for heat number: " + heatNumber);
        return result;
    }

    @Override
    @Transactional
    public void forceDeleteRetestSamples(String heatNumber) {
        List<RetestSample> retestSamples = rsr.findByHeatNumber(heatNumber);
        if (!retestSamples.isEmpty()) {
            System.out.println("DEBUG: Force deleting " + retestSamples.size() + " retest samples for heat number: " + heatNumber);
            for (RetestSample rs : retestSamples) {
                System.out.println("DEBUG: Force deleting retest sample ID: " + rs.getRetestSampleId() +
                                 ", Heat: " + rs.getHeatNumber() + ", Strand: " + rs.getStrandNumber() +
                                 ", TestType: " + rs.getTestType() + ", SampleLot: " + rs.getSampleLot());
            }
            rsr.deleteAll(retestSamples);
            System.out.println("DEBUG: Successfully force deleted all retest samples for heat number: " + heatNumber);
        } else {
            System.out.println("DEBUG: No retest samples found to delete for heat number: " + heatNumber);
        }
    }

}
