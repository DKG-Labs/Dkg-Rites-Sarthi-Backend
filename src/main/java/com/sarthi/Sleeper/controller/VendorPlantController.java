package com.sarthi.Sleeper.controller;

import com.sarthi.Sleeper.dto.VendorResponseDTO;
import com.sarthi.Sleeper.service.VendorPlantService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/vendor-plant")
public class VendorPlantController {
    @Autowired
    private VendorPlantService vendorService;

    @GetMapping("/vendor/{vendorCode}/plants")
    public ResponseEntity<VendorResponseDTO> getPlants(@PathVariable String vendorCode) {
        return ResponseEntity.ok(vendorService.getPlantsByVendorCode(vendorCode));
    }
}
