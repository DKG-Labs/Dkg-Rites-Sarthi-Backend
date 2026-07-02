package com.sarthi.Sms.service.Impl;


import com.sarthi.Sms.dto.sms.*;
import com.sarthi.Sms.dto.sms.common.*;
import com.sarthi.Sms.entity.sms.*;
import com.sarthi.Sms.exception.SmsErrorDetails;
import com.sarthi.Sms.exception.SmsInvalidArgumentException;
import com.sarthi.Sms.exception.SmsResourceNotFoundException;
import com.sarthi.Sms.repository.sms.*;
import com.sarthi.Sms.service.DutySequenceService;
import com.sarthi.Sms.service.SmsService;

import com.sarthi.Sms.util.DutyEnum;
import com.sarthi.Sms.util.SmsCommonUtils;
import com.sarthi.Sms.util.SmsHeatStageEnum;
import com.sarthi.constant.AppConstant;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

@Service
public class SmsServiceImpl implements SmsService {

    @Autowired
    private SmsDutyRepository sdr;

    @Autowired
    private HeatDtlRepository hdr;

    @Autowired
    private DutySequenceService dss;

    @Autowired
    private DutyHeatRelationRepository dhrr;

    @Autowired
    private BloomDtlRepository bdr;

    @Autowired
    private HeatDtlSms2Repository hds2r;

    @Autowired
    private HeatDtlSms3Repository hds3r;

    @Autowired
    private DutyHeatSms2Repository dhs2r;

    @Autowired
    private DutyHeatSms3Repository dhs3r;

    @Autowired
    private BloomDtlSms2Repository bds2r;

    @Autowired
    private BloomDtlSms3Repository bds3r;

    @Override
    @Transactional
    public StartDutyResDto startDuty(String ah, StartDutyReqDto req) {
        SmsDutyEntity se = new SmsDutyEntity();
        Integer userId = SmsCommonUtils.getUserIdFromAuthHeader(ah);

        // check if a duty is already in progres
        DutyStatusResDto dsrd = checkDutyStatus(ah);
        if (dsrd.getIsDutyInProgess()) {
            throw new SmsInvalidArgumentException(new SmsErrorDetails(AppConstant.USER_ALREADY_EXISTS,
                    AppConstant.USER_ALREADY_EXISTS, AppConstant.ERROR_TYPE_VALIDATION,
                    "Cannot start new duty for the user. Previous duty already in progress"));
        }

        LocalDate date = SmsCommonUtils.convertStringToDateObject(req.getStartDate());

        String dutyId = dss.generateDutyId(DutyEnum.SMS, date);
        se.setDutyId(dutyId);
        se.setUserId(userId);
        se.setDate(date);
        se.setRailGrade(req.getRailGrade());
        se.setShift(req.getShift());
        se.setStartTime(LocalDateTime.now());
        se.setSms(req.getSms());
        se = sdr.save(se);

        StartDutyResDto sdrd = new StartDutyResDto();
        sdrd.setDate(SmsCommonUtils.convertDateToString(se.getDate()));
        sdrd.setDutyId(se.getDutyId());
        sdrd.setShift(se.getShift());
        sdrd.setSms(se.getSms());
        sdrd.setStartTime(SmsCommonUtils.extractTime(se.getStartTime()));
        sdrd.setRailGrade(se.getRailGrade());
        return sdrd;
    }

    @Override
    @Transactional
    public void endDuty(EndDutyReqDto req) {
        SmsDutyEntity sde = sdr.findByDutyId(req.getDutyId())
                .orElseThrow(() -> new SmsResourceNotFoundException(
                        new SmsErrorDetails(AppConstant.ERROR_CODE_RESOURCE,
                                AppConstant.ERROR_TYPE_CODE_RESOURCE,
                                AppConstant.ERROR_TYPE_RESOURCE,
                                "No duties found for the provided duty id.")));

        if (Objects.nonNull(sde.getEndTime())) {
            throw new SmsInvalidArgumentException(
                    new SmsErrorDetails(
                            AppConstant.ERROR_CODE_VALIDATION,
                            AppConstant.ERROR_TYPE_CODE_VALIDATION,
                            AppConstant.ERROR_TYPE_VALIDATION,
                            "Duty already ended."));
        }
        sde.setShiftRemarks(req.getShiftRemarks());
        sde.setEndTime(LocalDateTime.now());
        sdr.save(sde);
    }

    @Override
    public DutyStatusResDto checkDutyStatus(String ah) {
        Integer userId = SmsCommonUtils.getUserIdFromAuthHeader(ah);
        String dutyId = sdr.checkDutyStatus(userId);
        DutyStatusResDto dsrd = new DutyStatusResDto();
        dsrd.setDutyId(dutyId);
        dsrd.setIsDutyInProgess(dutyId != null);

        return dsrd;
    }

    @Override
    public StartDutyResDto getOngoingDutyDtls(String ah) {
        Integer userId = SmsCommonUtils.getUserIdFromAuthHeader(ah);
        Optional<SmsDutyEntity> sdeOpt = sdr.getOngoingDutyDtls(userId);

        StartDutyResDto sdrd = new StartDutyResDto();
        if (sdeOpt.isPresent()) {
            SmsDutyEntity sde = sdeOpt.get();
            sdrd.setDate(SmsCommonUtils.convertDateToString(sde.getDate()));
            sdrd.setDutyId(sde.getDutyId());
            sdrd.setShift(sde.getShift());
            sdrd.setSms(sde.getSms());
            sdrd.setStartTime(SmsCommonUtils.extractTime(sde.getStartTime()));
            sdrd.setRailGrade(sde.getRailGrade());
        }

        return sdrd;
    }

    @Override
    public ShiftSummaryResDto getSmsShiftSummaryDtls(String dutyId) {
        if (Objects.isNull(dutyId) || dutyId.isEmpty()) {
            throw new SmsInvalidArgumentException(new SmsErrorDetails(
                    AppConstant.ERROR_CODE_MISSING_FIELDS,
                    AppConstant.ERROR_TYPE_CODE_MISSING_FIELDS,
                    AppConstant.ERROR_TYPE_MISSING_FIELDS,
                    "Please pass dutyId parameter."));
        }

        // find ems functioning etc details from SmsDutyEntity
        SmsDutyEntity sde = sdr.findByDutyId(dutyId)
                .orElseThrow(() -> new SmsResourceNotFoundException(
                        new SmsErrorDetails(AppConstant.ERROR_CODE_RESOURCE,
                                AppConstant.ERROR_TYPE_CODE_RESOURCE,
                                AppConstant.ERROR_TYPE_RESOURCE,
                                "No duties found for the provided duty id.")));

        List<ShiftSummaryHeatDtlDto> hdl = new ArrayList<>();

        if (sde.getSms().equalsIgnoreCase("SMS 2")) {
            List<HeatDtlSms2Entity> hds2eList = hds2r.findHeatDetailsNotInBloomDetails(dutyId);
            hdl = hds2eList.stream().map(record -> {
                ShiftSummaryHeatDtlDto sshdd = new ShiftSummaryHeatDtlDto(record);
                return sshdd;
            }).collect((Collectors.toList()));


        } else {
            List<HeatDtlSms3Entity> hds3eList = hds3r.findHeatDetailsNotInBloomDetails(dutyId);
            hdl = hds3eList.stream().map(record -> {
                ShiftSummaryHeatDtlDto sshdd = new ShiftSummaryHeatDtlDto(record);
                return sshdd;
            }).collect((Collectors.toList()));
        }

        // find heats worked in the current duty
        // step 1: find heat number associated with
        // the provided duty id in DutyHeatRelationEntity
        // List<DutyHeatRelationEntity> dutyHeatRelationEntityList = dhrr
        // .findByDutyHeatRelationIdDutyId(dutyId);

        // step 2 : find the heat dtls in HeatDtlEntity
        // List<ShiftSummaryHeatDtlDto> hdl = new ArrayList<ShiftSummaryHeatDtlDto>();
        // List<HeatDtlEntity> hdeList = hdr.findHeatDetailsNotInBloomDetails();
        // for (DutyHeatRelationEntity dhre : dutyHeatRelationEntityList) {
        // HeatDtlEntity hde = hdr
        // .findByHeatNo(dhre.getDutyHeatRelationId().getHeatNo())
        // .orElseThrow(() -> new ResourceNotFoundException(
        // new ErrorDetails(AppConstant.ERROR_CODE_RESOURCE,
        // AppConstant.ERROR_TYPE_CODE_RESOURCE,
        // AppConstant.ERROR_TYPE_RESOURCE,
        // "No heat detail found for the provided heat number.")));
        // ShiftSummaryHeatDtlDto shiftSummaryHeatDtlDto = new
        // ShiftSummaryHeatDtlDto(hde);
        // hdl.add(shiftSummaryHeatDtlDto);

        // }

        // List<ShiftSummaryHeatDtlDto> hdl = hdeList.stream().map(record -> {
        // ShiftSummaryHeatDtlDto sshdd = new ShiftSummaryHeatDtlDto(record);
        // return sshdd;
        // }).collect((Collectors.toList()));

        // step 3: make a response and send
        ShiftSummaryResDto ssrd = new ShiftSummaryResDto();
        ssrd.setAmlcFunctioning(sde.isAmlcFunctioning());
        ssrd.setDutyId(sde.getDutyId());
        ssrd.setEmsFunctioning(sde.isEmsFunctioning());
        ssrd.setHeatDtlList(hdl);
        ssrd.setHydrogenMeasurementAutomatic(sde.isHydrogenMeasurementAutomatic());
        ssrd.setLadleToTundishUsed(sde.isLadleToTundishUsed());
        ssrd.setMakeOfCastingPowder(sde.getMakeOfCastingPowder());
        ssrd.setMakeOfHydrisProbe(sde.getMakeOfHydrisProbe());
        ssrd.setSlagDetectorFunctioning(sde.isSlagDetectorFunctioning());
        ssrd.setTundishToMouldUsed(sde.isTundishToMouldUsed());

        // -----------------REQUIRES CHANGE WHEN CALIBRATION MODULE IS DONE----------
        ssrd.setHydrisClb(new HydrisClbResDto());

        LecoClbResDto lcrd1 = new LecoClbResDto("Leco 1", "20/04/2024");
        LecoClbResDto lcrd2 = new LecoClbResDto("Leco 2", "21/04/2024");
        LecoClbResDto lcrd3 = new LecoClbResDto("Leco 3", "22/04/2024");
        LecoClbResDto lcrd4 = new LecoClbResDto("Leco 4", "23/04/2024");

        List<LecoClbResDto> lcrdList = new ArrayList<>();
        lcrdList.add(lcrd1);
        lcrdList.add(lcrd2);
        lcrdList.add(lcrd3);
        lcrdList.add(lcrd4);
        ssrd.setLecoClbList(lcrdList);

        // --------------------------------------------------------------

        return ssrd;
    }

