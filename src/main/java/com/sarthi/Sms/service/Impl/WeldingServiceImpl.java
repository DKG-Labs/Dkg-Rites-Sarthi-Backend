package com.sarthi.Sms.service.Impl;

import com.sarthi.Sms.constant.RailMillAppConstant;
import com.sarthi.Sms.dto.sms.common.DutyStatusResDto;
import com.sarthi.Sms.dto.sms.common.EndDutyReqDto;
import com.sarthi.Sms.dto.sms.common.ReportReqDto;
import com.sarthi.Sms.dto.sms.common.StartDutyReqDto;
import com.sarthi.Sms.dto.sms.common.StartDutyResDto;
import com.sarthi.Sms.dto.welding.*;
import com.sarthi.Sms.entity.rolling.RollingDutyEntity;
import com.sarthi.Sms.entity.welding.*;
import com.sarthi.Sms.exception.SmsErrorDetails;
import com.sarthi.Sms.exception.SmsInvalidArgumentException;
import com.sarthi.Sms.exception.SmsResourceNotFoundException;
import com.sarthi.Sms.repository.welding.*;
import com.sarthi.Sms.service.DutySequenceService;
import com.sarthi.Sms.service.WeldingService;
import com.sarthi.Sms.util.RailMillCommonUtils;
import com.sarthi.Sms.util.DutyEnum;
import org.hibernate.annotations.Fetch;
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
public class WeldingServiceImpl implements WeldingService {

    @Autowired
    private WeldingInspectionDutyRepository wdtyr;

    @Autowired
    private DutySequenceService dss;

    @Autowired
    private WeldingMasterRepository wmr;

    @Autowired
    private WeldingDetailRepository wdr;

    @Autowired
    private WeldingDutyRelationRepository wrr;

    @Autowired
    private WeldingHardnessRepository whr;

    @Autowired
    private WeldingTestSampleRepository wtsr;

    @Autowired
    private WeldingTltRepository wtr;

    @Autowired
    private WeldingMicroRepository wmir;

    @Autowired
    private WeldingMacroRepository wmar;

    @Override
    @Transactional
    public StartDutyResDto startDuty(String authHeader, StartDutyReqDto request) {
        String userId = RailMillCommonUtils.getUserIdFromAuthHeader(authHeader);

        WeldingInspectionDutyEntity entity = new WeldingInspectionDutyEntity();

        DutyStatusResDto dutyStatus = checkDutyStatus(authHeader);

        if (dutyStatus.getIsDutyInProgess()) {
            throw new SmsInvalidArgumentException(new SmsErrorDetails(
                    RailMillAppConstant.USER_ALREADY_EXISTS,
                    RailMillAppConstant.USER_ALREADY_EXISTS,
                    RailMillAppConstant.ERROR_TYPE_VALIDATION,
                    "Cannot start new duty. Previous duty is already in progress"));
        }

        LocalDate date = RailMillCommonUtils.convertStringToDateObject(request.getStartDate());
        String dutyId = dss.generateDutyId(DutyEnum.WLD, date);
        entity.setDutyId(dutyId);
        entity.setUserId(userId);
        entity.setDate(date);
        entity.setStartTime(LocalDateTime.now());
        entity.setShift(request.getShift());
        entity.setWeldingLine(request.getWeldingLine());
        entity.setRailGrade(request.getRailGrade());
        entity.setRailSection(request.getRailSection());
        entity.setMill(request.getMill());

        wdtyr.save(entity);

        StartDutyResDto startDutyResDto = new StartDutyResDto();
        startDutyResDto.setDutyId(entity.getDutyId());
        startDutyResDto.setStartTime(RailMillCommonUtils.extractTime(entity.getStartTime()));
        startDutyResDto.setRailGrade(entity.getRailGrade());
        startDutyResDto.setRailSection(entity.getRailSection());
        startDutyResDto.setWeldingLine(entity.getWeldingLine());
        startDutyResDto.setMill(entity.getMill());
        startDutyResDto.setShift(entity.getShift());
        startDutyResDto.setDate(RailMillCommonUtils.convertDateToString(entity.getDate()));

        return startDutyResDto;
    }

