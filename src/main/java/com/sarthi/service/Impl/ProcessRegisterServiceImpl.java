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
        
        // 1. Fetch ALL results for this call
        List<ProcessLineFinalResult> allFinalResults = lineFinalResultRepository.findByInspectionCallNo(callNo);
        if (allFinalResults.isEmpty()) return new ArrayList<>();

        // Apply filters if provided
        if (date != null && !date.isEmpty() && shift != null && !shift.isEmpty()) {
            LocalDate inspectionDate = LocalDate.parse(date);
            allFinalResults = allFinalResults.stream()
                .filter(r -> {
                    LocalDate rDate = r.getDateOfInspection() != null ? r.getDateOfInspection() : (r.getCreatedAt() != null ? r.getCreatedAt().toLocalDate() : null);
                    return rDate != null && rDate.equals(inspectionDate) && shift.equalsIgnoreCase(r.getShift());
                })
                .collect(Collectors.toList());
        }

        if (allFinalResults.isEmpty()) return new ArrayList<>();

        // 2. Fetch Common Data ONCE
        Optional<MainPoInformation> poInfo = mainPoRepository.findByInspectionCallNo(callNo);
        String poNoAndDateStr = poInfo.map(po -> po.getPoNo() + " Date- " + (po.getPoDate() != null ? po.getPoDate().format(DateTimeFormatter.ofPattern("dd-MM-yyyy")) : "")).orElse("");
        String mfgNameStr = poInfo.map(MainPoInformation::getVendorName).orElse("");

        Optional<InspectionCallDetails> callDetails = callDetailsRepository.findFirstByInspectionCallNoOrderByIdDesc(callNo);
        String rmIcNumber = callDetails.map(InspectionCallDetails::getRmIcNumber).orElse("-");
        
        Optional<com.sarthi.entity.InspectionCompleteDetails> completeDetails = inspectionCompleteDetailsRepository.findFirstByCallNoOrderByCreatedOnDesc(callNo);
        String icDate = completeDetails.map(c -> c.getCreatedOn() != null ? c.getCreatedOn().format(DateTimeFormatter.ofPattern("dd-MM-yyyy")) : "").orElse("-");
        String rawMaterialIcNoAndDate = rmIcNumber + " Date-" + icDate;

        Optional<com.sarthi.entity.rawmaterial.InspectionCall> icEntity = inspectionCallRepository.findByIcNumber(callNo);
        String ercType = icEntity.map(com.sarthi.entity.rawmaterial.InspectionCall::getErcType).orElse("-");
        String callDateStr = icEntity.flatMap(ic -> Optional.ofNullable(ic.getCreatedAt())).map(d -> d.format(DateTimeFormatter.ofPattern("dd-MM-yyyy"))).orElse("");
        String callNoAndDateStr = callNo + (callDateStr.isEmpty() ? "" : " Date-" + callDateStr);

        // 3. Batch Fetch All Process Data
        List<ProcessShearingData> allShearing = shearingRepository.findByInspectionCallNo(callNo);
        List<ProcessTurningData> allTurning = turningRepository.findByInspectionCallNo(callNo);
        List<ProcessMpiData> allMpi = mpiRepository.findByInspectionCallNo(callNo);
        List<ProcessQuenchingData> allQuenching = quenchingRepository.findByInspectionCallNo(callNo);
        List<ProcessTemperingData> allTempering = temperingRepository.findByInspectionCallNo(callNo);
        List<ProcessTestingFinishingData> allFinishing = testingFinishingRepository.findByInspectionCallNo(callNo);
        List<ProcessFinalCheckData> allFinalCheck = finalCheckRepository.findByInspectionCallNo(callNo);

        // 4. Group Headers by GroupKey: dateOfInspection|shift|lotNo
        Map<String, List<ProcessLineFinalResult>> groupedHeaders = allFinalResults.stream().collect(Collectors.groupingBy(r -> {
            LocalDate rDate = r.getDateOfInspection() != null ? r.getDateOfInspection() : (r.getCreatedAt() != null ? r.getCreatedAt().toLocalDate() : null);
            return (rDate != null ? rDate.toString() : "") + "|" + r.getShift() + "|" + r.getLotNumber();
        }));

        List<ProcessInspectionRegisterResponseDTO> responseList = new ArrayList<>();

        for (Map.Entry<String, List<ProcessLineFinalResult>> entry : groupedHeaders.entrySet()) {
            List<ProcessLineFinalResult> groupResults = entry.getValue();
            ProcessLineFinalResult primaryResult = groupResults.get(0); // Use the first one for base info
            LocalDate resultDate = primaryResult.getDateOfInspection() != null ? primaryResult.getDateOfInspection() : (primaryResult.getCreatedAt() != null ? primaryResult.getCreatedAt().toLocalDate() : null);

            ProcessInspectionRegisterResponseDTO dto = new ProcessInspectionRegisterResponseDTO();
            
            // Collect all unique engineers involved in this Lot/Shift
            Set<String> engineerIds = groupResults.stream().map(ProcessLineFinalResult::getCreatedBy).collect(Collectors.toSet());
            String engineerNames = engineerIds.stream().map(id -> {
                try {
                    return userMasterRepository.findByUserId(Integer.parseInt(id)).map(UserMaster::getFullName).orElse(id);
                } catch(Exception e) {
                    return userMasterRepository.findByUserName(id).map(UserMaster::getFullName).orElse(id);
                }
            }).filter(Objects::nonNull).distinct().collect(Collectors.joining(", "));

            // Sum up total manufactured for the grid header
            long totalProduced = groupResults.stream().mapToLong(r -> r.getTotalManufactured() != null ? r.getTotalManufactured() : 0).sum();

            // Populate Static Data
            dto.setDate(resultDate != null ? resultDate.format(DateTimeFormatter.ofPattern("dd-MM-yyyy")) : "");
            dto.setShift(primaryResult.getShift());
            dto.setLotNo(primaryResult.getLotNumber());
            dto.setErcProducedDuringShift((int) totalProduced);
            dto.setPoNoAndDate(poNoAndDateStr);
            dto.setMfgName(mfgNameStr);
            dto.setInspectingEngineerName(engineerNames);
            dto.setRawMaterialIcNoAndDate(rawMaterialIcNoAndDate);
            dto.setCaseNoIbs(callNo);
            dto.setErcType(ercType);
            dto.setCallNoAndDate(callNoAndDateStr);
            dto.setHeatNo(primaryResult.getHeatNumber());
            dto.setLineNo(primaryResult.getLineNo());
            
            // Combine remarks if multiple exist
            String combinedRemarks = groupResults.stream().map(ProcessLineFinalResult::getAnnexureRemarks).filter(s -> s != null && !s.trim().isEmpty()).collect(Collectors.joining(" | "));
            dto.setRemarks(combinedRemarks.isEmpty() ? null : combinedRemarks);
            dto.setHourLabels(getHourLabelsForShift(primaryResult.getShift()));

            // Link child tables to these groupResults using proximity matching
            // Match condition: same shift, lot, createdBy, and createdAt within 2 minutes
            List<ProcessShearingData> lotShearing = allShearing.stream().filter(c -> matchesAnyHeader(c.getShift(), c.getLotNo(), c.getCreatedBy(), c.getCreatedAt(), groupResults)).collect(Collectors.toList());
            List<ProcessTurningData> lotTurning = allTurning.stream().filter(c -> matchesAnyHeader(c.getShift(), c.getLotNo(), c.getCreatedBy(), c.getCreatedAt(), groupResults)).collect(Collectors.toList());
            List<ProcessMpiData> lotMpi = allMpi.stream().filter(c -> matchesAnyHeader(c.getShift(), c.getLotNo(), c.getCreatedBy(), c.getCreatedAt(), groupResults)).collect(Collectors.toList());
            List<ProcessQuenchingData> lotQuenching = allQuenching.stream().filter(c -> matchesAnyHeader(c.getShift(), c.getLotNo(), c.getCreatedBy(), c.getCreatedAt(), groupResults)).collect(Collectors.toList());
            List<ProcessTemperingData> lotTempering = allTempering.stream().filter(c -> matchesAnyHeader(c.getShift(), c.getLotNo(), c.getCreatedBy(), c.getCreatedAt(), groupResults)).collect(Collectors.toList());
            List<ProcessTestingFinishingData> lotFinishing = allFinishing.stream().filter(c -> matchesAnyHeader(c.getShift(), c.getLotNo(), c.getCreatedBy(), c.getCreatedAt(), groupResults)).collect(Collectors.toList());
            List<ProcessFinalCheckData> lotFinalCheck = allFinalCheck.stream().filter(c -> matchesAnyHeader(c.getShift(), c.getLotNo(), c.getCreatedBy(), c.getCreatedAt(), groupResults)).collect(Collectors.toList());

            // Aggregate rejections from headers
            int shearingRej = groupResults.stream().mapToInt(r -> r.getShearingRejected() != null ? r.getShearingRejected() : 0).sum();
            int mpiRej = groupResults.stream().mapToInt(r -> r.getMpiRejected() != null ? r.getMpiRejected() : 0).sum();
            int forgingRej = groupResults.stream().mapToInt(r -> r.getForgingRejected() != null ? r.getForgingRejected() : 0).sum();
            int hardnessRej = groupResults.stream().mapToInt(r -> r.getHardnessCheckRejected() != null ? r.getHardnessCheckRejected() : 0).sum();
            int dimRej = groupResults.stream().mapToInt(r -> r.getDimensionsCheckRejected() != null ? r.getDimensionsCheckRejected() : 0).sum();

            List<ProcessInspectionRegisterRowDTO> rows = new ArrayList<>();

            rows.add(createRow(1, "Checking of Length of cut bars, random (3 bars/Hr.)", 
                mapHourly(lotShearing, d -> formatReadings(d.getLengthCutBar1(), d.getLengthCutBar2(), d.getLengthCutBar3())),
                shearingRej));

            rows.add(createRow(2, "Turning Length, random (3 bars/Hr.)", 
                mapHourly(lotTurning, d -> formatCombinedReadings(d.getStraightLength1(), d.getTaperLength1(), d.getStraightLength2(), d.getTaperLength2(), d.getStraightLength3(), d.getTaperLength3())),
                sumRejected(lotTurning, d -> (d.getParallelLengthRejected() != null ? d.getParallelLengthRejected() : 0) + (d.getFullTurningLengthRejected() != null ? d.getFullTurningLengthRejected() : 0))));

            rows.add(createRow(3, "Turning Dia, random (3 bars/Hr.)", 
                mapHourly(lotTurning, d -> formatReadings(d.getDia1(), d.getDia2(), d.getDia3())),
                sumRejected(lotTurning, d -> d.getTurningDiaRejected() != null ? d.getTurningDiaRejected() : 0)));

            rows.add(createRow(4, "MPI Test, random (3 bars/Hr.)", 
                mapHourly(lotMpi, d -> formatReadings(d.getTestResult1(), d.getTestResult2(), d.getTestResult3())),
                mpiRej));

            rows.add(createRow(5, "Forging Temp. (N/A)", mapHourly(null, null), forgingRej));

            // Fetch static periodic check to determine Row 6 display
            // Find any static check matching any of the headers
            Optional<ProcessStaticPeriodicCheck> staticCheck = Optional.empty();
            for (ProcessLineFinalResult r : groupResults) {
                staticCheck = staticPeriodicCheckRepository
                    .findByInspectionCallNoAndShiftAndLineNoAndLotNoAndCreatedByAndDateOfInspection(
                        callNo, r.getShift(), r.getLineNo(), r.getLotNumber(), r.getCreatedBy(), resultDate
                    );
                if (staticCheck.isPresent()) break;
            }
            if (!staticCheck.isPresent()) {
                staticCheck = staticPeriodicCheckRepository
                    .findFirstByInspectionCallNoAndLineNoOrderByCreatedAtDesc(callNo, primaryResult.getLineNo());
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

            rows.add(createRow(8, "Quenching Hardness (2 ERCs/Hr., Random)", 
                mapHourly(lotQuenching, d -> formatReadings(d.getQuenchingHardness1(), d.getQuenchingHardness2())),
                hardnessRej));

            rows.add(createRow(9, "Tempering Temp & Duration (100%)", 
                mapHourly(lotTempering, d -> formatCombinedReadings(d.getTemperingTemperature1(), d.getTemperingDuration1())),
                sumRejected(lotTempering, d -> (d.getTemperingTemperatureRejected() != null ? d.getTemperingTemperatureRejected() : 0) + (d.getTemperingDurationRejected() != null ? d.getTemperingDurationRejected() : 0))));

            rows.add(createRow(10, "Dimension Check (2 ERCs/Hr., Random)", 
                mapHourly(lotQuenching, d -> formatReadings(d.getBoxGauge1(), d.getFallingGauge1())),
                dimRej));

            rows.add(createRow(11, "Hardness of finished ERC (2 ERCs/Hr., Random)", 
                mapHourly(lotFinalCheck, d -> formatReadings(d.getTemperingHardness1(), d.getTemperingHardness2())),
                hardnessRej));

            rows.add(createRow(12, "Toe load of finished ERC (2 ERCs/Hr., Random)", 
                mapHourly(lotFinishing, d -> formatReadings(d.getToeLoad1(), d.getToeLoad2())),
                sumRejected(lotFinishing, ProcessTestingFinishingData::getToeLoadRejected)));

            // Row 13 Paint Logic
            List<String> row13Data = new ArrayList<>();
            for (int i = 1; i <= 8; i++) {
                final int hIdxFinal = i;
                final double minToe = ("MK-III".equalsIgnoreCase(ercType)) ? 850 : (("MK-V".equalsIgnoreCase(ercType)) ? 1200 : 0);
                final double maxToe = ("MK-III".equalsIgnoreCase(ercType)) ? 1100 : (("MK-V".equalsIgnoreCase(ercType)) ? 1500 : 0);

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
            rows.add(createRow(13, "Confirmation of Yellow and green paint on the end face of ERC MK-III & MK-V respectively (Cl. No. 6.1)", row13Data, row13RedCount));

            // Row 14 Documentation
            List<String> row14Data = new ArrayList<>();
            for (int i = 1; i <= 8; i++) {
                final int hIdxFinal = i;
                Optional<ProcessQuenchingData> qd = lotQuenching.stream().filter(d -> Integer.valueOf(hIdxFinal).equals(getHourIndex(d))).reduce((first, second) -> second);
                Optional<ProcessFinalCheckData> fcd = lotFinalCheck.stream().filter(d -> Integer.valueOf(hIdxFinal).equals(getHourIndex(d))).reduce((first, second) -> second);
                Optional<ProcessTestingFinishingData> tfd = lotFinishing.stream().filter(d -> Integer.valueOf(hIdxFinal).equals(getHourIndex(d))).reduce((first, second) -> second);

                boolean hasHourData = (qd.isPresent() && (qd.get().getBoxGauge1() != null || qd.get().getFallingGauge1() != null)) ||
                                      (fcd.isPresent() && (fcd.get().getTemperingHardness1() != null || fcd.get().getTemperingHardness2() != null)) ||
                                      (tfd.isPresent() && (tfd.get().getToeLoad1() != null || tfd.get().getToeLoad2() != null));

                if (!hasHourData) {
                    row14Data.add("-");
                } else {
                    row14Data.add("OK");
                }
            }
            rows.add(createRow(14, "Documentation (100%)", row14Data, "-"));

            dto.setRows(rows);
            
            // Sort rows safely before returning
            dto.getRows().sort(Comparator.comparing(ProcessInspectionRegisterRowDTO::getSrNo));
            responseList.add(dto);
        }
        
        // Sort response list by date, then shift, then lotNo
        responseList.sort(Comparator.comparing(ProcessInspectionRegisterResponseDTO::getDate)
                                    .thenComparing(ProcessInspectionRegisterResponseDTO::getShift)
                                    .thenComparing(ProcessInspectionRegisterResponseDTO::getLotNo));

        return responseList;
    }

    private boolean matchesAnyHeader(String shift, String lotNo, String createdBy, java.time.LocalDateTime createdAt, List<ProcessLineFinalResult> headers) {
        if (shift == null || lotNo == null || createdBy == null || createdAt == null) return false;
        for (ProcessLineFinalResult h : headers) {
            if (shift.equals(h.getShift()) && lotNo.equals(h.getLotNumber()) && createdBy.equals(h.getCreatedBy())) {
                if (h.getCreatedAt() != null) {
                    long diffMinutes = Math.abs(java.time.Duration.between(createdAt, h.getCreatedAt()).toMinutes());
                    if (diffMinutes <= 2) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    @Override
    public List<Map<String, Object>> getAvailableEntries(String callNo, String createdBy) {
        List<ProcessLineFinalResult> results = lineFinalResultRepository.findByInspectionCallNo(callNo);
        
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
        boolean allEmpty = hourlyData.stream().allMatch(h -> h == null || h.equals("-") || h.trim().isEmpty());
        
        if (allEmpty && totalRejected == 0) {
            row.setRemarks("No Production");
        } else if (totalReadings == 0 && totalRejected == 0) {
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

        boolean allEmpty = hourlyData.stream().allMatch(h -> h == null || h.equals("-") || h.trim().isEmpty());
        if (allEmpty && (remarks == null || remarks.equals("-") || remarks.trim().isEmpty())) {
            row.setRemarks("No Production");
        } else {
            row.setRemarks(remarks);
        }

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
