package com.sarthi.SRailPad.controller.dashboard;

import com.sarthi.SRailPad.service.Impl.dashboard.RailCmDashboardService;
import com.sarthi.service.CmCallScopeService;
import com.sarthi.util.ResponseBuilder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** Rail Pad calls for the Controlling Manager dashboard (mirrors the ERC / Sleeper cm-* endpoints). */
@RestController
@RequestMapping("/api/rail-dashboard")
public class RailCmDashboardController {

    private final RailCmDashboardService railCmDashboardService;
    private final CmCallScopeService cmCallScopeService;

    public RailCmDashboardController(RailCmDashboardService railCmDashboardService,
                                     CmCallScopeService cmCallScopeService) {
        this.railCmDashboardService = railCmDashboardService;
        this.cmCallScopeService = cmCallScopeService;
    }

    @GetMapping("/cm-inspection-calls")
    public ResponseEntity<Object> getCmInspectionCalls(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        return new ResponseEntity<>(ResponseBuilder.getSuccessResponse(cmCallScopeService.scopeTimed(() -> railCmDashboardService.getCmInspectionCalls(startDate, endDate))), HttpStatus.OK);
    }

    @GetMapping("/cm-overdue-calls")
    public ResponseEntity<Object> getCmOverdueCalls(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        return new ResponseEntity<>(ResponseBuilder.getSuccessResponse(cmCallScopeService.scopeTimed(() -> railCmDashboardService.getCmOverdueCalls(startDate, endDate))), HttpStatus.OK);
    }
}
