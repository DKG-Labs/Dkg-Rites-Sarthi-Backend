package com.sarthi.service.finalmaterial.impl;

import com.sarthi.dto.finalmaterial.FinalToeLoadTestRequest;
import com.sarthi.dto.finalmaterial.FinalToeLoadTestResponse;
import com.sarthi.entity.finalmaterial.FinalToeLoadTest;
import com.sarthi.entity.finalmaterial.FinalToeLoadTestSample;
import com.sarthi.repository.finalmaterial.FinalToeLoadTestRepository;
import com.sarthi.repository.finalmaterial.FinalToeLoadTestSampleRepository;
import com.sarthi.service.finalmaterial.FinalToeLoadTestService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Implementation of FinalToeLoadTestService
 */
@Service
@Slf4j
public class FinalToeLoadTestServiceImpl implements FinalToeLoadTestService {

    @Autowired
    private FinalToeLoadTestRepository toeLoadTestRepository;

    @Autowired
    private FinalToeLoadTestSampleRepository sampleRepository;

    @Override
    @Transactional
    public FinalToeLoadTestResponse saveOrUpdateToeLoadTest(FinalToeLoadTestRequest request, String userId) {
        log.info("Saving/Updating toe load test for call={}, lot={}, heat={}", 
            request.getInspectionCallNo(), request.getLotNo(), request.getHeatNo());

        // Check if inspection session already exists
        Optional<FinalToeLoadTest> existing = toeLoadTestRepository.findByInspectionCallNoAndLotNoAndHeatNo(
            request.getInspectionCallNo(),
            request.getLotNo(),
            request.getHeatNo()
        );

        FinalToeLoadTest test;
        if (existing.isPresent()) {
            // SUBSEQUENT SAVE: Update existing parent row
            test = existing.get();
            test.setUpdatedBy(userId);
            test.setUpdatedAt(LocalDateTime.now());
            test.setRemarks(request.getRemarks());
            test.setDateOfInspection(request.getDateOfInspection());
            if (request.getStatus() != null) {
                test.setStatus(request.getStatus());
            }
            if (request.getRejected() != null) {
                test.setRejected(request.getRejected());
            }
            log.info("Updating existing toe load test session, id={}", test.getId());
        } else {
            // FIRST SAVE: Create new parent row
            test = new FinalToeLoadTest();
            test.setInspectionCallNo(request.getInspectionCallNo());
            test.setLotNo(request.getLotNo());
            test.setHeatNo(request.getHeatNo());
            test.setQtyNo(request.getQtyNo());
            test.setStatus(request.getStatus() != null ? request.getStatus() : "PENDING");
            test.setRejected(request.getRejected() != null ? request.getRejected() : 0);
            test.setRemarks(request.getRemarks());
            test.setDateOfInspection(request.getDateOfInspection());
            test.setCreatedBy(userId);
            test.setUpdatedBy(userId);
            test.setCreatedAt(LocalDateTime.now());
            test.setUpdatedAt(LocalDateTime.now());
            log.info("Creating new toe load test session");
        }

        // Save parent
        test = toeLoadTestRepository.save(test);

        // ✅ UPSERT PATTERN: Update existing samples or insert new ones
        if (request.getSamples() != null && !request.getSamples().isEmpty()) {
            final FinalToeLoadTest finalTest = test;

            // Fetch existing samples for this test
            List<FinalToeLoadTestSample> existingSamples = sampleRepository.findByFinalToeLoadTestId(test.getId());

            // Create a map of existing samples by (samplingNo, sampleNo) for quick lookup
            Map<String, FinalToeLoadTestSample> existingSampleMap = existingSamples.stream()
                .collect(Collectors.toMap(
                    s -> s.getSamplingNo() + "-" + s.getSampleNo(),
                    s -> s
                ));

            // Track which samples were updated
            Map<String, Boolean> updatedSamples = new java.util.HashMap<>();

            // Process incoming samples: update existing or create new
            List<FinalToeLoadTestSample> samplesToSave = request.getSamples().stream()
                .map(sampleData -> {
                    String key = sampleData.getSamplingNo() + "-" + sampleData.getSampleNo();
                    FinalToeLoadTestSample sample;

                    if (existingSampleMap.containsKey(key)) {
                        // UPSERT: Update existing sample
                        sample = existingSampleMap.get(key);
                        sample.setSampleValue(sampleData.getSampleValue());
                        sample.setIsRejected(sampleData.getIsRejected());
                        log.info("Updating existing sample: samplingNo={}, sampleNo={}",
                            sampleData.getSamplingNo(), sampleData.getSampleNo());
                    } else {
                        // UPSERT: Create new sample
                        sample = new FinalToeLoadTestSample();
                        sample.setFinalToeLoadTest(finalTest);
                        sample.setSamplingNo(sampleData.getSamplingNo());
                        sample.setSampleNo(sampleData.getSampleNo());
                        sample.setSampleValue(sampleData.getSampleValue());
                        sample.setIsRejected(sampleData.getIsRejected());
                        log.info("Creating new sample: samplingNo={}, sampleNo={}",
                            sampleData.getSamplingNo(), sampleData.getSampleNo());
                    }

                    updatedSamples.put(key, true);
                    return sample;
                })
                .collect(Collectors.toList());

            // Save all samples (both updated and new)
            sampleRepository.saveAll(samplesToSave);
            log.info("Saved {} samples for test id={}", samplesToSave.size(), test.getId());

            // Delete orphaned samples (samples that exist in DB but not in the request)
            List<FinalToeLoadTestSample> orphanedSamples = existingSamples.stream()
                .filter(s -> !updatedSamples.containsKey(s.getSamplingNo() + "-" + s.getSampleNo()))
                .collect(Collectors.toList());

            if (!orphanedSamples.isEmpty()) {
                sampleRepository.deleteAll(orphanedSamples);
                log.info("Deleted {} orphaned samples for test id={}", orphanedSamples.size(), test.getId());
            }
        }

        // Backend recalculation as a safety measure
        long rejectedCount = sampleRepository.countRejectedByTestId(test.getId());
        test.setRejected((int) rejectedCount);
        test = toeLoadTestRepository.save(test);

        return mapToResponse(test);
    }

