package com.sarthi.Sms.service.Impl;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.sarthi.Sms.constant.RailMillAppConstant;
import com.sarthi.Sms.dto.sms.common.DutyStatusResDto;
import com.sarthi.Sms.dto.sms.common.EndDutyReqDto;
import com.sarthi.Sms.dto.sms.common.StartDutyReqDto;
import com.sarthi.Sms.dto.sms.common.StartDutyResDto;
import com.sarthi.Sms.dto.rolling.TestSampleAcptEntity;
import com.sarthi.Sms.dto.testing.CompletedTestDto;
import com.sarthi.Sms.dto.testing.HeatSheetResDto;
import com.sarthi.Sms.dto.testing.PendingTestSampleDtlResDto;
import com.sarthi.Sms.entity.rolling.RollingDutyEntity;
import com.sarthi.Sms.entity.testing.CompletedTest;
import com.sarthi.Sms.entity.testing.TestingDutyEntity;
import com.sarthi.Sms.exception.SmsErrorDetails;
import com.sarthi.Sms.exception.SmsInvalidArgumentException;
import com.sarthi.Sms.exception.SmsResourceNotFoundException;
import com.sarthi.Sms.repository.rolling.TestSampleAcptRepository;
import com.sarthi.Sms.repository.testing.CompletedTestRepository;
import com.sarthi.Sms.repository.testing.TestingDutyRepository;
import com.sarthi.Sms.service.DutySequenceService;
import com.sarthi.Sms.service.TestingService;
import com.sarthi.Sms.util.RailMillCommonUtils;
import com.sarthi.Sms.util.DutyEnum;
import jakarta.transaction.Transactional;

@Service
public class TestingServiceImpl implements TestingService{

    @Autowired
    DutySequenceService dss;

    @Autowired
    TestingDutyRepository tdr;

    @Autowired
    TestSampleAcptRepository tsar;

    @Autowired
    private CompletedTestRepository completedTestRepository;


    @Override
    @Transactional
    public StartDutyResDto startDuty(String ah, StartDutyReqDto req) {
        TestingDutyEntity tde = new TestingDutyEntity();
        String userId = RailMillCommonUtils.getUserIdFromAuthHeader(ah);

        // check if a duty is already in progress
        DutyStatusResDto dsrd = checkDutyStatus(ah);
        if (dsrd.getIsDutyInProgess()) {
            throw new SmsInvalidArgumentException(new SmsErrorDetails(RailMillAppConstant.USER_ALREADY_EXISTS,
                    RailMillAppConstant.USER_ALREADY_EXISTS, RailMillAppConstant.ERROR_TYPE_VALIDATION,
                    "Cannot start new duty for the user. Previous duty already in progress."));
        }

        LocalDate date = RailMillCommonUtils.convertStringToDateObject(req.getStartDate());

        String dutyId = dss.generateDutyId(DutyEnum.TESTING, date);

        tde.setDate(date);
        tde.setDutyId(dutyId);
        tde.setStartTime(LocalDateTime.now());
        tde.setUserId(userId);
        tde.setShift(req.getShift());

        tde = tdr.save(tde);

        StartDutyResDto sdrd = new StartDutyResDto();
        sdrd.setDate(RailMillCommonUtils.convertDateToString(tde.getDate()));
        sdrd.setDutyId(tde.getDutyId());
        sdrd.setStartTime(RailMillCommonUtils.extractTime(tde.getStartTime()));
        sdrd.setShift(tde.getShift());

        return sdrd;
    }

