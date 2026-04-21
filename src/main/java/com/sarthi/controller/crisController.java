package com.sarthi.controller;

import com.sarthi.dto.crisDtos.MaRequestDto;
import com.sarthi.dto.crisDtos.PoCancellationRequestDto;
import com.sarthi.dto.crisDtos.PoRequestDto;
import com.sarthi.exception.ErrorDetails;
import com.sarthi.service.crisService;
import com.sarthi.util.ResponseBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/Vendorsync")
public class crisController {

    @Autowired
    private crisService crisService;

    //manual-po/save

    @PostMapping("/save")
    public ResponseEntity<Object> savePo(@RequestBody PoRequestDto request) {

        try {
            crisService.savePoFromFrontend(request);

            return new ResponseEntity<>(
                    ResponseBuilder.getSuccessResponse("PO saved successfully"),
                    HttpStatus.OK
            );

        } catch (Exception e) {

            ErrorDetails error = new ErrorDetails(
                    1001,                      // errorCode
                    400,                       // errorTypeCode (HTTP)
                    "BAD_REQUEST",             // errorType
                    e.getMessage()             // message
            );

            return new ResponseEntity<>(
                    ResponseBuilder.getErrorResponse(error),
                    HttpStatus.BAD_REQUEST
            );
        }
    }

    @PostMapping("/savePoMa")
    public ResponseEntity<Object> savePoMa(@RequestBody MaRequestDto request) {

        try {
            crisService.saveMaFromFrontend(request);

            return new ResponseEntity<>(
                    ResponseBuilder.getSuccessResponse("PO Ma saved successfully"),
                    HttpStatus.OK
            );

        } catch (Exception e) {

            ErrorDetails error = new ErrorDetails(
                    1001,                      // errorCode
                    400,                       // errorTypeCode (HTTP)
                    "BAD_REQUEST",             // errorType
                    e.getMessage()             // message
            );

            return new ResponseEntity<>(
                    ResponseBuilder.getErrorResponse(error),
                    HttpStatus.BAD_REQUEST
            );
        }
    }

    @PostMapping("/savePoCa")
    public ResponseEntity<Object> savePoCa(@RequestBody PoCancellationRequestDto  request) {

        try {
            crisService.savePoCancellationFromFrontend(request);

            return new ResponseEntity<>(
                    ResponseBuilder.getSuccessResponse("PO CA saved successfully"),
                    HttpStatus.OK
            );

        } catch (Exception e) {

            ErrorDetails error = new ErrorDetails(
                    1001,                      // errorCode
                    400,                       // errorTypeCode (HTTP)
                    "BAD_REQUEST",             // errorType
                    e.getMessage()             // message
            );

            return new ResponseEntity<>(
                    ResponseBuilder.getErrorResponse(error),
                    HttpStatus.BAD_REQUEST
            );
        }
    }
    @PostMapping("/authenticate")
    public ResponseEntity<Object> authenticate() {
        try {
            String token = crisService.getImmsToken();
            java.util.Map<String, String> response = new java.util.HashMap<>();
            response.put("token", token);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @PostMapping("/fetch-po")
    public ResponseEntity<Object> fetchPo(@RequestBody java.util.Map<String, String> request) {
        try {
            Object data = crisService.fetchPoData(request);
            return ResponseEntity.ok(data);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }
}
