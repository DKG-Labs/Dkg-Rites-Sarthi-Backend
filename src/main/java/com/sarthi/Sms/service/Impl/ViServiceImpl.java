package com.sarthi.Sms.service.Impl;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.sarthi.Sms.constant.RailMillAppConstant;
import com.sarthi.Sms.dto.sms.common.DutyStatusResDto;
import com.sarthi.Sms.dto.sms.common.EndDutyReqDto;
import com.sarthi.Sms.dto.sms.common.ReportReqDto;
import com.sarthi.Sms.dto.sms.common.StartDutyReqDto;
import com.sarthi.Sms.dto.sms.common.StartDutyResDto;
import com.sarthi.Sms.dto.vi.AcptDataReportDto;
import com.sarthi.Sms.dto.vi.ViAcceptDtlsDto;
import com.sarthi.Sms.dto.vi.ViAcptRejReportDto;
import com.sarthi.Sms.dto.vi.ViDefectDtlsDto;
import com.sarthi.Sms.dto.vi.ViDefectRecordDto;
import com.sarthi.Sms.dto.vi.ViDtlsDto;
import com.sarthi.Sms.dto.vi.ViShiftSummaryResDto;
import com.sarthi.Sms.dto.vi.ViShiftSummaryReqDto;
import com.sarthi.Sms.dto.vi.ViSubRailIdEntity;
import com.sarthi.Sms.dto.vi.ViSubRailIdWithLengthDto;
import com.sarthi.Sms.dto.vi.ViAiComparisonDto;
import com.sarthi.Sms.dto.dashboard.SurfaceInspectionResDto;
import com.sarthi.Sms.dto.dashboard.DimensionalInspectionResDto;
import com.sarthi.Sms.entity.visualInspection.ViAcceptDataEntity;
import com.sarthi.Sms.entity.visualInspection.ViAcptId;
import com.sarthi.Sms.entity.visualInspection.ViDefectDataEntity;
import com.sarthi.Sms.entity.visualInspection.ViDutyEntity;
import com.sarthi.Sms.entity.visualInspection.ViMasterEntity;
import com.sarthi.Sms.exception.SmsErrorDetails;
import com.sarthi.Sms.exception.SmsInvalidArgumentException;
import com.sarthi.Sms.exception.SmsResourceNotFoundException;
import com.sarthi.Sms.repository.vi.ViAcceptDataRepository;
import com.sarthi.Sms.repository.vi.ViDefectDataRepository;
import com.sarthi.Sms.repository.vi.ViDutyRepository;
import com.sarthi.Sms.repository.vi.ViMasterRepository;
import com.sarthi.Sms.repository.vi.ViSubRailIdRepository;
import com.sarthi.Sms.repository.heatnumber.HeatnumberInspectionRepository;
import com.sarthi.Sms.service.DutySequenceService;
import com.sarthi.Sms.service.UserLookupService;
import com.sarthi.Sms.service.SmsService;
import com.sarthi.Sms.service.ViService;
import com.sarthi.Sms.service.RailMillDashboardService;
import com.sarthi.Sms.util.RailMillCommonUtils;
import com.sarthi.Sms.util.DutyEnum;
import jakarta.transaction.Transactional;

@Service
public class ViServiceImpl implements ViService {

    @Autowired
    private ViDutyRepository vdr;

    @Autowired
    private DutySequenceService dss;

    @Autowired
    private UserLookupService userLookupService;

    @Autowired
    private ViMasterRepository vmr;

    @Autowired
    private ViAcceptDataRepository vadr;

    @Autowired
    private ViSubRailIdRepository vsrir;

    @Autowired
    private ViDefectDataRepository vddr;

    @Autowired
    private SmsService ss;

    @Autowired
    private HeatnumberInspectionRepository heatnumberInspectionRepository;

    @Autowired
    private RailMillDashboardService dashboardService;

    @Override
    @Transactional
    public StartDutyResDto startDuty(String ah, StartDutyReqDto req) {
        ViDutyEntity ve = new ViDutyEntity();
        String userId = RailMillCommonUtils.getUserIdFromAuthHeader(ah);

        // check if a duty is already in progress
        DutyStatusResDto dsrd = checkDutyStatus(ah);
        if (dsrd.getIsDutyInProgess()) {
            throw new SmsInvalidArgumentException(new SmsErrorDetails(RailMillAppConstant.USER_ALREADY_EXISTS,
                    RailMillAppConstant.USER_ALREADY_EXISTS, RailMillAppConstant.ERROR_TYPE_VALIDATION,
                    "Cannot start new duty for the user. Previous duty already in progress"));
        }

        LocalDate date = RailMillCommonUtils.convertStringToDateObject(req.getStartDate());

        String dutyId = dss.generateDutyId(DutyEnum.VISUAL_INSPECTION, date);
        ve.setDutyId(dutyId);
        ve.setUserId(userId);
        ve.setDate(date);
        ve.setShift(req.getShift());
        ve.setMill(req.getMill());
        ve.setLineNo(req.getLineNo());
        ve.setRailGrade(req.getRailGrade());
        ve.setRailSection(req.getRailSection());
        ve.setStdOffLength(req.getStdOffLength());
        ve.setIeName1(req.getIeName1());
        ve.setIeName2(req.getIeName2());
        ve.setIeName3(req.getIeName3());
        ve.setRclIeName1(req.getRclIeName1());
        ve.setRclIeName2(req.getRclIeName2());
        ve.setRclIeName3(req.getRclIeName3());
        ve.setStartTime(LocalDateTime.now());
        ve = vdr.save(ve);

        StartDutyResDto sdrd = new StartDutyResDto();
        sdrd.setDate(RailMillCommonUtils.convertDateToString(ve.getDate()));
        sdrd.setDutyId(ve.getDutyId());
        sdrd.setShift(ve.getShift());
        sdrd.setMill(ve.getMill());
        sdrd.setLineNo(ve.getLineNo());
        sdrd.setRailGrade(ve.getRailGrade());
        sdrd.setRailSection(ve.getRailSection());
        sdrd.setStdOffLength(ve.getStdOffLength());
        sdrd.setIeName1(ve.getIeName1());
        sdrd.setIeName2(ve.getIeName2());
        sdrd.setIeName3(ve.getIeName3());
        sdrd.setRclIeName1(ve.getRclIeName1());
        sdrd.setRclIeName2(ve.getRclIeName2());
        sdrd.setRclIeName3(ve.getRclIeName3());
        sdrd.setStartTime(RailMillCommonUtils.extractTime(ve.getStartTime()));

        return sdrd;
    }

    @Override
    public DutyStatusResDto checkDutyStatus(String ah) {
        String userId = RailMillCommonUtils.getUserIdFromAuthHeader(ah);
        String dutyId = vdr.checkDutyStatus(userId);
        DutyStatusResDto dsrd = new DutyStatusResDto();
        dsrd.setDutyId(dutyId);
        dsrd.setIsDutyInProgess(dutyId != null);

        return dsrd;
    }

    @Override
    @Transactional
    public void endDuty(EndDutyReqDto req) {
        ViDutyEntity vde = vdr.findByDutyId(req.getDutyId())
                .orElseThrow(() -> new SmsResourceNotFoundException(
                        new SmsErrorDetails(RailMillAppConstant.ERROR_CODE_RESOURCE,
                                RailMillAppConstant.ERROR_TYPE_CODE_RESOURCE,
                                RailMillAppConstant.ERROR_TYPE_RESOURCE,
                                "No duties found for the provided duty id.")));

        if (Objects.nonNull(vde.getEndTime())) {
            throw new SmsInvalidArgumentException(
                    new SmsErrorDetails(
                            RailMillAppConstant.ERROR_CODE_VALIDATION,
                            RailMillAppConstant.ERROR_TYPE_CODE_VALIDATION,
                            RailMillAppConstant.ERROR_TYPE_VALIDATION,
                            "Duty already ended."));
        }

        vde.setShiftRemarks(req.getShiftRemarks());
        vde.setIeConfirmation(req.getIeConfirmation());
        vde.setEndTime(LocalDateTime.now());
        vdr.save(vde);
    }

