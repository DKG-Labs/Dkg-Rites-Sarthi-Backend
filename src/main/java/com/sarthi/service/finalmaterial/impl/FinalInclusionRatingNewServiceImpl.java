package com.sarthi.service.finalmaterial.impl;

import com.sarthi.dto.finalmaterial.FinalInclusionRatingNewRequest;
import com.sarthi.dto.finalmaterial.FinalInclusionRatingNewResponse;
import com.sarthi.entity.finalmaterial.FinalInclusionRatingNew;
import com.sarthi.entity.finalmaterial.FinalInclusionRatingSample;
import com.sarthi.repository.finalmaterial.FinalInclusionRatingNewRepository;
import com.sarthi.repository.finalmaterial.FinalInclusionRatingSampleRepository;
import com.sarthi.service.finalmaterial.FinalInclusionRatingNewService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class FinalInclusionRatingNewServiceImpl implements FinalInclusionRatingNewService {

    @Autowired
    private FinalInclusionRatingNewRepository repository;

    @Autowired
    private FinalInclusionRatingSampleRepository sampleRepository;

    @Override
    @Transactional
    public FinalInclusionRatingNewResponse saveOrUpdateInclusionRating(
            FinalInclusionRatingNewRequest request, String userId) {
        log.info("Saving/Updating inclusion rating for call={}, lot={}, heat={}", 
            request.getInspectionCallNo(), request.getLotNo(), request.getHeatNo());

        Optional<FinalInclusionRatingNew> existing = repository.findByInspectionCallNoAndLotNoAndHeatNo(
            request.getInspectionCallNo(), request.getLotNo(), request.getHeatNo());

        FinalInclusionRatingNew test;
        if (existing.isPresent()) {
            test = existing.get();
            test.setLotNo(request.getLotNo());
            test.setUpdatedBy(userId);
            test.setUpdatedAt(LocalDateTime.now());
            test.setRemarks(request.getRemarks());
            log.info("Updating existing inclusion rating session, id={}", test.getId());
        } else {
            test = new FinalInclusionRatingNew();
            test.setInspectionCallNo(request.getInspectionCallNo());
            test.setLotNo(request.getLotNo());
            test.setHeatNo(request.getHeatNo());
            test.setSampleSize(request.getSampleSize());
            test.setSamplingType(request.getSamplingType());
            test.setStatus(request.getStatus() != null ? request.getStatus() : "PENDING");
            test.setRemarks(request.getRemarks());
            test.setCreatedBy(userId);
            log.info("Creating new inclusion rating session");
        }

        test = repository.save(test);
        final FinalInclusionRatingNew savedTest = test;

        if (request.getSamples() != null && !request.getSamples().isEmpty()) {
            List<FinalInclusionRatingSample> existingSamples =
                sampleRepository.findByFinalInclusionRatingId(savedTest.getId());

            Map<String, FinalInclusionRatingSample> existingSampleMap = existingSamples.stream()
                .collect(Collectors.toMap(
                    s -> s.getSamplingNo() + "-" + s.getSampleNo(),
                    s -> s
                ));

            Map<String, Boolean> updatedSamples = new HashMap<>();
            List<FinalInclusionRatingSample> samplesToSave = request.getSamples().stream()
                .map(sampleData -> {
                    String key = sampleData.getSamplingNo() + "-" + sampleData.getSampleNo();
                    FinalInclusionRatingSample sample = existingSampleMap.getOrDefault(key,
                        new FinalInclusionRatingSample());

                    sample.setFinalInclusionRating(savedTest);
                    sample.setSamplingNo(sampleData.getSamplingNo());
                    sample.setSampleNo(sampleData.getSampleNo());
                    sample.setSampleValueA(sampleData.getSampleValueA());
                    sample.setSampleTypeA(sampleData.getSampleTypeA());
                    sample.setSampleValueB(sampleData.getSampleValueB());
                    sample.setSampleTypeB(sampleData.getSampleTypeB());
                    sample.setSampleValueC(sampleData.getSampleValueC());
                    sample.setSampleTypeC(sampleData.getSampleTypeC());
                    sample.setSampleValueD(sampleData.getSampleValueD());
                    sample.setSampleTypeD(sampleData.getSampleTypeD());
                    sample.setCreatedBy(userId);
                    
                    updatedSamples.put(key, true);
                    return sample;
                })
                .collect(Collectors.toList());

            sampleRepository.saveAll(samplesToSave);
            log.info("Saved {} samples for test id={}", samplesToSave.size(), savedTest.getId());

            List<FinalInclusionRatingSample> orphanedSamples = existingSamples.stream()
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
    public Optional<FinalInclusionRatingNewResponse> getInclusionRatingById(Long id) {
        return repository.findById(id).map(this::mapToResponse);
    }

    @Override
    public List<FinalInclusionRatingNewResponse> getInclusionRatingByCallNo(String inspectionCallNo) {
        return repository.findByInspectionCallNo(inspectionCallNo).stream()
            .map(this::mapToResponse)
            .collect(Collectors.toList());
    }

    @Override
    public List<FinalInclusionRatingNewResponse> getInclusionRatingByLotNo(String lotNo) {
        return repository.findByLotNo(lotNo).stream()
            .map(this::mapToResponse)
            .collect(Collectors.toList());
    }

    @Override
    public Optional<FinalInclusionRatingNewResponse> getInclusionRatingByCallLotHeat(
            String inspectionCallNo, String lotNo, String heatNo) {
        return repository.findByInspectionCallNoAndLotNoAndHeatNo(inspectionCallNo, lotNo, heatNo)
            .map(this::mapToResponse);
    }

    @Override
    @Transactional
    public FinalInclusionRatingNewResponse updateStatus(Long id, String status, String userId) {
        FinalInclusionRatingNew test = repository.findById(id)
            .orElseThrow(() -> new RuntimeException("Inclusion rating test not found"));
        test.setStatus(status);
        test.setUpdatedBy(userId);
        test.setUpdatedAt(LocalDateTime.now());
        test = repository.save(test);
        return mapToResponse(test);
    }

    @Override
    @Transactional
    public void deleteInclusionRating(Long id) {
        repository.deleteById(id);
    }

    private FinalInclusionRatingNewResponse mapToResponse(FinalInclusionRatingNew entity) {
        FinalInclusionRatingNewResponse response = new FinalInclusionRatingNewResponse();
        response.setId(entity.getId());
        response.setInspectionCallNo(entity.getInspectionCallNo());
        response.setLotNo(entity.getLotNo());
        response.setHeatNo(entity.getHeatNo());
        response.setSampleSize(entity.getSampleSize());
        response.setSamplingType(entity.getSamplingType());
        response.setRemarks(entity.getRemarks());
        response.setStatus(entity.getStatus());
        response.setCreatedBy(entity.getCreatedBy());
        response.setCreatedAt(entity.getCreatedAt());
        response.setUpdatedBy(entity.getUpdatedBy());
        response.setUpdatedAt(entity.getUpdatedAt());

        if (entity.getSamples() != null) {
            response.setSamples(entity.getSamples().stream()
                .map(sample -> {
                    FinalInclusionRatingNewResponse.SampleData sampleData = 
                        new FinalInclusionRatingNewResponse.SampleData();
                    sampleData.setId(sample.getId());
                    sampleData.setSamplingNo(sample.getSamplingNo());
                    sampleData.setSampleNo(sample.getSampleNo());
                    sampleData.setSampleValueA(sample.getSampleValueA());
                    sampleData.setSampleTypeA(sample.getSampleTypeA());
                    sampleData.setSampleValueB(sample.getSampleValueB());
                    sampleData.setSampleTypeB(sample.getSampleTypeB());
                    sampleData.setSampleValueC(sample.getSampleValueC());
                    sampleData.setSampleTypeC(sample.getSampleTypeC());
                    sampleData.setSampleValueD(sample.getSampleValueD());
                    sampleData.setSampleTypeD(sample.getSampleTypeD());
                    sampleData.setCreatedBy(sample.getCreatedBy());
                    sampleData.setCreatedAt(sample.getCreatedAt());
                    return sampleData;
                })
                .collect(Collectors.toList()));
        }

        return response;
    }
}