    @Override
    @Transactional
    public void endDuty(EndDutyReqDto req) {
        WeldingInspectionDutyEntity weldingDutyEntity = wdtyr.findById(req.getDutyId())
                .orElseThrow(() -> new SmsResourceNotFoundException(
                        new SmsErrorDetails(RailMillAppConstant.ERROR_CODE_RESOURCE,
                                RailMillAppConstant.ERROR_TYPE_CODE_RESOURCE,
                                RailMillAppConstant.ERROR_TYPE_RESOURCE,
                                "No duties found for the provided duty ID.")));

        // Check if duty has already ended
        if (Objects.nonNull(weldingDutyEntity.getEndTime())) {
            throw new SmsInvalidArgumentException(new SmsErrorDetails(
                    RailMillAppConstant.ERROR_CODE_VALIDATION,
                    RailMillAppConstant.ERROR_TYPE_CODE_VALIDATION,
                    RailMillAppConstant.ERROR_TYPE_VALIDATION,
                    "Duty has already ended."));
        }

        // Set end time and remarks
        weldingDutyEntity.setEndTime(LocalDateTime.now());
        weldingDutyEntity.setShiftRemarks(req.getShiftRemarks());
        weldingDutyEntity.setMachineSNoHead(req.getMachineSNoHead());
        weldingDutyEntity.setMachineSNoFoot(req.getMachineSNoFoot());
        weldingDutyEntity.setProbeDetailsHead(req.getProbeDetailsHead());
        weldingDutyEntity.setProbeDetailsFoot(req.getProbeDetailsFoot());
        weldingDutyEntity.setProbeDbHead(req.getProbeDbHead());
        weldingDutyEntity.setProbeDbFoot(req.getProbeDbFoot());
        weldingDutyEntity.setRemarksHead(req.getRemarksHead());
        weldingDutyEntity.setRemarksFoot(req.getRemarksFoot());
        weldingDutyEntity.setJointsWeldedPrevShiftBsp1(req.getJointsWeldedPrevShiftBsp1());
        weldingDutyEntity.setJointsWeldedPrevShiftBsp2(req.getJointsWeldedPrevShiftBsp2());
        weldingDutyEntity.setJointsWeldedPrevShiftBsp4(req.getJointsWeldedPrevShiftBsp4());

        // Save the updated duty entity
        wdtyr.save(weldingDutyEntity);
    }

    @Override
    public DutyStatusResDto checkDutyStatus(String authorizationHeader) {
        String userId = RailMillCommonUtils.getUserIdFromAuthHeader(authorizationHeader);
        String dutyId = wdtyr.checkDutyStatus(userId);

        DutyStatusResDto dutyProgressResDto = new DutyStatusResDto();
        dutyProgressResDto.setDutyId(dutyId);
        dutyProgressResDto.setIsDutyInProgess(dutyId != null);

        return dutyProgressResDto;
    }

    @Override
    public StartDutyResDto getOngoingDutyDtls(String ah) {
        String userId = RailMillCommonUtils.getUserIdFromAuthHeader(ah);
        Optional<WeldingInspectionDutyEntity> rdeOpt = wdtyr.getOngoingDutyDtls(userId);

        StartDutyResDto sdrd = new StartDutyResDto();
        if (rdeOpt.isPresent()) {
            WeldingInspectionDutyEntity rde = rdeOpt.get();
            sdrd.setDate(RailMillCommonUtils.convertDateToString(rde.getDate()));
            sdrd.setShift(rde.getShift());
            sdrd.setMill(rde.getMill());
            sdrd.setRailGrade(rde.getRailGrade());
            sdrd.setRailSection(rde.getRailSection());
            sdrd.setDutyId(rde.getDutyId());
            sdrd.setStartTime(RailMillCommonUtils.extractTime(rde.getStartTime()));
        }

        return sdrd;
    }

    @Override
    @Transactional
    public void saveWeldingInspection(String authorizationHeader, WeldingInspectionDto requestDto) {

        // Save welding_inspection_master
        WeldingMasterEntity master = new WeldingMasterEntity();

        master.setDutyId(requestDto.getDutyId());
        master.setPanelId(requestDto.getPanelId());
        master.setNoOfJoints(requestDto.getNoOfJoints());
        master.setPanelLength(requestDto.getPanelLength());
        master.setPanelDecision(requestDto.getPanelDecision());
        master.setPanelRemark(requestDto.getPanelRemark());
        master.setFrontResult(requestDto.getFrontResult());
        master.setBackResult(requestDto.getBackResult());
        master.setFrontResultReasonForNotOk(requestDto.getFrontResultReasonForNotOk());
        master.setBackResultReasonForNotOk(requestDto.getBackResultReasonForNotOk());
        master.setFrontRemark(requestDto.getFrontRemark());
        master.setBackRemark(requestDto.getBackRemark());

        master = wmr.save(master);

        // Save welding_duty_inspection_relation
        WeldingDutyRelationEntity relation = new WeldingDutyRelationEntity();

        // Create and set the embedded primary key
        WeldingDutyInspectionRelationKey relationKey = new WeldingDutyInspectionRelationKey();
        relationKey.setDutyId(requestDto.getDutyId());
        relationKey.setInspectionMasterId(master.getInspectionMasterId());

        // Set the embedded key in the relation entity
        relation.setId(relationKey);
        relation.setPanelDecision(requestDto.getPanelDecision());

        // Save the relation entity
        wrr.save(relation);

        // Save welding_inspection_detail records based on number_of_joints
        // for (int i = 0; i < requestDto.getNoOfJoints(); i++) {
        for (WeldingDetailDto detailDto : requestDto.getWeldList()) {
            WeldingDetailEntity detail = new WeldingDetailEntity();
            detail.setJointNo(detailDto.getJointNo());
            detail.setInspectionMasterId(master.getInspectionMasterId());
            detail.setWeldParameter(detailDto.getWeldParameter());
            detail.setVisual(detailDto.getVisual());
            detail.setMarking(detailDto.getMarking());
            detail.setDimension(detailDto.getDimension());
            detail.setDimensionDetail(detailDto.getDimensionDetail());
            detail.setUsfd(detailDto.getUsfd());
            detail.setRailId1Length(detailDto.getRailId1Length());
            detail.setRailId2Length(detailDto.getRailId2Length());
            detail.setUsfdDetail(detailDto.getUsfdDetail());
            detail.setWeldOperator(detailDto.getWeldOperator());
            detail.setUsfdOperator(detailDto.getUsfdOperator());
            detail.setResult(detailDto.getResult());
            detail.setRailId1(detailDto.getRailId1());
            detail.setRailId2(detailDto.getRailId2());
            detail.setRemark(detailDto.getRemark());

            wdr.save(detail);
        }
    }

    

