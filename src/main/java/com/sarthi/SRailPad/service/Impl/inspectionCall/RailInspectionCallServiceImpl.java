package com.sarthi.SRailPad.service.Impl.inspectionCall;

import com.sarthi.SRailPad.entity.inspectionCall.RailInspectionCall;
import com.sarthi.SRailPad.entity.inspectionCall.RailInspectionLot;
import com.sarthi.SRailPad.entity.inspectionCall.RailInspectionBatch;
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
        return repository.findAllByVendorCode(vendorCode);
    }

    @Override
    public RailInspectionCall getById(Long id) {
        return repository.findById(id).orElse(null);
    }

    @Override
    public RailInspectionCall getByCallNo(String callNo) {
        return repository.findByCallNo(callNo).orElse(null);
    }
}
