package com.sarthi.Sleeper.controller;

import com.sarthi.util.ResponseBuilder;
import com.sarthi.Sleeper.service.Impl.CementTestingStatusService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/cement-status")
@CrossOrigin(origins = "*", maxAge = 3600)
public class CementTestingStatusController {

    @Autowired
    private CementTestingStatusService cementTestingStatusService;

    @PostMapping("/bulk")
    public ResponseEntity<Object> getBulkStatus(@RequestBody List<Long> requestIds) {
        return new ResponseEntity<>(
                ResponseBuilder.getSuccessResponse(cementTestingStatusService.getBulkStatus(requestIds)),
                HttpStatus.OK
        );
    }
}