    @Override
    @Transactional
    public void saveShiftSummaryDtls(ShiftSummaryReqDto req) {
        SmsDutyEntity sde = sdr.findByDutyId(req.getDutyId())
                .orElseThrow(() -> new SmsResourceNotFoundException(
                        new SmsErrorDetails(AppConstant.ERROR_CODE_RESOURCE,
                                AppConstant.ERROR_TYPE_CODE_RESOURCE,
                                AppConstant.ERROR_TYPE_RESOURCE,
                                "No duties found for the provided duty id.")));

        sde.setEmsFunctioning(req.getEmsFunctioning());
        sde.setSlagDetectorFunctioning(req.getSlagDetectorFunctioning());
        sde.setAmlcFunctioning(req.getAmlcFunctioning());
        sde.setHydrogenMeasurementAutomatic(req.getHydrogenMeasurementAutomatic());
        sde.setLadleToTundishUsed(req.getLadleToTundishUsed());
        sde.setTundishToMouldUsed(req.getTundishToMouldUsed());
        sde.setMakeOfCastingPowder(req.getMakeOfCastingPowder());
        sde.setMakeOfHydrisProbe(req.getMakeOfHydrisProbe());

        sdr.save(sde);
    }

    @Override
    @Transactional
    public void addNewHeat(AddHeatReqDto req) {
        if (Objects.isNull(req.getDutyId()) ||
                Objects.isNull(req.getHeatNo()) 
                ) {
            throw new SmsInvalidArgumentException(new SmsErrorDetails(
                    AppConstant.ERROR_CODE_MISSING_FIELDS,
                    AppConstant.ERROR_TYPE_CODE_MISSING_FIELDS,
                    AppConstant.ERROR_TYPE_MISSING_FIELDS,
                    "Missing fields. Please add the required fields."));
        }

        // Optional<HeatDtlEntity> hdeOpt = hdr.findByHeatNo(req.getHeatNo());

        SmsDutyEntity sde = sdr.findByDutyId(req.getDutyId())
                .orElseThrow(() -> new SmsResourceNotFoundException(
                        new SmsErrorDetails(AppConstant.ERROR_CODE_RESOURCE,
                                AppConstant.ERROR_TYPE_CODE_RESOURCE,
                                AppConstant.ERROR_TYPE_RESOURCE,
                                "Duty ID not present in the record.")));

        if (sde.getSms().equalsIgnoreCase(AppConstant.SMS_2)) {
            Optional<HeatDtlSms2Entity> hds2eOpt = hds2r.findByHeatNo(req.getHeatNo());

            if (hds2eOpt.isPresent()) {
                throw new SmsInvalidArgumentException(
                        new SmsErrorDetails(
                                AppConstant.ERROR_CODE_VALIDATION,
                                AppConstant.ERROR_TYPE_CODE_VALIDATION,
                                AppConstant.ERROR_TYPE_VALIDATION,
                                "Heat already present in the table."));
            }

            DutyHeatSms2Entity dhs2e = new DutyHeatSms2Entity();
            dhs2e.setDutyHeatRelationId(new DutyHeatRelationIdSms2(req.getDutyId(), req.getHeatNo()));
            // dhs2e.setDutyHeatRelationId(new DutyHeatRelationIdSms2(req.getDutyId(), req.getHeatNo()));
            dhs2e.setHeatProcurementStage(SmsHeatStageEnum.NEW.getDescription());
            dhs2e.setHeatSurrenderStage(SmsHeatStageEnum.CONVERTER.getDescription());

            HeatDtlSms2Entity hds2e = new HeatDtlSms2Entity();
            hds2e.setHeatNo(req.getHeatNo());
            hds2e.setTurnDownTemp(req.getTurnDownTemp());
            hds2e.setTurnDownTempWv(req.getTurnDownTempWv());
            hds2e.setHeatStage(SmsHeatStageEnum.CONVERTER.getDescription());
            hds2e.setCreatedAt((LocalDateTime.now()));

            hds2r.save(hds2e);
            dhs2r.save(dhs2e);
        } else if (sde.getSms().equalsIgnoreCase(AppConstant.SMS_3)) {
            System.out.println("SMS # ADD NEW HEAT");
            Optional<HeatDtlSms3Entity> hds3eOpt = hds3r.findByHeatNo(req.getHeatNo());

            if (hds3eOpt.isPresent()) {
                throw new SmsInvalidArgumentException(
                        new SmsErrorDetails(
                                AppConstant.ERROR_CODE_VALIDATION,
                                AppConstant.ERROR_TYPE_CODE_VALIDATION,
                                AppConstant.ERROR_TYPE_VALIDATION,
                                "Heat already present in the table."));
            }

            DutyHeatSms3Entity dhs3e = new DutyHeatSms3Entity();
            dhs3e.setDutyHeatRelationId(new DutyHeatRelationIdSms3(req.getDutyId(), req.getHeatNo()));
            // dhs3e.setDutyHeatRelationId(new DutyHeatRelationIdSms3(req.getDutyId(), req.getHeatNo()));
            dhs3e.setHeatProcurementStage(SmsHeatStageEnum.NEW.getDescription());
            dhs3e.setHeatSurrenderStage(SmsHeatStageEnum.CONVERTER.getDescription());

            HeatDtlSms3Entity hds3e = new HeatDtlSms3Entity();
            hds3e.setHeatNo(req.getHeatNo());
            hds3e.setTurnDownTemp(req.getTurnDownTemp());
            hds3e.setTurnDownTempWv(req.getTurnDownTempWv());
            hds3e.setHeatStage(SmsHeatStageEnum.CONVERTER.getDescription());
            hds3e.setCreatedAt((LocalDateTime.now()));

            hds3r.save(hds3e);
            dhs3r.save(dhs3e);
            System.out.println("SAVED");
        } else {
            throw new SmsInvalidArgumentException(
                    new SmsErrorDetails(
                            AppConstant.ERROR_CODE_VALIDATION,
                            AppConstant.ERROR_TYPE_CODE_VALIDATION,
                            AppConstant.ERROR_TYPE_VALIDATION,
                            "Provided SMS number does not match any data."));
        }

        // if (hdeOpt.isPresent()) {
        // throw new InvalidArgumentException(
        // new ErrorDetails(
        // AppConstant.ERROR_CODE_VALIDATION,
        // AppConstant.ERROR_TYPE_CODE_VALIDATION,
        // AppConstant.ERROR_TYPE_VALIDATION,
        // "Heat already present in the list or heat marked as diverted before."));
        // }

        // DutyHeatRelationEntity dhre = new DutyHeatRelationEntity();
        // dhre.setDutyHeatRelationId(new DutyHeatRelationId(req.getDutyId(),
        // req.getHeatNo()));
        // dhre.setHeatProcurementStage(SmsHeatStageEnum.NEW.getDescription());
        // dhre.setHeatSurrenderStage(SmsHeatStageEnum.CONVERTER.getDescription());

        // HeatDtlEntity hde = new HeatDtlEntity();
        // hde.setHeatNo(req.getHeatNo());
        // hde.setTurnDownTemp(req.getTurnDownTemp());
        // hde.setTurnDownTempWv(req.getTurnDownTempWv());
        // hde.setHeatStage(SmsHeatStageEnum.CONVERTER.getDescription());

        // hdr.save(hde);
        // dhrr.save(dhre);
    }

