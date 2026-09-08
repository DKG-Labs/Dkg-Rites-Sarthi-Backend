package com.sarthi.Sleeper.service;

import com.sarthi.Sleeper.dto.FinalInspectionDtos.SleeperFinalIcEditDTO;
import com.sarthi.Sleeper.entity.FinalInspection.SleeperFinalIcEdit;
import com.sarthi.Sleeper.repository.FinalInspectionRepository.SleeperFinalIcEditRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class SleeperFinalIcEditService {

    private final SleeperFinalIcEditRepository sleeperFinalIcEditRepository;

    @Transactional(readOnly = true)
    public SleeperFinalIcEditDTO getByIcNumber(String icNumber) {
        if (icNumber == null || icNumber.trim().isEmpty()) {
            return null;
        }
        return sleeperFinalIcEditRepository.findByIcNumber(icNumber.trim())
                .map(this::mapToDTO)
                .orElse(null);
    }

    @Transactional
    public SleeperFinalIcEditDTO saveOrUpdate(SleeperFinalIcEditDTO dto) {
        if (dto == null || dto.getIcNumber() == null || dto.getIcNumber().trim().isEmpty()) {
            throw new IllegalArgumentException("IC number is required");
        }

        String icNo = dto.getIcNumber().trim();
        SleeperFinalIcEdit entity = sleeperFinalIcEditRepository.findByIcNumber(icNo)
                .orElse(new SleeperFinalIcEdit());

        entity.setIcNumber(icNo);
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
        entity.setManufacturer(dto.getManufacturer());
        entity.setTrRecDate(dto.getTrRecDate());
        entity.setNoOfVisits(dto.getNoOfVisits());
        entity.setDatesOfInspection(dto.getDatesOfInspection());
        entity.setSealingPattern(dto.getSealingPattern());
        entity.setReasonsForRejection(dto.getReasonsForRejection());
        entity.setFacsimileText(dto.getFacsimileText());
        entity.setInspectingEngineer(dto.getInspectingEngineer());
        entity.setStatus(dto.getStatus());

        if (entity.getId() == null) {
            entity.setCreatedBy(dto.getCreatedBy() != null ? dto.getCreatedBy() : "Inspecting Engineer");
        }
        entity.setUpdatedBy(dto.getUpdatedBy() != null ? dto.getUpdatedBy() : (dto.getCreatedBy() != null ? dto.getCreatedBy() : "Inspecting Engineer"));

        SleeperFinalIcEdit saved = sleeperFinalIcEditRepository.save(entity);
        log.info("Saved SleeperFinalIcEdit successfully for IC: {}", icNo);
        return mapToDTO(saved);
    }

    private SleeperFinalIcEditDTO mapToDTO(SleeperFinalIcEdit entity) {
        return SleeperFinalIcEditDTO.builder()
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
                .manufacturer(entity.getManufacturer())
                .trRecDate(entity.getTrRecDate())
                .noOfVisits(entity.getNoOfVisits())
                .datesOfInspection(entity.getDatesOfInspection())
                .sealingPattern(entity.getSealingPattern())
                .reasonsForRejection(entity.getReasonsForRejection())
                .facsimileText(entity.getFacsimileText())
                .inspectingEngineer(entity.getInspectingEngineer())
                .status(entity.getStatus())
                .createdBy(entity.getCreatedBy())
                .createdAt(entity.getCreatedAt())
                .updatedBy(entity.getUpdatedBy())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
