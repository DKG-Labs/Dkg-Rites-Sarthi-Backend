package com.sarthi.Sleeper.controller;

import com.sarthi.Sleeper.dto.ProductionDeclaration.ProductionDeclarationRequestDto;
import com.sarthi.Sleeper.service.ProductionDeclarationService;
import com.sarthi.util.ResponseBuilder;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/production-declaration")
@RequiredArgsConstructor
public class productionDeclarationController {

    private final ProductionDeclarationService service;


        @PostMapping("/create")
        public ResponseEntity<Object> create(
                @RequestBody ProductionDeclarationRequestDto dto) {

            return new ResponseEntity<>(
                    ResponseBuilder.getSuccessResponse(
                            service.create(dto)),
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
