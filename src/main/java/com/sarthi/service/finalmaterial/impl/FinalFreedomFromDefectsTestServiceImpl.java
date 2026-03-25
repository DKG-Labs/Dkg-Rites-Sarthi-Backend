package com.sarthi.service.finalmaterial.impl;

import com.sarthi.dto.finalmaterial.FinalFreedomFromDefectsTestRequest;
import com.sarthi.dto.finalmaterial.FinalFreedomFromDefectsTestResponse;
import com.sarthi.entity.finalmaterial.FinalFreedomFromDefectsTest;
import com.sarthi.entity.finalmaterial.FinalFreedomFromDefectsSample;
import com.sarthi.repository.finalmaterial.FinalFreedomFromDefectsTestRepository;
import com.sarthi.repository.finalmaterial.FinalFreedomFromDefectsSampleRepository;
import com.sarthi.service.finalmaterial.FinalFreedomFromDefectsTestService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class FinalFreedomFromDefectsTestServiceImpl implements FinalFreedomFromDefectsTestService {

    @Autowired
    private FinalFreedomFromDefectsTestRepository repository;

    @Autowired
    private FinalFreedomFromDefectsSampleRepository sampleRepository;

    @Override
    @Transactional
    public FinalFreedomFromDefectsTestResponse saveOrUpdateFreedomFromDefectsTest(
            FinalFreedomFromDefectsTestRequest request, String userId) {
        log.info("Saving/Updating freedom from defects test for call={}, lot={}, heat={}", 
            request.getInspectionCallNo(), request.getLotNo(), request.getHeatNo());

        Optional<FinalFreedomFromDefectsTest> existing = repository.findByInspectionCallNoAndLotNoAndHeatNo(
            request.getInspectionCallNo(), request.getLotNo(), request.getHeatNo());

        FinalFreedomFromDefectsTest test;
        if (existing.isPresent()) {
            test = existing.get();
            test.setLotNo(request.getLotNo());
            test.setUpdatedBy(userId);
            test.setUpdatedAt(LocalDateTime.now());
            test.setRemarks(request.getRemarks());
            test.setDateOfInspection(request.getDateOfInspection());
            test.setStatus(request.getStatus() != null ? request.getStatus() : "PENDING");
            test.setRejected(request.getRejected() != null ? request.getRejected() : 0);
            log.info("Updating existing freedom from defects test session, id={}", test.getId());
        } else {
            test = new FinalFreedomFromDefectsTest();
            test.setInspectionCallNo(request.getInspectionCallNo());
            test.setLotNo(request.getLotNo());
            test.setHeatNo(request.getHeatNo());
            test.setSampleSize(request.getSampleSize());
            test.setQty(request.getQty());
            test.setStatus(request.getStatus() != null ? request.getStatus() : "PENDING");
            test.setRejected(request.getRejected() != null ? request.getRejected() : 0);
            test.setRemarks(request.getRemarks());
            test.setDateOfInspection(request.getDateOfInspection());
            test.setCreatedBy(userId);
            log.info("Creating new freedom from defects test session");
        }

        test = repository.save(test);
        final FinalFreedomFromDefectsTest savedTest = test;

        if (request.getSamples() != null && !request.getSamples().isEmpty()) {
            List<FinalFreedomFromDefectsSample> existingSamples =
                sampleRepository.findByFinalFreedomFromDefectsTestId(savedTest.getId());

            // ✅ UPSERT PATTERN: Create map of existing samples by samplingNo-sampleNo for quick lookup
            Map<String, FinalFreedomFromDefectsSample> existingSampleMap = existingSamples.stream()
                .collect(Collectors.toMap(
                    s -> s.getSamplingNo() + "-" + s.getSampleNo(),
                    s -> s
                ));

            Map<String, Boolean> updatedSamples = new HashMap<>();
            List<FinalFreedomFromDefectsSample> samplesToSave = request.getSamples().stream()
                .map(sampleData -> {
                    Integer samplingNo = sampleData.getSamplingNo() != null ? sampleData.getSamplingNo() : 1;
                    Integer sampleNo = sampleData.getSampleNo();
                    String key = samplingNo + "-" + sampleNo;
                    FinalFreedomFromDefectsSample sample;

                    if (existingSampleMap.containsKey(key)) {
                        // UPSERT: Update existing sample
                        sample = existingSampleMap.get(key);
                        sample.setSampleType(sampleData.getSampleType());
                        log.info("Updating existing sample: samplingNo={}, sampleNo={}", samplingNo, sampleNo);
                    } else {
                        // UPSERT: Create new sample
                        sample = new FinalFreedomFromDefectsSample();
                        sample.setFinalFreedomFromDefectsTest(savedTest);
                        sample.setSamplingNo(samplingNo);
                        sample.setSampleNo(sampleNo);
                        sample.setSampleType(sampleData.getSampleType());
                        sample.setCreatedBy(userId);
                        log.info("Creating new sample: samplingNo={}, sampleNo={}", samplingNo, sampleNo);
                    }

                    updatedSamples.put(key, true);
                    return sample;
                })
                .collect(Collectors.toList());

            sampleRepository.saveAll(samplesToSave);
            log.info("Saved {} samples for test id={}", samplesToSave.size(), savedTest.getId());

            List<FinalFreedomFromDefectsSample> orphanedSamples = existingSamples.stream()
                .filter(s -> !updatedSamples.containsKey(s.getSamplingNo() + "-" + s.getSampleNo()))
                .collect(Collectors.toList());

            if (!orphanedSamples.isEmpty()) {
                sampleRepository.deleteAll(orphanedSamples);
                log.info("Deleted {} orphaned samples for test id={}", orphanedSamples.size(), savedTest.getId());
            }
        }

        return mapToResponse(savedTest);
    }

    @Override
    public Optional<FinalFreedomFromDefectsTestResponse> getFreedomFromDefectsTestById(Long id) {
        return repository.findById(id).map(this::mapToResponse);
    }

    @Override
    public List<FinalFreedomFromDefectsTestResponse> getFreedomFromDefectsTestByCallNo(String inspectionCallNo) {
        return repository.findByInspectionCallNo(inspectionCallNo).stream()
            .map(this::mapToResponse)
            .collect(Collectors.toList());
    }

    @Override
    public List<FinalFreedomFromDefectsTestResponse> getFreedomFromDefectsTestByLotNo(String lotNo) {
        return repository.findByLotNo(lotNo).stream()
            .map(this::mapToResponse)
            .collect(Collectors.toList());
    }

    @Override
    public Optional<FinalFreedomFromDefectsTestResponse> getFreedomFromDefectsTestByCallLotHeat(
            String inspectionCallNo, String lotNo, String heatNo) {
        return repository.findByInspectionCallNoAndLotNoAndHeatNo(inspectionCallNo, lotNo, heatNo)
            .map(this::mapToResponse);
    }

    @Override
    @Transactional
    public FinalFreedomFromDefectsTestResponse updateStatus(Long id, String status, String userId) {
        FinalFreedomFromDefectsTest test = repository.findById(id)
            .orElseThrow(() -> new RuntimeException("Freedom from defects test not found"));
        test.setStatus(status);
        test.setUpdatedBy(userId);
        test.setUpdatedAt(LocalDateTime.now());
        test = repository.save(test);
        return mapToResponse(test);
    }

    @Override
    @Transactional
    public void deleteFreedomFromDefectsTest(Long id) {
        repository.deleteById(id);
    }

    private FinalFreedomFromDefectsTestResponse mapToResponse(FinalFreedomFromDefectsTest entity) {
        FinalFreedomFromDefectsTestResponse response = new FinalFreedomFromDefectsTestResponse();
        response.setId(entity.getId());
        response.setInspectionCallNo(entity.getInspectionCallNo());
        response.setLotNo(entity.getLotNo());
        response.setHeatNo(entity.getHeatNo());
        response.setSampleSize(entity.getSampleSize());
        response.setQty(entity.getQty());
        response.setRemarks(entity.getRemarks());
        response.setDateOfInspection(entity.getDateOfInspection());
        response.setStatus(entity.getStatus());
        response.setRejected(entity.getRejected());
        response.setCreatedBy(entity.getCreatedBy());
        response.setCreatedAt(entity.getCreatedAt());
        response.setUpdatedBy(entity.getUpdatedBy());
        response.setUpdatedAt(entity.getUpdatedAt());

        if (entity.getSamples() != null) {
            response.setSamples(entity.getSamples().stream()
                .map(sample -> {
                    FinalFreedomFromDefectsTestResponse.SampleData sampleData =
                        new FinalFreedomFromDefectsTestResponse.SampleData();
                    sampleData.setId(sample.getId());
                    sampleData.setSamplingNo(sample.getSamplingNo());
                    sampleData.setSampleNo(sample.getSampleNo());
                    sampleData.setSampleType(sample.getSampleType());
                    sampleData.setCreatedBy(sample.getCreatedBy());
                    sampleData.setCreatedAt(sample.getCreatedAt());
                    return sampleData;
                })
                .collect(Collectors.toList()));
        }

        return response;
    }
}

