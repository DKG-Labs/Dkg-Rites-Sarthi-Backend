package com.sarthi.SRailPad.service.Impl;

import com.sarthi.SRailPad.dto.RailpadFinalIcEditDTO;
import com.sarthi.SRailPad.entity.inspectionCall.RailpadFinalIcSaveChanges;
import com.sarthi.SRailPad.repository.inspectionCall.RailpadFinalIcSaveChangesRepository;
import com.sarthi.SRailPad.service.RailpadFinalIcSaveChangesService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RailpadFinalIcSaveChangesServiceImpl implements RailpadFinalIcSaveChangesService {

    private final RailpadFinalIcSaveChangesRepository repository;

    @Override
    public RailpadFinalIcEditDTO getByIcNumber(String icNumber) {
        return repository.findByIcNumber(icNumber)
                .map(this::mapToDto)
                .orElse(null);
    }

    @Override
    @Transactional
    public RailpadFinalIcEditDTO saveOrUpdate(RailpadFinalIcEditDTO dto) {
        RailpadFinalIcSaveChanges entity = repository.findByIcNumber(dto.getIcNumber())
                .orElse(new RailpadFinalIcSaveChanges());
        
        mapToEntity(dto, entity);
        RailpadFinalIcSaveChanges savedEntity = repository.save(entity);
        return mapToDto(savedEntity);
    }

    private void mapToEntity(RailpadFinalIcEditDTO dto, RailpadFinalIcSaveChanges entity) {
        entity.setIcNumber(dto.getIcNumber());
        entity.setBookNo(dto.getBookNo());
        entity.setSetNo(dto.getSetNo());
        entity.setOfferedInstNo(dto.getOfferedInstNo());
        entity.setPassedInstNo(dto.getPassedInstNo());
        entity.setContractRef(dto.getContractRef());
        entity.setBillPayingOfficer(dto.getBillPayingOfficer());
        entity.setConsignee(dto.getConsignee());
        entity.setPurchasingAuthority(dto.getPurchasingAuthority());
        entity.setDescription(dto.getDescription());
        entity.setQtyOfferedPreviously(dto.getQtyOfferedPreviously());
        entity.setQtyPassedPreviously(dto.getQtyPassedPreviously());
        entity.setQtyNowRejected(dto.getQtyNowRejected());
        entity.setQtyStillDue(dto.getQtyStillDue());
        entity.setQuantityNowPassedText(dto.getQuantityNowPassedText());
        entity.setNoOfItemsChecked(dto.getNoOfItemsChecked());
        entity.setDatesOfInspection(dto.getDatesOfInspection());
        entity.setTrRecDate(dto.getTrRecDate());
        entity.setSealingPattern(dto.getSealingPattern());
        entity.setFacsimileText(dto.getFacsimileText());
        entity.setReasonsForRejection(dto.getReasonsForRejection());
        entity.setInspectingEngineer(dto.getInspectingEngineer());
        if(entity.getId() == null) {
            entity.setCreatedBy(dto.getCreatedBy());
        }
        entity.setUpdatedBy(dto.getUpdatedBy());
    }

    private RailpadFinalIcEditDTO mapToDto(RailpadFinalIcSaveChanges entity) {
        return RailpadFinalIcEditDTO.builder()
                .icNumber(entity.getIcNumber())
                .bookNo(entity.getBookNo())
                .setNo(entity.getSetNo())
                .offeredInstNo(entity.getOfferedInstNo())
                .passedInstNo(entity.getPassedInstNo())
                .contractRef(entity.getContractRef())
                .billPayingOfficer(entity.getBillPayingOfficer())
                .consignee(entity.getConsignee())
                .purchasingAuthority(entity.getPurchasingAuthority())
                .description(entity.getDescription())
                .qtyOfferedPreviously(entity.getQtyOfferedPreviously())
                .qtyPassedPreviously(entity.getQtyPassedPreviously())
                .qtyNowRejected(entity.getQtyNowRejected())
                .qtyStillDue(entity.getQtyStillDue())
                .quantityNowPassedText(entity.getQuantityNowPassedText())
                .noOfItemsChecked(entity.getNoOfItemsChecked())
                .datesOfInspection(entity.getDatesOfInspection())
                .trRecDate(entity.getTrRecDate())
                .sealingPattern(entity.getSealingPattern())
                .facsimileText(entity.getFacsimileText())
                .reasonsForRejection(entity.getReasonsForRejection())
                .inspectingEngineer(entity.getInspectingEngineer())
                .createdBy(entity.getCreatedBy())
                .createdAt(entity.getCreatedAt())
                .updatedBy(entity.getUpdatedBy())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
