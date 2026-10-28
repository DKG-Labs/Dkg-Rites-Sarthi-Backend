package com.sarthi.SRailPad.controller;

import com.sarthi.SRailPad.dto.RailpadPoiIeMappingReqDto;
import com.sarthi.SRailPad.repository.RailPoiIeMappingRepository;
import com.sarthi.SRailPad.service.RailWorkflowService;
import com.sarthi.util.ResponseBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/railpadMapping")
public class RailpadMapping {

    @Autowired
    private RailWorkflowService railWorkflowService;

    @Autowired
    private RailPoiIeMappingRepository railPoiIeMappingRepository;

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
        boolean exists = railPoiIeMappingRepository
                .existsByPoiCodeAndPlantIdAndIeUserIdAndIeType(poiCode, plantId, userId, ieType);
        return ResponseEntity.ok(Map.of("exists", exists));
    }
}