    @Override
    public StartDutyResDto getOngoingDutyDtls(String ah) {
        String userId = RailMillCommonUtils.getUserIdFromAuthHeader(ah);
        Optional<ViDutyEntity> vdeOpt = vdr.getOngoingDutyDtls(userId);

        StartDutyResDto sdrd = new StartDutyResDto();
        if (vdeOpt.isPresent()) {
            ViDutyEntity vde = vdeOpt.get();
            sdrd.setDate(RailMillCommonUtils.convertDateToString(vde.getDate()));
            sdrd.setDutyId(vde.getDutyId());
            sdrd.setShift(vde.getShift());
            sdrd.setMill(vde.getMill());
            sdrd.setLineNo(vde.getLineNo());
            sdrd.setRailGrade(vde.getRailGrade());
            sdrd.setRailSection(vde.getRailSection());
            sdrd.setStdOffLength(vde.getStdOffLength());
            sdrd.setIeName1(vde.getIeName1());
            sdrd.setIeName2(vde.getIeName2());
            sdrd.setIeName3(vde.getIeName3());
            sdrd.setRclIeName1(vde.getRclIeName1());
            sdrd.setRclIeName2(vde.getRclIeName2());
            sdrd.setRclIeName3(vde.getRclIeName3());
            sdrd.setStartTime(RailMillCommonUtils.extractTime(vde.getStartTime()));
        }

        return sdrd;
    }

    @Override
    @Transactional
    public void saveViDtls(ViDtlsDto req) {
        /**
         * BLOOM STAGE VALIDATION LOGIC:
         * - Heat numbers fetched from heatnumber_inspection table MUST be validated for bloom stage
         * - This ensures that heat numbers from AI/OCR system also follow proper SMS bloom cutting workflow
         * - All heat numbers (both from inspection table and manually entered) need bloom stage validation
         */

        // Check if heat number comes from heatnumber_inspection table
        String heatNumberFromInspection = null;
        boolean isHeatFromInspectionTable = false;

        try {
            heatNumberFromInspection = heatnumberInspectionRepository.findHeatnumberByRailId(req.getRailId());
            if (heatNumberFromInspection != null && !heatNumberFromInspection.trim().isEmpty()) {
                // Normalize both heat numbers for comparison (remove leading zeros, trim whitespace)
                String normalizedInspectionHeat = heatNumberFromInspection.trim().replaceFirst("^0+", "");
                String normalizedRequestHeat = req.getHeatNo().trim().replaceFirst("^0+", "");

                isHeatFromInspectionTable = normalizedInspectionHeat.equals(normalizedRequestHeat);
            }
        } catch (Exception e) {
            System.err.println("Error checking heatnumber_inspection table for rail ID " + req.getRailId() + ": " + e.getMessage());
            // If there's an error checking the table, proceed with bloom stage validation for safety
            isHeatFromInspectionTable = false;
        }

        // Validate bloom stage for ALL heat numbers (both from inspection table and manually entered)
        if (isHeatFromInspectionTable) {
            System.out.println("INFO: Validating bloom stage for heat number " + req.getHeatNo() +
                             " from heatnumber_inspection table (" + heatNumberFromInspection +
                             ") for rail ID " + req.getRailId());
            ss.validateHeatBloomStage(req.getHeatNo());
        } else {
            System.out.println("INFO: Validating bloom stage for manually entered heat number " + req.getHeatNo() +
                             " for rail ID " + req.getRailId());
            ss.validateHeatBloomStage(req.getHeatNo());
        }

        // try{
        Optional<ViMasterEntity> vmeOpt = vmr.findById(req.getRailId());

        if (vmeOpt.isPresent() && (Objects.isNull(req.getUpdateData()) || !req.getUpdateData())) {
            throw new SmsInvalidArgumentException(
                    new SmsErrorDetails(
                            RailMillAppConstant.ERROR_CODE_INVALID,
                            RailMillAppConstant.ERROR_TYPE_CODE_INVALID,
                            RailMillAppConstant.ERROR_TYPE_INVALID,
                            "Provided rail ID already present."));
        }

        // If updating existing data, clear related data first
        if (vmeOpt.isPresent() && Objects.nonNull(req.getUpdateData()) && req.getUpdateData()) {
            String railId = req.getRailId();

            // Delete existing sub rail IDs
            List<ViSubRailIdEntity> existingSubRailIds = vsrir.findByRailId(railId);
            vsrir.deleteAll(existingSubRailIds);

            // Delete existing defect data
            List<ViDefectDataEntity> existingDefectData = vddr.findByRailId(railId);
            vddr.deleteAll(existingDefectData);

            // Delete existing accept data
            List<ViAcceptDataEntity> existingAcceptData = vadr.findByRailId(railId);
            vadr.deleteAll(existingAcceptData);
        }

        ViMasterEntity vme = new ViMasterEntity();
        vme.setDutyId(req.getDutyId());
        // vme.setViId(new ViId(req.getHeatNo(), req.getRailId()));
        vme.setRailId(req.getRailId());
        vme.setHeatNo(req.getHeatNo());
        vme.setDate(RailMillCommonUtils.convertStringToDateObject(req.getDate()));
        vme.setShift(req.getShift());
        vme.setSerialNo(req.getSerialNo());
        vme.setHeatStatus(req.getHeatStatus());
        vme.setActualOfferedLength(req.getActualOfferedLength());
        vme.setUtFeedback(req.getUtFeedback());
        vme.setDimFeedback(req.getDimFeedback());
        vme.setVisualFeedback(req.getVisualFeedback());
        vme.setGaugingEndStraightness(req.getGaugingEndStraightness());
        vme.setRej13(req.getRej13());
        vme.setRej12(req.getRej12());
        vme.setRej11(req.getRej11());
        vme.setRej10(req.getRej10());
        vme.setRejCompLength(req.getRejCompLength());
        vme.setRemarks(req.getRemarks());
        // if(Objects.nonNull(req.getImage())){
        //     vme.setImage(req.getImage().getBytes());
        // }
        vme = vmr.save(vme);

        for(ViAcceptDtlsDto vadd : req.getAcptDataList()){
            ViAcceptDataEntity vae = new ViAcceptDataEntity();
            vae.setRailId(vme.getRailId());
            vae.setAcceptedLength(vadd.getAcceptedLength());
            vae.setAcceptedNo(vadd.getAcceptedNo());
            vae.setRailClass(vadd.getRailClass());

            vae = vadr.save(vae);

            // Generate sub rail IDs using length-wise code nomenclature
            for(int i=0; i<vadd.getAcceptedNo(); i++){
                String subRailId;

                if (shouldKeepOriginalRailId(vadd.getAcceptedLength(), vme.getActualOfferedLength())) {
                    // For large pieces (>65m), keep the parent rail ID
                    subRailId = req.getRailId();
                } else {
                    // For other lengths, use the length-wise code system
                    String lengthCode = getLengthCode(vadd.getAcceptedLength(), i + 1);
                    subRailId = req.getRailId() + "-" + lengthCode;
                }

                ViSubRailIdEntity vsrie = new ViSubRailIdEntity();
                vsrie.setRailId(vae.getRailId());
                vsrie.setSubRailId(subRailId);
                vsrie.setAcceptedLength(vadd.getAcceptedLength());

                vsrir.save(vsrie);
            }
        }

        for(ViDefectDtlsDto vddd : req.getDefectDataList()){
            ViDefectDataEntity vdde = new ViDefectDataEntity();
            vdde.setDefectCategory(vddd.getDefectCategory());
            vdde.setDefectType(vddd.getDefectType());
            vdde.setLocation(vddd.getLocation());
            vdde.setPosition(vddd.getPosition());
            // vdde.setHeatNo(vme.getHeatNo());
            vdde.setRailId(vme.getRailId());
            vddr.save(vdde);
        }

    // }catch(Exception e){
    //     throw new SmsInvalidArgumentException(new SmsErrorDetails(
    //         RailMillAppConstant.ERROR_CODE_INVALID,
    //         RailMillAppConstant.ERROR_TYPE_CODE_INVALID,
    //         RailMillAppConstant.ERROR_TYPE_INVALID,
    //         "Image upload failed."));
    // }
    }

