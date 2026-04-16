package com.sarthi.Sleeper.controller;

import com.sarthi.Sleeper.dto.VendorResponseDTO;
import com.sarthi.Sleeper.service.VendorPlantService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/vendor-plant")
public class VendorPlantController {
    @Autowired
    private VendorPlantService vendorService;

    @GetMapping("/vendor/{vendorCode}/plants")
    public ResponseEntity<VendorResponseDTO> getPlants(@PathVariable String vendorCode) {
        return ResponseEntity.ok(vendorService.getPlantsByVendorCode(vendorCode));
    }

    @GetMapping("/vendorUser/{vendorCode}/plants")
    public ResponseEntity<VendorResponseDTO> getUserPlants(@RequestParam String vendorCode, @RequestParam Integer userId) {
        return ResponseEntity.ok(vendorService.getPlantsByVendorCodeAndUser(vendorCode, userId));
    }
}