    // @Override
    @Transactional
    public HeatDtlsResDto getHeatDtls(String heatNo, String dutyId) {
        ModelMapper modelMapper = new ModelMapper();
        HeatDtlsResDto hdrd = new HeatDtlsResDto();

        SmsDutyEntity sde = sdr.findByDutyId(dutyId)
                .orElseThrow(() -> new SmsResourceNotFoundException(
                        new SmsErrorDetails(AppConstant.ERROR_CODE_RESOURCE,
                                AppConstant.ERROR_TYPE_CODE_RESOURCE,
                                AppConstant.ERROR_TYPE_RESOURCE,
                                "No duties found for the provided duty id.")));

        if (sde.getSms().equalsIgnoreCase(AppConstant.SMS_2)) {
            HeatDtlSms2Entity hds2e = hds2r.findByHeatNo(heatNo)
                    .orElseThrow(() -> new SmsResourceNotFoundException(
                            new SmsErrorDetails(AppConstant.ERROR_CODE_RESOURCE,
                                    AppConstant.ERROR_TYPE_CODE_RESOURCE,
                                    AppConstant.ERROR_TYPE_RESOURCE,
                                    "No heat detail found for the provided heat number in SMS 2. Add turn down temp and save the heat to start new heat in SMS 2.")));

            hdrd = modelMapper.map(hds2e, HeatDtlsResDto.class);
        } else if (sde.getSms().equalsIgnoreCase(AppConstant.SMS_3)) {
            HeatDtlSms3Entity hds3e = hds3r.findByHeatNo(heatNo)
                    .orElseThrow(() -> new SmsResourceNotFoundException(
                            new SmsErrorDetails(AppConstant.ERROR_CODE_RESOURCE,
                                    AppConstant.ERROR_TYPE_CODE_RESOURCE,
                                    AppConstant.ERROR_TYPE_RESOURCE,
                                    "No heat detail found for the provided heat number in SMS 2. Add turn down temp and save the heat to start new heat in SMS 2.")));

            hdrd = modelMapper.map(hds3e, HeatDtlsResDto.class);
        }

        return hdrd;

        // HeatDtlEntity hde = hdr.findByHeatNo(heatNo)
        // .orElseThrow(() -> new ResourceNotFoundException(
        // new ErrorDetails(AppConstant.ERROR_CODE_RESOURCE,
        // AppConstant.ERROR_TYPE_CODE_RESOURCE,
        // AppConstant.ERROR_TYPE_RESOURCE,
        // "No heat detail found for the provided heat number. Add turn down temp and
        // save the heat to start new heat.")));

        // HeatDtlsResDto hdrd1 = modelMapper.map(hde, HeatDtlsResDto.class);

        // HeatDtlsResDto hdrd = new HeatDtlsResDto();
        // hdrd.setHeatNo(hde.getHeatNo());
        // hdrd.setTurnDownTempWv(hde.getTurnDownTempWv());
        // hdrd.setDegassingVacuumWv(hde.getDegassingVacuumWv());
        // hdrd.setDegassingDurationWv(hde.getDegassingDurationWv());
        // hdrd.setHeatStage(hde.getHeatStage());
        // hdrd.setTurnDownTemp(hde.getTurnDownTemp());
        // hdrd.setDegassingVacuum(hde.getDegassingVacuum());
        // hdrd.setDegassingDuration(hde.getDegassingDuration());
        // hdrd.setCastingTemp(hde.getCastingTemp());
        // hdrd.setCastingTemp2(hde.getCastingTemp2());
        // hdrd.setCasterNo(hde.getCasterNo());
        // hdrd.setSequenceNo(hde.getSequenceNo());
        // hdrd.setHydris(hde.getHydris());
        // hdrd.setIsProbeDipped(hde.getIsProbeDipped());
        // hdrd.setIsHydrogenBw80And100(hde.getIsHydrogenBw80And100());
        // hdrd.setNitrogen(hde.getNitrogen());
        // hdrd.setOxygen(hde.getOxygen());
        // hdrd.setNoOfPrimeBlooms(hde.getNoOfPrimeBlooms());
        // hdrd.setPrimeBloomsLength(hde.getPrimeBloomsLength());
        // hdrd.setPrimeBloomsTotalLength(hde.getPrimeBloomsTotalLength());
        // hdrd.setNoOfCoBlooms(hde.getNoOfCoBlooms());
        // hdrd.setCoBloomsLength(hde.getCoBloomsLength());
        // hdrd.setCoBloomsTotalLength(hde.getCoBloomsTotalLength());
        // hdrd.setNoOfRejectedBlooms(hde.getNoOfRejectedBlooms());
        // hdrd.setRejectedBloomsLength(hde.getRejectedBloomsLength());
        // hdrd.setRejectedBloomsTotalLength(hde.getRejectedBloomsTotalLength());
        // hdrd.setWeightOfPrimeBlooms(hde.getWeightOfPrimeBlooms());
        // hdrd.setWeightOfCoBlooms(hde.getWeightOfCoBlooms());
        // hdrd.setWeightOfRejectedBlooms(hde.getWeightOfRejectedBlooms());
        // hdrd.setTotalCastWt(hde.getTotalCastWt());
        // hdrd.setSentToLadle(hde.getSentToLadle());
        // hdrd.setHeatRemark(hde.getHeatRemark());
        // hdrd.setIsDiverted(hde.getIsDiverted());

        // return hdrd1;
    }

