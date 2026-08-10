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
            } else if (vt.equalsIgnoreCase("Rail Pads") || vt.equalsIgnoreCase("RailPad") || vt.equalsIgnoreCase("Rail Pad") || vt.equalsIgnoreCase("RailPads")) {
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

        // Fetch all inspection calls for this vendor once to optimize
        List<com.sarthi.SRailPad.entity.inspectionCall.RailInspectionCall> vendorCalls = List.of();
        try {
            if (railInspectionCallRepository != null && vendorCode != null && !vendorCode.trim().isEmpty()) {
                vendorCalls = railInspectionCallRepository.findAllByVendorCode(vendorCode);
                if ((vendorCalls == null || vendorCalls.isEmpty())) {
                    String altCode = vendorCode.startsWith(":") ? vendorCode.substring(1) : ":" + vendorCode;
                    vendorCalls = railInspectionCallRepository.findAllByVendorCode(altCode);
                }
            }
            if (vendorCalls == null) {
                vendorCalls = List.of();
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

        // Calculate offered qty by matching against vendorCalls
        int totalOffered = vendorCalls.stream()
                .filter(c -> {
                    String cPoNo = c.getPoNo();
                    String callNo = c.getCallNo();

                    if (cPoNo == null || callNo == null || !callNo.startsWith("RPF"))
                        return false;

                    // 1. Exact match
                    if (poSrNo.equals(cPoNo))
                        return true;

                    // 2. Base PO match
                    if (basePoNo.equals(cPoNo))
                        return true;

                    // 3. Flexible SR match (handle 01 vs 001)
                    if (cPoNo.startsWith(basePoNo + "/")) {
                        String storedSr = cPoNo.substring(basePoNo.length() + 1);
                        try {
                            return Integer.parseInt(storedSr) == Integer.parseInt(srNo);
                        } catch (NumberFormatException e) {
                            return storedSr.equals(srNo);
                        }
                    }

                    return false;
                })
                .mapToInt(c -> c.getTotalQty() != null ? c.getTotalQty() : 0)
                .sum();

        BigDecimal offeredQty = BigDecimal.valueOf(totalOffered);
        logger.info("[PO Stats] PO/SR: {}, Offered: {}", poSrNo, offeredQty);

        dto.setOfferedTillNow(offeredQty);
        dto.setAcceptedTillNow(BigDecimal.ZERO);
        dto.setDue(BigDecimal.valueOf(item.getQty() != null ? item.getQty() : 0).subtract(offeredQty));

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
