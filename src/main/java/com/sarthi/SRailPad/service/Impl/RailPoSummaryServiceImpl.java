package com.sarthi.SRailPad.service.Impl;

import com.sarthi.SRailPad.dto.RailPoSummaryDto;
import com.sarthi.SRailPad.service.inspectionCall.RailPoSummaryService;
import com.sarthi.entity.PoHeader;
import com.sarthi.entity.PoItem;
import com.sarthi.entity.CricsPos.PoMaHeader;
import com.sarthi.repository.PoHeaderRepository;
import com.sarthi.repository.PoMaHeaderRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

/**
 * Railpad-specific implementation for fetching PO summary data.
 * Uses the shared repository layer but does NOT modify or extend
 * any shared Sleeper service, ensuring zero side-effects.
 */
@Service
public class RailPoSummaryServiceImpl implements RailPoSummaryService {

    private static final Logger log = LoggerFactory.getLogger(RailPoSummaryServiceImpl.class);
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    @Autowired
    private PoHeaderRepository poHeaderRepository;

    @Autowired
    private PoMaHeaderRepository poMaHeaderRepository;

    @Override
    public RailPoSummaryDto getSummaryByPoAndSr(String poNo, String poSrNo) {
        if (poNo == null || poNo.isBlank()) return null;

        // Extract bare PO number if it contains a slash (e.g. "60260074102063/001")
        String barePoNo = poNo.contains("/") ? poNo.split("/")[0] : poNo;

        Optional<PoHeader> headerOpt = poHeaderRepository.findByPoNoWithItems(barePoNo);
        if (headerOpt.isEmpty()) {
            log.warn("[RailPad] PO header not found for poNo={}", barePoNo);
            return null;
        }

        PoHeader header = headerOpt.get();
        List<PoMaHeader> maHeaders = poMaHeaderRepository.findByPoNo(barePoNo);

        return buildDto(header, maHeaders, poSrNo);
    }

    // -------------------------------------------------------------------------
    // Private helpers
    // -------------------------------------------------------------------------

    private RailPoSummaryDto buildDto(PoHeader header, List<PoMaHeader> maHeaders, String targetSrNo) {
        RailPoSummaryDto dto = new RailPoSummaryDto();

        // ---- Section A ----
        String rly = resolveRlyPrefix(header);
        dto.setRlyShortName(rly);
        dto.setPoNo(header.getPoNo());
        dto.setRlyPoNo(rly + "/" + header.getPoNo());           // SER/60260074102063
        dto.setPoDate(formatDt(header.getPoDate()));

        // Vendor details — prefer vendorDetails, fall back to firmDetails
        String vendorRaw = header.getVendorDetails() != null
                ? header.getVendorDetails()
                : header.getFirmDetails();
        dto.setVendorCode(header.getVendorCode());
        dto.setVendorName(extractVendorName(vendorRaw));

        // Purchasing authority
        dto.setPurchasingAuthority(header.getPurchaserDetail());

        // MA details
        if (maHeaders != null && !maHeaders.isEmpty()) {
            PoMaHeader ma = maHeaders.get(0);
            dto.setMaNo(ma.getMaNo());
            dto.setMaDate(ma.getMaDate() != null ? ma.getMaDate().format(DATE_FMT) : "N/A");
        } else {
            dto.setMaNo("N/A");
            dto.setMaDate("N/A");
        }

        // ---- Section B: PO Item ----
        List<PoItem> items = header.getItems();
        if (items != null && !items.isEmpty()) {

            // Match item by serial number
            PoItem matched = findMatchingItem(items, targetSrNo);

            dto.setItemDesc(matched.getItemDesc());
            dto.setConsignee(matched.getImmsConsigneeName());
            dto.setUnit(matched.getUom() != null ? matched.getUom() : "Nos.");
            dto.setPoSrQty(matched.getQty());
            dto.setOrigDp(formatDt(matched.getDeliveryDate()));
            dto.setExtDp(formatDt(matched.getExtendedDeliveryDate()));

            // Bill paying officer from matched item
            dto.setBillPayingOfficer(
                    matched.getBillPayOffDesc() != null ? matched.getBillPayOffDesc()
                            : (matched.getBillPassOff() != null ? matched.getBillPassOff() : "N/A")
            );

            // PO_QTY = qty of the specific serial number being inspected (from po_item.qty)
            // NOT the sum of all items — we want the qty for serial 001, not the whole PO total
            dto.setPoQty(matched.getQty());

            // Build full serial: SER/60260074102063/001
            // Use the actual item_sr_no from DB as the canonical suffix (e.g. "001")
            String srSuffix = matched.getItemSrNo() != null ? matched.getItemSrNo()
                    : (targetSrNo != null ? targetSrNo : "N/A");
            dto.setPoSerialNo(srSuffix);
            dto.setRlyPoNoSerial(dto.getRlyPoNo() + "/" + srSuffix);
        }

        // Place of inspection from firmDetails vendor city
        dto.setPlaceOfInspection(extractCity(vendorRaw));

        return dto;
    }

    /**
     * Find po_item matching the given serial number.
     * Handles leading zeros: "001" matches "1".
     * Falls back to the first item if nothing matches.
     */
    private PoItem findMatchingItem(List<PoItem> items, String targetSrNo) {
        if (targetSrNo == null) return items.get(0);

        // Strip composite prefix e.g. "60260074102063/001" -> "001"
        String srNo = targetSrNo.contains("/")
                ? targetSrNo.substring(targetSrNo.lastIndexOf('/') + 1)
                : targetSrNo;

        return items.stream()
                .filter(item -> {
                    String current = item.getItemSrNo();
                    if (current == null) return false;
                    if (srNo.trim().equalsIgnoreCase(current.trim())) return true;
                    try {
                        return Integer.parseInt(srNo.trim()) == Integer.parseInt(current.trim());
                    } catch (NumberFormatException e) {
                        return false;
                    }
                })
                .findFirst()
                .orElse(items.get(0));   // graceful fallback
    }

    /** Returns short railway name (≤6 chars) or falls back to railway code */
    private String resolveRlyPrefix(PoHeader header) {
        String s = header.getRlyShortName();
        if (s != null && !s.isBlank() && s.length() <= 6) return s;
        return header.getRlyCd() != null ? header.getRlyCd() : "";
    }

    /** Format LocalDateTime to dd/MM/yyyy, returns N/A if null */
    private String formatDt(LocalDateTime dt) {
        if (dt == null) return "N/A";
        return dt.toLocalDate().format(DATE_FMT);
    }

    /**
     * Extract vendor name from "VENDOR NAME-CITY~address~..." pattern
     * or from "VENDOR NAME~..." pattern.
     */
    private String extractVendorName(String raw) {
        if (raw == null || raw.isBlank()) return "N/A";
        String[] parts = raw.split("~");
        // First segment is "NAME-CITY" — strip city
        String segment = parts[0];
        int dashIdx = segment.lastIndexOf('-');
        if (dashIdx > 0) return segment.substring(0, dashIdx).trim();
        return segment.trim();
    }

    /** Extract city from vendor/firm details for place of inspection */
    private String extractCity(String raw) {
        if (raw == null || raw.isBlank()) return "N/A";
        String[] parts = raw.split("~");
        String segment = parts[0];
        int dashIdx = segment.lastIndexOf('-');
        if (dashIdx >= 0 && dashIdx < segment.length() - 1) {
            return segment.substring(dashIdx + 1).trim();
        }
        return "N/A";
    }
}
