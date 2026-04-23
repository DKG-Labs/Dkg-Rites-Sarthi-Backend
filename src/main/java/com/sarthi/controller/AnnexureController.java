package com.sarthi.controller;

import com.sarthi.dto.*;
import com.sarthi.service.AnnexureService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/annexures")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*") // Adjust as per security requirements
public class AnnexureController {

    private final AnnexureService annexureService;

    /**
     * Endpoint to fetch data for Chemical Analysis Annexure (Annexure-I).
     *
     * @param callNo The inspection call number
     * @return Chemical analysis report data
     */
    @GetMapping("/chemical-analysis/{callNo}")
    public ResponseEntity<ChemicalAnalysisResponseDTO> getChemicalAnalysisData(@PathVariable String callNo) {
        log.info("REST request to get Chemical Analysis data for call no: {}", callNo);
        try {
            ChemicalAnalysisResponseDTO data = annexureService.getChemicalAnalysisData(callNo);
            return ResponseEntity.ok(data);
        } catch (Exception e) {
            log.error("Error fetching chemical analysis data: ", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Endpoint to fetch data for Dimensional Check Annexure (Annexure-II).
     *
     * @param callNo The inspection call number
     * @return Dimensional check report data
     */
    @GetMapping("/dimensional-check/{callNo}")
    public ResponseEntity<DimensionalCheckResponseDTO> getDimensionalCheckData(@PathVariable String callNo) {
        log.info("REST request to get Dimensional Check data for call no: {}", callNo);
        try {
            DimensionalCheckResponseDTO data = annexureService.getDimensionalCheckData(callNo);
            return ResponseEntity.ok(data);
        } catch (Exception e) {
            log.error("Error fetching dimensional check data: ", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Endpoint to fetch data for Final Chemical Analysis Annexure (Annexure-VI).
     *
     * @param callNo The inspection call number
     * @return Final chemical analysis report data
     */
    @GetMapping("/final-chemical-analysis/{callNo}")
    public ResponseEntity<FinalChemicalAnalysisResponseDTO> getFinalChemicalAnalysisData(@PathVariable String callNo) {
        log.info("REST request to get Final Chemical Analysis data for call no: {}", callNo);
        try {
            FinalChemicalAnalysisResponseDTO data = annexureService.getFinalChemicalAnalysisData(callNo);
            return ResponseEntity.ok(data);
        } catch (Exception e) {
            log.error("Error fetching final chemical analysis data: ", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Endpoint to fetch data for Final Hardness Test Annexure (Annexure-VIII).
     *
     * @param callNo The inspection call number
     * @return Final hardness test report data
     */
    @GetMapping("/final-hardness-test/{callNo}")
    public ResponseEntity<HardnessAnnexureResponseDTO> getFinalHardnessTestData(@PathVariable String callNo) {
        log.info("REST request to get Final Hardness Test data for call no: {}", callNo);
        try {
            HardnessAnnexureResponseDTO data = annexureService.getFinalHardnessTestData(callNo);
            return ResponseEntity.ok(data);
        } catch (Exception e) {
            log.error("Error fetching final hardness test data: ", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Endpoint to fetch data for Final Toe Load Test Annexure (Annexure-XI).
     */
    @GetMapping("/final-toe-load-test/{callNo}")
    public ResponseEntity<ToeLoadAnnexureResponseDTO> getFinalToeLoadTestData(@PathVariable String callNo) {
        log.info("REST request to get Final Toe Load Test data for call no: {}", callNo);
        try {
            ToeLoadAnnexureResponseDTO data = annexureService.getFinalToeLoadTestData(callNo);
            return ResponseEntity.ok(data);
        } catch (Exception e) {
            log.error("Error fetching final toe load test data: ", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Endpoint to fetch data for Final Weight Test Annexure (Annexure-XV).
     *
     * @param callNo The inspection call number
     * @return Final weight test report data
     */
    @GetMapping("/final-weight-test/{callNo}")
    public ResponseEntity<WeightAnnexureResponseDTO> getFinalWeightTestData(@PathVariable String callNo) {
        log.info("REST request to get Final Weight Test data for call no: {}", callNo);
        try {
            WeightAnnexureResponseDTO data = annexureService.getFinalWeightTestData(callNo);
            return ResponseEntity.ok(data);
        } catch (Exception e) {
            log.error("Error fetching final weight test data: ", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Endpoint to fetch data for Process Inspection Annexure.
     * Contains Inclusion, Microstructure, Freedom, and Decarb tests.
     *
     * @param callNo The inspection call number
     * @return Process inspection test report data
     */
    @GetMapping("/final-inclusion/{callNo}")
    public ResponseEntity<FinalInclusionAnnexureResponseDTO> getFinalInclusionAnnexureData(@PathVariable String callNo) {
        log.info("REST request to get Process Inspection Annexure data for call no: {}", callNo);
        try {
            FinalInclusionAnnexureResponseDTO data = annexureService.getFinalInclusionAnnexureData(callNo);
            return ResponseEntity.ok(data);
        } catch (Exception e) {
            log.error("Error fetching process inspection annexure data: ", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Endpoint to fetch data for Application & Deflection Annexure.
     *
     * @param callNo The inspection call number
     * @return Application & Deflection report data
     */
    @GetMapping("/final-application-deflection/{callNo}")
    public ResponseEntity<FinalApplicationDeflectionResponseDTO> getFinalApplicationDeflectionData(@PathVariable String callNo) {
        log.info("REST request to get Application & Deflection data for call no: {}", callNo);
        try {
            FinalApplicationDeflectionResponseDTO data = annexureService.getFinalApplicationDeflectionData(callNo);
            return ResponseEntity.ok(data);
        } catch (Exception e) {
            log.error("Error fetching application & deflection data: ", e);
            return ResponseEntity.internalServerError().build();
        }
    }
}
