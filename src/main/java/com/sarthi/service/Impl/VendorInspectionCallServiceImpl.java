package com.sarthi.service.Impl;

import com.sarthi.dto.VendorInspectionCallStatusDto;
import com.sarthi.entity.WorkflowTransition;
import com.sarthi.entity.rawmaterial.InspectionCall;
import com.sarthi.entity.rawmaterial.RmInspectionDetails;
import com.sarthi.entity.processmaterial.ProcessInspectionDetails;
import com.sarthi.entity.finalmaterial.FinalInspectionDetails;
import com.sarthi.entity.PoHeader;
import com.sarthi.entity.UserMaster;
import com.sarthi.repository.WorkflowTransitionRepository;
import com.sarthi.repository.rawmaterial.InspectionCallRepository;
import com.sarthi.repository.finalmaterial.FinalInspectionLotDetailsRepository;
import com.sarthi.repository.finalmaterial.FinalCumulativeResultsRepository;
import com.sarthi.repository.RmHeatFinalResultRepository;
import com.sarthi.repository.processmaterial.ProcessLineFinalResultRepository;
import com.sarthi.repository.PoHeaderRepository;
import com.sarthi.repository.UserMasterRepository;
import com.sarthi.repository.rawmaterial.RmHeatQuantityRepository;
import com.sarthi.repository.InventoryEntryRepository;
import com.sarthi.service.AzureBlobStorageService;
import com.sarthi.service.VendorInspectionCallService;
import com.sarthi.exception.BusinessException;
import com.sarthi.exception.ErrorDetails;
import com.sarthi.constant.AppConstant;
import com.lowagie.text.Document;
import com.lowagie.text.pdf.PdfCopy;
import com.lowagie.text.pdf.PdfReader;
import java.io.ByteArrayOutputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Service implementation for Vendor Inspection Call operations.
 */
@Service
public class VendorInspectionCallServiceImpl implements VendorInspectionCallService {

    private static final Logger logger = LoggerFactory.getLogger(VendorInspectionCallServiceImpl.class);
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @Autowired
    private InspectionCallRepository inspectionCallRepository;

    @Autowired
    private WorkflowTransitionRepository workflowTransitionRepository;

    @Autowired
    private PoHeaderRepository poHeaderRepository;

    @Autowired
    private UserMasterRepository userMasterRepository;

    @Autowired
    private RmHeatQuantityRepository rmHeatQuantityRepository;

    @Autowired
    private FinalInspectionLotDetailsRepository finalInspectionLotDetailsRepository;

    @Autowired
    private InventoryEntryRepository inventoryEntryRepository;

    @Autowired
    private AzureBlobStorageService azureBlobStorageService;

    @Autowired
    private RmHeatFinalResultRepository rmHeatFinalResultRepository;

    @Autowired
    private ProcessLineFinalResultRepository processLineFinalResultRepository;

    @Autowired
    private FinalCumulativeResultsRepository finalCumulativeResultsRepository;

    @Autowired(required = false)
    private org.springframework.jdbc.core.JdbcTemplate jdbcTemplate;