    @Override
    @Transactional
    public void updateHeatDtls(UpdateHeatReqDto req) {
        SmsDutyEntity sde = sdr.findByDutyId(req.getDutyId())
                .orElseThrow(() -> new SmsResourceNotFoundException(
                        new SmsErrorDetails(AppConstant.ERROR_CODE_RESOURCE,
                                AppConstant.ERROR_TYPE_CODE_RESOURCE,
                                AppConstant.ERROR_TYPE_RESOURCE,
                                "No duties found for the provided duty id.")));

        // DIVERTED HEAT INFO - Allow editing diverted heats in same shift to change status
        // Users can edit diverted heats to make them non-diverted if needed

        if (sde.getSms().equalsIgnoreCase(AppConstant.SMS_2)) {
            HeatDtlSms2Entity hds2e = hds2r.findByHeatNo(req.getHeatNo())
                    .orElseThrow(() -> new SmsResourceNotFoundException(
                            new SmsErrorDetails(AppConstant.ERROR_CODE_RESOURCE,
                                    AppConstant.ERROR_TYPE_CODE_RESOURCE,
                                    AppConstant.ERROR_TYPE_RESOURCE,
                                    "No heat detail found for the provided heat number. Add new heat.")));

            Optional<DutyHeatSms2Entity> dhs2eOpt = dhs2r
                    .findById(new DutyHeatRelationIdSms2(req.getDutyId(), req.getHeatNo()));
            DutyHeatSms2Entity dhs2e = null;

            if (dhs2eOpt.isPresent()) {
                dhs2e = dhs2eOpt.get();
            } else {
                dhs2e = new DutyHeatSms2Entity();
                dhs2e.setDutyHeatRelationId(
                        new DutyHeatRelationIdSms2(req.getDutyId(), req.getHeatNo()));
                dhs2e.setHeatProcurementStage(hds2e.getHeatStage());
            }

            String newHeatStage = getNewHeatStage(req);

            hds2e.setHeatStage(newHeatStage);
            hds2e.setTurnDownTemp(req.getTurnDownTemp());
            hds2e.setOtherRemark(req.getOtherRemark());
            hds2e.setDegassingVacuum(req.getDegassingVacuum());
            hds2e.setDegassingDuration(req.getDegassingDuration());
            hds2e.setCastingTemp(req.getCastingTemp());
            hds2e.setCastingTemp2(req.getCastingTemp2());
            hds2e.setCasterNo(req.getCasterNo());
            hds2e.setSequenceNo(req.getSequenceNo());
            hds2e.setHydris(req.getHydris());
            hds2e.setIsProbeDipped(req.getIsProbeDipped());
            hds2e.setIsHydrogenBw80And100(req.getIsHydrogenBw80And100());
            hds2e.setNitrogen(req.getNitrogen());
            hds2e.setOxygen(req.getOxygen());
            hds2e.setSentToLadle(req.getSentToLadle());
            hds2e.setNoOfPrimeBlooms(req.getNoOfPrimeBlooms());
            hds2e.setPrimeBloomsLength(req.getPrimeBloomsLength());
            hds2e.setPrimeBloomsTotalLength(req.getPrimeBloomsTotalLength());
            hds2e.setWeightOfPrimeBlooms(req.getWeightOfPrimeBlooms());
            hds2e.setNoOfCoBlooms(req.getNoOfCoBlooms());
            hds2e.setCoBloomsLength(req.getCoBloomsLength());
            hds2e.setCoBloomsTotalLength(req.getCoBloomsTotalLength());
            hds2e.setWeightOfCoBlooms(req.getWeightOfCoBlooms());
            hds2e.setNoOfRejectedBlooms(req.getNoOfRejectedBlooms());
            hds2e.setRejectedBloomsLength(req.getRejectedBloomsLength());
            hds2e.setRejectedBloomsTotalLength(req.getRejectedBloomsTotalLength());
            hds2e.setWeightOfRejectedBlooms(req.getWeightOfRejectedBlooms());
            hds2e.setTotalCastWt(req.getTotalCastWt());
            hds2e.setIsDiverted(req.getIsDiverted());
            hds2e.setHeatRemark(req.getHeatRemark());
            hds2e.setTurnDownTempWv(req.getTurnDownTempWv());
            hds2e.setDegassingVacuumWv(req.getDegassingVacuumWv());
            hds2e.setDegassingDurationWv(req.getDegassingDurationWv());
            hds2e.setUpdatedAt(LocalDateTime.now());
            // Set heatSurrenderStage for dutyHeatEntity, it will be the newHeatStage
            dhs2e.setHeatSurrenderStage(newHeatStage);

            // now save
            hds2r.save(hds2e);
            dhs2r.save(dhs2e);
        } else if (sde.getSms().equalsIgnoreCase(AppConstant.SMS_3)) {
            // HeatDtlSms3Entity hds3e = hds3r.findByHeatNo(req.getHeatNo())
            // .orElseThrow(() -> new ResourceNotFoundException(
            // new ErrorDetails(AppConstant.ERROR_CODE_RESOURCE,
            // AppConstant.ERROR_TYPE_CODE_RESOURCE,
            // AppConstant.ERROR_TYPE_RESOURCE,
            // "No heat detail found for the provided heat number. Add new heat.")));

            // DutyHeatSms3Entity dhs3e = new DutyHeatSms3Entity();
            // dhs3e.setDutyHeatRelationId(
            // new DutyHeatRelationId(req.getDutyId(), req.getHeatNo()));
            // dhs3e.setHeatProcurementStage(hds3e.getHeatStage());

            HeatDtlSms3Entity hds3e = hds3r.findByHeatNo(req.getHeatNo())
                    .orElseThrow(() -> new SmsResourceNotFoundException(
                            new SmsErrorDetails(AppConstant.ERROR_CODE_RESOURCE,
                                    AppConstant.ERROR_TYPE_CODE_RESOURCE,
                                    AppConstant.ERROR_TYPE_RESOURCE,
                                    "No heat detail found for the provided heat number. Add new heat.")));

            Optional<DutyHeatSms3Entity> dhs3eOpt = dhs3r
                    .findById(new DutyHeatRelationIdSms3(req.getDutyId(), req.getHeatNo()));
            DutyHeatSms3Entity dhs3e = null;

            if (dhs3eOpt.isPresent()) {
                dhs3e = dhs3eOpt.get();
            } else {
                dhs3e = new DutyHeatSms3Entity();
                dhs3e.setDutyHeatRelationId(
                        new DutyHeatRelationIdSms3(req.getDutyId(), req.getHeatNo()));
                dhs3e.setHeatProcurementStage(hds3e.getHeatStage());
            }

            String newHeatStage = getNewHeatStage(req);

            hds3e.setHeatStage(newHeatStage);
            hds3e.setTurnDownTemp(req.getTurnDownTemp());
            hds3e.setOtherRemark(req.getOtherRemark());
            hds3e.setDegassingVacuum(req.getDegassingVacuum());
            hds3e.setDegassingDuration(req.getDegassingDuration());
            hds3e.setCastingTemp(req.getCastingTemp());
            hds3e.setCastingTemp2(req.getCastingTemp2());
            hds3e.setCasterNo(req.getCasterNo());
            hds3e.setSequenceNo(req.getSequenceNo());
            hds3e.setHydris(req.getHydris());
            hds3e.setIsProbeDipped(req.getIsProbeDipped());
            hds3e.setIsHydrogenBw80And100(req.getIsHydrogenBw80And100());
            hds3e.setNitrogen(req.getNitrogen());
            hds3e.setOxygen(req.getOxygen());
            hds3e.setSentToLadle(req.getSentToLadle());
            hds3e.setNoOfPrimeBlooms(req.getNoOfPrimeBlooms());
            hds3e.setPrimeBloomsLength(req.getPrimeBloomsLength());
            hds3e.setPrimeBloomsTotalLength(req.getPrimeBloomsTotalLength());
            hds3e.setWeightOfPrimeBlooms(req.getWeightOfPrimeBlooms());
            hds3e.setNoOfCoBlooms(req.getNoOfCoBlooms());
            hds3e.setCoBloomsLength(req.getCoBloomsLength());
            hds3e.setCoBloomsTotalLength(req.getCoBloomsTotalLength());
            hds3e.setWeightOfCoBlooms(req.getWeightOfCoBlooms());
            hds3e.setNoOfRejectedBlooms(req.getNoOfRejectedBlooms());
            hds3e.setRejectedBloomsLength(req.getRejectedBloomsLength());
            hds3e.setRejectedBloomsTotalLength(req.getRejectedBloomsTotalLength());
            hds3e.setWeightOfRejectedBlooms(req.getWeightOfRejectedBlooms());
            hds3e.setTotalCastWt(req.getTotalCastWt());
            hds3e.setIsDiverted(req.getIsDiverted());
            hds3e.setHeatRemark(req.getHeatRemark());
            hds3e.setTurnDownTempWv(req.getTurnDownTempWv());
            hds3e.setDegassingVacuumWv(req.getDegassingVacuumWv());
            hds3e.setDegassingDurationWv(req.getDegassingDurationWv());
            hds3e.setUpdatedAt(LocalDateTime.now());
            // set heatSurrenderStage for dutyHeatEntity, it will be the newHeatStage
            dhs3e.setHeatSurrenderStage(newHeatStage);
            // now save
            hds3r.save(hds3e);
            dhs3r.save(dhs3e);
        } 


        else {
            throw new SmsInvalidArgumentException(
                    new SmsErrorDetails(
                            AppConstant.ERROR_CODE_VALIDATION,
                            AppConstant.ERROR_TYPE_CODE_VALIDATION,
                            AppConstant.ERROR_TYPE_VALIDATION,
                            "Provided SMS number does not match any data."));
        }

        // // Get heat dtl from heatDtlEntity. If not present, throw error to add new
        // heat.
        // HeatDtlEntity hde = hdr.findByHeatNo(req.getHeatNo())
        // .orElseThrow(() -> new ResourceNotFoundException(
        // new ErrorDetails(AppConstant.ERROR_CODE_RESOURCE,
        // AppConstant.ERROR_TYPE_CODE_RESOURCE,
        // AppConstant.ERROR_TYPE_RESOURCE,
        // "No heat detail found for the provided heat number. Add new heat.")));

        // // If heat is present
        // // Update dutyHeatRelation entity with dutyId, heatNo and heatProcuementStage
        // // heatProcurement stage will be the heatStage of heatDtlEntity found above
        // DutyHeatRelationEntity dhre = new DutyHeatRelationEntity();
        // dhre.setDutyHeatRelationId(
        // new DutyHeatRelationId(req.getDutyId(), req.getHeatNo()));
        // dhre.setHeatProcurementStage(hde.getHeatStage());

        // // We need to change the heatStage of heats based on the below logic
        // String newHeatStage = null;
        // if (req.getWeightOfPrimeBlooms() != null &&
        // req.getWeightOfCoBlooms() != null &&
        // req.getWeightOfRejectedBlooms() != null &&
        // req.getTotalCastWt() != null) {
        // newHeatStage = SmsHeatStageEnum.BLOOM.getDescription();
        // } else if (req.getNitrogen() != null &&
        // req.getOxygen() != null &&
        // req.getSentToLadle() != null) {
        // newHeatStage = SmsHeatStageEnum.CHEMICAL.getDescription();
        // } else if (req.getCastingTemp() != null &&
        // req.getCasterNo() != null &&
        // req.getSequenceNo() != null &&
        // req.getIsHydrogenBw80And100() != null &&
        // req.getIsProbeDipped()) {
        // newHeatStage = SmsHeatStageEnum.CASTING.getDescription();
        // } else if (req.getDegassingDuration() != null &&
        // req.getDegassingVacuum() != null) {
        // newHeatStage = SmsHeatStageEnum.DEGASSING.getDescription();
        // } else if (req.getTurnDownTemp() != null) {
        // newHeatStage = SmsHeatStageEnum.CONVERTER.getDescription();
        // }

        // hde.setHeatStage(newHeatStage);
        // hde.setTurnDownTemp(req.getTurnDownTemp());
        // hde.setDegassingVacuum(req.getDegassingVacuum());
        // hde.setDegassingDuration(req.getDegassingDuration());
        // hde.setCastingTemp(req.getCastingTemp());
        // hde.setCastingTemp2(req.getCastingTemp2());
        // hde.setCasterNo(req.getCasterNo());
        // hde.setSequenceNo(req.getSequenceNo());
        // hde.setHydris(req.getHydris());
        // hde.setIsProbeDipped(req.getIsProbeDipped());
        // hde.setIsHydrogenBw80And100(req.getIsHydrogenBw80And100());
        // hde.setNitrogen(req.getNitrogen());
        // hde.setOxygen(req.getOxygen());
        // hde.setSentToLadle(req.getSentToLadle());
        // hde.setNoOfPrimeBlooms(req.getNoOfPrimeBlooms());
        // hde.setPrimeBloomsLength(req.getPrimeBloomsLength());
        // hde.setPrimeBloomsTotalLength(req.getPrimeBloomsTotalLength());
        // hde.setWeightOfPrimeBlooms(req.getWeightOfPrimeBlooms());
        // hde.setNoOfCoBlooms(req.getNoOfCoBlooms());
        // hde.setCoBloomsLength(req.getCoBloomsLength());
        // hde.setCoBloomsTotalLength(req.getCoBloomsTotalLength());
        // hde.setWeightOfCoBlooms(req.getWeightOfCoBlooms());
        // hde.setNoOfRejectedBlooms(req.getNoOfRejectedBlooms());
        // hde.setRejectedBloomsLength(req.getRejectedBloomsLength());
        // hde.setRejectedBloomsTotalLength(req.getRejectedBloomsTotalLength());
        // hde.setWeightOfRejectedBlooms(req.getWeightOfRejectedBlooms());
        // hde.setTotalCastWt(req.getTotalCastWt());
        // hde.setIsDiverted(req.getIsDiverted());
        // hde.setHeatRemark(req.getHeatRemark());
        // hde.setTurnDownTempWv(req.getTurnDownTempWv());
        // hde.setDegassingVacuumWv(req.getDegassingVacuumWv());
        // hde.setDegassingDurationWv(req.getDegassingDurationWv());

        // // Set heatSurrenderStage for dutyHeatEntity, it will be the newHeatStage
        // dhre.setHeatSurrenderStage(newHeatStage);

        // // now save
        // hdr.save(hde);
        // dhrr.save(dhre);
    }

