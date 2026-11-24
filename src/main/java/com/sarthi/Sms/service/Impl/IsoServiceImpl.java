package com.sarthi.Sms.service.Impl;

import java.math.BigDecimal;
import java.sql.Time;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.sarthi.Sms.constant.RailMillAppConstant;
import com.sarthi.Sms.dto.iso.BloomYardDto;
import com.sarthi.Sms.dto.iso.FilterDto;
import com.sarthi.Sms.dto.iso.RollingControlDto;
import com.sarthi.Sms.dto.iso.RollingControlHeatDetailDto;
import com.sarthi.Sms.dto.iso.RollingFinishingDto;
import com.sarthi.Sms.dto.iso.RollingVerificationDto;
import com.sarthi.Sms.dto.iso.ShortRailMasterDto;
import com.sarthi.Sms.dto.iso.ShortRailRejectionAnalysisDto;
import com.sarthi.Sms.dto.iso.SmsChemicalAnalysisDto;
import com.sarthi.Sms.dto.iso.SmsHeatDtlDto;
import com.sarthi.Sms.dto.iso.SriFinalInspDto;
import com.sarthi.Sms.dto.iso.TempObsvDto;
import com.sarthi.Sms.dto.iso.VerificationChargingDto;
import com.sarthi.Sms.dto.iso.ViOnlineInspectionDto;
import com.sarthi.Sms.dto.iso.WeldDimCheckDto;
import com.sarthi.Sms.dto.iso.WeldHardnessDtlDto;
import com.sarthi.Sms.dto.iso.WeldJointTestDto;
import com.sarthi.Sms.dto.iso.WeldMicroDtlDto;
import com.sarthi.Sms.dto.iso.WeldingMacroDtlDto;
import com.sarthi.Sms.dto.iso.WeldtltDtlDto;
import com.sarthi.Sms.dto.rolling.HotStampingDto;
import com.sarthi.Sms.dto.rolling.PreCamberingDto;
import com.sarthi.Sms.dto.rolling.StraighteningMachineDto;
import com.sarthi.Sms.entity.welding.WeldingMicroRepository;
import com.sarthi.Sms.repository.rolling.FinishingVerificationHotStampingRepository;
import com.sarthi.Sms.repository.rolling.FinishingVerificationPreCamberingRepository;
import com.sarthi.Sms.repository.rolling.FinishingVerificationStraighteningMachineRepository;
import com.sarthi.Sms.repository.rolling.RollingDutyRepository;
import com.sarthi.Sms.repository.rolling.TemperatureObservationRepository;
import com.sarthi.Sms.repository.rolling.VerificationBloomYardRepository;
import com.sarthi.Sms.repository.rolling.VerificationChargingRepository;
import com.sarthi.Sms.repository.shortrail.ShortRailMasterRepository;
import com.sarthi.Sms.repository.sms.BloomDtlRepository;
import com.sarthi.Sms.repository.sms.BloomDtlSms2Repository;
import com.sarthi.Sms.repository.sms.BloomDtlSms3Repository;
import com.sarthi.Sms.repository.vi.ViMasterRepository;
import com.sarthi.Sms.repository.welding.WeldingHardnessRepository;
import com.sarthi.Sms.repository.welding.WeldingMacroRepository;
import com.sarthi.Sms.repository.welding.WeldingMasterRepository;
import com.sarthi.Sms.repository.welding.WeldingTltRepository;
import com.sarthi.Sms.service.IsoService;
import com.sarthi.Sms.util.RailMillCommonUtils;
@Service
public class IsoServiceImpl implements IsoService {

    @Autowired
    private BloomDtlRepository bdr;

    @Autowired
    private FinishingVerificationHotStampingRepository fvhsr;

    @Autowired
    private FinishingVerificationPreCamberingRepository fvpcr;

    @Autowired
    private FinishingVerificationStraighteningMachineRepository fvsr;

    @Autowired
    RollingDutyRepository rdr;

    @Autowired
    VerificationBloomYardRepository vbyr;

    @Autowired
    VerificationChargingRepository vcr;

    @Autowired
    TemperatureObservationRepository tor;

    @Autowired
    private ViMasterRepository vmr;

    @Autowired
    private WeldingMasterRepository wmr;

    @Autowired
    private WeldingTltRepository wtr;

    @Autowired
    private WeldingHardnessRepository whr;

    @Autowired
    private WeldingMacroRepository wmacr;

    @Autowired
    private WeldingMicroRepository wmicr;

    @Autowired
    private ShortRailMasterRepository srmr;

    @Autowired
    private BloomDtlSms2Repository bds2r;

    @Autowired
    private BloomDtlSms3Repository bds3r;

