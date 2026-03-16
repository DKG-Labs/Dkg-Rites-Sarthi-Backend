package com.sarthi.Sleeper.controller;

import com.sarthi.Sleeper.dto.ProductionDeclaration.ProductionDeclarationRequestDto;
import com.sarthi.Sleeper.dto.ProductionDeclaration.ProductionDeclarationResponseDto;
import com.sarthi.Sleeper.service.ProductionDeclarationService;
import com.sarthi.Sleeper.service.SleeperWorkflowService;
import com.sarthi.util.ResponseBuilder;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/production-declaration")
@RequiredArgsConstructor
public class productionDeclarationController {

        private final ProductionDeclarationService service;

        @Autowired
        private SleeperWorkflowService sleeperWorkflowService;

        @PostMapping("/create")
        public ResponseEntity<Object> create(
                        @RequestBody ProductionDeclarationRequestDto dto) {

                ProductionDeclarationResponseDto result = service.create(dto);
                String requestId = String.valueOf(result.getId());
                Long md = 11L;
                Long wid = 1L;
                sleeperWorkflowService.initiateWorkflow(requestId, md, wid, Long.valueOf(result.getCreatedBy()));

                return new ResponseEntity<>(
                                ResponseBuilder.getSuccessResponse(),
                                HttpStatus.OK);
        }

        @PutMapping("/update/{id}")
        public ResponseEntity<Object> update(
                        @PathVariable Long id,
                        @RequestBody ProductionDeclarationRequestDto dto) {

                return new ResponseEntity<>(
                                ResponseBuilder.getSuccessResponse(
                                                service.update(id, dto)),
                                HttpStatus.OK);
        }

        @GetMapping("/{id}")
        public ResponseEntity<Object> getById(
                        @PathVariable Long id) {

                return new ResponseEntity<>(
                                ResponseBuilder.getSuccessResponse(
                                                service.getById(id)),
                                HttpStatus.OK);
        }

        @GetMapping("/getAll")
        public ResponseEntity<Object> getAll() {

                return new ResponseEntity<>(
                                ResponseBuilder.getSuccessResponse(
                                                service.getAll()),
                                HttpStatus.OK);
        }

        @GetMapping("/getByUser/{userId}")
        public ResponseEntity<Object> getByUser(@PathVariable Long userId) {
                return new ResponseEntity<>(
                                ResponseBuilder.getSuccessResponse(
                                                service.getByUser(userId)),
                                HttpStatus.OK);
        }

        @DeleteMapping("/delete/{id}")
        public ResponseEntity<Object> delete(
                        @PathVariable Long id) {

                service.delete(id);

                return new ResponseEntity<>(
                                ResponseBuilder.getSuccessResponse(
                                                "Deleted Successfully"),
                                HttpStatus.OK);
        }

}