    @Override
    @Transactional
    public void endDuty(EndDutyReqDto req) {
        TestingDutyEntity tde = tdr.findByDutyId(req.getDutyId())
                .orElseThrow(() -> new SmsResourceNotFoundException(
                        new SmsErrorDetails(RailMillAppConstant.ERROR_CODE_RESOURCE,
                                RailMillAppConstant.ERROR_TYPE_CODE_RESOURCE,
                                RailMillAppConstant.ERROR_TYPE_RESOURCE,
                                "No duties found for the provided duty id.")));

        if (Objects.nonNull(tde.getEndTime())) {
            throw new SmsInvalidArgumentException(
                    new SmsErrorDetails(
                            RailMillAppConstant.ERROR_CODE_VALIDATION,
                            RailMillAppConstant.ERROR_TYPE_CODE_VALIDATION,
                            RailMillAppConstant.ERROR_TYPE_VALIDATION,
                            "Duty already ended."));
        }
        tde.setShiftRemarks(req.getShiftRemarks());
        tde.setEndTime(LocalDateTime.now());

        tdr.save(tde);
    }

    @Override
    public DutyStatusResDto checkDutyStatus(String ah) {
        String userId = RailMillCommonUtils.getUserIdFromAuthHeader(ah);
        String dutyId = tdr.checkDutyStatus(userId);
        DutyStatusResDto dsrd = new DutyStatusResDto();
        dsrd.setDutyId(dutyId);
        dsrd.setIsDutyInProgess(dutyId != null);

        return dsrd;
    }

     @Override
    public StartDutyResDto getOngoingDutyDtls(String ah) {
        String userId = RailMillCommonUtils.getUserIdFromAuthHeader(ah);
        Optional<TestingDutyEntity> tdeOpt = tdr.getOngoingDutyDtls(userId);

        StartDutyResDto sdrd = new StartDutyResDto();
        if (tdeOpt.isPresent()) {
            TestingDutyEntity tde = tdeOpt.get();
            sdrd.setDate(RailMillCommonUtils.convertDateToString(tde.getDate()));
            sdrd.setDutyId(tde.getDutyId());
            sdrd.setShift(tde.getShift());
            sdrd.setStartTime(RailMillCommonUtils.extractTime(tde.getStartTime()));
        }

        return sdrd;
    }

    @Override
    public List<PendingTestSampleDtlResDto> getPendingTestSampleDtls(){
        try {
            // Use the original query that gets ALL samples from ALL shifts
            // The cross-shift filtering will be done at the database level
            List<Object[]> objRes = tdr.findPendingTestSampleDtls();

            List<PendingTestSampleDtlResDto> allSamples = objRes.stream().map(obj -> {
                try {
                    PendingTestSampleDtlResDto ptsdrd = new PendingTestSampleDtlResDto();

                    // Safe casting with null checks
                    ptsdrd.setHeatNo(obj[0] != null ? (String) obj[0] : null);
                    ptsdrd.setStrand(obj[1] != null ? (Integer) obj[1] : null);
                    ptsdrd.setMill(obj[2] != null ? (String) obj[2] : null);
                    ptsdrd.setRailGrade(obj[3] != null ? (String) obj[3] : null);
                    ptsdrd.setSampleType(obj[4] != null ? (String) obj[4] : null);
                    ptsdrd.setSampleLot(obj[5] != null ? (String) obj[5] : null);
                    ptsdrd.setSampleId(obj[6] != null ? (String) obj[6] : null);
                    ptsdrd.setTestsMarked(obj[7] != null ? (String) obj[7] : null);
                    ptsdrd.setTestStatuses(obj[8] != null ? (String) obj[8] : null);



                    return ptsdrd;
                } catch (Exception e) {
                    System.err.println("Error processing row: " + e.getMessage());
                    System.err.println("Row data: " + java.util.Arrays.toString(obj));
                    e.printStackTrace();
                    throw new RuntimeException("Error processing pending test sample data", e);
                }
            }).collect(Collectors.toList());

            return allSamples;
        } catch (Exception e) {
            System.err.println("Error in getPendingTestSampleDtls: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to retrieve pending test samples", e);
        }
    }

