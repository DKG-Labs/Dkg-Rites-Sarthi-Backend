package com.sarthi.Sleeper.controller;

import com.sarthi.Sleeper.dto.StressBenchRequestDto;
import com.sarthi.Sleeper.dto.StressBenchResponseDto;
import com.sarthi.Sleeper.service.SleeperWorkflowService;
import com.sarthi.Sleeper.service.StressBenchMasterService;
import com.sarthi.util.ResponseBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/stress-bench")
public class StressBenchMasterController {


        @Autowired
        private StressBenchMasterService stressBenchService;

        @Autowired
        private SleeperWorkflowService sleeperWorkflowService;

        @PostMapping("/create")
        public ResponseEntity<Object> create(
                @RequestBody StressBenchRequestDto dto) {

            StressBenchResponseDto result =  stressBenchService.createBench(dto);
            String requestId = String.valueOf(result.getId());
            Long md = 2L;
            Long wid = 1L;
            sleeperWorkflowService.initiateWorkflow(requestId,md, wid, Long.valueOf(result.getCreatedBy()));

            return new ResponseEntity<>(
                    ResponseBuilder.getSuccessResponse(
                           result),
                    HttpStatus.OK);
        }


        @PutMapping("/update/{id}")
        public ResponseEntity<Object> update(
                @PathVariable Long id,
                @RequestBody StressBenchRequestDto dto) {

            return new ResponseEntity<>(
                    ResponseBuilder.getSuccessResponse(
                            stressBenchService.updateBench(id, dto)),
                    HttpStatus.OK);
        }


        @GetMapping("/get/{id}")
        public ResponseEntity<Object> getById(
                @PathVariable Long id) {

            return new ResponseEntity<>(
                    ResponseBuilder.getSuccessResponse(
                            stressBenchService.getBenchById(id)),
                    HttpStatus.OK);
        }


        @GetMapping("/getAll")
        public ResponseEntity<Object> getAll() {

            return new ResponseEntity<>(
                    ResponseBuilder.getSuccessResponse(
                            stressBenchService.getAllBenches()),
                    HttpStatus.OK);
        }


        @DeleteMapping("/delete/{id}")
        public ResponseEntity<Object> delete(
                @PathVariable Long id) {

            stressBenchService.deleteBench(id);
            return new ResponseEntity<>(
                    ResponseBuilder.getSuccessResponse(
                            "Deleted Successfully"),
                    HttpStatus.OK);
        }

}
