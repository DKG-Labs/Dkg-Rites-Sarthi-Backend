package com.sarthi.service;

import com.sarthi.dto.FinalIcSaveChangesDTO;
import com.sarthi.entity.finalmaterial.FinalIcSaveChanges;
import com.sarthi.repository.finalmaterial.FinalIcSaveChangesRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class FinalIcSaveChangesService {

    private final FinalIcSaveChangesRepository finalIcSaveChangesRepository;

    @Transactional(readOnly = true)
    public FinalIcSaveChangesDTO getByIcNumber(String icNumber) {
        return finalIcSaveChangesRepository.findByIcNumber(icNumber)
                .map(this::mapToDTO)
                .orElse(null);
    }

    @Transactional
    public FinalIcSaveChangesDTO saveOrUpdate(FinalIcSaveChangesDTO dto) {
        FinalIcSaveChanges entity = finalIcSaveChangesRepository.findByIcNumber(dto.getIcNumber())
                .orElse(new FinalIcSaveChanges());

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
        
        if (entity.getId() == null) {
            entity.setCreatedBy(dto.getCreatedBy() != null ? dto.getCreatedBy() : "SYSTEM_USER");
        }
        entity.setUpdatedBy(dto.getUpdatedBy() != null ? dto.getUpdatedBy() : (dto.getCreatedBy() != null ? dto.getCreatedBy() : "SYSTEM_USER"));

        FinalIcSaveChanges saved = finalIcSaveChangesRepository.save(entity);
        return mapToDTO(saved);
    }

    private FinalIcSaveChangesDTO mapToDTO(FinalIcSaveChanges entity) {
        return FinalIcSaveChangesDTO.builder()
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