    @Override
    public List<PendingTestSampleDtlResDto> getPendingTestSampleDtlsByUser(String authHeader) {
        String userId = RailMillCommonUtils.getUserIdFromAuthHeader(authHeader);

        // Get all pending test samples first
        List<PendingTestSampleDtlResDto> allPendingTests = getPendingTestSampleDtls();

        // Check if user has an ongoing duty
        Optional<TestingDutyEntity> ongoingDutyOpt = tdr.getOngoingDutyDtls(userId);

        if (ongoingDutyOpt.isPresent()) {
            TestingDutyEntity ongoingDuty = ongoingDutyOpt.get();
            String currentDutyId = ongoingDuty.getDutyId();

            // Use the new database query that handles duty-based filtering directly
            List<Object[]> rawResults = tdr.findPendingTestSampleDtlsWithDutyFilter(currentDutyId, currentDutyId);

            List<PendingTestSampleDtlResDto> filteredTests = rawResults.stream()
                .map(this::mapToPendingTestSampleDto)
                .collect(Collectors.toList());

            return filteredTests;
        } else {
            return allPendingTests;
        }
    }

    private PendingTestSampleDtlResDto mapToPendingTestSampleDto(Object[] obj) {
        try {
            PendingTestSampleDtlResDto ptsdrd = new PendingTestSampleDtlResDto();

            // Safe casting with null checks
            ptsdrd.setHeatNo(obj[0] != null ? (String) obj[0] : null);
            ptsdrd.setStrand(obj[1] != null ? (Integer) obj[1] : null);
            ptsdrd.setMill(obj[2] != null ? (String) obj[2] : null);
            ptsdrd.setRailGrade(obj[3] != null ? (String) obj[3] : null);
            ptsdrd.setSampleType(obj[4] != null ? (String) obj[4] : null);
            ptsdrd.setSampleLot(obj[5] != null ? (String) obj[5] : null);
            ptsdrd.setSampleId(obj[6] != null ? (String) obj[6] : null);
            ptsdrd.setTestsMarked(obj[7] != null ? (String) obj[7] : null);
            ptsdrd.setTestStatuses(obj[8] != null ? (String) obj[8] : null);

            return ptsdrd;
        } catch (Exception e) {
            System.err.println("Error processing row: " + e.getMessage());
            System.err.println("Row data: " + java.util.Arrays.toString(obj));
            e.printStackTrace();
            throw new RuntimeException("Error processing pending test sample data", e);
        }
    }

    private String normalizeTestType(String testName) {
        // Normalize test names to match database test_type values
        String normalized = testName.toUpperCase().trim();

        // Handle FWT variations
        if (normalized.startsWith("FWT")) {
            return "FWT";
        }

        // Handle MICRO variations
        if (normalized.startsWith("MIC")) {
            return "MICRO";
        }

        // Handle CHEMICAL variations
        if (normalized.startsWith("CHEMIC")) {
            return "CHEMICAL";
        }

        // Handle MECHANICAL variations
        if (normalized.startsWith("MECHANIC")) {
            return "MECHANICAL";
        }

        // Handle TENSILE FOOT variations
        if (normalized.contains("TENSILE FO")) {
            return "TENSILE_FOOT";
        }

        // Handle TENSILE variations (but not TENSILE FOOT)
        if (normalized.startsWith("TENSI") && !normalized.contains("FOOT")) {
            return "TENSILE";
        }

        // Handle DECARB variations
        if (normalized.startsWith("DECA")) {
            return "DECARB";
        }

        // Handle other test types as-is
        return normalized;
    }

    private void setFieldValue(Object target, String fieldName, Object value) {
        try {
            java.lang.reflect.Field field = target.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(target, value);
        } catch (Exception e) {
            // Field not found or access error - silently ignore
        }
    }

