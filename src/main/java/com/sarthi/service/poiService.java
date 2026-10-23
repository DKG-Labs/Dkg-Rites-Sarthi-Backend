package com.sarthi.service;
import com.sarthi.dto.UnitDetailsDTO;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public interface poiService {
    List<String> getCompanyList(String vendorCode);

    List<String> getUnitsByCompany(String companyName);

    UnitDetailsDTO getUnitDetails(String companyName, String unitName);

}
