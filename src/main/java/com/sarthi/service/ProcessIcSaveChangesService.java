package com.sarthi.service;

import com.sarthi.dto.ProcessIcSaveChangesDTO;
import com.sarthi.entity.processmaterial.ProcessIcSaveChanges;
import com.sarthi.repository.processmaterial.ProcessIcSaveChangesRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProcessIcSaveChangesService {

    private final ProcessIcSaveChangesRepository processIcSaveChangesRepository;

    @Transactional(readOnly = true)
    public ProcessIcSaveChangesDTO getByIcNumber(String icNumber) {
        return processIcSaveChangesRepository.findByIcNumber(icNumber)
                .map(this::mapToDTO)
                .orElse(null);
    }

    @Transactional
    public ProcessIcSaveChangesDTO saveOrUpdate(ProcessIcSaveChangesDTO dto) {
        ProcessIcSaveChanges entity = processIcSaveChangesRepository.findByIcNumber(dto.getIcNumber())
                .orElse(new ProcessIcSaveChanges());

        entity.setIcNumber(dto.getIcNumber());
        entity.setCertificateId(dto.getCertificateId());
        entity.setBookNo(dto.getBookNo());
        entity.setSetNo(dto.getSetNo());
        entity.setOfferedInstallmentNo(dto.getOfferedInstallmentNo());
        entity.setPassedInstallmentNo(dto.getPassedInstallmentNo());
        entity.setConsignee(dto.getConsignee());
        entity.setContractRef(dto.getContractRef());
        entity.setMaNumberAndDate(dto.getMaNumberAndDate());
        entity.setBillPayingOfficer(dto.getBillPayingOfficer());
        entity.setPurchasingAuthority(dto.getPurchasingAuthority());
        entity.setDescription(dto.getDescription());
        entity.setQapNo(dto.getQapNo());
        entity.setInspectionDate(dto.getInspectionDate());
        entity.setManDays(dto.getManDays());
        
        if (entity.getId() == null) {
            entity.setCreatedBy(dto.getCreatedBy() != null ? dto.getCreatedBy() : "SYSTEM_USER");
        }
        entity.setUpdatedBy(dto.getUpdatedBy() != null ? dto.getUpdatedBy() : (dto.getCreatedBy() != null ? dto.getCreatedBy() : "SYSTEM_USER"));

        ProcessIcSaveChanges saved = processIcSaveChangesRepository.save(entity);
        return mapToDTO(saved);
    }

    private ProcessIcSaveChangesDTO mapToDTO(ProcessIcSaveChanges entity) {
        return ProcessIcSaveChangesDTO.builder()
                .icNumber(entity.getIcNumber())
                .certificateId(entity.getCertificateId())
                .bookNo(entity.getBookNo())
                .setNo(entity.getSetNo())
                .offeredInstallmentNo(entity.getOfferedInstallmentNo())
                .passedInstallmentNo(entity.getPassedInstallmentNo())
                .consignee(entity.getConsignee())
                .contractRef(entity.getContractRef())
                .maNumberAndDate(entity.getMaNumberAndDate())
                .billPayingOfficer(entity.getBillPayingOfficer())
                .purchasingAuthority(entity.getPurchasingAuthority())
                .description(entity.getDescription())
                .qapNo(entity.getQapNo())
                .inspectionDate(entity.getInspectionDate())
                .manDays(entity.getManDays())
                .createdBy(entity.getCreatedBy())
                .createdAt(entity.getCreatedAt())
                .updatedBy(entity.getUpdatedBy())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