    private String getNewHeatStage(UpdateHeatReqDto req) {
        String newHeatStage = null;
        if (req.getWeightOfPrimeBlooms() != null &&
                req.getWeightOfCoBlooms() != null &&
                req.getWeightOfRejectedBlooms() != null &&
                req.getTotalCastWt() != null) {
            return SmsHeatStageEnum.BLOOM.getDescription();
        } else if (req.getNitrogen() != null &&
                req.getSentToLadle() != null) {
            return SmsHeatStageEnum.CHEMICAL.getDescription();
        } else if (req.getCastingTemp() != null &&
                req.getCastingTemp2() != null &&
                req.getCasterNo() != null &&
                req.getSequenceNo() != null)
                // req.getIsHydrogenBw80And100() != null &&
                // req.getIsProbeDipped())
                {
            return SmsHeatStageEnum.CASTING.getDescription();
        } else if (req.getDegassingDuration() != null &&
                req.getDegassingVacuum() != null) {
            return SmsHeatStageEnum.DEGASSING.getDescription();
        } else if (req.getTurnDownTemp() != null) {
            return SmsHeatStageEnum.CONVERTER.getDescription();
        }

        return newHeatStage;
    }

    @Override
    public BloomDtlResDto getBloomDtls(String castNo, String dutyId) {
        BloomDtlResDto bdrd = new BloomDtlResDto();
        SmsDutyEntity sde = sdr.findByDutyId(dutyId)
                .orElseThrow(() -> new SmsResourceNotFoundException(
                        new SmsErrorDetails(AppConstant.ERROR_CODE_RESOURCE,
                                AppConstant.ERROR_TYPE_CODE_RESOURCE,
                                AppConstant.ERROR_TYPE_RESOURCE,
                                "No duties found for the provided duty id.")));

        if (sde.getSms().equalsIgnoreCase(AppConstant.SMS_2)) {
            HeatDtlSms2Entity hds2e = validateSms2Heat(castNo);

            // if(!hds2e.getHeatStage().equalsIngoreCase(SmsHeatStageEnum.BLOOM.getDescription())){

            // }

            if (!SmsHeatStageEnum.BLOOM.getDescription().equalsIgnoreCase(hds2e.getHeatStage())) {
                throw new SmsInvalidArgumentException(
                        new SmsErrorDetails(
                                AppConstant.ERROR_CODE_VALIDATION,
                                AppConstant.ERROR_TYPE_CODE_VALIDATION,
                                AppConstant.ERROR_TYPE_VALIDATION,
                                "Heat did not complete blooming stage in SMS 2. Cannot fetch details."));
            }

            bdrd.setCastNo(hds2e.getHeatNo());
            bdrd.setNoOfCoBlooms(hds2e.getNoOfCoBlooms());
            bdrd.setNoOfPrimeBlooms(hds2e.getNoOfPrimeBlooms());
        } else if (sde.getSms().equalsIgnoreCase(AppConstant.SMS_3)) {
            HeatDtlSms3Entity hds3e = validateSms3Heat(castNo);

            // if(!hds2e.getHeatStage().equalsIngoreCase(SmsHeatStageEnum.BLOOM.getDescription())){

            // }

            if (!SmsHeatStageEnum.BLOOM.getDescription().equalsIgnoreCase(hds3e.getHeatStage())) {
                throw new SmsInvalidArgumentException(
                        new SmsErrorDetails(
                                AppConstant.ERROR_CODE_VALIDATION,
                                AppConstant.ERROR_TYPE_CODE_VALIDATION,
                                AppConstant.ERROR_TYPE_VALIDATION,
                                "Heat did not complete blooming stage in SMS 3. Cannot fetch details."));
            }

            bdrd.setCastNo(hds3e.getHeatNo());
            bdrd.setNoOfCoBlooms(hds3e.getNoOfCoBlooms());
            bdrd.setNoOfPrimeBlooms(hds3e.getNoOfPrimeBlooms());
        }

        return bdrd;

        // HeatDtlEntity hde = validateHeatNo(castNo);

        // // if heat did not complete bloom stage
        // if
        // (!hde.getHeatStage().equalsIgnoreCase(SmsHeatStageEnum.BLOOM.getDescription()))
        // {
        // throw new InvalidArgumentException(
        // new ErrorDetails(
        // AppConstant.ERROR_CODE_VALIDATION,
        // AppConstant.ERROR_TYPE_CODE_VALIDATION,
        // AppConstant.ERROR_TYPE_VALIDATION,
        // "Heat did not complete blooming stage. Cannot fetch details."));
        // }

        // BloomDtlResDto bdrd = new BloomDtlResDto();
        // bdrd.setCastNo(hde.getHeatNo());
        // bdrd.setNoOfCoBlooms(hde.getNoOfCoBlooms());
        // bdrd.setNoOfPrimeBlooms(hde.getNoOfPrimeBlooms());

        // return bdrd;
    }