    private Object getFieldValue(Object source, String... fieldNames) {
        for (String fieldName : fieldNames) {
            try {
                String methodName = "get" + fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1);
                java.lang.reflect.Method method = source.getClass().getMethod(methodName);
                Object value = method.invoke(source);
                if (value != null) {
                    return value;
                }
            } catch (Exception e) {
                // Try next field name
            }
        }
        return null;
    }

    private String getCurrentUserId() {
        // For now, return a default user ID. In a real application, this would get the user from SecurityContext
        // TODO: Implement proper user authentication and get user from SecurityContext
        return "ADM0003"; // Default user for testing
    }

    public void getHeatSheet(String sampleId){
        // TestSampleAcptEntity tsae = tsar.findByTsaiSampleId(sampleId)
        //     .orElseThrow(() -> new SmsResourceNotFoundException(
        //                     new SmsErrorDetails(RailMillAppConstant.ERROR_CODE_RESOURCE,
        //                             RailMillAppConstant.ERROR_TYPE_CODE_RESOURCE,
        //                             RailMillAppConstant.ERROR_TYPE_RESOURCE,
        //                             "No detail found for provided rail ID.")));

        // HeatSheetResDto hsrd = new HeatSheetResDto();
        // hsrd.setHeatNo(tsae.getTsai().getHeatNo());
        // hsrd.setSampleId(sampleId);
    }
    @Override
    @Transactional
    public void saveCompletedTest(CompletedTestDto testDto) {
        System.out.println("🔧 Saving test: " + testDto.getTestType() + " for sample: " + testDto.getSampleId());

        CompletedTest test = completedTestRepository
            .findByHeatNumberAndStrandNumberAndSampleIdAndTestType(
                testDto.getHeatNumber(),
                testDto.getStrandNumber(),
                testDto.getSampleId(),
                testDto.getTestType().toUpperCase()
            ).orElse(new CompletedTest());
    
        // Set common fields (only if this is a new record)
        if (test.getId() == null) {
            test.setDutyId(testDto.getDutyId());
            test.setHeatNumber(testDto.getHeatNumber());
            test.setStrandNumber(testDto.getStrandNumber());
            test.setMill(testDto.getMill());
            test.setRailGrade(testDto.getRailGrade());
            test.setSampleType(testDto.getSampleType());
            test.setSampleLot(testDto.getSampleLot());
            test.setSampleId(testDto.getSampleId());
            test.setSampleIdentification(testDto.getSampleIdentification());
            test.setTestType(testDto.getTestType().toUpperCase());
            setFieldValue(test, "createdAt", LocalDateTime.now());
        }

        // Always update remarks
        test.setRemarks(testDto.getRemarks());
    
        // Set test-specific fields based on test type
        String testType = testDto.getTestType().toUpperCase();

        if ("FWT".equals(testType) || "FWT_ST".equals(testType) || "FWT_HS".equals(testType) || "FWT_ST_SR".equals(testType)) {
            // Handle FWT test variants - frontend sends fwtTestStatus
            System.out.println("🧪 Processing FWT test variant: " + testType);
            setFieldValue(test, "fwtStatus", getFieldValue(testDto, "fwtTestStatus", "fwtStatus"));
        } else if ("CHEMICAL".equals(testType)) {
            // Handle CHEMICAL test - frontend sends chemicalLadleStatus and chemicalProductStatus
            setFieldValue(test, "chemicalLadleStatus", getFieldValue(testDto, "chemicalLadleStatus"));
            setFieldValue(test, "chemicalProductStatus", getFieldValue(testDto, "chemicalProductStatus"));
        } else if ("MICRO".equals(testType)) {
            // Handle MICRO test
            setFieldValue(test, "microStatus", getFieldValue(testDto, "microStatus"));
            setFieldValue(test, "microStructurePearlitic", getFieldValue(testDto, "microStructurePearlitic"));
        } else if ("N2".equals(testType)) {
            // Handle N2 test
            setFieldValue(test, "n2Status", getFieldValue(testDto, "n2Status"));
            setFieldValue(test, "n2", getFieldValue(testDto, "n2"));
        } else if ("MECHANICAL".equals(testType)) {
            // Handle MECHANICAL test (uses macro_status in database)
            // Try multiple possible field names that the frontend might send
            setFieldValue(test, "macroStatus", getFieldValue(testDto, "mechanicalTestStatus", "macroStatus", "mechanicalStatus"));
        } else if ("SP".equals(testType)) {
            // Handle SP test
            setFieldValue(test, "spStatus", getFieldValue(testDto, "spStatus"));
            setFieldValue(test, "spValue", getFieldValue(testDto, "spValue"));
        } else if ("IR".equals(testType)) {
            // Handle IR test
            setFieldValue(test, "irStatus", getFieldValue(testDto, "irStatus"));
            setFieldValue(test, "irAluminate", getFieldValue(testDto, "irAluminate"));
            setFieldValue(test, "irOxide", getFieldValue(testDto, "irOxide"));
            setFieldValue(test, "irSilicate", getFieldValue(testDto, "irSilicate"));
            setFieldValue(test, "irSulphide", getFieldValue(testDto, "irSulphide"));
        } else if ("O2".equals(testType)) {
            // Handle O2 test
            setFieldValue(test, "o2Status", getFieldValue(testDto, "o2Status"));
            setFieldValue(test, "o2", getFieldValue(testDto, "o2"));
        } else if ("TENSILE_FOOT".equals(testType)) {
            // Handle TENSILE_FOOT test
            setFieldValue(test, "tensileFootStatus", getFieldValue(testDto, "tensileFootStatus"));
            setFieldValue(test, "tensileFootYield", getFieldValue(testDto, "tensileFootYield"));
            setFieldValue(test, "tensileFootUts", getFieldValue(testDto, "tensileFootUts"));
            setFieldValue(test, "tensileFootEi", getFieldValue(testDto, "tensileFootEi"));
        } else if ("DECARB".equals(testType)) {
            // Handle DECARB test
            setFieldValue(test, "decarbStatus", getFieldValue(testDto, "decarbStatus"));
            setFieldValue(test, "decarb1", getFieldValue(testDto, "decarb1"));
            setFieldValue(test, "decarb2", getFieldValue(testDto, "decarb2"));
            setFieldValue(test, "decarb3", getFieldValue(testDto, "decarb3"));
        } else if ("RSH".equals(testType)) {
            // Handle RSH test
            setFieldValue(test, "rshStatus", getFieldValue(testDto, "rshStatus"));
            setFieldValue(test, "rshValue", getFieldValue(testDto, "rshValue"));
        } else if ("PH".equals(testType)) {
            // Handle PH test
            System.out.println("🧪 Processing PH test - phStatus: " + getFieldValue(testDto, "phStatus") + ", phValue: " + getFieldValue(testDto, "phValue"));
            setFieldValue(test, "phStatus", getFieldValue(testDto, "phStatus"));
            setFieldValue(test, "phValue", getFieldValue(testDto, "phValue"));
            System.out.println("✅ PH test fields set successfully");
        } else if ("TENSILE".equals(testType)) {
            // Handle TENSILE test
            System.out.println("🧪 Processing TENSILE test");
            Object tensileTestStatusValue = getFieldValue(testDto, "tensileTestStatus");
            Object tensileStatusValue = getFieldValue(testDto, "tensileStatus");
            System.out.println("   tensileTestStatus: " + tensileTestStatusValue);
            System.out.println("   tensileStatus: " + tensileStatusValue);
            System.out.println("   tensileYield: " + getFieldValue(testDto, "tensileYield"));
            System.out.println("   tensileUts: " + getFieldValue(testDto, "tensileUts"));
            System.out.println("   tensileEi: " + getFieldValue(testDto, "tensileEi"));

            // Use tensileTestStatus if available, otherwise fall back to tensileStatus
            Object statusValue = tensileTestStatusValue != null ? tensileTestStatusValue : tensileStatusValue;
            System.out.println("   Final status value to save: " + statusValue);

            setFieldValue(test, "tensileStatus", statusValue);
            setFieldValue(test, "tensileYield", getFieldValue(testDto, "tensileYield"));
            setFieldValue(test, "tensileUts", getFieldValue(testDto, "tensileUts"));
            setFieldValue(test, "tensileEi", getFieldValue(testDto, "tensileEi"));
            System.out.println("✅ TENSILE test fields set successfully");
        } else if ("HARDNESS".equals(testType)) {
            // Handle HARDNESS test
            setFieldValue(test, "harnessStatus", getFieldValue(testDto, "hardnessTestStatus", "harnessStatus"));
            setFieldValue(test, "hardness1a", getFieldValue(testDto, "hardness1a"));
            setFieldValue(test, "hardness1b", getFieldValue(testDto, "hardness1b"));
            setFieldValue(test, "hardness1c", getFieldValue(testDto, "hardness1c"));
            setFieldValue(test, "hardness2a", getFieldValue(testDto, "hardness2a"));
            setFieldValue(test, "hardness2b", getFieldValue(testDto, "hardness2b"));
            setFieldValue(test, "hardness3", getFieldValue(testDto, "hardness3"));
            setFieldValue(test, "hardness4a", getFieldValue(testDto, "hardness4a"));
            setFieldValue(test, "hardness4b", getFieldValue(testDto, "hardness4b"));
            setFieldValue(test, "hardnessRs", getFieldValue(testDto, "hardnessRs"));
        } else if ("MACRO".equals(testType)) {
            // Handle MACRO test (separate from MECHANICAL)
            setFieldValue(test, "macroStatus", getFieldValue(testDto, "macroTestStatus", "macroStatus"));
        } else {
            throw new IllegalArgumentException("Unsupported test type: " + testType);
        }

        completedTestRepository.save(test);
    }

    @Override
    public boolean isTestCompleted(String heatNumber, Integer strandNumber, String sampleId, String testType) {
        try {
            return completedTestRepository
                .findByHeatNumberAndStrandNumberAndSampleIdAndTestType(heatNumber, strandNumber, sampleId, testType)
                .isPresent();
        } catch (Exception e) {
            System.err.println("Error checking if test is completed: " + e.getMessage());
            return false;
        }
    }

    @Override
    public CompletedTestDto getCompletedTest(String heatNumber, Integer strandNumber, String sampleId, String testType) {
        try {
            System.out.println("Searching for completed test with params: " +
                "heatNumber=" + heatNumber + ", strandNumber=" + strandNumber +
                ", sampleId=" + sampleId + ", testType=" + testType);

            Optional<CompletedTest> testOpt = completedTestRepository
                .findByHeatNumberAndStrandNumberAndSampleIdAndTestType(heatNumber, strandNumber, sampleId, testType);

            System.out.println("Found test: " + testOpt.isPresent());

            if (testOpt.isPresent()) {
                CompletedTest test = testOpt.get();
                CompletedTestDto dto = new CompletedTestDto();

                System.out.println("Mapping test data for test type: " + test.getTestType());

                // Map basic fields from entity to DTO - only the essential ones
                dto.setId(test.getId());
                dto.setDutyId(test.getDutyId());
                dto.setTestType(test.getTestType());
                dto.setHeatNumber(test.getHeatNumber());
                dto.setStrandNumber(test.getStrandNumber());
                dto.setSampleId(test.getSampleId());

                // Map test-specific fields based on test type
                String type = test.getTestType().toUpperCase();
                try {
                    if ("CHEMICAL".equals(type)) {
                        dto.setChemicalLadleStatus(test.getChemicalLadleStatus());
                        dto.setChemicalProductStatus(test.getChemicalProductStatus());
                        System.out.println("Mapped chemical test fields successfully");
                    } else if ("TENSILE_FOOT".equals(type)) {
                        dto.setTensileFootStatus(test.getTensileFootStatus());
                        dto.setTensileFootYield(test.getTensileFootYield());
                        dto.setTensileFootUts(test.getTensileFootUts());
                        dto.setTensileFootEi(test.getTensileFootEi());
                        System.out.println("Mapped tensile foot test fields successfully");
                    } else if ("O2".equals(type)) {
                        dto.setO2Status(test.getO2Status());
                        dto.setO2(test.getO2());
                        System.out.println("Mapped O2 test fields successfully");
                    } else if ("FWT".equals(type) || "FWT_ST".equals(type) || "FWT_HS".equals(type) || "FWT_ST_SR".equals(type)) {
                        dto.setFwtStatus(test.getFwtStatus());
                        System.out.println("Mapped FWT test variant (" + type + ") fields successfully");
                    } else if ("N2".equals(type)) {
                        dto.setN2Status(test.getN2Status());
                        dto.setN2(test.getN2());
                        System.out.println("Mapped N2 test fields successfully");
                    } else if ("MECHANICAL".equals(type)) {
                        dto.setMechanicalTestStatus(test.getMacroStatus());
                        System.out.println("Mapped mechanical test fields successfully");
                    } else if ("MICRO".equals(type)) {
                        dto.setMicroStatus(test.getMicroStatus());
                        dto.setMicroStructurePearlitic(test.getMicroStructurePearlitic());
                        System.out.println("Mapped micro test fields successfully");
                    } else if ("TENSILE".equals(type)) {
                        dto.setTensileStatus(test.getTensileStatus());
                        dto.setTensileYield(test.getTensileYield());
                        dto.setTensileUts(test.getTensileUts());
                        dto.setTensileEi(test.getTensileEi());
                        System.out.println("Mapped tensile test fields successfully");
                    } else if ("IR".equals(type)) {
                        dto.setIrStatus(test.getIrStatus());
                        dto.setIrSulphide(test.getIrSulphide());
                        dto.setIrAluminate(test.getIrAluminate());
                        dto.setIrSilicate(test.getIrSilicate());
                        dto.setIrOxide(test.getIrOxide());
                        System.out.println("Mapped IR test fields successfully");
                    } else if ("DECARB".equals(type)) {
                        dto.setDecarbStatus(test.getDecarbStatus());
                        dto.setDecarb1(test.getDecarb1());
                        dto.setDecarb2(test.getDecarb2());
                        dto.setDecarb3(test.getDecarb3());
                        System.out.println("Mapped decarb test fields successfully");
                    } else if ("SP".equals(type)) {
                        dto.setSpStatus(test.getSpStatus());
                        dto.setSpValue(test.getSpValue());
                        System.out.println("Mapped SP test fields successfully");
                    } else if ("RSH".equals(type)) {
                        dto.setRshStatus(test.getRshStatus());
                        dto.setRshValue(test.getRshValue());
                        System.out.println("Mapped RSH test fields successfully");
                    } else if ("PH".equals(type)) {
                        dto.setPhStatus(test.getPhStatus());
                        dto.setPhValue(test.getPhValue());
                        System.out.println("Mapped PH test fields successfully");
                    } else if ("HARDNESS".equals(type)) {
                        dto.setHarnessStatus(test.getHarnessStatus());
                        dto.setHardness1a(test.getHardness1a());
                        dto.setHardness1b(test.getHardness1b());
                        dto.setHardness1c(test.getHardness1c());
                        dto.setHardness2a(test.getHardness2a());
                        dto.setHardness2b(test.getHardness2b());
                        dto.setHardness3(test.getHardness3());
                        dto.setHardness4a(test.getHardness4a());
                        dto.setHardness4b(test.getHardness4b());
                        dto.setHardnessRs(test.getHardnessRs());
                        System.out.println("Mapped hardness test fields successfully");
                    } else if ("MACRO".equals(type)) {
                        dto.setMacroStatus(test.getMacroStatus());
                        System.out.println("Mapped macro test fields successfully");
                    }
                } catch (Exception fieldError) {
                    System.err.println("Error mapping " + type + " fields: " + fieldError.getMessage());
                }

                System.out.println("Successfully created DTO");
                return dto;
            }

            System.out.println("No test found, returning null");
            return null;
        } catch (Exception e) {
            System.err.println("Error retrieving completed test: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }



    @Override
    public List<CompletedTest> getAllCompletedTests() {
        return completedTestRepository.findAll();
    }

    @Override
    public void saveTestRecord() {
        throw new SmsResourceNotFoundException(
            new SmsErrorDetails(
                RailMillAppConstant.ERROR_CODE_RESOURCE,
                RailMillAppConstant.ERROR_TYPE_CODE_RESOURCE,
                RailMillAppConstant.ERROR_TYPE_RESOURCE,
                "Resource not found"
            )
        );
    }

}


