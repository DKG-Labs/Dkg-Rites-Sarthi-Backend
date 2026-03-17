package com.sarthi.Sleeper.service.Impl;

import com.sarthi.Sleeper.dto.FinalInspectionDtos.WaterCubeStrengthDetailRequestDto;
import com.sarthi.Sleeper.dto.FinalInspectionDtos.WaterCubeStrengthTestRequestDto;
import com.sarthi.Sleeper.entity.FinalInspection.WaterCubeSampleDeclaration;
import com.sarthi.Sleeper.entity.FinalInspection.WaterCubeStrengthDetail;
import com.sarthi.Sleeper.entity.FinalInspection.WaterCubeStrengthTest;
import com.sarthi.Sleeper.repository.FinalInspectionRepository.WaterCubeSampleRepository;
import com.sarthi.Sleeper.repository.FinalInspectionRepository.WaterCubeStrengthTestRepository;
import com.sarthi.Sleeper.service.WaterCubeStrengthTestService;
import com.sarthi.util.ResponseBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class WaterCubeStrengthTestServiceImpl implements WaterCubeStrengthTestService {

    @Autowired
    private WaterCubeStrengthTestRepository waterCubeStrengthTestRepository;

    @Autowired
    private WaterCubeSampleRepository waterCubeSampleRepository;

    @Override
    public ResponseEntity<?> saveTestResult(WaterCubeStrengthTestRequestDto requestDto) {
        try {
            WaterCubeStrengthTest test = new WaterCubeStrengthTest();

            if (requestDto.getWaterCubeSampleDeclarationId() != null) {
                Optional<WaterCubeSampleDeclaration> sampleOpt = waterCubeSampleRepository.findById(requestDto.getWaterCubeSampleDeclarationId());
                sampleOpt.ifPresent(test::setWaterCubeSampleDeclaration);
            }

            test.setBatchNumber(requestDto.getBatchNumber());
            test.setConcreteGrade(requestDto.getConcreteGrade());
            if (requestDto.getCastingDate() != null && !requestDto.getCastingDate().isEmpty()) {
                test.setCastingDate(requestDto.getCastingDate());
            }
            test.setShift(requestDto.getShift());
            test.setLineNo(requestDto.getLineNo());
            test.setFckTarget(requestDto.getFckTarget());
            test.setAgeDays(requestDto.getAgeDays());
            
            test.setS1Avg(requestDto.getS1Avg());
            test.setS2Avg(requestDto.getS2Avg());
            test.setAvgX(requestDto.getAvgX());
            test.setMinY(requestDto.getMinY());
            test.setS1Variation(requestDto.getS1Variation());
            test.setS2Variation(requestDto.getS2Variation());
            
            test.setCondition1(requestDto.getCondition1());
            test.setCondition2(requestDto.getCondition2());
            test.setCondition3(requestDto.getCondition3());
            test.setMrSamplesRequired(requestDto.getMrSamplesRequired());
            test.setFinalTestResult(requestDto.getFinalTestResult());

            test.setCreatedBy(requestDto.getCreatedBy());
            test.setCreatedDate(LocalDateTime.now());

            List<WaterCubeStrengthDetail> detailsList = new ArrayList<>();
            if (requestDto.getDetails() != null) {
                for (WaterCubeStrengthDetailRequestDto detailDto : requestDto.getDetails()) {
                    WaterCubeStrengthDetail detail = new WaterCubeStrengthDetail();
                    detail.setSampleNumber(detailDto.getSampleNumber());
                    detail.setCubeIndex(detailDto.getCubeIndex());
                    detail.setCubeId(detailDto.getCubeId());
                    detail.setWeightKg(detailDto.getWeightKg());
                    detail.setLoadKn(detailDto.getLoadKn());
                    detail.setStrengthNmm2(detailDto.getStrengthNmm2());
                    
                    if (detailDto.getTestingDate() != null && !detailDto.getTestingDate().isEmpty()) {
                        detail.setTestingDate(detailDto.getTestingDate());
                    }
                    if (detailDto.getTestingTime() != null && !detailDto.getTestingTime().isEmpty()) {
                        detail.setTestingTime(detailDto.getTestingTime());
                    }

                    detail.setStrengthTest(test);
                    detailsList.add(detail);
                }
            }
            test.setDetails(detailsList);

            WaterCubeStrengthTest savedTest = waterCubeStrengthTestRepository.save(test);
            return new ResponseEntity<>(ResponseBuilder.getSuccessResponse(savedTest.getId()), HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(ResponseBuilder.getSuccessResponse("Error saving test result: " + e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public ResponseEntity<?> getTestResultsByUser(Long userId) {
        try {
            List<WaterCubeStrengthTest> results = waterCubeStrengthTestRepository.findByCreatedBy(userId);
            List<java.util.Map<String, Object>> mappedResults = new ArrayList<>();
            for (WaterCubeStrengthTest test : results) {
                mappedResults.add(mapTestToDto(test));
            }
            return new ResponseEntity<>(ResponseBuilder.getSuccessResponse(mappedResults), HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(ResponseBuilder.getSuccessResponse("Error fetching test results: " + e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public ResponseEntity<?> getTestResultById(Long id) {
        try {
            Optional<WaterCubeStrengthTest> result = waterCubeStrengthTestRepository.findById(id);
            if (result.isPresent()) {
                return new ResponseEntity<>(ResponseBuilder.getSuccessResponse(mapTestToDto(result.get())), HttpStatus.OK);
            } else {
                return new ResponseEntity<>(ResponseBuilder.getSuccessResponse("Test Result not found"), HttpStatus.NOT_FOUND);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(ResponseBuilder.getSuccessResponse("Error fetching test result: " + e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private java.util.Map<String, Object> mapTestToDto(WaterCubeStrengthTest test) {
        java.util.Map<String, Object> map = new java.util.HashMap<>();
        map.put("id", test.getId());
        map.put("waterCubeSampleDeclarationId", test.getWaterCubeSampleDeclaration() != null ? test.getWaterCubeSampleDeclaration().getId() : null);
        map.put("batchNumber", test.getBatchNumber());
        map.put("concreteGrade", test.getConcreteGrade());
        map.put("castingDate", test.getCastingDate());
        map.put("shift", test.getShift());
        map.put("lineNo", test.getLineNo());
        map.put("fckTarget", test.getFckTarget());
        map.put("ageDays", test.getAgeDays());
        
        map.put("s1Avg", test.getS1Avg());
        map.put("s2Avg", test.getS2Avg());
        map.put("avgX", test.getAvgX());
        map.put("minY", test.getMinY());
        map.put("s1Variation", test.getS1Variation());
        map.put("s2Variation", test.getS2Variation());
        
        map.put("condition1", test.getCondition1());
        map.put("condition2", test.getCondition2());
        map.put("condition3", test.getCondition3());
        map.put("mrSamplesRequired", test.getMrSamplesRequired());
        map.put("finalTestResult", test.getFinalTestResult());
        map.put("createdBy", test.getCreatedBy());
        map.put("createdDate", test.getCreatedDate());
        
        List<java.util.Map<String, Object>> detailsList = new ArrayList<>();
        if (test.getDetails() != null) {
            for (WaterCubeStrengthDetail d : test.getDetails()) {
                java.util.Map<String, Object> dm = new java.util.HashMap<>();
                dm.put("id", d.getId());
                dm.put("sampleNumber", d.getSampleNumber());
                dm.put("cubeIndex", d.getCubeIndex());
                dm.put("cubeId", d.getCubeId());
                dm.put("weightKg", d.getWeightKg());
                dm.put("loadKn", d.getLoadKn());
                dm.put("strengthNmm2", d.getStrengthNmm2());
                dm.put("testingDate", d.getTestingDate());
                dm.put("testingTime", d.getTestingTime() != null ? d.getTestingTime().toString() : null);
                detailsList.add(dm);
            }
        }
        map.put("details", detailsList);
        
        return map;
    }
}