    @Override
    public List<SmsHeatDtlDto> getHeatDtls(FilterDto req) {
        List<Object[]> results = new ArrayList<>();

        if(RailMillAppConstant.SMS_2.equalsIgnoreCase(req.getSms())) {
            System.out.println("SMS2");
            results = bds2r.getHeatDtls(RailMillCommonUtils.convertStringToDateObject(req.getDate()), req.getShift(), req.getSms(),
                    req.getRailGrade());
        }
        else if(RailMillAppConstant.SMS_3.equalsIgnoreCase(req.getSms())) {
            results = bds3r.getHeatDtls(RailMillCommonUtils.convertStringToDateObject(req.getDate()), req.getShift(), req.getSms(),
                    req.getRailGrade());
        }
        // List<Object[]> results = bds2r.getHeatDtls(RailMillCommonUtils.convertStringToDateObject(req.getDate()), req.getShift(),
        //         req.getSms(), req.getRailGrade());

        return results.stream().map(obj -> {
            SmsHeatDtlDto dto = new SmsHeatDtlDto();
            dto.setHeatNumber((String) obj[0]);
            dto.setHeatStage((String) obj[1]);
            dto.setHeatRemark((String) obj[2]);
            dto.setTurnDownTemp((Integer) obj[3]);
            dto.setDegassingVacuum((BigDecimal) obj[4]);
            dto.setDegassingDuration((BigDecimal) obj[5]);
            dto.setCastingTemp((Integer) obj[6]);
            dto.setCasterNumber((String) obj[7]);
            dto.setSequenceNumber((String) obj[8]);
            dto.setHydris((BigDecimal) obj[9]);
            dto.setIsProbeDipped((Boolean) obj[10]);
            dto.setIsHydrogenBw80And100((Boolean) obj[11]);
            dto.setNitrogen((BigDecimal) obj[12]);
            dto.setOxygen((BigDecimal) obj[13]);
            dto.setNumberOfPrimeBlooms((Integer) obj[14]);
            dto.setPrimeBloomsLength((BigDecimal) obj[15]);
            dto.setPrimeBloomsTotalLength((BigDecimal) obj[16]);
            dto.setNumberOfCoBlooms((Integer) obj[17]);
            dto.setCoBloomsLength((BigDecimal) obj[18]);
            dto.setCoBloomsTotalLength((BigDecimal) obj[19]);
            dto.setNumberOfRejectedBlooms((Integer) obj[20]);
            dto.setRejectedBloomsLength((BigDecimal) obj[21]);
            dto.setRejectedBloomsTotalLength((BigDecimal) obj[22]);
            dto.setWeightOfPrimeBlooms((BigDecimal) obj[23]);
            dto.setWeightOfCoBlooms((BigDecimal) obj[24]);
            dto.setWeightOfRejectedBlooms((BigDecimal) obj[25]);
            dto.setTotalCastWt((BigDecimal) obj[26]);
            dto.setIsDiverted((Boolean) obj[27]);
            dto.setSentToLadle((String) obj[28]);

            // Bloom Details
            dto.setCastNumber((String) obj[29]);
            dto.setBloomIdentification((String) obj[30]);
            dto.setLengthOfBlooms((BigDecimal) obj[31]);
            dto.setSurfaceConditionOfBlooms((String) obj[32]);
            dto.setNumberOfPrimeBloomsRejected((Integer) obj[33]);
            dto.setNumberOfCoBloomsRejected((Integer) obj[34]);
            dto.setRemark((String) obj[35]);
            dto.setTurnDownTempWv((String) obj[36]);
            dto.setDegassingVacuumWv((String) obj[37]);
            dto.setDegassingDurationWv((String) obj[38]);
            dto.setIsLadleToTundishUsed((Boolean) obj[39]);
            dto.setIsTundishToMouldUsed((Boolean) obj[40]);
            return dto;
        }).collect(Collectors.toList());
    }

    @Override
    public List<SmsChemicalAnalysisDto> getSmsChemicalAnalysis(FilterDto req) {
        List<Object[]> results = bdr.getSmsChemicalAnalysis(RailMillCommonUtils.convertStringToDateObject(req.getDate()),
                req.getShift(), req.getSms());

        return results.stream().map(obj -> {
            SmsChemicalAnalysisDto dto = new SmsChemicalAnalysisDto();
            dto.setHeatNumber((String) obj[0]);
            dto.setSequenceNumber((String) obj[1]);
            dto.setNitrogen((BigDecimal) obj[2]);
            dto.setOxygen((BigDecimal) obj[3]);
            dto.setHydris((BigDecimal) obj[4]);
            dto.setDegassingVacuum((BigDecimal) obj[5]);
            dto.setDegassingDuration((BigDecimal) obj[6]);
            dto.setHeatRemark((String) obj[7]);

            return dto;
        }).collect(Collectors.toList());
    }

