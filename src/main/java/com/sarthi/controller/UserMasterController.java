package com.sarthi.controller;

import com.sarthi.dto.*;
import com.sarthi.dto.MFA.VerifyOtpRequestDto;
import com.sarthi.dto.WorkflowDtos.userRequestDto;
import com.sarthi.service.UserService;
import com.sarthi.util.ResponseBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@CrossOrigin
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

  /*  @PostMapping("/loginBasedOnType")
    public ResponseEntity<Object> loginBasedOnType(@RequestBody LoginRequestBasedTypeDto loginRequestBasedTypeDto) {
        LoginResponseDto res = userService.loginBasedOnType(loginRequestBasedTypeDto);
        return new ResponseEntity<Object>(ResponseBuilder.getSuccessResponse(res), HttpStatus.OK);
//    }*/

    @PostMapping("/loginBasedOnType")
    public ResponseEntity<Object> loginBasedOnType(
            @RequestBody LoginRequestBasedTypeDto loginRequestBasedTypeDto) {

        // =====================================================
        // MFA STEP 1
        //
        // This now checks username/password and sends OTP.
        // It does NOT generate JWT.
        // =====================================================

        Object res =
                userService.loginBasedOnType(
                        loginRequestBasedTypeDto);

        return new ResponseEntity<Object>(
                ResponseBuilder.getSuccessResponse(res),
                HttpStatus.OK);
    }

    // =====================================================
// MFA STEP 2
// Verify OTP and complete login
// =====================================================

    @PostMapping("/verifyOtp")
    public ResponseEntity<Object> verifyOtp(
            @RequestBody VerifyOtpRequestDto request) {

        LoginResponseDto res =
                userService.verifyOtp(request);

        return new ResponseEntity<Object>(
                ResponseBuilder.getSuccessResponse(res),
                HttpStatus.OK);
    }
    @PostMapping("/forgot-password")
    public ResponseEntity<Object> forgotPassword(@RequestBody ForgotPasswordRequestDto requestDto) {
        userService.forgotPassword(requestDto);
        return new ResponseEntity<Object>(ResponseBuilder.getSuccessResponse("Password updated successfully"), HttpStatus.OK);
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

    @PostMapping("/map")
    public ResponseEntity<Object> mapProcessIe(
            @RequestBody PoiProcessIeRequestDto dto) {

        String res= userService.mapProcessIe(dto);
        return ResponseEntity.ok(res);
    }

    @GetMapping("/api/users")
    public ResponseEntity<Object> getAllUsers() {
        return new ResponseEntity<Object>(ResponseBuilder.getSuccessResponse(userService.getAllUsers()), HttpStatus.OK);
    }

    @PostMapping("/api/users")
    public ResponseEntity<Object> createUserFromAdmin(@RequestBody userRequestDto userRequestDto) {
        UserDto user = userService.createUser(userRequestDto);
        return new ResponseEntity<Object>(ResponseBuilder.getSuccessResponse(user), HttpStatus.OK);
    }

    @PutMapping("/api/users")
    public ResponseEntity<Object> updateUser(@RequestBody userRequestDto userRequestDto) {
        UserDto user = userService.updateUser(userRequestDto);
        return new ResponseEntity<Object>(ResponseBuilder.getSuccessResponse(user), HttpStatus.OK);
    }

    @DeleteMapping("/api/users/{userId}")
    public ResponseEntity<Object> deleteUser(@PathVariable Integer userId) {
        userService.deleteUser(userId);
        return new ResponseEntity<Object>(ResponseBuilder.getSuccessResponse("User deleted successfully"), HttpStatus.OK);
    }

    @PutMapping("/api/users/{userId}/region")
    public ResponseEntity<Object> updateUserRegion(@PathVariable Integer userId, @RequestBody Map<String, String> request) {
        String res = userService.updateUserRegion(userId, request.get("region"));
        return new ResponseEntity<Object>(ResponseBuilder.getSuccessResponse(res), HttpStatus.OK);
    }

    @PutMapping("/api/pincode-poi/{poiCode}/contact")
    public ResponseEntity<Object> updateUnitContact(
            @PathVariable String poiCode,
            @RequestBody Map<String, String> request) {
        String contactPerson = request.get("contactPerson");
        String contactPersonNumber = request.get("contactPersonNumber");
        String res = userService.updateUnitContact(poiCode, contactPerson, contactPersonNumber);
        return new ResponseEntity<Object>(ResponseBuilder.getSuccessResponse(res), HttpStatus.OK);
    }

    @PostMapping("/api/erc-vendor")
    public ResponseEntity<Object> createOrUpdateErcVendor(@RequestBody com.sarthi.dto.ErcVendorCreationDto dto) {
        Object response = userService.createOrUpdateErcVendor(dto);
        return new ResponseEntity<Object>(ResponseBuilder.getSuccessResponse(response), HttpStatus.OK);
    }

    @GetMapping("/api/erc-vendor/{userId}")
    public ResponseEntity<Object> getErcVendorDetails(@PathVariable Integer userId) {
        Object response = userService.getErcVendorDetails(userId);
        return new ResponseEntity<Object>(ResponseBuilder.getSuccessResponse(response), HttpStatus.OK);
    }

    @PostMapping("/api/sleeper-vendor")
    public ResponseEntity<Object> createOrUpdateSleeperVendor(@RequestBody com.sarthi.dto.SleeperVendorCreationDto dto) {
        Object response = userService.createOrUpdateSleeperVendor(dto);
        return new ResponseEntity<Object>(ResponseBuilder.getSuccessResponse(response), HttpStatus.OK);
    }

    @GetMapping("/api/sleeper-vendor/{userId}")
    public ResponseEntity<Object> getSleeperVendorDetails(@PathVariable Integer userId) {
        Object response = userService.getSleeperVendorDetails(userId);
        return new ResponseEntity<Object>(ResponseBuilder.getSuccessResponse(response), HttpStatus.OK);
    }
}
