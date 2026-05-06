package com.sarthi.service;

import com.sarthi.dto.processmaterial.ProcessInspectionRegisterResponseDTO;
import java.util.List;
import java.util.Map;

public interface ProcessRegisterService {
    List<ProcessInspectionRegisterResponseDTO> getProcessInspectionRegister(String callNo, String date, String shift, String createdBy);
    
    List<Map<String, Object>> getAvailableEntries(String callNo, String createdBy);
    
    void updateRemarks(String callNo, String shift, String lineNo, String lotNo, String createdBy, String remarks);
}
