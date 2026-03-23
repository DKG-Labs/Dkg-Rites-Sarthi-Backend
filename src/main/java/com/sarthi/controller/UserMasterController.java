package com.sarthi.controller;

import com.sarthi.dto.*;
import com.sarthi.dto.WorkflowDtos.userRequestDto;
import com.sarthi.service.UserService;
import com.sarthi.util.ResponseBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/auth")
public class UserMasterController {

    @Autowired
    private UserService userService;

    @PostMapping
    public ResponseEntity<Object> createUser(@RequestBody userRequestDto userRequestDto) {
        UserDto user = userService.createUser(userRequestDto);
        return new ResponseEntity<Object>(ResponseBuilder.getSuccessResponse(user), HttpStatus.OK);
    }

    @PostMapping("/login")
    public ResponseEntity<Object> login(@RequestBody LoginRequestDto loginRequestDto) {
        LoginResponseDto res = userService.login(loginRequestDto);
        return new ResponseEntity<Object>(ResponseBuilder.getSuccessResponse(res), HttpStatus.OK);
    }

    @PostMapping("/loginBasedOnType")
    public ResponseEntity<Object> loginBasedOnType(@RequestBody LoginRequestBasedTypeDto loginRequestBasedTypeDto) {
        LoginResponseDto res = userService.loginBasedOnType(loginRequestBasedTypeDto);
        return new ResponseEntity<Object>(ResponseBuilder.getSuccessResponse(res), HttpStatus.OK);
    }

    @PostMapping("/api/OnlyRoleBasedCreation")
    public ResponseEntity<Object> createUserAndRole(@RequestBody userRequestDto userRequestDto) {
        UserDto user = userService.createUserAndRole(userRequestDto);
        return new ResponseEntity<Object>(ResponseBuilder.getSuccessResponse(user), HttpStatus.OK);
    }

    @PostMapping("/api/IeMapping")
    public ResponseEntity<Object> creatMapingIe(@RequestParam Long userId, @RequestBody IeSetupRequestDto dto) {

        return new ResponseEntity<Object>(ResponseBuilder.getSuccessResponse(userService.setupIe(userId, dto)),
                HttpStatus.OK);
    }

    @PostMapping("/api/processIeMapping")
    public ResponseEntity<Object> createMappingProcessIe(@RequestBody ProcessIeMappingRequestDto dto,
            @RequestParam Long userId,
            @RequestParam String createdBy) {

        return new ResponseEntity<Object>(
                ResponseBuilder.getSuccessResponse(userService.mapProcessIe(userId, dto, createdBy)), HttpStatus.OK);
    }

    @GetMapping("/api/roles")
    public ResponseEntity<Object> getAllRoles() {
        return new ResponseEntity<Object>(ResponseBuilder.getSuccessResponse(userService.getAllRoleNames()),
                HttpStatus.OK);
    }

    @GetMapping("/api/users/by-role")
    public ResponseEntity<Object> getUsersByRole(@RequestParam String roleName) {
        return new ResponseEntity<Object>(ResponseBuilder.getSuccessResponse(userService.getUsersByRole(roleName)),
                HttpStatus.OK);
    }

    @GetMapping("/api/pincode-poi/companies")
    public ResponseEntity<Object> getAllCompanies() {
        return new ResponseEntity<Object>(ResponseBuilder.getSuccessResponse(userService.getAllCompanies()),
                HttpStatus.OK);
    }

    @GetMapping("/api/pincode-poi/units")
    public ResponseEntity<Object> getUnitsByCompany(@RequestParam String companyName) {
        return new ResponseEntity<Object>(
                ResponseBuilder.getSuccessResponse(userService.getUnitsByCompany(companyName)),
                HttpStatus.OK);
    }

    @GetMapping("/api/pincode-poi/details")
    public ResponseEntity<Object> getMappingDetails(@RequestParam String companyName, @RequestParam String unitName) {
        return new ResponseEntity<Object>(
                ResponseBuilder.getSuccessResponse(userService.getMappingByCompanyAndUnit(companyName, unitName)),
                HttpStatus.OK);
    }


    @GetMapping("/ie/company-unit")
    public ResponseEntity<Object> getIeByCompanyAndUnit() {
        return new ResponseEntity<Object>(
                ResponseBuilder.getSuccessResponse( userService.getAllCompanyMappedIe()),
                HttpStatus.OK);

    }

    @GetMapping("/company-unit-process-ie")
    public ResponseEntity<Object> getCompanyUnitProcessIe() {
        return new ResponseEntity<Object>(
                ResponseBuilder.getSuccessResponse( userService.getCompanyUnitProcessIe()),
                HttpStatus.OK);

    }

    @GetMapping("/employee-codes/{callNo}")
    public ResponseEntity<Object> getEmployeeCodes(@PathVariable String callNo) {
     List<String > result =  userService.getEmployeeCodesByCallNo(callNo);
        return new ResponseEntity<Object>(
                ResponseBuilder.getSuccessResponse(result),
                HttpStatus.OK);
    }


    @PutMapping("/company/{poiCode}/updateIemapping")
    public ResponseEntity<Object> syncCompanyMapping(
            @PathVariable String poiCode,
            @RequestBody List<IePinPoiDto> dtoList) {

        String response = userService.updateCompanyIeMapping(poiCode, dtoList);
        return ResponseEntity.ok(response);
    }


    @GetMapping("/getEmpBYcompany/{poiCode}")
    public ResponseEntity<Object> getEmployeesByCompany(@PathVariable String poiCode) {

        return ResponseEntity.ok(
                userService.getEmployeesByPoi(poiCode)
        );
    }

    @GetMapping("/poi/{poiCode}/getProcessIeByPOI")
    public ResponseEntity<Object> getProcessAndIe(@PathVariable String poiCode) {
        return ResponseEntity.ok(userService.getProcessAndIeUsers(poiCode));
    }

    @PutMapping("/poi/{poiCode}/Update/processIe")
    public ResponseEntity<Object> updatePoiIeUsers(
            @PathVariable String poiCode,
            @RequestBody List<Long> ieUserIds,
            @RequestHeader("userId") String createdBy) {

        return ResponseEntity.ok(
                userService.updatePoiIeUsers(poiCode, ieUserIds, createdBy)
        );



    }

    @GetMapping("/{callNo}/poi-codes")
    public ResponseEntity<?> getPoiCodes(@PathVariable String callNo) {

        return ResponseEntity.ok(
                userService.getPlaceOfInspection(callNo)
        );
    }
}