    @Override
    public RollingFinishingDto getRollingFinishingDtls(FilterDto req) {
        // Fetch data using the filters
        
        List<Object[]> hotStampingData = fvhsr.getHsDtls(RailMillCommonUtils.convertStringToDateObject(req.getDate()),
                req.getShift(), req.getRailSection(), req.getMill());
        List<Object[]> preCamberingData = fvpcr.getPcDtls(RailMillCommonUtils.convertStringToDateObject(req.getDate()),
                req.getShift(), req.getRailSection(), req.getMill());
        List<Object[]> straighteningMachineData = fvsr.getSmDtls(RailMillCommonUtils.convertStringToDateObject(req.getDate()),
                req.getShift(), req.getRailSection(), req.getMill());

        // Map the raw data to DTOs

        // HotStampingDto Mapping
        List<HotStampingDto> hotStampingList = hotStampingData.stream().map(obj -> {
            HotStampingDto dto = new HotStampingDto();
            dto.setHeatNo((String) obj[0]);
            dto.setBranding((String) obj[1]);
            return dto;
        }).collect(Collectors.toList());

        // PreCamberingDto Mapping
        List<PreCamberingDto> preCamberingList = preCamberingData.stream().map(obj -> {
            PreCamberingDto dto = new PreCamberingDto();
            dto.setHeatNo((String) obj[0]);
            dto.setIsSatisfactory((Boolean) obj[1]);
            return dto;
        }).collect(Collectors.toList());

        // StraighteningMachineDto Mapping
        List<StraighteningMachineDto> straighteningMachineList = straighteningMachineData.stream().map(obj -> {
            StraighteningMachineDto dto = new StraighteningMachineDto();
            dto.setSmR1Hrsm((String) obj[0]);
            dto.setSmR4Hrsm((String) obj[1]);
            dto.setSmR6Hrsm((String) obj[2]);
            dto.setSmR8Hrsm((String) obj[3]);
            dto.setSmR1Vrsm((String) obj[4]);
            dto.setSmR4Vrsm((String) obj[5]);
            dto.setSmR6Vrsm((String) obj[6]);
            dto.setSmR8Vrsm((String) obj[7]);
            dto.setSmIsSatisfactory((Boolean) obj[8]);
            dto.setSmSrR1Hrsm((String) obj[9]);
            dto.setSmSrR2Hrsm((String) obj[10]);
            dto.setSmSrR3Hrsm((String) obj[11]);
            dto.setSmSrR4Hrsm((String) obj[12]);
            dto.setSmSrR1Vrsm((String) obj[13]);
            dto.setSmSrR2Vrsm((String) obj[14]);
            dto.setSmSrR3Vrsm((String) obj[15]);
            dto.setSmSrR4Vrsm((String) obj[16]);
            dto.setSmSrIsSatisfactory((Boolean) obj[17]);
            dto.setSmLrR1Hrsm((String) obj[18]);
            dto.setSmLrR2Hrsm((String) obj[19]);
            dto.setSmLrR3Hrsm((String) obj[20]);
            dto.setSmLrR4Hrsm((String) obj[21]);
            dto.setSmLrR1Vrsm((String) obj[22]);
            dto.setSmLrR2Vrsm((String) obj[23]);
            dto.setSmLrR3Vrsm((String) obj[24]);
            dto.setSmLrR4Vrsm((String) obj[25]);
            dto.setSmLrIsSatisfactory((Boolean) obj[26]);
            dto.setStraightnessRemarks((String) obj[27]);
            dto.setRemarks((String) obj[28]);
            return dto;
        }).collect(Collectors.toList());

        // Create RollingFinishingDto and set the lists
        RollingFinishingDto responseDto = new RollingFinishingDto();
        responseDto.setHotStampingList(hotStampingList);
        responseDto.setPreCamberingList(preCamberingList);
        responseDto.setStraighteningMachineList(straighteningMachineList);

        return responseDto;
    }

    public List<RollingControlDto> getRollingControlDtls(FilterDto req) {
        List<Object[]> dutyRecords = rdr.findAllRollingDuties(RailMillCommonUtils.convertStringToDateObject(req.getDate()),
                req.getShift(), req.getRailGrade(), req.getRailSection(), req.getMill());
        // if (dutyRecords.isEmpty()) {
        // return List.of(); // Return empty list if no data found
        // }

        return dutyRecords.stream().map(dutyData -> {
            String dutyId = (String) dutyData[0];
            String vernierNumber = (String) dutyData[1];
            String micrometerNumber = (String) dutyData[2];
            Integer numberOfGauges = (Integer) dutyData[3];

            // Fetch heat details for this dutyId
            List<Object[]> heatData = rdr.findHeatDetailsByDutyId(dutyId);
            List<RollingControlHeatDetailDto> heatDetailDtos = heatData.stream().map(obj -> {
                RollingControlHeatDetailDto dto = new RollingControlHeatDetailDto();
                dto.setHeatNumber((String) obj[0]);
                dto.setSampleNumber((Integer) obj[1]);
                dto.setTiming((java.sql.Time) obj[2]);
                dto.setSampleLocation((String) obj[3]);
                dto.setHeight((java.math.BigDecimal) obj[4]);
                dto.setHead((String) obj[5]);
                dto.setFlange((java.math.BigDecimal) obj[6]);
                dto.setWeb((java.math.BigDecimal) obj[7]);
                dto.setAsy((String) obj[8]);
                dto.setFootToe((String) obj[9]);
                dto.setFishingHeight((String) obj[10]);
                dto.setWeight((java.math.BigDecimal) obj[11]);
                dto.setCrownProfile((String) obj[12]);
                dto.setFootFlatness((String) obj[13]);
                dto.setRemark((String) obj[14]);
                return dto;
            }).collect(Collectors.toList());

            // Create response DTO
            RollingControlDto responseDto = new RollingControlDto();
            responseDto.setDutyId(dutyId);
            responseDto.setVernierNumber(vernierNumber);
            responseDto.setMicrometerNumber(micrometerNumber);
            responseDto.setNumberOfGauges(numberOfGauges);
            responseDto.setHeatDtlList(heatDetailDtos);

            return responseDto;
        }).collect(Collectors.toList());
    }

    @Override
    public RollingVerificationDto getRollingVerificationDtls(FilterDto req) {
        List<Object[]> bloomYardResObj = vbyr.findVerificationBloomYardDtls(
                RailMillCommonUtils.convertStringToDateObject(req.getDate()), req.getShift(), req.getRailGrade(),
                req.getRailSection(), req.getMill());
        List<Object[]> chargingResObj = vcr.getChargingVerificationDtls(
                RailMillCommonUtils.convertStringToDateObject(req.getDate()), req.getShift(), req.getRailGrade(),
                req.getRailSection(), req.getMill());
        List<Object[]> tempObjResObj = tor.findTemperatureObsDtls(RailMillCommonUtils.convertStringToDateObject(req.getDate()),
                req.getShift(), req.getRailGrade(), req.getRailSection(), req.getMill());

        List<BloomYardDto> byRes = bloomYardResObj.stream().map(result -> new BloomYardDto(
                (String) result[0], // heat_number
                (Boolean) result[1], // internal_transfer_verification
                (Boolean) result[2] // heat_register_verification
        )).collect(Collectors.toList());

        List<VerificationChargingDto> vcRes = chargingResObj.stream().map(result -> new VerificationChargingDto(
                (String) result[0], // heat_number
                (BigDecimal) result[1], // length
                (String) result[2], // defect
                (Boolean) result[3], // bloom_tracking_verification
                (Boolean) result[4] // hot_blooms_descaling
        )).collect(Collectors.toList());

        List<TempObsvDto> toRes = tempObjResObj.stream().map(result -> new TempObsvDto(
                // (String) RailMillCommonUtils.convertTimeToString((LocalTime) result[0]), //
                // created_at
                (Time) result[0],
                (Integer) result[1], // finishing_temperature
                (Integer) result[2] // soaking_zone_temperature
        )).collect(Collectors.toList());

        RollingVerificationDto res = new RollingVerificationDto();
        res.setBloomYardDtlList(byRes);
        res.setTempDtlList(toRes);
        res.setVerificationChargingDtlList(vcRes);

        return res;
    }