    public String getNextSerialNo(){
        return "abcd";
    }

    /**
     * Generates length-wise code for sub rail IDs based on accepted length and sequence number
     * Following the nomenclature:
     * 65m → S1, S2, S3...
     * 52m → F1, F2, F3...
     * 26m → A1, A2, A3...
     * 25m → B1, B2, B3...
     * 24m → C1, C2, C3...
     * 23m → D1, D2, D3...
     * 22m → E1, E2, E3...
     * 21m → G1, G2, G3... (F is used for 52m)
     * 20m → H1, H2, H3...
     * 19m → I1, I2, I3...
     * 18m → J1, J2, J3...
     * 17m → K1, K2, K3...
     * 16m → L1, L2, L3...
     * 15m → M1, M2, M3...
     * 14m → N1, N2, N3...
     * 13m → W1 to W10
     * 12m → X1 to X10
     * 11m → Y1 to Y11
     * 10m → Z1 to Z13
     */
    private String getLengthCode(BigDecimal acceptedLength, int sequenceNumber) {
        int length = acceptedLength.intValue();

        switch (length) {
            case 65: return "S" + sequenceNumber;
            case 52: return "F" + sequenceNumber;
            case 26: return "A" + sequenceNumber;
            case 25: return "B" + sequenceNumber;
            case 24: return "C" + sequenceNumber;
            case 23: return "D" + sequenceNumber;
            case 22: return "E" + sequenceNumber;
            case 21: return "G" + sequenceNumber; // F is used for 52m
            case 20: return "H" + sequenceNumber;
            case 19: return "I" + sequenceNumber;
            case 18: return "J" + sequenceNumber;
            case 17: return "K" + sequenceNumber;
            case 16: return "L" + sequenceNumber;
            case 15: return "M" + sequenceNumber;
            case 14: return "N" + sequenceNumber;
            case 13:
                if (sequenceNumber <= 10) {
                    return "W" + sequenceNumber;
                }
                break;
            case 12:
                if (sequenceNumber <= 10) {
                    return "X" + sequenceNumber;
                }
                break;
            case 11:
                if (sequenceNumber <= 11) {
                    return "Y" + sequenceNumber;
                }
                break;
            case 10:
                if (sequenceNumber <= 13) {
                    return "Z" + sequenceNumber;
                }
                break;
            default:
                // For non-standard lengths, use a fallback format
                return "L" + length + "_" + sequenceNumber;
        }

        // If sequence number exceeds limit, use fallback format with warning
        return "L" + length + "_" + sequenceNumber + "_EXCEED";
    }

    /**
     * Checks if the accepted length should keep the original rail ID
     * This happens when:
     * 1. The accepted length equals the actual offered length (full length piece)
     * 2. The accepted length is greater than 65m (large pieces keep parent rail ID)
     */
    private boolean shouldKeepOriginalRailId(BigDecimal acceptedLength, BigDecimal actualOfferedLength) {
        // If accepted length equals actual offered length, keep original ID
        if (acceptedLength.compareTo(actualOfferedLength) == 0) {
            return true;
        }

        // For pieces greater than 65m, keep the original Parent Rail ID
        if (acceptedLength.compareTo(new BigDecimal("65")) > 0) {
            return true;
        }

        return false;
    }

    @Override
    public List<ViAcptRejReportDto> getAcptRejReport(ReportReqDto req){
        LocalDateTime startDate = RailMillCommonUtils.convertStringToDateObject(req.getStartDate()).atStartOfDay();
        LocalDateTime endDate = RailMillCommonUtils.convertStringToDateObject(req.getEndDate()).atTime(LocalTime.MAX);

        List<Object[]> resObjList = vmr.getActRejReport(startDate, endDate, req.getLineNo());

        return resObjList.stream().map(row -> {
            ViAcptRejReportDto dto = new ViAcptRejReportDto();
            dto.setDate(RailMillCommonUtils.convertDateToString(((java.sql.Date) row[0]).toLocalDate()));
            dto.setShift((String) row[1]); // Map shift
            dto.setInspectionWorkspot((String) row[2]); // Map inspection workspot
            dto.setRailGrade((String) row[3]); // Map rail grade
            dto.setRailSection((String) row[4]); // Map rail section
            // Round standard offered length to 2 decimal places for proper display
            dto.setStdOfferedLengthForInspection(roundToTwoDecimalPlaces((BigDecimal) row[5])); // Map standard offered length
            dto.setRailsOfferedForInspection(((Number) row[6]).longValue()); // Map rails offered for inspection

            // Map acceptance data by rail class and length
            dto.setClassA130m(((Number) row[7]).longValue());
            dto.setClassA117m(((Number) row[8]).longValue());
            dto.setClassA104m(((Number) row[9]).longValue());
            dto.setClassA86_67m(((Number) row[10]).longValue());
            dto.setClassA65m(((Number) row[11]).longValue());
            dto.setClassA52m(((Number) row[12]).longValue());
            dto.setClassA26m(((Number) row[13]).longValue());
            dto.setClassA25m(((Number) row[14]).longValue());
            dto.setClassA24m(((Number) row[15]).longValue());
            dto.setClassA18m(((Number) row[16]).longValue());
            dto.setClassA17m(((Number) row[17]).longValue());
            dto.setClassA16m(((Number) row[18]).longValue());
            dto.setClassA13m(((Number) row[19]).longValue());
            dto.setClassA12m(((Number) row[20]).longValue());
            dto.setClassA11m(((Number) row[21]).longValue());
            dto.setClassA10m(((Number) row[22]).longValue());

            // New aggregates
            dto.setClassAPlus01Total(((Number) row[23]).longValue());
            // Round total accepted meters to 2 decimal places for proper display
            dto.setTotalAcceptedMeters(roundToTwoDecimalPlaces((BigDecimal) row[24]));
            // totalAcceptedPieces = sum of all classAxxm + classAPlus01Total
            long sumA = dto.getClassA130m()+dto.getClassA117m()+dto.getClassA104m()+dto.getClassA86_67m()+dto.getClassA65m()+dto.getClassA52m()+dto.getClassA26m()+dto.getClassA25m()+dto.getClassA24m()+dto.getClassA18m()+dto.getClassA17m()+dto.getClassA16m()+dto.getClassA13m()+dto.getClassA12m()+dto.getClassA11m()+dto.getClassA10m();
            dto.setTotalAcceptedPieces(sumA + dto.getClassAPlus01Total());

            // Tonnes calculation using a simple kg/m mapping per railSection
            BigDecimal kgPerMeter = getKgPerMeter(dto.getRailSection());
            if (dto.getTotalAcceptedMeters() != null && kgPerMeter != null) {
                // Round tonnes to 2 decimal places for proper display
                BigDecimal tonnes = dto.getTotalAcceptedMeters().multiply(kgPerMeter).divide(new BigDecimal("1000"));
                dto.setTotalAcceptedTonnes(roundToTwoDecimalPlaces(tonnes));
            }

            // Map recycle data
            dto.setRecycleCutBar(((Number) row[25]).longValue());
            dto.setRecycleRefinish(((Number) row[26]).longValue());

            // Map rejection data
            dto.setRejection13m(row[27] != null ? ((Number) row[27]).longValue() : 0);
            dto.setRejection12m(row[28] != null ? ((Number) row[28]).longValue() : 0);
            dto.setRejection11m(row[29] != null ? ((Number) row[29]).longValue() : 0);
            dto.setRejection10m(row[30] != null ? ((Number) row[30]).longValue() : 0);
            dto.setRejectionComponentLength(row[31] != null ? ((Number) row[31]).longValue() : 0);

            // report.add(dto);
            return dto;
        }).collect(Collectors.toList());
    }

