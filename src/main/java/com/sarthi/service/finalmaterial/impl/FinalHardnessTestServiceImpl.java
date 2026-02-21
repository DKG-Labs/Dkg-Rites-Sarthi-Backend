package com.sarthi.service.finalmaterial.impl;

import com.sarthi.dto.finalmaterial.FinalHardnessTestRequest;
import com.sarthi.dto.finalmaterial.FinalHardnessTestResponse;
import com.sarthi.entity.finalmaterial.FinalHardnessTest;
import com.sarthi.entity.finalmaterial.FinalHardnessTestSample;
import com.sarthi.repository.finalmaterial.FinalHardnessTestRepository;
import com.sarthi.repository.finalmaterial.FinalHardnessTestSampleRepository;
import com.sarthi.service.finalmaterial.FinalHardnessTestService;
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
 * Implementation of FinalHardnessTestService
 */
@Service
@Slf4j
public class FinalHardnessTestServiceImpl implements FinalHardnessTestService {

    @Autowired
    private FinalHardnessTestRepository hardnessTestRepository;

    @Autowired
    private FinalHardnessTestSampleRepository sampleRepository;

    @Override
    @Transactional
    public FinalHardnessTestResponse saveOrUpdateHardnessTest(FinalHardnessTestRequest request, String userId) {
        log.info("Saving/Updating hardness test for call={}, lot={}, heat={}", 
            request.getInspectionCallNo(), request.getLotNo(), request.getHeatNo());

        // Check if inspection session already exists
        Optional<FinalHardnessTest> existing = hardnessTestRepository.findByInspectionCallNoAndLotNoAndHeatNo(
            request.getInspectionCallNo(),
            request.getLotNo(),
            request.getHeatNo()
        );

        FinalHardnessTest test;
        if (existing.isPresent()) {
            // SUBSEQUENT SAVE: Update existing parent row
            test = existing.get();
            test.setUpdatedBy(userId);
            test.setUpdatedAt(LocalDateTime.now());
            test.setRemarks(request.getRemarks());
            log.info("Updating existing hardness test session, id={}", test.getId());
        } else {
            // FIRST SAVE: Create new parent row
            test = new FinalHardnessTest();
            test.setInspectionCallNo(request.getInspectionCallNo());
            test.setLotNo(request.getLotNo());
            test.setHeatNo(request.getHeatNo());
            test.setQtyNo(request.getQtyNo());
            test.setStatus("PENDING");
            test.setRemarks(request.getRemarks());
            test.setCreatedBy(userId);
            test.setUpdatedBy(userId);
            test.setCreatedAt(LocalDateTime.now());
            test.setUpdatedAt(LocalDateTime.now());
            log.info("Creating new hardness test session");
        }

        // Save parent
        test = hardnessTestRepository.save(test);

        // ✅ UPSERT PATTERN: Update existing samples or insert new ones
        if (request.getSamples() != null && !request.getSamples().isEmpty()) {
            final FinalHardnessTest finalTest = test;

            // Fetch existing samples for this test
            List<FinalHardnessTestSample> existingSamples = sampleRepository.findByFinalHardnessTestId(test.getId());

            // Create a map of existing samples by (samplingNo, sampleNo) for quick lookup
            Map<String, FinalHardnessTestSample> existingSampleMap = existingSamples.stream()
                .collect(Collectors.toMap(
                    s -> s.getSamplingNo() + "-" + s.getSampleNo(),
                    s -> s
                ));

            // Track which samples were updated
            Map<String, Boolean> updatedSamples = new java.util.HashMap<>();

            // Process incoming samples: update existing or create new
            List<FinalHardnessTestSample> samplesToSave = request.getSamples().stream()
                .map(sampleData -> {
                    String key = sampleData.getSamplingNo() + "-" + sampleData.getSampleNo();
                    FinalHardnessTestSample sample;

                    if (existingSampleMap.containsKey(key)) {
                        // UPSERT: Update existing sample
                        sample = existingSampleMap.get(key);
                        sample.setSampleValue(sampleData.getSampleValue());
                        sample.setIsRejected(sampleData.getIsRejected());
                        log.info("Updating existing sample: samplingNo={}, sampleNo={}",
                            sampleData.getSamplingNo(), sampleData.getSampleNo());
                    } else {
                        // UPSERT: Create new sample
                        sample = new FinalHardnessTestSample();
                        sample.setFinalHardnessTest(finalTest);
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
            List<FinalHardnessTestSample> orphanedSamples = existingSamples.stream()
                .filter(s -> !updatedSamples.containsKey(s.getSamplingNo() + "-" + s.getSampleNo()))
                .collect(Collectors.toList());

            if (!orphanedSamples.isEmpty()) {
                sampleRepository.deleteAll(orphanedSamples);
                log.info("Deleted {} orphaned samples for test id={}", orphanedSamples.size(), test.getId());
            }
        }

        // Update rejected count
        long rejectedCount = sampleRepository.countRejectedByTestId(test.getId());
        test.setRejected((int) rejectedCount);
        test = hardnessTestRepository.save(test);

        return mapToResponse(test);
    }

    @Override
    public Optional<FinalHardnessTestResponse> getHardnessTest(String inspectionCallNo, String lotNo, String heatNo) {
        return hardnessTestRepository.findByInspectionCallNoAndLotNoAndHeatNo(inspectionCallNo, lotNo, heatNo)
            .map(this::mapToResponse);
    }

    @Override
    public List<FinalHardnessTestResponse> getHardnessTestsByCall(String inspectionCallNo) {
        return hardnessTestRepository.findByInspectionCallNo(inspectionCallNo).stream()
            .map(this::mapToResponse)
            .collect(Collectors.toList());
    }

    @Override
    public Optional<FinalHardnessTestResponse> getHardnessTestById(Long id) {
        return hardnessTestRepository.findById(id)
            .map(this::mapToResponse);
    }

    @Override
    @Transactional
    public FinalHardnessTestResponse updateStatus(Long id, String status, String userId) {
        FinalHardnessTest test = hardnessTestRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Hardness test not found"));
        test.setStatus(status);
        test.setUpdatedBy(userId);
        test.setUpdatedAt(LocalDateTime.now());
        test = hardnessTestRepository.save(test);
        return mapToResponse(test);
    }

    @Override
    @Transactional
    public FinalHardnessTestResponse updateRemarks(Long id, String remarks, String userId) {
        FinalHardnessTest test = hardnessTestRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Hardness test not found"));
        test.setRemarks(remarks);
        test.setUpdatedBy(userId);
        test.setUpdatedAt(LocalDateTime.now());
        test = hardnessTestRepository.save(test);
        return mapToResponse(test);
    }

    @Override
    @Transactional
    public void deleteHardnessTest(Long id) {
        hardnessTestRepository.deleteById(id);
    }

    private FinalHardnessTestResponse mapToResponse(FinalHardnessTest test) {
        FinalHardnessTestResponse response = new FinalHardnessTestResponse();
        response.setId(test.getId());
        response.setInspectionCallNo(test.getInspectionCallNo());
        response.setLotNo(test.getLotNo());
        response.setHeatNo(test.getHeatNo());
        response.setQtyNo(test.getQtyNo());
        response.setStatus(test.getStatus());
        response.setRejected(test.getRejected());
        response.setRemarks(test.getRemarks());
        response.setCreatedBy(test.getCreatedBy());
        response.setCreatedAt(test.getCreatedAt());
        response.setUpdatedBy(test.getUpdatedBy());
        response.setUpdatedAt(test.getUpdatedAt());

        // Map samples
        if (test.getSamples() != null) {
            response.setSamples(test.getSamples().stream()
                .map(sample -> {
                    FinalHardnessTestResponse.SampleResponse sr = new FinalHardnessTestResponse.SampleResponse();
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

