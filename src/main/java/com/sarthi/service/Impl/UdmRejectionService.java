package com.sarthi.service.Impl;

import com.sarthi.dto.crisDtos.UdmRejectionDTO;
import com.sarthi.entity.UdmRejection;
import com.sarthi.repository.UdmRejectionRepository;
import com.sarthi.udmcomplaint.UdmComplaintConstants;
import com.sarthi.udmcomplaint.entity.UdmComplaint;
import com.sarthi.udmcomplaint.repository.UdmComplaintRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class UdmRejectionService {

    @Autowired
    private UdmRejectionRepository repository;

    @Autowired
    private UdmComplaintRepository udmComplaintRepository;

    /**
     * Same as {@link #getConsigneeRejections(String, String, String, String)} plus the SARTHI complaint
     * state of each row, and an optional tab filter ("pending" = not closed, "closed" = closed).
     * Complaint state is loaded with one bulk query (no N+1).
     */
    public List<UdmRejectionDTO> getConsigneeRejectionsWithComplaint(String rejDate, String poNo,
                                                                     String vendorCode, String type, String tab) {
        List<UdmRejectionDTO> list = getConsigneeRejections(rejDate, poNo, vendorCode, type);
        List<Long> ids = list.stream().map(UdmRejectionDTO::getId).filter(Objects::nonNull).toList();
        Map<Long, UdmComplaint> byRejection = ids.isEmpty() ? Map.of()
                : udmComplaintRepository.findByUdmRejectionIdIn(ids).stream()
                    .collect(Collectors.toMap(UdmComplaint::getUdmRejectionId, Function.identity(), (a, b) -> a));

        for (UdmRejectionDTO dto : list) {
            UdmComplaint c = dto.getId() != null ? byRejection.get(dto.getId()) : null;
            if (c == null) {
                dto.setComplaintStatus(UdmComplaintConstants.ST_PENDING_CC);
                dto.setPendingWith(UdmComplaintConstants.ROLE_COMPLAINT_CELL);
            } else {
                dto.setComplaintStatus(c.getStatus());
                dto.setPendingWith(c.getPendingWithRoleName());
                dto.setAssignedRio(c.getAssignedRio());
                dto.setAssignedCmUserId(c.getAssignedCmUserId());
                dto.setAssignedCmName(c.getAssignedCmName());
                dto.setComplaintRemarks(c.getLastRemarks());
            }
        }

        if (tab == null || tab.isBlank()) return list;
        boolean wantClosed = "closed".equalsIgnoreCase(tab.trim());
        return list.stream()
                .filter(d -> UdmComplaintConstants.ST_CLOSED.equals(d.getComplaintStatus()) == wantClosed)
                .collect(Collectors.toList());
    }

    public List<UdmRejectionDTO> getConsigneeRejections(String rejDate, String poNo,
                                                         String vendorCode, String type) {
        List<UdmRejection> records;
        if (rejDate != null && !rejDate.isBlank()) {
            LocalDate date = LocalDate.parse(rejDate.trim(), DateTimeFormatter.ISO_LOCAL_DATE);
            records = repository.findByFetchedDate(date);
        } else {
            records = repository.findAll(
                Sort.by(Sort.Direction.DESC, "fetchedDate", "rejectionDate"));
        }

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

    private UdmRejectionDTO toDto(UdmRejection e) {
        UdmRejectionDTO dto = new UdmRejectionDTO();
        dto.setId(e.getId());
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