    @Override
    @Transactional(readOnly = true)
    public List<VendorInspectionCallStatusDto> getVendorInspectionCallsWithStatus(String vendorId) {
        logger.info("Fetching inspection calls with workflow status for vendor: {}", vendorId);

        long startTime = System.currentTimeMillis();

        // 1. Fetch all inspection calls for the vendor
        long stepStart = System.currentTimeMillis();
        // Vendor code is stored both as ":1084223" (regular calls) and "1084223" (ERC diversion calls created on
        // CM approval) — match both spellings so diversion calls are not missed.
        String bare = vendorId == null ? "" : vendorId.trim().replaceFirst("^:+", "");
        java.util.Set<String> vendorIds = new java.util.LinkedHashSet<>();
        if (vendorId != null) vendorIds.add(vendorId.trim());
        if (!bare.isEmpty()) {
            vendorIds.add(bare);
            vendorIds.add(":" + bare);
        }
        List<InspectionCall> inspectionCalls = vendorIds.isEmpty() ? java.util.Collections.emptyList()
                : inspectionCallRepository.findByVendorIdInOrderByCreatedAtDesc(vendorIds);
        logger.info("Step 1: Fetched {} inspection calls in {}ms", inspectionCalls.size(), (System.currentTimeMillis() - stepStart));

        if (inspectionCalls.isEmpty()) {
            return Collections.emptyList();
        }

        // 2. Collect all necessary IDs for bulk fetching
        List<String> icNumbers = inspectionCalls.stream().map(InspectionCall::getIcNumber).collect(Collectors.toList());
        List<String> poNos = inspectionCalls.stream().map(InspectionCall::getPoNo).distinct().collect(Collectors.toList());

        // 3. Perform bulk fetches
        // Latest Transitions
        stepStart = System.currentTimeMillis();
        Map<String, WorkflowTransition> transitionMap = workflowTransitionRepository.findLatestByRequestIds(icNumbers)
                .stream().collect(Collectors.toMap(WorkflowTransition::getRequestId, wt -> wt, (wt1, wt2) -> wt1));
        logger.info("Step 3a: Fetched {} latest transitions in {}ms", transitionMap.size(), (System.currentTimeMillis() - stepStart));

        // PO Headers
        stepStart = System.currentTimeMillis();
        Map<String, PoHeader> poMap = poHeaderRepository.findByPoNoIn(poNos)
                .stream().collect(Collectors.toMap(PoHeader::getPoNo, ph -> ph, (ph1, ph2) -> ph1));
        logger.info("Step 3b: Fetched {} PO headers in {}ms", poMap.size(), (System.currentTimeMillis() - stepStart));

        // Note: Inspection Details (RM, Process, Final) are already eager-loaded via EntityGraph on findByVendorId

        // User Details (IE Names)
        stepStart = System.currentTimeMillis();
        Set<Integer> userIds = transitionMap.values().stream()
                .flatMap(wt -> Stream.of(wt.getAssignedToUser(), wt.getProcessIeUserId()))
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        Map<Integer, String> userNamesMap = Collections.emptyMap();
        Map<Integer, String> userMobilesMap = Collections.emptyMap();
        if (!userIds.isEmpty()) {
            List<UserMaster> users = userMasterRepository.findByUserIdIn(new ArrayList<>(userIds));
            userNamesMap = users.stream().collect(Collectors.toMap(
                    UserMaster::getUserId,
                    u -> Optional.ofNullable(u.getFullName()).orElse("Unknown"),
                    (e1, e2) -> e1
            ));
            userMobilesMap = users.stream().collect(Collectors.toMap(
                    UserMaster::getUserId,
                    u -> {
                        if (u.getMobileNumber() != null && !u.getMobileNumber().isBlank()) {
                            return u.getMobileNumber().trim();
                        } else if (u.getAlternateMobileNumber() != null && !u.getAlternateMobileNumber().isBlank()) {
                            return u.getAlternateMobileNumber().trim();
                        }
                        return "";
                    },
                    (e1, e2) -> e1
            ));
        }
        logger.info("Step 3c: Fetched {} user names in {}ms", userNamesMap.size(), (System.currentTimeMillis() - stepStart));

        // Additional nested data
        // RM Heat Quantities
        stepStart = System.currentTimeMillis();
        List<Long> rmDetailIds = inspectionCalls.stream()
                .map(InspectionCall::getRmInspectionDetails)
                .filter(Objects::nonNull)
                .map(RmInspectionDetails::getId)
                .collect(Collectors.toList());

        Map<Long, Long> rmHeatCountMap = Collections.emptyMap();
        if (!rmDetailIds.isEmpty()) {
            rmHeatCountMap = rmHeatQuantityRepository.findByRmInspectionDetailsIdIn(rmDetailIds)
                    .stream()
                    .filter(hq -> hq.getHeatNumber() != null && !hq.getHeatNumber().trim().isEmpty())
                    .collect(Collectors.groupingBy(
                            hq -> hq.getRmInspectionDetails().getId(),
                            Collectors.mapping(
                                    hq -> hq.getHeatNumber().trim(),
                                    Collectors.collectingAndThen(Collectors.toSet(), set -> (long) set.size())
                            )
                    ));
        }

        // Final Lot Details
        List<Long> finalDetailIds = inspectionCalls.stream()
                .map(InspectionCall::getFinalInspectionDetails)
                .filter(Objects::nonNull)
                .map(FinalInspectionDetails::getId)
                .collect(Collectors.toList());

        Map<Long, String> finalLotNoMap = Collections.emptyMap();
        if (!finalDetailIds.isEmpty()) {
            finalLotNoMap = finalInspectionLotDetailsRepository.findByFinalDetailIdIn(finalDetailIds)
                    .stream().collect(Collectors.toMap(
                            ld -> ld.getFinalDetailId(),
                            ld -> Optional.ofNullable(ld.getLotNumber()).orElse("N/A"),
                            (ld1, ld2) -> ld1 // Take first lot
                    ));
        }
        logger.info("Step 3d: Fetched extra details (Heat count/Lots) in {}ms", (System.currentTimeMillis() - stepStart));

        // 3e. Bulk-fetch actual accepted quantities from result tables
        stepStart = System.currentTimeMillis();
        List<String> rmIcNumbers = inspectionCalls.stream()
                .filter(ic -> "Raw Material".equalsIgnoreCase(ic.getTypeOfCall()))
                .map(InspectionCall::getIcNumber).collect(Collectors.toList());
        List<String> processIcNumbers = inspectionCalls.stream()
                .filter(ic -> "Process".equalsIgnoreCase(ic.getTypeOfCall()))
                .map(InspectionCall::getIcNumber).collect(Collectors.toList());
        List<String> finalIcNumbers = inspectionCalls.stream()
                .filter(ic -> "Final".equalsIgnoreCase(ic.getTypeOfCall()))
                .map(InspectionCall::getIcNumber).collect(Collectors.toList());

        Map<String, Long> acceptedQtyMap = new HashMap<>();
        if (!rmIcNumbers.isEmpty()) {
            rmHeatFinalResultRepository.sumAcceptedQtyByIcNumbers(rmIcNumbers)
                    .forEach(row -> acceptedQtyMap.put((String) row[0], ((Number) row[1]).longValue()));
        }
        if (!processIcNumbers.isEmpty()) {
            processLineFinalResultRepository.sumAcceptedQtyByIcNumbers(processIcNumbers)
                    .forEach(row -> acceptedQtyMap.put((String) row[0], ((Number) row[1]).longValue()));
        }
        if (!finalIcNumbers.isEmpty()) {
            finalCumulativeResultsRepository.sumAcceptedQtyByIcNumbers(finalIcNumbers)
                    .forEach(row -> acceptedQtyMap.put((String) row[0], ((Number) row[1]).longValue()));
        }
        logger.info("Step 3e: Fetched accepted qtys for {} calls in {}ms", acceptedQtyMap.size(), (System.currentTimeMillis() - stepStart));

        // 3f. Bulk-fetch IC Number & IC Date from edit tables (process_ic_edit, final_ic_edit, rm_ic_edit) FIRST, then fallback
        stepStart = System.currentTimeMillis();
        Map<String, String> certNoMap = new HashMap<>();
        Map<String, String> certDateMap = new HashMap<>();
        if (jdbcTemplate != null && !icNumbers.isEmpty()) {
            try {
                // 1. Primary Source: Query edit tables (process_ic_edit, final_ic_edit, rm_ic_edit) for created_at and ic_number
                for (String tbl : new String[]{"process_ic_edit", "final_ic_edit", "rm_ic_edit"}) {
                    try {
                        String sqlEdit = "SELECT ic_number, created_at FROM " + tbl + " WHERE ic_number IS NOT NULL AND created_at IS NOT NULL ORDER BY id DESC";
                        jdbcTemplate.query(sqlEdit, rs -> {
                            String icNum = rs.getString("ic_number");
                            Object crAtObj = rs.getObject("created_at");
                            if (icNum != null && !icNum.isBlank() && crAtObj != null) {
                                String icTrim = icNum.trim();
                                for (String callNo : icNumbers) {
                                    if (!certNoMap.containsKey(callNo) || !certDateMap.containsKey(callNo)) {
                                        String cleanCall = callNo != null ? callNo.trim() : "";
                                        String rawNum = cleanCall.contains("-") ? cleanCall.substring(cleanCall.indexOf("-") + 1).trim() : cleanCall;
                                        boolean isMatch = icTrim.equals(cleanCall)
                                                || icTrim.contains("/" + cleanCall + "/")
                                                || icTrim.contains(cleanCall)
                                                || (!rawNum.isEmpty() && (icTrim.contains("/" + rawNum + "/") || icTrim.contains(rawNum)));
                                        if (isMatch) {
                                            if (!certNoMap.containsKey(callNo)) {
                                                certNoMap.put(callNo, icTrim);
                                            }
                                            if (!certDateMap.containsKey(callNo)) {
                                                if (crAtObj instanceof java.time.LocalDateTime) {
                                                    certDateMap.put(callNo, ((java.time.LocalDateTime) crAtObj).format(DATE_FORMATTER));
                                                } else if (crAtObj instanceof java.sql.Timestamp) {
                                                    certDateMap.put(callNo, ((java.sql.Timestamp) crAtObj).toLocalDateTime().format(DATE_FORMATTER));
                                                } else if (crAtObj instanceof java.sql.Date) {
                                                    certDateMap.put(callNo, ((java.sql.Date) crAtObj).toLocalDate().format(DATE_FORMATTER));
                                                } else {
                                                    String s = crAtObj.toString().trim();
                                                    if (s.length() >= 10) {
                                                        certDateMap.put(callNo, s.substring(0, 10));
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        });
                    } catch (Exception ignored) {}
                }

                // 2. Secondary fallback: Query inspection_complete_details by CALL_NO for remaining calls
                List<String> missing = icNumbers.stream().filter(n -> !certNoMap.containsKey(n) || !certDateMap.containsKey(n)).collect(Collectors.toList());
                if (!missing.isEmpty()) {
                    for (int i = 0; i < missing.size(); i += 500) {
                        List<String> subList = missing.subList(i, Math.min(i + 500, missing.size()));
                        String inClause = "'" + String.join("','", subList) + "'";
                        String sql = "SELECT CALL_NO, CERTIFICATE_NO, CREATED_ON FROM inspection_complete_details WHERE CALL_NO IN (" + inClause + ") ORDER BY id ASC";
                        jdbcTemplate.query(sql, rs -> {
                            String callNo = rs.getString("CALL_NO");
                            String certNo = rs.getString("CERTIFICATE_NO");
                            Object crOnObj = rs.getObject("CREATED_ON");
                            if (callNo != null) {
                                if (!certNoMap.containsKey(callNo) && certNo != null && !certNo.isBlank()) {
                                    certNoMap.put(callNo, certNo.trim());
                                }
                                if (!certDateMap.containsKey(callNo) && crOnObj != null) {
                                    if (crOnObj instanceof java.time.LocalDateTime) {
                                        certDateMap.put(callNo, ((java.time.LocalDateTime) crOnObj).format(DATE_FORMATTER));
                                    } else if (crOnObj instanceof java.sql.Timestamp) {
                                        certDateMap.put(callNo, ((java.sql.Timestamp) crOnObj).toLocalDateTime().format(DATE_FORMATTER));
                                    } else {
                                        String s = crOnObj.toString().trim();
                                        if (s.length() >= 10) certDateMap.put(callNo, s.substring(0, 10));
                                    }
                                }
                            }
                        });
                    }
                }

                // 3. Tertiary fallback: Query certificate_storage for remaining calls
                List<String> stillMissing = icNumbers.stream().filter(n -> !certNoMap.containsKey(n) || !certDateMap.containsKey(n)).collect(Collectors.toList());
                if (!stillMissing.isEmpty()) {
                    try {
                        String sqlCs = "SELECT ic_number, file_name, ic_date, uploaded_at FROM certificate_storage WHERE ic_number IS NOT NULL ORDER BY id DESC";
                        jdbcTemplate.query(sqlCs, rs -> {
                            String icNum = rs.getString("ic_number");
                            String fName = rs.getString("file_name");
                            java.sql.Date icDt = rs.getDate("ic_date");
                            Object upAtObj = rs.getObject("uploaded_at");
                            for (String callNo : stillMissing) {
                                boolean match = (icNum != null && (icNum.equals(callNo) || icNum.contains("/" + callNo + "/") || icNum.contains(callNo)))
                                        || (fName != null && fName.contains(callNo));
                                if (match) {
                                    if (!certNoMap.containsKey(callNo)) {
                                        certNoMap.put(callNo, (icNum != null && !icNum.isBlank()) ? icNum.trim() : callNo);
                                    }
                                    if (!certDateMap.containsKey(callNo)) {
                                        if (icDt != null) {
                                            certDateMap.put(callNo, icDt.toString());
                                        } else if (upAtObj != null) {
                                            if (upAtObj instanceof java.time.LocalDateTime) {
                                                certDateMap.put(callNo, ((java.time.LocalDateTime) upAtObj).format(DATE_FORMATTER));
                                            } else if (upAtObj instanceof java.sql.Timestamp) {
                                                certDateMap.put(callNo, ((java.sql.Timestamp) upAtObj).toLocalDateTime().format(DATE_FORMATTER));
                                            } else {
                                                String s = upAtObj.toString().trim();
                                                if (s.length() >= 10) certDateMap.put(callNo, s.substring(0, 10));
                                            }
                                        }
                                    }
                                }
                            }
                        });
                    } catch (Exception ignored) {}
                }
            } catch (Exception e) {
                logger.warn("Step 3f: Error bulk-fetching certificates: {}", e.getMessage());
            }
        }
        logger.info("Step 3f: Fetched certificates for {} calls in {}ms", certNoMap.size(), (System.currentTimeMillis() - stepStart));

        // 4. Map each inspection call to DTO using bulk-fetched data
        stepStart = System.currentTimeMillis();
        final Map<Integer, String> finalUserNamesMap = userNamesMap;
        final Map<Integer, String> finalUserMobilesMap = userMobilesMap;
        final Map<Long, Long> finalRmHeatCountMap = rmHeatCountMap;
        final Map<Long, String> finalFinalLotNoMap = finalLotNoMap;
        final Map<String, Long> finalAcceptedQtyMap = acceptedQtyMap;
        final Map<String, String> finalCertNoMap = certNoMap;
        final Map<String, String> finalCertDateMap = certDateMap;

        List<VendorInspectionCallStatusDto> results = inspectionCalls.stream()
                .map(ic -> mapToVendorInspectionCallStatusDtoOptimized(
                        ic,
                        transitionMap.get(ic.getIcNumber()),
                        poMap.get(ic.getPoNo()),
                        ic.getRmInspectionDetails(),
                        ic.getProcessInspectionDetails(),
                        ic.getFinalInspectionDetails(),
                        finalUserNamesMap,
                        finalUserMobilesMap,
                        finalRmHeatCountMap,
                        finalFinalLotNoMap,
                        finalAcceptedQtyMap,
                        finalCertNoMap,
                        finalCertDateMap))
                .collect(Collectors.toList());

        long endTime = System.currentTimeMillis();
        logger.info("Successfully fetched {} inspection calls for vendor: {} in {}ms", results.size(), vendorId, (endTime - startTime));

        return results;
    }

    /**
     * Optimized mapping from InspectionCall entity to VendorInspectionCallStatusDto
     */
    private VendorInspectionCallStatusDto mapToVendorInspectionCallStatusDtoOptimized(
            InspectionCall ic,
            WorkflowTransition latestTransition,
            PoHeader ph,
            RmInspectionDetails rmDetails,
            List<ProcessInspectionDetails> processList,
            FinalInspectionDetails finalDetails,
            Map<Integer, String> userNamesMap,
            Map<Integer, String> userMobilesMap,
            Map<Long, Long> rmHeatCountMap,
            Map<Long, String> finalLotNoMap,
            Map<String, Long> acceptedQtyMap,
            Map<String, String> certNoMap,
            Map<String, String> certDateMap) {

        // Get item name and quantity based on type of call
        String itemName = getItemNameOptimized(ic, rmDetails, processList, finalDetails);
        Integer quantityOffered = getQuantityOfferedOptimized(ic, rmDetails, processList, finalDetails);

        // Fetch PoHeader details
        String rlyShortName = ph != null ? ph.getRlyShortName() : "N/A";
        String rlyCd = ph != null ? ph.getRlyCd() : "N/A";

        // IE Name & Mobile from Map
        String ieName = "Not Assigned";
        String ieContactNo = "";
        if (latestTransition != null) {
            Integer targetUserId = latestTransition.getAssignedToUser();
            if (targetUserId == null || targetUserId <= 0) {
                targetUserId = latestTransition.getProcessIeUserId();
            }
            if (targetUserId != null && targetUserId > 0) {
                ieName = userNamesMap.getOrDefault(targetUserId, "Not Assigned");
                ieContactNo = userMobilesMap.getOrDefault(targetUserId, "");
            }
        }

        // Get Heats/Lots count
        Integer noOfHeatsRM = null;
        String lotNoProcess = null;
        String lotNoFinal = null;
        String uom = "N/A";

        if ("Raw Material".equalsIgnoreCase(ic.getTypeOfCall()) && rmDetails != null) {
            Long count = rmHeatCountMap.get(rmDetails.getId());
            noOfHeatsRM = count != null ? count.intValue() : 0;
            uom = rmDetails.getUnitOfMeasurement();
        } else if ("Process".equalsIgnoreCase(ic.getTypeOfCall()) && processList != null && !processList.isEmpty()) {
            lotNoProcess = processList.get(0).getLotNumber();
        } else if ("Final".equalsIgnoreCase(ic.getTypeOfCall()) && finalDetails != null) {
            lotNoFinal = finalLotNoMap.get(finalDetails.getId());
        }

        String scheduledDate = null;
        boolean isScheduled = (latestTransition != null && (
                "SCHEDULED".equalsIgnoreCase(latestTransition.getStatus()) ||
                "IE_SCHEDULED".equalsIgnoreCase(latestTransition.getStatus()) ||
                "SCHEDULED".equalsIgnoreCase(latestTransition.getJobStatus()) ||
                (latestTransition.getAction() != null && latestTransition.getAction().toUpperCase().contains("SCHEDULE"))
        )) || ic.getActualInspectionDate() != null;
        if (isScheduled && ic.getActualInspectionDate() != null) {
            scheduledDate = ic.getActualInspectionDate().format(DATE_FORMATTER);
        }

        // Determine Certificate No and Date
        String certNo = certNoMap != null ? certNoMap.get(ic.getIcNumber()) : null;
        String certDate = certDateMap != null ? certDateMap.get(ic.getIcNumber()) : null;

        String effectiveStatus = latestTransition != null ? latestTransition.getStatus() : ic.getStatus();
        boolean isIbsOrComplete = effectiveStatus != null && (
                effectiveStatus.toUpperCase().contains("IBS") ||
                effectiveStatus.toUpperCase().contains("COMPLET")
        );

        if (isIbsOrComplete) {
            if (certNo == null || certNo.isBlank()) {
                String ieShort = "ARV";
                if (latestTransition != null && latestTransition.getAssignedToUser() != null) {
                    String fullIe = userNamesMap.get(latestTransition.getAssignedToUser());
                    if (fullIe != null && !fullIe.isBlank()) {
                        String[] parts = fullIe.trim().split("\\s+");
                        ieShort = parts[0].substring(0, Math.min(3, parts[0].length())).toUpperCase();
                    }
                }
                certNo = "N/" + ic.getIcNumber() + "/" + ieShort;
            }
            if (certDate == null || certDate.isBlank()) {
                if (latestTransition != null && latestTransition.getCreatedDate() != null) {
                    certDate = new java.text.SimpleDateFormat("yyyy-MM-dd").format(latestTransition.getCreatedDate());
                } else if (ic.getUpdatedAt() != null) {
                    certDate = ic.getUpdatedAt().format(DATE_FORMATTER);
                } else if (ic.getDesiredInspectionDate() != null) {
                    certDate = ic.getDesiredInspectionDate().format(DATE_FORMATTER);
                }
            }
        }

        return VendorInspectionCallStatusDto.builder()
                .workflowTransitionId(latestTransition != null ? latestTransition.getWorkflowTransitionId() : null)
                .icNumber(ic.getIcNumber())
                .poNo(ic.getPoNo())
                .poSerialNo(ic.getPoSerialNo())
                .typeOfCall(ic.getTypeOfCall())
                .desiredInspectionDate(ic.getDesiredInspectionDate() != null ? ic.getDesiredInspectionDate().format(DATE_FORMATTER) : null)
                .placeOfInspection(ic.getPlaceOfInspection())
                .itemName(itemName)
                .quantityOffered(quantityOffered)
                .workflowStatus(latestTransition != null ? latestTransition.getStatus() : ic.getStatus())
                .currentRoleName(latestTransition != null ? latestTransition.getCurrentRoleName() : null)
                .nextRoleName(latestTransition != null ? latestTransition.getNextRoleName() : null)
                .jobStatus(latestTransition != null ? latestTransition.getJobStatus() : null)
                .companyName(ic.getCompanyName())
                .unitName(ic.getUnitName())
                .createdAt(ic.getCreatedAt() != null ? ic.getCreatedAt().format(DATE_FORMATTER) : null)
                .updatedAt(ic.getUpdatedAt() != null ? ic.getUpdatedAt().format(DATE_FORMATTER) : null)
                .rlyShortName(rlyShortName)
                .rlyCd(rlyCd)
                .ercType(ic.getErcType())
                .noOfHeatsRM(noOfHeatsRM)
                .lotNoProcess(lotNoProcess)
                .lotNoFinal(lotNoFinal)
                .ieName(ieName)
                .ieContactNo(ieContactNo)
                .ieMobile(ieContactNo)
                .uom(uom)
                .scheduledDate(scheduledDate)
                .acceptedQty(acceptedQtyMap.getOrDefault(ic.getIcNumber(), 0L))
                .certificateNo(certNo)
                .certificateDate(certDate)
                .icNo(certNo)
                .icDate(certDate)
                .build();
    }

    private String getItemNameOptimized(InspectionCall ic, RmInspectionDetails rmDetails, List<ProcessInspectionDetails> processList, FinalInspectionDetails finalDetails) {
        if ("Raw Material".equalsIgnoreCase(ic.getTypeOfCall()) && rmDetails != null) {
            return rmDetails.getItemDescription();
        } else if ("Process".equalsIgnoreCase(ic.getTypeOfCall()) && processList != null && !processList.isEmpty()) {
            return "Process Inspection - Lot: " + processList.get(0).getLotNumber();
        } else if ("Final".equalsIgnoreCase(ic.getTypeOfCall()) && finalDetails != null) {
            return "Final Inspection - " + finalDetails.getTotalLots() + " lots";
        }
        return "N/A";
    }

    private Integer getQuantityOfferedOptimized(InspectionCall ic, RmInspectionDetails rmDetails, List<ProcessInspectionDetails> processList, FinalInspectionDetails finalDetails) {
        if ("Raw Material".equalsIgnoreCase(ic.getTypeOfCall()) && rmDetails != null) {
            return rmDetails.getOfferedQtyErc();
        } else if ("Process".equalsIgnoreCase(ic.getTypeOfCall()) && processList != null && !processList.isEmpty()) {
            return processList.stream().filter(p -> p.getOfferedQty() != null).mapToInt(p -> p.getOfferedQty()).sum();
        } else if ("Final".equalsIgnoreCase(ic.getTypeOfCall()) && finalDetails != null) {
            return finalDetails.getTotalOfferedQty();
        }
        return 0;
    }

    // Deprecated methods replaced by optimized versions

    @Autowired
    private com.sarthi.repository.rawmaterial.RmInspectionDetailsRepository rmInspectionDetailsRepository;

    @Override
    @Transactional(readOnly = true)
    public boolean hasTcDocs(String callNo) {
        // DB lookup only — no blob download, so the case letter popup can show "available" for ~0 MB
        if (callNo == null || callNo.isBlank()) return false;
        return !resolveTcFilePaths(callNo).isEmpty();
    }

    @Override
    @Transactional(readOnly = true)
    public byte[] getTcDocsByCallNo(String callNo) {
        if (callNo == null || callNo.isBlank()) {
            throw new BusinessException(new ErrorDetails(AppConstant.NO_RECORD_FOUND, AppConstant.ERROR_TYPE_CODE_VALIDATION, AppConstant.ERROR_TYPE_VALIDATION, "Call number cannot be empty"));
        }
        String trimmed = callNo.trim();
        Set<String> tcFilePaths = resolveTcFilePaths(callNo);
        return downloadAndMergeTcDocs(trimmed, tcFilePaths);
    }

    /** Steps 1-5: resolve the TC file paths for a call (DB only). Shared by hasTcDocs and getTcDocsByCallNo. */
    private Set<String> resolveTcFilePaths(String callNo) {
        if (callNo == null || callNo.isBlank()) {
            throw new BusinessException(new ErrorDetails(AppConstant.NO_RECORD_FOUND, AppConstant.ERROR_TYPE_CODE_VALIDATION, AppConstant.ERROR_TYPE_VALIDATION, "Call number cannot be empty"));
        }
        String trimmed = callNo.trim();
        logger.info("Fetching TC docs for call number: {}", trimmed);

        Set<String> heatNumbers = new LinkedHashSet<>();
        Set<String> tcNumbers = new LinkedHashSet<>();
        Set<String> tcFilePaths = new LinkedHashSet<>();

        // Step 1: Find InspectionCall by IC number variants
        List<String> callVariants = new ArrayList<>();
        callVariants.add(trimmed);
        if (trimmed.contains("-")) {
            callVariants.add(trimmed.substring(trimmed.indexOf("-") + 1).trim());
        }
        if (!trimmed.startsWith("ER-")) {
            callVariants.add("ER-" + trimmed);
        }

        InspectionCall matchedCall = null;
        for (String variant : callVariants) {
            if (variant.isBlank()) continue;
            Optional<InspectionCall> opt = inspectionCallRepository.findByIcNumber(variant);
            if (opt.isPresent()) {
                matchedCall = opt.get();
                logger.info("Found InspectionCall with ID: {} for variant: {}", matchedCall.getId(), variant);
                break;
            }
        }

        // Step 2: From InspectionCall -> RmInspectionDetails -> RmHeatQuantity
        if (matchedCall != null) {
            RmInspectionDetails rmDetails = matchedCall.getRmInspectionDetails();
            if (rmDetails == null) {
                rmDetails = rmInspectionDetailsRepository.findByIcId(matchedCall.getId()).orElse(null);
            }

            if (rmDetails != null) {
                logger.info("Found RmInspectionDetails ID: {} for InspectionCall ID: {}", rmDetails.getId(), matchedCall.getId());
                if (rmDetails.getTcNumber() != null && !rmDetails.getTcNumber().isBlank()) {
                    tcNumbers.add(rmDetails.getTcNumber().trim());
                }
                if (rmDetails.getHeatNumbers() != null && !rmDetails.getHeatNumbers().isBlank()) {
                    for (String hn : rmDetails.getHeatNumbers().split(",")) {
                        if (!hn.trim().isBlank()) heatNumbers.add(hn.trim());
                    }
                }

                List<com.sarthi.entity.rawmaterial.RmHeatQuantity> hqList = 
                        rmHeatQuantityRepository.findByRmDetailId(Math.toIntExact(rmDetails.getId()));
                if (hqList != null && !hqList.isEmpty()) {
                    for (com.sarthi.entity.rawmaterial.RmHeatQuantity hq : hqList) {
                        if (hq.getHeatNumber() != null && !hq.getHeatNumber().isBlank()) {
                            heatNumbers.add(hq.getHeatNumber().trim());
                        }
                        if (hq.getTcNumber() != null && !hq.getTcNumber().isBlank()) {
                            tcNumbers.add(hq.getTcNumber().trim());
                        }
                    }
                }
            }
        }

        // Step 3: Fallback query via RmHeatQuantityRepository
        if (heatNumbers.isEmpty() && tcNumbers.isEmpty()) {
            for (String variant : callVariants) {
                List<com.sarthi.entity.rawmaterial.RmHeatQuantity> hqList = 
                        rmHeatQuantityRepository.findByInspectionCallNo(variant);
                if (hqList != null && !hqList.isEmpty()) {
                    for (com.sarthi.entity.rawmaterial.RmHeatQuantity hq : hqList) {
                        if (hq.getHeatNumber() != null && !hq.getHeatNumber().isBlank()) {
                            heatNumbers.add(hq.getHeatNumber().trim());
                        }
                        if (hq.getTcNumber() != null && !hq.getTcNumber().isBlank()) {
                            tcNumbers.add(hq.getTcNumber().trim());
                        }
                    }
                    break;
                }
            }
        }

        logger.info("Resolved Heat Numbers: {} and TC Numbers: {} for call: {}", heatNumbers, tcNumbers, trimmed);

        // Step 4: Find inventory entries by heat numbers
        if (!heatNumbers.isEmpty()) {
            List<com.sarthi.entity.InventoryEntry> entries = inventoryEntryRepository.findByHeatNumberIn(new ArrayList<>(heatNumbers));
            if (entries != null) {
                for (com.sarthi.entity.InventoryEntry ie : entries) {
                    if (ie.getTcFilePath() != null && !ie.getTcFilePath().isBlank()) {
                        tcFilePaths.add(ie.getTcFilePath().trim());
                    }
                }
            }
        }

        // Step 5: Fallback to repository native query
        if (tcFilePaths.isEmpty()) {
            for (String variant : callVariants) {
                List<String> paths = inventoryEntryRepository.findTcFilePathsByCallNo(variant);
                if (paths != null && !paths.isEmpty()) {
                    tcFilePaths.addAll(paths);
                    break;
                }
            }
        }

        logger.info("Found {} TC file paths for call {}: {}", tcFilePaths.size(), trimmed, tcFilePaths);
        return tcFilePaths;
    }

    /** Steps 6-7: download the TC PDFs from Azure and merge them (unchanged logic). */
    private byte[] downloadAndMergeTcDocs(String trimmed, Set<String> tcFilePaths) {
        if (tcFilePaths.isEmpty()) {
            throw new BusinessException(new ErrorDetails(AppConstant.NO_RECORD_FOUND, AppConstant.ERROR_TYPE_CODE_VALIDATION, AppConstant.ERROR_TYPE_VALIDATION, "No TC Documents found for this call number"));
        }

        // Step 6: Download all TC PDF files from Azure Storage
        List<byte[]> pdfBytesList = new ArrayList<>();
        for (String path : tcFilePaths) {
            try {
                if (path == null || path.isBlank()) continue;
                String blobName = path.substring(path.lastIndexOf('/') + 1);
                blobName = java.net.URLDecoder.decode(blobName, java.nio.charset.StandardCharsets.UTF_8);
                byte[] pdfBytes = azureBlobStorageService.downloadFile(blobName);
                if (pdfBytes != null && pdfBytes.length > 0) {
                    pdfBytesList.add(pdfBytes);
                }
            } catch (Exception e) {
                logger.error("Failed to download TC document from blob: {}", path, e);
            }
        }

        if (pdfBytesList.isEmpty()) {
            throw new BusinessException(new ErrorDetails(AppConstant.NO_RECORD_FOUND, AppConstant.ERROR_TYPE_CODE_VALIDATION, AppConstant.ERROR_TYPE_VALIDATION, "Failed to download any TC Documents"));
        }
        
        if (pdfBytesList.size() == 1) {
            return pdfBytesList.get(0);
        }

        // Step 7: Merge multiple TC files into a single PDF
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            Document document = new Document();
            PdfCopy copy = new PdfCopy(document, baos);
            document.open();
            for (byte[] pdf : pdfBytesList) {
                PdfReader reader = new PdfReader(pdf);
                for (int i = 1; i <= reader.getNumberOfPages(); i++) {
                    copy.addPage(copy.getImportedPage(reader, i));
                }
                reader.close();
            }
            document.close();
            return baos.toByteArray();
        } catch (Exception e) {
            logger.error("Error merging PDF documents for call: {}", trimmed, e);
            throw new BusinessException(new ErrorDetails(AppConstant.INTERNAL_SERVER_ERROR, AppConstant.ERROR_TYPE_CODE_INTERNAL, AppConstant.ERROR_TYPE_INTERNAL, "Error merging TC documents"));
        }
    }
}