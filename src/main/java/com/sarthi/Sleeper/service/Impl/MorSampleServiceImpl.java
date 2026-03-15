package com.sarthi.Sleeper.service.Impl;



import com.sarthi.Sleeper.dto.FinalInspectionDtos.MorSampleDetailDto;
import com.sarthi.Sleeper.dto.FinalInspectionDtos.MorSampleRequestDto;
import com.sarthi.Sleeper.dto.FinalInspectionDtos.MorSampleResponseDto;
import com.sarthi.Sleeper.dto.FinalInspectionDtos.MorTestResultDto;
import com.sarthi.Sleeper.entity.FinalInspection.MorSampleDeclaration;
import com.sarthi.Sleeper.entity.FinalInspection.MorSampleDetail;
import com.sarthi.Sleeper.entity.FinalInspection.MorTestResult;
import com.sarthi.Sleeper.entity.FinalInspection.WaterCubeStrengthTest;
import com.sarthi.Sleeper.repository.FinalInspectionRepository.MorSampleRepository;
import com.sarthi.Sleeper.repository.FinalInspectionRepository.WaterCubeStrengthRepository;

import com.sarthi.Sleeper.service.MorSampleService;
import com.sarthi.constant.AppConstant;
import com.sarthi.exception.BusinessException;
import com.sarthi.exception.ErrorDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
public class MorSampleServiceImpl implements MorSampleService {

    @Autowired
    private MorSampleRepository repository;

    @Autowired
    private WaterCubeStrengthRepository waterCubeStrengthRepository;

    // ================= CREATE =================
    @Override
    public MorSampleResponseDto create(MorSampleRequestDto dto) {

        MorSampleDeclaration entity = new MorSampleDeclaration();

        entity.setSamplingDate(parseDate(dto.getSamplingDate()));
        entity.setShift(dto.getShift());
        entity.setLineNo(dto.getLineNo());
        entity.setConcreteGrade(dto.getConcreteGrade());
        entity.setPlantType(dto.getPlantType());
        entity.setShedLine(dto.getShedLine());
        entity.setSampleIdentificationNumber(dto.getSampleIdentificationNumber());
        entity.setWaterCubeStrengthTestId(dto.getWaterCubeStrengthTestId());
        entity.setBatchNumber(dto.getBatchNumber());
        entity.setCastingDate(parseDate(dto.getCastingDate()));
        entity.setMrSamplesRequired(dto.getMrSamplesRequired());
        entity.setMrTestType(dto.getMrTestType());
        entity.setStatus("PENDING_TEST");

        entity.setCreatedBy(dto.getCreatedBy());
        entity.setCreatedDate(LocalDateTime.now());

        if (dto.getDetails() != null) {
            List<MorSampleDetail> details = new ArrayList<>();
            for (MorSampleDetailDto dDto : dto.getDetails()) {
                MorSampleDetail dEntity = new MorSampleDetail();
                dEntity.setDeclaration(entity);
                dEntity.setBenchNumber(dDto.getBenchNumber());
                dEntity.setSleeperNo(dDto.getSleeperNo());
                details.add(dEntity);
            }
            entity.setDetails(details);
        }

        repository.save(entity);

        return buildResponse(entity);
    }

    // ================= UPDATE =================
    @Override
    public MorSampleResponseDto update(Long id, MorSampleRequestDto dto) {

        MorSampleDeclaration entity = repository.findById(id)
                .orElseThrow(() -> new BusinessException(
                        new ErrorDetails(
                                AppConstant.ERROR_CODE_RESOURCE,
                                AppConstant.ERROR_TYPE_CODE_RESOURCE,
                                AppConstant.ERROR_TYPE_VALIDATION,
                                "MOR Sample not found")));

        entity.setSamplingDate(parseDate(dto.getSamplingDate()));
        entity.setShift(dto.getShift());
        entity.setLineNo(dto.getLineNo());
        entity.setConcreteGrade(dto.getConcreteGrade());
        entity.setPlantType(dto.getPlantType());
        entity.setShedLine(dto.getShedLine());
        entity.setSampleIdentificationNumber(dto.getSampleIdentificationNumber());

        entity.setUpdatedBy(dto.getUpdatedBy());
        entity.setUpdatedDate(LocalDateTime.now());

        repository.save(entity);

        return buildResponse(entity);
    }

