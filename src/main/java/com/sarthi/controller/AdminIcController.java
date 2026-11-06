package com.sarthi.controller;

import com.sarthi.constant.AppConstant;
import com.sarthi.exception.ErrorDetails;
import com.sarthi.service.AdminIcService;
import com.sarthi.util.ResponseBuilder;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/ic")
@AllArgsConstructor
@Slf4j
public class AdminIcController {

    private AdminIcService adminIcService;

    @GetMapping("/get-details")
    public ResponseEntity<Object> getIcDetails(
            @RequestParam(name = "callNo") String callNo) {
        try {
            if (callNo == null || callNo.isBlank()) {
                return new ResponseEntity<>(
                        ResponseBuilder.getErrorResponse(new ErrorDetails(
                                HttpStatus.BAD_REQUEST.value(),
                                HttpStatus.BAD_REQUEST.value(),
                                AppConstant.ERROR_TYPE_ERROR,
                                "callNo is required"
                        )),
                        HttpStatus.BAD_REQUEST
                );
            }

            Map<String, Object> result = adminIcService.getIcDetails(callNo.trim());

            return new ResponseEntity<>(
                    ResponseBuilder.getSuccessResponse(result),
                    HttpStatus.OK
            );
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(
                    ResponseBuilder.getErrorResponse(new ErrorDetails(
                            HttpStatus.NOT_FOUND.value(),
                            HttpStatus.NOT_FOUND.value(),
                            AppConstant.ERROR_TYPE_ERROR,
                            e.getMessage()
                    )),
                    HttpStatus.NOT_FOUND
            );
        } catch (Exception e) {
            log.error("Error fetching IC details: {}", e.getMessage(), e);
            return new ResponseEntity<>(
                    ResponseBuilder.getErrorResponse(new ErrorDetails(
                            HttpStatus.INTERNAL_SERVER_ERROR.value(),
                            HttpStatus.INTERNAL_SERVER_ERROR.value(),
                            AppConstant.ERROR_TYPE_ERROR,
                            "Failed to fetch IC details: " + e.getMessage()
                    )),
                    HttpStatus.INTERNAL_SERVER_ERROR
            );
        }
    }

    @PostMapping("/update-date")
    public ResponseEntity<Object> updateIcDate(
            @RequestBody Map<String, Object> req) {
        try {
            String callNo = (String) req.get("callNo");
            String newDateStr = (String) req.get("newDate");

            if (callNo == null || callNo.isBlank() || newDateStr == null || newDateStr.isBlank()) {
                return new ResponseEntity<>(
                        ResponseBuilder.getErrorResponse(new ErrorDetails(
                                HttpStatus.BAD_REQUEST.value(),
                                HttpStatus.BAD_REQUEST.value(),
                                AppConstant.ERROR_TYPE_ERROR,
                                "callNo and newDate are required"
                        )),
                        HttpStatus.BAD_REQUEST
                );
            }

            LocalDate newDate = LocalDate.parse(newDateStr);
            Map<String, Object> result = adminIcService.updateIcDate(callNo.trim(), newDate);

            return new ResponseEntity<>(
                    ResponseBuilder.getSuccessResponse(result),
                    HttpStatus.OK
            );
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(
                    ResponseBuilder.getErrorResponse(new ErrorDetails(
                            HttpStatus.BAD_REQUEST.value(),
                            HttpStatus.BAD_REQUEST.value(),
                            AppConstant.ERROR_TYPE_ERROR,
                            e.getMessage()
                    )),
                    HttpStatus.BAD_REQUEST
            );
        } catch (Exception e) {
            log.error("Error updating IC date: {}", e.getMessage(), e);
            return new ResponseEntity<>(
                    ResponseBuilder.getErrorResponse(new ErrorDetails(
                            HttpStatus.INTERNAL_SERVER_ERROR.value(),
                            HttpStatus.INTERNAL_SERVER_ERROR.value(),
                            AppConstant.ERROR_TYPE_ERROR,
                            "Failed to update IC date: " + e.getMessage()
                    )),
                    HttpStatus.INTERNAL_SERVER_ERROR
            );
        }
    }
}