    @Override
    @Transactional(readOnly = true)
    public List<WeldingInspectionResponseDto> getDetailsByPanelDecision() {
        return wdr.findInspectionDetailsByPanelDecision();
    }

    @Override
    @Transactional
    public void updateWeldingInspection(String authorizationHeader, WeldingInspectionDto req) {
        // Extract user ID from authorization header
        // String userId = RailMillCommonUtils.getUserIdFromAuthHeader(authorizationHeader);

        // Fetch the existing WeldingMasterInspectionEntity by panelId
        // Optional<WeldingMasterInspectionEntity> existingMasterOpt =
        // wmr.findByPanelId(requestDto.getPanelId());
        // if (!existingMasterOpt.isPresent()) {
        // throw new SmsInvalidArgumentException(new SmsErrorDetails(
        // RailMillAppConstant.ERROR_CODE_VALIDATION,
        // RailMillAppConstant.ERROR_TYPE_CODE_VALIDATION,
        // RailMillAppConstant.ERROR_TYPE_VALIDATION,
        // "Panel ID not found in the database."));
        // }

        // // Validate the duty ID (assuming there's a WeldingInspectionDutyRepository)
        // WeldingInspectionDutyEntity dutyEntity =
        // wdtyr.findById(requestDto.getDutyId())
        // .orElseThrow(() -> new SmsInvalidArgumentException(new SmsErrorDetails(
        // RailMillAppConstant.ERROR_CODE_VALIDATION,
        // RailMillAppConstant.ERROR_TYPE_CODE_VALIDATION,
        // RailMillAppConstant.ERROR_TYPE_VALIDATION,
        // "Invalid Duty ID provided.")));

        // if (!dutyEntity.getUserId().equals(userId)) {
        // throw new SmsInvalidArgumentException(new SmsErrorDetails(
        // RailMillAppConstant.ERROR_CODE_VALIDATION,
        // RailMillAppConstant.ERROR_TYPE_CODE_VALIDATION,
        // RailMillAppConstant.ERROR_TYPE_VALIDATION,
        // "You are not authorized to update welding inspection for this duty ID."
        // ));
        // }

        // Update the WeldingMasterInspectionEntity fields with new details from the
        // request DTO

        WeldingMasterEntity wme = wmr.findByInspectionMasterId(req.getId())
                .orElseThrow(() -> new SmsResourceNotFoundException(
                        new SmsErrorDetails(RailMillAppConstant.ERROR_CODE_RESOURCE,
                                RailMillAppConstant.ERROR_TYPE_CODE_RESOURCE,
                                RailMillAppConstant.ERROR_TYPE_RESOURCE,
                                "No detail found for the provided id.")));
        wme.setNoOfJoints(req.getNoOfJoints());
        wme.setPanelLength(req.getPanelLength());
        wme.setPanelDecision(req.getPanelDecision());
        wme.setPanelRemark(req.getPanelRemark());
        wme.setFrontResult(req.getFrontResult());
        wme.setBackResult(req.getBackResult());
        wme.setFrontResultReasonForNotOk(req.getFrontResultReasonForNotOk());
        wme.setBackResultReasonForNotOk(req.getBackResultReasonForNotOk());
        wme.setFrontRemark(req.getFrontRemark());
        wme.setBackRemark(req.getBackRemark());

        // Save updated WeldingMasterInspectionEntity
        wme = wmr.save(wme);

        // Update the welding_duty_inspection_relation entity
        Optional<WeldingDutyRelationEntity> existingRelationOpt = wrr
                .findById_DutyIdAndId_InspectionMasterId(req.getDutyId(), wme.getInspectionMasterId());
        if (existingRelationOpt.isPresent()) {
            WeldingDutyRelationEntity wdire = existingRelationOpt.get();
            wdire.setPanelDecision(req.getPanelDecision());
            wrr.save(wdire);
        } else {
            // If no existing relation, create a new one
            WeldingDutyRelationEntity newRelation = new WeldingDutyRelationEntity();

            // Create and set the embedded primary key
            WeldingDutyInspectionRelationKey relationKey = new WeldingDutyInspectionRelationKey();
            relationKey.setDutyId(req.getDutyId());
            relationKey.setInspectionMasterId(req.getId());

            // Set the embedded key and other fields in the relation entity
            newRelation.setId(relationKey);
            newRelation.setPanelDecision(req.getPanelDecision());

            // Save the relation entity
            wrr.save(newRelation);

        }

        // Update or insert welding_inspection_detail records based on number_of_joints
        for (int i = 0; i < req.getNoOfJoints(); i++) {
            WeldingDetailDto detailDto = req.getWeldList().get(i);

            // Check if the detail already exists, update or insert accordingly
            Optional<WeldingDetailEntity> existingDetail = wdr
                    .findByJointNo(detailDto.getJointNo());
            WeldingDetailEntity detailEntity = existingDetail.isPresent() ? existingDetail.get()
                    : new WeldingDetailEntity();

            detailEntity.setJointNo(detailDto.getJointNo());
            detailEntity.setInspectionMasterId(wme.getInspectionMasterId());
            detailEntity.setWeldParameter(detailDto.getWeldParameter());
            detailEntity.setMarking(detailDto.getMarking());
            detailEntity.setDimension(detailDto.getDimension());
            detailEntity.setDimensionDetail(detailDto.getDimensionDetail());
            detailEntity.setUsfd(detailDto.getUsfd());
            detailEntity.setUsfdDetail(detailDto.getUsfdDetail());
            detailEntity.setWeldOperator(detailDto.getWeldOperator());
            detailEntity.setUsfdOperator(detailDto.getUsfdOperator());
            detailEntity.setResult(detailDto.getResult());
            detailEntity.setRailId1(detailDto.getRailId1());
            detailEntity.setRailId1Length(detailDto.getRailId1Length());
            detailEntity.setRailId2(detailDto.getRailId2());
            detailEntity.setRailId2Length(detailDto.getRailId2Length());
            detailEntity.setRemark(detailDto.getRemark());

            wdr.save(detailEntity);
        }
    }

