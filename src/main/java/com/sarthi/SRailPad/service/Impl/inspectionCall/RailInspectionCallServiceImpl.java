package com.sarthi.SRailPad.service.Impl.inspectionCall;

import com.sarthi.SRailPad.entity.inspectionCall.RailInspectionCall;
import com.sarthi.SRailPad.entity.inspectionCall.RailInspectionLot;
import com.sarthi.SRailPad.entity.inspectionCall.RailInspectionBatch;
import com.sarthi.SRailPad.repository.RailWorkflowTransactionRepository;
import com.sarthi.SRailPad.repository.inspectionCall.RailInspectionCallRepository;
import com.sarthi.SRailPad.service.inspectionCall.RailInspectionCallService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class RailInspectionCallServiceImpl implements RailInspectionCallService {

    private final RailInspectionCallRepository repository;
    private final com.sarthi.repository.PoHeaderRepository poHeaderRepository;
    private final com.sarthi.repository.VendorMasterRepository vendorMasterRepository;
    private final RailWorkflowTransactionRepository railWorkflowTransactionRepository;

    @Override
    @Transactional
    public RailInspectionCall create(RailInspectionCall call) {
        // Generate Call No: RPF-MMDD001
        String datePart = LocalDate.now().format(DateTimeFormatter.ofPattern("MMdd"));
        String pattern = "RPF-" + datePart + "%";
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
        
        String generatedCallNo = String.format("RPF-%s%03d", datePart, seq);
        call.setCallNo(generatedCallNo);
        
        // Ensure bidirectional links are set for JPA cascade
        if (call.getLots() != null) {
            for (RailInspectionLot lot : call.getLots()) {
                lot.setInspectionCall(call);
                if (lot.getBatches() != null) {
                    for (RailInspectionBatch batch : lot.getBatches()) {
                        batch.setLot(lot);
                    }
                }
            }
        }
        
        return repository.save(call);
    }

    @Override
    public List<RailInspectionCall> getAllByVendorCode(String vendorCode) {
        List<RailInspectionCall> calls = repository.findAllByVendorCode(vendorCode);
        calls.forEach(this::enrichCallData);
        return calls;
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
    }

    private String extractVendorName(String raw) {
        if (raw == null || raw.isBlank()) return null;
        String[] parts = raw.split("~");
        String segment = parts[0];
        int dashIdx = segment.lastIndexOf('-');
        if (dashIdx > 0) return segment.substring(0, dashIdx).trim();
        return segment.trim();
    }
}
