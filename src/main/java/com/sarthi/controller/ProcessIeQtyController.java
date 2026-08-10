package com.sarthi.controller;

import com.sarthi.dto.InspectionQtySummaryResponse;
import com.sarthi.service.ProcessIeQtyService;
import com.sarthi.util.ResponseBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/processIe")
public class ProcessIeQtyController {

        @Autowired
        private ProcessIeQtyService processIeQtyService;

        @GetMapping("/qty-summary/{requestId}")
        public ResponseEntity<Object> getInspectionQtySummary(@PathVariable String requestId) {

                return new ResponseEntity<>(
                                ResponseBuilder.getSuccessResponse(processIeQtyService.getQtySummary(requestId)),
                                HttpStatus.OK);
        }

        @GetMapping("/getPoNumnerByCallId/{requestId}")
        public ResponseEntity<Object> getPoNumnerByCallId(@PathVariable String requestId) {

                return new ResponseEntity<>(
                                ResponseBuilder.getSuccessResponse(processIeQtyService.getpoNumberByCallNo(requestId)),
                                HttpStatus.OK);
        }

        @GetMapping("/getManufaturedQtyOfPo/{heatNo}/{poNo}")
        public ResponseEntity<Object> getTotalManufaturedQty(
                        @PathVariable String heatNo,
                        @PathVariable String poNo,
                        @org.springframework.web.bind.annotation.RequestParam(required = false) String callNo) {

                return new ResponseEntity<>(
                                ResponseBuilder.getSuccessResponse(
                                                processIeQtyService.getTotalManufaturedQtyPo(heatNo, poNo, callNo)),
                                HttpStatus.OK);
        }

        @GetMapping("/accepted-qty-for-lot")
        public ResponseEntity<Object> getAcceptedQtyForLot(
                        @org.springframework.web.bind.annotation.RequestParam String requestId,
                        @org.springframework.web.bind.annotation.RequestParam String lotNumber,
                        @org.springframework.web.bind.annotation.RequestParam String heatNo) {
                return new ResponseEntity<>(
                                ResponseBuilder
                                                .getSuccessResponse(processIeQtyService.getAcceptedQtyForLot(requestId,
                                                                lotNumber, heatNo)),
                                HttpStatus.OK);
        }

}
