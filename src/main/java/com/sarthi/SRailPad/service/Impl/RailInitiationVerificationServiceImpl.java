package com.sarthi.SRailPad.service.Impl;

import com.sarthi.SRailPad.dto.RailInitiationVerificationDto;
import com.sarthi.SRailPad.entity.inspectionCall.RailInitiationVerification;
import com.sarthi.SRailPad.repository.RailInitiationVerificationRepository;
import com.sarthi.SRailPad.service.inspectionCall.RailInitiationVerificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class RailInitiationVerificationServiceImpl implements RailInitiationVerificationService {

    @Autowired
    private RailInitiationVerificationRepository repository;

    @Override
    public RailInitiationVerification save(RailInitiationVerificationDto dto) {
        // Upsert: update existing record if present, else create new
        RailInitiationVerification entity = repository.findByCallNo(dto.getCallNo())
                .orElse(new RailInitiationVerification());

        // ---- Section A ----
        entity.setCallNo(dto.getCallNo());
        entity.setRlyPoNo(dto.getRlyPoNo());
        entity.setPoNo(dto.getPoNo());
        entity.setPoDate(dto.getPoDate());
        entity.setPoQty(dto.getPoQty());
        entity.setPoSrQty(dto.getPoSrQty());
        entity.setVendorName(dto.getVendorName());
        entity.setVendorCode(dto.getVendorCode());
        entity.setMaNo(dto.getMaNo());
        entity.setMaDate(dto.getMaDate());
        entity.setPurchasingAuthority(dto.getPurchasingAuthority());
        entity.setBillPayingOfficer(dto.getBillPayingOfficer());
        entity.setSectionAStatus(dto.getSectionAStatus() != null ? dto.getSectionAStatus() : "approved");

        // ---- Section B ----
        entity.setRlyPoNoSerial(dto.getRlyPoNoSerial());
        entity.setItemDesc(dto.getItemDesc());
        entity.setErcType(dto.getErcType());
        entity.setUnit(dto.getUnit());
        entity.setConsignee(dto.getConsignee());
        entity.setOrigDp(dto.getOrigDp());
        entity.setExtDp(dto.getExtDp());
        entity.setCallQty(dto.getCallQty());
        entity.setQtyUnit(dto.getQtyUnit());
        entity.setPlaceOfInspection(dto.getPlaceOfInspection());
        entity.setRemarks(dto.getRemarks());
        entity.setSectionBStatus(dto.getSectionBStatus() != null ? dto.getSectionBStatus() : "approved");

        // ---- Shift Details ----
        entity.setShift(dto.getShift());
        entity.setCompany(dto.getCompany());
        entity.setCastingDate(dto.getCastingDate());
        entity.setProductionUnit(dto.getProductionUnit());

        // ---- Audit ----
        entity.setVerifiedBy(dto.getVerifiedBy());

        return repository.save(entity);
    }

    @Override
    public Optional<RailInitiationVerification> getByCallNo(String callNo) {
        return repository.findByCallNo(callNo);
    }
}
