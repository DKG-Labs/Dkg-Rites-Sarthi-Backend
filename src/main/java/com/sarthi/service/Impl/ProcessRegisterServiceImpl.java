package com.sarthi.service.Impl;

import com.sarthi.dto.processmaterial.ProcessInspectionRegisterResponseDTO;
import com.sarthi.dto.processmaterial.ProcessInspectionRegisterRowDTO;
import com.sarthi.entity.InspectionCallDetails;
import com.sarthi.entity.MainPoInformation;
import com.sarthi.entity.UserMaster;
import com.sarthi.entity.processmaterial.*;
import com.sarthi.repository.InspectionCallDetailsRepository;
import com.sarthi.repository.MainPoInformationRepository;
import com.sarthi.repository.UserMasterRepository;
import com.sarthi.repository.processmaterial.*;
import com.sarthi.repository.rawmaterial.InspectionCallRepository;
import com.sarthi.service.ProcessRegisterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ProcessRegisterServiceImpl implements ProcessRegisterService {

    @Autowired
    private ProcessLineFinalResultRepository lineFinalResultRepository;

    @Autowired
    private ProcessShearingDataRepository shearingRepository;

    @Autowired
    private ProcessTurningDataRepository turningRepository;

    @Autowired
    private ProcessMpiDataRepository mpiRepository;

    @Autowired
    private ProcessQuenchingDataRepository quenchingRepository;

    @Autowired
    private ProcessTemperingDataRepository temperingRepository;

    @Autowired
    private ProcessTestingFinishingDataRepository testingFinishingRepository;

    @Autowired
    private ProcessFinalCheckDataRepository finalCheckRepository;

    @Autowired
    private UserMasterRepository userMasterRepository;

    @Autowired
    private InspectionCallDetailsRepository callDetailsRepository;

    @Autowired
    private MainPoInformationRepository mainPoRepository;

    @Autowired
    private com.sarthi.repository.InspectionCompleteDetailsRepository inspectionCompleteDetailsRepository;

    @Autowired
    private InspectionCallRepository inspectionCallRepository;

    @Autowired
    private ProcessStaticPeriodicCheckRepository staticPeriodicCheckRepository;

    @Override
    public List<ProcessInspectionRegisterResponseDTO> getProcessInspectionRegister(String callNo, String date, String shift, String createdBy) {
        
        // 1. Fetch available lots/lines for this call, date, shift, and user
        List<ProcessLineFinalResult> finalResults;
        if (date != null && !date.isEmpty() && shift != null && !shift.isEmpty()) {
            LocalDate inspectionDate = LocalDate.parse(date);
            finalResults = lineFinalResultRepository.findByInspectionCallNoAndDateOfInspectionAndShiftAndCreatedBy(callNo, inspectionDate, shift, createdBy);
        } else {
            finalResults = lineFinalResultRepository.findByInspectionCallNoAndCreatedBy(callNo, createdBy);
        }

        if (finalResults.isEmpty()) return new ArrayList<>();

        // 2. Performance Optimization: Fetch Common Data ONCE (N+1 Prevention)
        Optional<MainPoInformation> poInfo = mainPoRepository.findByInspectionCallNo(callNo);
        String poNoAndDateStr = poInfo.map(po -> po.getPoNo() + " Date- " + (po.getPoDate() != null ? po.getPoDate().format(DateTimeFormatter.ofPattern("dd-MM-yyyy")) : "")).orElse("");
        String mfgNameStr = poInfo.map(MainPoInformation::getVendorName).orElse("");

        String engineerName = createdBy;
        try {
            Integer userId = Integer.parseInt(createdBy);
            engineerName = userMasterRepository.findByUserId(userId).map(UserMaster::getFullName).orElse(createdBy);
        } catch(NumberFormatException e) {
            engineerName = userMasterRepository.findByUserName(createdBy).map(UserMaster::getFullName).orElse(createdBy);
        }

        Optional<InspectionCallDetails> callDetails = callDetailsRepository.findFirstByInspectionCallNoOrderByIdDesc(callNo);
        String rmIcNumber = callDetails.map(InspectionCallDetails::getRmIcNumber).orElse("-");
        
        Optional<com.sarthi.entity.InspectionCompleteDetails> completeDetails = inspectionCompleteDetailsRepository.findFirstByCallNoOrderByCreatedOnDesc(callNo);
        String icDate = completeDetails.map(c -> c.getCreatedOn() != null ? c.getCreatedOn().format(DateTimeFormatter.ofPattern("dd-MM-yyyy")) : "").orElse("-");
        String rawMaterialIcNoAndDate = rmIcNumber + " Date-" + icDate;

        Optional<com.sarthi.entity.rawmaterial.InspectionCall> icEntity = inspectionCallRepository.findByIcNumber(callNo);
        String ercType = icEntity.map(com.sarthi.entity.rawmaterial.InspectionCall::getErcType).orElse("-");
        String callDateStr = icEntity.flatMap(ic -> Optional.ofNullable(ic.getCreatedAt())).map(d -> d.format(DateTimeFormatter.ofPattern("dd-MM-yyyy"))).orElse("");
        String callNoAndDateStr = callNo + (callDateStr.isEmpty() ? "" : " Date-" + callDateStr);

            // 3. Batch Fetch All Process Data for the entire Call (Extreme Performance Boost)
            List<ProcessShearingData> allShearing = shearingRepository.findByInspectionCallNo(callNo);
            List<ProcessTurningData> allTurning = turningRepository.findByInspectionCallNo(callNo);
            List<ProcessMpiData> allMpi = mpiRepository.findByInspectionCallNo(callNo);
            List<ProcessQuenchingData> allQuenching = quenchingRepository.findByInspectionCallNo(callNo);
            List<ProcessTemperingData> allTempering = temperingRepository.findByInspectionCallNo(callNo);
            List<ProcessTestingFinishingData> allFinishing = testingFinishingRepository.findByInspectionCallNo(callNo);
            List<ProcessFinalCheckData> allFinalCheck = finalCheckRepository.findByInspectionCallNo(callNo);

        // Index data by shift|lot|user|date for strict O(1) lookup
        // We use .toLocalDate() to ensure "same day" matching as requested
        Map<String, List<ProcessShearingData>> shearingMap = allShearing.stream().collect(Collectors.groupingBy(d -> 
            d.getShift() + "|" + d.getLotNo() + "|" + d.getCreatedBy() + "|" + (d.getCreatedAt() != null ? d.getCreatedAt().toLocalDate() : "")));
        Map<String, List<ProcessTurningData>> turningMap = allTurning.stream().collect(Collectors.groupingBy(d -> 
            d.getShift() + "|" + d.getLotNo() + "|" + d.getCreatedBy() + "|" + (d.getCreatedAt() != null ? d.getCreatedAt().toLocalDate() : "")));
        Map<String, List<ProcessMpiData>> mpiMap = allMpi.stream().collect(Collectors.groupingBy(d -> 
            d.getShift() + "|" + d.getLotNo() + "|" + d.getCreatedBy() + "|" + (d.getCreatedAt() != null ? d.getCreatedAt().toLocalDate() : "")));
        Map<String, List<ProcessQuenchingData>> quenchingMap = allQuenching.stream().collect(Collectors.groupingBy(d -> 
            d.getShift() + "|" + d.getLotNo() + "|" + d.getCreatedBy() + "|" + (d.getCreatedAt() != null ? d.getCreatedAt().toLocalDate() : "")));
        Map<String, List<ProcessTemperingData>> temperingMap = allTempering.stream().collect(Collectors.groupingBy(d -> 
            d.getShift() + "|" + d.getLotNo() + "|" + d.getCreatedBy() + "|" + (d.getCreatedAt() != null ? d.getCreatedAt().toLocalDate() : "")));
        Map<String, List<ProcessTestingFinishingData>> finishingMap = allFinishing.stream().collect(Collectors.groupingBy(d -> 
            d.getShift() + "|" + d.getLotNo() + "|" + d.getCreatedBy() + "|" + (d.getCreatedAt() != null ? d.getCreatedAt().toLocalDate() : "")));
        Map<String, List<ProcessFinalCheckData>> finalCheckMap = allFinalCheck.stream().collect(Collectors.groupingBy(d -> 
            d.getShift() + "|" + d.getLotNo() + "|" + d.getCreatedBy() + "|" + (d.getCreatedAt() != null ? d.getCreatedAt().toLocalDate() : "")));

        List<ProcessInspectionRegisterResponseDTO> responseList = new ArrayList<>();

        for (ProcessLineFinalResult result : finalResults) {
            LocalDate resultDate = result.getDateOfInspection() != null ? result.getDateOfInspection() : (result.getCreatedAt() != null ? result.getCreatedAt().toLocalDate() : null);
            String lotKey = result.getShift() + "|" + result.getLotNumber() + "|" + result.getCreatedBy() + "|" + (resultDate != null ? resultDate : "");
            ProcessInspectionRegisterResponseDTO dto = new ProcessInspectionRegisterResponseDTO();
            
            // Populate Static Data
            dto.setDate(result.getDateOfInspection() != null ? result.getDateOfInspection().format(DateTimeFormatter.ofPattern("dd-MM-yyyy")) : (result.getCreatedAt() != null ? result.getCreatedAt().format(DateTimeFormatter.ofPattern("dd-MM-yyyy")) : ""));
            dto.setShift(result.getShift());
            dto.setLotNo(result.getLotNumber());
            dto.setErcProducedDuringShift(result.getTotalManufactured());
            dto.setPoNoAndDate(poNoAndDateStr);
            dto.setMfgName(mfgNameStr);
            dto.setInspectingEngineerName(engineerName);
            dto.setRawMaterialIcNoAndDate(rawMaterialIcNoAndDate);
            dto.setCaseNoIbs(callNo);
            dto.setErcType(ercType);
            dto.setCallNoAndDate(callNoAndDateStr);
            dto.setHeatNo(result.getHeatNumber());
            dto.setLineNo(result.getLineNo());
            dto.setRemarks(result.getAnnexureRemarks());
            dto.setHourLabels(getHourLabelsForShift(result.getShift()));

            // Get pre-fetched data for this specific lot
            List<ProcessShearingData> lotShearing = shearingMap.getOrDefault(lotKey, Collections.emptyList());
            List<ProcessTurningData> lotTurning = turningMap.getOrDefault(lotKey, Collections.emptyList());
            List<ProcessMpiData> lotMpi = mpiMap.getOrDefault(lotKey, Collections.emptyList());
            List<ProcessQuenchingData> lotQuenching = quenchingMap.getOrDefault(lotKey, Collections.emptyList());
            List<ProcessTemperingData> lotTempering = temperingMap.getOrDefault(lotKey, Collections.emptyList());
            List<ProcessTestingFinishingData> lotFinishing = finishingMap.getOrDefault(lotKey, Collections.emptyList());
            List<ProcessFinalCheckData> lotFinalCheck = finalCheckMap.getOrDefault(lotKey, Collections.emptyList());

            List<ProcessInspectionRegisterRowDTO> rows = new ArrayList<>();

            rows.add(createRow(1, "Checking of Length of cut bars, random (3 bars/Hr.)", 
                mapHourly(lotShearing, d -> formatReadings(d.getLengthCutBar1(), d.getLengthCutBar2(), d.getLengthCutBar3())),
                result.getShearingRejected() != null ? result.getShearingRejected() : 0));

            rows.add(createRow(2, "Turning Length, random (3 bars/Hr.)", 
                mapHourly(lotTurning, d -> formatCombinedReadings(d.getStraightLength1(), d.getTaperLength1(), d.getStraightLength2(), d.getTaperLength2(), d.getStraightLength3(), d.getTaperLength3())),
                sumRejected(lotTurning, d -> (d.getParallelLengthRejected() != null ? d.getParallelLengthRejected() : 0) + (d.getFullTurningLengthRejected() != null ? d.getFullTurningLengthRejected() : 0))));

            rows.add(createRow(3, "Turning Dia, random (3 bars/Hr.)", 
                mapHourly(lotTurning, d -> formatReadings(d.getDia1(), d.getDia2(), d.getDia3())),
                sumRejected(lotTurning, d -> d.getTurningDiaRejected() != null ? d.getTurningDiaRejected() : 0)));

            rows.add(createRow(4, "MPI Test, random (3 bars/Hr.)", 
                mapHourly(lotMpi, d -> formatReadings(d.getTestResult1(), d.getTestResult2(), d.getTestResult3())),
                result.getMpiRejected() != null ? result.getMpiRejected() : 0));

            rows.add(createRow(5, "Forging Temp. (N/A)", mapHourly(null, null), result.getForgingRejected() != null ? result.getForgingRejected() : 0));

            // Fetch static periodic check to determine Row 6 display
            // Try precise composite key first; fall back to callNo+lineNo for older records without shift/lot/date
            Optional<ProcessStaticPeriodicCheck> staticCheck = staticPeriodicCheckRepository
                .findByInspectionCallNoAndShiftAndLineNoAndLotNoAndCreatedByAndDateOfInspection(
                    callNo, result.getShift(), result.getLineNo(), result.getLotNumber(), result.getCreatedBy(), resultDate
                );
            if (!staticCheck.isPresent()) {
                staticCheck = staticPeriodicCheckRepository
                    .findFirstByInspectionCallNoAndLineNoOrderByCreatedAtDesc(callNo, result.getLineNo());
            }
            
            String dieCheckRemark = "-";
            if (staticCheck.isPresent()) {
                Boolean isDieCheckOk = staticCheck.get().getForgingDieCheck();
                if (isDieCheckOk != null && isDieCheckOk) {
                    dieCheckRemark = "OK";
                } else if (isDieCheckOk != null && !isDieCheckOk) {
                    dieCheckRemark = "NOT OK";
                }
            }
            rows.add(createRow(6, "Checking of Die (100%)", mapHourly(null, null), dieCheckRemark));

            rows.add(createRow(7, "Quenching Temp & Duration (100%)", 
                mapHourly(lotQuenching, d -> formatCombinedReadings(d.getQuenchingTemperature1(), d.getQuenchingDuration1())),
                sumRejected(lotQuenching, d -> (d.getQuenchingTemperatureRejected() != null ? d.getQuenchingTemperatureRejected() : 0) + (d.getQuenchingDurationRejected() != null ? d.getQuenchingDurationRejected() : 0))));

            rows.add(createRow(8, "Quenching Hardness (2 ERCs/Hr.)", 
                mapHourly(lotQuenching, d -> formatReadings(d.getQuenchingHardness1(), d.getQuenchingHardness2())),
                result.getHardnessCheckRejected() != null ? result.getHardnessCheckRejected() : 0));

            rows.add(createRow(9, "Tempering Temp & Duration (100%)", 
                mapHourly(lotTempering, d -> formatCombinedReadings(d.getTemperingTemperature1(), d.getTemperingDuration1())),
                sumRejected(lotTempering, d -> (d.getTemperingTemperatureRejected() != null ? d.getTemperingTemperatureRejected() : 0) + (d.getTemperingDurationRejected() != null ? d.getTemperingDurationRejected() : 0))));

            rows.add(createRow(10, "Dimension Check (2 ERCs/Hr.)", 
                mapHourly(lotQuenching, d -> formatReadings(d.getBoxGauge1(), d.getFallingGauge1())),
                result.getDimensionsCheckRejected() != null ? result.getDimensionsCheckRejected() : 0));

            rows.add(createRow(11, "Hardness of finished ERC (2 ERCs/Hr.)", 
                mapHourly(lotFinalCheck, d -> formatReadings(d.getTemperingHardness1(), d.getTemperingHardness2())),
                result.getHardnessCheckRejected() != null ? result.getHardnessCheckRejected() : 0));

            rows.add(createRow(12, "Toe load of finished ERC (2 ERCs/Hr.)", 
                mapHourly(lotFinishing, d -> formatReadings(d.getToeLoad1(), d.getToeLoad2())),
                sumRejected(lotFinishing, ProcessTestingFinishingData::getToeLoadRejected)));

            // Row 13 Paint Logic using pre-fetched lists
            List<String> row13Data = new ArrayList<>();
            for (int i = 1; i <= 8; i++) {
                final int hIdxFinal = i;
                final double minToe = ("MK-III".equalsIgnoreCase(ercType)) ? 850 : (("MK-V".equalsIgnoreCase(ercType)) ? 1200 : 0);
                final double maxToe = ("MK-III".equalsIgnoreCase(ercType)) ? 1100 : (("MK-V".equalsIgnoreCase(ercType)) ? 1500 : 0);

                // Pick the LATEST record for this hour to match the grid display exactly
                Optional<ProcessQuenchingData> qd = lotQuenching.stream().filter(d -> Integer.valueOf(hIdxFinal).equals(getHourIndex(d))).reduce((first, second) -> second);
                Optional<ProcessFinalCheckData> fcd = lotFinalCheck.stream().filter(d -> Integer.valueOf(hIdxFinal).equals(getHourIndex(d))).reduce((first, second) -> second);
                Optional<ProcessTestingFinishingData> tfd = lotFinishing.stream().filter(d -> Integer.valueOf(hIdxFinal).equals(getHourIndex(d))).reduce((first, second) -> second);

                boolean hasHourData = (qd.isPresent() && (qd.get().getBoxGauge1() != null || qd.get().getFallingGauge1() != null)) ||
                                      (fcd.isPresent() && (fcd.get().getTemperingHardness1() != null || fcd.get().getTemperingHardness2() != null)) ||
                                      (tfd.isPresent() && (tfd.get().getToeLoad1() != null || tfd.get().getToeLoad2() != null));

                if (!hasHourData) {
                    row13Data.add("-");
                } else {
                    boolean isRejected = false;
                    if (qd.isPresent() && (isTextRejected(qd.get().getBoxGauge1()) || isTextRejected(qd.get().getFallingGauge1()))) isRejected = true;
                    if (!isRejected && fcd.isPresent() && (isOutOfRange(fcd.get().getTemperingHardness1(), 40.0, 44.0) || isOutOfRange(fcd.get().getTemperingHardness2(), 40.0, 44.0))) isRejected = true;
                    if (!isRejected && tfd.isPresent() && minToe > 0 && (isOutOfRange(tfd.get().getToeLoad1(), minToe, maxToe) || isOutOfRange(tfd.get().getToeLoad2(), minToe, maxToe))) isRejected = true;

                    if (isRejected) row13Data.add("Red");
                    else if ("MK-III".equalsIgnoreCase(ercType)) row13Data.add("Yellow");
                    else if ("MK-V".equalsIgnoreCase(ercType)) row13Data.add("Green");
                    else row13Data.add("-");
                }
            }

            int row13RedCount = (int) row13Data.stream().filter("Red"::equals).count();
            rows.add(createRow(13, "Confirmation of yellow and green paint", row13Data, row13RedCount));
            dto.setRows(rows);
            responseList.add(dto);
        }
        return responseList;
    }

    @Override
    public List<Map<String, Object>> getAvailableEntries(String callNo, String createdBy) {
        List<ProcessLineFinalResult> results = lineFinalResultRepository.findByInspectionCallNoAndCreatedBy(callNo, createdBy);
        
        return results.stream()
                .map(r -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("date", r.getDateOfInspection() != null ? r.getDateOfInspection().toString() : "");
                    map.put("shift", r.getShift());
                    map.put("lotNo", r.getLotNumber());
                    return map;
                })
                .distinct() // Ensure unique combinations
                .collect(Collectors.toList());
    }

    private ProcessInspectionRegisterRowDTO createRow(int srNo, String activity, List<String> hourlyData, int totalRejected) {
        ProcessInspectionRegisterRowDTO row = new ProcessInspectionRegisterRowDTO();
        row.setSrNo(srNo);
        row.setActivity(activity);
        row.setHourlyData(hourlyData);
        
        int totalReadings = countReadings(hourlyData);
        
        if (totalReadings == 0 && totalRejected == 0) {
            row.setRemarks("-");
        } else {
            // Failsafe in case totalReadings is less than totalRejected
            int totalAccepted = Math.max(0, totalReadings - totalRejected);
            
            // If totalReadings is 0 but there are rejections (e.g. 100% check), just show rejected
            if (totalReadings == 0 && totalRejected > 0) {
                row.setRemarks(totalRejected + "-Not Accepted");
            } else if (totalRejected == 0) {
                row.setRemarks(totalAccepted + "-Accepted");
            } else if (totalAccepted == 0) {
                row.setRemarks(totalRejected + "-Not Accepted");
            } else {
                row.setRemarks(totalAccepted + "-Accepted, " + totalRejected + "-Not Accepted");
            }
        }
        return row;
    }

    private ProcessInspectionRegisterRowDTO createRow(int srNo, String activity, List<String> hourlyData, String remarks) {
        ProcessInspectionRegisterRowDTO row = new ProcessInspectionRegisterRowDTO();
        row.setSrNo(srNo);
        row.setActivity(activity);
        row.setHourlyData(hourlyData);
        row.setRemarks(remarks);
        return row;
    }

    private <T> List<String> mapHourly(List<T> data, java.util.function.Function<T, String> formatter) {
        List<String> hourly = new ArrayList<>(Collections.nCopies(8, "-"));
        if (data == null) return hourly;
        
        for (T item : data) {
            Integer index = getHourIndex(item);
            if (index != null) {
                // Adjust for 1-based index (1-8) vs 0-based index (0-7)
                int targetIndex = (index > 0 && index <= 8) ? index - 1 : index;
                if (targetIndex >= 0 && targetIndex < 8) {
                    hourly.set(targetIndex, formatter.apply(item));
                }
            }
        }
        return hourly;
    }

    private <T> int sumRejected(List<T> data, java.util.function.Function<T, Integer> rejectionCheck) {
        if (data == null) return 0;
        int sum = 0;
        for (T item : data) {
            Integer rejected = rejectionCheck.apply(item);
            if (rejected != null) {
                sum += rejected;
            }
        }
        return sum;
    }

    private int countReadings(List<String> hourlyData) {
        int count = 0;
        for (String hour : hourlyData) {
            if (hour != null && !hour.equals("-") && !hour.trim().isEmpty()) {
                count += hour.split(",").length;
            }
        }
        return count;
    }

    private String formatReadings(Object... readings) {
        return Arrays.stream(readings)
                .filter(Objects::nonNull)
                .map(Object::toString)
                .filter(s -> !s.trim().isEmpty())
                .collect(Collectors.joining(", "));
    }

    private String formatCombinedReadings(Object... readings) {
        if (readings == null || readings.length < 2) return "-";
        
        List<String> combined = new ArrayList<>();
        for (int i = 0; i < readings.length; i += 2) {
            Object r1 = readings[i];
            Object r2 = (i + 1 < readings.length) ? readings[i + 1] : null;
            
            if (r1 != null || r2 != null) {
                combined.add(formatValue(r1) + "&" + formatValue(r2));
            }
        }
        
        if (combined.isEmpty()) return "-";
        return String.join(", ", combined);
    }

    private String formatValue(Object val) {
        if (val == null) return "0";
        if (val instanceof BigDecimal) {
            BigDecimal bd = (BigDecimal) val;
            if (bd.compareTo(BigDecimal.ZERO) == 0) return "0";
            // Remove trailing zeros and return string
            String s = bd.stripTrailingZeros().toPlainString();
            return s;
        }
        return val.toString();
    }

    private Integer getHourIndex(Object item) {
        try {
            return (Integer) item.getClass().getMethod("getHourIndex").invoke(item);
        } catch (Exception e) {
            return null;
        }
    }

    private List<String> getHourLabelsForShift(String shift) {
        if (shift == null) return Arrays.asList("06:00-07:00", "07:00-08:00", "08:00-09:00", "09:00-10:00", "10:00-11:00", "11:00-12:00", "12:00-13:00", "13:00-14:00");
        String s = shift.toLowerCase();
        if (s.contains("2") || s.contains("ii") || s.contains("second") || s.contains("b")) {
            // Shift 2 (14:00 - 22:00)
            return Arrays.asList("14:00-15:00", "15:00-16:00", "16:00-17:00", "17:00-18:00", "18:00-19:00", "19:00-20:00", "20:00-21:00", "21:00-22:00");
        } else if (s.contains("3") || s.contains("iii") || s.contains("third") || s.contains("c")) {
            // Shift 3 (22:00 - 06:00)
            return Arrays.asList("22:00-23:00", "23:00-00:00", "00:00-01:00", "01:00-02:00", "02:00-03:00", "03:00-04:00", "04:00-05:00", "05:00-06:00");
        } else if (s.contains("g") || s.contains("general")) {
            // General Shift (09:00 - 17:00)
            return Arrays.asList("09:00-10:00", "10:00-11:00", "11:00-12:00", "12:00-13:00", "13:00-14:00", "14:00-15:00", "15:00-16:00", "16:00-17:00");
        }
        // Default to Shift 1 (06:00 - 14:00)
        return Arrays.asList("06:00-07:00", "07:00-08:00", "08:00-09:00", "09:00-10:00", "10:00-11:00", "11:00-12:00", "12:00-13:00", "13:00-14:00");
    }

    @Override
    public void updateRemarks(String callNo, String shift, String lineNo, String lotNo, String createdBy, String remarks) {
        List<ProcessLineFinalResult> results = lineFinalResultRepository.findByInspectionCallNoAndShiftAndLotNumberAndLineNoAndCreatedBy(
            callNo, shift, lotNo, lineNo, createdBy
        );
        
        if (!results.isEmpty()) {
            ProcessLineFinalResult result = results.get(0);
            result.setAnnexureRemarks(remarks);
            lineFinalResultRepository.save(result);
        }
    }

    private boolean isTextRejected(String val) {
        if (val == null || val.trim().isEmpty() || val.equals("-")) return false;
        String v = val.trim().toUpperCase();
        if (v.equals("OK") || v.equals("ACCEPTED") || v.equals("PASS")) return false;
        // If it looks like a number, use range logic (handled elsewhere) or assume OK if positive
        try {
            double d = Double.parseDouble(v);
            return d <= 0; // Negative or 0 might mean failed depending on context, but usually numbers here are measurements
        } catch (Exception e) {
            // If it's some other text like "REJECTED" or "FAIL", it's a rejection
            return v.contains("REJECT") || v.contains("FAIL") || v.contains("NOT OK");
        }
    }

    private boolean isOutOfRange(Object val, double min, double max) {
        if (val == null) return false;
        try {
            double d;
            if (val instanceof BigDecimal) d = ((BigDecimal) val).doubleValue();
            else d = Double.parseDouble(val.toString());
            
            if (d == 0) return false; // Assume 0 means not entered
            return d < min || d > max;
        } catch (Exception e) {
            return false;
        }
    }
}
