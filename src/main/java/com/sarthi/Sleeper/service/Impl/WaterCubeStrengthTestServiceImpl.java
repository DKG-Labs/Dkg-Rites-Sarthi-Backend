package com.sarthi.Sleeper.service.Impl;

import com.sarthi.Sleeper.dto.FinalInspectionDtos.WaterCubeStrengthDetailRequestDto;
import com.sarthi.Sleeper.dto.FinalInspectionDtos.WaterCubeStrengthTestRequestDto;
import com.sarthi.Sleeper.entity.FinalInspection.WaterCubeSampleDeclaration;
import com.sarthi.Sleeper.entity.FinalInspection.WaterCubeStrengthDetail;
import com.sarthi.Sleeper.entity.FinalInspection.WaterCubeStrengthTest;
import com.sarthi.Sleeper.repository.FinalInspectionRepository.WaterCubeSampleRepository;
import com.sarthi.Sleeper.repository.FinalInspectionRepository.WaterCubeStrengthTestRepository;
import com.sarthi.Sleeper.service.WaterCubeLotService;
import com.sarthi.Sleeper.service.WaterCubeStrengthTestService;
import com.sarthi.constant.AppConstant;
import com.sarthi.exception.BusinessException;
import com.sarthi.exception.ErrorDetails;
import com.sarthi.util.ResponseBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class WaterCubeStrengthTestServiceImpl implements WaterCubeStrengthTestService {

    @Autowired
    private WaterCubeStrengthTestRepository waterCubeStrengthTestRepository;

    @Autowired
    private WaterCubeSampleRepository waterCubeSampleRepository;

    @Autowired
    private WaterCubeLotService waterCubeLotService;

    private String normalizeToIsoDate(String dateStr) {
        if (dateStr == null || dateStr.trim().isEmpty() || "N/A".equalsIgnoreCase(dateStr.trim())) {
            return null;
        }
        String str = dateStr.trim();
        if (str.contains("/")) {
            String[] parts = str.split("/");
            if (parts.length == 3) {
                try {
                    return String.format("%04d-%02d-%02d", Integer.parseInt(parts[2]), Integer.parseInt(parts[1]), Integer.parseInt(parts[0]));
                } catch (Exception ignored) {}
            }
        }
        if (str.contains("-")) {
            return str.split("T")[0];
        }
        return str;
    }

    /** The sample declaration the test belongs to; it links the test to its production batch (the lot). */
    private WaterCubeSampleDeclaration requireSample(WaterCubeStrengthTestRequestDto requestDto) {
        Long sampleId = requestDto.getWaterCubeSampleDeclarationId();
        if (sampleId == null) {
            throw validation("Water cube sample declaration is required to save a test result.");
        }
        return waterCubeSampleRepository.findById(sampleId)
                .orElseThrow(() -> validation("Water cube sample declaration " + sampleId + " not found."));
    }

    private static Long batchIdOf(WaterCubeSampleDeclaration sample, WaterCubeStrengthTestRequestDto requestDto) {
        return sample != null && sample.getProductionDeclarationId() != null
                ? sample.getProductionDeclarationId()
                : requestDto.getProductionDeclarationId();
    }

    /** Stores the server-side evaluation, not the client-sent figures. */
    private static void applyEvaluation(WaterCubeStrengthTest test, WaterCubeLotService.Evaluation eval) {
        test.setFckTarget(eval.fck());
        test.setS1Avg(eval.s1Avg());
        test.setS2Avg(eval.s2Avg());
        test.setAvgX(eval.avgX());
        test.setMinY(eval.minY());
        test.setS1Variation(eval.s1Variation());
        test.setS2Variation(eval.s2Variation());
        test.setCondition1(eval.condition1());
        test.setCondition2(eval.condition2());
        test.setCondition3(eval.condition3());
        test.setMrSamplesRequired(eval.mrSamplesRequired());
        test.setFinalTestResult(eval.finalTestResult());
    }

    /** On FAIL the whole lot is rejected; the response tells the IE which inspection calls still carry the batch. */
    private Map<String, Object> finishSave(WaterCubeStrengthTest saved, WaterCubeLotService.Evaluation eval,
                                           Long fallbackBatchId, Long userId) {
        Map<String, Object> result = new java.util.HashMap<>();
        result.put("id", saved.getId());
        result.put("finalTestResult", saved.getFinalTestResult());
        result.put("lotRejected", false);
        result.put("openCallNos", java.util.Collections.emptyList());
        if (eval.failed()) {
            WaterCubeLotService.Rejection rejection = waterCubeLotService.rejectLot(saved, fallbackBatchId, userId);
            result.put("lotRejected", true);
            result.put("productionDeclarationId", rejection.productionDeclarationId());
            result.put("batchNumber", rejection.batchNumber());
            result.put("openCallNos", rejection.affectedCallNos());
        }
        return result;
    }

    private static BusinessException validation(String message) {
        return new BusinessException(new ErrorDetails(
                AppConstant.ERROR_CODE_RESOURCE,
                AppConstant.ERROR_TYPE_CODE_RESOURCE,
                AppConstant.ERROR_TYPE_VALIDATION,
                message));
    }

    // Errors propagate (handled by ExceptionHelper) so the test and the lot rejection commit or roll back together.
    @Override
    @Transactional
    public ResponseEntity<?> saveTestResult(WaterCubeStrengthTestRequestDto requestDto) {
            WaterCubeSampleDeclaration sample = requireSample(requestDto);
            Long batchId = batchIdOf(sample, requestDto);
            waterCubeLotService.assertNotRejected(batchId);
            WaterCubeLotService.Evaluation eval = waterCubeLotService.evaluate(requestDto);

            WaterCubeStrengthTest test = new WaterCubeStrengthTest();
            test.setWaterCubeSampleDeclaration(sample);

            test.setBatchNumber(requestDto.getBatchNumber());
            test.setConcreteGrade(requestDto.getConcreteGrade());
            if (requestDto.getCastingDate() != null && !requestDto.getCastingDate().isEmpty()) {
                test.setCastingDate(normalizeToIsoDate(requestDto.getCastingDate()));
            }
            test.setShift(requestDto.getShift());
            test.setLineNo(requestDto.getLineNo());
            test.setAgeDays(requestDto.getAgeDays());

            test.setVendorCode(requestDto.getVendorCode());
            test.setPlantId(requestDto.getPlantId());

            applyEvaluation(test, eval);

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
                        detail.setTestingDate(normalizeToIsoDate(detailDto.getTestingDate()));
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
            Map<String, Object> result = finishSave(savedTest, eval, batchId, requestDto.getCreatedBy());
            return new ResponseEntity<>(ResponseBuilder.getSuccessResponse(result), HttpStatus.OK);
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

    @Override
    @Transactional
    public ResponseEntity<?> updateTestResult(Long id, WaterCubeStrengthTestRequestDto requestDto) {
            Optional<WaterCubeStrengthTest> optional = waterCubeStrengthTestRepository.findById(id);

            if (optional.isEmpty()) {
                return new ResponseEntity<>("Record not found", HttpStatus.NOT_FOUND);
            }

            WaterCubeStrengthTest test = optional.get();
            WaterCubeLotService.Evaluation eval = waterCubeLotService.evaluate(requestDto);

            // A test that rejected its lot is locked: it can be corrected, but its result can no longer pass
            if (waterCubeLotService.isRejectingTest(id) && !eval.failed()) {
                throw validation("This water cube test rejected its batch, which has been returned to the vendor. "
                        + "Its result can no longer be changed to " + eval.finalTestResult() + ".");
            }
            Long batchId = batchIdOf(test.getWaterCubeSampleDeclaration(), requestDto);

            // Update fields
            test.setBatchNumber(requestDto.getBatchNumber());
            test.setConcreteGrade(requestDto.getConcreteGrade());
            test.setCastingDate(normalizeToIsoDate(requestDto.getCastingDate()));
            test.setShift(requestDto.getShift());
            test.setLineNo(requestDto.getLineNo());
            test.setAgeDays(requestDto.getAgeDays());

            test.setVendorCode(requestDto.getVendorCode());
            test.setPlantId(requestDto.getPlantId());

            applyEvaluation(test, eval);

          //  test.setUpdatedBy(requestDto.getUpdatedBy());
            test.setUpdatedDate(LocalDateTime.now());


            test.getDetails().clear();

            // Add new details
            List<WaterCubeStrengthDetail> detailsList = new ArrayList<>();
            if (requestDto.getDetails() != null) {
                for (WaterCubeStrengthDetailRequestDto dto : requestDto.getDetails()) {
                    WaterCubeStrengthDetail detail = new WaterCubeStrengthDetail();

                    detail.setSampleNumber(dto.getSampleNumber());
                    detail.setCubeIndex(dto.getCubeIndex());
                    detail.setCubeId(dto.getCubeId());
                    detail.setWeightKg(dto.getWeightKg());
                    detail.setLoadKn(dto.getLoadKn());
                    detail.setStrengthNmm2(dto.getStrengthNmm2());
                    detail.setTestingDate(normalizeToIsoDate(dto.getTestingDate()));
                    detail.setTestingTime(dto.getTestingTime());

                    detail.setStrengthTest(test);
                    detailsList.add(detail);
                }
            }

            test.setDetails(detailsList);

            WaterCubeStrengthTest savedTest = waterCubeStrengthTestRepository.save(test);
            Map<String, Object> result = finishSave(savedTest, eval, batchId, requestDto.getCreatedBy());
            result.put("message", "Updated successfully");

            return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<?> deleteTestResult(Long id) {
        // A test that rejected its lot is the record of that rejection and cannot be deleted
        if (waterCubeLotService.isRejectingTest(id)) {
            throw validation("This water cube test rejected its batch, which has been returned to the vendor. It cannot be deleted.");
        }
        try {
            if (!waterCubeStrengthTestRepository.existsById(id)) {
                return new ResponseEntity<>("Record not found", HttpStatus.NOT_FOUND);
            }

            waterCubeStrengthTestRepository.deleteById(id);

            return new ResponseEntity<>("Deleted successfully", HttpStatus.OK);

        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>("Error deleting: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public List<Map<String, Object>> getAll() {
      //  return waterCubeStrengthTestRepository.findAll();

        return waterCubeStrengthTestRepository.findAll()
                .stream()
                .map(this::mapTestToDto)
                .toList();
    }

    @Override
    public List<Map<String, Object>> getAllLite(String plantId, Integer days) {
        return getAllLite(plantId, days, null, null);
    }

    // An explicit casting-date range takes precedence over the last-N-days window (which is by test entry date)
    @Override
    public List<Map<String, Object>> getAllLite(String plantId, Integer days,
            java.time.LocalDate fromDate, java.time.LocalDate toDate) {

        String normPlant = null;
        if (plantId != null && !plantId.trim().isEmpty()) {
            normPlant = plantId.replace(":", "").replaceAll("\\s+", "").toLowerCase();
        }
        if (fromDate != null || toDate != null) {
            return waterCubeStrengthTestRepository.findForListByCasting(
                            normPlant,
                            fromDate,
                            toDate,
                            fromDate != null ? fromDate.atStartOfDay() : null,
                            toDate != null ? toDate.plusDays(1).atStartOfDay() : null)
                    .stream()
                    .map(this::mapTestToLiteDto)
                    .toList();
        }
        java.time.LocalDateTime fromDateTime = (days != null && days > 0)
                ? java.time.LocalDateTime.now().minusDays(days)
                : null;

        return waterCubeStrengthTestRepository.findForList(normPlant, fromDateTime)
                .stream()
                .map(this::mapTestToLiteDto)
                .toList();
    }

    @Override
    public com.sarthi.Sleeper.dto.PagedResponse<Map<String, Object>> getPage(
            String plantId, Integer days, Integer page, Integer size) {

        String normPlant = null;
        if (plantId != null && !plantId.trim().isEmpty()) {
            normPlant = plantId.replace(":", "").replaceAll("\\s+", "").toLowerCase();
        }
        java.time.LocalDateTime fromDate = (days != null && days > 0)
                ? java.time.LocalDateTime.now().minusDays(days)
                : null;
        int p = com.sarthi.Sleeper.dto.PagedResponse.clampPage(page);
        int sz = com.sarthi.Sleeper.dto.PagedResponse.clampSize(size);

        org.springframework.data.domain.Page<Long> idPage = waterCubeStrengthTestRepository
                .findIdsForPage(normPlant, fromDate, org.springframework.data.domain.PageRequest.of(p, sz));

        List<Long> ids = idPage.getContent();
        Map<Long, WaterCubeStrengthTest> byId = new java.util.HashMap<>();
        if (!ids.isEmpty()) {
            waterCubeStrengthTestRepository.findByIdIn(ids).forEach(t -> byId.put(t.getId(), t));
        }

        // keep the newest-first order of the id page
        List<Map<String, Object>> content = ids.stream()
                .map(byId::get)
                .filter(java.util.Objects::nonNull)
                .map(this::mapTestToLiteDto)
                .toList();

        return new com.sarthi.Sleeper.dto.PagedResponse<>(content, p, sz, idPage.getTotalElements());
    }

    // Same shape as mapTestToDto; kept as a separate method so the list endpoints can be slimmed later.
    private java.util.Map<String, Object> mapTestToLiteDto(WaterCubeStrengthTest test) {
        java.util.Map<String, Object> map = new java.util.HashMap<>();
        map.put("id", test.getId());
        map.put("waterCubeSampleDeclarationId", test.getWaterCubeSampleDeclaration() != null ? test.getWaterCubeSampleDeclaration().getId() : null);
        map.put("productionDeclarationId", test.getWaterCubeSampleDeclaration() != null ? test.getWaterCubeSampleDeclaration().getProductionDeclarationId() : null);
        map.put("batchNumber", test.getBatchNumber());
        map.put("concreteGrade", test.getConcreteGrade());
        map.put("castingDate", test.getCastingDate());
        map.put("shift", test.getShift());
        map.put("lineNo", test.getLineNo());
        map.put("fckTarget", test.getFckTarget());
        map.put("ageDays", test.getAgeDays());
        map.put("vendorCode", test.getVendorCode());
        map.put("plantId", test.getPlantId());
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
        String testDate = null;
        if (test.getDetails() != null) {
            for (WaterCubeStrengthDetail d : test.getDetails()) {
                java.util.Map<String, Object> dm = new java.util.HashMap<>();
                // id is needed by the edit flow to update the existing cube rows
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
                if (testDate == null && d.getTestingDate() != null && !d.getTestingDate().isEmpty()) {
                    testDate = d.getTestingDate();
                }
            }
        }
        map.put("testDate", testDate);
        map.put("details", detailsList);
        return map;
    }

    private java.util.Map<String, Object> mapTestToDto(WaterCubeStrengthTest test) {
        java.util.Map<String, Object> map = new java.util.HashMap<>();
        map.put("id", test.getId());
        map.put("waterCubeSampleDeclarationId", test.getWaterCubeSampleDeclaration() != null ? test.getWaterCubeSampleDeclaration().getId() : null);
        map.put("productionDeclarationId", test.getWaterCubeSampleDeclaration() != null ? test.getWaterCubeSampleDeclaration().getProductionDeclarationId() : null);
        map.put("batchNumber", test.getBatchNumber());
        map.put("concreteGrade", test.getConcreteGrade());
        map.put("castingDate", test.getCastingDate());
        map.put("shift", test.getShift());
        map.put("lineNo", test.getLineNo());
        map.put("fckTarget", test.getFckTarget());
        map.put("ageDays", test.getAgeDays());

        map.put("vendorCode", test.getVendorCode());
        map.put("plantId", test.getPlantId());
        
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
        String testDate = null;
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
                if (testDate == null && d.getTestingDate() != null && !d.getTestingDate().isEmpty()) {
                    testDate = d.getTestingDate();
                }
            }
        }
        map.put("testDate", testDate);
        map.put("details", detailsList);
        
        return map;
    }
}

