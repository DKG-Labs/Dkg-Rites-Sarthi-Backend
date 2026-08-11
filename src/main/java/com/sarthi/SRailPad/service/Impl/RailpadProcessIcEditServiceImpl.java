package com.sarthi.SRailPad.service.Impl;

import com.sarthi.SRailPad.dto.RailpadProcessIcEditDTO;
import com.sarthi.SRailPad.entity.inspectionCall.RailpadProcessIcEdit;
import com.sarthi.SRailPad.repository.inspectionCall.RailpadProcessIcEditRepository;
import com.sarthi.SRailPad.service.RailpadProcessIcEditService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RailpadProcessIcEditServiceImpl implements RailpadProcessIcEditService {

    private final RailpadProcessIcEditRepository repository;

    @Override
    public RailpadProcessIcEditDTO getByIcNumber(String icNumber) {
        return repository.findByIcNumber(icNumber)
                .map(this::mapToDto)
                .orElse(null);
    }

    @Override
    @Transactional
    public RailpadProcessIcEditDTO saveOrUpdate(RailpadProcessIcEditDTO dto) {
        RailpadProcessIcEdit entity = repository.findByIcNumber(dto.getIcNumber())
                .orElse(new RailpadProcessIcEdit());
        mapToEntity(dto, entity);
        return mapToDto(repository.save(entity));
    }

    private void mapToEntity(RailpadProcessIcEditDTO dto, RailpadProcessIcEdit entity) {
        entity.setIcNumber(dto.getIcNumber());
        entity.setBookNo(dto.getBookNo());
        entity.setSetNo(dto.getSetNo());
        entity.setInstallmentNo(dto.getInstallmentNo());
        entity.setOfferedInstNo(dto.getOfferedInstNo());
        entity.setPassedInstNo(dto.getPassedInstNo());
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

    private RailpadProcessIcEditDTO mapToDto(RailpadProcessIcEdit entity) {
        return RailpadProcessIcEditDTO.builder()
                .icNumber(entity.getIcNumber())
                .bookNo(entity.getBookNo())
                .setNo(entity.getSetNo())
                .installmentNo(entity.getInstallmentNo())
                .offeredInstNo(entity.getOfferedInstNo())
                .passedInstNo(entity.getPassedInstNo())
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
