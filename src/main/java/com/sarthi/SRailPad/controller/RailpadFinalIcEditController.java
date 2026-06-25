package com.sarthi.SRailPad.controller;

import com.sarthi.SRailPad.dto.RailpadFinalIcEditDTO;
import com.sarthi.SRailPad.service.RailpadFinalIcEditService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.sarthi.util.ResponseBuilder;
import com.sarthi.exception.BusinessException;
import com.sarthi.exception.ErrorDetails;
import com.sarthi.constant.AppConstant;
import org.springframework.http.HttpStatus;

@RestController
@RequestMapping("/api/railpad-final-ic-edit")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class RailpadFinalIcEditController {

    private final RailpadFinalIcEditService service;

    @GetMapping("/{icNumber}")
    public ResponseEntity<Object> getByIcNumber(@PathVariable String icNumber) {
        log.info("REST request to get Railpad Final IC Edit for IC (path): {}", icNumber);
        RailpadFinalIcEditDTO dto = service.getByIcNumber(icNumber);
        return new ResponseEntity<>(ResponseBuilder.getSuccessResponse(dto), HttpStatus.OK);
    }

    @GetMapping
    public ResponseEntity<Object> getByIcNumberQuery(@RequestParam String icNumber) {
        log.info("REST request to get Railpad Final IC Edit for IC (query): {}", icNumber);
        RailpadFinalIcEditDTO dto = service.getByIcNumber(icNumber);
        return new ResponseEntity<>(ResponseBuilder.getSuccessResponse(dto), HttpStatus.OK);
    }

    @PostMapping
    public ResponseEntity<Object> saveOrUpdate(@RequestBody RailpadFinalIcEditDTO dto) {
        log.info("REST request to save/update Railpad Final IC Edit for IC: {}", dto.getIcNumber());
        try {
            RailpadFinalIcEditDTO saved = service.saveOrUpdate(dto);
            return new ResponseEntity<>(ResponseBuilder.getSuccessResponse(saved), HttpStatus.OK);
        } catch (Exception e) {
            log.error("Error saving Railpad Final IC Edit: ", e);
            throw new BusinessException(new ErrorDetails(AppConstant.ERROR_CODE_INTERNAL, AppConstant.ERROR_TYPE_CODE_INTERNAL, AppConstant.ERROR_TYPE_ERROR, "Failed to save data."));
        }
    }
}
