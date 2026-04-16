package com.sarthi.Sleeper.controller;

import com.sarthi.Sleeper.dto.RlyProjection;
import com.sarthi.Sleeper.dto.VendorResponseDTO;
import com.sarthi.Sleeper.service.VendorPlantService;
import com.sarthi.util.ResponseBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects;

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


    @GetMapping("/Rlylist")
    public ResponseEntity<Object>  getUniqueRlyList() {
        List<RlyProjection> rly = vendorService.getUniqueRlyList();

        return new ResponseEntity<>(
                ResponseBuilder.getSuccessResponse(rly),
                HttpStatus.OK);
    }


}