    // Basic kg/m mapping; extend as needed
    private BigDecimal getKgPerMeter(String railSection) {
        if (railSection == null) return null;
        switch (railSection.trim().toUpperCase()) {
            case "60E1":
                return new BigDecimal("60.21");
            case "60E1A1":
                return new BigDecimal("72.97");
            case "UIC60":
                return new BigDecimal("60.34");
            case "136RE":
                return new BigDecimal("67.364");
            case "52E1":
                return new BigDecimal("51.51");
            case "IRS52":
                return new BigDecimal("51.89");
            default:
                return null; // unknown section; caller will skip tonnes
        }
    }

    /**
     * Round BigDecimal to 2 decimal places for display
     * Example: 4.12999 -> 4.13
     */
    private BigDecimal roundToTwoDecimalPlaces(BigDecimal value) {
        if (value == null) return null;
        return value.setScale(2, RoundingMode.HALF_UP);
    }

    @Override
    public List<AcptDataReportDto> getAcptDataReport(ReportReqDto req) {
        LocalDateTime startDate = RailMillCommonUtils.convertStringToDateObject(req.getStartDate()).atStartOfDay();
        LocalDateTime endDate = RailMillCommonUtils.convertStringToDateObject(req.getEndDate()).atTime(LocalTime.MAX);

        List<Object[]> resObjList = vmr.getAcptDataReport(startDate, endDate);

        return resObjList.stream().map(row -> {
            AcptDataReportDto dto = new AcptDataReportDto();

            dto.setRailId((String) row[0]); // Map Rail_ID
            dto.setHeatNumber((String) row[1]); // Map Heat_Number
            dto.setDateShift((String) row[2]); // Map Date_Shift
            dto.setRailGrade((String) row[3]); // Map Rail_Grade
            dto.setRailSection((String) row[4]); // Map Rail_Section
            dto.setInspectionWorkspot((String) row[5]); // Map Inspection_Workspot
            // Round length values to 2 decimal places for proper display
            dto.setStandardOfferedLength(roundToTwoDecimalPlaces((BigDecimal) row[6])); // Map Standard_Offered_Length
            dto.setActualOfferedLength(roundToTwoDecimalPlaces((BigDecimal) row[7])); // Map Actual_Offered_Length
            dto.setAcceptanceInClassA((String) row[8]); // Map Acceptance_in_Class_A
            dto.setAcceptanceInClassAPlus01((String) row[9]); // Map Acceptance_in_Class_A_Plus_0_1
            dto.setAcceptanceInClassB((String) row[10]); // Map Acceptance_in_Class_B
            dto.setAcceptedIds((String) row[11]); // Map Accepted_IDs
            dto.setRejectedLength((String) row[12]); // Map Rejected_Length
            dto.setReasonForDefect((String) row[13]); // Map Reason_for_Defect

            return dto;
        }).collect(Collectors.toList());
    }

    @Override
    public List<ViDefectRecordDto> getViDefectAnalysisRecord(ReportReqDto req){
        List<Object[]> objResList = vmr.findViDefectAnalysisRecord(
            RailMillCommonUtils.convertStringToDateObject(req.getStartDate()),
            RailMillCommonUtils.convertStringToDateObject(req.getEndDate())
        );

        return objResList.stream().map(obj -> {
            ViDefectRecordDto dto = new ViDefectRecordDto();
            dto.setDate(RailMillCommonUtils.convertDateToString(((java.sql.Date) obj[0]).toLocalDate()));
            dto.setInspectionWorkspot((Integer) obj[1]);
            dto.setRailSection((String) obj[2]);
            dto.setRailGrade((String) obj[3]);
            dto.setStdOfferedLengthForInspection((BigDecimal) obj[4]);
            dto.setNoOfRailsOfferedForInspection((Long) obj[5]);
            dto.setNmi((BigDecimal) obj[6]);
            dto.setMdf((BigDecimal) obj[7]);
            dto.setMdm((BigDecimal) obj[8]);
            dto.setHs((BigDecimal) obj[9]);
            dto.setLs((BigDecimal) obj[10]);
            dto.setHh((BigDecimal) obj[11]);
            dto.setLh((BigDecimal) obj[12]);
            dto.setNf((BigDecimal) obj[13]);
            dto.setWf((BigDecimal) obj[14]);
            dto.setOht((BigDecimal) obj[15]);
            dto.setUht((BigDecimal) obj[16]);
            dto.setTnw((BigDecimal) obj[17]);
            dto.setTkw((BigDecimal) obj[18]);
            dto.setLap((BigDecimal) obj[19]);
            dto.setGm((BigDecimal) obj[20]);
            dto.setBr((BigDecimal) obj[21]);
            dto.setKk((BigDecimal) obj[22]);
            dto.setTw((BigDecimal) obj[23]);
            dto.setCRc((BigDecimal) obj[24]);
            dto.setNs((BigDecimal) obj[25]);
            dto.setOl((BigDecimal) obj[26]);
            dto.setSl((BigDecimal) obj[27]);
            dto.setOs((BigDecimal) obj[28]);
            dto.setNhn((BigDecimal) obj[29]);
            dto.setAsy((BigDecimal) obj[30]);
            dto.setBe((BigDecimal) obj[31]);
            dto.setUs((BigDecimal) obj[32]);
            dto.setDs((BigDecimal) obj[33]);
            dto.setUt((BigDecimal) obj[34]);
            dto.setTfPositive((BigDecimal) obj[35]);
            dto.setTfNegative((BigDecimal) obj[36]);
            dto.setHfPositive((BigDecimal) obj[37]);
            dto.setHfNegative((BigDecimal) obj[38]);
            dto.setCp((BigDecimal) obj[39]);
            dto.setFbc((BigDecimal) obj[40]);
            dto.setFbcx((BigDecimal) obj[41]);
            dto.setHr((BigDecimal) obj[42]);
            dto.setOthers((BigDecimal) obj[43]);
            dto.setTotal((Long) obj[44]);
            dto.setDateForSteelMillFinishUT(RailMillCommonUtils.convertDateToString(((java.sql.Date) obj[45]).toLocalDate()));
            dto.setMill((String) obj[46]);
            dto.setFinish((String) obj[47]);
            dto.setUtStandardOfferedLength((BigDecimal) obj[48]);
            return dto;
        }).collect(Collectors.toList());
    }

