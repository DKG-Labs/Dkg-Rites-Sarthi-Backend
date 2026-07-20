package com.sarthi.service;

import com.sarthi.dto.FinalIcEditDTO;
import com.sarthi.entity.finalmaterial.FinalIcEdit;
import com.sarthi.repository.finalmaterial.FinalIcEditRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class FinalIcEditService {

    private final FinalIcEditRepository finalIcEditRepository;

    @Transactional(readOnly = true)
    public FinalIcEditDTO getByIcNumber(String icNumber) {
        return finalIcEditRepository.findByIcNumber(icNumber)
                .map(this::mapToDTO)
                .orElse(null);
    }

    @Transactional
    public FinalIcEditDTO saveOrUpdate(FinalIcEditDTO dto) {
        FinalIcEdit entity = finalIcEditRepository.findByIcNumber(dto.getIcNumber())
                .orElse(new FinalIcEdit());

        entity.setIcNumber(dto.getIcNumber());
        entity.setCertificateId(dto.getCertificateId());
        entity.setBookNo(dto.getBookNo());
        entity.setSetNo(dto.getSetNo());
        entity.setOfferedInstallmentNo(dto.getOfferedInstallmentNo());
        entity.setPassedInstallmentNo(dto.getPassedInstallmentNo());
        entity.setConsignee(dto.getConsignee());
        entity.setCummQtyOfferedPrev(dto.getCummQtyOfferedPrev());
        entity.setQtyPrevPassed(dto.getQtyPrevPassed());
        entity.setQtyStillDue(dto.getQtyStillDue());
        entity.setMaNumberAndDate(dto.getMaNumberAndDate());
        entity.setPurchasingAuthority(dto.getPurchasingAuthority());
        entity.setDescription(dto.getDescription());
        entity.setTrRecDate(dto.getTrRecDate());
        entity.setNoOfVisits(dto.getNoOfVisits());
        entity.setDatesOfInspection(dto.getDatesOfInspection());
        
        // Use provided user if available, fallback to SYSTEM_USER
        if (entity.getId() == null) {
            entity.setCreatedBy(dto.getCreatedBy() != null ? dto.getCreatedBy() : "SYSTEM_USER");
        }
        entity.setUpdatedBy(dto.getUpdatedBy() != null ? dto.getUpdatedBy() : (dto.getCreatedBy() != null ? dto.getCreatedBy() : "SYSTEM_USER"));

        FinalIcEdit saved = finalIcEditRepository.save(entity);
        return mapToDTO(saved);
    }

    private FinalIcEditDTO mapToDTO(FinalIcEdit entity) {
        return FinalIcEditDTO.builder()
                .icNumber(entity.getIcNumber())
                .certificateId(entity.getCertificateId())
                .bookNo(entity.getBookNo())
                .setNo(entity.getSetNo())
                .offeredInstallmentNo(entity.getOfferedInstallmentNo())
                .passedInstallmentNo(entity.getPassedInstallmentNo())
                .consignee(entity.getConsignee())
                .cummQtyOfferedPrev(entity.getCummQtyOfferedPrev())
                .qtyPrevPassed(entity.getQtyPrevPassed())
                .qtyStillDue(entity.getQtyStillDue())
                .maNumberAndDate(entity.getMaNumberAndDate())
                .purchasingAuthority(entity.getPurchasingAuthority())
                .description(entity.getDescription())
                .trRecDate(entity.getTrRecDate())
                .noOfVisits(entity.getNoOfVisits())
                .datesOfInspection(entity.getDatesOfInspection())
                .createdBy(entity.getCreatedBy())
                .createdAt(entity.getCreatedAt())
                .updatedBy(entity.getUpdatedBy())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
