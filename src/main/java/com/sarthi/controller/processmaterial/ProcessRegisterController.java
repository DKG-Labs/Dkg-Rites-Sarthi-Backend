package com.sarthi.controller.processmaterial;

import com.sarthi.dto.processmaterial.ProcessInspectionRegisterResponseDTO;
import com.sarthi.service.ProcessRegisterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/process-annexure")
public class ProcessRegisterController {

    @Autowired
    private ProcessRegisterService registerService;

    @GetMapping("/register")
    public ResponseEntity<List<ProcessInspectionRegisterResponseDTO>> getRegister(
            @RequestParam String callNo,
            @RequestParam(required = false) String date,
            @RequestParam(required = false) String shift,
            @RequestParam(required = false) String createdBy,
            Authentication authentication) {
        
        String user = createdBy;
        if (user == null && authentication != null) {
            user = authentication.getName();
        }
        
        List<ProcessInspectionRegisterResponseDTO> response = registerService.getProcessInspectionRegister(callNo, date, shift, user);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/available-entries")
    public ResponseEntity<List<java.util.Map<String, Object>>> getAvailableEntries(
            @RequestParam String callNo,
            @RequestParam(required = false) String createdBy,
            Authentication authentication) {
        
        String user = createdBy;
        if (user == null && authentication != null) {
            user = authentication.getName();
        }
        
        return ResponseEntity.ok(registerService.getAvailableEntries(callNo, user));
    }

    @Autowired
    private com.sarthi.repository.processmaterial.ProcessLineFinalResultRepository debugRepo;

    @GetMapping("/debug-all")
    public ResponseEntity<List<java.util.Map<String, Object>>> getDebugAll(
            @RequestParam String callNo) {
        
        List<com.sarthi.entity.processmaterial.ProcessLineFinalResult> results = debugRepo.findByInspectionCallNo(callNo);
        List<java.util.Map<String, Object>> out = new java.util.ArrayList<>();
        for(var r : results) {
            java.util.Map<String, Object> m = new java.util.HashMap<>();
            m.put("createdBy", r.getCreatedBy());
            m.put("shift", r.getShift());
            m.put("lot", r.getLotNumber());
            m.put("call", r.getInspectionCallNo());
            out.add(m);
        }
        return ResponseEntity.ok(out);
    }
}
