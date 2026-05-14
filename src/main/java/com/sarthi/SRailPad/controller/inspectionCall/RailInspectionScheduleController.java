package com.sarthi.SRailPad.controller.inspectionCall;

import com.sarthi.SRailPad.entity.inspectionCall.RailInspectionSchedule;
import com.sarthi.SRailPad.service.inspectionCall.RailInspectionScheduleService;
import com.sarthi.util.ResponseBuilder;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/rail-inspection-schedule")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class RailInspectionScheduleController {

    private final RailInspectionScheduleService service;

    @PostMapping("/schedule")
    public ResponseEntity<Object> schedule(@RequestBody RailInspectionSchedule schedule) {
        return new ResponseEntity<>(
                ResponseBuilder.getSuccessResponse(service.schedule(schedule)),
                HttpStatus.OK
        );
    }

    @PutMapping("/reschedule")
    public ResponseEntity<Object> reschedule(@RequestBody RailInspectionSchedule schedule) {
        return new ResponseEntity<>(
                ResponseBuilder.getSuccessResponse(service.reschedule(schedule)),
                HttpStatus.OK
        );
    }

    @GetMapping("/{callNo}")
    public ResponseEntity<Object> getByCallNo(@PathVariable String callNo) {
        return new ResponseEntity<>(
                ResponseBuilder.getSuccessResponse(service.getByCallNo(callNo).orElse(null)),
                HttpStatus.OK
        );
    }

    @GetMapping
    public ResponseEntity<Object> getAll() {
        return new ResponseEntity<>(
                ResponseBuilder.getSuccessResponse(service.getAll()),
                HttpStatus.OK
        );
    }

    @GetMapping("/count-by-date")
    public ResponseEntity<Object> getCountByDate(@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return new ResponseEntity<>(
                ResponseBuilder.getSuccessResponse(service.getCountByDate(date)),
                HttpStatus.OK
        );
    }

    @DeleteMapping("/{callNo}")
    public ResponseEntity<Object> delete(@PathVariable String callNo) {
        service.deleteByCallNo(callNo);
        return new ResponseEntity<>(
                ResponseBuilder.getSuccessResponse("Deleted successfully"),
                HttpStatus.OK
        );
    }
}
