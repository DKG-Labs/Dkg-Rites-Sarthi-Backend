package com.sarthi.SRailPad.controller;

import com.sarthi.SRailPad.dto.RailPlantDTO;
import com.sarthi.SRailPad.dto.RailVendorResponseDTO;
import com.sarthi.SRailPad.entity.raipadMapping.RailVendorPlants;
import com.sarthi.SRailPad.repository.RailVendorPlantsRepository;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/railpad-vendor-plant")
@AllArgsConstructor
@CrossOrigin(origins = "*")
public class RailVendorPlantController {

    private final RailVendorPlantsRepository railVendorPlantsRepository;

    @GetMapping("/vendor/{vendorCode}/plants")
    public ResponseEntity<RailVendorResponseDTO> getPlants(@PathVariable String vendorCode) {
        List<RailVendorPlants> plants = railVendorPlantsRepository.findByVendorCode(vendorCode);
        
        RailVendorResponseDTO response = new RailVendorResponseDTO();
        response.setVendorCode(vendorCode);
        
        if (!plants.isEmpty()) {
            response.setCompanyName(plants.get(0).getCompanyName());
            response.setPlants(plants.stream()
                    .map(p -> new RailPlantDTO(p.getPlantName(), p.getPlantId()))
                    .collect(Collectors.toList()));
        } else {
            response.setCompanyName("Vendor Workspace");
            response.setPlants(List.of());
        }
        return ResponseEntity.ok(response);
    }

    @GetMapping("/companies")
    public ResponseEntity<Object> getRailpadCompanies() {
        List<RailVendorPlants> allPlants = railVendorPlantsRepository.findAll();
        Map<String, Map<String, String>> companyMap = new LinkedHashMap<>();
        
        for (RailVendorPlants p : allPlants) {
            if (p.getVendorCode() != null && !p.getVendorCode().isBlank()) {
                String vCode = p.getVendorCode();
                if (!companyMap.containsKey(vCode)) {
                    Map<String, String> m = new HashMap<>();
                    m.put("companyName", p.getCompanyName() != null && !p.getCompanyName().isBlank() ? p.getCompanyName() : vCode);
                    m.put("vendorCode", vCode);
                    m.put("poiCode", vCode);
                    companyMap.put(vCode, m);
                }
            }
        }
        
        return ResponseEntity.ok(com.sarthi.util.ResponseBuilder.getSuccessResponse(new ArrayList<>(companyMap.values())));
    }
}
