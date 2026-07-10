package com.sarthi.service.Impl;

import com.sarthi.constant.AppConstant;
import com.sarthi.dto.FeedbackTransitionActionReqDto;
import com.sarthi.dto.FeedbackWorkflowTransitionDto;
import com.sarthi.entity.ProcessInspectionDiscrepancy;
import com.sarthi.exception.BusinessException;
import com.sarthi.exception.ErrorDetails;
import com.sarthi.repository.ProcessInspectionDiscrepancyRepository;
import com.sarthi.service.FeedbackWorkflowService;
import com.sarthi.service.ProcessInspectionDiscrepancyService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.sarthi.entity.UserMaster;
import com.sarthi.entity.VendorMaster;
import com.sarthi.entity.PincodePoIMapping;
import com.sarthi.repository.UserMasterRepository;
import com.sarthi.repository.VendorMasterRepository;
import com.sarthi.repository.PincodePoIMappingRepository;
import org.springframework.web.multipart.MultipartFile;
import com.sarthi.util.FileCompressionUtil;
import com.sarthi.service.AzureBlobStorageService;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ProcessInspectionDiscrepancyServiceImpl implements ProcessInspectionDiscrepancyService {

    private final ProcessInspectionDiscrepancyRepository repository;
    private final FeedbackWorkflowService feedbackWorkflowService;
    private final UserMasterRepository userMasterRepository;
    private final VendorMasterRepository vendorMasterRepository;
    private final PincodePoIMappingRepository pincodePoIMappingRepository;
    private final AzureBlobStorageService azureBlobStorageService;

    @Override
    @Transactional
    public ProcessInspectionDiscrepancy createDiscrepancy(ProcessInspectionDiscrepancy discrepancy, String poiCode, MultipartFile file) {
        
        // If poiCode is invalid ("undefined" or null or empty), fetch it from DB using vendorCode
        if (poiCode == null || poiCode.trim().isEmpty() || "undefined".equalsIgnoreCase(poiCode.trim())) {
            List<PincodePoIMapping> mappings = pincodePoIMappingRepository.findByVendorCode(discrepancy.getVendorCode());
            if (mappings != null && !mappings.isEmpty()) {
                poiCode = mappings.get(0).getPoiCode();
            }
        }

        // Generate Discrepancy No
        String prefix = getPrefixByProductType(discrepancy.getProductType()) + "-" + LocalDate.now().format(DateTimeFormatter.ofPattern("MMddyy"));
        Optional<ProcessInspectionDiscrepancy> lastDiscrepancy = repository.findTopByDiscrepancyNoStartingWithOrderByDiscrepancyNoDesc(prefix);
        
        int nextSequence = 1;
        if (lastDiscrepancy.isPresent()) {
            String lastNo = lastDiscrepancy.get().getDiscrepancyNo();
            String seqStr = lastNo.substring(prefix.length());
            try {
                nextSequence = Integer.parseInt(seqStr) + 1;
            } catch (NumberFormatException ignored) {}
        }
        String generatedNo = prefix + String.format("%03d", nextSequence);
        discrepancy.setDiscrepancyNo(generatedNo);
        discrepancy.setDateOfRaising(LocalDate.now());
        discrepancy.setStatus("PENDING_RECTIFICATION"); // Initial status

        if (file != null && !file.isEmpty()) {
            try {
                String fileName = generatedNo + "_" + file.getOriginalFilename();
                byte[] compressedBytes = FileCompressionUtil.compress(file.getBytes());
                // Temporarily bypassing Azure Blob Storage upload for local testing
                // azureBlobStorageService.uploadFileBytes(compressedBytes, fileName);
                discrepancy.setIeDocumentPath(fileName);
            } catch (Exception e) {
                // Temporarily just logging the error instead of blocking creation
                System.out.println("Failed to compress and upload file: " + e.getMessage());
                // throw new BusinessException(new ErrorDetails(AppConstant.ERROR_CODE_RESOURCE, AppConstant.ERROR_TYPE_CODE_RESOURCE, AppConstant.ERROR_TYPE_VALIDATION, "Failed to compress and upload file"));
            }
        }

        ProcessInspectionDiscrepancy saved = repository.save(discrepancy);

        // Initiate workflow
        feedbackWorkflowService.initiateFeedbackWorkflow(
                generatedNo,
                discrepancy.getCreatedBy(),
                discrepancy.getProductType(),
                poiCode,
                discrepancy.getPlantId()
        );

        return saved;
    }

    private String getPrefixByProductType(String productType) {
        if (productType == null) return "DX";
        String type = productType.toUpperCase();
        if (type.contains("ERC")) return "DE";
        if (type.contains("SLEEPER")) return "DS";
        if (type.contains("RAILPAD") || type.contains("RAIL PAD")) return "DR";
        return "DX";
    }

    @Override
    @Transactional
    public ProcessInspectionDiscrepancy updateDiscrepancy(Long id, ProcessInspectionDiscrepancy discrepancyDetails, Integer actionBy) {
        ProcessInspectionDiscrepancy existing = repository.findById(id)
                .orElseThrow(() -> new BusinessException(new ErrorDetails(AppConstant.ERROR_CODE_RESOURCE, AppConstant.ERROR_TYPE_CODE_RESOURCE, AppConstant.ERROR_TYPE_VALIDATION, "Discrepancy not found")));

        if (!existing.getStatus().equals("PENDING_RECTIFICATION")) {
            throw new BusinessException(new ErrorDetails(AppConstant.ERROR_CODE_RESOURCE, AppConstant.ERROR_TYPE_CODE_RESOURCE, AppConstant.ERROR_TYPE_VALIDATION, "Cannot edit discrepancy after vendor action"));
        }

        existing.setCategory(discrepancyDetails.getCategory());
        existing.setSubCategory(discrepancyDetails.getSubCategory());
        existing.setUrgency(discrepancyDetails.getUrgency());
        existing.setDescription(discrepancyDetails.getDescription());
        if (discrepancyDetails.getIeDocumentPath() != null) {
            existing.setIeDocumentPath(discrepancyDetails.getIeDocumentPath());
        }

        return repository.save(existing);
    }

    @Override
    @Transactional
    public void deleteDiscrepancy(Long id, Integer actionBy) {
        ProcessInspectionDiscrepancy existing = repository.findById(id)
                .orElseThrow(() -> new BusinessException(new ErrorDetails(AppConstant.ERROR_CODE_RESOURCE, AppConstant.ERROR_TYPE_CODE_RESOURCE, AppConstant.ERROR_TYPE_VALIDATION, "Discrepancy not found")));

        if (!existing.getStatus().equals("PENDING_RECTIFICATION")) {
            throw new BusinessException(new ErrorDetails(AppConstant.ERROR_CODE_RESOURCE, AppConstant.ERROR_TYPE_CODE_RESOURCE, AppConstant.ERROR_TYPE_VALIDATION, "Cannot delete discrepancy after vendor action"));
        }

        repository.delete(existing);
    }

    @Override
    @Transactional
    public ProcessInspectionDiscrepancy vendorRectification(String discrepancyNo, ProcessInspectionDiscrepancy rectificationDetails, Integer actionBy) {
        ProcessInspectionDiscrepancy existing = repository.findByDiscrepancyNo(discrepancyNo)
                .orElseThrow(() -> new BusinessException(new ErrorDetails(AppConstant.ERROR_CODE_RESOURCE, AppConstant.ERROR_TYPE_CODE_RESOURCE, AppConstant.ERROR_TYPE_VALIDATION, "Discrepancy not found")));

        existing.setDateOfRectification(rectificationDetails.getDateOfRectification());
        existing.setCorrectiveAction(rectificationDetails.getCorrectiveAction());
        if (rectificationDetails.getVendorDocumentPath() != null) {
            existing.setVendorDocumentPath(rectificationDetails.getVendorDocumentPath());
        }
        existing.setStatus("PENDING_IE_VERIFICATION");

        ProcessInspectionDiscrepancy saved = repository.save(existing);

        return saved;
    }

    @Override
    public List<Map<String, Object>> getCompletedDiscrepancies(Integer roleId, String productType, Integer userId) {
        List<ProcessInspectionDiscrepancy> completed = repository.findCompletedDiscrepanciesByUserId(userId);
        
        List<Map<String, Object>> result = new ArrayList<>();
        for (ProcessInspectionDiscrepancy d : completed) {
            if (productType != null && !productType.isEmpty() && !d.getProductType().equalsIgnoreCase(productType)) {
                continue;
            }
            Map<String, Object> map = new HashMap<>();
            map.put("discrepancy", d);
            result.add(map);
        }
        return result;
    }

    @Override
    public List<Map<String, Object>> getVendorsByProduct(String productType) {
        Integer roleId = null;
        if ("ERC".equalsIgnoreCase(productType)) {
            roleId = 1; // Explicitly requested by user for ERC
        } else if ("Sleeper".equalsIgnoreCase(productType)) {
            roleId = 12;
        } else if ("Rail Pad".equalsIgnoreCase(productType) || "RailPad".equalsIgnoreCase(productType)) {
            roleId = 13;
        } else {
            roleId = 1; // Fallback
        }

        List<Map<String, Object>> vendors = new ArrayList<>();
        if (roleId != null) {
            List<UserMaster> users = userMasterRepository.findUsersByRoleId(roleId);
            for (UserMaster user : users) {
                String vendorCode = user.getUsername();
                if (vendorCode == null || vendorCode.trim().isEmpty()) continue;
                
                vendorMasterRepository.findByVendorCode(vendorCode).ifPresent(vendor -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("vendorCode", vendor.getVendorCode());
                    map.put("vendorName", vendor.getVendorName());
                    vendors.add(map);
                });
            }
        }
        return vendors;
    }

    @Override
    public List<Map<String, Object>> getPlantsByVendor(String vendorCode) {
        List<PincodePoIMapping> mappings = pincodePoIMappingRepository.findByVendorCode(vendorCode);
        List<Map<String, Object>> plants = new ArrayList<>();
        
        for (PincodePoIMapping mapping : mappings) {
            Map<String, Object> map = new HashMap<>();
            map.put("plantId", mapping.getId()); // or some other identifier if needed
            map.put("unitName", mapping.getUnitName());
            plants.add(map);
        }
        return plants;
    }

    @Override
    public byte[] getDecompressedDocument(String discrepancyNo) {
        ProcessInspectionDiscrepancy existing = repository.findByDiscrepancyNo(discrepancyNo)
                .orElseThrow(() -> new BusinessException(new ErrorDetails(AppConstant.ERROR_CODE_RESOURCE, AppConstant.ERROR_TYPE_CODE_RESOURCE, AppConstant.ERROR_TYPE_VALIDATION, "Discrepancy not found")));

        String fileName = existing.getIeDocumentPath();
        if (fileName == null || fileName.isEmpty()) {
            throw new BusinessException(new ErrorDetails(AppConstant.ERROR_CODE_RESOURCE, AppConstant.ERROR_TYPE_CODE_RESOURCE, AppConstant.ERROR_TYPE_VALIDATION, "No document attached to this discrepancy"));
        }

        try {
            byte[] compressedBytes = azureBlobStorageService.downloadFile(fileName);
            return FileCompressionUtil.decompress(compressedBytes);
        } catch (Exception e) {
            throw new BusinessException(new ErrorDetails(AppConstant.ERROR_CODE_RESOURCE, AppConstant.ERROR_TYPE_CODE_RESOURCE, AppConstant.ERROR_TYPE_VALIDATION, "Failed to download and decompress document"));
        }
    }
}
