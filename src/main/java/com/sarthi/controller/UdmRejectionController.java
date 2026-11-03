package com.sarthi.controller;

import com.sarthi.dto.crisDtos.UdmRejectionDTO;
import com.sarthi.service.Impl.UdmRejectionService;
import com.sarthi.service.Impl.UdmRejectionSyncService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.sarthi.entity.UdmSyncLog;
import com.sarthi.entity.UserMaster;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;

import java.util.HashMap;
import java.util.LinkedHashMap;
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
    public ResponseEntity<?> syncDate(@RequestParam(required = false) String date,
                                      @AuthenticationPrincipal Object principal) {
        try {
            LocalDate syncDate = (date != null && !date.isBlank())
                    ? LocalDate.parse(date)
                    : LocalDate.now().minusDays(1);
            UdmSyncLog log = udmRejectionSyncService.runSync(syncDate, UdmSyncLog.TRIGGER_MANUAL, describeUser(principal));

            // "date" and "recordsSaved" kept as before; status/reason added so the screen can show failures
            Map<String, Object> result = new HashMap<>(toJson(log));
            result.put("date", syncDate.toString());
            result.put("recordsSaved", nz(log.getRecordsNew()) + nz(log.getRecordsUpdated()));
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Sync failed: " + e.getMessage());
            return ResponseEntity.status(500).body(error);
        }
    }

    /**
     * GET /api/udm-rejections/sync-history?limit=50
     * Latest sync runs (scheduler and manual), newest first, with counts and the failure reason.
     */
    @GetMapping("/sync-history")
    public ResponseEntity<?> getSyncHistory(@RequestParam(defaultValue = "50") int limit) {
        try {
            List<Map<String, Object>> rows = udmRejectionSyncService.getSyncHistory(limit).stream()
                    .map(UdmRejectionController::toJson)
                    .toList();
            return ResponseEntity.ok(rows);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to fetch sync history: " + e.getMessage());
            return ResponseEntity.status(500).body(error);
        }
    }

    private static Map<String, Object> toJson(UdmSyncLog l) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", l.getId());
        m.put("syncDate", l.getSyncDate() != null ? l.getSyncDate().toString() : null);
        m.put("triggerType", l.getTriggerType());
        m.put("triggeredBy", l.getTriggeredBy());
        m.put("startedAt", withZone(l.getStartedAt()));
        m.put("finishedAt", withZone(l.getFinishedAt()));
        m.put("durationMs", l.getDurationMs());
        m.put("status", l.getStatus());
        m.put("recordsReceived", l.getRecordsReceived());
        m.put("recordsNew", l.getRecordsNew());
        m.put("recordsUpdated", l.getRecordsUpdated());
        m.put("recordsFailed", l.getRecordsFailed());
        m.put("errorMessage", l.getErrorMessage());
        return m;
    }

    /** Server-local time with its offset (e.g. +05:30), so the browser can show it in IST. */
    private static String withZone(LocalDateTime t) {
        return t == null ? null : t.atZone(ZoneId.systemDefault()).toOffsetDateTime().toString();
    }

    private static String describeUser(Object principal) {
        if (principal instanceof UserMaster u) {
            String name = u.getFullName() != null && !u.getFullName().isBlank() ? u.getFullName() : u.getUsername();
            String code = u.getEmployeeCode();
            return code != null && !code.isBlank() && !code.equals(name) ? name + " (" + code + ")" : name;
        }
        if (principal instanceof org.springframework.security.core.userdetails.UserDetails d) {
            return d.getUsername();
        }
        return null;
    }

    private static int nz(Integer v) { return v != null ? v : 0; }

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
