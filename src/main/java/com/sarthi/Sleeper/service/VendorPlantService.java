package com.sarthi.Sleeper.service;

import com.sarthi.Sleeper.dto.RlyProjection;
import com.sarthi.Sleeper.dto.VendorResponseDTO;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface VendorPlantService {

    public VendorResponseDTO getPlantsByVendorCode(String vendorCode);

    public VendorResponseDTO getPlantsByVendorCodeAndUser(String vendorCode, Integer userId);

    public List<RlyProjection> getUniqueRlyList();
}
