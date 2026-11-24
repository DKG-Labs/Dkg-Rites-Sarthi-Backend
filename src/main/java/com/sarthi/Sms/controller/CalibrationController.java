package com.sarthi.Sms.controller;

import com.sarthi.Sms.dto.calibration.BulkCalibrationReqDto;
import com.sarthi.Sms.dto.calibration.CalibrationDetailDto;
import com.sarthi.Sms.dto.calibration.CalibrationReqDto;
import com.sarthi.Sms.dto.calibration.CalibrationCsvImportDto;
import com.sarthi.Sms.dto.calibration.CalibrationCsvImportResponseDto;
import com.sarthi.Sms.dto.calibration.BulkCalibrationCsvImportDto;
import com.sarthi.Sms.exception.SmsErrorDetails;
import com.sarthi.Sms.constant.RailMillAppConstant;
import com.sarthi.Sms.dto.sms.common.DutyStatusResDto;
import com.sarthi.Sms.dto.sms.common.EndDutyReqDto;
import com.sarthi.Sms.dto.sms.common.StartDutyReqDto;
import com.sarthi.Sms.dto.sms.common.StartDutyResDto;
import com.sarthi.Sms.service.CalibrationService;
import com.sarthi.Sms.util.SmsResponseBuilder;
import com.sarthi.Sms.constant.RailMillAppConstant;
// [railmill] dropped: import com.sarthi.Sms.entity.auth.UserEntity;
// [railmill] dropped: import com.sarthi.Sms.entity.auth.UserType;
import com.sarthi.Sms.exception.SmsErrorDetails;
import com.sarthi.Sms.exception.SmsInvalidArgumentException;
// [railmill] dropped: import com.sarthi.Sms.repository.auth.UserRepository;
// [railmill] dropped: import com.rites.ritesbackend.service.JwtService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

import java.util.List;
import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@RestController
@RequestMapping({"/sms/calibration"})
public class CalibrationController {

    @Autowired
    private CalibrationService calibrationDutyService;

    @Autowired
    private com.sarthi.repository.UserMasterRepository userMasterRepository;

    // Rites MAIN_ADMIN / LOCAL_ADMIN -> SARTHI users holding an Admin role (Admin, Super Admin, Rites Admin)
    private void validateAdminAccess(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new SmsInvalidArgumentException(new SmsErrorDetails(
                RailMillAppConstant.ERROR_CODE_INVALID,
                RailMillAppConstant.ERROR_TYPE_CODE_INVALID,
                RailMillAppConstant.ERROR_TYPE_INVALID,
                "Authorization header is required"));
        }

