package com.sarthi.SRailPad.controller;

import com.sarthi.SRailPad.dto.RailpadPoiIeMappingReqDto;
import com.sarthi.SRailPad.service.RailWorkflowService;
import com.sarthi.util.ResponseBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/railpadMapping")
public class RailpadMapping {

    @Autowired
    private RailWorkflowService railWorkflowService;

    @PostMapping
    public ResponseEntity<Object> createMapping(@RequestBody RailpadPoiIeMappingReqDto req) {
        return new ResponseEntity<Object>(
            ResponseBuilder.getSuccessResponse(railWorkflowService.saveRailpadMapping(req)),
            HttpStatus.OK
        );
    }
}
