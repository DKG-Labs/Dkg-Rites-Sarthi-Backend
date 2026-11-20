package com.sarthi.SRailPad.controller;

import com.sarthi.SRailPad.dto.RailpadPoiIeMappingReqDto;
import com.sarthi.SRailPad.entity.raipadMapping.RailPoiIeMapping;
import com.sarthi.SRailPad.repository.RailPoiIeMappingRepository;
import com.sarthi.SRailPad.service.RailWorkflowService;
import com.sarthi.entity.UserMaster;
import com.sarthi.repository.UserMasterRepository;
import com.sarthi.util.ResponseBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/railpadMapping")
public class RailpadMapping {

    @Autowired
    private RailWorkflowService railWorkflowService;

    @Autowired
    private RailPoiIeMappingRepository railPoiIeMappingRepository;

    @Autowired(required = false)
    private UserMasterRepository userMasterRepository;

    @PostMapping
    public ResponseEntity<Object> createMapping(@RequestBody RailpadPoiIeMappingReqDto req) {
        return new ResponseEntity<Object>(
            ResponseBuilder.getSuccessResponse(railWorkflowService.saveRailpadMapping(req)),
            HttpStatus.OK
        );
    }

    @GetMapping("/check-duplicate")
    public ResponseEntity<Object> checkDuplicateRailpad(
            @RequestParam Integer userId,
            @RequestParam String plantId,
            @RequestParam String poiCode,
            @RequestParam String ieType) {
        
        List<RailPoiIeMapping> existing = railPoiIeMappingRepository
                .findMappingsByPlantOrPoiAndIeType(plantId, poiCode, ieType);
        
        if (existing != null && !existing.isEmpty()) {
            for (RailPoiIeMapping m : existing) {
                if (m.getIeUserId() != null && !m.getIeUserId().equals(userId)) {
                    String ieName = String.valueOf(m.getIeUserId());
                    if (userMasterRepository != null) {
                        try {
                            UserMaster u = userMasterRepository.findById(m.getIeUserId()).orElse(null);
                            if (u != null) {
                                ieName = (u.getEmployeeCode() != null ? u.getEmployeeCode() + " - " : "") + (u.getUsername() != null ? u.getUsername() : "");
                            }
                        } catch (Exception ignored) {}
                    }
                    String stageLabel = ieType.toUpperCase().contains("PROCESS") ? "Process" : "Final (Main IE)";
                    return ResponseEntity.ok(Map.of(
                            "exists", true,
                            "message", "This plant is already mapped to IE (" + ieName + ") for Rail Pad " + stageLabel + ". Only one IE can be mapped."
                    ));
                }
            }
            return ResponseEntity.ok(Map.of(
                    "exists", true,
                    "message", "This plant is already mapped to this IE."
            ));
        }

        boolean exists = railPoiIeMappingRepository
                .existsByPoiCodeAndPlantIdAndIeUserIdAndIeType(poiCode, plantId, userId, ieType);
        return ResponseEntity.ok(Map.of("exists", exists));
    }
}
