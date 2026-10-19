package com.sarthi.service.Impl;

import com.sarthi.dto.UnitDetailsDTO;
import com.sarthi.dto.UnitDto;
import com.sarthi.dto.VendorUnitDto;
import com.sarthi.entity.IEFieldsMapping;
import com.sarthi.entity.PincodePoIMapping;
import com.sarthi.repository.IeFieldsMappingRepository;
import com.sarthi.repository.PincodePoIMappingRepository;
import com.sarthi.service.poiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class PoiServiceImpl implements poiService {
    @Autowired
    private PincodePoIMappingRepository pincodePoIMappingRepository;

    @Autowired
    private IeFieldsMappingRepository ieFieldsMappingRepository;

    @Override
    public List<String> getCompanyList(String vendorCode) {
        return pincodePoIMappingRepository.findDistinctCompanyNamesByVendorCode(vendorCode);
    }

    //  Unit dropdown based on company
    @Override
    public List<String> getUnitsByCompany(String companyName) {
        return pincodePoIMappingRepository.findUnitsByCompany(companyName)
                .stream()
                .map(UnitDto::getUnitName)
                .toList();
    }

    // 3️ Auto-fill address + get poiCode
    @Override
    public UnitDetailsDTO getUnitDetails(String companyName, String unitName) {
        if (companyName != null && !companyName.trim().isEmpty() && unitName != null && !unitName.trim().isEmpty()) {
            Optional<UnitDetailsDTO> details = pincodePoIMappingRepository.findUnitDetails(companyName.trim(), unitName.trim());
            if (details.isPresent()) {
                return details.get();
            }
        }
        if (unitName != null && !unitName.trim().isEmpty()) {
            List<UnitDetailsDTO> byUnit = pincodePoIMappingRepository.findUnitDetailsByUnitNameOnly(unitName.trim());
            if (!byUnit.isEmpty()) {
                return byUnit.get(0);
            }
        }
        return new UnitDetailsDTO("", "", "");
    }

    @Override
    public List<VendorUnitDto> getVendorUnits(String vendorCode) {
        if (vendorCode == null || vendorCode.trim().isEmpty()) {
            return Collections.emptyList();
        }
        String cleanVendor = vendorCode.trim();
        List<PincodePoIMapping> list = pincodePoIMappingRepository.findByVendorCodeWithColonHandling(cleanVendor);
        if (list.isEmpty() && cleanVendor.startsWith(":")) {
            list = pincodePoIMappingRepository.findByVendorCodeWithColonHandling(cleanVendor.substring(1));
        }

        // Bulk-fetch RIO for all pinCodes from ie_fields_mapping (product=ERC)
        List<String> pinCodes = list.stream()
                .map(PincodePoIMapping::getPinCode)
                .filter(p -> p != null && !p.isBlank())
                .distinct()
                .collect(Collectors.toList());
        Map<String, String> pinToRio = ieFieldsMappingRepository
                .findByPinCodeInAndProduct(pinCodes, "ERC")
                .stream()
                .collect(Collectors.toMap(
                        IEFieldsMapping::getPinCode,
                        IEFieldsMapping::getRio,
                        (a, b) -> a));

        return list.stream().map(p -> {
            VendorUnitDto dto = new VendorUnitDto();
            dto.setId(p.getId());
            dto.setPinCode(p.getPinCode());
            dto.setCompanyName(p.getCompanyName());
            dto.setCin(p.getCin());
            dto.setUnitName(p.getUnitName());
            dto.setAddress(p.getAddress());
            dto.setDistrict(p.getDistrict());
            dto.setState(p.getState());
            dto.setStatus(p.getStatus());
            dto.setPoiCode(p.getPoiCode());
            dto.setVendorCode(p.getVendorCode());
            dto.setContactPerson(p.getContactPerson());
            dto.setContactPersonNumber(p.getContactPersonNumber());
            dto.setRio(p.getPinCode() != null ? pinToRio.get(p.getPinCode()) : null);
            return dto;
        }).collect(Collectors.toList());
    }
}
