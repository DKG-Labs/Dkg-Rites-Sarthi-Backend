package com.sarthi.Sleeper.controller;

import com.sarthi.Sleeper.dto.CompanyUnitResponseDto;
import com.sarthi.Sleeper.dto.SgciInventory.SgciInsertRequestDto;
import com.sarthi.Sleeper.dto.SleeperPoiIeMappingDto;
import com.sarthi.Sleeper.service.SleeperPoiIeMappingService;
import com.sarthi.util.ResponseBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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

    @GetMapping("/company-units/{ieUserId}")
    public ResponseEntity<Object>  getCompanyUnits(
            @PathVariable("ieUserId") Integer ieUserId) {

        List<CompanyUnitResponseDto> result = sleeperPoiIeMappingService.getCompanyUnits(ieUserId);
        return new ResponseEntity<>(
                ResponseBuilder.getSuccessResponse(result),
                HttpStatus.OK
        );
    }


}
