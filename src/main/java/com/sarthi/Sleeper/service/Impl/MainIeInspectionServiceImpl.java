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
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class MainIeInspectionServiceImpl implements MainIeInspectionService {

    @Autowired
    private SleeperInspectionCallRepository inspectionCallRepository;

    @Autowired
    private ProductionDeclarationRepository productionDeclarationRepository;

    @Autowired
    private com.sarthi.Sleeper.repository.DemouldingDefectiveSleeperRepository demouldingDefectiveSleeperRepository;

    @Autowired
    private com.sarthi.Sleeper.repository.FInalCallRepo.SleeperFinalResultRepository sleeperFinalResultRepository;

    @Autowired(required = false)
    private com.sarthi.Sleeper.repository.EtSleeperDetailsRepository etSleeperDetailsRepository;

    @Autowired(required = false)
    private com.sarthi.Sleeper.repository.MfTestDetailsRepository mfTestDetailsRepository;

    @Autowired(required = false)
    private com.sarthi.Sleeper.repository.ModulusOfFailureRepository modulusOfFailureRepository;

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

        // If sleeper final result exists, prioritize its verified inspection totals
        sleeperFinalResultRepository.findByCallNumber(callNo).ifPresent(sfr -> {
            if (dto.getQtyOfferedNow() == null || dto.getQtyOfferedNow() == 0) {
                if (sfr.getTotalOfferedQuantity() != null) {
                    dto.setQtyOfferedNow(sfr.getTotalOfferedQuantity().intValue());
                }
            }
            if (sfr.getTotalAccepted() != null) {
                dto.setTotalAccepted(sfr.getTotalAccepted().intValue());
            }
            if (sfr.getTotalRejected() != null) {
                dto.setTotalRejected(sfr.getTotalRejected().intValue());
            }
        });

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

        List<SleeperInspectionCallBatch> batches = call.getBatchesSelected() != null ? call.getBatchesSelected() : Collections.emptyList();
        Set<String> batchNos = batches.stream()
                .map(SleeperInspectionCallBatch::getBatchNo)
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toSet());

        // Fetch all relevant declarations for the call's batches
        List<ProductionDeclaration> allDeclarations = new ArrayList<>();
        if (!batchNos.isEmpty()) {
            if (call.getPlantId() != null && !call.getPlantId().trim().isEmpty()) {
                allDeclarations.addAll(productionDeclarationRepository.findAllByBatchNumbersAndPlantId(batchNos, call.getPlantId().trim()));
            }
            if (allDeclarations.isEmpty()) {
                allDeclarations.addAll(productionDeclarationRepository.findAllByBatchNumbers(batchNos));
            }
        }

        Map<String, List<ProductionDeclaration>> declListMap = new HashMap<>();
        for (ProductionDeclaration pd : allDeclarations) {
            if (pd.getBatchNumber() != null) {
                declListMap.computeIfAbsent(pd.getBatchNumber().trim(), k -> new ArrayList<>()).add(pd);
            }
        }

        Set<String> allBatchNames = new HashSet<>();
        for (String b : batchNos) {
            allBatchNames.add(b);
            String clean = b.replaceAll("(?i)^batch\\s*[-_:]*\\s*", "").trim();
            allBatchNames.add(clean);
            allBatchNames.add("Batch " + clean);
            allBatchNames.add("BATCH " + clean);
        }

        // Preload ET sleepers by batch
        Map<String, List<String>> etSleepersByBatch = new HashMap<>();
        if (!allBatchNames.isEmpty() && etSleeperDetailsRepository != null) {
            try {
                List<com.sarthi.Sleeper.entity.EtSleeperDetails> etList = etSleeperDetailsRepository.findByEt_BatchNumberIn(allBatchNames);
                if (etList != null) {
                    for (com.sarthi.Sleeper.entity.EtSleeperDetails et : etList) {
                        if (et != null && et.getEt() != null && et.getEt().getBatchNumber() != null && et.getSleeperNo() != null) {
                            String bNo = et.getEt().getBatchNumber().trim();
                            etSleepersByBatch.computeIfAbsent(bNo, k -> new ArrayList<>()).add(et.getSleeperNo().trim());
                            String cleanBNo = bNo.replaceAll("(?i)^batch\\s*[-_:]*\\s*", "").trim();
                            etSleepersByBatch.computeIfAbsent(cleanBNo, k -> new ArrayList<>()).add(et.getSleeperNo().trim());
                        }
                    }
                }
            } catch (Exception ignore) {}
        }

        // Preload MF tested sleepers by batch
        Map<String, List<String>> mfSleepersByBatch = new HashMap<>();
        if (!allBatchNames.isEmpty()) {
            if (mfTestDetailsRepository != null) {
                try {
                    List<com.sarthi.Sleeper.entity.MfTestDetails> mfTests = mfTestDetailsRepository.findByBatchNumbersIn(allBatchNames);
                    if (mfTests != null) {
                        for (com.sarthi.Sleeper.entity.MfTestDetails t : mfTests) {
                            if (t != null && t.getBatchNo() != null && t.getSampleIdentification() != null && !t.getSampleIdentification().isBlank()) {
                                String bNo = t.getBatchNo().trim();
                                String sNo = cleanMfSleeperNo(t.getSampleIdentification());
                                if (!sNo.isBlank()) {
                                    mfSleepersByBatch.computeIfAbsent(bNo, k -> new ArrayList<>()).add(sNo);
                                    String cleanBNo = bNo.replaceAll("(?i)^batch\\s*[-_:]*\\s*", "").trim();
                                    mfSleepersByBatch.computeIfAbsent(cleanBNo, k -> new ArrayList<>()).add(sNo);
                                }
                            }
                        }
                    }
                } catch (Exception ignore) {}
            }
            if (modulusOfFailureRepository != null) {
                try {
                    List<com.sarthi.Sleeper.entity.ModulusOfFailure> mfs = modulusOfFailureRepository.findByBatchNumbersIn(allBatchNames);
                    if (mfs != null) {
                        for (com.sarthi.Sleeper.entity.ModulusOfFailure m : mfs) {
                            if (m != null && m.getBatchNo() != null && m.getSampleIdentification() != null && !m.getSampleIdentification().isBlank()) {
                                String bNo = m.getBatchNo().trim();
                                String sNo = cleanMfSleeperNo(m.getSampleIdentification());
                                if (!sNo.isBlank()) {
                                    mfSleepersByBatch.computeIfAbsent(bNo, k -> new ArrayList<>()).add(sNo);
                                    String cleanBNo = bNo.replaceAll("(?i)^batch\\s*[-_:]*\\s*", "").trim();
                                    mfSleepersByBatch.computeIfAbsent(cleanBNo, k -> new ArrayList<>()).add(sNo);
                                }
                            }
                        }
                    }
                } catch (Exception ignore) {}
            }
        }

        List<SleeperInspectionBatchDetailDTO> response = new ArrayList<>();

        for (SleeperInspectionCallBatch batch : batches) {

            SleeperInspectionBatchDetailDTO dto = new SleeperInspectionBatchDetailDTO();

            String batchNo = batch.getBatchNo();
            dto.setBatchNo(batchNo);

            List<ProductionDeclaration> candidates = (batchNo != null) ? declListMap.getOrDefault(batchNo.trim(), Collections.emptyList()) : Collections.emptyList();
            
            Set<String> batchGoodSleeperNos = (batch.getGoodSleepers() != null) 
                    ? batch.getGoodSleepers().stream()
                        .map(SleeperDetail::getSleeperNo)
                        .filter(s -> s != null && !s.isBlank())
                        .map(String::trim)
                        .collect(Collectors.toSet())
                    : Collections.emptySet();

            // Find best matching declaration by offered sleepers or casting date or max sleepers
            ProductionDeclaration declaration = null;
            if (!candidates.isEmpty()) {
                if (!batchGoodSleeperNos.isEmpty()) {
                    for (ProductionDeclaration cand : candidates) {
                        if (cand.getChambers() != null) {
                            boolean hasMatch = cand.getChambers().stream()
                                .flatMap(c -> c.getBenchGroups() != null ? c.getBenchGroups().stream() : java.util.stream.Stream.empty())
                                .flatMap(bg -> bg.getSleepers() != null ? bg.getSleepers().stream() : java.util.stream.Stream.empty())
                                .anyMatch(s -> s.getSleeperNo() != null && batchGoodSleeperNos.contains(s.getSleeperNo().trim()));
                            if (hasMatch) {
                                declaration = cand;
                                break;
                            }
                        }
                    }
                }
                if (declaration == null && batch.getCastDate() != null) {
                    for (ProductionDeclaration cand : candidates) {
                        if (cand.getCastingDate() != null && cand.getCastingDate().toString().equals(batch.getCastDate())) {
                            declaration = cand;
                            break;
                        }
                    }
                }
                if (declaration == null && call.getPoNo() != null) {
                    for (ProductionDeclaration cand : candidates) {
                        if (call.getPoNo().trim().equalsIgnoreCase(cand.getPoNo())) {
                            declaration = cand;
                            break;
                        }
                    }
                }
                if (declaration == null) {
                    declaration = candidates.stream()
                        .max(Comparator.comparingInt(d -> d.getTotalCastedSleepers() != null ? d.getTotalCastedSleepers() : 0))
                        .orElse(candidates.get(0));
                }
            }

            if (declaration != null && declaration.getCastingDate() != null) {
                dto.setCastingDate(declaration.getCastingDate().toString());
            } else if (batch.getCastDate() != null) {
                dto.setCastingDate(batch.getCastDate());
            } else {
                dto.setCastingDate("N/A");
            }

            int chamberSleeperCount = (declaration != null && declaration.getChambers() != null)
                    ? (int) declaration.getChambers().stream()
                        .flatMap(c -> c.getBenchGroups() != null ? c.getBenchGroups().stream() : java.util.stream.Stream.empty())
                        .flatMap(bg -> bg.getSleepers() != null ? bg.getSleepers().stream() : java.util.stream.Stream.empty())
                        .filter(s -> s.getSleeperNo() != null && !s.getSleeperNo().isBlank())
                        .count()
                    : 0;

            Integer totalCasted = (declaration != null && declaration.getTotalCastedSleepers() != null)
                    ? declaration.getTotalCastedSleepers()
                    : (batch.getTotalCasted() != null ? batch.getTotalCasted() : 0);

            if (chamberSleeperCount > totalCasted) {
                totalCasted = chamberSleeperCount;
            }
            if (batch.getTotalCasted() != null && batch.getTotalCasted() > totalCasted) {
                totalCasted = batch.getTotalCasted();
            }

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

            dto.setAcceptedSleepers(accepted);
            dto.setRejectedSleepers(rejected);

            int passed = accepted.size();
            int rejectedCount = rejected.size();

            dto.setPassed(passed);
            dto.setRejected(rejectedCount);

            int offeredNow = passed + rejectedCount;
            dto.setOfferedNow(offeredNow);

            if (totalCasted == null || totalCasted < offeredNow) {
                totalCasted = offeredNow;
            }
            dto.setTotalSleepersCasted(totalCasted);

            int unoffered = totalCasted - offeredNow;
            dto.setUnoffered(Math.max(0, unoffered));

            String rawBatchNo = batchNo != null ? batchNo.trim() : "";
            String cleanBatch = rawBatchNo.replaceAll("(?i)^batch\\s*[-_:]*\\s*", "").trim();

            Set<String> etDistinct = new java.util.LinkedHashSet<>();
            if (etSleepersByBatch.containsKey(rawBatchNo)) etDistinct.addAll(etSleepersByBatch.get(rawBatchNo));
            if (etSleepersByBatch.containsKey(cleanBatch)) etDistinct.addAll(etSleepersByBatch.get(cleanBatch));
            dto.setEtSleepers(new ArrayList<>(etDistinct));

            Set<String> mfDistinct = new java.util.LinkedHashSet<>();
            if (mfSleepersByBatch.containsKey(rawBatchNo)) mfDistinct.addAll(mfSleepersByBatch.get(rawBatchNo));
            if (mfSleepersByBatch.containsKey(cleanBatch)) mfDistinct.addAll(mfSleepersByBatch.get(cleanBatch));
            dto.setMfSleepers(new ArrayList<>(mfDistinct));

            response.add(dto);
        }

        return response;
    }

    private List<String> generateTurnoutSleepers(String sleeperType, int count) {
        List<String> list = new ArrayList<>();
        // Approach
        if (sleeperType != null && sleeperType.contains("RT-9841")) {
            list.addAll(List.of("60S", "60-4A", "60-3A", "60-2AS", "60-1AS"));
        } else if (sleeperType != null && (sleeperType.contains("RT-4218") || sleeperType.contains("RT-4865") || sleeperType.contains("RT-6068") || sleeperType.contains("RT-5691"))) {
            list.addAll(List.of("60S", "1AS", "2AS", "3A", "4A"));
        } else {
            // Default 1 in 12 PnC: RT-9790
            list.addAll(List.of("60S", "60-4A", "60-3A", "60-2AS", "60-1AS"));
        }
        
        // Turnout body
        int maxBody = sleeperType != null && (sleeperType.contains("RT-4865") || sleeperType.contains("RT-9841")) ? 54 : (sleeperType != null && sleeperType.contains("RT-6068") ? 22 : (sleeperType != null && sleeperType.contains("RT-5691") ? 101 : 83));
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

    private String cleanMfSleeperNo(String s) {
        if (s == null || s.isBlank()) return "";
        String trimmed = s.trim();
        if (trimmed.contains("+")) {
            String[] parts = trimmed.split("\\+");
            String bench = "";
            String mould = "";
            for (String p : parts) {
                String clean = p.trim();
                if (clean.matches("(?i)^shed.*")) continue;
                if (clean.matches("^\\d+$")) bench = clean;
                else if (clean.matches("^[a-zA-Z]$")) mould = clean.toUpperCase();
                else if (clean.matches("^\\d+[a-zA-Z]+$")) return clean.toUpperCase();
            }
            if (!bench.isEmpty() || !mould.isEmpty()) return bench + mould;
        }
        return trimmed;
    }
}
