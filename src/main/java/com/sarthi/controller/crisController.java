package com.sarthi.controller;

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



}
