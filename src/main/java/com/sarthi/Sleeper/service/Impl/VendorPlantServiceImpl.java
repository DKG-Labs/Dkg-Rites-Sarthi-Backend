package com.sarthi.Sleeper.service.Impl;

import com.sarthi.Sleeper.dto.PlantDTO;
import com.sarthi.Sleeper.dto.VendorResponseDTO;
import com.sarthi.Sleeper.entity.VendorPlant;
import com.sarthi.Sleeper.repository.VendorPlantRepository;
import com.sarthi.Sleeper.service.VendorPlantService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class VendorPlantServiceImpl implements VendorPlantService {
    @Autowired
    private VendorPlantRepository vendorPlantRepository;

    public VendorResponseDTO getPlantsByVendorCode(String vendorCode) {

        List<VendorPlant> list = vendorPlantRepository.findByVendorCode(vendorCode);

        if (list.isEmpty()) {
            throw new RuntimeException("No plants found for vendor: " + vendorCode);
        }

        VendorResponseDTO response = new VendorResponseDTO();
        response.setVendorCode(vendorCode);
        response.setCompanyName(list.get(0).getCompanyName());

        List<PlantDTO> plants = list.stream()
                .map(p -> new PlantDTO(p.getPlantName(), p.getPlantId()))
                .toList();

        response.setPlants(plants);

        return response;
    }
}
