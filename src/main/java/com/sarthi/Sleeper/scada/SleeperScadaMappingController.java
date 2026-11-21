package com.sarthi.Sleeper.scada;

import com.sarthi.entity.UserMaster;
import com.sarthi.util.ResponseBuilder;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;

/**
 * Sleeper SCADA topic setup. Each plant row gives the gateway codes; the 9 topics per line are
 * SPLR/&lt;vendorShortName&gt;/&lt;plantShortName&gt;/&lt;lineId&gt;/&lt;STAGE&gt;.
 */
@RestController
@RequestMapping("/api/sleeper/scada-mapping")
@RequiredArgsConstructor
public class SleeperScadaMappingController {

    /** Production stages published by the gateway (same order as the gateway script). */
    public static final List<String> STAGES = List.of(
            "BATCHING", "CHAMBERS", "FLEXURALBEAM", "MORTARCUBE", "SBT",
            "STEAMCUBE", "TENSIONING", "VIBRATOR", "WATERCUBE");
    private static final String TOPIC_ROOT = "SPLR";

    private final SleeperScadaPlantMappingRepository repository;

    /** Active mappings, each with its lines and generated topics. */
    @GetMapping
    public ResponseEntity<Object> list() {
        List<Map<String, Object>> out = new ArrayList<>();
        for (SleeperScadaPlantMapping m : repository.findByActiveTrueOrderByVendorShortNameAscPlantShortNameAsc()) {
            out.add(view(m));
        }
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("stages", STAGES);
        body.put("mappings", out);
        return ResponseEntity.ok(ResponseBuilder.getSuccessResponse(body));
    }

    @PostMapping
    public ResponseEntity<Object> create(@RequestBody Map<String, Object> dto) {
        SleeperScadaPlantMapping m = new SleeperScadaPlantMapping();
        String err = apply(m, dto);
        if (err != null) return bad(err);
        Optional<SleeperScadaPlantMapping> dup = repository
                .findByVendorShortNameIgnoreCaseAndPlantShortNameIgnoreCase(m.getVendorShortName(), m.getPlantShortName());
        if (dup.isPresent() && Boolean.TRUE.equals(dup.get().getActive())) {
            return bad(m.getVendorShortName() + " / " + m.getPlantShortName() + " is already set up");
        }
        if (dup.isPresent()) m = reuse(dup.get(), dto);   // re-activate a removed row instead of a duplicate key error
        m.setActive(true);
        m.setCreatedAt(m.getCreatedAt() != null ? m.getCreatedAt() : LocalDateTime.now());
        m.setCreatedBy(m.getCreatedBy() != null ? m.getCreatedBy() : currentUserId());
        return ResponseEntity.ok(ResponseBuilder.getSuccessResponse(view(repository.save(m))));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Object> update(@PathVariable Long id, @RequestBody Map<String, Object> dto) {
        SleeperScadaPlantMapping m = repository.findById(id).orElse(null);
        if (m == null) return bad("Mapping not found");
        String err = apply(m, dto);
        if (err != null) return bad(err);
        Optional<SleeperScadaPlantMapping> dup = repository
                .findByVendorShortNameIgnoreCaseAndPlantShortNameIgnoreCase(m.getVendorShortName(), m.getPlantShortName());
        if (dup.isPresent() && !dup.get().getId().equals(id)) {
            return bad(m.getVendorShortName() + " / " + m.getPlantShortName() + " is already set up");
        }
        m.setUpdatedAt(LocalDateTime.now());
        m.setUpdatedBy(currentUserId());
        return ResponseEntity.ok(ResponseBuilder.getSuccessResponse(view(repository.save(m))));
    }

    /** Soft delete: the row is kept (history) but no longer listed. */
    @DeleteMapping("/{id}")
    public ResponseEntity<Object> delete(@PathVariable Long id) {
        SleeperScadaPlantMapping m = repository.findById(id).orElse(null);
        if (m == null) return bad("Mapping not found");
        m.setActive(false);
        m.setUpdatedAt(LocalDateTime.now());
        m.setUpdatedBy(currentUserId());
        repository.save(m);
        return ResponseEntity.ok(ResponseBuilder.getSuccessResponse("Removed"));
    }

    // ---------- helpers ----------

    private SleeperScadaPlantMapping reuse(SleeperScadaPlantMapping old, Map<String, Object> dto) {
        apply(old, dto);
        old.setUpdatedAt(LocalDateTime.now());
        old.setUpdatedBy(currentUserId());
        return old;
    }

    /** Validates and copies fields. Codes are upper case, letters / digits / - / _ only (they go into topics). */
    private static String apply(SleeperScadaPlantMapping m, Map<String, Object> dto) {
        String vendor = code(dto.get("vendorShortName"));
        String plant = code(dto.get("plantShortName"));
        if (vendor == null) return "Vendor short name is required (letters, digits, - or _), e.g. MBSL";
        if (plant == null) return "Plant short name is required (letters, digits, - or _), e.g. MBKRSA";
        List<String> lines = new ArrayList<>();
        for (String l : String.valueOf(dto.getOrDefault("lineIds", "")).split("[,\\s]+")) {
            String c = code(l);
            if (c != null && !lines.contains(c)) lines.add(c);
        }
        if (lines.isEmpty()) return "Enter at least one line ID, e.g. U1";
        m.setVendorShortName(vendor);
        m.setPlantShortName(plant);
        m.setLineIds(String.join(",", lines));
        m.setVendorName(text(dto.get("vendorName")));
        m.setPlantName(text(dto.get("plantName")));
        m.setPlantId(text(dto.get("plantId")));
        return null;
    }

    private static Map<String, Object> view(SleeperScadaPlantMapping m) {
        Map<String, Object> v = new LinkedHashMap<>();
        v.put("id", m.getId());
        v.put("vendorShortName", m.getVendorShortName());
        v.put("vendorName", m.getVendorName());
        v.put("plantShortName", m.getPlantShortName());
        v.put("plantName", m.getPlantName());
        v.put("plantId", m.getPlantId());
        List<String> lines = Arrays.asList(m.getLineIds().split(","));
        v.put("lineIds", lines);
        Map<String, List<String>> topics = new LinkedHashMap<>();
        for (String line : lines) {
            String base = String.join("/", TOPIC_ROOT, m.getVendorShortName(), m.getPlantShortName(), line);
            topics.put(line, STAGES.stream().map(s -> base + "/" + s).toList());
        }
        v.put("topics", topics);
        return v;
    }

    private static String code(Object o) {
        if (o == null) return null;
        String s = o.toString().trim().toUpperCase();
        return s.matches("[A-Z0-9_-]{1,20}") ? s : null;
    }

    private static String text(Object o) {
        if (o == null) return null;
        String s = o.toString().trim();
        return s.isEmpty() ? null : s;
    }

    private static String currentUserId() {
        try {
            Authentication a = SecurityContextHolder.getContext().getAuthentication();
            if (a != null && a.getPrincipal() instanceof UserMaster u && u.getUserId() != null) return String.valueOf(u.getUserId());
        } catch (Exception ignored) { }
        return null;
    }

    private static ResponseEntity<Object> bad(String msg) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", msg, "responseStatus", Map.of("message", msg)));
    }
}
