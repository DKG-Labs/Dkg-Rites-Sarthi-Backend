package com.sarthi.Sleeper.controller;

import com.sarthi.Sleeper.dto.mapping.*;
import com.sarthi.Sleeper.service.mappingService;
import com.sarthi.util.ResponseBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/sleeper-mapping")
public class SleeperMapping {

    @Autowired
    private mappingService  mappingService;

    @GetMapping("/sleeper-plants/{vendorCode}")
    public ResponseEntity<List<String>> getPlantIdsByVendorCode(
            @PathVariable String vendorCode) {

        return ResponseEntity.ok(
                mappingService.getPlantIdsByVendorCode(vendorCode)
        );
    }

    @GetMapping("/sleeper-companies")
    public ResponseEntity<Object> getAllCompanies() {


        return new ResponseEntity<>(
                ResponseBuilder.getSuccessResponse(mappingService.getAllCompanies()),
                HttpStatus.OK
        );
    }

    @PostMapping("/sleeperMapping")
    public ResponseEntity<Object> createMapping(
            @RequestBody SleeperPoiIeMappingReqDto req){

        return new ResponseEntity<>(
                ResponseBuilder.getSuccessResponse(mappingService.createMapping(req)),
                HttpStatus.OK
        );
    }

    @GetMapping("/mapped-emp-list")
    public ResponseEntity<Object> mappedEmployeeList(

            @RequestParam String companyName,

            @RequestParam String plantId,

            @RequestParam String ieType){

        EmployeeMappingFetchReqDto req =
                new EmployeeMappingFetchReqDto();

        req.setCompanyName(companyName);
        req.setPlantId(plantId);
        req.setIeType(ieType);

        return new ResponseEntity<>(
                ResponseBuilder.getSuccessResponse(
                        mappingService.getMappedEmployees(req)
                ),
                HttpStatus.OK
        );
    }



    @PostMapping("/company-wise-sleeper-mapping")
    public ResponseEntity<Object> createMapping(
            @RequestBody CompanyEmployeeMappingReqDto req){

        return new ResponseEntity<>(
                ResponseBuilder.getSuccessResponse(mappingService.createBulkMapping(req)),
                HttpStatus.OK
        );
    }

    @GetMapping("/employees-by-role")
    public ResponseEntity<Object> getEmployeesByRoleId(
            @RequestParam Integer roleId){

        return new ResponseEntity<>(
                ResponseBuilder.getSuccessResponse(
                        mappingService.getEmployeesByRoleId(roleId)
                ),
                HttpStatus.OK
        );
    }


}