    @Override
    public Map<String, BigDecimal> getActOffLengthByRailId(String railId){
        // If railId contains a hyphen, it's a sub rail ID - return accepted length
        if (railId.contains("-")) {
            // This is a sub rail ID, fetch the accepted length from sub rail ID table
            ViSubRailIdEntity subRailEntity = vsrir.findBySubRailId(railId);

            if (subRailEntity == null) {
                throw new SmsResourceNotFoundException(
                    new SmsErrorDetails(
                        RailMillAppConstant.ERROR_CODE_RESOURCE,
                        RailMillAppConstant.ERROR_TYPE_CODE_RESOURCE,
                        RailMillAppConstant.ERROR_TYPE_RESOURCE, "No record found for the provided sub rail id.")
                );
            }

            Map<String, BigDecimal> res = new HashMap<>();
            res.put("length", subRailEntity.getAcceptedLength());
            return res;
        } else {
            // This is a parent rail ID, return accepted length from sub rail ID table
            // For parent rail IDs, we need to find the accepted length from the sub rail ID table
            List<ViSubRailIdEntity> subRailEntities = vsrir.findByRailId(railId);

            if (subRailEntities == null || subRailEntities.isEmpty()) {
                throw new SmsResourceNotFoundException(
                    new SmsErrorDetails(
                        RailMillAppConstant.ERROR_CODE_RESOURCE,
                        RailMillAppConstant.ERROR_TYPE_CODE_RESOURCE,
                        RailMillAppConstant.ERROR_TYPE_RESOURCE, "No sub rail ID found for the provided parent rail id.")
                );
            }

            // For parent rail ID, return the accepted length from the first sub rail entry
            // (all entries for the same parent rail should have the same accepted length)
            Map<String, BigDecimal> res = new HashMap<>();
            res.put("length", subRailEntities.get(0).getAcceptedLength());
            return res;
        }
    }
    @Override
    public List<ViShiftSummaryResDto> getShiftSummary(String dutyId){
        List<ViMasterEntity> vmeList = vmr.findByDutyId(dutyId);

        // Get duty details for additional information
        Optional<ViDutyEntity> dutyOpt = vdr.findByDutyId(dutyId);
        ViDutyEntity duty = dutyOpt.orElse(null);

        return vmeList.stream().map(vme -> {
            ViShiftSummaryResDto dto = new ViShiftSummaryResDto();

            // Basic master data
            dto.setRailId(vme.getRailId());
            dto.setHeatNo(vme.getHeatNo());
            dto.setDutyId(vme.getDutyId());
            dto.setDate(vme.getDate());
            dto.setShift(vme.getShift());
            dto.setSerialNo(vme.getSerialNo());
            dto.setHeatStatus(vme.getHeatStatus());
            dto.setActualOfferedLength(vme.getActualOfferedLength());
            dto.setUtFeedback(vme.getUtFeedback());
            dto.setDimFeedback(vme.getDimFeedback());
            dto.setVisualFeedback(vme.getVisualFeedback());
            dto.setGaugingEndStraightness(vme.getGaugingEndStraightness());
            dto.setRej13(vme.getRej13());
            dto.setRej12(vme.getRej12());
            dto.setRej11(vme.getRej11());
            dto.setRej10(vme.getRej10());
            dto.setRejCompLength(vme.getRejCompLength());
            dto.setRemarks(vme.getRemarks());

            // Duty-related data
            if (duty != null) {
                dto.setLineNumber(duty.getLineNo().toString());
                dto.setRailGrade(duty.getRailGrade());
                dto.setRailSection(duty.getRailSection());
                dto.setMill(duty.getMill());
                dto.setStandardOfferedLength(duty.getStdOffLength());
            }

            // Get acceptance data
            List<ViAcceptDataEntity> acceptDataList = vadr.findByRailId(vme.getRailId());
            List<ViAcceptDtlsDto> acceptDtoList = acceptDataList.stream().map(vade -> {
                ViAcceptDtlsDto vadd = new ViAcceptDtlsDto();
                vadd.setAcceptedLength(vade.getAcceptedLength());
                vadd.setAcceptedNo(vade.getAcceptedNo());
                vadd.setRailClass(vade.getRailClass());
                return vadd;
            }).collect(Collectors.toList());
            dto.setAcptDataList(acceptDtoList);

            // Get defect data
            List<ViDefectDataEntity> defectDataList = vddr.findByRailId(vme.getRailId());
            List<ViDefectDtlsDto> defectDtoList = defectDataList.stream().map(vdde -> {
                ViDefectDtlsDto vddd = new ViDefectDtlsDto();
                vddd.setDefectCategory(vdde.getDefectCategory());
                vddd.setDefectType(vdde.getDefectType());
                vddd.setLocation(vdde.getLocation());
                vddd.setPosition(vdde.getPosition());
                return vddd;
            }).collect(Collectors.toList());
            dto.setDefectDataList(defectDtoList);

            return dto;
        }).collect(Collectors.toList());
    }

    @Override
    public List<ViShiftSummaryResDto> getShiftSummaryByLine(String dutyId, String lineNumber) {
        System.out.println("getShiftSummaryByLine called with dutyId: " + dutyId + ", lineNumber: " + lineNumber);

        // If no line number specified, return all data
        if (lineNumber == null || lineNumber.isEmpty()) {
            System.out.println("No line number specified, returning all data");
            return getShiftSummary(dutyId);
        }

        // Get duty details to check if it matches the requested line number
        Optional<ViDutyEntity> dutyOpt = vdr.findByDutyId(dutyId);
        if (!dutyOpt.isPresent()) {
            System.out.println("Duty not found for dutyId: " + dutyId);
            return new ArrayList<>();
        }

        ViDutyEntity duty = dutyOpt.get();
        System.out.println("Duty found - Line No: " + duty.getLineNo() + ", Requested Line: " + lineNumber);

        // Check if the duty's line number matches the requested line number
        if (!duty.getLineNo().toString().equals(lineNumber)) {
            System.out.println("Line numbers don't match. Duty line: " + duty.getLineNo() + ", Requested: " + lineNumber);
            return new ArrayList<>(); // Return empty list if line numbers don't match
        }

        System.out.println("Line numbers match, returning all data for this duty");
        // If line numbers match, return all data for this duty
        return getShiftSummary(dutyId);
    }

    @Override
    public List<ViShiftSummaryResDto> getShiftSummaryByDateAndShift(ViShiftSummaryReqDto req) {
        LocalDate date = RailMillCommonUtils.convertStringToDateObject(req.getDate());

        System.out.println("=== SERVICE DEBUG ===");
        System.out.println("Converted date: " + date);
        System.out.println("Shift: " + req.getShift());
        System.out.println("Line number: " + req.getLineNumber());
        System.out.println("Line number is null: " + (req.getLineNumber() == null));
        System.out.println("Mill: " + req.getMill());
        System.out.println("Mill is null: " + (req.getMill() == null));

        List<ViMasterEntity> vmeList;

        // Check if both line number and mill filters are specified
        if (req.getLineNumber() != null && req.getMill() != null) {
            System.out.println("Using line and mill-specific query for line: " + req.getLineNumber() + ", mill: " + req.getMill());
            vmeList = vmr.findByDateShiftLineAndMill(date, req.getShift(), req.getLineNumber(), req.getMill());
        } else if (req.getLineNumber() != null) {
            System.out.println("Using line-specific query for line: " + req.getLineNumber());
            // Filter by specific line number
            vmeList = vmr.findByDateShiftAndLine(date, req.getShift(), req.getLineNumber());
        } else if (req.getMill() != null) {
            System.out.println("Using mill-specific query for mill: " + req.getMill());
            // Filter by specific mill
            vmeList = vmr.findByDateShiftAndMill(date, req.getShift(), req.getMill());
        } else {
            System.out.println("Using all-lines and all-mills query");
            // Get all lines and mills for the date and shift
            vmeList = vmr.findByDateAndShift(date, req.getShift());
        }

        System.out.println("Query returned " + vmeList.size() + " records");
        System.out.println("=== END SERVICE DEBUG ===");

        // Convert to DTOs with duty information - reuse existing logic from getShiftSummary
        return vmeList.stream().map(vme -> {
            ViShiftSummaryResDto dto = new ViShiftSummaryResDto();

            // Set basic VI master data
            dto.setHeatNo(vme.getHeatNo());
            dto.setRailId(vme.getRailId());
            dto.setDutyId(vme.getDutyId());
            dto.setDate(vme.getDate());
            dto.setShift(vme.getShift());
            dto.setSerialNo(vme.getSerialNo());
            dto.setHeatStatus(vme.getHeatStatus());
            dto.setActualOfferedLength(vme.getActualOfferedLength());
            dto.setUtFeedback(vme.getUtFeedback());
            dto.setDimFeedback(vme.getDimFeedback());
            dto.setVisualFeedback(vme.getVisualFeedback());
            dto.setGaugingEndStraightness(vme.getGaugingEndStraightness());
            dto.setRej13(vme.getRej13());
            dto.setRej12(vme.getRej12());
            dto.setRej11(vme.getRej11());
            dto.setRej10(vme.getRej10());
            dto.setRejCompLength(vme.getRejCompLength());
            dto.setRemarks(vme.getRemarks());

            // Get duty details for additional information
            Optional<ViDutyEntity> dutyOpt = vdr.findByDutyId(vme.getDutyId());
            if (dutyOpt.isPresent()) {
                ViDutyEntity duty = dutyOpt.get();
                dto.setLineNumber(duty.getLineNo().toString());
                dto.setRailGrade(duty.getRailGrade());
                dto.setRailSection(duty.getRailSection());
                dto.setMill(duty.getMill());
                dto.setStandardOfferedLength(duty.getStdOffLength());
            }

            // Get acceptance data
            List<ViAcceptDataEntity> acceptDataList = vadr.findByRailId(vme.getRailId());
            List<ViAcceptDtlsDto> acptDtoList = acceptDataList.stream().map(acceptData -> {
                ViAcceptDtlsDto acptDto = new ViAcceptDtlsDto();
                acptDto.setRailClass(acceptData.getRailClass());
                acptDto.setAcceptedLength(acceptData.getAcceptedLength());
                acptDto.setAcceptedNo(acceptData.getAcceptedNo());
                return acptDto;
            }).collect(Collectors.toList());
            dto.setAcptDataList(acptDtoList);

            // Get defect data
            List<ViDefectDataEntity> defectDataList = vddr.findByRailId(vme.getRailId());
            List<ViDefectDtlsDto> defectDtoList = defectDataList.stream().map(defectData -> {
                ViDefectDtlsDto defectDto = new ViDefectDtlsDto();
                defectDto.setDefectCategory(defectData.getDefectCategory());
                defectDto.setDefectType(defectData.getDefectType());
                defectDto.setLocation(defectData.getLocation());
                defectDto.setPosition(defectData.getPosition());
                return defectDto;
            }).collect(Collectors.toList());
            dto.setDefectDataList(defectDtoList);

            return dto;
        }).collect(Collectors.toList());
    }

