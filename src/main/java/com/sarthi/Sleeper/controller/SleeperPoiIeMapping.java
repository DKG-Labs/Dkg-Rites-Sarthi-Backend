package com.sarthi.Sleeper.controller;

import com.sarthi.Sleeper.dto.SgciInventory.SgciInsertRequestDto;
import com.sarthi.Sleeper.dto.SleeperPoiIeMappingDto;
import com.sarthi.Sleeper.service.SleeperPoiIeMappingService;
import com.sarthi.util.ResponseBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/sleeper-mapping")
public class SleeperPoiIeMapping {
    @Autowired
    private SleeperPoiIeMappingService sleeperPoiIeMappingService;



    @PostMapping("/Sleeper-mapping")
    public  ResponseEntity<Object> saveMapping(@RequestBody SleeperPoiIeMappingDto dto) {

        return new ResponseEntity<>(
                ResponseBuilder.getSuccessResponse(sleeperPoiIeMappingService.saveMapping(dto)),
                HttpStatus.OK
        );
    }


}
