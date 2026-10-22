package com.sarthi.service;
import com.sarthi.dto.UnitDetailsDTO;
import com.sarthi.dto.VendorUnitDto;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public interface poiService {
    List<String> getCompanyList(String vendorCode);

    List<String> getUnitsByCompany(String companyName);

    UnitDetailsDTO getUnitDetails(String companyName, String unitName);

    List<VendorUnitDto> getVendorUnits(String vendorCode);
}
