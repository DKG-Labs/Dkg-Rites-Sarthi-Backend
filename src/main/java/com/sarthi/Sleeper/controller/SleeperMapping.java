package com.sarthi.Sleeper.controller;

import com.sarthi.Sleeper.dto.mapping.*;
import com.sarthi.Sleeper.service.mappingService;
import com.sarthi.exception.ErrorDetails;
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
        try {
            return new ResponseEntity<>(
                    ResponseBuilder.getSuccessResponse(mappingService.createMapping(req)),
                    HttpStatus.OK
            );
        } catch (Exception ex) {
            String msg = ex.getMessage();
            if (msg == null || msg.trim().isEmpty()) {
                msg = "Failed to create sleeper mapping";
            }
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                    ResponseBuilder.getErrorResponse(new ErrorDetails(
                            HttpStatus.BAD_REQUEST.value(),
                            400,
                            "error",
                            msg
                    ))
            );
        }
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
        try {
            return new ResponseEntity<>(
                    ResponseBuilder.getSuccessResponse(mappingService.createBulkMapping(req)),
                    HttpStatus.OK
            );
        } catch (Exception ex) {
            String msg = ex.getMessage();
            if (msg == null || msg.trim().isEmpty()) {
                msg = "Failed to create bulk sleeper mapping";
            }
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                    ResponseBuilder.getErrorResponse(new ErrorDetails(
                            HttpStatus.BAD_REQUEST.value(),
                            400,
                            "error",
                            msg
                    ))
            );
        }
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
