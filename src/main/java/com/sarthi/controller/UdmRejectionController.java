package com.sarthi.controller;

import com.sarthi.dto.crisDtos.UdmRejectionDTO;
import com.sarthi.service.Impl.UdmRejectionService;
import com.sarthi.service.Impl.UdmRejectionSyncService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/udm-rejections")
@CrossOrigin(origins = "*")
public class UdmRejectionController {

    @Autowired
    private UdmRejectionService udmRejectionService;

    @Autowired
    private UdmRejectionSyncService udmRejectionSyncService;

    /**
     * GET /api/udm-rejections
     * Optional params: rejDate (YYYY-MM-DD), poNo, vendorCode, type (Initial/Warranty)
     */
    @GetMapping
    public ResponseEntity<?> getRejections(
            @RequestParam(required = false) String rejDate,
            @RequestParam(required = false) String poNo,
            @RequestParam(required = false) String vendorCode,
            @RequestParam(required = false) String type) {
        try {
            List<UdmRejectionDTO> rejections = udmRejectionService
                    .getConsigneeRejections(rejDate, poNo, vendorCode, type);
            return ResponseEntity.ok(rejections);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to fetch UDM rejections: " + e.getMessage());
            return ResponseEntity.status(500).body(error);
        }
    }

    /**
     * POST /api/udm-rejections/sync?date=YYYY-MM-DD
     * Manually trigger a sync for a specific date (backfill).
     */
    @PostMapping("/sync")
    public ResponseEntity<?> syncDate(@RequestParam(required = false) String date) {
        try {
            LocalDate syncDate = (date != null && !date.isBlank())
                    ? LocalDate.parse(date)
                    : LocalDate.now().minusDays(1);
            int count = udmRejectionSyncService.syncForDate(syncDate);
            Map<String, Object> result = new HashMap<>();
            result.put("date", syncDate.toString());
            result.put("recordsSaved", count);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Sync failed: " + e.getMessage());
            return ResponseEntity.status(500).body(error);
        }
    }

    @GetMapping("/summary")
    public ResponseEntity<?> getSummary(@RequestParam(required = false) String rejDate) {
        try {
            List<UdmRejectionDTO> all = udmRejectionService
                    .getConsigneeRejections(rejDate, null, null, null);

            long total    = all.size();
            long initial  = all.stream().filter(r -> "Initial".equalsIgnoreCase(r.getTypeOfRejection())).count();
            long warranty = all.stream().filter(r -> "Warranty".equalsIgnoreCase(r.getTypeOfRejection())).count();
            long erc      = all.stream().filter(r -> r.getItemDescription() != null &&
                    r.getItemDescription().toUpperCase().contains("ERC")).count();
            long sleeper  = all.stream().filter(r -> r.getItemDescription() != null &&
                    r.getItemDescription().toUpperCase().contains("SLEEPER")).count();
            long railPad  = all.stream().filter(r -> r.getItemDescription() != null &&
                    (r.getItemDescription().toUpperCase().contains("RAIL PAD") ||
                     r.getItemDescription().toUpperCase().contains("RAILPAD"))).count();

            Map<String, Long> summary = new HashMap<>();
            summary.put("total",    total);
            summary.put("initial",  initial);
            summary.put("warranty", warranty);
            summary.put("erc",      erc);
            summary.put("sleeper",  sleeper);
            summary.put("railPad",  railPad);

            return ResponseEntity.ok(summary);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to fetch summary: " + e.getMessage());
            return ResponseEntity.status(500).body(error);
        }
    }
}
