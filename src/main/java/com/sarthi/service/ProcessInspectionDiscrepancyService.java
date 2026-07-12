package com.sarthi.service;

import com.sarthi.entity.ProcessInspectionDiscrepancy;
import java.util.List;
import java.util.Map;

import org.springframework.web.multipart.MultipartFile;

public interface ProcessInspectionDiscrepancyService {

    ProcessInspectionDiscrepancy createDiscrepancy(ProcessInspectionDiscrepancy discrepancy, String poiCode, MultipartFile file);
    
    byte[] getDecompressedDocument(String discrepancyNo);
    
    ProcessInspectionDiscrepancy updateDiscrepancy(Long id, ProcessInspectionDiscrepancy discrepancyDetails, Integer actionBy);

    void deleteDiscrepancy(Long id, Integer actionBy);

    ProcessInspectionDiscrepancy vendorRectification(String discrepancyNo, ProcessInspectionDiscrepancy rectificationDetails, Integer actionBy);
    
    List<Map<String, Object>> getCompletedDiscrepancies(Integer roleId, String productType, Integer userId);

    List<Map<String, Object>> getVendorsByProduct(String productType);

    List<Map<String, Object>> getPlantsByVendor(String vendorCode);
}