    @Override
    public ViDtlsDto getDtlByRailId(String railId){
        Optional<ViMasterEntity> vmeOpt = vmr.findById(railId);
        if(!vmeOpt.isPresent()){
            throw new SmsResourceNotFoundException(
                new SmsErrorDetails(
                    RailMillAppConstant.ERROR_CODE_RESOURCE,
                    RailMillAppConstant.ERROR_TYPE_CODE_RESOURCE,
                    RailMillAppConstant.ERROR_TYPE_RESOURCE, "No record found for the provided rail id.")
            );
        }

        ViMasterEntity req = vmeOpt.get();
        ViDtlsDto vme = new ViDtlsDto();
        vme.setDutyId(req.getDutyId());
        // vme.setViId(new ViId(req.getHeatNo(), req.getRailId()));
        vme.setRailId(req.getRailId());
        vme.setHeatNo(req.getHeatNo());
        vme.setDate(RailMillCommonUtils.convertDateToString(req.getDate()));
        vme.setShift(req.getShift());
        vme.setSerialNo(req.getSerialNo());
        vme.setHeatStatus(req.getHeatStatus());
        vme.setActualOfferedLength(req.getActualOfferedLength());
        vme.setUtFeedback(req.getUtFeedback());
        vme.setDimFeedback(req.getDimFeedback());
        vme.setVisualFeedback(req.getVisualFeedback());
        vme.setGaugingEndStraightness(req.getGaugingEndStraightness());
        vme.setRej13(req.getRej13());
        vme.setRej12(req.getRej12());
        vme.setRej11(req.getRej11());
        vme.setRej10(req.getRej10());
        vme.setRejCompLength(req.getRejCompLength());
        vme.setRemarks(req.getRemarks());

        List<ViAcceptDataEntity> vadeList = vadr.findByRailId(railId);

        List<ViAcceptDtlsDto> vadeDtoList = vadeList.stream().map(vade -> {
            ViAcceptDtlsDto vadd = new ViAcceptDtlsDto();
            vadd.setAcceptedLength(vade.getAcceptedLength());
            vadd.setAcceptedNo(vade.getAcceptedNo());
            vadd.setRailClass(vade.getRailClass());
            return vadd;
        }).collect(Collectors.toList());


        List<ViDefectDataEntity>  vdde = vddr.findByRailId(railId);
        List<ViDefectDtlsDto> vddDtoList = vdde.stream().map(vdd -> {
            ViDefectDtlsDto vddDto = new ViDefectDtlsDto();
            vddDto.setDefectCategory(vdd.getDefectCategory());
            vddDto.setDefectType(vdd.getDefectType());
            vddDto.setLocation(vdd.getLocation());
            vddDto.setPosition(vdd.getPosition());
            return vddDto;
        }).collect(Collectors.toList());

        vme.setAcptDataList(vadeDtoList);
        vme.setDefectDataList(vddDtoList);

        return vme;
    }

    @Override
    public List<ViSubRailIdEntity> getSubRailIdsByRailId(String railId) {
        return vsrir.findByRailId(railId);
    }