    @Override
    public Optional<FinalToeLoadTestResponse> getToeLoadTest(String inspectionCallNo, String lotNo, String heatNo) {
        return toeLoadTestRepository.findByInspectionCallNoAndLotNoAndHeatNo(inspectionCallNo, lotNo, heatNo)
            .map(this::mapToResponse);
    }

    @Override
    public List<FinalToeLoadTestResponse> getToeLoadTestsByCall(String inspectionCallNo) {
        return toeLoadTestRepository.findByInspectionCallNo(inspectionCallNo).stream()
            .map(this::mapToResponse)
            .collect(Collectors.toList());
    }

    @Override
    public Optional<FinalToeLoadTestResponse> getToeLoadTestById(Long id) {
        return toeLoadTestRepository.findById(id)
            .map(this::mapToResponse);
    }

    @Override
    @Transactional
    public FinalToeLoadTestResponse updateStatus(Long id, String status, String userId) {
        FinalToeLoadTest test = toeLoadTestRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Toe load test not found"));
        test.setStatus(status);
        test.setUpdatedBy(userId);
        test.setUpdatedAt(LocalDateTime.now());
        test = toeLoadTestRepository.save(test);
        return mapToResponse(test);
    }

    @Override
    @Transactional
    public FinalToeLoadTestResponse updateRemarks(Long id, String remarks, String userId) {
        FinalToeLoadTest test = toeLoadTestRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Toe load test not found"));
        test.setRemarks(remarks);
        test.setUpdatedBy(userId);
        test.setUpdatedAt(LocalDateTime.now());
        test = toeLoadTestRepository.save(test);
        return mapToResponse(test);
    }

    @Override
    @Transactional
    public void deleteToeLoadTest(Long id) {
        toeLoadTestRepository.deleteById(id);
    }

    private FinalToeLoadTestResponse mapToResponse(FinalToeLoadTest test) {
        FinalToeLoadTestResponse response = new FinalToeLoadTestResponse();
        response.setId(test.getId());
        response.setInspectionCallNo(test.getInspectionCallNo());
        response.setLotNo(test.getLotNo());
        response.setHeatNo(test.getHeatNo());
        response.setQtyNo(test.getQtyNo());
        response.setStatus(test.getStatus());
        response.setRejected(test.getRejected());
        response.setRemarks(test.getRemarks());
        response.setDateOfInspection(test.getDateOfInspection());
        response.setCreatedBy(test.getCreatedBy());
        response.setCreatedAt(test.getCreatedAt());
        response.setUpdatedBy(test.getUpdatedBy());
        response.setUpdatedAt(test.getUpdatedAt());

        // Map samples
        if (test.getSamples() != null) {
            response.setSamples(test.getSamples().stream()
                .map(sample -> {
                    FinalToeLoadTestResponse.SampleResponse sr = new FinalToeLoadTestResponse.SampleResponse();
                    sr.setId(sample.getId());
                    sr.setSamplingNo(sample.getSamplingNo());
                    sr.setSampleNo(sample.getSampleNo());
                    sr.setSampleValue(sample.getSampleValue());
                    sr.setIsRejected(sample.getIsRejected());
                    sr.setCreatedAt(sample.getCreatedAt());
                    return sr;
                })
                .collect(Collectors.toList()));
        }

        return response;
    }
}
