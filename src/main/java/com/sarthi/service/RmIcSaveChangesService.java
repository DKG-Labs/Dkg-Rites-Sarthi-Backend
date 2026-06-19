package com.sarthi.service;

import com.sarthi.dto.RmIcSaveChangesDTO;
import com.sarthi.entity.rawmaterial.RmIcSaveChanges;
import com.sarthi.repository.rawmaterial.RmIcSaveChangesRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class RmIcSaveChangesService {

    private final RmIcSaveChangesRepository rmIcSaveChangesRepository;

    @Transactional(readOnly = true)
    public RmIcSaveChangesDTO getByIcNumber(String icNumber) {
        return rmIcSaveChangesRepository.findByIcNumber(icNumber)
                .map(this::mapToDTO)
                .orElse(null);
    }

    @Transactional
    public RmIcSaveChangesDTO saveOrUpdate(RmIcSaveChangesDTO dto) {
        RmIcSaveChanges entity = rmIcSaveChangesRepository.findByIcNumber(dto.getIcNumber())
                .orElse(new RmIcSaveChanges());

        entity.setIcNumber(dto.getIcNumber());
        entity.setCertificateId(dto.getCertificateId());
        entity.setBookNo(dto.getBookNo());
        entity.setSetNo(dto.getSetNo());
        entity.setOfferedInstallmentNo(dto.getOfferedInstallmentNo());
        entity.setPassedInstallmentNo(dto.getPassedInstallmentNo());
        entity.setDrawingNo(dto.getDrawingNo());
        entity.setManufacturer(dto.getManufacturer());
        entity.setContractorPo(dto.getContractorPo());
        entity.setConsigneeRailway(dto.getConsigneeRailway());
        entity.setConsigneeManufacturer(dto.getConsigneeManufacturer());
        entity.setPurchasingAuthority(dto.getPurchasingAuthority());
        entity.setDescription(dto.getDescription());
        entity.setSpecNo(dto.getSpecNo());
        entity.setQapNo(dto.getQapNo());
        entity.setChpClause(dto.getChpClause());
        
        if (entity.getId() == null) {
            entity.setCreatedBy(dto.getCreatedBy() != null ? dto.getCreatedBy() : "SYSTEM_USER");
        }
        entity.setUpdatedBy(dto.getUpdatedBy() != null ? dto.getUpdatedBy() : (dto.getCreatedBy() != null ? dto.getCreatedBy() : "SYSTEM_USER"));

        RmIcSaveChanges saved = rmIcSaveChangesRepository.save(entity);
        return mapToDTO(saved);
    }

    private RmIcSaveChangesDTO mapToDTO(RmIcSaveChanges entity) {
        return RmIcSaveChangesDTO.builder()
                .icNumber(entity.getIcNumber())
                .certificateId(entity.getCertificateId())
                .bookNo(entity.getBookNo())
                .setNo(entity.getSetNo())
                .offeredInstallmentNo(entity.getOfferedInstallmentNo())
                .passedInstallmentNo(entity.getPassedInstallmentNo())
                .drawingNo(entity.getDrawingNo())
                .manufacturer(entity.getManufacturer())
                .contractorPo(entity.getContractorPo())
                .consigneeRailway(entity.getConsigneeRailway())
                .consigneeManufacturer(entity.getConsigneeManufacturer())
                .purchasingAuthority(entity.getPurchasingAuthority())
                .description(entity.getDescription())
                .specNo(entity.getSpecNo())
                .qapNo(entity.getQapNo())
                .chpClause(entity.getChpClause())
                .createdBy(entity.getCreatedBy())
                .createdAt(entity.getCreatedAt())
                .updatedBy(entity.getUpdatedBy())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
