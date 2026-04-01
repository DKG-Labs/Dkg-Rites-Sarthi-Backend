package com.sarthi.service.finalmaterial.impl;

import com.sarthi.dto.finalmaterial.FinalWeightTestRequest;
import com.sarthi.dto.finalmaterial.FinalWeightTestResponse;
import com.sarthi.entity.finalmaterial.FinalWeightTest;
import com.sarthi.entity.finalmaterial.FinalWeightTestSample;
import com.sarthi.repository.finalmaterial.FinalWeightTestRepository;
import com.sarthi.repository.finalmaterial.FinalWeightTestSampleRepository;
import com.sarthi.service.finalmaterial.FinalWeightTestService;
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
 * Implementation of FinalWeightTestService
 */
@Service
@Slf4j
public class FinalWeightTestServiceImpl implements FinalWeightTestService {

    @Autowired
    private FinalWeightTestRepository weightTestRepository;

    @Autowired
    private FinalWeightTestSampleRepository sampleRepository;

    @Override
    @Transactional
    public FinalWeightTestResponse saveOrUpdateWeightTest(FinalWeightTestRequest request, String userId) {
        log.info("Saving/Updating weight test for call={}, lot={}, heat={}", 
            request.getInspectionCallNo(), request.getLotNo(), request.getHeatNo());

        // Check if inspection session already exists
        Optional<FinalWeightTest> existing = weightTestRepository.findByInspectionCallNoAndLotNoAndHeatNo(
            request.getInspectionCallNo(),
            request.getLotNo(),
            request.getHeatNo()
        );

        FinalWeightTest test;
        if (existing.isPresent()) {
            // SUBSEQUENT SAVE: Update existing parent row
            test = existing.get();
            if (request.getStatus() != null) {
                test.setStatus(request.getStatus());
            }
            if (request.getRejected() != null) {
                test.setRejected(request.getRejected());
            }
            test.setUpdatedBy(userId);
            test.setUpdatedAt(LocalDateTime.now());
            test.setRemarks(request.getRemarks());
            test.setDateOfInspection(request.getDateOfInspection());
            log.info("Updating existing weight test session, id={}", test.getId());
        } else {
            // FIRST SAVE: Create new parent row
            test = new FinalWeightTest();
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
            log.info("Creating new weight test session");
        }

        // Save parent
        test = weightTestRepository.save(test);

        // ✅ UPSERT PATTERN: Update existing samples or insert new ones
        if (request.getSamples() != null && !request.getSamples().isEmpty()) {
            final FinalWeightTest finalTest = test;

            // Fetch existing samples for this test
            List<FinalWeightTestSample> existingSamples = sampleRepository.findByFinalWeightTestId(test.getId());

            // Create a map of existing samples by (samplingNo, sampleNo) for quick lookup
            Map<String, FinalWeightTestSample> existingSampleMap = existingSamples.stream()
                .collect(Collectors.toMap(
                    s -> s.getSamplingNo() + "-" + s.getSampleNo(),
                    s -> s
                ));

            // Track which samples were updated
            Map<String, Boolean> updatedSamples = new java.util.HashMap<>();

            // Process incoming samples: update existing or create new
            List<FinalWeightTestSample> samplesToSave = request.getSamples().stream()
                .map(sampleData -> {
                    String key = sampleData.getSamplingNo() + "-" + sampleData.getSampleNo();
                    FinalWeightTestSample sample;

                    if (existingSampleMap.containsKey(key)) {
                        // UPSERT: Update existing sample
                        sample = existingSampleMap.get(key);
                        sample.setSampleValue(sampleData.getSampleValue());
                        sample.setIsRejected(sampleData.getIsRejected());
                        log.info("Updating existing sample: samplingNo={}, sampleNo={}",
                            sampleData.getSamplingNo(), sampleData.getSampleNo());
                    } else {
                        // UPSERT: Create new sample
                        sample = new FinalWeightTestSample();
                        sample.setFinalWeightTest(finalTest);
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
            List<FinalWeightTestSample> orphanedSamples = existingSamples.stream()
                .filter(s -> !updatedSamples.containsKey(s.getSamplingNo() + "-" + s.getSampleNo()))
                .collect(Collectors.toList());

            if (!orphanedSamples.isEmpty()) {
                sampleRepository.deleteAll(orphanedSamples);
                log.info("Deleted {} orphaned samples for test id={}", orphanedSamples.size(), test.getId());
            }
        }

        // Update rejected count using a separate repository call for accuracy
        long rejectedCount = sampleRepository.countRejectedByTestId(test.getId());
        test.setRejected((int) rejectedCount);
        test = weightTestRepository.save(test);

        return mapToResponse(test);
    }

    @Override
    public Optional<FinalWeightTestResponse> getWeightTest(String inspectionCallNo, String lotNo, String heatNo) {
        return weightTestRepository.findByInspectionCallNoAndLotNoAndHeatNo(inspectionCallNo, lotNo, heatNo)
            .map(this::mapToResponse);
    }

    @Override
    public List<FinalWeightTestResponse> getWeightTestsByCall(String inspectionCallNo) {
        return weightTestRepository.findByInspectionCallNo(inspectionCallNo).stream()
            .map(this::mapToResponse)
            .collect(Collectors.toList());
    }

    @Override
    public Optional<FinalWeightTestResponse> getWeightTestById(Long id) {
        return weightTestRepository.findById(id)
            .map(this::mapToResponse);
    }

    @Override
    @Transactional
    public FinalWeightTestResponse updateStatus(Long id, String status, String userId) {
        FinalWeightTest test = weightTestRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Weight test not found"));
        test.setStatus(status);
        test.setUpdatedBy(userId);
        test.setUpdatedAt(LocalDateTime.now());
        test = weightTestRepository.save(test);
        return mapToResponse(test);
    }

    @Override
    @Transactional
    public FinalWeightTestResponse updateRemarks(Long id, String remarks, String userId) {
        FinalWeightTest test = weightTestRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Weight test not found"));
        test.setRemarks(remarks);
        test.setUpdatedBy(userId);
        test.setUpdatedAt(LocalDateTime.now());
        test = weightTestRepository.save(test);
        return mapToResponse(test);
    }

    @Override
    @Transactional
    public void deleteWeightTest(Long id) {
        weightTestRepository.deleteById(id);
    }

    private FinalWeightTestResponse mapToResponse(FinalWeightTest test) {
        FinalWeightTestResponse response = new FinalWeightTestResponse();
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
                    FinalWeightTestResponse.SampleResponse sr = new FinalWeightTestResponse.SampleResponse();
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
