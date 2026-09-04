package com.sarthi.Sleeper.service.Impl;

import com.sarthi.Sleeper.dto.MainIeInspectionDtos.SleeperInspectionBatchDetailDTO;
import com.sarthi.Sleeper.dto.MainIeInspectionDtos.SleeperInspectionCallSummaryDTO;
import com.sarthi.Sleeper.entity.FinalInspection.SleeperInspectionCall;
import com.sarthi.Sleeper.entity.FinalInspection.SleeperInspectionCallBatch;
import com.sarthi.Sleeper.entity.FinalInspection.SleeperDetail;
import com.sarthi.Sleeper.entity.ProductionDeclaration.ProductionDeclaration;
import com.sarthi.Sleeper.repository.FinalInspectionRepository.SleeperInspectionCallRepository;
import com.sarthi.Sleeper.repository.ProductionDeclaration.ProductionDeclarationRepository;
import com.sarthi.Sleeper.service.MainIeInspectionService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class MainIeInspectionServiceImpl implements MainIeInspectionService {

    @Autowired
    private SleeperInspectionCallRepository inspectionCallRepository;

    @Autowired
    private ProductionDeclarationRepository productionDeclarationRepository;

    @Autowired
    private com.sarthi.Sleeper.repository.DemouldingDefectiveSleeperRepository demouldingDefectiveSleeperRepository;
    @Override
    public SleeperInspectionCallSummaryDTO getInspectionCallSummary(String callNo) {

        SleeperInspectionCall call = inspectionCallRepository
                .findByCallNo(callNo)
                .orElseThrow(() -> new RuntimeException("Call not found"));

        SleeperInspectionCallSummaryDTO dto = new SleeperInspectionCallSummaryDTO();

        dto.setPoNo(call.getPoNo());
        dto.setSrNo(call.getSrNo());

        dto.setSleeperType(call.getSleeperType());

        // Call Date
        if (call.getCreatedAt() != null) {
            dto.setCallDate(call.getCreatedAt().toLocalDate().toString());
        }

        // Desired Inspection Date
        if (call.getDesiredInspectionDate() != null) {
            dto.setDesiredInspectionDate(call.getDesiredInspectionDate().toString());
        }

        // Qty Offered Now
        dto.setQtyOfferedNow(call.getTotalOffered());

        // No of batches
        int batchCount = call.getBatchesSelected() != null ?
                call.getBatchesSelected().size() : 0;

        dto.setNoOfBatches(batchCount);

        // Total Rejected
        dto.setTotalRejected(call.getTotalRejected());

        // 🔥 Calculate Accepted
        int accepted = 0;

        if (call.getBatchesSelected() != null) {
            for (SleeperInspectionCallBatch batch : call.getBatchesSelected()) {

                if (batch.getGoodSleepers() != null) {
                    accepted += batch.getGoodSleepers().size();
                }
            }
        }

        dto.setTotalAccepted(accepted);

        // Optional fields
        dto.setQuantityOnOrder(null);
        dto.setCumulativeQtyOffered(0);
        dto.setCumulativeQtyPassed(0);

        // ET sleepers (as per requirement)
        dto.setNoOfEtSleepers(null);

        return dto;
    }

    @Override
    public List<SleeperInspectionBatchDetailDTO> getBatchWiseDetails(String callNo) {

        SleeperInspectionCall call = inspectionCallRepository
                .findByCallNo(callNo)
                .orElseThrow(() -> new RuntimeException("Call not found"));

        List<SleeperInspectionBatchDetailDTO> response = new ArrayList<>();

        for (SleeperInspectionCallBatch batch : call.getBatchesSelected()) {

            SleeperInspectionBatchDetailDTO dto = new SleeperInspectionBatchDetailDTO();

            String batchNo = batch.getBatchNo();
            dto.setBatchNo(batchNo);


            ProductionDeclaration declaration =
                    productionDeclarationRepository.findByBatchNumber(batchNo);

            dto.setCastingDate(declaration.getCastingDate().toString());
            Integer totalCasted = declaration.getTotalCastedSleepers();
            dto.setTotalSleepersCasted(totalCasted);

            List<String> accepted = (batch.getGoodSleepers() != null && !batch.getGoodSleepers().isEmpty())
                    ? batch.getGoodSleepers().stream()
                        .map(SleeperDetail::getSleeperNo)
                        .filter(s -> s != null && !s.isBlank() && !s.trim().equals("0"))
                        .sorted((a, b) -> {
                            try {
                                String numA = a.replaceAll("\\D", "");
                                String numB = b.replaceAll("\\D", "");
                                if (!numA.isEmpty() && !numB.isEmpty()) {
                                    return Long.compare(Long.parseLong(numA), Long.parseLong(numB));
                                }
                                return a.compareTo(b);
                            } catch (Exception e) {
                                return a.compareTo(b);
                            }
                        })
                        .collect(Collectors.toList())
                    : (declaration != null && declaration.getChambers() != null
                        ? declaration.getChambers().stream()
                            .flatMap(c -> c.getBenchGroups() != null ? c.getBenchGroups().stream() : java.util.stream.Stream.empty())
                            .flatMap(bg -> bg.getSleepers() != null ? bg.getSleepers().stream() : java.util.stream.Stream.empty())
                            .map(com.sarthi.Sleeper.entity.ProductionDeclaration.ProductionSleeper::getSleeperNo)
                            .filter(s -> s != null && !s.isBlank() && !s.trim().equals("0"))
                            .sorted((a, b) -> {
                                try {
                                    String numA = a.replaceAll("\\D", "");
                                    String numB = b.replaceAll("\\D", "");
                                    if (!numA.isEmpty() && !numB.isEmpty()) {
                                        return Long.compare(Long.parseLong(numA), Long.parseLong(numB));
                                    }
                                    return a.compareTo(b);
                                } catch (Exception e) {
                                    return a.compareTo(b);
                                }
                            })
                            .collect(Collectors.toList())
                        : new ArrayList<>());

            List<String> rejected = batch.getBadSleepers() != null
                    ? batch.getBadSleepers().stream()
                        .map(SleeperDetail::getSleeperNo)
                        .filter(s -> s != null && !s.isBlank() && !s.trim().equals("0"))
                        .sorted((a, b) -> {
                            try {
                                String numA = a.replaceAll("\\D", "");
                                String numB = b.replaceAll("\\D", "");
                                if (!numA.isEmpty() && !numB.isEmpty()) {
                                    return Long.compare(Long.parseLong(numA), Long.parseLong(numB));
                                }
                                return a.compareTo(b);
                            } catch (Exception e) {
                                return a.compareTo(b);
                            }
                        })
                        .collect(Collectors.toList())
                    : new ArrayList<>();

            boolean isTurnout = (call.getSleeperType() != null && (call.getSleeperType().contains("PnC") || call.getSleeperType().contains("RT-9790") || call.getSleeperType().contains("RT-4218") || call.getSleeperType().contains("RT-4865") || call.getSleeperType().contains("Turnout"))) || (batchNo != null && batchNo.toUpperCase().startsWith("TO"));

            if (isTurnout) {
                boolean hasWrongLineNumbers = accepted.isEmpty() || accepted.stream().anyMatch(s -> s.matches("^\\d{5,}$")) || rejected.stream().anyMatch(s -> s.matches("^\\d{5,}$"));
                if (hasWrongLineNumbers) {
                    List<String> turnoutList = generateTurnoutSleepers(call.getSleeperType(), totalCasted != null && totalCasted > 0 ? totalCasted : 62);
                    List<String> mappedRejected = new ArrayList<>();
                    if (!rejected.isEmpty()) {
                        for (int i = 0; i < rejected.size() && i < turnoutList.size(); i++) {
                            String code = rejected.get(i);
                            if (turnoutList.contains(code)) {
                                mappedRejected.add(code);
                            } else {
                                mappedRejected.add(turnoutList.get(i));
                            }
                        }
                    }
                    java.util.Set<String> rejSet = new java.util.HashSet<>(mappedRejected);
                    accepted = turnoutList.stream().filter(s -> !rejSet.contains(s)).collect(Collectors.toList());
                    rejected = mappedRejected;
                }
            }

            // Query Demoulding Rejections for this batch
            if (demouldingDefectiveSleeperRepository != null) {
                try {
                    java.util.Set<String> demouldingRej = demouldingDefectiveSleeperRepository.findAllRejectedSleeperNosByBatchNo(batchNo);
                    if (demouldingRej != null && !demouldingRej.isEmpty()) {
                        for (String dr : demouldingRej) {
                            if (dr != null && !dr.isBlank()) {
                                String cleanDr = dr.trim();
                                if (!rejected.contains(cleanDr)) {
                                    rejected.add(cleanDr);
                                    accepted.remove(cleanDr);
                                }
                            }
                        }
                    }
                } catch (Exception e) {
                    System.err.println("Error loading demoulding rejections for batch " + batchNo + ": " + e.getMessage());
                }
            }

            dto.setAcceptedSleepers(accepted);
            dto.setRejectedSleepers(rejected);

            int passed = accepted.size();
            int rejectedCount = rejected.size();

            dto.setPassed(passed);
            dto.setRejected(rejectedCount);

            int offeredNow = passed + rejectedCount;
            dto.setOfferedNow(offeredNow);

            int unoffered = (totalCasted != null ? totalCasted : offeredNow) - offeredNow;
            dto.setUnoffered(Math.max(0, unoffered));

            dto.setEtSleepers(null);

            response.add(dto);
        }

        return response;
    }

    private List<String> generateTurnoutSleepers(String sleeperType, int count) {
        List<String> list = new ArrayList<>();
        // Approach
        if (sleeperType != null && sleeperType.contains("RT-9841")) {
            list.addAll(List.of("90S", "90-4A", "90-3A", "90-2AS"));
        } else if (sleeperType != null && (sleeperType.contains("RT-4218") || sleeperType.contains("RT-4865") || sleeperType.contains("RT-6068") || sleeperType.contains("RT-5691"))) {
            list.addAll(List.of("60S", "1AS", "2AS", "3A", "4A"));
        } else {
            // Default 1 in 12 PnC: RT-9790
            list.addAll(List.of("60S", "60-4A", "60-3A", "60-2AS", "60-1AS"));
        }
        
        // Turnout body
        int maxBody = sleeperType != null && sleeperType.contains("RT-4865") ? 54 : (sleeperType != null && sleeperType.contains("RT-6068") ? 22 : (sleeperType != null && sleeperType.contains("RT-5691") ? 101 : 83));
        for (int i = 1; i <= maxBody; i++) {
            list.add(String.valueOf(i));
        }

        // Exit
        list.addAll(List.of("1E", "2E", "3E", "4E"));

        if (count > 0 && count < list.size()) {
            return new ArrayList<>(list.subList(0, count));
        }
        return list;
    }
}
