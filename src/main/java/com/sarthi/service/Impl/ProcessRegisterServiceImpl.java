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
    private com.sarthi.repository.rawmaterial.InspectionCallRepository inspectionCallRepository;

    @Override
    public List<ProcessInspectionRegisterResponseDTO> getProcessInspectionRegister(String callNo, String date, String shift, String createdBy) {
        
        // 1. Fetch available lots/lines for this call, date, shift, and user
        List<ProcessLineFinalResult> finalResults;
        
        if (date != null && !date.isEmpty() && shift != null && !shift.isEmpty()) {
            LocalDate inspectionDate = LocalDate.parse(date);
            finalResults = lineFinalResultRepository
                    .findByInspectionCallNoAndDateOfInspectionAndShiftAndCreatedBy(callNo, inspectionDate, shift, createdBy);
        } else {
            finalResults = lineFinalResultRepository.findByInspectionCallNoAndCreatedBy(callNo, createdBy);
        }

        List<ProcessInspectionRegisterResponseDTO> responseList = new ArrayList<>();

        for (ProcessLineFinalResult result : finalResults) {
            ProcessInspectionRegisterResponseDTO dto = new ProcessInspectionRegisterResponseDTO();
            String dateStr = result.getDateOfInspection() != null 
                ? result.getDateOfInspection().format(DateTimeFormatter.ofPattern("dd-MM-yyyy"))
                : (result.getCreatedAt() != null ? result.getCreatedAt().format(DateTimeFormatter.ofPattern("dd-MM-yyyy")) : "");
            dto.setDate(dateStr);
            dto.setShift(result.getShift());
            dto.setLotNo(result.getLotNumber());
            dto.setErcProducedDuringShift(result.getTotalManufactured());

            // Fetch PO Info
            Optional<MainPoInformation> poInfo = mainPoRepository.findByInspectionCallNo(callNo);
            if (poInfo.isPresent()) {
                String poDateStr = poInfo.get().getPoDate() != null ? poInfo.get().getPoDate().format(DateTimeFormatter.ofPattern("dd-MM-yyyy")) : "";
                dto.setPoNoAndDate(poInfo.get().getPoNo() + " & " + poDateStr);
            }

            // Fetch Inspecting Engineer Name
            try {
                Integer userId = Integer.parseInt(createdBy);
                Optional<UserMaster> user = userMasterRepository.findByUserId(userId);
                dto.setInspectingEngineerName(user.map(UserMaster::getFullName).orElse(createdBy));
            } catch(NumberFormatException e) {
                Optional<UserMaster> user = userMasterRepository.findByUserName(createdBy);
                dto.setInspectingEngineerName(user.map(UserMaster::getFullName).orElse(createdBy));
            }

            // Fetch RM IC Info & Date
            Optional<InspectionCallDetails> callDetails = callDetailsRepository.findFirstByInspectionCallNoOrderByIdDesc(callNo);
            String rmIcNumber = callDetails.map(InspectionCallDetails::getRmIcNumber).orElse("-");
            
            Optional<com.sarthi.entity.InspectionCompleteDetails> completeDetails = inspectionCompleteDetailsRepository.findFirstByCallNoOrderByCreatedOnDesc(callNo);
            String icDate = completeDetails.map(c -> c.getCreatedOn() != null ? c.getCreatedOn().format(DateTimeFormatter.ofPattern("dd-MM-yyyy")) : "").orElse("-");
            
            dto.setRawMaterialIcNoAndDate(rmIcNumber + " & " + icDate);
            dto.setCaseNoIbs(callNo); // Using Call No as Case No if not specifically stored

            // Fetch ERC Type from Inspection Call
            Optional<com.sarthi.entity.rawmaterial.InspectionCall> icDetails = inspectionCallRepository.findByIcNumber(callNo);
            dto.setErcType(icDetails.map(com.sarthi.entity.rawmaterial.InspectionCall::getErcType).orElse("-"));

            // Hour Labels
            dto.setHourLabels(getHourLabelsForShift(result.getShift()));

            // Fetch all process data for this lot
            String currentShift = result.getShift();
            List<ProcessShearingData> shearingData = shearingRepository.findByInspectionCallNoAndShiftAndLotNoAndCreatedBy(callNo, currentShift, result.getLotNumber(), createdBy);
            List<ProcessTurningData> turningData = turningRepository.findByInspectionCallNoAndShiftAndLotNoAndCreatedBy(callNo, currentShift, result.getLotNumber(), createdBy);
            List<ProcessMpiData> mpiData = mpiRepository.findByInspectionCallNoAndShiftAndLotNoAndCreatedBy(callNo, currentShift, result.getLotNumber(), createdBy);
            List<ProcessQuenchingData> quenchingData = quenchingRepository.findByInspectionCallNoAndShiftAndLotNoAndCreatedBy(callNo, currentShift, result.getLotNumber(), createdBy);
            List<ProcessTemperingData> temperingData = temperingRepository.findByInspectionCallNoAndShiftAndLotNoAndCreatedBy(callNo, currentShift, result.getLotNumber(), createdBy);
            List<ProcessTestingFinishingData> finishingData = testingFinishingRepository.findByInspectionCallNoAndShiftAndLotNoAndCreatedBy(callNo, currentShift, result.getLotNumber(), createdBy);
            List<ProcessFinalCheckData> finalCheckData = finalCheckRepository.findByInspectionCallNoAndShiftAndLotNoAndCreatedBy(callNo, currentShift, result.getLotNumber(), createdBy);

            List<ProcessInspectionRegisterRowDTO> rows = new ArrayList<>();

            // Row 1: Shearing (Length cut bars)
            rows.add(createRow(1, "Checking of Length of cut bars, random (3 bars/Hr.)", 
                mapHourly(shearingData, d -> formatReadings(d.getLengthCutBar1(), d.getLengthCutBar2(), d.getLengthCutBar3())),
                result.getShearingRejected() != null ? result.getShearingRejected() : 0));

            // Row 2: Turning Length (Straight & Taper Length)
            rows.add(createRow(2, "Turning Length, random (3 bars/Hr.)", 
                mapHourly(turningData, d -> formatCombinedReadings(d.getStraightLength1(), d.getTaperLength1(), d.getStraightLength2(), d.getTaperLength2(), d.getStraightLength3(), d.getTaperLength3())),
                sumRejected(turningData, d -> {
                    int rej = 0;
                    if (d.getParallelLengthRejected() != null) rej += d.getParallelLengthRejected();
                    if (d.getFullTurningLengthRejected() != null) rej += d.getFullTurningLengthRejected();
                    return rej;
                })));

            // Row 3: Turning Dia
            rows.add(createRow(3, "Turning Dia, random (3 bars/Hr.)", 
                mapHourly(turningData, d -> formatReadings(d.getDia1(), d.getDia2(), d.getDia3())),
                sumRejected(turningData, d -> d.getTurningDiaRejected() != null ? d.getTurningDiaRejected() : 0)));

            // Row 4: MPI Test
            rows.add(createRow(4, "MPI Test, random (3 bars/Hr.)", 
                mapHourly(mpiData, d -> formatReadings(d.getTestResult1(), d.getTestResult2(), d.getTestResult3())),
                result.getMpiRejected() != null ? result.getMpiRejected() : 0));

            // Row 5: Forging Temp (N/A)
            rows.add(createRow(5, "Forging Temp. (N/A)", mapHourly(null, null), 
                result.getForgingRejected() != null ? result.getForgingRejected() : 0));

            // Row 6: Checking of Die (N/A for now or map if data exists)
            rows.add(createRow(6, "Checking of Die (100%)", mapHourly(null, null), "-"));

            // Row 7: Quenching Temp & Duration
            rows.add(createRow(7, "Quenching Temp & Duration (100%)", 
                mapHourly(quenchingData, d -> formatCombinedReadings(d.getQuenchingTemperature1(), d.getQuenchingDuration1())),
                sumRejected(quenchingData, d -> {
                    int rej = 0;
                    if (d.getQuenchingTemperatureRejected() != null) rej += d.getQuenchingTemperatureRejected();
                    if (d.getQuenchingDurationRejected() != null) rej += d.getQuenchingDurationRejected();
                    return rej;
                })));

            // Row 8: Quenching Hardness
            rows.add(createRow(8, "Quenching Hardness (2 ERCs/Hr.)", 
                mapHourly(quenchingData, d -> formatReadings(d.getQuenchingHardness1(), d.getQuenchingHardness2())),
                result.getHardnessCheckRejected() != null ? result.getHardnessCheckRejected() : 0));

            // Row 9: Tempering Temp & Duration
            rows.add(createRow(9, "Tempering Temp & Duration (100%)", 
                mapHourly(temperingData, d -> formatCombinedReadings(d.getTemperingTemperature1(), d.getTemperingDuration1())),
                sumRejected(temperingData, d -> {
                    int rej = 0;
                    if (d.getTemperingTemperatureRejected() != null) rej += d.getTemperingTemperatureRejected();
                    if (d.getTemperingDurationRejected() != null) rej += d.getTemperingDurationRejected();
                    return rej;
                })));

            // Row 10: Dimension Check
            rows.add(createRow(10, "Dimension Check (2 ERCs/Hr.)", 
                mapHourly(quenchingData, d -> formatReadings(d.getBoxGauge1(), d.getFallingGauge1())),
                result.getDimensionsCheckRejected() != null ? result.getDimensionsCheckRejected() : 0));

            // Row 11: Hardness of finished ERC
            rows.add(createRow(11, "Hardness of finished ERC (2 ERCs/Hr.)", 
                mapHourly(finalCheckData, d -> formatReadings(d.getTemperingHardness1(), d.getTemperingHardness2())),
                result.getHardnessCheckRejected() != null ? result.getHardnessCheckRejected() : 0));

            // Row 12: Toe load of finished ERC
            rows.add(createRow(12, "Toe load of finished ERC (2 ERCs/Hr.)", 
                mapHourly(finishingData, d -> formatReadings(d.getToeLoad1(), d.getToeLoad2())),
                sumRejected(finishingData, ProcessTestingFinishingData::getToeLoadRejected)));

            // Row 13: Confirmation of yellow and green paint
            rows.add(createRow(13, "Confirmation of yellow and green paint", 
                mapHourly(finishingData, d -> formatReadings(d.getPaintIdentification1(), d.getPaintIdentification2())),
                result.getVisualCheckRejected() != null ? result.getVisualCheckRejected() : 0));

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
}