    // ================= GET BY ID =================
    @Override
    public MorSampleResponseDto getById(Long id) {

        MorSampleDeclaration entity = repository.findById(id)
                .orElseThrow(() -> new BusinessException(
                        new ErrorDetails(
                                AppConstant.ERROR_CODE_RESOURCE,
                                AppConstant.ERROR_TYPE_CODE_RESOURCE,
                                AppConstant.ERROR_TYPE_VALIDATION,
                                "MOR Sample not found")));

        return buildResponse(entity);
    }

    // ================= GET ALL =================
    @Override
    public List<MorSampleResponseDto> getAll(Long userId) {
        List<MorSampleResponseDto> list = new ArrayList<>();
        for (MorSampleDeclaration entity : repository.findByStatusAndCreatedBy("PENDING_TEST", userId)) {
            list.add(buildResponse(entity));
        }
        return list;
    }

    // ================= GET HISTORICAL =================
    @Override
    public List<MorSampleResponseDto> getHistorical(Long userId) {
        List<MorSampleResponseDto> list = new ArrayList<>();
        for (MorSampleDeclaration entity : repository.findByStatusAndCreatedBy("COMPLETED", userId)) {
            list.add(buildResponse(entity));
        }
        return list;
    }

    // ================= GET PENDING =================
    @Override
    public List<MorSampleResponseDto> getPendingMorDeclarations(Long userId) {
        List<MorSampleResponseDto> pending = new ArrayList<>();

        // 1. Fetch all PASSED water cube strength tests
        List<WaterCubeStrengthTest> passTests = waterCubeStrengthRepository.findByFinalTestResult("PASS");

        for (WaterCubeStrengthTest test : passTests) {
            // 2. Filter out those already present in MorSampleDeclaration
            if (!repository.existsByWaterCubeStrengthTestId(test.getId())) {
                MorSampleResponseDto dto = new MorSampleResponseDto();
                dto.setWaterCubeStrengthTestId(test.getId());
                dto.setBatchNumber(test.getBatchNumber());
                dto.setConcreteGrade(test.getConcreteGrade());
                dto.setCastingDate(test.getCastingDate());
                dto.setShift(test.getShift());
                dto.setLineNo(test.getLineNo());
                dto.setMrSamplesRequired(test.getMrSamplesRequired());
                dto.setMrTestType("Fresh"); // Default for first record

                pending.add(dto);
            }
        }

        return pending;
    }

    // ================= SAVE TEST RESULTS =================
    @Override
    public MorSampleResponseDto saveTestResults(Long declarationId, List<MorTestResultDto> results) {
        MorSampleDeclaration declaration = repository.findById(declarationId)
                .orElseThrow(() -> new BusinessException(new ErrorDetails(AppConstant.ERROR_CODE_RESOURCE, AppConstant.ERROR_TYPE_CODE_RESOURCE, AppConstant.ERROR_TYPE_RESOURCE, "Declaration not found")));

        List<MorTestResult> testResults = new ArrayList<>();
        for (MorTestResultDto rDto : results) {
            MorTestResult rEntity = new MorTestResult();
            rEntity.setDeclaration(declaration);
            rEntity.setBenchNumber(rDto.getBenchNumber());
            rEntity.setSleeperNo(rDto.getSleeperNo());
            
            // Map structural fields
            rEntity.setCtKn(rDto.getCtKn());
            rEntity.setCbKn(rDto.getCbKn());
            rEntity.setRsKn(rDto.getRsKn());
            
            // Map database columns from screenshot
            rEntity.setWeight(rDto.getWeight());
            rEntity.setLoadKn(rDto.getLoadKn());
            rEntity.setStrength(rDto.getStrength());
            rEntity.setResult(rDto.getResult());
            rEntity.setRemarks(rDto.getRemarks());
            
            rEntity.setIsPass(rDto.getIsPass());
            rEntity.setTestDate(LocalDate.now());
            rEntity.setCreatedBy(declaration.getUpdatedBy()); // Or use from DTO
            rEntity.setCreatedDate(LocalDateTime.now());
            testResults.add(rEntity);
        }

        declaration.setTestResults(testResults);
        declaration.setStatus("COMPLETED");
        declaration.setUpdatedDate(LocalDateTime.now());

        repository.save(declaration);
        return buildResponse(declaration);
    }

