package com.sarthi.service.finalmaterial.impl;

import com.sarthi.dto.finalmaterial.FinalDimensionalInspectionRequest;
import com.sarthi.dto.finalmaterial.FinalDimensionalInspectionResponse;
import com.sarthi.entity.finalmaterial.FinalDimensionalInspection;
import com.sarthi.entity.finalmaterial.FinalDimensionalInspectionSample;
import com.sarthi.repository.finalmaterial.FinalDimensionalInspectionRepository;
import com.sarthi.repository.finalmaterial.FinalDimensionalInspectionSampleRepository;
import com.sarthi.service.finalmaterial.FinalDimensionalInspectionNewService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class FinalDimensionalInspectionNewServiceImpl implements FinalDimensionalInspectionNewService {

    private final FinalDimensionalInspectionRepository dimensionalInspectionRepository;
    private final FinalDimensionalInspectionSampleRepository sampleRepository;

    @Override
    @Transactional
    public FinalDimensionalInspectionResponse saveOrUpdateDimensionalInspection(
            FinalDimensionalInspectionRequest request, String userId) {
        log.info("Saving/Updating dimensional inspection for call={}, lot={}, heat={}", 
            request.getInspectionCallNo(), request.getLotNo(), request.getHeatNo());

        // Check if inspection session already exists
        Optional<FinalDimensionalInspection> existing = dimensionalInspectionRepository
                .findByInspectionCallNoAndLotNoAndHeatNo(
                    request.getInspectionCallNo(),
                    request.getLotNo(),
                    request.getHeatNo()
                );

        FinalDimensionalInspection inspection;
        if (existing.isPresent()) {
            // UPDATE: Existing inspection session
            inspection = existing.get();
            inspection.setRemarks(request.getRemarks());
            inspection.setStatus(request.getStatus() != null ? request.getStatus() : "OK");
            inspection.setRejected(request.getRejected() != null ? request.getRejected() : 0);
            inspection.setRemarks(request.getRemarks());
            inspection.setDateOfInspection(request.getDateOfInspection());
            inspection.setUpdatedBy(userId);
            log.info("Updating existing dimensional inspection record");
        } else {
            // CREATE: New inspection session
            inspection = new FinalDimensionalInspection();
            inspection.setInspectionCallNo(request.getInspectionCallNo());
            inspection.setLotNo(request.getLotNo());
            inspection.setHeatNo(request.getHeatNo());
            inspection.setSampleSize(request.getSampleSize());
            inspection.setStatus(request.getStatus() != null ? request.getStatus() : "OK");
            inspection.setRejected(request.getRejected() != null ? request.getRejected() : 0);
            inspection.setRemarks(request.getRemarks());
            inspection.setDateOfInspection(request.getDateOfInspection());
            inspection.setCreatedBy(userId);
            inspection.setUpdatedBy(userId);
            log.info("Creating new dimensional inspection record");
        }

        inspection = dimensionalInspectionRepository.save(inspection);

        // ✅ UPSERT PATTERN: Update existing samples or insert new ones
        if (request.getSamples() != null && !request.getSamples().isEmpty()) {
            final FinalDimensionalInspection finalInspection = inspection;

            // Fetch existing samples for this inspection
            List<FinalDimensionalInspectionSample> existingSamples = sampleRepository.findByFinalDimensionalInspectionId(inspection.getId());

            // Create a map of existing samples by samplingNo for quick lookup
            Map<Integer, FinalDimensionalInspectionSample> existingSampleMap = existingSamples.stream()
                .collect(Collectors.toMap(
                    FinalDimensionalInspectionSample::getSamplingNo,
                    s -> s
                ));

            // Track which samples were updated
            Map<Integer, Boolean> updatedSamples = new java.util.HashMap<>();

            // Process incoming samples: update existing or create new
            List<FinalDimensionalInspectionSample> samplesToSave = request.getSamples().stream()
                .map(sampleData -> {
                    Integer samplingNo = sampleData.getSamplingNo();
                    FinalDimensionalInspectionSample sample;

                    if (existingSampleMap.containsKey(samplingNo)) {
                        // UPSERT: Update existing sample
                        sample = existingSampleMap.get(samplingNo);
                        sample.setGoGaugeFailed(sampleData.getGoGaugeFailed() != null ? sampleData.getGoGaugeFailed() : 0);
                        sample.setNoGoGaugeFailed(sampleData.getNoGoGaugeFailed() != null ? sampleData.getNoGoGaugeFailed() : 0);
                        sample.setFlatnessFailed(sampleData.getFlatnessFailed() != null ? sampleData.getFlatnessFailed() : 0);
                        log.info("Updating existing sample: samplingNo={}", samplingNo);
                    } else {
                        // UPSERT: Create new sample
                        sample = new FinalDimensionalInspectionSample();
                        sample.setFinalDimensionalInspection(finalInspection);
                        sample.setSamplingNo(samplingNo);
                        sample.setGoGaugeFailed(sampleData.getGoGaugeFailed() != null ? sampleData.getGoGaugeFailed() : 0);
                        sample.setNoGoGaugeFailed(sampleData.getNoGoGaugeFailed() != null ? sampleData.getNoGoGaugeFailed() : 0);
                        sample.setFlatnessFailed(sampleData.getFlatnessFailed() != null ? sampleData.getFlatnessFailed() : 0);
                        sample.setCreatedBy(userId);
                        log.info("Creating new sample: samplingNo={}", samplingNo);
                    }

                    updatedSamples.put(samplingNo, true);
                    return sample;
                })
                .collect(Collectors.toList());

            // Save all samples (both updated and new)
            sampleRepository.saveAll(samplesToSave);
            log.info("Saved {} samples for inspection id={}", samplesToSave.size(), inspection.getId());

            // Delete orphaned samples (samples that exist in DB but not in the request)
            List<FinalDimensionalInspectionSample> orphanedSamples = existingSamples.stream()
                .filter(s -> !updatedSamples.containsKey(s.getSamplingNo()))
                .collect(Collectors.toList());

            if (!orphanedSamples.isEmpty()) {
                sampleRepository.deleteAll(orphanedSamples);
                log.info("Deleted {} orphaned samples for inspection id={}", orphanedSamples.size(), inspection.getId());
            }
        }

        return mapToResponse(inspection);
    }

    @Override
    public Optional<FinalDimensionalInspectionResponse> getDimensionalInspection(
            String inspectionCallNo, String lotNo, String heatNo) {
        return dimensionalInspectionRepository.findByInspectionCallNoAndLotNoAndHeatNo(inspectionCallNo, lotNo, heatNo)
            .map(this::mapToResponse);
    }

    @Override
    public List<FinalDimensionalInspectionResponse> getDimensionalInspectionsByCall(String inspectionCallNo) {
        return dimensionalInspectionRepository.findByInspectionCallNo(inspectionCallNo).stream()
            .map(this::mapToResponse)
            .collect(Collectors.toList());
    }

    @Override
    public Optional<FinalDimensionalInspectionResponse> getDimensionalInspectionById(Long id) {
        return dimensionalInspectionRepository.findById(id)
            .map(this::mapToResponse);
    }

    @Override
    @Transactional
    public FinalDimensionalInspectionResponse updateStatus(Long id, String status, String userId) {
        FinalDimensionalInspection inspection = dimensionalInspectionRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Dimensional inspection not found"));
        inspection.setStatus(status);
        inspection.setUpdatedBy(userId);
        inspection.setUpdatedAt(LocalDateTime.now());
        inspection = dimensionalInspectionRepository.save(inspection);
        return mapToResponse(inspection);
    }

    @Override
    @Transactional
    public FinalDimensionalInspectionResponse updateRemarks(Long id, String remarks, String userId) {
        FinalDimensionalInspection inspection = dimensionalInspectionRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Dimensional inspection not found"));
        inspection.setRemarks(remarks);
        inspection.setUpdatedBy(userId);
        inspection.setUpdatedAt(LocalDateTime.now());
        inspection = dimensionalInspectionRepository.save(inspection);
        return mapToResponse(inspection);
    }

    @Override
    @Transactional
    public void deleteDimensionalInspection(Long id) {
        dimensionalInspectionRepository.deleteById(id);
    }

    @Override
    @Transactional
    public void deleteDimensionalInspectionsByCall(String inspectionCallNo) {
        List<FinalDimensionalInspection> inspections = dimensionalInspectionRepository.findByInspectionCallNo(inspectionCallNo);
        dimensionalInspectionRepository.deleteAll(inspections);
    }

    private FinalDimensionalInspectionResponse mapToResponse(FinalDimensionalInspection inspection) {
        FinalDimensionalInspectionResponse response = new FinalDimensionalInspectionResponse();
        response.setId(inspection.getId());
        response.setInspectionCallNo(inspection.getInspectionCallNo());
        response.setLotNo(inspection.getLotNo());
        response.setHeatNo(inspection.getHeatNo());
        response.setSampleSize(inspection.getSampleSize());
        response.setStatus(inspection.getStatus());
        response.setRejected(inspection.getRejected());
        response.setRemarks(inspection.getRemarks());
        response.setDateOfInspection(inspection.getDateOfInspection());
        response.setCreatedBy(inspection.getCreatedBy());
        response.setCreatedAt(inspection.getCreatedAt());
        response.setUpdatedBy(inspection.getUpdatedBy());
        response.setUpdatedAt(inspection.getUpdatedAt());

        // Map samples
        if (inspection.getSamples() != null) {
            response.setSamples(inspection.getSamples().stream()
                .map(this::mapSampleToResponse)
                .collect(Collectors.toList()));
        }

        return response;
    }

    private FinalDimensionalInspectionResponse.SampleResponse mapSampleToResponse(FinalDimensionalInspectionSample sample) {
        FinalDimensionalInspectionResponse.SampleResponse response = new FinalDimensionalInspectionResponse.SampleResponse();
        response.setId(sample.getId());
        response.setSamplingNo(sample.getSamplingNo());
        response.setGoGaugeFailed(sample.getGoGaugeFailed());
        response.setNoGoGaugeFailed(sample.getNoGoGaugeFailed());
        response.setFlatnessFailed(sample.getFlatnessFailed());
        response.setCreatedAt(sample.getCreatedAt());
        response.setCreatedBy(sample.getCreatedBy());
        return response;
    }
}