    @Override
    public List<ViSubRailIdWithLengthDto> getSubRailIdsWithLengthByRailId(String railId) {
        List<ViSubRailIdEntity> entities = vsrir.findByRailId(railId);

        return entities.stream().map(entity -> {
            ViSubRailIdWithLengthDto dto = new ViSubRailIdWithLengthDto();
            dto.setRailId(entity.getRailId());
            dto.setSubRailId(entity.getSubRailId());
            dto.setAcceptedLength(entity.getAcceptedLength());
            return dto;
        }).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void deleteViDtls(String railId) {
        // Check if the rail ID exists
        Optional<ViMasterEntity> vmeOpt = vmr.findById(railId);
        if (vmeOpt.isEmpty()) {
            throw new SmsResourceNotFoundException(
                new SmsErrorDetails(
                    RailMillAppConstant.ERROR_CODE_RESOURCE,
                    RailMillAppConstant.ERROR_TYPE_CODE_RESOURCE,
                    RailMillAppConstant.ERROR_TYPE_RESOURCE,
                    "No Visual Inspection data found for the provided rail ID: " + railId
                )
            );
        }

        // Delete related data in the correct order (child entities first)

        // 1. Delete sub rail IDs
        List<ViSubRailIdEntity> subRailIds = vsrir.findByRailId(railId);
        vsrir.deleteAll(subRailIds);

        // 2. Delete defect data
        List<ViDefectDataEntity> defectData = vddr.findByRailId(railId);
        vddr.deleteAll(defectData);

        // 3. Delete accept data
        List<ViAcceptDataEntity> acceptData = vadr.findByRailId(railId);
        vadr.deleteAll(acceptData);

        // 4. Finally delete the master entity
        vmr.deleteById(railId);
    }

    @Override
    public String getHeatNumberByRailId(String railId) {
        try {
            // First try to get heat number from heatnumber_inspection table
            String heatNumber = heatnumberInspectionRepository.findHeatnumberByRailId(railId);

            if (heatNumber != null && !heatNumber.trim().isEmpty()) {
                return heatNumber.trim();
            }

            // If not found, return null (engineer will need to enter manually)
            return null;

        } catch (Exception e) {
            // Log error and return null so engineer can enter manually
            System.err.println("Error fetching heat number for rail ID " + railId + ": " + e.getMessage());
            return null;
        }
    }

    @Override
    public ViAiComparisonDto compareViWithAiData(String railId) {
        try {
            ViAiComparisonDto comparison = new ViAiComparisonDto();
            comparison.setRailId(railId);

            // Get Visual Inspection data
            Optional<ViMasterEntity> viDataOpt = vmr.findById(railId);
            if (!viDataOpt.isPresent()) {
                comparison.setHasDiscrepancies(true);
                comparison.setComparisonSummary("Visual Inspection data not found for rail ID: " + railId);
                return comparison;
            }

            ViMasterEntity viData = viDataOpt.get();
            comparison.setHeatNumber(viData.getHeatNo());

            // Get AI data from dashboard service
            List<SurfaceInspectionResDto> aiSurfaceData = dashboardService.getSurfaceInspectionDtlByRailId(railId);
            List<DimensionalInspectionResDto> aiDimensionalData = dashboardService.getDimensionalInspectionDtlByRailId(railId);

            // Compare OCR (Heat Numbers)
            ViAiComparisonDto.OcrComparison ocrComparison = compareOcrData(viData, railId);
            comparison.setOcrComparison(ocrComparison);

            // Compare Surface Defect Detection
            ViAiComparisonDto.SurfaceDefectComparison surfaceComparison = compareSurfaceDefectData(viData, aiSurfaceData, railId);
            comparison.setSurfaceDefectComparison(surfaceComparison);

            // Compare Dimensional Variation Detection
            ViAiComparisonDto.DimensionalVariationComparison dimensionalComparison = compareDimensionalVariationData(viData, aiDimensionalData, railId);
            comparison.setDimensionalVariationComparison(dimensionalComparison);

            // Determine overall discrepancies
            boolean hasDiscrepancies = !ocrComparison.isMatches() ||
                                     !surfaceComparison.isMatches() ||
                                     !dimensionalComparison.isMatches();
            comparison.setHasDiscrepancies(hasDiscrepancies);

            // Generate summary
            String summary = generateComparisonSummary(ocrComparison, surfaceComparison, dimensionalComparison);
            comparison.setComparisonSummary(summary);

            return comparison;

        } catch (Exception e) {
            System.err.println("Error comparing VI with AI data for rail ID " + railId + ": " + e.getMessage());
            ViAiComparisonDto errorComparison = new ViAiComparisonDto();
            errorComparison.setRailId(railId);
            errorComparison.setHasDiscrepancies(true);
            errorComparison.setComparisonSummary("Error occurred during comparison: " + e.getMessage());
            return errorComparison;
        }
    }

    /**
     * Compare OCR data (Heat Numbers)
     * Rule: Compare heat number from Visual Inspection with heat number from heatnumber_inspection table
     */
    private ViAiComparisonDto.OcrComparison compareOcrData(ViMasterEntity viData, String railId) {
        ViAiComparisonDto.OcrComparison ocrComparison = new ViAiComparisonDto.OcrComparison();

        String viHeatNumber = viData.getHeatNo();
        String aiOcrHeatNumber = heatnumberInspectionRepository.findHeatnumberByRailId(railId);

        ocrComparison.setVisualInspectionHeatNumber(viHeatNumber);
        ocrComparison.setAiOcrHeatNumber(aiOcrHeatNumber);

        // Compare heat numbers (case-insensitive, trimmed)
        boolean matches = false;
        if (viHeatNumber != null && aiOcrHeatNumber != null) {
            matches = viHeatNumber.trim().equalsIgnoreCase(aiOcrHeatNumber.trim());
        }

        ocrComparison.setMatches(matches);

        if (!matches) {
            ocrComparison.setDiscrepancyDetails(
                String.format("Heat number mismatch: VI='%s', AI OCR='%s'",
                    viHeatNumber, aiOcrHeatNumber)
            );
        } else {
            ocrComparison.setDiscrepancyDetails("Heat numbers match");
        }

        return ocrComparison;
    }

    /**
     * Compare Surface Defect Detection data
     * Rule: Compare visual feedback with AI surface inspection data
     */
    private ViAiComparisonDto.SurfaceDefectComparison compareSurfaceDefectData(ViMasterEntity viData, List<SurfaceInspectionResDto> aiSurfaceData, String railId) {
        ViAiComparisonDto.SurfaceDefectComparison surfaceComparison = new ViAiComparisonDto.SurfaceDefectComparison();

        String viSurfaceFeedback = viData.getVisualFeedback();
        String aiSurfaceDetection = aiSurfaceData.stream()
            .map(ai -> ai.getLabelName() + " - " + ai.getCameraName() + " at " + (ai.getDistance() / 100.0) + " m")
            .collect(Collectors.joining(", "));

        surfaceComparison.setVisualInspectionFeedback(viSurfaceFeedback);
        surfaceComparison.setAiSurfaceDetection(aiSurfaceDetection);

        // Simple comparison - if both are empty/null or both have content, consider as match
        // For detailed comparison, you would implement specific logic based on defect types, locations, etc.
        boolean matches = (viSurfaceFeedback == null || viSurfaceFeedback.trim().isEmpty()) ==
                         (aiSurfaceDetection == null || aiSurfaceDetection.trim().isEmpty());

        surfaceComparison.setMatches(matches);

        if (!matches) {
            surfaceComparison.setDiscrepancyDetails(
                String.format("Surface defect mismatch: VI='%s', AI='%s'",
                    viSurfaceFeedback, aiSurfaceDetection)
            );
        } else {
            surfaceComparison.setDiscrepancyDetails("Surface defect detection matches");
        }

        return surfaceComparison;
    }

    /**
     * Compare Dimensional Variation Detection data with tolerance rules
     * Rules:
     * - Defect Category must be "dimension"
     * - Defect Type must match exactly
     * - Location/Distance: ±2 meter tolerance
     * - Camera Name must match
     * - Position: BSB = Head (mapping rule)
     */
    private ViAiComparisonDto.DimensionalVariationComparison compareDimensionalVariationData(ViMasterEntity viData, List<DimensionalInspectionResDto> aiDimensionalData, String railId) {
        ViAiComparisonDto.DimensionalVariationComparison dimensionalComparison = new ViAiComparisonDto.DimensionalVariationComparison();

        String viDimFeedback = viData.getDimFeedback();
        String aiDimensionalDetection = aiDimensionalData.stream()
            .map(ai -> ai.getDefectType() + " at " + ai.getDistanceFromHead() + " m")
            .collect(Collectors.joining(", "));

        dimensionalComparison.setVisualInspectionDimFeedback(viDimFeedback);
        dimensionalComparison.setAiDimensionalDetection(aiDimensionalDetection);

        // Get VI defect data for detailed comparison
        List<ViDefectDataEntity> viDefectData = vddr.findByRailId(railId);

        boolean matches = compareDimensionalDefectsWithTolerance(viDefectData, aiDimensionalData);
        dimensionalComparison.setMatches(matches);

        if (!matches) {
            dimensionalComparison.setDiscrepancyDetails(
                String.format("Dimensional variation mismatch: VI='%s', AI='%s'",
                    viDimFeedback, aiDimensionalDetection)
            );
        } else {
            dimensionalComparison.setDiscrepancyDetails("Dimensional variation detection matches within tolerance");
        }

        return dimensionalComparison;
    }

    /**
     * Compare dimensional defects with ±2 meter tolerance
     */
    private boolean compareDimensionalDefectsWithTolerance(List<ViDefectDataEntity> viDefectData, List<DimensionalInspectionResDto> aiDimensionalData) {
        // Filter VI defects for dimensional category only
        List<ViDefectDataEntity> viDimensionalDefects = viDefectData.stream()
            .filter(defect -> "dimension".equalsIgnoreCase(defect.getDefectCategory()))
            .collect(Collectors.toList());

        // If no dimensional defects in VI but AI found some, it's a mismatch
        if (viDimensionalDefects.isEmpty() && !aiDimensionalData.isEmpty()) {
            return false;
        }

        // If VI has dimensional defects but AI found none, it's a mismatch
        if (!viDimensionalDefects.isEmpty() && aiDimensionalData.isEmpty()) {
            return false;
        }

        // If both are empty, it's a match
        if (viDimensionalDefects.isEmpty() && aiDimensionalData.isEmpty()) {
            return true;
        }

        // Compare each VI defect with AI defects using tolerance rules
        for (ViDefectDataEntity viDefect : viDimensionalDefects) {
            boolean foundMatch = false;

            for (DimensionalInspectionResDto aiDefect : aiDimensionalData) {
                if (isDefectMatch(viDefect, aiDefect)) {
                    foundMatch = true;
                    break;
                }
            }

            if (!foundMatch) {
                return false; // VI defect not found in AI data
            }
        }

        return true; // All VI defects have matching AI defects
    }

    /**
     * Check if a VI defect matches an AI defect based on tolerance rules
     * Camera Names: HT, BS, NBS, FB
     * Positions: Head, Foot, WNBS, WBS
     * Position Mapping: BSB = Head (as per user requirement)
     */
    private boolean isDefectMatch(ViDefectDataEntity viDefect, DimensionalInspectionResDto aiDefect) {
        // Rule 1: Defect Type must match exactly
        if (!viDefect.getDefectType().equalsIgnoreCase(aiDefect.getDefectType())) {
            return false;
        }

        // Rule 2: Distance tolerance ±2 meters
        // Parse location from VI data if it contains distance information
        Double viDistance = parseDistanceFromLocation(viDefect.getLocation());
        if (viDistance != null && aiDefect.getDistanceFromHead() != null) {
            double aiDistance = aiDefect.getDistanceFromHead().doubleValue();
            if (Math.abs(viDistance - aiDistance) > 2.0) {
                return false; // Distance difference exceeds ±2 meter tolerance
            }
        }

        // Rule 3: Camera Name = Position validation
        // Camera names directly correspond to positions in surface inspection
        if (aiDefect.getCameraName() != null && viDefect.getPosition() != null) {
            String aiCameraName = aiDefect.getCameraName().trim().toUpperCase();
            String viPosition = viDefect.getPosition().trim().toUpperCase();

            if (!isValidCameraName(aiCameraName)) {
                return false; // Invalid camera name
            }

            if (!isValidPosition(viPosition)) {
                return false; // Invalid position
            }

            // Check if camera name matches position (Camera Name = Position principle)
            if (!isCameraNamePositionMatch(aiCameraName, viPosition)) {
                return false; // Camera name doesn't match position
            }
        }

        return true; // All available rules passed
    }

    /**
     * Parse distance from location string (e.g., "8m", "10.5 meters", "at 5m")
     */
    private Double parseDistanceFromLocation(String location) {
        if (location == null || location.trim().isEmpty()) {
            return null;
        }

        try {
            // Remove common words and extract numeric value
            String cleaned = location.toLowerCase()
                .replaceAll("[^0-9.]", " ")
                .trim();

            if (!cleaned.isEmpty()) {
                String[] parts = cleaned.split("\\s+");
                for (String part : parts) {
                    if (!part.isEmpty()) {
                        return Double.parseDouble(part);
                    }
                }
            }
        } catch (NumberFormatException e) {
            // Location doesn't contain parseable distance
        }

        return null;
    }

    /**
     * Check if camera name is valid
     * Valid camera names: HT, BS, NBS, FB, BST, BSB, NBST, NBSB
     */
    private boolean isValidCameraName(String cameraName) {
        if (cameraName == null) return false;
        String name = cameraName.trim().toUpperCase();
        return "HT".equals(name) || "BS".equals(name) || "NBS".equals(name) || "FB".equals(name) ||
               "BST".equals(name) || "BSB".equals(name) || "NBST".equals(name) || "NBSB".equals(name);
    }

    /**
     * Check if camera name matches position based on CAMERA NAME = POSITION principle
     * @param cameraName AI camera name (normalized to uppercase)
     * @param position Manual inspection position (normalized to uppercase)
     * @return true if camera name corresponds to position
     */
    private boolean isCameraNamePositionMatch(String cameraName, String position) {
        // Direct exact matches first
        if (cameraName.equals(position)) {
            return true;
        }

        // Camera Name = Position mappings
        switch (cameraName) {
            case "HT": // Head Top
                return "HEAD".equals(position) || "HT".equals(position);

            case "BST": // Base Side Top
            case "BSB": // Base Side Bottom
            case "BS":  // Base Side (generic)
                return "BASE".equals(position) || "BS".equals(position) ||
                       "BASE_SIDE".equals(position) || "WBS".equals(position) ||
                       "BST".equals(position) || "BSB".equals(position);

            case "FB": // Foot Bottom
                return "FOOT".equals(position) || "FB".equals(position);

            case "NBSB": // Non-Base Side Bottom
            case "NBST": // Non-Base Side Top
            case "NBS":  // Non-Base Side (generic)
                return "NON_BASE".equals(position) || "NBS".equals(position) ||
                       "NON_BASE_SIDE".equals(position) || "WNBS".equals(position) ||
                       "NBSB".equals(position) || "NBST".equals(position);

            default:
                // For any other camera names, try direct string matching
                return cameraName.equals(position);
        }
    }

    /**
     * Normalize position with special mapping rules
     * BSB = Head (as per user requirement)
     * Valid positions: Head, Foot, WNBS, WBS
     */
    private String normalizePosition(String position) {
        if (position == null) return null;

        String normalized = position.trim().toUpperCase();

        // Special mapping: BSB = Head
        if ("BSB".equals(normalized)) {
            return "HEAD";
        }

        return normalized;
    }

    /**
     * Check if position is valid
     * Valid positions: HEAD, FOOT, WNBS, WBS
     */
    private boolean isValidPosition(String position) {
        if (position == null) return false;
        String pos = position.trim().toUpperCase();
        return "HEAD".equals(pos) || "FOOT".equals(pos) || "WNBS".equals(pos) || "WBS".equals(pos);
    }

    /**
     * Generate comparison summary
     */
    private String generateComparisonSummary(ViAiComparisonDto.OcrComparison ocrComparison,
                                           ViAiComparisonDto.SurfaceDefectComparison surfaceComparison,
                                           ViAiComparisonDto.DimensionalVariationComparison dimensionalComparison) {
        StringBuilder summary = new StringBuilder();

        summary.append("Comparison Results: ");

        if (ocrComparison.isMatches() && surfaceComparison.isMatches() && dimensionalComparison.isMatches()) {
            summary.append("All systems match - No discrepancies found");
        } else {
            summary.append("Discrepancies found - ");

            if (!ocrComparison.isMatches()) {
                summary.append("OCR mismatch; ");
            }
            if (!surfaceComparison.isMatches()) {
                summary.append("Surface defect mismatch; ");
            }
            if (!dimensionalComparison.isMatches()) {
                summary.append("Dimensional variation mismatch; ");
            }
        }

        return summary.toString();
    }

    @Override
    public Map<String, Object> validateHeatNumberWithBloomStage(String railId, String heatNumber) {
        Map<String, Object> validationResult = new HashMap<>();

        try {
            // Check if heat number comes from heatnumber_inspection table
            String heatNumberFromInspection = heatnumberInspectionRepository.findHeatnumberByRailId(railId);
            boolean isFromInspectionTable = false;

            if (heatNumberFromInspection != null && !heatNumberFromInspection.trim().isEmpty()) {
                // Normalize both heat numbers for comparison
                String normalizedInspectionHeat = heatNumberFromInspection.trim().replaceFirst("^0+", "");
                String normalizedRequestHeat = heatNumber.trim().replaceFirst("^0+", "");
                isFromInspectionTable = normalizedInspectionHeat.equals(normalizedRequestHeat);
            }

            validationResult.put("railId", railId);
            validationResult.put("heatNumber", heatNumber);
            validationResult.put("heatNumberFromInspectionTable", heatNumberFromInspection);
            validationResult.put("isFromInspectionTable", isFromInspectionTable);

            // Perform bloom stage validation for all heat numbers
            try {
                ss.validateHeatBloomStage(heatNumber);
                validationResult.put("bloomStageValidation", "PASSED");
                validationResult.put("validationMessage", "Heat number has completed bloom cutting stage");

                if (isFromInspectionTable) {
                    validationResult.put("source", "HEATNUMBER_INSPECTION_TABLE");
                    validationResult.put("note", "Heat number from AI/OCR system validated for bloom stage");
                } else {
                    validationResult.put("source", "MANUAL_ENTRY");
                    validationResult.put("note", "Manually entered heat number validated for bloom stage");
                }

            } catch (Exception bloomValidationError) {
                validationResult.put("bloomStageValidation", "FAILED");
                validationResult.put("validationMessage", bloomValidationError.getMessage());
                validationResult.put("error", "Bloom stage validation failed");
            }

        } catch (Exception e) {
            validationResult.put("error", "Error during heat number validation: " + e.getMessage());
            validationResult.put("bloomStageValidation", "ERROR");
        }

        return validationResult;
    }

}
