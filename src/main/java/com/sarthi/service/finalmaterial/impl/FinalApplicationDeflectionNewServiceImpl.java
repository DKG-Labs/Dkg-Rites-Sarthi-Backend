package com.sarthi.service.finalmaterial.impl;

import com.sarthi.dto.finalmaterial.FinalApplicationDeflectionRequest;
import com.sarthi.dto.finalmaterial.FinalApplicationDeflectionResponse;
import com.sarthi.entity.finalmaterial.FinalApplicationDeflection;
import com.sarthi.entity.finalmaterial.FinalApplicationDeflectionSample;
import com.sarthi.repository.finalmaterial.FinalApplicationDeflectionRepository;
import com.sarthi.repository.finalmaterial.FinalApplicationDeflectionSampleRepository;
import com.sarthi.service.finalmaterial.FinalApplicationDeflectionNewService;
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
public class FinalApplicationDeflectionNewServiceImpl implements FinalApplicationDeflectionNewService {

    private final FinalApplicationDeflectionRepository applicationDeflectionRepository;
    private final FinalApplicationDeflectionSampleRepository sampleRepository;

    @Override
    @Transactional
    public FinalApplicationDeflectionResponse saveOrUpdateApplicationDeflection(
            FinalApplicationDeflectionRequest request, String userId) {
        log.info("Saving/Updating application & deflection test for call={}, lot={}, heat={}",
            request.getInspectionCallNo(), request.getLotNo(), request.getHeatNo());

        // Check if inspection session already exists
        Optional<FinalApplicationDeflection> existing = applicationDeflectionRepository
                .findByInspectionCallNoAndLotNoAndHeatNo(
                    request.getInspectionCallNo(),
                    request.getLotNo(),
                    request.getHeatNo()
                );

        FinalApplicationDeflection inspection;
        if (existing.isPresent()) {
            // SUBSEQUENT SAVE: Update existing parent row
            inspection = existing.get();
            inspection.setUpdatedBy(userId);
            inspection.setUpdatedAt(LocalDateTime.now());
            inspection.setRemarks(request.getRemarks());
            inspection.setDateOfInspection(request.getDateOfInspection());
            if (request.getStatus() != null) {
                inspection.setStatus(request.getStatus());
            }
            if (request.getRejected() != null) {
                inspection.setRejected(request.getRejected());
            }
            log.info("Updating existing application & deflection test session, id={}", inspection.getId());
        } else {
            // FIRST SAVE: Create new parent row
            inspection = new FinalApplicationDeflection();
            inspection.setInspectionCallNo(request.getInspectionCallNo());
            inspection.setLotNo(request.getLotNo());
            inspection.setHeatNo(request.getHeatNo());
            inspection.setSampleSize(request.getSampleSize());
            inspection.setStatus(request.getStatus() != null ? request.getStatus() : "PENDING");
            inspection.setRejected(request.getRejected() != null ? request.getRejected() : 0);
            inspection.setRemarks(request.getRemarks());
            inspection.setDateOfInspection(request.getDateOfInspection());
            inspection.setCreatedBy(userId);
            inspection.setCreatedAt(LocalDateTime.now());
            log.info("Creating new application & deflection test session");
        }

        // Save parent
        inspection = applicationDeflectionRepository.save(inspection);

        // ✅ UPSERT PATTERN: Update existing samples or insert new ones
        if (request.getSamples() != null && !request.getSamples().isEmpty()) {
            final FinalApplicationDeflection finalInspection = inspection;

            // Fetch existing samples for this inspection
            List<FinalApplicationDeflectionSample> existingSamples = sampleRepository.findByFinalApplicationDeflectionId(inspection.getId());

            // Create a map of existing samples by samplingNo for quick lookup
            Map<Integer, FinalApplicationDeflectionSample> existingSampleMap = existingSamples.stream()
                .collect(Collectors.toMap(
                    FinalApplicationDeflectionSample::getSamplingNo,
                    s -> s
                ));

            // Track which samples were updated
            Map<Integer, Boolean> updatedSamples = new java.util.HashMap<>();

            // Process incoming samples: update existing or create new
            List<FinalApplicationDeflectionSample> samplesToSave = request.getSamples().stream()
                .map(sampleData -> {
                    Integer samplingNo = sampleData.getSamplingNo();
                    FinalApplicationDeflectionSample sample;

                    if (existingSampleMap.containsKey(samplingNo)) {
                        // UPSERT: Update existing sample
                        sample = existingSampleMap.get(samplingNo);
                        sample.setNoOfSamplesFailed(sampleData.getNoOfSamplesFailed() != null ? sampleData.getNoOfSamplesFailed() : 0);
                        log.info("Updating existing sample: samplingNo={}", samplingNo);
                    } else {
                        // UPSERT: Create new sample
                        sample = new FinalApplicationDeflectionSample();
                        sample.setFinalApplicationDeflection(finalInspection);
                        sample.setSamplingNo(samplingNo);
                        sample.setNoOfSamplesFailed(sampleData.getNoOfSamplesFailed() != null ? sampleData.getNoOfSamplesFailed() : 0);
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
            List<FinalApplicationDeflectionSample> orphanedSamples = existingSamples.stream()
                .filter(s -> !updatedSamples.containsKey(s.getSamplingNo()))
                .collect(Collectors.toList());

            if (!orphanedSamples.isEmpty()) {
                sampleRepository.deleteAll(orphanedSamples);
                log.info("Deleted {} orphaned samples for inspection id={}", orphanedSamples.size(), inspection.getId());
            }
        }

        // Backend recalculation as a safety measure
        long totalRejected = sampleRepository.sumRejectedByTestId(inspection.getId());
        inspection.setRejected((int) totalRejected);
        inspection = applicationDeflectionRepository.save(inspection);

        return mapToResponse(inspection);
    }

    @Override
    public Optional<FinalApplicationDeflectionResponse> getApplicationDeflection(
            String inspectionCallNo, String lotNo, String heatNo) {
        return applicationDeflectionRepository.findByInspectionCallNoAndLotNoAndHeatNo(inspectionCallNo, lotNo, heatNo)
            .map(this::mapToResponse);
    }

    @Override
    public List<FinalApplicationDeflectionResponse> getApplicationDeflectionsByCall(String inspectionCallNo) {
        return applicationDeflectionRepository.findByInspectionCallNo(inspectionCallNo).stream()
            .map(this::mapToResponse)
            .collect(Collectors.toList());
    }

    @Override
    public Optional<FinalApplicationDeflectionResponse> getApplicationDeflectionById(Long id) {
        return applicationDeflectionRepository.findById(id)
            .map(this::mapToResponse);
    }

    @Override
    @Transactional
    public FinalApplicationDeflectionResponse updateStatus(Long id, String status, String userId) {
        FinalApplicationDeflection inspection = applicationDeflectionRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Application & deflection test not found"));
        inspection.setStatus(status);
        inspection.setUpdatedBy(userId);
        inspection.setUpdatedAt(LocalDateTime.now());
        inspection = applicationDeflectionRepository.save(inspection);
        return mapToResponse(inspection);
    }

    @Override
    @Transactional
    public FinalApplicationDeflectionResponse updateRemarks(Long id, String remarks, String userId) {
        FinalApplicationDeflection inspection = applicationDeflectionRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Application & deflection test not found"));
        inspection.setRemarks(remarks);
        inspection.setUpdatedBy(userId);
        inspection.setUpdatedAt(LocalDateTime.now());
        inspection = applicationDeflectionRepository.save(inspection);
        return mapToResponse(inspection);
    }

    @Override
    @Transactional
    public void deleteApplicationDeflection(Long id) {
        applicationDeflectionRepository.deleteById(id);
    }

    @Override
    @Transactional
    public void deleteApplicationDeflectionsByCall(String inspectionCallNo) {
        List<FinalApplicationDeflection> inspections = applicationDeflectionRepository.findByInspectionCallNo(inspectionCallNo);
        applicationDeflectionRepository.deleteAll(inspections);
    }

    private FinalApplicationDeflectionResponse mapToResponse(FinalApplicationDeflection inspection) {
        FinalApplicationDeflectionResponse response = new FinalApplicationDeflectionResponse();
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

    private FinalApplicationDeflectionResponse.SampleResponse mapSampleToResponse(FinalApplicationDeflectionSample sample) {
        FinalApplicationDeflectionResponse.SampleResponse response = new FinalApplicationDeflectionResponse.SampleResponse();
        response.setId(sample.getId());
        response.setSamplingNo(sample.getSamplingNo());
        response.setNoOfSamplesFailed(sample.getNoOfSamplesFailed());
        response.setCreatedAt(sample.getCreatedAt());
        response.setCreatedBy(sample.getCreatedBy());
        return response;
    }
}

