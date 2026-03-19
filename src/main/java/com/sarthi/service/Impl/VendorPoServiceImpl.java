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

@Service
public class VendorPoServiceImpl implements VendorPoService {

    @Autowired
    private PoHeaderRepository poHeaderRepository;

    public List<VendorPoHeaderResponseDto> getPoListByVendorCode(String vendorCode , String vendorType) {

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


        return poHeaders.stream().map(this::mapToHeaderDto).toList();
    }

    private VendorPoHeaderResponseDto mapToHeaderDto(PoHeader poHeader) {

        VendorPoHeaderResponseDto dto = new VendorPoHeaderResponseDto();

        dto.setPoNo(poHeader.getPoNo());
        dto.setPoDate(
                poHeader.getPoDate() != null
                        ? poHeader.getPoDate().toLocalDate().toString()
                        : null);
        dto.setPoDes(poHeader.getFirmDetails());
        dto.setUnit("Nos");
        dto.setRlyShortName(poHeader.getRlyShortName());
        dto.setRlyCd(poHeader.getRlyCd());
        dto.setItemCategory(poHeader.getItemCatDescr());
        dto.setStatus(poHeader.getPoStatus());

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
                .map(this::mapToItemDto)
                .toList();

        dto.setPoItem(itemDtos);

        return dto;
    }

    private VendorPoItemsResponseDto mapToItemDto(PoItem item) {

        VendorPoItemsResponseDto dto = new VendorPoItemsResponseDto();

        dto.setPoSerialNo(item.getPoHeader().getPoNo() + "/" + item.getItemSrNo());
        dto.setPoDes(item.getItemDesc());
        dto.setConigness(item.getImmsConsigneeName());
        dto.setOrderedQty(BigDecimal.valueOf(item.getQty() != null ? item.getQty() : 0));
        
        dto.setItemSrNo(item.getItemSrNo());
        dto.setOfferedTillNow(BigDecimal.ZERO);
        dto.setAcceptedTillNow(BigDecimal.ZERO);
        dto.setDue(BigDecimal.valueOf(item.getQty() != null ? item.getQty() : 0));

        dto.setDeliveryPeriod(
                item.getDeliveryDate() != null
                        ? item.getDeliveryDate().toLocalDate().toString()
                        : null);

        return dto;
    }
}
