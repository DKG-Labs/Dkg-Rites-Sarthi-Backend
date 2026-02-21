package com.sarthi.service.finalmaterial.impl;

import com.sarthi.dto.finalmaterial.FinalMicrostructureTestRequest;
import com.sarthi.dto.finalmaterial.FinalMicrostructureTestResponse;
import com.sarthi.entity.finalmaterial.FinalMicrostructureTest;
import com.sarthi.entity.finalmaterial.FinalMicrostructureSample;
import com.sarthi.repository.finalmaterial.FinalMicrostructureTestRepository;
import com.sarthi.repository.finalmaterial.FinalMicrostructureSampleRepository;
import com.sarthi.service.finalmaterial.FinalMicrostructureTestService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class FinalMicrostructureTestServiceImpl implements FinalMicrostructureTestService {

    @Autowired
    private FinalMicrostructureTestRepository repository;

    @Autowired
    private FinalMicrostructureSampleRepository sampleRepository;

    @Override
    @Transactional
    public FinalMicrostructureTestResponse saveOrUpdateMicrostructureTest(
            FinalMicrostructureTestRequest request, String userId) {
        log.info("Saving/Updating microstructure test for call={}, lot={}, heat={}", 
            request.getInspectionCallNo(), request.getLotNo(), request.getHeatNo());

        Optional<FinalMicrostructureTest> existing = repository.findByInspectionCallNoAndLotNoAndHeatNo(
            request.getInspectionCallNo(), request.getLotNo(), request.getHeatNo());

        FinalMicrostructureTest test;
        if (existing.isPresent()) {
            test = existing.get();
            test.setLotNo(request.getLotNo());
            test.setUpdatedBy(userId);
            test.setUpdatedAt(LocalDateTime.now());
            test.setRemarks(request.getRemarks());
            log.info("Updating existing microstructure test session, id={}", test.getId());
        } else {
            test = new FinalMicrostructureTest();
            test.setInspectionCallNo(request.getInspectionCallNo());
            test.setLotNo(request.getLotNo());
            test.setHeatNo(request.getHeatNo());
            test.setSampleSize(request.getSampleSize());
            test.setQty(request.getQty());
            test.setStatus(request.getStatus() != null ? request.getStatus() : "PENDING");
            test.setRemarks(request.getRemarks());
            test.setCreatedBy(userId);
            log.info("Creating new microstructure test session");
        }

        test = repository.save(test);
        final FinalMicrostructureTest savedTest = test;

        if (request.getSamples() != null && !request.getSamples().isEmpty()) {
            List<FinalMicrostructureSample> existingSamples =
                sampleRepository.findByFinalMicrostructureTestId(savedTest.getId());

            // ✅ UPSERT PATTERN: Create map of existing samples by (samplingNo, sampleNo)
            Map<String, FinalMicrostructureSample> existingSampleMap = existingSamples.stream()
                .collect(Collectors.toMap(
                    s -> s.getSamplingNo() + "-" + s.getSampleNo(),
                    s -> s
                ));

            Map<String, Boolean> updatedSamples = new HashMap<>();
            List<FinalMicrostructureSample> samplesToSave = request.getSamples().stream()
                .map(sampleData -> {
                    Integer sampleNo = sampleData.getSampleNo();
                    Integer samplingNo = sampleData.getSamplingNo() != null ? sampleData.getSamplingNo() : 1;
                    String key = samplingNo + "-" + sampleNo;

                    FinalMicrostructureSample sample;

                    if (existingSampleMap.containsKey(key)) {
                        // UPSERT: Update existing sample
                        sample = existingSampleMap.get(key);
                        sample.setSampleType(sampleData.getSampleType());
                        log.info("Updating existing sample: samplingNo={}, sampleNo={}", samplingNo, sampleNo);
                    } else {
                        // UPSERT: Create new sample
                        sample = new FinalMicrostructureSample();
                        sample.setFinalMicrostructureTest(savedTest);
                        sample.setSampleNo(sampleNo);
                        sample.setSamplingNo(samplingNo);
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

            List<FinalMicrostructureSample> orphanedSamples = existingSamples.stream()
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
    public Optional<FinalMicrostructureTestResponse> getMicrostructureTestById(Long id) {
        return repository.findById(id).map(this::mapToResponse);
    }

    @Override
    public List<FinalMicrostructureTestResponse> getMicrostructureTestByCallNo(String inspectionCallNo) {
        return repository.findByInspectionCallNo(inspectionCallNo).stream()
            .map(this::mapToResponse)
            .collect(Collectors.toList());
    }

    @Override
    public List<FinalMicrostructureTestResponse> getMicrostructureTestByLotNo(String lotNo) {
        return repository.findByLotNo(lotNo).stream()
            .map(this::mapToResponse)
            .collect(Collectors.toList());
    }

    @Override
    public Optional<FinalMicrostructureTestResponse> getMicrostructureTestByCallLotHeat(
            String inspectionCallNo, String lotNo, String heatNo) {
        return repository.findByInspectionCallNoAndLotNoAndHeatNo(inspectionCallNo, lotNo, heatNo)
            .map(this::mapToResponse);
    }

    @Override
    @Transactional
    public FinalMicrostructureTestResponse updateStatus(Long id, String status, String userId) {
        FinalMicrostructureTest test = repository.findById(id)
            .orElseThrow(() -> new RuntimeException("Microstructure test not found"));
        test.setStatus(status);
        test.setUpdatedBy(userId);
        test.setUpdatedAt(LocalDateTime.now());
        test = repository.save(test);
        return mapToResponse(test);
    }

    @Override
    @Transactional
    public void deleteMicrostructureTest(Long id) {
        repository.deleteById(id);
    }

    private FinalMicrostructureTestResponse mapToResponse(FinalMicrostructureTest entity) {
        FinalMicrostructureTestResponse response = new FinalMicrostructureTestResponse();
        response.setId(entity.getId());
        response.setInspectionCallNo(entity.getInspectionCallNo());
        response.setLotNo(entity.getLotNo());
        response.setHeatNo(entity.getHeatNo());
        response.setSampleSize(entity.getSampleSize());
        response.setQty(entity.getQty());
        response.setRemarks(entity.getRemarks());
        response.setStatus(entity.getStatus());
        response.setCreatedBy(entity.getCreatedBy());
        response.setCreatedAt(entity.getCreatedAt());
        response.setUpdatedBy(entity.getUpdatedBy());
        response.setUpdatedAt(entity.getUpdatedAt());

        if (entity.getSamples() != null) {
            response.setSamples(entity.getSamples().stream()
                .map(sample -> {
                    FinalMicrostructureTestResponse.SampleData sampleData = 
                        new FinalMicrostructureTestResponse.SampleData();
                    sampleData.setId(sample.getId());
                    sampleData.setSampleNo(sample.getSampleNo());
                    sampleData.setSamplingNo(sample.getSamplingNo());
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