    @Override
    @Transactional
    public void saveBloomInsp(BloomInspReqDto req) {

        SmsDutyEntity sde = sdr.findByDutyId(req.getDutyId())
                .orElseThrow(() -> new SmsResourceNotFoundException(
                        new SmsErrorDetails(AppConstant.ERROR_CODE_RESOURCE,
                                AppConstant.ERROR_TYPE_CODE_RESOURCE,
                                AppConstant.ERROR_TYPE_RESOURCE,
                                "No duties found for the provided duty id.")));

        if (sde.getSms().equalsIgnoreCase(AppConstant.SMS_2)) {
            System.out.println("CAST NUMBERL  " + req.getCastNo());
            HeatDtlSms2Entity hds2e = validateSms2Heat(req.getCastNo());

            // if(!hds2e.getHeatStage().equalsIngoreCase(SmsHeatStageEnum.BLOOM.getDescription())){

            // }

            if (!hds2e.getHeatStage().equalsIgnoreCase(SmsHeatStageEnum.BLOOM.getDescription())) {
                throw new SmsInvalidArgumentException(
                        new SmsErrorDetails(
                                AppConstant.ERROR_CODE_VALIDATION,
                                AppConstant.ERROR_TYPE_CODE_VALIDATION,
                                AppConstant.ERROR_TYPE_VALIDATION,
                                "Heat did not complete blooming stage in SMS 2. Cannot fetch details."));
            }

            ModelMapper mm = new ModelMapper();
            BloomDtlSms2Entity bds2e = mm.map(req, BloomDtlSms2Entity.class);
            bds2r.save(bds2e);
        } else if (sde.getSms().equalsIgnoreCase(AppConstant.SMS_3)) {
            HeatDtlSms3Entity hds3e = validateSms3Heat(req.getCastNo());

            // if(!hds2e.getHeatStage().equalsIngoreCase(SmsHeatStageEnum.BLOOM.getDescription())){

            // }

            if (!hds3e.getHeatStage().equalsIgnoreCase(SmsHeatStageEnum.BLOOM.getDescription())) {
                throw new SmsInvalidArgumentException(
                        new SmsErrorDetails(
                                AppConstant.ERROR_CODE_VALIDATION,
                                AppConstant.ERROR_TYPE_CODE_VALIDATION,
                                AppConstant.ERROR_TYPE_VALIDATION,
                                "Heat did not complete blooming stage in SMS 3. Cannot save bloom inspection."));
            }

            ModelMapper mm = new ModelMapper();
            BloomDtlSms3Entity bds3e = mm.map(req, BloomDtlSms3Entity.class);
            bds3r.save(bds3e);
        }

        // validateHeatNo(req.getCastNo());
        // ModelMapper modelMapper = new ModelMapper();
        // BloomDtlEntity bde = modelMapper.map(req, BloomDtlEntity.class);
        // bdr.save(bde);
    }

    @Override
    @Transactional
    public List<ReportResDto> getSmsReport(ReportReqDto req) {
        LocalDateTime startDate = SmsCommonUtils.convertStringToDateObject(req.getStartDate()).atStartOfDay();
        LocalDateTime endDate = SmsCommonUtils.convertStringToDateObject(req.getEndDate()).atTime(LocalTime.MAX);

        List<Object[]> res = hdr.getRecordDtl(startDate, endDate);

        // Apply shift-specific date range filtering if both startShift and endShift are provided
        if (req.getStartShift() != null && req.getEndShift() != null) {
            return res.stream()
                .map(this::mapToReportResDto)
                .filter(dto -> isWithinShiftDateRange(dto, req))
                .collect(Collectors.toList());
        }

        return res.stream().map(this::mapToReportResDto).collect(Collectors.toList());

    }

    /**
     * Helper method to check if a record falls within the shift-specific date range
     * @param dto The report data
     * @param req The request with startShift and endShift
     * @return true if the record is within the specified shift date range
     */
    private boolean isWithinShiftDateRange(ReportResDto dto, ReportReqDto req) {
        try {
            LocalDate recordDate = dto.getDate().toLocalDate();
            LocalDate startDate = SmsCommonUtils.convertStringToDateObject(req.getStartDate());
            LocalDate endDate = SmsCommonUtils.convertStringToDateObject(req.getEndDate());

            String recordShift = dto.getShift();

            // If record is before start date, exclude it
            if (recordDate.isBefore(startDate)) {
                return false;
            }

            // If record is after end date, exclude it
            if (recordDate.isAfter(endDate)) {
                return false;
            }

            // If record is on start date, check if shift is >= startShift
            if (recordDate.equals(startDate)) {
                return isShiftGreaterOrEqual(recordShift, req.getStartShift());
            }

            // If record is on end date, check if shift is <= endShift
            if (recordDate.equals(endDate)) {
                return isShiftLessOrEqual(recordShift, req.getEndShift());
            }

            // If record is between start and end dates, include it
            return true;

        } catch (Exception e) {
            // If there's any parsing error, include the record (fallback)
            return true;
        }
    }

    /**
     * Helper method to compare shifts (A < B < C)
     */
    private boolean isShiftGreaterOrEqual(String shift1, String shift2) {
        int order1 = getShiftOrder(shift1);
        int order2 = getShiftOrder(shift2);
        return order1 >= order2;
    }

    /**
     * Helper method to compare shifts (A < B < C)
     */
    private boolean isShiftLessOrEqual(String shift1, String shift2) {
        int order1 = getShiftOrder(shift1);
        int order2 = getShiftOrder(shift2);
        return order1 <= order2;
    }

    /**
     * Get numeric order for shift comparison (A=1, B=2, C=3)
     */
    private int getShiftOrder(String shift) {
        switch (shift.toUpperCase()) {
            case "A": return 1;
            case "B": return 2;
            case "C": return 3;
            default: return 0; // Unknown shift
        }
    }

    /**
     * Helper method to check if a heat report record falls within the shift-specific date range
     * @param dto The heat report data
     * @param req The request with startShift and endShift
     * @return true if the record is within the specified shift date range
     */
    private boolean isWithinHeatShiftDateRange(SmsHeatReportDto dto, ReportReqDto req) {
        try {
            // Parse dateAndShiftOfCasting which is in format "YYYY-MM-DD SHIFT" (e.g., "2025-04-01 A")
            String dateAndShift = dto.getDateAndShiftOfCasting();
            if (dateAndShift == null || dateAndShift.trim().isEmpty()) {
                return true; // Include if no date info
            }

            String[] parts = dateAndShift.trim().split("\\s+");
            if (parts.length < 2) {
                return true; // Include if format is unexpected
            }

            LocalDate recordDate = LocalDate.parse(parts[0]); // Parse date part
            String recordShift = parts[1]; // Extract shift part

            LocalDate startDate = SmsCommonUtils.convertStringToDateObject(req.getStartDate());
            LocalDate endDate = SmsCommonUtils.convertStringToDateObject(req.getEndDate());

            // If record is before start date, exclude it
            if (recordDate.isBefore(startDate)) {
                return false;
            }

            // If record is after end date, exclude it
            if (recordDate.isAfter(endDate)) {
                return false;
            }

            // If record is on start date, check if shift is >= startShift
            if (recordDate.equals(startDate)) {
                return isShiftGreaterOrEqual(recordShift, req.getStartShift());
            }

            // If record is on end date, check if shift is <= endShift
            if (recordDate.equals(endDate)) {
                return isShiftLessOrEqual(recordShift, req.getEndShift());
            }

            // If record is between start and end dates, include it
            return true;

        } catch (Exception e) {
            // If there's any parsing error, include the record (fallback)
            return true;
        }
    }

    private ReportResDto mapToReportResDto(Object[] row) {
    // Updated mapping to match the new query structure with concatenated caster numbers
    String formattedDate = (String) row[0];  // Already formatted as YYYY-MM-DD
    String sms = (String) row[1];
    String shift = (String) row[2];
    String casterNumbers = (String) row[3];  // Now contains concatenated caster numbers
    String railGrade = (String) row[4];
    Long noOfHeatCasted = ((Number) row[5]).longValue();
    Long noOfHeatRejected = ((Number) row[6]).longValue();
    Long noOfDivertedHeats = ((Number) row[7]).longValue();
    String rejectedHeatNumbers = (String) row[8];
    BigDecimal weightOfHeatsCast = (BigDecimal) row[9];
    BigDecimal weightOfPrimeBlooms = (BigDecimal) row[10];
    BigDecimal weightOfCOBlooms = (BigDecimal) row[11];
    BigDecimal weightOfAcceptedBlooms = (BigDecimal) row[12];
    BigDecimal weightOfRejectedBlooms = (BigDecimal) row[13];
    String reasonForRejection = (String) row[14];

    // Convert formatted date string back to Date object for DTO
    Date date;
    try {
        date = Date.valueOf(formattedDate);
    } catch (Exception e) {
        date = new Date(System.currentTimeMillis()); // fallback to current date
    }

    // Create formatted date and shift string for Excel export (same format as Heat Summary)
    String dateAndShift = formattedDate + " " + shift;

    ReportResDto dto = new ReportResDto(date, sms, shift, casterNumbers, railGrade, noOfHeatCasted, noOfHeatRejected,
            noOfDivertedHeats, rejectedHeatNumbers, weightOfHeatsCast, weightOfPrimeBlooms, weightOfCOBlooms,
            weightOfAcceptedBlooms, weightOfRejectedBlooms, reasonForRejection);

    // Set the formatted date and shift field
    dto.setDateAndShift(dateAndShift);

    return dto;
}

