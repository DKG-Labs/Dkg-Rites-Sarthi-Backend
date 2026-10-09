package com.sarthi.service;
import com.sarthi.dto.UnitDetailsDTO;
import com.sarthi.entity.PincodePoIMapping;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public interface poiService {
    List<String> getCompanyList(String vendorCode);

    List<String> getUnitsByCompany(String companyName);

    UnitDetailsDTO getUnitDetails(String companyName, String unitName);

    List<PincodePoIMapping> getVendorUnits(String vendorCode);
}