    @Override
    @Transactional
    public void saveWeldingHardness(String authHeader, WeldingHardnessRequestDto request) {
        String userId = RailMillCommonUtils.getUserIdFromAuthHeader(authHeader);

        WeldingInspectionDutyEntity dutyEntity = wdtyr.findById(request.getDutyId())
                .orElseThrow(() -> new SmsInvalidArgumentException(new SmsErrorDetails(
                        RailMillAppConstant.ERROR_CODE_VALIDATION,
                        RailMillAppConstant.ERROR_TYPE_CODE_VALIDATION,
                        RailMillAppConstant.ERROR_TYPE_VALIDATION,
                        "Invalid Duty ID provided.")));

        if (!dutyEntity.getUserId().equals(userId)) {
            throw new SmsInvalidArgumentException(new SmsErrorDetails(
                    RailMillAppConstant.ERROR_CODE_VALIDATION,
                    RailMillAppConstant.ERROR_TYPE_CODE_VALIDATION,
                    RailMillAppConstant.ERROR_TYPE_VALIDATION,
                    "You are not authorized to add hardness data for this duty ID."));
        }

        WeldingHardnessEntity hardnessEntity = new WeldingHardnessEntity();
        WeldingHardnessKey hardnessId = new WeldingHardnessKey();
        hardnessId.setTestType(request.getTestType());
        hardnessId.setJointNo(request.getJointNo());
        hardnessEntity.setId(hardnessId);

        hardnessEntity.setMachineNo(request.getMachineNo());
        hardnessEntity.setParentHardnessL(request.getParentHardnessL());
        hardnessEntity.setParentHardnessR(request.getParentHardnessR());
        hardnessEntity.setHazHardnessL(request.getHazHardnessL());
        hardnessEntity.setHazHardnessR(request.getHazHardnessR());
        hardnessEntity.setDeviationL(request.getDeviationL());
        hardnessEntity.setDeviationR(request.getDeviationR());
        hardnessEntity.setRemark(request.getRemark());
        hardnessEntity.setDutyId(request.getDutyId());
        hardnessEntity.setCreatedAt(LocalDateTime.now());
        whr.save(hardnessEntity);
    }

