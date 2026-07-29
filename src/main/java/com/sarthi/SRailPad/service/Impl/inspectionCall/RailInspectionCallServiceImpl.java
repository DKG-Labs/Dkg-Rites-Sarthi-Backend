package com.sarthi.SRailPad.service.Impl.inspectionCall;

import com.sarthi.SRailPad.entity.inspectionCall.RailInspectionCall;
import com.sarthi.SRailPad.entity.RailWorkflowTransaction;
import com.sarthi.SRailPad.entity.inspectionCall.RailInspectionLot;
import com.sarthi.SRailPad.entity.inspectionCall.RailInspectionBatch;
import com.sarthi.SRailPad.repository.RailWorkflowTransactionRepository;
import com.sarthi.SRailPad.repository.inspectionCall.RailInspectionCallRepository;
import com.sarthi.SRailPad.repository.inspectionCall.RailInspectionCompleteDetailsRepository;
import com.sarthi.SRailPad.repository.inspectionCall.RailProcessCallDetailsRepository;
import com.sarthi.SRailPad.entity.inspectionCall.RailInspectionCompleteDetails;
import com.sarthi.SRailPad.service.inspectionCall.RailInspectionCallService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RailInspectionCallServiceImpl implements RailInspectionCallService {

    private final RailInspectionCallRepository repository;
    private final RailProcessCallDetailsRepository processCallDetailsRepository;
    private final com.sarthi.repository.PoHeaderRepository poHeaderRepository;
    private final com.sarthi.repository.VendorMasterRepository vendorMasterRepository;
    private final RailWorkflowTransactionRepository railWorkflowTransactionRepository;
    private final com.sarthi.repository.PoItemRepository poItemRepository;
    private final com.sarthi.repository.PoMaHeaderRepository poMaHeaderRepository;
    private final com.sarthi.SRailPad.repository.ieVerification.RailFinalInspectionLotResultsRepository railFinalInspectionLotResultsRepository;
    private final RailInspectionCompleteDetailsRepository railInspectionCompleteDetailsRepository;
    private final com.sarthi.SRailPad.repository.RailPoiIeMappingRepository railPoiIeMappingRepository;
    private final com.sarthi.SRailPad.repository.inspectionCall.RailInspectionCallAuditRepository railInspectionCallAuditRepository;

    private static final String[] units = { "", "ONE", "TWO", "THREE", "FOUR", "FIVE", "SIX", "SEVEN", "EIGHT", "NINE", "TEN", "ELEVEN", "TWELVE", "THIRTEEN", "FOURTEEN", "FIFTEEN", "SIXTEEN", "SEVENTEEN", "EIGHTEEN", "NINETEEN" };
    private static final String[] tens = { "", "", "TWENTY", "THIRTY", "FORTY", "FIFTY", "SIXTY", "SEVENTY", "EIGHTY", "NINETY" };

    private String convertToWords(long n) {
        if (n < 0) return "MINUS " + convertToWords(-n);
        if (n == 0) return "ZERO";
        if (n < 20) return units[(int) n];
        if (n < 100) return tens[(int) (n / 10)] + ((n % 10 != 0) ? " " : "") + units[(int) (n % 10)];
        if (n < 1000) return units[(int) (n / 100)] + " HUNDRED" + ((n % 100 != 0) ? " " : "") + convertToWords(n % 100);
        if (n < 100000) return convertToWords(n / 1000) + " THOUSAND" + ((n % 1000 != 0) ? " " : "") + convertToWords(n % 1000);
        if (n < 10000000) return convertToWords(n / 100000) + " LAKH" + ((n % 100000 != 0) ? " " : "") + convertToWords(n % 100000);
        return convertToWords(n / 10000000) + " CRORE" + ((n % 10000000 != 0) ? " " : "") + convertToWords(n % 10000000);
    }

    @Override
    @Transactional
    public RailInspectionCall create(RailInspectionCall call) {
        // Determine prefix based on call type
        String prefix = "PROCESS".equalsIgnoreCase(call.getCallType()) ? "RPP-" : "RPF-";

        // Generate Call No: PREFIX-MMDDYY001
        String datePart = LocalDate.now().format(DateTimeFormatter.ofPattern("MMddyy"));
        String pattern = prefix + datePart + "%";
        Optional<String> lastCallNo = repository.findLastCallNoByPattern(pattern);
        
        int seq = 1;
        if (lastCallNo.isPresent()) {
            try {
                String lastSeqStr = lastCallNo.get().substring(lastCallNo.get().length() - 3);
                seq = Integer.parseInt(lastSeqStr) + 1;
            } catch (Exception e) {
                System.err.println("Error parsing sequence from last call no: " + lastCallNo.get());
            }
        }
        
        String generatedCallNo = String.format("%s%s%03d", prefix, datePart, seq);
        call.setCallNo(generatedCallNo);
        
        if (call.getCreatedBy() == null) {
            call.setCreatedBy(1L);
        }
        if (call.getVendorCode() != null) {
            call.setVendorCode(call.getVendorCode().replaceAll("^:", ""));
        }
        if (call.getPlantId() != null) {
            call.setPlantId(call.getPlantId().replaceAll("^:", ""));
        }
        
        // Ensure bidirectional links are set for JPA cascade
        if (call.getLots() != null) {
            for (RailInspectionLot lot : call.getLots()) {
                lot.setInspectionCall(call);
                if (lot.getBatches() != null) {
                    for (RailInspectionBatch batch : lot.getBatches()) {
                        batch.setLot(lot);
                        if (batch.getQtyToUse() != null && batch.getQuantity() == null) {
                            batch.setQuantity(batch.getQtyToUse());
                        } else if (batch.getQuantity() != null && batch.getQtyToUse() == null) {
                            batch.setQtyToUse(batch.getQuantity());
                        }
                        if (batch.getAvailableQty() != null && batch.getQtyToUse() != null && batch.getBalanceQty() == null) {
                            batch.setBalanceQty(Math.max(0, batch.getAvailableQty() - batch.getQtyToUse()));
                        }
                    }
                }
            }
        }
        
        RailInspectionCall savedCall = repository.save(call);

        // If it's a PROCESS call, save the child details
        if ("PROCESS".equalsIgnoreCase(savedCall.getCallType())) {
            com.sarthi.SRailPad.entity.inspectionCall.RailProcessCallDetails details = new com.sarthi.SRailPad.entity.inspectionCall.RailProcessCallDetails();
            details.setInspectionCall(savedCall);
            details.setDrawingNo(call.getDrawingNo());
            details.setUom(call.getUom());
            details.setQtyOnOrder(call.getQtyOnOrder());
            details.setQtyAcceptedTillNow(call.getQtyAcceptedTillNow());
            details.setQtyDesiredForFinal(call.getQtyDesiredForFinal());
            details.setQtyDue(call.getQtyDue());
            details.setProductionInitiationDate(call.getProductionInitiationDate());
            details.setCreatedBy(call.getCreatedBy());
            details.setUpdatedBy(call.getUpdatedBy());
            
            processCallDetailsRepository.save(details);
        }

        return savedCall;
    }

    @Override
    public List<RailInspectionCall> getAllByVendorCode(String vendorCode) {
        List<RailInspectionCall> calls = repository.findAllByVendorCode(vendorCode);
        calls.forEach(this::enrichCallData);
        return calls;
    }

    @Override
    public List<RailInspectionCall> getAllByPlantId(String plantId) {
        List<RailInspectionCall> calls = repository.findAllByPlantId(plantId);
        calls.forEach(this::enrichCallData);
        return calls;
    }

    @Override
    public Page<RailInspectionCall> getPaginatedCallsByVendor(String vendorCode, Pageable pageable) {
        Page<RailInspectionCall> callsPage = repository.findByVendorCodeOrderByCreatedAtDesc(vendorCode, pageable);
        callsPage.forEach(this::enrichCallData);
        return callsPage;
    }

    @Override
    public Page<RailInspectionCall> getPaginatedCallsByPlant(String plantId, String statusType, Pageable pageable) {
        List<String> terminalStatuses = Arrays.asList(
                "Completed", "COMPLETED", "IC_ISSUE", "Ic_issue", "IC ISSUE", "Ic issue",
                "GENERATE_IC", "Generate_ic", "GENERATE IC", "Generate ic",
                "WITHDRAWN", "Withdrawn", "WITHDRAW", "Withdraw",
                "CANCEL", "Cancel", "FINISH", "Finish"
        );
        Page<RailInspectionCall> page;
        if ("pending".equalsIgnoreCase(statusType)) {
            page = repository.findPendingCallsForPlantNative(plantId, terminalStatuses, pageable);
        } else {
            page = repository.findByPlantIdOrderByCreatedAtDesc(plantId, pageable);
        }

        page.forEach(this::enrichCallData);
        return page;
    }

    @Override
    public Page<RailInspectionCall> getCompletedPaginatedCallsByPlant(String plantId, Pageable pageable) {
        List<String> exactStatuses = Arrays.asList(
                "Completed", "COMPLETED", "IC_ISSUE", "Ic_issue", "IC ISSUE", "Ic issue",
                "GENERATE_IC", "Generate_ic", "GENERATE IC", "Generate ic",
                "WITHDRAWN", "Withdrawn", "WITHDRAW", "Withdraw",
                "CANCEL", "Cancel", "FINISH", "Finish"
        );
        Page<RailInspectionCall> page = repository.findCompletedCallsForPlantNative(plantId, exactStatuses, pageable);
        page.forEach(this::enrichCallData);
        return page;
    }

    @Override
    public RailInspectionCall getById(Long id) {
        RailInspectionCall call = repository.findById(id).orElse(null);
        if (call != null) {
            enrichCallData(call);
        }
        return call;
    }

    @Override
    public RailInspectionCall getByCallNo(String callNo) {
        RailInspectionCall call = repository.findByCallNo(callNo).orElse(null);
        if (call != null) {
            enrichCallData(call);
        }
        return call;
    }

    private void enrichCallData(RailInspectionCall call) {
        if (call == null) return;

        // Force initialize lazy lots and batches for JSON serialization and drawingNo resolution
        if (call.getLots() != null && !call.getLots().isEmpty()) {
            call.getLots().forEach(lot -> {
                if (lot.getBatches() != null) {
                    lot.getBatches().size();
                    if ((call.getDrawingNo() == null || call.getDrawingNo().isBlank() || "N/A".equalsIgnoreCase(call.getDrawingNo())) && !lot.getBatches().isEmpty()) {
                        String bDrg = lot.getBatches().get(0).getDrawingNo();
                        if (bDrg != null && !bDrg.isBlank()) {
                            call.setDrawingNo(bDrg);
                        }
                    }
                }
            });
        }

        // Infer Rail Pad Type if missing
        if (call.getRailPadType() == null || call.getRailPadType().isBlank() || "N/A".equalsIgnoreCase(call.getRailPadType())) {
            String drg = call.getDrawingNo();
            if (drg != null) {
                if (drg.contains("888") || drg.contains("889") || drg.contains("701") || drg.contains("8779") || drg.contains("9774") || drg.contains("4218") || drg.contains("890")) {
                    call.setRailPadType("6.00mm NCRGRSP");
                } else if (drg.contains("6618") || drg.contains("8327")) {
                    call.setRailPadType("6.20mm CGRSP");
                } else if (drg.contains("8528") || drg.contains("8747")) {
                    call.setRailPadType("10.00mm CGRSP");
                } else if (drg.contains("3703") || drg.contains("3711")) {
                    call.setRailPadType("6.00mm GRSP");
                } else {
                    call.setRailPadType("6.00mm NCRGRSP");
                }
            } else {
                call.setRailPadType("6.00mm NCRGRSP");
            }
        }

        String barePoNo = call.getPoNo();
        if (barePoNo != null && barePoNo.contains("/")) {
            barePoNo = barePoNo.split("/")[0];
        }

        // 1. Fetch PO Header for RLY info and Vendor Details
        Optional<com.sarthi.entity.PoHeader> headerOpt = poHeaderRepository.findByPoNo(barePoNo);
        if (headerOpt.isPresent()) {
            com.sarthi.entity.PoHeader header = headerOpt.get();
            call.setScrCode(header.getRlyShortName());
            
            String rlyPrefix = header.getRlyShortName() != null ? header.getRlyShortName() : "";
            call.setRlyPoSrNo(rlyPrefix + "/" + call.getPoNo());

            // Try to extract vendor name from header first
            if (header.getVendorDetails() != null) {
                call.setVendorName(extractVendorName(header.getVendorDetails()));
            }
        }

        // 2. Fetch Vendor Master for official Name if still null
        if (call.getVendorName() == null || "N/A".equals(call.getVendorName())) {
            Optional<com.sarthi.entity.VendorMaster> vendorOpt = vendorMasterRepository.findByVendorCode(call.getVendorCode());
            vendorOpt.ifPresent(v -> call.setVendorName(v.getVendorName()));
        }

        // 3. Fetch latest status dynamically from workflow transactions
        if (call.getCallNo() != null) {
            String latestStatus = railWorkflowTransactionRepository
                    .findLatestStatusByRequestId(call.getCallNo())
                    .orElse(call.getStatus());
            call.setStatus(latestStatus);
        }

        // 4. Populate IE Assigned Name: ONLY if status is verified, fetch Main IE from rail_poi_ie_mapping
        String status = call.getStatus();
        boolean isVerified = status != null && (
                status.equalsIgnoreCase("VERIFY") || status.equalsIgnoreCase("VERIFIED") ||
                status.equalsIgnoreCase("CALL_REGISTERED") || status.equalsIgnoreCase("REGISTERED") ||
                status.equalsIgnoreCase("SCHEDULED") || status.equalsIgnoreCase("IE_SCHEDULED") ||
                status.equalsIgnoreCase("MAIN_IE_SCHEDULE_CALL") || status.equalsIgnoreCase("INITIATE_CALL") ||
                status.equalsIgnoreCase("INSPECTION_INITIATION") || status.equalsIgnoreCase("INSPECTION_IN_PROGRESS") ||
                status.equalsIgnoreCase("COMPLETED") || status.equalsIgnoreCase("IC_ISSUE") ||
                status.equalsIgnoreCase("GENERATE_IC") || status.equalsIgnoreCase("DESK_COMPLETED") ||
                status.equalsIgnoreCase("PO_VERIFICATION")
        );

        if (!isVerified) {
            call.setIeAssignedName("IE Not Assigned");
        } else {
            List<String> mainIeNames = railPoiIeMappingRepository.findMainIeNamesByPlantId(call.getPlantId(), null);
            if (mainIeNames != null && !mainIeNames.isEmpty()) {
                call.setIeAssignedName(String.join(", ", mainIeNames));
            } else {
                call.setIeAssignedName("IE Not Assigned");
            }
        }
    }

    private String extractVendorName(String raw) {
        if (raw == null || raw.isBlank()) return null;
        String[] parts = raw.split("~");
        String segment = parts[0];
        int dashIdx = segment.lastIndexOf('-');
        if (dashIdx > 0) return segment.substring(0, dashIdx).trim();
        return segment.trim();
    }

    private String extractVendorAddress(String raw) {
        if (raw == null || raw.isBlank()) return "";
        String[] parts = raw.split("~");
        if (parts.length <= 1) return "";
        StringBuilder sb = new StringBuilder();
        for (int i = 1; i < parts.length; i++) {
            String p = parts[i] != null ? parts[i].trim() : "";
            if (p.isEmpty()) continue;
            if (sb.length() > 0) sb.append(", ");
            sb.append(p);
        }
        return sb.toString();
    }

    @Override
    public com.sarthi.SRailPad.dto.RailpadIcCertificateDto getRailpadIcDetails(String callNo) {
        RailInspectionCall call = repository.findByCallNo(callNo)
                .orElseThrow(() -> new RuntimeException("Call not found: " + callNo));

        String poNo = call.getPoNo();
        String poSr = call.getPoSr();
        if (poNo != null && poNo.contains("/")) {
            poNo = poNo.split("/")[0];
        }

        com.sarthi.entity.PoHeader poHeader = poHeaderRepository.findByPoNo(poNo).orElse(null);
        com.sarthi.entity.PoItem poItem = null;
        if (poHeader != null) {
            List<com.sarthi.entity.PoItem> items = poItemRepository.findByPoHeader_Id(poHeader.getId());
            poItem = items.stream()
                    .filter(item -> item.getItemSrNo() != null && item.getItemSrNo().trim().equals(poSr != null ? poSr.trim() : ""))
                    .findFirst()
                    .orElse(items.isEmpty() ? null : items.get(0));
        }

        List<com.sarthi.entity.CricsPos.PoMaHeader> amendments = poMaHeaderRepository.findByPoNo(poNo).stream()
                .sorted((a, b) -> b.getMaDate().compareTo(a.getMaDate()))
                .limit(4)
                .collect(Collectors.toList());

        List<String> latest4Amendments = amendments.stream()
                .map(ma -> "M.A.NO. " + ma.getMaNo() + " dated " + (ma.getMaDate() != null ? ma.getMaDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) : ""))
                .collect(Collectors.toList());

        Double qtyOnOrder = poItem != null && poItem.getQty() != null ? poItem.getQty() : 0.0;
        
        Double prevOffered = repository.sumTotalQtyByPoAndSrBeforeDate(poNo, poSr, call.getCreatedAt());
        prevOffered = prevOffered != null ? prevOffered : 0.0;

        List<Object[]> prevPassedRejected = railFinalInspectionLotResultsRepository.sumQtyByPoAndSrBeforeDate(poNo, poSr, call.getCreatedAt());
        Double prevPassed = 0.0;
        if (prevPassedRejected != null && !prevPassedRejected.isEmpty() && prevPassedRejected.get(0) != null) {
            Object[] arr = prevPassedRejected.get(0);
            prevPassed = arr[0] != null ? ((Number) arr[0]).doubleValue() : 0.0;
        }

        List<com.sarthi.SRailPad.entity.ieVerification.RailFinalInspectionLotResults> currentResults = railFinalInspectionLotResultsRepository.findAllByCallNo(callNo);
        Double qtyNowPassed = 0.0;
        Double qtyNowRejected = 0.0;
        for (com.sarthi.SRailPad.entity.ieVerification.RailFinalInspectionLotResults r : currentResults) {
            if (r.getAcceptedQty() != null) qtyNowPassed += r.getAcceptedQty().doubleValue();
            if (r.getRejectedQty() != null) qtyNowRejected += r.getRejectedQty().doubleValue();
        }

        Double qtyNowOffered = call.getTotalQty() != null ? call.getTotalQty().doubleValue() : 0.0;
        Double qtyStillDue = qtyOnOrder - prevPassed - qtyNowPassed;

        List<com.sarthi.SRailPad.entity.RailWorkflowTransaction> transitions = railWorkflowTransactionRepository.findByRequestIdOrderByCreatedDateAsc(callNo);
        java.util.Set<LocalDate> visitDatesSet = new java.util.HashSet<>();
        boolean inspectionStarted = false;
        if (transitions != null) {
            for (com.sarthi.SRailPad.entity.RailWorkflowTransaction wt : transitions) {
                String status = wt.getStatus() != null ? wt.getStatus() : "";
                String action = wt.getAction() != null ? wt.getAction() : "";
                if ("INSPECTION_INITIATION".equalsIgnoreCase(status) || "INITIATE_INSPECTION".equalsIgnoreCase(action) || "INITIATE_CALL".equalsIgnoreCase(action) || "INSPECTION_IN_PROGRESS".equalsIgnoreCase(status)) {
                    inspectionStarted = true;
                }
                if (inspectionStarted && wt.getCreatedDate() != null) {
                    visitDatesSet.add(wt.getCreatedDate().toLocalDate());
                }
                if ("INSPECTION_COMPLETE_CONFIRM".equalsIgnoreCase(status) || "INSPECTION_COMPLETE_CONFIRM".equalsIgnoreCase(action) || "FINISH".equalsIgnoreCase(action)) {
                    if (wt.getCreatedDate() != null) {
                        visitDatesSet.add(wt.getCreatedDate().toLocalDate());
                    }
                    break;
                }
            }
        }
        
        List<LocalDate> visitDates = visitDatesSet.stream().sorted().collect(Collectors.toList());
        String datesString = visitDates.stream().map(d -> d.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))).collect(Collectors.joining(", "));
        String visitsCount = visitDates.isEmpty() ? "0" : convertToWords(visitDates.size());

        String vendorName = poHeader != null ? extractVendorName(poHeader.getVendorDetails()) : "";
        String vendorAddr = poHeader != null ? extractVendorAddress(poHeader.getVendorDetails()) : "";
        String vendorFull = vendorName + (vendorAddr.isBlank() ? "" : ", " + vendorAddr);
        if (vendorFull.trim().isEmpty() && call.getVendorCode() != null) {
            Optional<com.sarthi.entity.VendorMaster> vendorOpt = vendorMasterRepository.findByVendorCode(call.getVendorCode());
            if (vendorOpt.isPresent()) {
                vendorFull = vendorOpt.get().getVendorName();
            }
        }

        String poDateStr = poHeader != null && poHeader.getPoDate() != null ? poHeader.getPoDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) : "";
        
        String uom = poItem != null && poItem.getUom() != null ? poItem.getUom() : "Nos";
        
        String passedWordsTemplate = String.format("QUANTITY NOW PASSED %s %s ONLY INCLUDING ONE NOS CONSUMED IN MF TESTING. %s NOS. REJECTED DURING INSPECTION AS PER ANNEXURE-I TO IC ATTACHED.", 
            convertToWords(qtyNowPassed.longValue()), uom.toUpperCase(), convertToWords(qtyNowRejected.longValue()));

        String rejectionReasonTemplate = qtyNowRejected > 0 ? "REJECTED DURING INSPECTION AS DETAILED IN ANNEXURE-I" : "Not Applicable";

        String certificateNo = railInspectionCompleteDetailsRepository.findFirstByCallNoOrderByCreatedOnDesc(callNo)
                .map(RailInspectionCompleteDetails::getCertificateNo)
                .orElse("");

        com.sarthi.SRailPad.dto.RailpadIcCertificateDto dto = new com.sarthi.SRailPad.dto.RailpadIcCertificateDto();
        dto.setBookNo("");
        dto.setSetNo("");
        dto.setCertificateDate(LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        dto.setCertificateNo(certificateNo);
        dto.setOfferedInsttNo("");
        dto.setPassedInsttNo("");
        dto.setContractorName(vendorFull);
        dto.setPlaceOfInspection(vendorFull);
        dto.setContractReferences("PO NO. " + poNo + (poDateStr.isEmpty() ? "" : " dated " + poDateStr));
        dto.setLatest4Amendments(latest4Amendments);
        dto.setBillPayingOfficer(poItem != null ? poItem.getBillPayOffDesc() : "");
        dto.setConsignee(poItem != null ? poItem.getConsigneeDetail() : "");
        String purchaserDetail = poHeader != null ? poHeader.getPurchaserDetail() : "";
        dto.setPurchasingAuthority(purchaserDetail);
        dto.setItemNo(poItem != null && poItem.getItemSrNo() != null && !poItem.getItemSrNo().trim().isEmpty() ? poItem.getItemSrNo() : poSr);
        dto.setDescriptionOfStores(poItem != null ? poItem.getItemDesc() : "");
        dto.setQuantityOnOrder(qtyOnOrder);
        dto.setCumulativeQtyOfferedPreviously(prevOffered);
        dto.setQtyPrevPassed(prevPassed);
        dto.setQtyNowOffered(qtyNowOffered);
        dto.setQtyNowPassed(qtyNowPassed);
        dto.setQtyNowRejected(qtyNowRejected);
        dto.setQtyStillDue(qtyStillDue);
        dto.setUom(uom);
        dto.setQuantityNowPassedInWords(passedWordsTemplate);
        dto.setNoOfItemsChecked("ONE");
        dto.setDateOfCall(call.getInspectionDate() != null ? call.getInspectionDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) : "");
        dto.setNoOfVisits(visitsCount);
        dto.setDateOfInspection(datesString);
        dto.setTrRecDt("");
        dto.setReasonOfRejection(rejectionReasonTemplate);

        return dto;
    }

    @Override
    public List<RailInspectionCall> getProcessCallsByTypeDrawingAndPlant(String railPadType, String drawingNo, String plantId) {
        return repository.findProcessCallsByTypeAndDrawingAndPlant(railPadType, drawingNo, plantId);
    }

    @Override
    public List<RailInspectionCall> getProcessCalls(String railPadType, String drawingNo, String plantId, String poNo, String poSr) {
        if ((poNo != null && !poNo.isEmpty()) || (poSr != null && !poSr.isEmpty())) {
            List<RailInspectionCompleteDetails> completeDetails = railInspectionCompleteDetailsRepository.findProcessCallsByPoNoAndSr(poNo, poSr);
            if (completeDetails != null && !completeDetails.isEmpty()) {
                List<String> callNos = completeDetails.stream()
                        .map(RailInspectionCompleteDetails::getCallNo)
                        .filter(c -> c != null && c.toUpperCase().startsWith("RPP"))
                        .distinct()
                        .toList();

                if (!callNos.isEmpty()) {
                    List<RailInspectionCall> calls = repository.findByCallNoIn(callNos);
                    if (calls != null && !calls.isEmpty()) {
                        return calls;
                    }
                }
            }
            return repository.findProcessCalls(railPadType, drawingNo, plantId, poNo, poSr);
        }
        return repository.findProcessCallsByTypeAndDrawingAndPlant(railPadType, drawingNo, plantId);
    }

    @Override
    @Transactional
    public RailInspectionCall modifyCall(com.sarthi.SRailPad.dto.RailCallModificationDto dto) {
        if (dto == null || dto.getCallNo() == null || dto.getCallNo().trim().isEmpty()) {
            throw new IllegalArgumentException("Call No is required for modification.");
        }

        RailInspectionCall call = repository.findByCallNo(dto.getCallNo())
                .orElseThrow(() -> new RuntimeException("Inspection call not found: " + dto.getCallNo()));

        String user = dto.getUpdatedBy() != null && !dto.getUpdatedBy().isBlank() ? dto.getUpdatedBy() : "Vendor";
        List<com.sarthi.SRailPad.entity.inspectionCall.RailInspectionCallAudit> auditList = new ArrayList<>();

        // 1. Check Type of Rail Pad
        if (dto.getRailPadType() != null && !dto.getRailPadType().trim().isEmpty() &&
                !dto.getRailPadType().trim().equalsIgnoreCase(call.getRailPadType() != null ? call.getRailPadType().trim() : "")) {
            auditList.add(createAuditObject(call.getCallNo(), "Type of Rail Pad", call.getRailPadType(), dto.getRailPadType().trim(), user));
            call.setRailPadType(dto.getRailPadType().trim());
        }

        // 2. Fetch process details once (No N+1)
        com.sarthi.SRailPad.entity.inspectionCall.RailProcessCallDetails processDetails =
                processCallDetailsRepository.findByInspectionCall_CallNo(call.getCallNo()).orElse(null);

        String oldDrawingNo = processDetails != null && processDetails.getDrawingNo() != null ? processDetails.getDrawingNo() : call.getDrawingNo();
        if (dto.getDrawingNo() != null && !dto.getDrawingNo().trim().isEmpty() &&
                !dto.getDrawingNo().trim().equalsIgnoreCase(oldDrawingNo != null ? oldDrawingNo.trim() : "")) {
            auditList.add(createAuditObject(call.getCallNo(), "Drawing No", oldDrawingNo, dto.getDrawingNo().trim(), user));
            call.setDrawingNo(dto.getDrawingNo().trim());
            if (processDetails != null) {
                processDetails.setDrawingNo(dto.getDrawingNo().trim());
            }
        }

        // 3. Check Quantity Desired For Final Inspection
        if (dto.getTotalQty() != null && dto.getTotalQty() > 0 &&
                !dto.getTotalQty().equals(call.getTotalQty())) {
            String oldQty = call.getTotalQty() != null ? String.valueOf(call.getTotalQty()) : "0";
            String newQty = String.valueOf(dto.getTotalQty());
            auditList.add(createAuditObject(call.getCallNo(), "Quantity Desired for Final Inspection", oldQty, newQty, user));
            call.setTotalQty(dto.getTotalQty());
            if (processDetails != null) {
                processDetails.setQtyDesiredForFinal(dto.getTotalQty());
            }
        }

        // 4. Check Approx. Date of Production Initiation / Desired Inspection Date
        if (dto.getInspectionDate() != null && !dto.getInspectionDate().trim().isEmpty()) {
            try {
                LocalDate newDate = LocalDate.parse(dto.getInspectionDate().trim());
                LocalDate oldDate = call.getInspectionDate();
                if (oldDate == null || !oldDate.equals(newDate)) {
                    String oldDateStr = oldDate != null ? oldDate.toString() : "N/A";
                    auditList.add(createAuditObject(call.getCallNo(), "Approx. Date of Production Initiation", oldDateStr, newDate.toString(), user));
                    call.setInspectionDate(newDate);
                    if (processDetails != null) {
                        processDetails.setProductionInitiationDate(newDate);
                    }
                }
            } catch (Exception e) {
                throw new IllegalArgumentException("Invalid date format: " + dto.getInspectionDate());
            }
        }

        if (processDetails != null) {
            processCallDetailsRepository.save(processDetails);
        }

        // Batch save audit entries in a single query (No N+1 queries)
        if (!auditList.isEmpty()) {
            railInspectionCallAuditRepository.saveAll(auditList);
        }

        RailInspectionCall savedCall = repository.save(call);
        enrichCallData(savedCall);
        return savedCall;
    }

    private com.sarthi.SRailPad.entity.inspectionCall.RailInspectionCallAudit createAuditObject(String callNo, String fieldName, String oldValue, String newValue, String user) {
        com.sarthi.SRailPad.entity.inspectionCall.RailInspectionCallAudit audit = new com.sarthi.SRailPad.entity.inspectionCall.RailInspectionCallAudit();
        audit.setCallNo(callNo);
        audit.setFieldName(fieldName);
        audit.setOldValue(oldValue != null ? oldValue : "N/A");
        audit.setNewValue(newValue != null ? newValue : "N/A");
        audit.setCreatedBy(user);
        audit.setUpdatedBy(user);
        return audit;
    }
}
