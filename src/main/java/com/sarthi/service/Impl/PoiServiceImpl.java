package com.sarthi.service.Impl;

import com.sarthi.dto.UnitDetailsDTO;
import com.sarthi.dto.UnitDto;
import com.sarthi.entity.PincodePoIMapping;
import com.sarthi.repository.PincodePoIMappingRepository;
import com.sarthi.service.poiService;
import jakarta.persistence.Access;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
public class PoiServiceImpl implements poiService {
@Autowired
    private PincodePoIMappingRepository pincodePoIMappingRepository;

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
    public List<PincodePoIMapping> getVendorUnits(String vendorCode) {
        if (vendorCode == null || vendorCode.trim().isEmpty()) {
            return Collections.emptyList();
        }
        String cleanVendor = vendorCode.trim();
        List<PincodePoIMapping> list = pincodePoIMappingRepository.findByVendorCodeWithColonHandling(cleanVendor);
        if (list.isEmpty() && cleanVendor.startsWith(":")) {
            list = pincodePoIMappingRepository.findByVendorCodeWithColonHandling(cleanVendor.substring(1));
        }
        return list;
    }
}