    @Override
    public List<ViOnlineInspectionDto> getViOnlineInspDtls(FilterDto req) {
        List<Object[]> res = vmr.findViOnlineInspDtl(
                RailMillCommonUtils.convertStringToDateObject(req.getDate()),
                req.getShift(),
                req.getRailGrade(),
                req.getRailSection());

        return res.stream()
                .map(ViOnlineInspectionDto::new)
                .collect(Collectors.toList());
    }

    @Override
    public List<WeldDimCheckDto> getWeldDimCheckDtls(FilterDto req) {
        System.out.println("IsoServiceImpl.getWeldDimCheckDtls called with date: " + req.getDate() +
                          ", shift: " + req.getShift() + ", railGrade: " + req.getRailGrade() +
                          ", railSection: " + req.getRailSection());
        List<Object[]> res = wmr.findWeldingInspDimensionalCheck(
                RailMillCommonUtils.convertStringToDateObject(req.getDate()),
                req.getShift(),
                req.getRailGrade(),
                req.getRailSection());
        System.out.println("Query result size: " + res.size());

        return res.stream().map(obj -> new WeldDimCheckDto(
                (String) obj[0], // joint_number
                (String) obj[1], // LH
                (String) obj[2], // 1m RH
                (String) obj[3], // 1m Vertical
                (String) obj[4], // 10cm LH
                (String) obj[5], // 10cm RH
                (String) obj[6], // 10cm Vertical
                (String) obj[7], // visual
                (String) obj[8], // marking
                (String) obj[9] // remark
        )).collect(Collectors.toList());
    }

    @Override
    public List<WeldJointTestDto> getWeldJointtestDtls(FilterDto req) {
        List<Object[]> res = wmr.findJointTestingDtl(
                RailMillCommonUtils.convertStringToDateObject(req.getDate()),
                req.getShift(),
                req.getRailGrade(),
                req.getRailSection());

        return res.stream().map(obj -> new WeldJointTestDto(
                (String) obj[0], // joint_number
                (String) obj[1], // weld_operator
                (String) obj[2], // usfd_operator
                (String) obj[3], // result
                (String) obj[4], // remark
                (String) obj[5] // panel_remark
        )).collect(Collectors.toList());
    }

    @Override
    public WeldtltDtlDto getWeldTltDtls(FilterDto req) {
        System.out.println("IsoServiceImpl.getWeldTltDtls called with jointNo: " + req.getJointNo());
        List<Object[]> res = wtr.findTltDtlByJointNo(req.getJointNo());
        System.out.println("Query result size: " + res.size());

        if (!res.isEmpty()) {
            Object[] obj = res.get(0); // Get the first result
            System.out.println("Found TLT data for joint: " + req.getJointNo());

            return new WeldtltDtlDto(
                    (String) obj[0], // railSection
                    (String) obj[1], // machineNumber
                    ((java.sql.Date) obj[2]).toLocalDate(), // date
                    (BigDecimal) obj[3], // loadApplied
                    (BigDecimal) obj[4], // deflection
                    (BigDecimal) obj[5], // span
                    (String) obj[6], // isBroken
                    (String) obj[7] // remark
            );
        }

        System.out.println("No TLT data found for joint: " + req.getJointNo());
        return new WeldtltDtlDto();
    }

    @Override
    public WeldHardnessDtlDto getWeldHardnesstDtls(FilterDto req) {
        System.out.println("IsoServiceImpl.getWeldHardnesstDtls called with jointNo: " + req.getJointNo());
        List<Object[]> objList = whr.findHardnessDtlByJointNo(req.getJointNo());
        System.out.println("Query result size: " + (objList != null ? objList.size() : "null"));

        if (objList == null || objList.isEmpty()) {
            System.out.println("No hardness data found for joint: " + req.getJointNo());
            return new WeldHardnessDtlDto(); // Return empty DTO
        }

        Object[] obj = objList.get(0); // Get the first row
        System.out.println("Found hardness data for joint: " + req.getJointNo());

        return new WeldHardnessDtlDto(
                RailMillCommonUtils.convertDateToString(((java.sql.Date) obj[0]).toLocalDate()),
                (String) obj[1], // railSection
                (BigDecimal) obj[2], // parentHardnessL
                (BigDecimal) obj[3], // parentHardnessR
                (BigDecimal) obj[4], // hazHardnessL
                (BigDecimal) obj[5], // hazHardnessR
                (BigDecimal) obj[6], // deviationL
                (BigDecimal) obj[7], // deviationR
                (String) obj[8] // remark
        );
    }

    @Override
    public WeldingMacroDtlDto getWeldMacroDtls(FilterDto req) {
        List<Object[]> objList = wmacr.findWeldMacroDtlByJointNo(req.getJointNo());

        if (objList == null || objList.isEmpty()) {
            return new WeldingMacroDtlDto(); // Return empty DTO
        }

        Object[] obj = objList.get(0); // Get the first row

        // if (Objects.nonNull(obj)) {
        return new WeldingMacroDtlDto(
                RailMillCommonUtils.convertDateToString(((java.sql.Date) obj[0]).toLocalDate()),
                (String) obj[1], // railSection
                (String) obj[2], // machineNumber
                (BigDecimal) obj[3], // headL
                (BigDecimal) obj[4], // headR
                (BigDecimal) obj[5], // webL
                (BigDecimal) obj[6], // webR
                (BigDecimal) obj[7], // footL
                (BigDecimal) obj[8], // footR
                (String) obj[9] // remark
        );
        // }
        // return new WeldingMacroDtlDto();
    }