    @Override
    @Transactional
    public void saveWeldingTlt(String authHeader, WeldingTltRequestDto request) {
        String userId = RailMillCommonUtils.getUserIdFromAuthHeader(authHeader);

        WeldingInspectionDutyEntity dutyEntity = wdtyr.findById(request.getDutyId())
                .orElseThrow(() -> new SmsInvalidArgumentException(new SmsErrorDetails(
                        RailMillAppConstant.ERROR_CODE_VALIDATION,
                        RailMillAppConstant.ERROR_TYPE_CODE_VALIDATION,
                        RailMillAppConstant.ERROR_TYPE_VALIDATION,
                        "Invalid Duty ID provided.")));

        if (!dutyEntity.getUserId().equals(userId)) {
            throw new SmsInvalidArgumentException(new SmsErrorDetails(
                    RailMillAppConstant.ERROR_CODE_VALIDATION,
                    RailMillAppConstant.ERROR_TYPE_CODE_VALIDATION,
                    RailMillAppConstant.ERROR_TYPE_VALIDATION,
                    "You are not authorized to add TLT data for this duty ID."));
        }

        WeldingTltEntity tltEntity = new WeldingTltEntity();
        WeldingTltKey tltId = new WeldingTltKey();
        tltId.setTestType(request.getTestType());
        tltId.setJointNumber(request.getJointNo());
        tltEntity.setId(tltId);
        tltEntity.setDeflection(request.getDeflection());
        tltEntity.setMachineNo(request.getMachineNo());
        tltEntity.setSpan(request.getSpan());
        tltEntity.setLoadApplied(request.getLoadApplied());
        tltEntity.setLoadAt20MmDeflection(request.getLoadAt20MmDeflection());
        tltEntity.setIsBroken(request.getIsBroken());
        tltEntity.setRemark(request.getRemark());
        tltEntity.setDutyId(request.getDutyId());
        tltEntity.setCreatedAt(LocalDateTime.now());

        wtr.save(tltEntity);
    }

    @Override
    @Transactional
    public void saveWeldingTestSample(String authHeader, WeldingTestSampleRequestDto request) {
        // String userId = RailMillCommonUtils.getUserIdFromAuthHeader(authHeader);

        // WeldingInspectionDutyEntity dutyEntity = wdtyr.findById(request.getDutyId())
        // .orElseThrow(() -> new SmsInvalidArgumentException(new SmsErrorDetails(
        // RailMillAppConstant.ERROR_CODE_VALIDATION,
        // RailMillAppConstant.ERROR_TYPE_CODE_VALIDATION,
        // RailMillAppConstant.ERROR_TYPE_VALIDATION,
        // "Invalid Duty ID provided.")));

        // if (!dutyEntity.getUserId().equals(userId)) {
        // throw new SmsInvalidArgumentException(new SmsErrorDetails(
        // RailMillAppConstant.ERROR_CODE_VALIDATION,
        // RailMillAppConstant.ERROR_TYPE_CODE_VALIDATION,
        // RailMillAppConstant.ERROR_TYPE_VALIDATION,
        // "You are not authorized to add test samples for this duty ID."));
        // }

        WeldingTestSampleEntity sampleEntity = new WeldingTestSampleEntity();
        WeldingTestSampleKey sampleId = new WeldingTestSampleKey();
        sampleId.setTestType(request.getTestType());
        sampleId.setJointNo(request.getJointNo());
        sampleEntity.setId(sampleId);

        sampleEntity.setRailGrade(request.getRailGrade());
        sampleEntity.setRailSection(request.getRailSection());
        sampleEntity.setPlant(request.getPlant());
        sampleEntity.setStatus(request.getStatus());
        sampleEntity.setDutyId(request.getDutyId());

        wtsr.save(sampleEntity);
    }

    @Override
    @Transactional
    public void deleteWeldInspection(Integer id){
        wmr.deleteById(id);
    }

