package com.sarthi.superadmin.dpchange;

import com.sarthi.entity.UserMaster;
import com.sarthi.superadmin.SuperAdminTxnService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.NoSuchElementException;
import java.util.function.Supplier;

/** Super Admin – temporary DP change of a PO serial (auto-reverted after 2 hours). User always comes from the JWT. */
@Slf4j
@RestController
@RequestMapping("/api/super-admin/dp-change")
@RequiredArgsConstructor
public class DpChangeController {

    private final DpChangeService service;
    private final SuperAdminTxnService superAdminTxnService;   // reuses the Super Admin role check

    /** PO dropdown: searchable (PO no., vendor name / code), paged for infinite scroll. */
    @GetMapping("/pos")
    public ResponseEntity<Object> pos(@RequestParam(required = false) String q,
                                      @RequestParam(defaultValue = "0") int page,
                                      @RequestParam(defaultValue = "50") int size) {
        return guarded(() -> service.listPos(q, page, size));
    }

    /** PO summary + serials with DP / Ext. DP and any active temporary change. */
    @GetMapping("/po")
    public ResponseEntity<Object> po(@RequestParam String poNo) {
        return guarded(() -> service.getPo(poNo));
    }

    /** Body: { "poNo": "...", "srNo": "001", "newDp": "2026-11-30", "remarks": "..." } */
    @PostMapping("/apply")
    public ResponseEntity<Object> apply(@RequestBody Map<String, String> body) {
        return guarded(() -> service.apply(body.get("poNo"), body.get("srNo"), body.get("newDp"), body.get("remarks"), currentUser()));
    }

    @PostMapping("/{id}/revert")
    public ResponseEntity<Object> revert(@PathVariable long id) {
        return guarded(() -> service.revertNow(id, currentUser()));
    }

    @GetMapping("/active")
    public ResponseEntity<Object> active() {
        return guarded(() -> service.activeChanges(null));
    }

    @GetMapping("/history")
    public ResponseEntity<Object> history(@RequestParam(required = false) String poNo,
                                          @RequestParam(defaultValue = "0") int page,
                                          @RequestParam(defaultValue = "20") int size) {
        return guarded(() -> service.history(poNo, page, size));
    }

    private ResponseEntity<Object> guarded(Supplier<Object> action) {
        UserMaster user = currentUser();
        if (user == null) return error(HttpStatus.UNAUTHORIZED, "Please log in again.");
        if (!superAdminTxnService.isSuperAdmin(user)) return error(HttpStatus.FORBIDDEN, "Only Super Admin can use this screen.");
        try {
            return ResponseEntity.ok(Map.of("success", true, "data", action.get()));
        } catch (IllegalArgumentException e) {
            return error(HttpStatus.BAD_REQUEST, e.getMessage());
        } catch (NoSuchElementException e) {
            return error(HttpStatus.NOT_FOUND, e.getMessage());
        } catch (IllegalStateException e) {
            return error(HttpStatus.CONFLICT, e.getMessage());
        } catch (Exception e) {
            log.error("DP change failed", e);
            return error(HttpStatus.INTERNAL_SERVER_ERROR, "Something went wrong. Nothing was changed.");
        }
    }

    private static ResponseEntity<Object> error(HttpStatus s, String m) {
        return ResponseEntity.status(s).body(Map.of("success", false, "message", m == null ? "Error" : m));
    }

    private static UserMaster currentUser() {
        try {
            Authentication a = SecurityContextHolder.getContext().getAuthentication();
            if (a != null && a.getPrincipal() instanceof UserMaster u) return u;
        } catch (Exception ignored) { }
        return null;
    }
}