    @Override
    public WeldMicroDtlDto getWeldMicroDtls(FilterDto req) {
        List<Object[]> objList = wmicr.findWeldMicroDtl(req.getJointNo());

        if (objList == null || objList.isEmpty()) {
            return new WeldMicroDtlDto(); // Return empty DTO
        }

        Object[] obj = objList.get(0); // Get the first row

        return new WeldMicroDtlDto(
                RailMillCommonUtils.convertDateToString(((java.sql.Date) obj[0]).toLocalDate()),
                // (java.util.Date) obj[0],
                (String) obj[1], // railSection
                (Boolean) obj[2], // isMicroFree
                (Boolean) obj[3], // isAstmCoarser
                (String) obj[4] // remark
        );
    }

    @Override
    public SriFinalInspDto getSriFinishingDtls(FilterDto req) {
        List<Object[]> masterObjList = srmr.findShortRailMaster(
                RailMillCommonUtils.convertStringToDateObject(req.getDate()), req.getShift(), req.getRailGrade(),
                req.getRailSection());

        // If no records found, return empty result
        if (masterObjList.isEmpty()) {
            SriFinalInspDto emptyResult = new SriFinalInspDto();
            emptyResult.setMasterDtls(new ShortRailMasterDto());
            emptyResult.setRejectionAnalysisDtls(new ArrayList<>());
            return emptyResult;
        }

        ShortRailMasterDto srmd = new ShortRailMasterDto();

        // Initialize all fields to 0 for aggregation
        initializeAggregationFields(srmd);

        // Process all records and aggregate the values
        for (Object[] obj : masterObjList) {

            // Set metadata from first record
            if (srmd.getDutyId() == null) {
                srmd.setDutyId((String) obj[0]);
                srmd.setRailSectionInspected((String) obj[1]);
                srmd.setRailGradeInspected((String) obj[2]);
            }

            // Aggregate acceptance values (CL-A base values only, CL-A+0.1 tracked separately)
            // FIXED: Only use base CL-A values for Final Inspection Report calculations
            srmd.setAcceptance26mCLA(
                srmd.getAcceptance26mCLA() + (obj[3] != null ? (Integer) obj[3] : 0)
            );
            srmd.setAcceptance26mCLA_01(
                srmd.getAcceptance26mCLA_01() + (obj[4] != null ? (Integer) obj[4] : 0)
            );
            srmd.setAcceptance26mCLB(
                srmd.getAcceptance26mCLB() + (obj[5] != null ? (Integer) obj[5] : 0)
            );
            srmd.setAcceptance26mIU(
                srmd.getAcceptance26mIU() + (obj[6] != null ? (Integer) obj[6] : 0)
            );

            srmd.setAcceptance25mCLA(
                srmd.getAcceptance25mCLA() + (obj[7] != null ? (Integer) obj[7] : 0)
            );
            srmd.setAcceptance25mCLA_01(
                srmd.getAcceptance25mCLA_01() + (obj[8] != null ? (Integer) obj[8] : 0)
            );
            srmd.setAcceptance25mCLB(
                srmd.getAcceptance25mCLB() + (obj[9] != null ? (Integer) obj[9] : 0)
            );
            srmd.setAcceptance25mIU(
                srmd.getAcceptance25mIU() + (obj[10] != null ? (Integer) obj[10] : 0)
            );

            srmd.setAcceptance24mCLA(
                srmd.getAcceptance24mCLA() + (obj[11] != null ? (Integer) obj[11] : 0)
            );
            srmd.setAcceptance24mCLA_01(
                srmd.getAcceptance24mCLA_01() + (obj[12] != null ? (Integer) obj[12] : 0)
            );
            srmd.setAcceptance24mCLB(
                srmd.getAcceptance24mCLB() + (obj[13] != null ? (Integer) obj[13] : 0)
            );
            srmd.setAcceptance24mIU(
                srmd.getAcceptance24mIU() + (obj[14] != null ? (Integer) obj[14] : 0)
            );

            srmd.setAcceptance13mCLA(
                srmd.getAcceptance13mCLA() + (obj[15] != null ? (Integer) obj[15] : 0)
            );
            srmd.setAcceptance13mCLA_01(
                srmd.getAcceptance13mCLA_01() + (obj[16] != null ? (Integer) obj[16] : 0)
            );
            srmd.setAcceptance13mCLB(
                srmd.getAcceptance13mCLB() + (obj[17] != null ? (Integer) obj[17] : 0)
            );
            srmd.setAcceptance13mIU(
                srmd.getAcceptance13mIU() + (obj[18] != null ? (Integer) obj[18] : 0)
            );

            srmd.setAcceptance12mCLA(
                srmd.getAcceptance12mCLA() + (obj[19] != null ? (Integer) obj[19] : 0)
            );
            srmd.setAcceptance12mCLA_01(
                srmd.getAcceptance12mCLA_01() + (obj[20] != null ? (Integer) obj[20] : 0)
            );
            srmd.setAcceptance12mCLB(
                srmd.getAcceptance12mCLB() + (obj[21] != null ? (Integer) obj[21] : 0)
            );
            srmd.setAcceptance12mIU(
                srmd.getAcceptance12mIU() + (obj[22] != null ? (Integer) obj[22] : 0)
            );

            srmd.setAcceptance11mCLA(
                srmd.getAcceptance11mCLA() + (obj[23] != null ? (Integer) obj[23] : 0)
            );
            srmd.setAcceptance11mCLA_01(
                srmd.getAcceptance11mCLA_01() + (obj[24] != null ? (Integer) obj[24] : 0)
            );
            srmd.setAcceptance11mCLB(
                srmd.getAcceptance11mCLB() + (obj[25] != null ? (Integer) obj[25] : 0)
            );
            srmd.setAcceptance11mIU(
                srmd.getAcceptance11mIU() + (obj[26] != null ? (Integer) obj[26] : 0)
            );

            srmd.setAcceptance10mCLA(
                srmd.getAcceptance10mCLA() + (obj[27] != null ? (Integer) obj[27] : 0)
            );
            srmd.setAcceptance10mCLA_01(
                srmd.getAcceptance10mCLA_01() + (obj[28] != null ? (Integer) obj[28] : 0)
            );
            srmd.setAcceptance10mCLB(
                srmd.getAcceptance10mCLB() + (obj[29] != null ? (Integer) obj[29] : 0)
            );
            srmd.setAcceptance10mIU(
                srmd.getAcceptance10mIU() + (obj[30] != null ? (Integer) obj[30] : 0)
            );

            // Aggregate rejection values 26m
            srmd.setRejRefRejection26m(
                srmd.getRejRefRejection26m() + (obj[31] != null ? (Integer) obj[31] : 0)
            );
            srmd.setRejRefCutbar26m(
                srmd.getRejRefCutbar26m() + (obj[32] != null ? (Integer) obj[32] : 0)
            );
            srmd.setRejRefRefinish26m(
                srmd.getRejRefRefinish26m() + (obj[33] != null ? (Integer) obj[33] : 0)
            );

            // Aggregate rejection values 13m
            srmd.setRejRefRejection13m(
                srmd.getRejRefRejection13m() + (obj[34] != null ? (Integer) obj[34] : 0)
            );
            srmd.setRejRefCutbar13m(
                srmd.getRejRefCutbar13m() + (obj[35] != null ? (Integer) obj[35] : 0)
            );
            srmd.setRejRefRefinish13m(
                srmd.getRejRefRefinish13m() + (obj[36] != null ? (Integer) obj[36] : 0)
            );

            // Aggregate UTNP values 26m
            srmd.setUtnpRejection26m(
                srmd.getUtnpRejection26m() + (obj[37] != null ? (Integer) obj[37] : 0)
            );
            srmd.setUtnpCutbar26m(
                srmd.getUtnpCutbar26m() + (obj[38] != null ? (Integer) obj[38] : 0)
            );
            srmd.setUtnpRefinish26m(
                srmd.getUtnpRefinish26m() + (obj[39] != null ? (Integer) obj[39] : 0)
            );

            // Aggregate UTNP values 13m
            srmd.setUtnpRejection13m(
                srmd.getUtnpRejection13m() + (obj[40] != null ? (Integer) obj[40] : 0)
            );
            srmd.setUtnpCutbar13m(
                srmd.getUtnpCutbar13m() + (obj[41] != null ? (Integer) obj[41] : 0)
            );
            srmd.setUtnpRefinish13m(
                srmd.getUtnpRefinish13m() + (obj[42] != null ? (Integer) obj[42] : 0)
            );

            // Set remarks from last record (or keep first non-null)
            if (obj[43] != null && !((String) obj[43]).trim().isEmpty()) {
                srmd.setRemarks((String) obj[43]);
            }
        }

        // Calculate correct aggregated values for Final Inspection Report AFTER processing all records
        calculateCorrectAggregatedValues(srmd);



        List<Object[]> resObjList = srmr.findSriRejectionAnalysisDtls(
                RailMillCommonUtils.convertStringToDateObject(req.getDate()), req.getShift(), req.getRailGrade(),
                req.getRailSection());

        List<ShortRailRejectionAnalysisDto> rejectionAnalysisDtls = resObjList.stream()
                .map(row -> new ShortRailRejectionAnalysisDto(
                        (String) row[0],
                        (Integer) row[1],
                        (String) row[2],
                        (Integer) row[3]))
                .collect(Collectors.toList());

        SriFinalInspDto res = new SriFinalInspDto();
        res.setMasterDtls(srmd);
        res.setRejectionAnalysisDtls(rejectionAnalysisDtls);

        return res;
    }