    @Override
    public WeldingInspectionDto getWeldingDtl(Integer id) {
        WeldingMasterEntity wme = wmr.findById(id)
                .orElseThrow(() -> new SmsResourceNotFoundException(
                        new SmsErrorDetails(RailMillAppConstant.ERROR_CODE_RESOURCE,
                                RailMillAppConstant.ERROR_TYPE_CODE_RESOURCE,
                                RailMillAppConstant.ERROR_TYPE_RESOURCE,
                                "No detail found for the provided id.")));

        List<WeldingDetailEntity> wdeList = wdr.findByInspectionMasterId(id);

        WeldingInspectionDto wid = new WeldingInspectionDto();

        wid.setId(wme.getInspectionMasterId());
        wid.setBackRemark(wme.getBackRemark());
        wid.setBackResult(wme.getBackResult());
        wid.setBackResultReasonForNotOk(wme.getBackResultReasonForNotOk());
        wid.setDutyId(null);
        wid.setFrontRemark(wme.getFrontRemark());
        wid.setFrontResult(wme.getFrontResult());
        wid.setFrontResultReasonForNotOk(wme.getFrontResultReasonForNotOk());
        wid.setNoOfJoints(wme.getNoOfJoints());
        wid.setPanelDecision(wme.getPanelDecision());
        wid.setPanelId(wme.getPanelId());
        wid.setPanelLength(wme.getPanelLength());
        wid.setPanelRemark(wme.getPanelRemark());

        List<WeldingDetailDto> weldList = wdeList.stream().map(wde -> {
            WeldingDetailDto wdd = new WeldingDetailDto();

            wdd.setDimension(wde.getDimension());
            wdd.setDimensionDetail(wde.getDimensionDetail());
            wdd.setJointNo(wde.getJointNo());
            wdd.setMarking(wde.getMarking());
            wdd.setRailId1(wde.getRailId1());
            wdd.setRailId1Length(wde.getRailId1Length());
            wdd.setRailId2(wde.getRailId2());
            wdd.setRailId2Length(wde.getRailId2Length());
            wdd.setRemark(wde.getRemark());
            wdd.setResult(wde.getResult());
            wdd.setUsfd(wde.getUsfd());
            wdd.setUsfdDetail(wde.getUsfdDetail());
            wdd.setUsfdOperator(wde.getUsfdOperator());
            wdd.setVisual(wde.getVisual());
            wdd.setWeldOperator(wde.getWeldOperator());
            wdd.setWeldParameter(wde.getWeldParameter());

            return wdd;
        }).collect(Collectors.toList());

        wid.setWeldList(weldList);

        return wid;

    }

