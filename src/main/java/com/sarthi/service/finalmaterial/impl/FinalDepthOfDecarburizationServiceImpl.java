package com.sarthi.service.finalmaterial.impl;

import com.sarthi.dto.finalmaterial.FinalDepthOfDecarburizationRequest;
import com.sarthi.dto.finalmaterial.FinalDepthOfDecarburizationResponse;
import com.sarthi.entity.finalmaterial.FinalDepthOfDecarburization;
import com.sarthi.entity.finalmaterial.FinalDepthOfDecarburizationSample;
import com.sarthi.repository.finalmaterial.FinalDepthOfDecarburizationRepository;
import com.sarthi.repository.finalmaterial.FinalDepthOfDecarburizationSampleRepository;
import com.sarthi.service.finalmaterial.FinalDepthOfDecarburizationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class FinalDepthOfDecarburizationServiceImpl implements FinalDepthOfDecarburizationService {

    @Autowired
    private FinalDepthOfDecarburizationRepository repository;

    @Autowired
    private FinalDepthOfDecarburizationSampleRepository sampleRepository;

    @Override
    @Transactional
    public FinalDepthOfDecarburizationResponse saveOrUpdateDepthOfDecarburization(
            FinalDepthOfDecarburizationRequest request, String userId) {
        log.info("Saving/Updating depth of decarburization for call={}, lot={}, heat={}",
            request.getInspectionCallNo(), request.getLotNo(), request.getHeatNo());

        // ✅ UPSERT Pattern: Check if data exists for this call + lot + heat combination
        // This supports multiple lots per heat (unlike the old callNo+heatNo only)
        Optional<FinalDepthOfDecarburization> existing = repository.findByInspectionCallNoAndLotNoAndHeatNo(
            request.getInspectionCallNo(), request.getLotNo(), request.getHeatNo());

        FinalDepthOfDecarburization test;
        if (existing.isPresent()) {
            test = existing.get();
            test.setLotNo(request.getLotNo());
            test.setUpdatedBy(userId);
            test.setUpdatedAt(LocalDateTime.now());
            test.setStatus(request.getStatus() != null ? request.getStatus() : "PENDING");
            test.setRemarks(request.getRemarks());
            test.setDateOfInspection(request.getDateOfInspection());
            log.info("Updating existing depth of decarburization session, id={}", test.getId());
        } else {
            test = new FinalDepthOfDecarburization();
            test.setInspectionCallNo(request.getInspectionCallNo());
            test.setLotNo(request.getLotNo());
            test.setHeatNo(request.getHeatNo());
            test.setSampleSize(request.getSampleSize());
            test.setQty(request.getQty());
            test.setStatus(request.getStatus() != null ? request.getStatus() : "PENDING");
            test.setRejected(request.getRejected() != null ? request.getRejected() : 0);
            test.setRejected(request.getRejected() != null ? request.getRejected() : 0);
            test.setRemarks(request.getRemarks());
            test.setDateOfInspection(request.getDateOfInspection());
            test.setCreatedBy(userId);
            log.info("Creating new depth of decarburization session");
        }

        test = repository.save(test);
        final FinalDepthOfDecarburization savedTest = test;

        // ✅ UPSERT PATTERN: Update existing samples or insert new ones
        if (request.getSamples() != null && !request.getSamples().isEmpty()) {
            List<FinalDepthOfDecarburizationSample> existingSamples =
                sampleRepository.findByFinalDepthOfDecarburizationId(savedTest.getId());

            Map<String, FinalDepthOfDecarburizationSample> existingSampleMap = existingSamples.stream()
                .collect(Collectors.toMap(
                    s -> s.getSamplingNo() + "-" + s.getSampleNo(),
                    s -> s
                ));

            Map<String, Boolean> updatedSamples = new HashMap<>();
            List<FinalDepthOfDecarburizationSample> samplesToSave = request.getSamples().stream()
                .map(sampleData -> {
                    String key = sampleData.getSamplingNo() + "-" + sampleData.getSampleNo();
                    FinalDepthOfDecarburizationSample sample = existingSampleMap.getOrDefault(key,
                        new FinalDepthOfDecarburizationSample());

                    sample.setFinalDepthOfDecarburization(savedTest);
                    sample.setSamplingNo(sampleData.getSamplingNo());
                    sample.setSampleNo(sampleData.getSampleNo());
                    sample.setSampleValue(sampleData.getSampleValue() != null ? 
                        new java.math.BigDecimal(sampleData.getSampleValue()) : null);
                    sample.setCreatedBy(userId);
                    
                    updatedSamples.put(key, true);
                    return sample;
                })
                .collect(Collectors.toList());

            sampleRepository.saveAll(samplesToSave);
            log.info("Saved {} samples for test id={}", samplesToSave.size(), savedTest.getId());

            List<FinalDepthOfDecarburizationSample> orphanedSamples = existingSamples.stream()
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
    public Optional<FinalDepthOfDecarburizationResponse> getDepthOfDecarburizationById(Long id) {
        return repository.findById(id).map(this::mapToResponse);
    }

    @Override
    public List<FinalDepthOfDecarburizationResponse> getDepthOfDecarburizationByCallNo(String inspectionCallNo) {
        return repository.findByInspectionCallNo(inspectionCallNo).stream()
            .map(this::mapToResponse)
            .collect(Collectors.toList());
    }

    @Override
    public List<FinalDepthOfDecarburizationResponse> getDepthOfDecarburizationByHeatNo(String heatNo) {
        return repository.findByHeatNo(heatNo).stream()
            .map(this::mapToResponse)
            .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public FinalDepthOfDecarburizationResponse updateStatus(Long id, String status, String userId) {
        FinalDepthOfDecarburization test = repository.findById(id)
            .orElseThrow(() -> new RuntimeException("Depth of decarburization test not found"));
        test.setStatus(status);
        test.setUpdatedBy(userId);
        test.setUpdatedAt(LocalDateTime.now());
        test = repository.save(test);
        return mapToResponse(test);
    }

    @Override
    @Transactional
    public void deleteDepthOfDecarburization(Long id) {
        repository.deleteById(id);
    }

    private FinalDepthOfDecarburizationResponse mapToResponse(FinalDepthOfDecarburization entity) {
        FinalDepthOfDecarburizationResponse response = new FinalDepthOfDecarburizationResponse();
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
                    FinalDepthOfDecarburizationResponse.SampleData sampleData = 
                        new FinalDepthOfDecarburizationResponse.SampleData();
                    sampleData.setId(sample.getId());
                    sampleData.setSamplingNo(sample.getSamplingNo());
                    sampleData.setSampleNo(sample.getSampleNo());
                    sampleData.setSampleValue(sample.getSampleValue() != null ? 
                        sample.getSampleValue().toString() : null);
                    sampleData.setCreatedBy(sample.getCreatedBy());
                    sampleData.setCreatedAt(sample.getCreatedAt());
                    return sampleData;
                })
                .collect(Collectors.toList()));
        }

        return response;
    }
}

