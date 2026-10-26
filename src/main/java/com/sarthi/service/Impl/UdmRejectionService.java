package com.sarthi.service.Impl;

import com.sarthi.dto.crisDtos.UdmRejectionDTO;
import com.sarthi.entity.UdmRejection;
import com.sarthi.repository.UdmRejectionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class UdmRejectionService {

    @Autowired
    private UdmRejectionRepository repository;

    public List<UdmRejectionDTO> getConsigneeRejections(String rejDate, String poNo,
                                                         String vendorCode, String type) {
        LocalDate date = parseDate(rejDate);

        List<UdmRejection> records = repository.findByFetchedDate(date);

        return records.stream()
                .filter(r -> poNo == null || poNo.isBlank() ||
                        (r.getPoNumber() != null && r.getPoNumber().contains(poNo)))
                .filter(r -> vendorCode == null || vendorCode.isBlank() ||
                        (r.getVendorCode() != null && r.getVendorCode().contains(vendorCode)))
                .filter(r -> type == null || type.isBlank() ||
                        type.equalsIgnoreCase(r.getTypeOfRejection()))
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    private LocalDate parseDate(String rejDate) {
        if (rejDate != null && !rejDate.isBlank()) {
            try {
                return LocalDate.parse(rejDate.trim(), DateTimeFormatter.ISO_LOCAL_DATE);
            } catch (Exception ignored) {}
        }
        return LocalDate.now().minusDays(1);
    }

    private UdmRejectionDTO toDto(UdmRejection e) {
        UdmRejectionDTO dto = new UdmRejectionDTO();
        dto.setPoNumber(e.getPoNumber());
        dto.setPoDate(e.getPoDate() != null ? e.getPoDate().toString() : null);
        dto.setPlNumber(e.getPlNumber());
        dto.setPoSerialNumber(e.getPoSerialNumber());
        dto.setRejectionNumber(e.getRejectionNumber());
        dto.setRejectionDate(e.getRejectionDate() != null ? e.getRejectionDate().toString() : null);
        dto.setTypeOfRejection(e.getTypeOfRejection());
        dto.setRejectedQty(e.getRejectedQty() != null ? e.getRejectedQty().toPlainString() : null);
        dto.setUom(e.getUom());
        dto.setReasonForRejection(e.getReasonForRejection());
        dto.setRejectionValue(e.getRejectionValue() != null ? e.getRejectionValue().toPlainString() : null);
        dto.setStatus(e.getStatus());
        dto.setItemDescription(e.getItemDescription());
        dto.setIcNumber(e.getIcNumber());
        dto.setIcDate(e.getIcDate() != null ? e.getIcDate().toString() : null);
        dto.setInspectionAgency(e.getInspectionAgency());
        dto.setVendorCode(e.getVendorCode());
        dto.setVendorName(e.getVendorName());
        dto.setConsignee(e.getConsignee());
        dto.setMaterialReceivedDate(e.getMaterialReceivedDate() != null ? e.getMaterialReceivedDate().toString() : null);
        dto.setSource(e.getSource());
        dto.setPdfPath(e.getPdfPath());
        return dto;
    }
}
