package com.sarthi.Sleeper.controller;

import com.sarthi.Sleeper.dto.FinalCalDtos.*;
import com.sarthi.Sleeper.dto.SleeperTransitionActionReqDto;
import com.sarthi.Sleeper.entity.FInalCall.FinalCallnspectionSectionA;
import com.sarthi.Sleeper.service.FinalCallInspectionService;
import com.sarthi.Sleeper.service.SleeperWorkflowService;
import com.sarthi.util.ResponseBuilder;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/FinalCallinspection")
@RequiredArgsConstructor
public class FinalCalInspectionController {

    private final FinalCallInspectionService service;

    private final SleeperWorkflowService sleeperWorkflowService;

    @GetMapping("/section1/{callNo}")
    public ResponseEntity<Object> getDetails(@PathVariable String callNo) {
        InspectionCallSection1Response result = service.getDetails(callNo);
        return new ResponseEntity<>(
                ResponseBuilder.getSuccessResponse(result),
                HttpStatus.OK
        );
    }

    @GetMapping("/section2/{callNo}")
    public ResponseEntity<Object> getCallDetails(@PathVariable String callNo) {
        InspectionCallSection2DetailsResponse  result = service.getSectionB(callNo);
        return new ResponseEntity<>(
                ResponseBuilder.getSuccessResponse(result),
                HttpStatus.OK
        );
    }

    @PostMapping("/section1")
    public ResponseEntity<Object> createSectionA(@RequestBody SectionARequest request) {

        SectionARequest result = service.create(request);

        return new ResponseEntity<>(
                ResponseBuilder.getSuccessResponse(result),
                HttpStatus.CREATED
        );
    }

    @PostMapping("/section2")
    public ResponseEntity<Object> createSectionB(@RequestBody SectionBRequest request) {

        SectionBRequest result = service.create(request);

        return new ResponseEntity<>(
                ResponseBuilder.getSuccessResponse(result),
                HttpStatus.CREATED
        );
    }

    @PostMapping("scheduleingCall")
    public ResponseEntity<Object> create(@RequestBody SleeperScheduleRequest request) {

        SleeperScheduleRequest result = service.create(request);

        SleeperTransitionActionReqDto req = new  SleeperTransitionActionReqDto();
        req.setWorkflowTransitionId(request.getWorkflowTransitionId());
        req.setAction("MAIN_IE_SCHEDULE_CALL");
        req.setActionBy(request.getCreatedBy());
        req.setRequestId(request.getCallNo());

        sleeperWorkflowService.performTransitionAction(req);

        return new ResponseEntity<>(
                ResponseBuilder.getSuccessResponse(result),
                HttpStatus.CREATED
        );
    }

}