    /**
     * Calculate aggregated values for Final Inspection Report - CORRECT IMPLEMENTATION
     * Based on the reference image, the system SHOULD aggregate:
     * - Prime Quality 26m = 26m + 25m + 24m lengths
     * - Prime Quality 13m = 13m + 12m + 11m + 10m lengths
     * - Total Classified = Prime Quality + IU + Rejection
     * - Total Inspected = Total Classified + Cutbar + Refinish
     */
    private void calculateCorrectAggregatedValues(ShortRailMasterDto srmd) {

        // Prime Quality aggregated values (26m category = 26m+25m+24m)
        // Updated to include both CL-A and CL-A+0.1 values as per requirement
        int primeQuality26mCLA = (srmd.getAcceptance26mCLA() != null ? srmd.getAcceptance26mCLA() : 0) +
                                (srmd.getAcceptance26mCLA_01() != null ? srmd.getAcceptance26mCLA_01() : 0) +
                                (srmd.getAcceptance25mCLA() != null ? srmd.getAcceptance25mCLA() : 0) +
                                (srmd.getAcceptance25mCLA_01() != null ? srmd.getAcceptance25mCLA_01() : 0) +
                                (srmd.getAcceptance24mCLA() != null ? srmd.getAcceptance24mCLA() : 0) +
                                (srmd.getAcceptance24mCLA_01() != null ? srmd.getAcceptance24mCLA_01() : 0);
        srmd.setPrimeQuality26mCLA(primeQuality26mCLA);

        int primeQuality26mCLB = (srmd.getAcceptance26mCLB() != null ? srmd.getAcceptance26mCLB() : 0) +
                                (srmd.getAcceptance25mCLB() != null ? srmd.getAcceptance25mCLB() : 0) +
                                (srmd.getAcceptance24mCLB() != null ? srmd.getAcceptance24mCLB() : 0);
        srmd.setPrimeQuality26mCLB(primeQuality26mCLB);

        int primeQuality26mIU = (srmd.getAcceptance26mIU() != null ? srmd.getAcceptance26mIU() : 0) +
                               (srmd.getAcceptance25mIU() != null ? srmd.getAcceptance25mIU() : 0) +
                               (srmd.getAcceptance24mIU() != null ? srmd.getAcceptance24mIU() : 0);
        srmd.setPrimeQuality26mIU(primeQuality26mIU);

        // Prime Quality aggregated values (13m category = 13m+12m+11m+10m)
        // Updated to include both CL-A and CL-A+0.1 values as per requirement
        int primeQuality13mCLA = (srmd.getAcceptance13mCLA() != null ? srmd.getAcceptance13mCLA() : 0) +
                                (srmd.getAcceptance13mCLA_01() != null ? srmd.getAcceptance13mCLA_01() : 0) +
                                (srmd.getAcceptance12mCLA() != null ? srmd.getAcceptance12mCLA() : 0) +
                                (srmd.getAcceptance12mCLA_01() != null ? srmd.getAcceptance12mCLA_01() : 0) +
                                (srmd.getAcceptance11mCLA() != null ? srmd.getAcceptance11mCLA() : 0) +
                                (srmd.getAcceptance11mCLA_01() != null ? srmd.getAcceptance11mCLA_01() : 0) +
                                (srmd.getAcceptance10mCLA() != null ? srmd.getAcceptance10mCLA() : 0) +
                                (srmd.getAcceptance10mCLA_01() != null ? srmd.getAcceptance10mCLA_01() : 0);
        srmd.setPrimeQuality13mCLA(primeQuality13mCLA);

        int primeQuality13mCLB = (srmd.getAcceptance13mCLB() != null ? srmd.getAcceptance13mCLB() : 0) +
                                (srmd.getAcceptance12mCLB() != null ? srmd.getAcceptance12mCLB() : 0) +
                                (srmd.getAcceptance11mCLB() != null ? srmd.getAcceptance11mCLB() : 0) +
                                (srmd.getAcceptance10mCLB() != null ? srmd.getAcceptance10mCLB() : 0);
        srmd.setPrimeQuality13mCLB(primeQuality13mCLB);

        int primeQuality13mIU = (srmd.getAcceptance13mIU() != null ? srmd.getAcceptance13mIU() : 0) +
                               (srmd.getAcceptance12mIU() != null ? srmd.getAcceptance12mIU() : 0) +
                               (srmd.getAcceptance11mIU() != null ? srmd.getAcceptance11mIU() : 0) +
                               (srmd.getAcceptance10mIU() != null ? srmd.getAcceptance10mIU() : 0);
        srmd.setPrimeQuality13mIU(primeQuality13mIU);

        // Force UTNP values to zero to prevent them from affecting calculations
        srmd.setUtnpRejection26m(0);
        srmd.setUtnpCutbar26m(0);
        srmd.setUtnpRefinish26m(0);
        srmd.setUtnpRejection13m(0);
        srmd.setUtnpCutbar13m(0);
        srmd.setUtnpRefinish13m(0);

        // Calculate Total Classified = Prime Quality + Rejection (REJ-REF only, NEVER include UTNP)
        // CRITICAL FIX: Based on user analysis, expected 26M = 43+30+30+1 = 114
        int totalClassified26m = primeQuality26mCLA + primeQuality26mCLB + primeQuality26mIU +
                                (srmd.getRejRefRejection26m() != null ? srmd.getRejRefRejection26m() : 0);
        // ENSURE UTNP values are NEVER added to Total Classified
        srmd.setTotalClassified26m(totalClassified26m);

        int totalClassified13m = primeQuality13mCLA + primeQuality13mCLB + primeQuality13mIU +
                                (srmd.getRejRefRejection13m() != null ? srmd.getRejRefRejection13m() : 0);
        // ENSURE UTNP values are NEVER added to Total Classified
        srmd.setTotalClassified13m(totalClassified13m);

        // Calculate Total Inspected = Total Classified + Cutbar + Refinish (REJ-REF only, not UTNP)
        // FIXED: Only use REJ-REF cutbar/refinish values, not UTNP (based on reference image)
        int totalInspected26m = totalClassified26m +
                               (srmd.getRejRefCutbar26m() != null ? srmd.getRejRefCutbar26m() : 0) +
                               (srmd.getRejRefRefinish26m() != null ? srmd.getRejRefRefinish26m() : 0);
        srmd.setTotalInspected26m(totalInspected26m);

        int totalInspected13m = totalClassified13m +
                               (srmd.getRejRefCutbar13m() != null ? srmd.getRejRefCutbar13m() : 0) +
                               (srmd.getRejRefRefinish13m() != null ? srmd.getRejRefRefinish13m() : 0);
        srmd.setTotalInspected13m(totalInspected13m);


    }

