package com.sarthi.service.Impl;

import com.sarthi.dto.UnitDetailsDTO;
import com.sarthi.dto.UnitDto;
import com.sarthi.repository.PincodePoIMappingRepository;
import com.sarthi.service.poiService;
import jakarta.persistence.Access;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;

import java.util.List;

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
        return pincodePoIMappingRepository.findUnitDetails(companyName, unitName)
                .orElseThrow(() ->
                        new RuntimeException("Unit not found"));
    }
}
