package com.sarthi.SRailPad.service.Impl;

import com.sarthi.SRailPad.dto.RailpadProcessIcEditDTO;
import com.sarthi.SRailPad.entity.inspectionCall.RailpadProcessIcSaveChanges;
import com.sarthi.SRailPad.repository.inspectionCall.RailpadProcessIcSaveChangesRepository;
import com.sarthi.SRailPad.service.RailpadProcessIcSaveChangesService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RailpadProcessIcSaveChangesServiceImpl implements RailpadProcessIcSaveChangesService {

    private final RailpadProcessIcSaveChangesRepository repository;

    @Override
    public RailpadProcessIcEditDTO getByIcNumber(String icNumber) {
        return repository.findByIcNumber(icNumber)
                .map(this::mapToDto)
                .orElse(null);
    }

    @Override
    @Transactional
    public RailpadProcessIcEditDTO saveOrUpdate(RailpadProcessIcEditDTO dto) {
        RailpadProcessIcSaveChanges entity = repository.findByIcNumber(dto.getIcNumber())
                .orElse(new RailpadProcessIcSaveChanges());
        mapToEntity(dto, entity);
        return mapToDto(repository.save(entity));
    }

    private void mapToEntity(RailpadProcessIcEditDTO dto, RailpadProcessIcSaveChanges entity) {
        entity.setIcNumber(dto.getIcNumber());
        entity.setBookNo(dto.getBookNo());
        entity.setSetNo(dto.getSetNo());
        entity.setInstallmentNo(dto.getInstallmentNo());
        entity.setContractor(dto.getContractor());
        entity.setContractRef(dto.getContractRef());
        entity.setBillPayingOfficer(dto.getBillPayingOfficer());
        entity.setConsignee(dto.getConsignee());
        entity.setPurchasingAuthority(dto.getPurchasingAuthority());
        entity.setDescription(dto.getDescription());
        entity.setDrgNo(dto.getDrgNo());
        entity.setSpecNo(dto.getSpecNo());
        entity.setQapNo(dto.getQapNo());
        entity.setTypeOfInspection(dto.getTypeOfInspection());
        entity.setChpClNo(dto.getChpClNo());
        entity.setLotNo(dto.getLotNo());
        entity.setQtyNowOffered(dto.getQtyNowOffered());
        entity.setQtyNowPassed(dto.getQtyNowPassed());
        entity.setQtyNowRejected(dto.getQtyNowRejected());
        entity.setQuantityNowPassedText(dto.getQuantityNowPassedText());
        entity.setReasonsForRejection(dto.getReasonsForRejection());
        entity.setDateOfCall(dto.getDateOfCall());
        entity.setNoOfVisits(dto.getNoOfVisits());
        entity.setDatesOfInspection(dto.getDatesOfInspection());
        entity.setSealingPattern(dto.getSealingPattern());
        entity.setInspectingEngineer(dto.getInspectingEngineer());
        if (entity.getId() == null) {
            entity.setCreatedBy(dto.getCreatedBy());
        }
        entity.setUpdatedBy(dto.getUpdatedBy());
    }

    private RailpadProcessIcEditDTO mapToDto(RailpadProcessIcSaveChanges entity) {
        return RailpadProcessIcEditDTO.builder()
                .icNumber(entity.getIcNumber())
                .bookNo(entity.getBookNo())
                .setNo(entity.getSetNo())
                .installmentNo(entity.getInstallmentNo())
                .contractor(entity.getContractor())
                .contractRef(entity.getContractRef())
                .billPayingOfficer(entity.getBillPayingOfficer())
                .consignee(entity.getConsignee())
                .purchasingAuthority(entity.getPurchasingAuthority())
                .description(entity.getDescription())
                .drgNo(entity.getDrgNo())
                .specNo(entity.getSpecNo())
                .qapNo(entity.getQapNo())
                .typeOfInspection(entity.getTypeOfInspection())
                .chpClNo(entity.getChpClNo())
                .lotNo(entity.getLotNo())
                .qtyNowOffered(entity.getQtyNowOffered())
                .qtyNowPassed(entity.getQtyNowPassed())
                .qtyNowRejected(entity.getQtyNowRejected())
                .quantityNowPassedText(entity.getQuantityNowPassedText())
                .reasonsForRejection(entity.getReasonsForRejection())
                .dateOfCall(entity.getDateOfCall())
                .noOfVisits(entity.getNoOfVisits())
                .datesOfInspection(entity.getDatesOfInspection())
                .sealingPattern(entity.getSealingPattern())
                .inspectingEngineer(entity.getInspectingEngineer())
                .createdBy(entity.getCreatedBy())
                .createdAt(entity.getCreatedAt())
                .updatedBy(entity.getUpdatedBy())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
