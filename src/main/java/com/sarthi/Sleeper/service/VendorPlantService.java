package com.sarthi.Sleeper.service;

import com.sarthi.Sleeper.dto.VendorResponseDTO;
import org.springframework.stereotype.Service;

@Service
public interface VendorPlantService {

    public VendorResponseDTO getPlantsByVendorCode(String vendorCode);

    public VendorResponseDTO getPlantsByVendorCodeAndUser(String vendorCode, Integer userId);
}
