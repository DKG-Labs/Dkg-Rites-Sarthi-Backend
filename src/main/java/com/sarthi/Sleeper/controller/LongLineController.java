package com.sarthi.Sleeper.controller;

import com.sarthi.Sleeper.dto.LonglineRequestDTO;
import com.sarthi.Sleeper.dto.LonglineResponseDTO;
import com.sarthi.Sleeper.service.LonglineService;
import com.sarthi.Sleeper.service.SleeperWorkflowService;
import com.sarthi.util.ResponseBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/longLine-bench")
public class LongLineController {

    @Autowired
    private LonglineService longlineService;
    @Autowired
    private SleeperWorkflowService sleeperWorkflowService;

    // CREATE
    @PostMapping("/create")
    public ResponseEntity<Object> create(@RequestBody LonglineRequestDTO dto) {

        LonglineResponseDTO result = longlineService.create(dto);

        String requestId = String.valueOf(result.getId());
        Long md= 12L;
        Long wid = 1L;

        sleeperWorkflowService.initiateWorkflow(requestId,md, wid, Long.valueOf(result.getCreatedBy()));

        return new ResponseEntity<>(
                ResponseBuilder.getSuccessResponse(result),
                HttpStatus.OK
        );
    }


    @PutMapping("/update/{id}")
    public ResponseEntity<Object> update(@PathVariable Long id,
                                         @RequestBody LonglineRequestDTO dto) {

        LonglineResponseDTO result = longlineService.update(id, dto);

        return new ResponseEntity<>(
                ResponseBuilder.getSuccessResponse(result),
                HttpStatus.OK
        );
    }


    @GetMapping("/{id}")
    public ResponseEntity<Object> getById(@PathVariable Long id) {

        LonglineResponseDTO result = longlineService.getById(id);

        return new ResponseEntity<>(
                ResponseBuilder.getSuccessResponse(result),
                HttpStatus.OK
        );
    }


    @GetMapping("/all")
    public ResponseEntity<Object> getAll() {

        return new ResponseEntity<>(
                ResponseBuilder.getSuccessResponse(longlineService.getAll()),
                HttpStatus.OK
        );
    }


    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Object> delete(@PathVariable Long id) {

        longlineService.delete(id);

        return new ResponseEntity<>(
                ResponseBuilder.getSuccessResponse("Deleted Successfully"),
                HttpStatus.OK
        );
    }

}