    @Override
    public List<WeldingTestResDto> getPendingTestSampleDtl() {
        List<Object[]> resObj = wtsr.findMissingRecords();
        return resObj.stream().map(obj -> {
            WeldingTestResDto res = new WeldingTestResDto();
            res.setTestType((String) obj[0]);
            res.setJointNo((String) obj[1]);
            res.setDate((String) obj[2]);
            res.setShift((String) obj[3]);
            res.setMachineNo((String) obj[4]);

            return res;
        }).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void saveMicro(WeldingMicroDto req) {
        WeldingMicroEntity wme = new WeldingMicroEntity();

        wme.setDutyId(req.getDutyId());
        wme.setIsMicroFree(req.getIsMicroFree());
        wme.setIsAstmCoarser(req.getIsAstmCoarser());
        wme.setRemarks(req.getRemarks());
        wme.setMachineNo(req.getMachineNo());
        wme.setId(new WeldingMicroId(req.getTestType(), req.getJointNo()));
        wme.setCreatedAt(LocalDateTime.now());
        wmir.save(wme);
    }

    @Override
    @Transactional
    public void saveMacro(WeldingMacroDto req) {
        WeldingMacroEntity wme = new WeldingMacroEntity();

        wme.setDutyId(req.getDutyId());
        wme.setHeadL(req.getHeadL());
        wme.setWebL(req.getWebL());
        wme.setFootL(req.getFootL());
        wme.setHeadR(req.getHeadR());
        wme.setWebR(req.getWebR());
        wme.setFootR(req.getFootR());
        wme.setRemarks(req.getRemarks());
        wme.setMachineNo(req.getMachineNo());
        wme.setId(new WeldingMacroId(req.getTestType(), req.getJointNo()));
        wme.setCreatedAt(LocalDateTime.now());
        wmar.save(wme);
    }

    @Override
    public List<RecentTestSummaryDto> getRecentTestSummary() {
        // List<Object[]> recentTestList = wtsr.getRecentAddedRecord();
        // List<Object[]> counterList = wtsr.getTestTypeJointCount();

        // Fetch the most recent records
        List<Object[]> recentRecords = wtsr.getRecentAddedRecord();

        // Fetch row counts for each joint_number
        List<Object[]> rowCounts = wtsr.getTestTypeJointCount();

        // Iterate through the list and print each row count entry
        rowCounts.forEach(row -> {
            String testType = (String) row[0]; // Assuming the first element is the joint_number
            Long rowCount = (Long) row[1]; // Assuming the second element is the row_count

            System.out.println("Test type: " + testType + ", Row count: " + rowCount);
        });

        // Combine results
        return recentRecords.stream().map(recentRecord -> {
            String jointNo = (String) recentRecord[0];
            // String date = (String) RailMillCommonUtils.convertDateToString((LocalDate)
            // recentRecord[1]);
            String date = RailMillCommonUtils.convertDateToString(((java.sql.Date) recentRecord[1]).toLocalDate());
            String shift = (String) recentRecord[2];
            String testType = (String) recentRecord[3];

            // Find the row count for this joint_no
            long rowCount = rowCounts.stream()
                    .filter(rowCountObj -> testType.equals(rowCountObj[0]))
                    .mapToLong(rowCountObj -> (long) rowCountObj[1])
                    .findFirst()
                    .orElse(0); // Default to 0 if no matching row count is found

            // Create the DTO and populate fields
            RecentTestSummaryDto dto = new RecentTestSummaryDto();
            dto.setJointNo(jointNo);
            dto.setDate(date);
            dto.setShift(shift);
            dto.setTestType(testType);
            dto.setCounter(rowCount);

            return dto;
        }).collect(Collectors.toList());
    }

    @Override
    public List<WeldingMachineWiseCountDto> getMachineWiseTestCount() {
        List<Object[]> machineWiseCount = wtsr.getMachineWiseTestCount();
        return machineWiseCount.stream()
                .map(obj -> new WeldingMachineWiseCountDto(
                        (String) obj[0], // machineNumber
                        ((Number) obj[1]).longValue(), // tltCount
                        ((Number) obj[2]).longValue(), // hardnessCount
                        ((Number) obj[3]).longValue(), // macroCount
                        ((Number) obj[4]).longValue() // microCount
                )).collect(Collectors.toList());
    }

    @Override
    public List<TestSampleReportDto> getTestSampleReport(ReportReqDto req) {
        LocalDateTime startDate = RailMillCommonUtils.convertStringToDateObject(req.getStartDate()).atStartOfDay();
        LocalDateTime endDate = RailMillCommonUtils.convertStringToDateObject(req.getEndDate()).atTime(LocalTime.MAX);

        List<Object[]> resObjList = wtsr.getTestSampleReport(startDate, endDate);

        return resObjList.stream().map(obj -> {
            return new TestSampleReportDto(
                    (String) obj[0],  // mill
                (String) obj[1],  // railSection
                (String) obj[2],  // railGrade
                (String) obj[3],  // inspectionWorkspot
                (String) obj[4],  // weldingMachineNumber
                (String) obj[5],  // tltJointNo
                (String) obj[6],  // tltMarkingDateShift
                (String) obj[7],  // tltTestingStatus
                (String) obj[8],  // tltLoadDeflection
                (String) obj[9],  // tltRemark
                (String) obj[10], // hardnessJointNo
                // (LocalDate) obj[11], // hardnessMarkingDate
                obj[11] != null ? RailMillCommonUtils.convertDateToString(((java.sql.Date) obj[11]).toLocalDate()) : null,  // hardnessMarkingDate
                (String) obj[12], // hardnessTestingStatus
                (String) obj[13], // hardnessDeviation
                (String) obj[14], // hardnessRemark
                (String) obj[15], // macroMicroJointNo
                obj[16] != null ? RailMillCommonUtils.convertDateToString(((java.sql.Date) obj[16]).toLocalDate()) : null,  // macroMicroMarkingDate
                (String) obj[17], // macroMicroTestingStatus
                (String) obj[18], // macroMicroHazFullRailFoot
                (String) obj[19], // macroMicroHazOneSideFoot
                (String) obj[20], // macroMicroRemark
                (BigDecimal) obj[21], // railLength1
                (BigDecimal) obj[22]  // railLength2
            );
        }).collect(Collectors.toList());
    }

    @Override
    public List<NewWeldReportDto> getNewWeldingReport(ReportReqDto req){
        LocalDateTime startDate = RailMillCommonUtils.convertStringToDateObject(req.getStartDate()).atStartOfDay();
        LocalDateTime endDate = RailMillCommonUtils.convertStringToDateObject(req.getEndDate()).atTime(LocalTime.MAX);

        List<Object[]> resObjList = wmr.getNewWeldingReport(startDate, endDate);

        return resObjList.stream().map(obj -> new NewWeldReportDto(
                obj[0] != null ? RailMillCommonUtils.convertDateToString(((java.sql.Date) obj[0]).toLocalDate()) : null,  // macroMicroMarkingDate
                // (Long) obj[0],  // dateAndShift
                (String) obj[1],  // inspectionWorkspot
                (String) obj[2], // inspectionWorkspot
                (String) obj[3],  // railSection
                (String) obj[4],  // railGrade
                (String) obj[5],  // panelId
                (String) obj[6],  // jointNumber
                (String) obj[7],  // weldJointResult
                (String) obj[8],  // weldJointReason
                (String) obj[9],  // weldJointRemark
                (String) obj[10],  // railId1
                (String) obj[11], // railId2
                (Integer) obj[12], // panelLength
                (String) obj[13], // panelDecision
                (String) obj[14], // panelRemark
                (BigDecimal) obj[15], // weldingRailLength1
                (BigDecimal) obj[16]  // weldingRailLength2
        )).collect(Collectors.toList());

    }

    @Override
    public List<WeldingSummaryReportDto> getWeldingSummary(ReportReqDto req){
        LocalDateTime startDate = RailMillCommonUtils.convertStringToDateObject(req.getStartDate()).atStartOfDay();
        LocalDateTime endDate = RailMillCommonUtils.convertStringToDateObject(req.getEndDate()).atTime(LocalTime.MAX);

        List<Object[]> resObjList = wmr.getWeldingSummaryReport(startDate, endDate);

        return resObjList.stream().map(obj -> new WeldingSummaryReportDto(
                // obj[0] != null ? obj[0].toString() : null,  // date
                obj[0] != null ? RailMillCommonUtils.convertDateToString(((java.sql.Date) obj[0]).toLocalDate()) : null,  // date
                (String) obj[1],  // railSection
                (String) obj[2],  // railGrade
                (String) obj[3],  // inspectionWorkspot
                (String) obj[4],  // weldingMachineNumber
                 obj[5] != null ? ((Number) obj[5]).intValue() : null,  // numberOfJointsOfferedForInspection
                ((Number) obj[6]).intValue(),  // numberOfPanelsOfferedForInspection
                ((Number) obj[7]).intValue(),  // dimensionalCheckAccepted
                ((Number) obj[8]).intValue(),  // dimensionalCheckRejected
                ((Number) obj[9]).intValue(),  // utCheckAccepted
                ((Number) obj[10]).intValue(), // utCheckRejected
                ((Number) obj[11]).intValue(), // numberOfJointsAccepted
                ((Number) obj[12]).intValue(), // numberOfJointsRejected
                ((Number) obj[13]).intValue(), // numberOfPanelsAccepted
                ((Number) obj[14]).intValue(), // numberOfPanelsRejected
                obj[15] != null ? obj[15].toString() : null  // railLength
        )).collect(Collectors.toList());
    }



    public List<WeldCombinedDto> getInspectionsByDutyId(String dutyId) {
        List<WeldingMasterEntity> masters = wmr.findByDutyId(dutyId);

        List<WeldCombinedDto> result = new ArrayList<>();

        for (WeldingMasterEntity master : masters) {
            WeldCombinedDto dto = new WeldCombinedDto();
            dto.setInspectionMasterId(master.getInspectionMasterId());
            dto.setPanelId(master.getPanelId());
            dto.setDutyId(master.getDutyId());
            dto.setNoOfJoints(master.getNoOfJoints());
            dto.setPanelLength(master.getPanelLength());
            dto.setPanelDecision(master.getPanelDecision());
            dto.setPanelRemark(master.getPanelRemark());
            dto.setFrontRemark(master.getFrontRemark());
            dto.setBackRemark(master.getBackRemark());
            dto.setFrontRemarkForNotOk(master.getFrontResultReasonForNotOk());
            dto.setBackRemarkForNotOk(master.getBackResultReasonForNotOk());
            dto.setFrontRemark(master.getFrontRemark());
            dto.setBackRemark(master.getBackRemark());
            dto.setFrontResult(master.getFrontResult());
            dto.setBackResult(master.getBackResult());


            List<WeldingDetailEntity> details = wmr.findDetailsByInspectionMasterId(master.getInspectionMasterId());
            List<WeldCombinedDto.WeldingDetailDto> detailDtos = details.stream().map(d -> {
                WeldCombinedDto.WeldingDetailDto detailDto = new WeldCombinedDto.WeldingDetailDto();
                detailDto.setJointNo(d.getJointNo());
                detailDto.setWeldParameter(d.getWeldParameter());
                detailDto.setMarking(d.getMarking());
                detailDto.setVisual(d.getVisual());
                detailDto.setDimension(d.getDimension());
                detailDto.setDimensionDetail(d.getDimensionDetail());
                detailDto.setUsfd(d.getUsfd());
                detailDto.setUsfdDetail(d.getUsfdDetail());
                detailDto.setWeldOperator(d.getWeldOperator());
                detailDto.setUsfdOperator(d.getUsfdOperator());
                detailDto.setResult(d.getResult());
                detailDto.setRailId1(d.getRailId1());
                detailDto.setRailId2(d.getRailId2());
                detailDto.setRailId1Length(d.getRailId1Length());
                detailDto.setRailId2Length(d.getRailId2Length());
                detailDto.setRemark(d.getRemark());
                return detailDto;
            }).toList();

            dto.setDetails(detailDtos);

            result.add(dto);
        }

        return result;
    }


}