    /**
     * Initialize all aggregation fields to 0
     */
    private void initializeAggregationFields(ShortRailMasterDto srmd) {
        // Acceptance fields
        srmd.setAcceptance26mCLA(0);
        srmd.setAcceptance26mCLA_01(0);
        srmd.setAcceptance26mCLB(0);
        srmd.setAcceptance26mIU(0);

        srmd.setAcceptance25mCLA(0);
        srmd.setAcceptance25mCLA_01(0);
        srmd.setAcceptance25mCLB(0);
        srmd.setAcceptance25mIU(0);

        srmd.setAcceptance24mCLA(0);
        srmd.setAcceptance24mCLA_01(0);
        srmd.setAcceptance24mCLB(0);
        srmd.setAcceptance24mIU(0);

        srmd.setAcceptance13mCLA(0);
        srmd.setAcceptance13mCLA_01(0);
        srmd.setAcceptance13mCLB(0);
        srmd.setAcceptance13mIU(0);

        srmd.setAcceptance12mCLA(0);
        srmd.setAcceptance12mCLA_01(0);
        srmd.setAcceptance12mCLB(0);
        srmd.setAcceptance12mIU(0);

        srmd.setAcceptance11mCLA(0);
        srmd.setAcceptance11mCLA_01(0);
        srmd.setAcceptance11mCLB(0);
        srmd.setAcceptance11mIU(0);

        srmd.setAcceptance10mCLA(0);
        srmd.setAcceptance10mCLA_01(0);
        srmd.setAcceptance10mCLB(0);
        srmd.setAcceptance10mIU(0);

        // Rejection fields
        srmd.setRejRefRejection26m(0);
        srmd.setRejRefCutbar26m(0);
        srmd.setRejRefRefinish26m(0);
        srmd.setRejRefRejection13m(0);
        srmd.setRejRefCutbar13m(0);
        srmd.setRejRefRefinish13m(0);

        // UTNP fields
        srmd.setUtnpRejection26m(0);
        srmd.setUtnpCutbar26m(0);
        srmd.setUtnpRefinish26m(0);
        srmd.setUtnpRejection13m(0);
        srmd.setUtnpCutbar13m(0);
        srmd.setUtnpRefinish13m(0);
    }

