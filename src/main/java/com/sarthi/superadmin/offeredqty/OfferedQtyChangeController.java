package com.sarthi.superadmin.offeredqty;

import com.sarthi.entity.UserMaster;
import com.sarthi.superadmin.SuperAdminTxnService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.function.Supplier;

/** Super Admin – change offered qty of ERC calls (ER / EP / EF). User always comes from the JWT. */
@Slf4j
@RestController
@RequestMapping("/api/super-admin/offered-qty")
@RequiredArgsConstructor
public class OfferedQtyChangeController {

    private final OfferedQtyChangeService service;
    private final SuperAdminTxnService superAdminTxnService;   // reuses the Super Admin role check

    @GetMapping("/calls")
    public ResponseEntity<Object> calls(@RequestParam String type,
                                        @RequestParam(required = false) String q,
                                        @RequestParam(defaultValue = "0") int page,
                                        @RequestParam(defaultValue = "20") int size) {
        return guarded(() -> service.listCalls(type, q, page, size));
    }

    @GetMapping("/history")
    public ResponseEntity<Object> history(@RequestParam(required = false) String callNo,
                                          @RequestParam(defaultValue = "0") int page,
                                          @RequestParam(defaultValue = "20") int size) {
        return guarded(() -> service.history(callNo, page, size));
    }

    @GetMapping("/{callNo}")
    public ResponseEntity<Object> call(@PathVariable String callNo) {
        return guarded(() -> service.getCall(callNo));
    }

    /** Body: { "items": [ { "rowId": 12, "newQty": 1250 } ], "remarks": "..." } */
    @SuppressWarnings("unchecked")
    @PutMapping("/{callNo}")
    public ResponseEntity<Object> update(@PathVariable String callNo, @RequestBody Map<String, Object> body) {
        return guarded(() -> service.update(callNo, (List<Map<String, Object>>) body.get("items"),
                (String) body.get("remarks"), currentUser()));
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
            log.error("Offered qty change failed", e);
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
