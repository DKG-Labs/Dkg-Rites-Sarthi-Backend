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

    public List<VendorPoHeaderResponseDto> getPoListByVendorCode(String vendorCode , String vendorType) {
        logger.info("[DB Debug] Entering getPoListByVendorCode for vendor: {}, type: {}", vendorCode, vendorType);
        // Debug: Print all poNo in DB
        try {
            long count = railInspectionCallRepository.count();
            logger.info("[DB Debug] Total Records in RailInspectionCall: {}", count);
            if (count > 0) {
                railInspectionCallRepository.findAll().forEach(c -> 
                    logger.info("[DB Debug] Call ID: {}, poNo: '{}', Qty: {}", c.getId(), c.getPoNo(), c.getTotalQty())
                );
            }
        } catch (Exception e) {
            logger.error("[DB Debug] Error accessing railInspectionCallRepository", e);
        }

       //  List<PoHeader> poHeaders = poHeaderRepository.findByVendorCode(vendorCode);
      //  List<PoHeader> poHeaders = poHeaderRepository.findAllByVendorCodeWithItems(vendorCode);
       String type = null;
        if(vendorType.equalsIgnoreCase("ERC")){
            type = "Elastic Rail Clips";
        }else if(vendorType.equalsIgnoreCase("Sleeper")){
            type = "PSC Mainline Sleeper";
        }else if(vendorType.equalsIgnoreCase("Rail Pads")){
            type = "Rail Pads";
        }
        List<PoHeader> poHeaders =
                poHeaderRepository.findAllByVendorCodeAndItemCatDescrWithItems(vendorCode, type);

        // Fetch all inspection calls for this vendor once to optimize
        List<com.sarthi.SRailPad.entity.inspectionCall.RailInspectionCall> vendorCalls = railInspectionCallRepository.findAllByVendorCode(vendorCode);
        logger.info("[DB Debug] Found {} total inspection calls for vendor {}", vendorCalls.size(), vendorCode);

        return poHeaders.stream().map(po -> mapToHeaderDto(po, vendorCalls)).toList();
    }

    private VendorPoHeaderResponseDto mapToHeaderDto(PoHeader poHeader, List<com.sarthi.SRailPad.entity.inspectionCall.RailInspectionCall> vendorCalls) {

        VendorPoHeaderResponseDto dto = new VendorPoHeaderResponseDto();

        dto.setPoNo(poHeader.getPoNo());
        dto.setPoDate(
                poHeader.getPoDate() != null
                        ? poHeader.getPoDate().toLocalDate().toString()
                        : null);
        dto.setPoDes(poHeader.getFirmDetails());
        dto.setUnit(poHeader.getItems() != null && !poHeader.getItems().isEmpty() 
                && poHeader.getItems().get(0).getUom() != null 
                ? poHeader.getItems().get(0).getUom() : "Nos");
        dto.setRlyShortName(poHeader.getRlyShortName());
        dto.setRlyCd(poHeader.getRlyCd());
        dto.setItemCategory(poHeader.getItemCatDescr());
        dto.setStatus(poHeader.getPoStatus());
        dto.setPdfPath(poHeader.getPdfPath());

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

    private VendorPoItemsResponseDto mapToItemDto(PoItem item, List<com.sarthi.SRailPad.entity.inspectionCall.RailInspectionCall> vendorCalls) {

        VendorPoItemsResponseDto dto = new VendorPoItemsResponseDto();

        String basePoNo = item.getPoHeader().getPoNo();
        String srNo = item.getItemSrNo();
        String poSrNo = basePoNo + "/" + srNo;
        
        dto.setPoSerialNo(poSrNo);
        dto.setPoDes(item.getItemDesc());
        dto.setConigness(item.getImmsConsigneeName());
        dto.setOrderedQty(BigDecimal.valueOf(item.getQty() != null ? item.getQty() : 0));
        dto.setItemSrNo(srNo);
        
        // Calculate offered qty by matching against vendorCalls
        int totalOffered = vendorCalls.stream()
                .filter(c -> {
                    String cPoNo = c.getPoNo();
                    if (cPoNo == null) return false;
                    
                    // 1. Exact match
                    if (poSrNo.equals(cPoNo)) return true;
                    
                    // 2. Base PO match
                    if (basePoNo.equals(cPoNo)) return true;
                    
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

        return dto;
    }
}