    @Override
    public Map<String, Object> debugDatabaseQuery(FilterDto req) {
        System.out.println("=== COMPREHENSIVE DATABASE DEBUG ===");
        System.out.println("Request parameters:");
        System.out.println("- Date: " + req.getDate());
        System.out.println("- Shift: " + req.getShift());
        System.out.println("- Rail Grade: " + req.getRailGrade());
        System.out.println("- Rail Section: " + req.getRailSection());

        Map<String, Object> debugInfo = new HashMap<>();

        try {
            // Step 1: Check if duty exists for the given date/shift
            System.out.println("\n=== STEP 1: DUTY VERIFICATION ===");
            // Note: We need to add a query to check short_rail_duty table

            // Step 2: Execute the main query
            System.out.println("\n=== STEP 2: MAIN QUERY EXECUTION ===");
            List<Object[]> rawResults = srmr.findShortRailMaster(
                RailMillCommonUtils.convertStringToDateObject(req.getDate()),
                req.getShift(),
                req.getRailGrade(),
                req.getRailSection()
            );

            System.out.println("Query returned " + rawResults.size() + " records");
            debugInfo.put("recordCount", rawResults.size());

            if (!rawResults.isEmpty()) {
                System.out.println("\n=== STEP 3: RAW DATA ANALYSIS ===");
                for (int i = 0; i < rawResults.size(); i++) {
                    Object[] record = rawResults.get(i);
                    System.out.println("Record " + (i + 1) + ":");
                    System.out.println("  Duty ID: " + record[0]);
                    System.out.println("  Rail Section: " + record[1]);
                    System.out.println("  Rail Grade: " + record[2]);
                    System.out.println("  26m CL-A: " + record[3]);
                    System.out.println("  26m CL-A+0.1: " + record[4]);
                    System.out.println("  26m CL-B: " + record[5]);
                    System.out.println("  26m IU: " + record[6]);
                    System.out.println("  25m CL-A: " + record[7]);
                    System.out.println("  25m CL-A+0.1: " + record[8]);
                    System.out.println("  24m CL-A: " + record[11]);
                    System.out.println("  24m CL-A+0.1: " + record[12]);
                    System.out.println("  13m CL-A: " + record[15]);
                    System.out.println("  13m CL-A+0.1: " + record[16]);
                    System.out.println("  Rejection 26m: " + record[31]);
                    System.out.println("  Cutbar 26m: " + record[32]);
                    System.out.println("  Refinish 26m: " + record[33]);
                    System.out.println("  Full record: " + java.util.Arrays.toString(record));
                }

                // Store first record for analysis
                debugInfo.put("firstRecord", rawResults.get(0));
                debugInfo.put("allRecords", rawResults);
            }

            // Step 4: Test the service processing
            System.out.println("\n=== STEP 4: SERVICE PROCESSING TEST ===");
            SriFinalInspDto processedResult = getSriFinishingDtls(req);
            debugInfo.put("processedResult", processedResult);

            if (processedResult != null && processedResult.getMasterDtls() != null) {
                System.out.println("Processed results:");
                System.out.println("  Prime Quality 26m CLA: " + processedResult.getMasterDtls().getPrimeQuality26mCLA());
                System.out.println("  Prime Quality 13m CLA: " + processedResult.getMasterDtls().getPrimeQuality13mCLA());
                System.out.println("  Total Classified 26m: " + processedResult.getMasterDtls().getTotalClassified26m());
                System.out.println("  Total Inspected 26m: " + processedResult.getMasterDtls().getTotalInspected26m());
            }

        } catch (Exception e) {
            System.err.println("Error during debug: " + e.getMessage());
            e.printStackTrace();
            debugInfo.put("error", e.getMessage());
        }

        return debugInfo;
    }

}
