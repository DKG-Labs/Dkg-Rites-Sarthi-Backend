package com.sarthi.controller;

import com.sarthi.entity.PincodePoIMapping;
import com.sarthi.repository.PincodePoIMappingRepository;
import com.sarthi.repository.PoHeaderRepository;
import com.sarthi.util.ResponseBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/filters")
public class FilterController {

    @Autowired
    private PincodePoIMappingRepository pincodePoIMappingRepository;

    @Autowired
    private PoHeaderRepository poHeaderRepository;

    @Autowired
    private com.sarthi.SRailPad.repository.RailVendorPlantsRepository railVendorPlantsRepository;

    @GetMapping("/vendor-plants")
    public ResponseEntity<Object> getVendorPlants() {
        List<PincodePoIMapping> list = pincodePoIMappingRepository.findVendorPlantsWithPo();
        return new ResponseEntity<>(ResponseBuilder.getSuccessResponse(list), HttpStatus.OK);
    }

    @GetMapping("/railpad-vendor-plants")
    public ResponseEntity<Object> getRailPadVendorPlants() {
        List<com.sarthi.SRailPad.entity.raipadMapping.RailVendorPlants> list = railVendorPlantsRepository.findAll();
        return new ResponseEntity<>(ResponseBuilder.getSuccessResponse(list), HttpStatus.OK);
    }

    @GetMapping("/zonal-railways")
    public ResponseEntity<Object> getZonalRailways(@RequestParam String poiCode) {
        List<String> list = poHeaderRepository.findZonalRailwaysByPoiCode(poiCode);
        return new ResponseEntity<>(ResponseBuilder.getSuccessResponse(list), HttpStatus.OK);
    }

    @GetMapping("/all-zonal-railways")
    public ResponseEntity<Object> getAllZonalRailways() {
        List<String> list = poHeaderRepository.findAllZonalRailways();
        return new ResponseEntity<>(ResponseBuilder.getSuccessResponse(list), HttpStatus.OK);
    }

    @GetMapping("/vendor-plants-by-zone")
    public ResponseEntity<Object> getVendorPlantsByZone(@RequestParam String zone) {
        List<PincodePoIMapping> list = pincodePoIMappingRepository.findVendorPlantsByZone(zone);
        return new ResponseEntity<>(ResponseBuilder.getSuccessResponse(list), HttpStatus.OK);
    }

    @GetMapping("/railpad-zonal-railways")
    public ResponseEntity<Object> getRailPadZonalRailways() {
        List<String> list = poHeaderRepository.findRailPadZonalRailways();
        return new ResponseEntity<>(ResponseBuilder.getSuccessResponse(list), HttpStatus.OK);
    }

    @GetMapping("/railpad-vendor-plants-by-zone")
    public ResponseEntity<Object> getRailPadVendorPlantsByZone(@RequestParam(required = false) String zone) {
        List<Object[]> rawList = poHeaderRepository.findRailPadVendorPlantsByZone(zone);
        List<java.util.Map<String, Object>> result = new java.util.ArrayList<>();
        if (rawList != null) {
            for (Object[] row : rawList) {
                java.util.Map<String, Object> map = new java.util.HashMap<>();
                map.put("poiCode", row[0] != null ? row[0].toString() : "");
                map.put("companyName", row[1] != null ? row[1].toString() : "");
                map.put("unitName", row[2] != null ? row[2].toString() : "");
                map.put("address", row[3] != null ? row[3].toString() : "");
                result.add(map);
            }
        }
        return new ResponseEntity<>(ResponseBuilder.getSuccessResponse(result), HttpStatus.OK);
    }
}