    // ================= DATE PARSER =================
    private LocalDate parseDate(String dateStr) {
        if (dateStr == null || dateStr.isEmpty()) return null;
        try {
            if (dateStr.contains("/")) {
                return LocalDate.parse(dateStr, java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy"));
            }
            return LocalDate.parse(dateStr);
        } catch (Exception e) {
            return null;
        }
    }

    // ================= DELETE =================
    @Override
    public void delete(Long id) {

        MorSampleDeclaration entity = repository.findById(id)
                .orElseThrow(() -> new BusinessException(
                        new ErrorDetails(
                                AppConstant.ERROR_CODE_RESOURCE,
                                AppConstant.ERROR_TYPE_CODE_RESOURCE,
                                AppConstant.ERROR_TYPE_VALIDATION,
                                "MOR Sample not found")));

        repository.deleteById(entity.getId());
    }

    // ================= RESPONSE BUILDER =================
    private MorSampleResponseDto buildResponse(MorSampleDeclaration entity) {

        MorSampleResponseDto dto = new MorSampleResponseDto();

        dto.setId(entity.getId());
        dto.setSamplingDate(String.valueOf(entity.getSamplingDate()));
        dto.setShift(entity.getShift());
        dto.setLineNo(entity.getLineNo());
        dto.setConcreteGrade(entity.getConcreteGrade());
        dto.setPlantType(entity.getPlantType());
        dto.setShedLine(entity.getShedLine());
        dto.setSampleIdentificationNumber(entity.getSampleIdentificationNumber());

        dto.setWaterCubeStrengthTestId(entity.getWaterCubeStrengthTestId());
        dto.setBatchNumber(entity.getBatchNumber());
        dto.setCastingDate(String.valueOf(entity.getCastingDate()));
        dto.setMrSamplesRequired(entity.getMrSamplesRequired());
        dto.setMrTestType(entity.getMrTestType());
        dto.setStatus(entity.getStatus());

        if (entity.getTestResults() != null && !entity.getTestResults().isEmpty()) {
            boolean allPass = entity.getTestResults().stream().allMatch(tr -> Boolean.TRUE.equals(tr.getIsPass()));
            dto.setOverallResult(allPass ? "Pass" : "Fail");
        }

        if (entity.getDetails() != null) {
            List<MorSampleDetailDto> detailDtos = new ArrayList<>();
            for (MorSampleDetail d : entity.getDetails()) {
                MorSampleDetailDto dDto = new MorSampleDetailDto();
                dDto.setId(d.getId());
                dDto.setBenchNumber(d.getBenchNumber());
                dDto.setSleeperNo(d.getSleeperNo());
                detailDtos.add(dDto);
            }
            dto.setDetails(detailDtos);
        }

        if (entity.getTestResults() != null) {
            List<MorTestResultDto> resultDtos = new ArrayList<>();
            for (MorTestResult r : entity.getTestResults()) {
                MorTestResultDto rDto = new MorTestResultDto();
                rDto.setId(r.getId());
                rDto.setBenchNumber(r.getBenchNumber());
                rDto.setSleeperNo(r.getSleeperNo());
                rDto.setCtKn(r.getCtKn());
                rDto.setCbKn(r.getCbKn());
                rDto.setRsKn(r.getRsKn());
                
                rDto.setWeight(r.getWeight());
                rDto.setLoadKn(r.getLoadKn());
                rDto.setStrength(r.getStrength());
                rDto.setResult(r.getResult());
                rDto.setRemarks(r.getRemarks());
                
                rDto.setIsPass(r.getIsPass());
                rDto.setTestDate(String.valueOf(r.getTestDate()));
                rDto.setCreatedBy(r.getCreatedBy());
                resultDtos.add(rDto);
            }
            dto.setTestResults(resultDtos);
        }

        dto.setCreatedBy(entity.getCreatedBy());
        dto.setCreatedDate(entity.getCreatedDate());
        dto.setUpdatedBy(entity.getUpdatedBy());
        dto.setUpdatedDate(entity.getUpdatedDate());

        return dto;
    }
}