    @Override
    public HeatDtlEntity validateHeatNo(String heatNo) {
        // if heat did not complete bloom stage
        HeatDtlEntity hde = hdr.findByHeatNo(heatNo)
                .orElseThrow(() -> new SmsResourceNotFoundException(
                        new SmsErrorDetails(AppConstant.ERROR_CODE_RESOURCE,
                                AppConstant.ERROR_TYPE_CODE_RESOURCE,
                                AppConstant.ERROR_TYPE_RESOURCE,
                                "No heat detail found for the provided heat number. Add new heat.")));

        // if heat did not complete bloom stage
        if (hde.getIsDiverted()) {
            throw new SmsInvalidArgumentException(
                    new SmsErrorDetails(
                            AppConstant.ERROR_CODE_VALIDATION,
                            AppConstant.ERROR_TYPE_CODE_VALIDATION,
                            AppConstant.ERROR_TYPE_VALIDATION,
                            "Heat is marked as diverted."));
        }

        return hde;

    }

    @Override
    public void validateBloomHeat(String heatNo) {
        // if heat did not complete bloom stage
        bdr.findByCastNo(heatNo)
                .orElseThrow(() -> new SmsResourceNotFoundException(
                        new SmsErrorDetails(AppConstant.ERROR_CODE_RESOURCE,
                                AppConstant.ERROR_TYPE_CODE_RESOURCE,
                                AppConstant.ERROR_TYPE_RESOURCE,
                                "Provided heat did not complete bloom inspection yet.")));
    }

    @Override
    public void validateSmsHeat(String heatNo) {
        // if heat did not complete bloom stage
        hdr.findById(heatNo)
                .orElseThrow(() -> new SmsResourceNotFoundException(
                        new SmsErrorDetails(AppConstant.ERROR_CODE_RESOURCE,
                                AppConstant.ERROR_TYPE_CODE_RESOURCE,
                                AppConstant.ERROR_TYPE_RESOURCE,
                                "Provided heat not present in SMS.")));
    }

    @Override
    public List<SmsHeatReportDto> getHeatReport(ReportReqDto req) {
        // LocalDateTime startDate = CommonUtils.convertStringToDateObject(req.getStartDate()).atStartOfDay();
        // LocalDateTime endDate = CommonUtils.convertStringToDateObject(req.getEndDate()).atTime(LocalTime.MAX);

        // List<Object[]> objRes = hdr.getHeatReport(startDate, endDate);

        // return objRes.stream().map(result -> {
        //     SmsHeatReportDto dto = new SmsHeatReportDto();
        //     dto.setHeatNo((String) result[0]);
        //     dto.setSms((String) result[1]);
        //     dto.setCasterNo((String) result[2]);
        //     dto.setRailGrade((String) result[3]);
        //     dto.setDateAndShiftOfCasting((String) result[4]); // Assuming date format "yyyy-MM-dd HH:mm"

        //     dto.setSequenceNo((String) result[5]);
        //     dto.setTurnDownTemp((Integer) result[6]);
        //     dto.setDegassingVacuum((BigDecimal) result[7]);
        //     dto.setDegassingDuration((BigDecimal) result[8]);
        //     dto.setCastingTemp((Integer) result[9]);
        //     dto.setHydrogen((BigDecimal) result[10]);
        //     dto.setNitrogen((BigDecimal) result[11]);
        //     dto.setOxygen((BigDecimal) result[12]);
        //     dto.setChemical((String) result[13]);
        //     dto.setNoOfPrimeBlooms((Integer) result[14]);
        //     dto.setNoOfCoBlooms((Integer) result[15]);
        //     dto.setNoOfRejectedBlooms((Integer) result[16]);
        //     dto.setTotalCastWt((BigDecimal) result[17]);
        //     dto.setHeatRemark((String) result[18]);
        //     dto.setReasonForRejection((String) result[19]);
        //     dto.setProbeMakeName((String) result[20]);
        //     dto.setIsHydrisMeasuredBw80To100mOfCasting((Boolean) result[21]);
        //     dto.setIsProbeDippedBelow300mmFromSlagMetalInterface((Boolean) result[22]);
        //     dto.setMakeOfCastingPowder((String) result[23]);
        //     dto.setIsEmsFunctioning((Boolean) result[24]);
        //     dto.setIsSlagDetectorFunctioning((Boolean) result[25]);
        //     dto.setIsAmlcFunctioning((Boolean) result[26]);
        //     dto.setIsHydrogenMeasurementAutomatic((Boolean) result[27]);
        //     dto.setIsLadleToTundishUsed((Boolean) result[28]);
        //     dto.setIsTundishToMouldUsed((Boolean) result[29]);

        //     return dto;
        // }).collect(Collectors.toList());
        LocalDateTime startDate = SmsCommonUtils.convertStringToDateObject(req.getStartDate()).atStartOfDay();
        LocalDateTime endDate = SmsCommonUtils.convertStringToDateObject(req.getEndDate()).atTime(LocalTime.MAX);
    
        // Fetch raw query results
        List<Object[]> objRes = hdr.getHeatReport(startDate, endDate);

        // Map to DTO and apply shift-specific date range filtering if needed
        List<SmsHeatReportDto> allResults = objRes.stream().map(result -> {
            SmsHeatReportDto dto = new SmsHeatReportDto();
    
            dto.setHeatNo((String) result[0]);
            dto.setSmsNumber((String) result[1]);
            dto.setCasterNumber((String) result[2]);
            dto.setRailGrade((String) result[3]);
            dto.setDateAndShiftOfCasting((String) result[4]);
    
            dto.setSequenceNo((String) result[5]);
            dto.setTurnDownTemp(result[6] != null ? ((Number) result[6]).intValue() : null);
            dto.setDegassingVacuum(result[7] != null ? (BigDecimal) result[7] : null);
            dto.setDegassingDuration(result[8] != null ? (BigDecimal) result[8] : null);
            dto.setCastingTemp(result[9] != null ? ((Number) result[9]).intValue() : null);
            dto.setHydrogen(result[10] != null ? (BigDecimal) result[10] : null);
            dto.setNitrogen(result[11] != null ? (BigDecimal) result[11] : null);
            dto.setOxygen(result[12] != null ? (BigDecimal) result[12] : null);
            dto.setChemical((String) result[13]);
    
            dto.setNoOfPrimeBlooms(result[14] != null ? ((Number) result[14]).intValue() : null);
            dto.setNoOfCoBlooms(result[15] != null ? ((Number) result[15]).intValue() : null);
            dto.setNoOfRejectedBlooms(result[16] != null ? ((Number) result[16]).intValue() : null);
            dto.setTotalCastWt(result[17] != null ? (BigDecimal) result[17] : null);
            dto.setHeatRemark((String) result[18]);
            dto.setReasonForRejection((String) result[19]);
            dto.setProbeMakeName((String) result[20]);
    
            // Boolean fields: convert to Boolean safely
            dto.setIsHydrisMeasuredBw80To100mOfCasting("1".equals(String.valueOf(result[21]).trim()) || "true".equalsIgnoreCase(String.valueOf(result[21])));
            dto.setIsProbeDippedBelow300mmFromSlagMetalInterface("1".equals(String.valueOf(result[22]).trim()) || "true".equalsIgnoreCase(String.valueOf(result[22])));
            dto.setMakeOfCastingPowder((String) result[23]);
            dto.setIsEmsFunctioning("1".equals(String.valueOf(result[24]).trim()) || "true".equalsIgnoreCase(String.valueOf(result[24])));
            dto.setIsSlagDetectorFunctioning("1".equals(String.valueOf(result[25]).trim()) || "true".equalsIgnoreCase(String.valueOf(result[25])));
            dto.setIsAmlcFunctioning("1".equals(String.valueOf(result[26]).trim()) || "true".equalsIgnoreCase(String.valueOf(result[26])));
            dto.setIsHydrogenMeasurementAutomatic("1".equals(String.valueOf(result[27]).trim()) || "true".equalsIgnoreCase(String.valueOf(result[27])));
            dto.setIsLadleToTundishUsed("1".equals(String.valueOf(result[28]).trim()) || "true".equalsIgnoreCase(String.valueOf(result[28])));
            dto.setIsTundishToMouldUsed("1".equals(String.valueOf(result[29]).trim()) || "true".equalsIgnoreCase(String.valueOf(result[29])));
    
            return dto;
        }).collect(Collectors.toList());

        // Apply shift-specific date range filtering if both startShift and endShift are provided
        if (req.getStartShift() != null && req.getEndShift() != null) {
            return allResults.stream()
                .filter(dto -> isWithinHeatShiftDateRange(dto, req))
                .collect(Collectors.toList());
        }

        return allResults;
    }

    @Transactional
    @Override
    public void deleteHeatDtl(DeleteHeatReqDto req) {

        if(req.getSms().equalsIgnoreCase(AppConstant.SMS_2)){
            hds2r.deleteById(req.getHeatNo());
        }
        else if(req.getSms().equalsIgnoreCase(AppConstant.SMS_3)){
            hds3r.deleteById(req.getHeatNo());
        }
    }

