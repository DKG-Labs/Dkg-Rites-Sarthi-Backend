package com.sarthi.SRailPad.controller;

import com.sarthi.SRailPad.dto.RailPlantDTO;
import com.sarthi.SRailPad.dto.RailVendorResponseDTO;
import com.sarthi.SRailPad.entity.raipadMapping.RailPadPincodePoIMapping;
import com.sarthi.SRailPad.entity.raipadMapping.RailVendorPlants;
import com.sarthi.SRailPad.repository.RailPadPincodePoIMappingRepository;
import com.sarthi.SRailPad.repository.RailVendorPlantsRepository;
import com.sarthi.entity.PincodePoIMapping;
import com.sarthi.repository.PincodePoIMappingRepository;
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
    private final RailPadPincodePoIMappingRepository railPadPincodePoIMappingRepository;
    private final PincodePoIMappingRepository pincodePoIMappingRepository;

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
        List<RailPadPincodePoIMapping> allPoiMappings = railPadPincodePoIMappingRepository.findAll();
        
        Map<String, String> vendorToPoiMap = new HashMap<>();
        Map<String, String> companyToPoiMap = new HashMap<>();
        
        for (RailPadPincodePoIMapping poi : allPoiMappings) {
            if (poi.getPoiCode() != null && !poi.getPoiCode().isBlank()) {
                String pCode = poi.getPoiCode().trim();
                if (poi.getVendorCode() != null && !poi.getVendorCode().isBlank()) {
                    String cleanV = poi.getVendorCode().replace(":", "").trim();
                    vendorToPoiMap.put(cleanV, pCode);
                    vendorToPoiMap.put(":" + cleanV, pCode);
                }
                if (poi.getCompanyName() != null && !poi.getCompanyName().isBlank()) {
                    companyToPoiMap.put(poi.getCompanyName().trim().toLowerCase(), pCode);
                }
            }
        }
        
        Map<String, Map<String, String>> companyMap = new LinkedHashMap<>();
        
        for (RailVendorPlants p : allPlants) {
            if (p.getVendorCode() != null && !p.getVendorCode().isBlank()) {
                String vCode = p.getVendorCode();
                String cleanVCode = vCode.replace(":", "").trim();
                String cName = (p.getCompanyName() != null && !p.getCompanyName().isBlank()) ? p.getCompanyName().trim() : vCode;

                if (!companyMap.containsKey(vCode)) {
                    String poiCode = vendorToPoiMap.get(cleanVCode);
                    if (poiCode == null) {
                        poiCode = companyToPoiMap.get(cName.toLowerCase());
                    }
                    if (poiCode == null) {
                        List<PincodePoIMapping> generalList = pincodePoIMappingRepository.findByVendorCode(cleanVCode);
                        if (generalList != null && !generalList.isEmpty() && generalList.get(0).getPoiCode() != null) {
                            poiCode = generalList.get(0).getPoiCode().trim();
                        }
                    }
                    if (poiCode == null) {
                        poiCode = vCode;
                    }

                    Map<String, String> m = new HashMap<>();
                    m.put("companyName", cName);
                    m.put("vendorCode", vCode);
                    m.put("poiCode", poiCode);
                    companyMap.put(vCode, m);
                }
            }
        }
        
        return ResponseEntity.ok(com.sarthi.util.ResponseBuilder.getSuccessResponse(new ArrayList<>(companyMap.values())));
    }
}
