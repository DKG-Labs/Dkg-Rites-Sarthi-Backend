package com.sarthi.SRailPad.controller;

import com.sarthi.SRailPad.dto.RailpadProcessIcEditDTO;
import com.sarthi.SRailPad.service.RailpadProcessIcEditService;
import com.sarthi.SRailPad.service.RailpadProcessIcSaveChangesService;
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
@RequestMapping("/api/railpad-process-ic")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class RailpadProcessIcController {

    private final RailpadProcessIcSaveChangesService saveChangesService;
    private final RailpadProcessIcEditService editService;

    // ─── SAVE CHANGES (Draft) ─────────────────────────────────────────────────

    @GetMapping("/save-changes/{icNumber}")
    public ResponseEntity<Object> getSaveChanges(@PathVariable String icNumber) {
        log.info("GET Process IC Save Changes for: {}", icNumber);
        RailpadProcessIcEditDTO dto = saveChangesService.getByIcNumber(icNumber);
        return new ResponseEntity<>(ResponseBuilder.getSuccessResponse(dto), HttpStatus.OK);
    }

    @PostMapping("/save-changes")
    public ResponseEntity<Object> postSaveChanges(@RequestBody RailpadProcessIcEditDTO dto) {
        log.info("POST Process IC Save Changes for: {}", dto.getIcNumber());
        try {
            RailpadProcessIcEditDTO saved = saveChangesService.saveOrUpdate(dto);
            return new ResponseEntity<>(ResponseBuilder.getSuccessResponse(saved), HttpStatus.OK);
        } catch (Exception e) {
            log.error("Error saving Process IC Save Changes: ", e);
            throw new BusinessException(new ErrorDetails(
                    AppConstant.ERROR_CODE_INTERNAL, AppConstant.ERROR_TYPE_CODE_INTERNAL,
                    AppConstant.ERROR_TYPE_ERROR, "Failed to save process IC changes."));
        }
    }

    // ─── E-SIGN / FINAL SIGNED DATA ───────────────────────────────────────────

    @GetMapping("/edit/{icNumber}")
    public ResponseEntity<Object> getEdit(@PathVariable String icNumber) {
        log.info("GET Process IC Edit (e-sign) for: {}", icNumber);
        RailpadProcessIcEditDTO dto = editService.getByIcNumber(icNumber);
        return new ResponseEntity<>(ResponseBuilder.getSuccessResponse(dto), HttpStatus.OK);
    }

    @PostMapping("/edit")
    public ResponseEntity<Object> postEdit(@RequestBody RailpadProcessIcEditDTO dto) {
        log.info("POST Process IC Edit (e-sign) for: {}", dto.getIcNumber());
        try {
            RailpadProcessIcEditDTO saved = editService.saveOrUpdate(dto);
            return new ResponseEntity<>(ResponseBuilder.getSuccessResponse(saved), HttpStatus.OK);
        } catch (Exception e) {
            log.error("Error saving Process IC Edit (e-sign): ", e);
            throw new BusinessException(new ErrorDetails(
                    AppConstant.ERROR_CODE_INTERNAL, AppConstant.ERROR_TYPE_CODE_INTERNAL,
                    AppConstant.ERROR_TYPE_ERROR, "Failed to save process IC e-sign data."));
        }
    }
}
