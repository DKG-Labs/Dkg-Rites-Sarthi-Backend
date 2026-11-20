package com.sarthi.Sleeper.controller;

import com.sarthi.Sleeper.dto.mapping.*;
import com.sarthi.Sleeper.entity.SleeperPoiIeMapping;
import com.sarthi.Sleeper.repository.SleeperPoiIeMappingRepository;
import com.sarthi.Sleeper.service.mappingService;
import com.sarthi.entity.UserMaster;
import com.sarthi.repository.UserMasterRepository;
import com.sarthi.exception.ErrorDetails;
import com.sarthi.util.ResponseBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/sleeper-mapping")
public class SleeperMapping {

    @Autowired
    private mappingService  mappingService;

    @Autowired
    private SleeperPoiIeMappingRepository sleeperPoiIeMappingRepository;

    @Autowired(required = false)
    private UserMasterRepository userMasterRepository;

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

    @GetMapping("/check-duplicate")
    public ResponseEntity<Object> checkDuplicateSleeper(
            @RequestParam Integer userId,
            @RequestParam String plantId,
            @RequestParam String poiCode,
            @RequestParam String ieType) {
        
        boolean isMainIe = ieType != null && (ieType.toUpperCase().contains("MAIN") || ieType.equalsIgnoreCase("Primary"));
        
        if (isMainIe) {
            List<SleeperPoiIeMapping> mainMappings = sleeperPoiIeMappingRepository
                    .findMainIeMappingsByPlantOrPoi(plantId, poiCode);
            if (mainMappings != null && !mainMappings.isEmpty()) {
                for (SleeperPoiIeMapping m : mainMappings) {
                    if (m.getIeUserId() != null && !m.getIeUserId().equals(userId)) {
                        String ieName = String.valueOf(m.getIeUserId());
                        if (userMasterRepository != null) {
                            try {
                                UserMaster u = userMasterRepository.findById(m.getIeUserId()).orElse(null);
                                if (u != null) {
                                    ieName = (u.getEmployeeCode() != null ? u.getEmployeeCode() + " - " : "") + (u.getUsername() != null ? u.getUsername() : "");
                                }
                            } catch (Exception ignored) {}
                        }
                        return ResponseEntity.ok(Map.of(
                                "exists", true,
                                "message", "This plant is already mapped to IE (" + ieName + ") for Sleeper Final (Main IE). Only one IE can be mapped."
                        ));
                    }
                }
                return ResponseEntity.ok(Map.of(
                        "exists", true,
                        "message", "This plant is already mapped to this IE for Sleeper Final (Main IE)."
                ));
            }
        }

        boolean exists = sleeperPoiIeMappingRepository
                .existsByPoiCodeAndPlantIdAndIeUserIdAndIeType(poiCode, plantId, userId, ieType);
        return ResponseEntity.ok(Map.of("exists", exists));
    }


}