        Integer userId = com.sarthi.Sms.util.SmsCommonUtils.getUserIdFromAuthHeader(authHeader);
        Optional<com.sarthi.entity.UserMaster> userOpt = userMasterRepository.findByUserId(userId);
        boolean isAdmin = userOpt.map(u -> u.getRoleName() != null
                && u.getRoleName().toLowerCase().contains("admin")).orElse(false);
        if (!isAdmin) {
            throw new SmsInvalidArgumentException(new SmsErrorDetails(
                RailMillAppConstant.ERROR_CODE_INVALID,
                RailMillAppConstant.ERROR_TYPE_CODE_INVALID,
                RailMillAppConstant.ERROR_TYPE_INVALID,
                "Access denied. Only MAIN_ADMIN and LOCAL_ADMIN can create new calibrations"));
        }
    }

    @PostMapping("/startDuty")
    public ResponseEntity<Object> startDuty(@RequestHeader("Authorization") String ah, @RequestBody StartDutyReqDto req) {
        StartDutyResDto res = calibrationDutyService.startDuty(ah, req);
        return new ResponseEntity<>(SmsResponseBuilder.getSuccessResponse(res), HttpStatus.OK);
    }

    @PostMapping("/endDuty")
    public ResponseEntity<Object> endDuty(@RequestBody EndDutyReqDto req) {
        calibrationDutyService.endDuty(req);
        return new ResponseEntity<>(SmsResponseBuilder.getSuccessResponse(), HttpStatus.OK);
    }

    @GetMapping("/checkDutyStatus")
    public ResponseEntity<Object> checkDutyStatus(@RequestHeader("Authorization") String ah) {
        DutyStatusResDto res = calibrationDutyService.checkDutyStatus(ah);
        return new ResponseEntity<>(SmsResponseBuilder.getSuccessResponse(res), HttpStatus.OK);
    }

    @GetMapping("/getOngoingDutyDtls")
    public ResponseEntity<Object> getOngoingDutyDtls(@RequestHeader("Authorization") String ah) {
        StartDutyResDto res = calibrationDutyService.getOngoingDutyDtls(ah);
        return new ResponseEntity<Object>(SmsResponseBuilder.getSuccessResponse(res), HttpStatus.OK);
    }

    @PostMapping("/saveCalibration")
    public ResponseEntity<Object> saveCalibration(@RequestHeader("Authorization") String ah,@RequestBody CalibrationReqDto requestDto) {
        // Check if this is a new calibration (creation) or modification of existing calibration
        boolean isExistingCalibration = calibrationDutyService.isExistingCalibration(requestDto.getSerialNumber());

        // Only MAIN_ADMIN and LOCAL_ADMIN can create new calibrations
        // Normal users can only modify existing calibrations
        if (!isExistingCalibration) {
            validateAdminAccess(ah);
        }

        calibrationDutyService.saveCalibration(ah,requestDto);
        return new ResponseEntity<>(SmsResponseBuilder.getSuccessResponse(), HttpStatus.OK);
    }

    @GetMapping("/getCalibrationDtls")
    public ResponseEntity<Object> getCalibrationDetails(@RequestParam("serialNumber") String serialNumber) {
        CalibrationDetailDto response = calibrationDutyService.getLatestCalibrationDetail(serialNumber);
        return new ResponseEntity<>(SmsResponseBuilder.getSuccessResponse(response), HttpStatus.OK);
    }

    @GetMapping("/getAllLatestCalibrations")
    public ResponseEntity<Object> getAllLatestCalibrations() {
        List<CalibrationDetailDto> response = calibrationDutyService.getAllLatestCalibrations();
        return new ResponseEntity<>(SmsResponseBuilder.getSuccessResponse(response), HttpStatus.OK);
    }

    @PostMapping("/saveBulkCalibration")
    public ResponseEntity<Object> saveBulkCalibration(
            @RequestHeader("Authorization") String authorizationHeader,
            @RequestBody BulkCalibrationReqDto bulkCalibrationRequestDto) {

        calibrationDutyService.saveBulkCalibration( authorizationHeader,bulkCalibrationRequestDto);

        return new ResponseEntity<>(SmsResponseBuilder.getSuccessResponse(), HttpStatus.OK);
    }

    @GetMapping("/getVrnrMmWmcInstList")
    public ResponseEntity<Object> getMmVrnrWmcInstList() {
        Map<String, List<String> > res = calibrationDutyService.getMmVrnrWmcInstList();
        return new ResponseEntity<>(SmsResponseBuilder.getSuccessResponse(res), HttpStatus.OK);
    }

    @DeleteMapping("/deleteCalibration")
    public ResponseEntity<Object> deleteCalibration(
            @RequestHeader("Authorization") String authHeader,
            @RequestParam String serialNumber) {
        // Only MAIN_ADMIN and LOCAL_ADMIN can delete calibration instruments
        validateAdminAccess(authHeader);

        calibrationDutyService.deleteCalibration(serialNumber);
        return new ResponseEntity<>(SmsResponseBuilder.getSuccessResponse(), HttpStatus.OK);
    }

    @PostMapping("/importCalibrationFromCsv")
    public ResponseEntity<Object> importCalibrationFromCsv(
            @RequestHeader("Authorization") String authorizationHeader,
            @RequestBody CalibrationCsvImportDto csvData) {

        CalibrationCsvImportResponseDto response = calibrationDutyService.importCalibrationFromCsv(authorizationHeader, csvData);

        if (response.hasErrors()) {
            SmsErrorDetails errorDetails = new SmsErrorDetails(
                RailMillAppConstant.ERROR_CODE_VALIDATION,
                RailMillAppConstant.ERROR_TYPE_CODE_VALIDATION,
                RailMillAppConstant.ERROR_TYPE_VALIDATION,
                "CSV import failed: " + response.getSummary()
            );
            return new ResponseEntity<>(SmsResponseBuilder.getErrorResponse(errorDetails), HttpStatus.BAD_REQUEST);
        }

        return new ResponseEntity<>(SmsResponseBuilder.getSuccessResponse(response), HttpStatus.OK);
    }

    @PostMapping("/importBulkCalibrationFromCsv")
    public ResponseEntity<Object> importBulkCalibrationFromCsv(
            @RequestHeader("Authorization") String authorizationHeader,
            @RequestBody BulkCalibrationCsvImportDto bulkCsvData) {

        CalibrationCsvImportResponseDto response = calibrationDutyService.importBulkCalibrationFromCsv(authorizationHeader, bulkCsvData);

        if (response.hasErrors()) {
            SmsErrorDetails errorDetails = new SmsErrorDetails(
                RailMillAppConstant.ERROR_CODE_VALIDATION,
                RailMillAppConstant.ERROR_TYPE_CODE_VALIDATION,
                RailMillAppConstant.ERROR_TYPE_VALIDATION,
                "Bulk CSV import completed with errors: " + response.getSummary()
            );
            return new ResponseEntity<>(SmsResponseBuilder.getErrorResponse(errorDetails), HttpStatus.BAD_REQUEST);
        }

        return new ResponseEntity<>(SmsResponseBuilder.getSuccessResponse(response), HttpStatus.OK);
    }
}
