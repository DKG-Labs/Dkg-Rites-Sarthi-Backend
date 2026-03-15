package com.sarthi.Sleeper.controller;

import com.sarthi.Sleeper.dto.FinalInspectionDtos.BatchInspectionDetailDto;
import com.sarthi.Sleeper.dto.FinalInspectionDtos.BatchTestingListResponseDto;
import com.sarthi.Sleeper.dto.FinalInspectionDtos.InspectionSaveRequestDto;
import com.sarthi.Sleeper.service.ProductionFinalInspectionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/FinalInspectionController")
public class FinalProductionInspectionController {

    @Autowired
    private ProductionFinalInspectionService inspectionService;

    @PostMapping("/save")
    public ResponseEntity<String> saveInspection(
            @RequestBody InspectionSaveRequestDto dto) {

        inspectionService.saveInspection(dto);

        return ResponseEntity.ok("Inspection Saved Successfully");
    }
    @GetMapping("/inspection/batches")
    public List<BatchTestingListResponseDto> getAllBatches() {
        return inspectionService.getAllBatchTesting();
    }

    @GetMapping("/inspection/batch/{batchId}")
    public BatchInspectionDetailDto getBatchInspection(
            @PathVariable Long batchId) {

        return inspectionService.getBatchInspection(batchId);
    }
}