    @Override
    public StageDtlResDto getStageDtl(StageDtlReqDto req) {

        StageDtlResDto res = new StageDtlResDto();
        SmsDutyEntity sde = sdr.findByDutyId(req.getDutyId())
                .orElseThrow(() -> new SmsResourceNotFoundException(
                        new SmsErrorDetails(AppConstant.ERROR_CODE_RESOURCE,
                                AppConstant.ERROR_TYPE_CODE_RESOURCE,
                                AppConstant.ERROR_TYPE_RESOURCE,
                                "No duties found for the provided duty id.")));

        if (sde.getSms().equalsIgnoreCase(AppConstant.SMS_2)) {
            Optional<DutyHeatSms2Entity> dhs2eOpt = dhs2r
                    .findById(new DutyHeatRelationIdSms2(req.getDutyId(), req.getHeatNo()));

            if (dhs2eOpt.isPresent()) {
                res.setHeatProcurementStage(dhs2eOpt.get().getHeatProcurementStage());
                res.setHeatProcurementStageCode(
                        SmsHeatStageEnum.getCodeFromDesc(dhs2eOpt.get().getHeatProcurementStage()));
                res.setHeatSurrenderStage(dhs2eOpt.get().getHeatSurrenderStage());
                res.setHeatSurrenderStageCode(SmsHeatStageEnum.getCodeFromDesc(dhs2eOpt.get().getHeatSurrenderStage()));
            }

        }

        else if (sde.getSms().equalsIgnoreCase(AppConstant.SMS_3)) {
            Optional<DutyHeatSms3Entity> dhs3eOpt = dhs3r
                    .findById(new DutyHeatRelationIdSms3(req.getDutyId(), req.getHeatNo()));

            if (dhs3eOpt.isPresent()) {
                res.setHeatProcurementStage(dhs3eOpt.get().getHeatProcurementStage());
                res.setHeatProcurementStageCode(
                        SmsHeatStageEnum.getCodeFromDesc(dhs3eOpt.get().getHeatProcurementStage()));
                res.setHeatSurrenderStage(dhs3eOpt.get().getHeatSurrenderStage());
                res.setHeatSurrenderStageCode(SmsHeatStageEnum.getCodeFromDesc(dhs3eOpt.get().getHeatSurrenderStage()));
            }

        }

        return res;

        // Optional<DutyHeatRelationEntity> dhreOpt = dhrr
        // .findByDutyHeatRelationId(new DutyHeatRelationId(req.getDutyId(),
        // req.getHeatNo()) );

        // StageDtlResDto res = new StageDtlResDto();

        // if (dhreOpt.isPresent()) {
        // res.setHeatProcurementStage(dhreOpt.get().getHeatProcurementStage());
        // res.setHeatProcurementStageCode(SmsHeatStageEnum.getCodeFromDesc(dhreOpt.get().getHeatProcurementStage()));
        // res.setHeatSurrenderStage(dhreOpt.get().getHeatSurrenderStage());
        // res.setHeatSurrenderStageCode(SmsHeatStageEnum.getCodeFromDesc(dhreOpt.get().getHeatSurrenderStage()));
        // }

        // return res;
    }

    @Override
    public HeatDtlSms2Entity validateSms2Heat(String heatNo) {
        HeatDtlSms2Entity hds2e = hds2r.findByHeatNo(heatNo)
                .orElseThrow(() -> new SmsResourceNotFoundException(
                        new SmsErrorDetails(AppConstant.ERROR_CODE_RESOURCE,
                                AppConstant.ERROR_TYPE_CODE_RESOURCE,
                                AppConstant.ERROR_TYPE_RESOURCE,
                                "Provided heat number not present in SMS 2")));

        if (hds2e.getIsDiverted()) {
            throw new SmsInvalidArgumentException(
                    new SmsErrorDetails(
                            AppConstant.ERROR_CODE_VALIDATION,
                            AppConstant.ERROR_TYPE_CODE_VALIDATION,
                            AppConstant.ERROR_TYPE_VALIDATION,
                            "Heat is marked as diverted."));
        }

        return hds2e;
    }

    @Override
    public HeatDtlSms3Entity validateSms3Heat(String heatNo) {
        HeatDtlSms3Entity hds3e = hds3r.findByHeatNo(heatNo)
                .orElseThrow(() -> new SmsResourceNotFoundException(
                        new SmsErrorDetails(AppConstant.ERROR_CODE_RESOURCE,
                                AppConstant.ERROR_TYPE_CODE_RESOURCE,
                                AppConstant.ERROR_TYPE_RESOURCE,
                                "Provided heat number not present in SMS 3")));

        if (hds3e.getIsDiverted()) {
            throw new SmsInvalidArgumentException(
                    new SmsErrorDetails(
                            AppConstant.ERROR_CODE_VALIDATION,
                            AppConstant.ERROR_TYPE_CODE_VALIDATION,
                            AppConstant.ERROR_TYPE_VALIDATION,
                            "Heat is marked as diverted."));
        }

        return hds3e;
    }

    @Override
    public void validateSms2Sms3Heat(String heatNo) {

        Optional<HeatDtlSms3Entity> hds3eOpt = hds3r.findByHeatNo(heatNo);
        Optional<HeatDtlSms2Entity> hds2eOpt = hds2r.findByHeatNo(heatNo);

        if(hds3eOpt.isPresent() || hds2eOpt.isPresent()){

        }
        else {
            throw new SmsResourceNotFoundException(
                    new SmsErrorDetails(AppConstant.ERROR_CODE_RESOURCE,
                            AppConstant.ERROR_TYPE_CODE_RESOURCE,
                            AppConstant.ERROR_TYPE_RESOURCE,
                            "Provided heat number " + heatNo + " not declared in SMS"));
        }

    }

    @Override
    public void validateHeatBloomStage(String heatNo) {
        System.out.println("DEBUG: Validating heat number: " + heatNo + " has completed Bloom Cutting stage in SMS2 and SMS3");

        boolean isBloomStageCompleted = false;
        String foundInSystem = "";

        // Check SMS2
        try {
            Optional<HeatDtlSms2Entity> heatSms2Opt = hds2r.findByHeatNo(heatNo);
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
                Optional<HeatDtlSms3Entity> heatSms3Opt = hds3r.findByHeatNo(heatNo);
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
                AppConstant.ERROR_CODE_VALIDATION,
                AppConstant.ERROR_TYPE_CODE_VALIDATION,
                AppConstant.ERROR_TYPE_VALIDATION,
                "Heat number " + heatNo + " has not reached 'Bloom Cutting' stage in SMS. Please complete bloom stage before proceeding."
            )
        );
    }

    /**
     * Validates that a heat number is not marked as diverted
     * Prevents editing/progression of diverted heats
     */
    private void validateHeatNotDiverted(String heatNo, String smsSystem) {
        System.out.println("DEBUG: Validating heat " + heatNo + " is not diverted in " + smsSystem);

        try {
            if (AppConstant.SMS_2.equalsIgnoreCase(smsSystem)) {
                Optional<HeatDtlSms2Entity> heatOpt = hds2r.findByHeatNo(heatNo);
                if (heatOpt.isPresent() && Boolean.TRUE.equals(heatOpt.get().getIsDiverted())) {
                    System.out.println("DEBUG: Heat " + heatNo + " is DIVERTED in SMS2 - blocking edit operation");
                    throw new SmsInvalidArgumentException(
                        new SmsErrorDetails(
                            AppConstant.ERROR_CODE_VALIDATION,
                            AppConstant.ERROR_TYPE_CODE_VALIDATION,
                            AppConstant.ERROR_TYPE_VALIDATION,
                            "Heat number " + heatNo + " is marked as diverted. Diverted heats cannot be edited or progressed to next stages."
                        )
                    );
                }
            } else if (AppConstant.SMS_3.equalsIgnoreCase(smsSystem)) {
                Optional<HeatDtlSms3Entity> heatOpt = hds3r.findByHeatNo(heatNo);
                if (heatOpt.isPresent() && Boolean.TRUE.equals(heatOpt.get().getIsDiverted())) {
                    System.out.println("DEBUG: Heat " + heatNo + " is DIVERTED in SMS3 - blocking edit operation");
                    throw new SmsInvalidArgumentException(
                        new SmsErrorDetails(
                            AppConstant.ERROR_CODE_VALIDATION,
                            AppConstant.ERROR_TYPE_CODE_VALIDATION,
                            AppConstant.ERROR_TYPE_VALIDATION,
                            "Heat number " + heatNo + " is marked as diverted. Diverted heats cannot be edited or progressed to next stages."
                        )
                    );
                }
            }
            System.out.println("DEBUG: Heat " + heatNo + " is not diverted - allowing edit operation");
        } catch (SmsInvalidArgumentException e) {
            throw e; // Re-throw validation exceptions
        } catch (Exception e) {
            System.out.println("DEBUG: Error checking diverted status for heat " + heatNo + ": " + e.getMessage());
            // Don't block operations due to technical errors
        }
    }

}
