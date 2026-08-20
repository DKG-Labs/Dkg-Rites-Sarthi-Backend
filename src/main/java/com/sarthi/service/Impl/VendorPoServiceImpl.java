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
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class VendorPoServiceImpl implements VendorPoService {

    private static final Logger logger = LoggerFactory.getLogger(VendorPoServiceImpl.class);

    @Autowired
    private PoHeaderRepository poHeaderRepository;

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
                        ? poHeader.getPoDate().toLocalDate().toString()
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
                .map(item -> mapToItemDto(item, vendorCalls))
                .toList();

        dto.setPoItem(itemDtos);

        return dto;
    }

    private VendorPoItemsResponseDto mapToItemDto(PoItem item,
            List<com.sarthi.SRailPad.entity.inspectionCall.RailInspectionCall> vendorCalls) {

        VendorPoItemsResponseDto dto = new VendorPoItemsResponseDto();

        String basePoNo = item.getPoHeader().getPoNo();
        String srNo = item.getItemSrNo();
        String poSrNo = basePoNo + "/" + srNo;

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

        dto.setDeliveryPeriod(
                item.getDeliveryDate() != null
                        ? item.getDeliveryDate().toLocalDate().toString()
                        : null);
        dto.setExtendedDeliveryPeriod(
                item.getExtendedDeliveryDate() != null
                        ? item.getExtendedDeliveryDate().toLocalDate().toString()
                        : null);

        dto.setDeliveryDate(
                item.getDeliveryDate() != null
                        ? item.getDeliveryDate().toLocalDate().toString()
                        : null);

        dto.setExtendedDeliveryDate(
                item.getExtendedDeliveryDate() != null
                        ? item.getExtendedDeliveryDate().toLocalDate().toString()
                        : null);

        return dto;
    }
}
