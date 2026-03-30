package com.sarthi.Sleeper.service.Impl;

import com.sarthi.Sleeper.dto.MixDesignRequestDto;
import com.sarthi.Sleeper.dto.MixDesignResponseDto;
import com.sarthi.Sleeper.entity.MixDesign;
import com.sarthi.Sleeper.entity.SleeperWorkflowTransaction;
import com.sarthi.Sleeper.repository.MixDesignRepository;
import com.sarthi.Sleeper.repository.SleeperWorkflowRepository;
import com.sarthi.Sleeper.service.MixDesignService;
import com.sarthi.constant.AppConstant;
import com.sarthi.exception.BusinessException;
import com.sarthi.exception.ErrorDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class MixDesignServiceImpl implements MixDesignService {

    @Autowired
    private MixDesignRepository repository;

    @Autowired
    private SleeperWorkflowRepository sleeperWorkflowRepository;

    // ================= CREATE =================
    @Override
    public MixDesignResponseDto create(MixDesignRequestDto dto) {

        MixDesign entity = new MixDesign();

        // map all fields directly
        entity.setIdentification(dto.getIdentification());
        entity.setConcreteGrade(dto.getConcreteGrade());
        entity.setAuthorityOfApproval(dto.getAuthorityOfApproval());
        entity.setCement(dto.getCement());
        entity.setCa1(dto.getCa1());
        entity.setCa2(dto.getCa2());
        entity.setFa(dto.getFa());
        entity.setWater(dto.getWater());
        entity.setVendorCode(dto.getVendorCode());

        entity.setPlantId(dto.getPlantId());
        entity.setAcRatio(dto.getAcRatio());   // coming from frontend
        entity.setWcRatio(dto.getWcRatio());   // coming from frontend

        entity.setCreatedBy(dto.getCreatedBy());
        entity.setCreatedDate(LocalDateTime.now());

        repository.save(entity);

        return buildResponse(entity);
    }

    // ================= UPDATE =================
    @Override
    public MixDesignResponseDto update(Long id, MixDesignRequestDto dto) {

        MixDesign entity = repository.findById(id)
                .orElseThrow(() -> new BusinessException(
                        new ErrorDetails(
                                AppConstant.ERROR_CODE_RESOURCE,
                                AppConstant.ERROR_TYPE_CODE_RESOURCE,
                                AppConstant.ERROR_TYPE_VALIDATION,
                                "Mix Design not found")));
        entity.setIdentification(dto.getIdentification());
        entity.setConcreteGrade(dto.getConcreteGrade());
        entity.setAuthorityOfApproval(dto.getAuthorityOfApproval());
        entity.setCement(dto.getCement());
        entity.setCa1(dto.getCa1());
        entity.setCa2(dto.getCa2());
        entity.setFa(dto.getFa());
        entity.setWater(dto.getWater());
        entity.setVendorCode(dto.getVendorCode());

        entity.setPlantId(dto.getPlantId());

        entity.setAcRatio(dto.getAcRatio());   // from frontend
        entity.setWcRatio(dto.getWcRatio());   // from frontend

        entity.setUpdatedBy(dto.getUpdatedBy());
        entity.setUpdatedDate(LocalDateTime.now());

        repository.save(entity);

        return buildResponse(entity);
    }

    // ================= GET BY ID =================
    @Override
    public MixDesignResponseDto getById(Long id) {

        MixDesign entity = repository.findById(id)
                .orElseThrow(() -> new BusinessException(
                        new ErrorDetails(
                                AppConstant.ERROR_CODE_RESOURCE,
                                AppConstant.ERROR_TYPE_CODE_RESOURCE,
                                AppConstant.ERROR_TYPE_VALIDATION,
                                "Mix Design not found")));
        return buildResponse(entity);
    }

    // ================= GET ALL =================
    @Override
    public List<MixDesignResponseDto> getAll() {

        List<MixDesignResponseDto> list = new ArrayList<>();

        for (MixDesign entity : repository.findAll()) {
            list.add(buildResponse(entity));
        }

        return list;
    }

    // ================= GET VERIFIED IDENTIFICATIONS =================
    @Override
    public List<String> getVerifiedMixDesignIdentifications() {
        List<String> requestIds = sleeperWorkflowRepository.findCompletedRequestIdsByModuleId(4L);
        if (requestIds == null || requestIds.isEmpty()) {
            return new ArrayList<>();
        }

        List<Long> ids = requestIds.stream()
                .map(Long::valueOf)
                .toList();

        List<MixDesign> verifiedMixes = repository.findByIdIn(ids);

        return verifiedMixes.stream()
                .map(mix -> mix.getIdentification() + "(" + mix.getCreatedBy() + ")")
                .toList();
    }

    // ================= DELETE =================
    @Override
    public void delete(Long id) {
        MixDesign entity = repository.findById(id)
                .orElseThrow(() -> new BusinessException(
                        new ErrorDetails(
                                AppConstant.ERROR_CODE_RESOURCE,
                                AppConstant.ERROR_TYPE_CODE_RESOURCE,
                                AppConstant.ERROR_TYPE_VALIDATION,
                                "Mix Design not found")));
        repository.deleteById(entity.getId());

        Long moduleId = 4L;

        SleeperWorkflowTransaction lastWorkflow =
                sleeperWorkflowRepository
                        .findTopByModuleIdAndRequestIdOrderByWorkflowTransitionIdDesc(
                                moduleId,
                                String.valueOf(entity.getId())
                        );

        SleeperWorkflowTransaction newWorkflow = new SleeperWorkflowTransaction();

        newWorkflow.setModuleId(moduleId);
        newWorkflow.setRequestId(String.valueOf(entity.getId()));

        newWorkflow.setAction("DELETE");
        newWorkflow.setStatus("DELETED");

        if (lastWorkflow != null) {
            newWorkflow.setWorkflowId(lastWorkflow.getWorkflowId());
            newWorkflow.setCurrentRole(lastWorkflow.getCurrentRole());
            newWorkflow.setNextRole(null);
            newWorkflow.setAssignedToUser(lastWorkflow.getAssignedToUser());
        }

        newWorkflow.setModifiedBy(Long.valueOf(entity.getCreatedBy()));
        newWorkflow.setCreatedDate(LocalDateTime.now());

        sleeperWorkflowRepository.save(newWorkflow);
    }

    private MixDesignResponseDto buildResponse(MixDesign entity) {

        MixDesignResponseDto dto = new MixDesignResponseDto();

        dto.setId(entity.getId());
        dto.setIdentification(entity.getIdentification());
        dto.setConcreteGrade(entity.getConcreteGrade());
        dto.setAuthorityOfApproval(entity.getAuthorityOfApproval());
        dto.setCement(entity.getCement());
        dto.setCa1(entity.getCa1());
        dto.setCa2(entity.getCa2());
        dto.setFa(entity.getFa());
        dto.setWater(entity.getWater());
        dto.setAcRatio(entity.getAcRatio());
        dto.setWcRatio(entity.getWcRatio());
        dto.setCreatedBy(entity.getCreatedBy());
        dto.setCreatedDate(entity.getCreatedDate());
        dto.setUpdatedBy(entity.getUpdatedBy());
        dto.setUpdatedDate(entity.getUpdatedDate());
        dto.setVendorCode(entity.getVendorCode());
        dto.setPlantId(entity.getPlantId());
        String status = sleeperWorkflowRepository
                .findLatestStatusByRequestIdAndModuleId(String.valueOf(entity.getId()), 4L )
                .orElse("NOT_STARTED");
        if (status != null) {
            dto.setStatus(status);
        }

        return dto;
    }


    @Override
    public List<MixDesignResponseDto> getApprovedMixDesigns(Long moduleId) {

        List<MixDesign> list = repository.findApprovedMixDesigns(moduleId);

        return list.stream()
                .map(this::buildResponse)
                .toList();
    }
}
