package com.sarthi.Sleeper.controller;

import com.sarthi.util.ResponseBuilder;
import com.sarthi.Sleeper.service.Impl.AggregateTestingStatusService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/aggregate-status")
@CrossOrigin(origins = "*", maxAge = 3600)
public class AggregateTestingStatusController {

    @Autowired
    private AggregateTestingStatusService aggregateTestingStatusService;

    @PostMapping("/bulk")
    public ResponseEntity<Object> getBulkStatus(@RequestBody List<Long> requestIds) {
        return new ResponseEntity<>(
                ResponseBuilder.getSuccessResponse(aggregateTestingStatusService.getBulkStatus(requestIds)),
                HttpStatus.OK
        );
    }
}
