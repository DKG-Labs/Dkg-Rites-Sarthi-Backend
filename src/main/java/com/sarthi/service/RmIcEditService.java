package com.sarthi.service;

import com.sarthi.dto.RmIcEditDTO;
import com.sarthi.entity.rawmaterial.RmIcEdit;
import com.sarthi.repository.rawmaterial.RmIcEditRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class RmIcEditService {

    private final RmIcEditRepository rmIcEditRepository;

    @Transactional(readOnly = true)
    public RmIcEditDTO getByIcNumber(String icNumber) {
        return rmIcEditRepository.findByIcNumber(icNumber)
                .map(this::mapToDTO)
                .orElse(null);
    }

    @Transactional
    public RmIcEditDTO saveOrUpdate(RmIcEditDTO dto) {
        RmIcEdit entity = rmIcEditRepository.findByIcNumber(dto.getIcNumber())
                .orElse(new RmIcEdit());

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
        
        // Use provided user if available, fallback to SYSTEM_USER
        if (entity.getId() == null) {
            entity.setCreatedBy(dto.getCreatedBy() != null ? dto.getCreatedBy() : "SYSTEM_USER");
        }
        entity.setUpdatedBy(dto.getUpdatedBy() != null ? dto.getUpdatedBy() : (dto.getCreatedBy() != null ? dto.getCreatedBy() : "SYSTEM_USER"));

        RmIcEdit saved = rmIcEditRepository.save(entity);
        return mapToDTO(saved);
    }

    private RmIcEditDTO mapToDTO(RmIcEdit entity) {
        return RmIcEditDTO.builder()
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
