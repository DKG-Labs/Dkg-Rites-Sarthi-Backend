package com.sarthi.SRailPad.service.plantDeclaration.Impl;

import com.sarthi.SRailPad.dto.plantDeclaration.ApprovedAshSGRequestDto;
import com.sarthi.SRailPad.dto.plantDeclaration.ApprovedAshSGResponseDto;
import com.sarthi.SRailPad.entity.plantDeclaration.ApprovedAshSG;
import com.sarthi.SRailPad.repository.RailWorkflowTransactionRepository;
import com.sarthi.SRailPad.repository.plantDeclaration.RailApprovedAshSGRepository;
import com.sarthi.SRailPad.service.RailWorkflowService;
import com.sarthi.SRailPad.service.plantDeclaration.RailApprovedAshSGService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class RailApprovedAshSGServiceImpl implements RailApprovedAshSGService {

    @Autowired
    private RailApprovedAshSGRepository repository;

    @Autowired
    private RailWorkflowTransactionRepository workflowTransactionRepository;

    @Autowired
    private RailWorkflowService railWorkflowService;

    @Autowired
    private com.sarthi.SRailPad.repository.RailUnblockWorkflowHistoryRepository unblockHistoryRepository;

    private static final Long MODULE_ID = 5L;
    private static final Long WORKFLOW_ID = 1L;

    @Override
    @Transactional
    public ApprovedAshSGResponseDto create(ApprovedAshSGRequestDto dto) {
        ApprovedAshSG entity = new ApprovedAshSG();
        mapDtoToEntity(dto, entity);
        entity.setCreatedDate(LocalDateTime.now());
        entity.setCreatedBy(dto.getCreatedBy());

        repository.save(entity);

        // Trigger Workflow
        railWorkflowService.initiateWorkflow(
                String.valueOf(entity.getId()),
                MODULE_ID,
                WORKFLOW_ID,
                dto.getCreatedBy(),
                dto.getVendorCode(),
                dto.getPlantId(),
                dto.getShift()
        );

        return buildResponse(entity);
    }

    @Override
    @Transactional
    public ApprovedAshSGResponseDto update(Long id, ApprovedAshSGRequestDto dto) {
        ApprovedAshSG entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Approved Ash & SG record not found"));

        mapDtoToEntity(dto, entity);
        entity.setUpdatedBy(dto.getUpdatedBy());
        entity.setUpdatedDate(LocalDateTime.now());

        repository.save(entity);
        return buildResponse(entity);
    }

    private void mapDtoToEntity(ApprovedAshSGRequestDto dto, ApprovedAshSG entity) {
        entity.setVendorName(dto.getVendorName());
        entity.setVendorCode(dto.getVendorCode());
        entity.setPlantId(dto.getPlantId());
        entity.setShift(dto.getShift());
        entity.setPadType(dto.getPadType());
        entity.setAshContentA(dto.getAshContentA());
        entity.setSpecificGravityA(dto.getSpecificGravityA());
        entity.setAshContentB(dto.getAshContentB());
        entity.setSpecificGravityB(dto.getSpecificGravityB());
        entity.setApprovalRefNo(dto.getApprovalRefNo());
        entity.setApprovalDate(dto.getApprovalDate());
    }

    @Override
    public ApprovedAshSGResponseDto getById(Long id) {
        ApprovedAshSG entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Approved Ash & SG record not found"));
        return buildResponse(entity);
    }

    @Override
    public List<ApprovedAshSGResponseDto> getAllByVendorCode(String vendorCode) {
        return repository.findAllByVendorCode(vendorCode).stream()
                .map(this::buildResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<ApprovedAshSGResponseDto> getAllByPlantId(String plantId) {
        return repository.findAllByPlantId(plantId).stream()
                .map(this::buildResponse)
                .collect(Collectors.toList());
    }

    @Override
    public void delete(Long id) {
        repository.deleteById(id);
    }

    @Override
    @Transactional
    public void unblockApprovedAshSG(Long id, com.sarthi.SRailPad.dto.RailUnblockReqDto unblockDto) {
        if (id == null) return;

        ApprovedAshSG ashSg = repository.findById(id).orElse(null);
        if (ashSg == null) {
            throw new RuntimeException("Approved Ash & SG not found with id: " + id);
        }

        // 1. Fetch latest workflow transaction to record previous state
        String reqIdStr = String.valueOf(id);
        com.sarthi.SRailPad.entity.RailWorkflowTransaction latestTx = 
                workflowTransactionRepository.findFirstByRequestIdOrderByWorkflowTransitionIdDesc(reqIdStr);

        // 2. Record audit trail in Rail_unblock_workflow_hitory table
        try {
            com.sarthi.SRailPad.entity.RailUnblockWorkflowHistory history = new com.sarthi.SRailPad.entity.RailUnblockWorkflowHistory();
            history.setRequestId(reqIdStr);
            history.setModuleId(MODULE_ID);
            history.setModuleName("APPROVED_ASH_SG");
            history.setWorkflowId(WORKFLOW_ID);
            history.setPlantId(ashSg.getPlantId());
            history.setVendorCode(ashSg.getVendorCode());
            history.setShift(ashSg.getShift());
            if (latestTx != null) {
                history.setPreviousStatus(latestTx.getStatus());
                history.setPreviousAction(latestTx.getAction());
                history.setPreviousRemarks(latestTx.getRemarks());
            } else {
                history.setPreviousStatus("COMPLETED");
                history.setPreviousAction("VERIFY");
            }
            if (unblockDto != null) {
                history.setUnblockedBy(unblockDto.getUnblockedBy());
                history.setUnblockedByName(unblockDto.getUnblockedByName());
                history.setUnblockedByRole(unblockDto.getUnblockedByRole());
                history.setUnblockRemarks(unblockDto.getRemarks());
            }
            history.setUnblockedOn(LocalDateTime.now());
            if (unblockHistoryRepository != null) {
                unblockHistoryRepository.save(history);
            }
        } catch (Exception e) {
            System.err.println("Warning: Error saving unblock history for Approved Ash & SG " + id + ": " + e.getMessage());
        }

        // 3. Delete completed workflow transactions for this Approved Ash & SG
        workflowTransactionRepository.deleteByRequestIdAndModuleId(reqIdStr, MODULE_ID);

        // 4. Re-initiate pending workflow transaction
        railWorkflowService.initiateWorkflow(
                reqIdStr,
                MODULE_ID,
                WORKFLOW_ID,
                ashSg.getCreatedBy(),
                ashSg.getVendorCode(),
                ashSg.getPlantId(),
                ashSg.getShift()
        );
    }

    private ApprovedAshSGResponseDto buildResponse(ApprovedAshSG entity) {
        ApprovedAshSGResponseDto dto = new ApprovedAshSGResponseDto();
        dto.setId(entity.getId());
        dto.setVendorName(entity.getVendorName());
        dto.setVendorCode(entity.getVendorCode());
        dto.setPlantId(entity.getPlantId());
        dto.setShift(entity.getShift());
        dto.setPadType(entity.getPadType());
        dto.setAshContentA(entity.getAshContentA());
        dto.setSpecificGravityA(entity.getSpecificGravityA());
        dto.setAshContentB(entity.getAshContentB());
        dto.setSpecificGravityB(entity.getSpecificGravityB());
        dto.setApprovalRefNo(entity.getApprovalRefNo());
        dto.setApprovalDate(entity.getApprovalDate());
        dto.setCreatedBy(entity.getCreatedBy());
        dto.setCreatedDate(entity.getCreatedDate());
        dto.setUpdatedBy(entity.getUpdatedBy());
        dto.setUpdatedDate(entity.getUpdatedDate());

        // Get Status from Workflow
        String status = workflowTransactionRepository
                .findLatestStatusByRequestIdAndModuleId(String.valueOf(entity.getId()), MODULE_ID)
                .orElse("NOT_STARTED");
        dto.setStatus(status);

        return dto;
    }
}
