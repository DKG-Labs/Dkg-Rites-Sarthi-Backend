package com.sarthi.Sleeper.controller;

import com.sarthi.Sleeper.dto.RawMaterialConsumptionDto;
import com.sarthi.Sleeper.service.RawMaterialConsumptionService;
import com.sarthi.Sleeper.service.SleeperWorkflowService;
import com.sarthi.Sleeper.dto.SleeperTransitionActionReqDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/rm-consumption")
@CrossOrigin(origins = "*", maxAge = 3600)
public class RawMaterialConsumptionController {

    @Autowired
    private RawMaterialConsumptionService service;

    @Autowired
    private SleeperWorkflowService sleeperWorkflowService;

    @PostMapping
    public ResponseEntity<Map<String, Object>> createConsumption(@RequestBody RawMaterialConsumptionDto dto) {
        RawMaterialConsumptionDto savedDto = service.saveConsumption(dto);
        
        // Initiate workflow
        if (savedDto.getNumericId() != null && dto.getCreatedBy() != null) {
            String requestId = String.valueOf(savedDto.getNumericId());
            Long moduleId = getModuleIdForMaterial(dto.getRawMaterial());
            Long workflowId = 1L;
            if (moduleId != null) {
                sleeperWorkflowService.initiateWorkflow(requestId, moduleId, workflowId, Long.valueOf(dto.getCreatedBy()), dto.getVendorCode(), dto.getPlantId());
            }
        }

        Map<String, Object> response = new HashMap<>();
        response.put("responseData", savedDto);
        response.put("message", "Consumption record created successfully");
        return ResponseEntity.ok(response);
    }

    private Long getModuleIdForMaterial(String materialName) {
        if (materialName == null) return 13L; // Default fallback
        switch (materialName.toLowerCase()) {
            case "hts wire":
            case "hts-wire":
                return 13L; // HTS RM Consumption Records
            case "cement":
                return 14L; // CEMENT RM Consumption Records
            case "admixture":
                return 15L; // ADMIXTURE RM Consumption Records
            case "aggregates":
            case "aggregate":
                return 16L; // AGGREGATE RM Consumption Records
            case "sgci insert":
            case "sgci-insert":
                return 17L; // SGCI Insert RM Consumption Records
            case "dowel":
                return 18L; // DOWEL RM Consumption Records
            default:
                return 13L; // Default to HTS if unknown for now
        }
    }

    @PutMapping("/{id:\\d+}")
    public ResponseEntity<Map<String, Object>> updateConsumption(@PathVariable Long id, @RequestBody RawMaterialConsumptionDto dto) {
        dto.setNumericId(id);
        
        // Fetch existing to compare status
        RawMaterialConsumptionDto existing = service.getConsumptionById(id);
        
        RawMaterialConsumptionDto savedDto = service.saveConsumption(dto);
        
        // Perform transition if status changed to Completed/Verified
        if (existing != null && 
            !"Completed".equalsIgnoreCase(existing.getStatus()) && 
            "Completed".equalsIgnoreCase(dto.getStatus()) && 
            dto.getUpdatedBy() != null) {
            
            SleeperTransitionActionReqDto actionReq = new SleeperTransitionActionReqDto();
            actionReq.setRequestId(String.valueOf(id));
            actionReq.setAction("IE_VERIFY"); 
            actionReq.setActionBy(Long.valueOf(dto.getUpdatedBy()));
            actionReq.setRemarks("Verified by IE");
            
            try {
                sleeperWorkflowService.performTransitionAction(actionReq);
            } catch (Exception e) {
                System.out.println("Workflow transition failed for RM consumption: " + e.getMessage());
            }
        }

        Map<String, Object> response = new HashMap<>();
        response.put("responseData", savedDto);
        response.put("message", "Consumption record updated successfully");
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id:\\d+}")
    public ResponseEntity<Map<String, Object>> getConsumptionById(@PathVariable Long id) {
        RawMaterialConsumptionDto dto = service.getConsumptionById(id);
        Map<String, Object> response = new HashMap<>();
        response.put("responseData", dto);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/by-plant")
    public ResponseEntity<Map<String, Object>> getAllByPlant(
            @RequestParam("plantId") String plantId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Map<String, Object> response = new HashMap<>();
        try {
            org.springframework.data.domain.Page<RawMaterialConsumptionDto> pageResult = service.getAllConsumptionByPlant(plantId, page, size);
            response.put("responseData", pageResult.getContent());
            response.put("currentPage", pageResult.getNumber());
            response.put("totalItems", pageResult.getTotalElements());
            response.put("totalPages", pageResult.getTotalPages());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("message", "Failed to fetch records: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    @GetMapping("/by-plant-material")
    public ResponseEntity<Map<String, Object>> getAllByPlantAndMaterial(
            @RequestParam("plantId") String plantId, 
            @RequestParam("material") String material,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Map<String, Object> response = new HashMap<>();
        try {
            org.springframework.data.domain.Page<RawMaterialConsumptionDto> pageResult = service.getAllConsumptionByPlantAndMaterial(plantId, material, page, size);
            response.put("responseData", pageResult.getContent());
            response.put("currentPage", pageResult.getNumber());
            response.put("totalItems", pageResult.getTotalElements());
            response.put("totalPages", pageResult.getTotalPages());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("message", "Failed to fetch records: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    @DeleteMapping("/{id:\\d+}")
    public ResponseEntity<Map<String, Object>> deleteConsumption(@PathVariable Long id) {
        Map<String, Object> response = new HashMap<>();
        try {
            service.deleteConsumption(id);
            response.put("message", "Consumption record deleted successfully");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("message", "Failed to delete record: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    @GetMapping("/{id:\\d+}/history")
    public ResponseEntity<Map<String, Object>> getConsumptionHistory(@PathVariable Long id) {
        Map<String, Object> response = new HashMap<>();
        try {
            var history = sleeperWorkflowService.workflowTransitionHistory(String.valueOf(id));
            response.put("responseData", history);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("message", "Failed to fetch history");
            return ResponseEntity.badRequest().body(response);
        }
    }
}
