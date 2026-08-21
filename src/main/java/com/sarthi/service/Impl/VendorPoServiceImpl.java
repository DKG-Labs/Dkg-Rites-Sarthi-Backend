package com.sarthi.service.Impl;

import com.sarthi.dto.vendorDtos.VendorPoHeaderResponseDto;
import com.sarthi.dto.vendorDtos.VendorPoItemsResponseDto;
import com.sarthi.entity.PoHeader;
import com.sarthi.entity.PoItem;
import com.sarthi.repository.PoHeaderRepository;
import com.sarthi.service.VendorPoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class VendorPoServiceImpl implements VendorPoService {

    private static final Logger logger = LoggerFactory.getLogger(VendorPoServiceImpl.class);

    @Autowired
    private PoHeaderRepository poHeaderRepository;

    @Autowired
    private com.sarthi.repository.PoMaDetailRepository poMaDetailRepository;

    @Autowired
    private com.sarthi.repository.PoItemRepository poItemRepository;

    @Autowired
    private com.sarthi.SRailPad.repository.inspectionCall.RailInspectionCallRepository railInspectionCallRepository;

    public List<VendorPoHeaderResponseDto> getPoListByVendorCode(String vendorCode, String vendorType) {
        logger.info("[DB Debug] Entering getPoListByVendorCode for vendor: {}, type: {}", vendorCode, vendorType);

        String type = null;
        if (vendorType != null && !vendorType.trim().isEmpty()) {
            String vt = vendorType.trim();
            if (vt.equalsIgnoreCase("ERC") || vt.equalsIgnoreCase("Elastic Rail Clips")) {
                type = "Elastic Rail Clips";
            } else if (vt.equalsIgnoreCase("Sleeper") || vt.equalsIgnoreCase("PSC Mainline Sleeper")) {
                type = "PSC Mainline Sleeper";
            } else if (vt.equalsIgnoreCase("Rail Pads") || vt.equalsIgnoreCase("RailPad")
                    || vt.equalsIgnoreCase("Rail Pad") || vt.equalsIgnoreCase("RailPads")) {
                type = "Rail Pads";
            } else {
                type = vt;
            }
        }

        List<PoHeader> poHeaders = List.of();
        if (vendorCode != null && !vendorCode.trim().isEmpty()) {
            if (type != null && !type.trim().isEmpty()) {
                poHeaders = poHeaderRepository.findAllByVendorCodeAndItemCatDescrWithItems(vendorCode, type);
                if (poHeaders.isEmpty()) {
                    String altCode = vendorCode.startsWith(":") ? vendorCode.substring(1) : ":" + vendorCode;
                    poHeaders = poHeaderRepository.findAllByVendorCodeAndItemCatDescrWithItems(altCode, type);
                }
            } else {
                poHeaders = poHeaderRepository.findAllByVendorCodeWithItems(vendorCode);
                if (poHeaders.isEmpty()) {
                    String altCode = vendorCode.startsWith(":") ? vendorCode.substring(1) : ":" + vendorCode;
                    poHeaders = poHeaderRepository.findAllByVendorCodeWithItems(altCode);
                }
            }
        }

        // Fetch all inspection calls for this vendor (combining both with & without colon variants)
        List<com.sarthi.SRailPad.entity.inspectionCall.RailInspectionCall> vendorCalls = new java.util.ArrayList<>();
        try {
            if (railInspectionCallRepository != null && vendorCode != null && !vendorCode.trim().isEmpty()) {
                List<com.sarthi.SRailPad.entity.inspectionCall.RailInspectionCall> primaryCalls = railInspectionCallRepository.findAllByVendorCode(vendorCode);
                if (primaryCalls != null && !primaryCalls.isEmpty()) {
                    vendorCalls.addAll(primaryCalls);
                }
                String altCode = vendorCode.startsWith(":") ? vendorCode.substring(1) : ":" + vendorCode;
                List<com.sarthi.SRailPad.entity.inspectionCall.RailInspectionCall> altCalls = railInspectionCallRepository.findAllByVendorCode(altCode);
                if (altCalls != null && !altCalls.isEmpty()) {
                    for (com.sarthi.SRailPad.entity.inspectionCall.RailInspectionCall ac : altCalls) {
                        if (vendorCalls.stream().noneMatch(c -> c.getId() != null && c.getId().equals(ac.getId()))) {
                            vendorCalls.add(ac);
                        }
                    }
                }
            }
        } catch (Exception e) {
            logger.warn("Error fetching rail inspection calls for vendor {}: {}", vendorCode, e.getMessage());
            vendorCalls = List.of();
        }
        logger.info("[DB Debug] Found {} total inspection calls for vendor {}", vendorCalls.size(), vendorCode);

        final List<com.sarthi.SRailPad.entity.inspectionCall.RailInspectionCall> finalVendorCalls = vendorCalls;
        return poHeaders.stream().map(po -> mapToHeaderDto(po, finalVendorCalls)).toList();
    }

    public String getPdfPathByRawPoNo(String rawPoNo) {
        if (rawPoNo == null || rawPoNo.isEmpty())
            return null;
        return poHeaderRepository.findByPoNo(rawPoNo)
                .map(com.sarthi.entity.PoHeader::getPdfPath)
                .orElse(null);
    }

    private VendorPoHeaderResponseDto mapToHeaderDto(PoHeader poHeader,
            List<com.sarthi.SRailPad.entity.inspectionCall.RailInspectionCall> vendorCalls) {

        VendorPoHeaderResponseDto dto = new VendorPoHeaderResponseDto();

        dto.setPoNo(poHeader.getPoNo());
        dto.setPoDate(
                poHeader.getPoDate() != null
                        ? poHeader.getPoDate()
                                .atZone(java.time.ZoneId.systemDefault())      // treat as IST (JVM timezone)
                                .withZoneSameInstant(java.time.ZoneOffset.UTC) // convert back to UTC
                                .toLocalDate()
                                .toString()                                     // e.g. "2025-10-15"
                        : null);
        dto.setPoDes(poHeader.getFirmDetails());
        dto.setUnit(poHeader.getItems() != null && !poHeader.getItems().isEmpty()
                && poHeader.getItems().get(0).getUom() != null
                        ? poHeader.getItems().get(0).getUom()
                        : "Nos");
        dto.setRlyShortName(poHeader.getRlyShortName());
        dto.setRlyCd(poHeader.getRlyCd());
        dto.setItemCategory(poHeader.getItemCatDescr());
        dto.setStatus(poHeader.getPoStatus());
        dto.setPdfPath(poHeader.getPdfPath());
        dto.setCaseNo(poHeader.getCaseNo());
        dto.setPoKey(poHeader.getPoKey());

        BigDecimal totalQty = poHeader.getItems().stream()
                .map(item -> BigDecimal.valueOf(item.getQty() != null ? item.getQty() : 0))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        dto.setQty(totalQty);

        BigDecimal totalVal = poHeader.getItems().stream()
                .map(item -> item.getValue() != null ? item.getValue() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        dto.setTotalValue(totalVal);

        List<VendorPoItemsResponseDto> itemDtos = poHeader.getItems()
                .stream()
                .filter(item -> !(item.getItemSrNo() == null && item.getItemDesc() == null && (item.getQty() == null || item.getQty() == 0)))
                .map(item -> mapToItemDto(item, vendorCalls))
                .toList();

        dto.setPoItem(itemDtos);

        return dto;
    }

    private VendorPoItemsResponseDto mapToItemDto(PoItem item,
            List<com.sarthi.SRailPad.entity.inspectionCall.RailInspectionCall> vendorCalls) {

        VendorPoItemsResponseDto dto = new VendorPoItemsResponseDto();

        String basePoNo = item.getPoHeader() != null ? item.getPoHeader().getPoNo() : "";
        String srNo = item.getItemSrNo();
        String poSrNo = (srNo != null && !srNo.trim().isEmpty() && !srNo.equalsIgnoreCase("null"))
                ? basePoNo + "/" + srNo.trim()
                : basePoNo;

        dto.setPoSerialNo(poSrNo);
        dto.setPoDes(item.getItemDesc());
        dto.setConigness(item.getImmsConsigneeName());
        dto.setOrderedQty(BigDecimal.valueOf(item.getQty() != null ? item.getQty() : 0));
        dto.setItemSrNo(srNo);

        String itemUom = item.getUom();
        if (itemUom == null || itemUom.trim().isEmpty()) {
            if (item.getPoHeader() != null && item.getPoHeader().getItems() != null
                    && !item.getPoHeader().getItems().isEmpty()) {
                itemUom = item.getPoHeader().getItems().get(0).getUom();
            }
        }
        if (itemUom == null || itemUom.trim().isEmpty()) {
            itemUom = "Nos.";
        }
        dto.setUom(itemUom);
        dto.setUnit(itemUom);

        // Calculate offered qty by strictly matching specific PO item serial number and
        // excluding withdrawn calls
        final boolean isSetUom = itemUom != null && itemUom.toUpperCase().contains("SET");

        int totalOffered = vendorCalls.stream()
                .filter(c -> {
                    String cPoNo = c.getPoNo();
                    String callNo = c.getCallNo();

                    if (cPoNo == null || callNo == null || !callNo.startsWith("RPF"))
                        return false;

                    // Exclude WITHDRAWN or CANCELLED calls
                    String status = c.getStatus() != null ? c.getStatus().toUpperCase() : "";
                    if (status.contains("WITHDRAW") || status.contains("CANCEL"))
                        return false;

                    // Verify Base PO number
                    String cBasePo = cPoNo.contains("/") ? cPoNo.split("/")[0].trim() : cPoNo.trim();
                    if (!cBasePo.equalsIgnoreCase(basePoNo.trim()))
                        return false;

                    // Match SR Number
                    String cPoSr = c.getPoSr();
                    if (cPoSr != null && !cPoSr.trim().isEmpty()) {
                        try {
                            return Integer.parseInt(cPoSr.trim()) == Integer.parseInt(srNo.trim());
                        } catch (NumberFormatException e) {
                            return cPoSr.trim().equalsIgnoreCase(srNo.trim());
                        }
                    } else if (cPoNo.contains("/")) {
                        String storedSr = cPoNo.substring(cPoNo.indexOf("/") + 1).trim();
                        try {
                            return Integer.parseInt(storedSr) == Integer.parseInt(srNo.trim());
                        } catch (NumberFormatException e) {
                            return storedSr.equalsIgnoreCase(srNo.trim());
                        }
                    }

                    return false;
                })
                .mapToInt(c -> {
                    if (isSetUom && c.getNoOfSets() != null && c.getNoOfSets() > 0) {
                        return c.getNoOfSets();
                    }
                    return c.getTotalQty() != null ? c.getTotalQty() : 0;
                })
                .sum();

        BigDecimal offeredQty = BigDecimal.valueOf(totalOffered);
        logger.info("[PO Stats] PO/SR: {}, Offered: {}", poSrNo, offeredQty);

        dto.setOfferedTillNow(offeredQty);
        dto.setAcceptedTillNow(BigDecimal.ZERO);
        int itemQtyVal = item.getQty() != null ? item.getQty() : 0;
        int dueVal = Math.max(0, itemQtyVal - totalOffered);
        dto.setDue(BigDecimal.valueOf(dueVal));

        LocalDate effectiveOdp = resolveEffectiveOriginalDeliveryDate(item);
        LocalDate effectiveEdp = resolveEffectiveExtendedDeliveryDate(item);

        dto.setDeliveryPeriod(effectiveOdp != null ? effectiveOdp.toString() : null);
        dto.setDeliveryDate(effectiveOdp != null ? effectiveOdp.toString() : null);

        dto.setExtendedDeliveryPeriod(effectiveEdp != null ? effectiveEdp.toString() : null);
        dto.setExtendedDeliveryDate(effectiveEdp != null ? effectiveEdp.toString() : null);

        return dto;
    }

    private LocalDate getUtcDate(LocalDateTime dt) {
        if (dt == null) return null;
        return dt.atZone(java.time.ZoneId.systemDefault())
                 .withZoneSameInstant(java.time.ZoneOffset.UTC)
                 .toLocalDate();
    }

    private LocalDate resolveEffectiveOriginalDeliveryDate(PoItem item) {
        LocalDate currentOdp = getUtcDate(item.getDeliveryDate());
        if (currentOdp != null) {
            return currentOdp;
        }

        String poNo = item.getPoHeader() != null ? item.getPoHeader().getPoNo() : null;
        String poKey = item.getPoHeader() != null ? item.getPoHeader().getPoKey() : null;

        List<com.sarthi.entity.CricsPos.PoMaDetail> maDetails = List.of();
        if (poNo != null && !poNo.isBlank()) {
            maDetails = poMaDetailRepository.findByMaPoHeaderPoNo(poNo);
        }
        if ((maDetails == null || maDetails.isEmpty()) && poKey != null && !poKey.isBlank()) {
            maDetails = poMaDetailRepository.findByMaPoHeaderPoKey(poKey);
        }

        if (maDetails != null && !maDetails.isEmpty()) {
            String srNo = item.getItemSrNo();
            for (com.sarthi.entity.CricsPos.PoMaDetail d : maDetails) {
                if (isPoSrMatch(d.getPoSr(), srNo)) {
                    LocalDate parsedDate = null;
                    if (d.getOrigDp() != null && !d.getOrigDp().isBlank()) {
                        parsedDate = parseDateFromMaValue(d.getOrigDp());
                    }
                    if (parsedDate == null && d.getOldValue() != null && !d.getOldValue().isBlank() && isDpField(d.getMaFld(), d.getMaFldDescr())) {
                        parsedDate = parseDateFromMaValue(d.getOldValue());
                    }
                    if (parsedDate != null) {
                        try {
                            item.setDeliveryDate(parsedDate.atStartOfDay());
                            poItemRepository.save(item);
                            logger.info("[MA ODP Update] Updated item {}/{} ODP to {} from po_ma_detail", poNo, srNo, parsedDate);
                        } catch (Exception e) {
                            logger.warn("Could not persist updated ODP for item {}: {}", srNo, e.getMessage());
                        }
                        return parsedDate;
                    }
                }
            }
        }
        return null;
    }

    private LocalDate resolveEffectiveExtendedDeliveryDate(PoItem item) {
        LocalDate currentEdp = getUtcDate(item.getExtendedDeliveryDate());
        LocalDate now = LocalDate.now();

        boolean isNullOrExpired = (currentEdp == null) || currentEdp.isBefore(now);

        if (isNullOrExpired) {
            String poNo = item.getPoHeader() != null ? item.getPoHeader().getPoNo() : null;
            String poKey = item.getPoHeader() != null ? item.getPoHeader().getPoKey() : null;

            List<com.sarthi.entity.CricsPos.PoMaDetail> maDetails = List.of();
            if (poNo != null && !poNo.isBlank()) {
                maDetails = poMaDetailRepository.findByMaPoHeaderPoNo(poNo);
            }
            if ((maDetails == null || maDetails.isEmpty()) && poKey != null && !poKey.isBlank()) {
                maDetails = poMaDetailRepository.findByMaPoHeaderPoKey(poKey);
            }

            if (maDetails != null && !maDetails.isEmpty()) {
                String srNo = item.getItemSrNo();
                LocalDate latestMaEdpDate = null;

                for (com.sarthi.entity.CricsPos.PoMaDetail d : maDetails) {
                    if (isDpField(d.getMaFld(), d.getMaFldDescr()) && isPoSrMatch(d.getPoSr(), srNo)) {
                        LocalDate parsedDate = parseDateFromMaValue(d.getNewValue());
                        if (parsedDate != null) {
                            if (latestMaEdpDate == null || parsedDate.isAfter(latestMaEdpDate)) {
                                latestMaEdpDate = parsedDate;
                            }
                        }
                    }
                }

                if (latestMaEdpDate != null) {
                    try {
                        item.setExtendedDeliveryDate(latestMaEdpDate.atStartOfDay());
                        poItemRepository.save(item);
                        logger.info("[MA EDP Update] Updated item {}/{} EDP to {} from po_ma_detail", poNo, srNo, latestMaEdpDate);
                    } catch (Exception e) {
                        logger.warn("Could not persist updated EDP for item {}: {}", srNo, e.getMessage());
                    }
                    return latestMaEdpDate;
                }
            }
        }
        return currentEdp;
    }

    private boolean isDpField(String maFld, String maFldDescr) {
        if (maFld != null && (maFld.equalsIgnoreCase("DP") || maFld.equalsIgnoreCase("DELV_DT") || maFld.equalsIgnoreCase("EXT_DELV_DT"))) {
            return true;
        }
        if (maFldDescr != null) {
            String descrLower = maFldDescr.toLowerCase();
            return descrLower.contains("delivery period") || descrLower.contains("delivery date") || descrLower.contains("dp");
        }
        return false;
    }

    private boolean isPoSrMatch(String maPoSr, String itemSrNo) {
        if (maPoSr == null || itemSrNo == null) return false;
        String cleanMa = maPoSr.trim();
        String cleanItem = itemSrNo.trim();
        if (cleanMa.equalsIgnoreCase(cleanItem)) return true;
        try {
            return Integer.parseInt(cleanMa) == Integer.parseInt(cleanItem);
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public static LocalDate parseDateFromMaValue(String rawValue) {
        if (rawValue == null || rawValue.trim().isEmpty()) {
            return null;
        }
        String val = rawValue.trim();

        if (val.contains("(")) {
            val = val.substring(0, val.indexOf("(")).trim();
        }

        java.util.regex.Matcher m = java.util.regex.Pattern.compile(
                "(\\d{1,2}[-/][A-Za-z]{3}[-/]\\d{2,4}|\\d{1,2}[-/]\\d{1,2}[-/]\\d{2,4}|\\d{4}[-/]\\d{1,2}[-/]\\d{1,2})")
                .matcher(val);
        if (m.find()) {
            val = m.group(1);
        }

        List<java.time.format.DateTimeFormatter> formatters = List.of(
                new java.time.format.DateTimeFormatterBuilder().parseCaseInsensitive().appendPattern("dd-MMM-yy").toFormatter(java.util.Locale.ENGLISH),
                new java.time.format.DateTimeFormatterBuilder().parseCaseInsensitive().appendPattern("dd-MMM-yyyy").toFormatter(java.util.Locale.ENGLISH),
                new java.time.format.DateTimeFormatterBuilder().parseCaseInsensitive().appendPattern("d-MMM-yy").toFormatter(java.util.Locale.ENGLISH),
                new java.time.format.DateTimeFormatterBuilder().parseCaseInsensitive().appendPattern("d-MMM-yyyy").toFormatter(java.util.Locale.ENGLISH),
                java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy"),
                java.time.format.DateTimeFormatter.ofPattern("dd-MM-yyyy"),
                java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd"),
                java.time.format.DateTimeFormatter.ofPattern("dd/MM/yy"),
                java.time.format.DateTimeFormatter.ofPattern("dd-MM-yy")
        );

        for (java.time.format.DateTimeFormatter fmt : formatters) {
            try {
                return LocalDate.parse(val, fmt);
            } catch (Exception ignored) {
            }
        }
        return null;
    }
}
