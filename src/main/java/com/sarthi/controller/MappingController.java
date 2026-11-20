package com.sarthi.controller;

import com.sarthi.dto.ProcessIeMappingRequestDto;
import com.sarthi.entity.IePincodePoiMapping;
import com.sarthi.entity.UserMaster;
import com.sarthi.repository.IePincodePoiMappingRepository;
import com.sarthi.repository.PoiProcessIeMappingRepository;
import com.sarthi.repository.UserMasterRepository;
import com.sarthi.util.ResponseBuilder;
import com.sarthi.service.MappingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/mapping")
@CrossOrigin("*")
public class MappingController {

    @Autowired
    private MappingService mappingService;

    @Autowired
    private IePincodePoiMappingRepository iePincodePoiMappingRepository;

    @Autowired
    private PoiProcessIeMappingRepository poiProcessIeMappingRepository;

    @Autowired(required = false)
    private UserMasterRepository userMasterRepository;

    @PostMapping("/processIe")
    public ResponseEntity<Object> createMappingProcessIe(
            @RequestBody ProcessIeMappingRequestDto dto,
            @RequestParam("userId") Long userId,
            @RequestParam("createdBy") String createdBy) {

        try {
            Object obj = mappingService.mapProcessIe(userId, dto, createdBy);
            return ResponseEntity.ok(ResponseBuilder.getSuccessResponse(obj));
        } catch (IllegalArgumentException ex) {
            // validation (e.g. duplicate IE + POI mapping): show the message as-is
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                    ResponseBuilder.getErrorResponse(new com.sarthi.exception.ErrorDetails(
                            HttpStatus.BAD_REQUEST.value(),
                            1000,
                            "error",
                            ex.getMessage()
                    ))
            );
        } catch (org.springframework.dao.DataIntegrityViolationException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                    ResponseBuilder.getErrorResponse(new com.sarthi.exception.ErrorDetails(
                            HttpStatus.BAD_REQUEST.value(),
                            1000,
                            "error",
                            "Mapping already exists for the selected User and POI."
                    ))
            );
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    ResponseBuilder.getErrorResponse(new com.sarthi.exception.ErrorDetails(
                            HttpStatus.INTERNAL_SERVER_ERROR.value(),
                            5000,
                            "error",
                            "Failed to save mapping: " + ex.getMessage()
                    ))
            );
        }
    }


    @GetMapping("/all")
    public ResponseEntity<Object> getAllMappings() {
        try {
            return ResponseEntity.ok(ResponseBuilder.getSuccessResponse(mappingService.getAllMappings()));
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    ResponseBuilder.getErrorResponse(new com.sarthi.exception.ErrorDetails(
                            HttpStatus.INTERNAL_SERVER_ERROR.value(),
                            5000,
                            "error",
                            "Failed to retrieve mappings: " + ex.getMessage()
                    ))
            );
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Object> deleteMapping(@PathVariable("id") String id) {
        try {
            mappingService.deleteMapping(id);
            return ResponseEntity.ok(ResponseBuilder.getSuccessResponse("Mapping deleted successfully"));
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    ResponseBuilder.getErrorResponse(new com.sarthi.exception.ErrorDetails(
                            HttpStatus.INTERNAL_SERVER_ERROR.value(),
                            5000,
                            "error",
                            "Failed to delete mapping: " + ex.getMessage()
                    ))
            );
        }
    }

    @PostMapping("/cm-ie")
    public ResponseEntity<Object> saveCmIeMapping(@RequestBody Map<String, Object> body) {
        try {
            Integer cmUserId = (Integer) body.get("cmUserId");
            @SuppressWarnings("unchecked")
            List<Integer> ieUserIds = (List<Integer>) body.get("ieUserIds");
            String result = mappingService.saveCmIeMapping(cmUserId, ieUserIds);
            return ResponseEntity.ok(ResponseBuilder.getSuccessResponse(result));
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    ResponseBuilder.getErrorResponse(new com.sarthi.exception.ErrorDetails(
                            HttpStatus.INTERNAL_SERVER_ERROR.value(), 5000, "error",
                            "Failed to save CM-IE mapping: " + ex.getMessage())));
        }
    }

    @GetMapping("/check-duplicate")
    public ResponseEntity<Object> checkDuplicateErcMain(
            @RequestParam String empCode,
            @RequestParam String poiCode,
            @RequestParam(required = false, defaultValue = "") String ieType) {
        
        List<IePincodePoiMapping> primaryMappings = iePincodePoiMappingRepository.findPrimaryMappingsByPoiCode(poiCode);
        if (primaryMappings != null && !primaryMappings.isEmpty()) {
            for (IePincodePoiMapping m : primaryMappings) {
                String existingEmp = m.getEmployeeCode();
                if (existingEmp != null && !existingEmp.equalsIgnoreCase(empCode)) {
                    String ieName = existingEmp;
                    if (userMasterRepository != null) {
                        try {
                            UserMaster u = userMasterRepository.findByEmployeeCode(existingEmp);
                            if (u != null && u.getUsername() != null) {
                                ieName = existingEmp + " - " + u.getUsername();
                            }
                        } catch (Exception ignored) {}
                    }
                    return ResponseEntity.ok(Map.of(
                            "exists", true,
                            "message", "This POI is already mapped to IE (" + ieName + ") for RM/Final. Only one IE can be mapped."
                    ));
                }
            }
        }

        boolean exists = iePincodePoiMappingRepository
                .existsByPoiCodeAndEmployeeCodeAndIeType(poiCode, empCode, ieType);
        if (exists) {
            return ResponseEntity.ok(Map.of(
                    "exists", true,
                    "message", "This POI is already mapped to this IE."
            ));
        }
        return ResponseEntity.ok(Map.of("exists", false));
    }

    @GetMapping("/check-duplicate-process")
    public ResponseEntity<Object> checkDuplicateErcProcess(
            @RequestParam String empCode,
            @RequestParam String poiCode) {
        boolean exists = poiProcessIeMappingRepository
                .existsByPoiCodeAndEmployeeCode(poiCode, empCode);
        return ResponseEntity.ok(Map.of("exists", exists));
    }

    @DeleteMapping("/cm-ie/{ieUserId}")
    public ResponseEntity<Object> removeCmIeMapping(@PathVariable Integer ieUserId) {
        try {
            String result = mappingService.removeCmIeMapping(ieUserId);
            return ResponseEntity.ok(ResponseBuilder.getSuccessResponse(result));
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    ResponseBuilder.getErrorResponse(new com.sarthi.exception.ErrorDetails(
                            HttpStatus.INTERNAL_SERVER_ERROR.value(), 5000, "error",
                            "Failed to remove CM-IE mapping: " + ex.getMessage())));
        }
    }

}
