package com.sarthi.superadmin;

import com.sarthi.entity.UserMaster;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.NoSuchElementException;

/**
 * Super Admin – Cancel Last Transaction (ERC / Sleeper / Rail Pad).
 * The acting user always comes from the JWT (never from the request body) and must hold the "Super Admin" role.
 */
@Slf4j
@RestController
@RequestMapping("/api/super-admin/transactions")
@RequiredArgsConstructor
public class SuperAdminTxnController {

    private final SuperAdminTxnService service;

    /** All transactions of a call (oldest first); only the latest is deletable. */
    @GetMapping
    public ResponseEntity<Object> list(@RequestParam String callNo) {
        return guarded(() -> service.listTransactions(callNo));
    }

    /** Delete the latest transaction of the call. Body: { "callNo": "...", "remarks": "..." } */
    @DeleteMapping("/{transitionId}")
    public ResponseEntity<Object> deleteLatest(@PathVariable long transitionId, @RequestBody Map<String, String> body) {
        return guarded(() -> service.deleteLatest(body.get("callNo"), transitionId, body.get("remarks"), currentUser()));
    }

    /** Past cancellations by Super Admins (paginated, optional call no. filter). */
    @GetMapping("/history")
    public ResponseEntity<Object> history(@RequestParam(required = false) String callNo,
                                          @RequestParam(defaultValue = "0") int page,
                                          @RequestParam(defaultValue = "20") int size) {
        return guarded(() -> service.history(callNo, page, size));
    }

    // ───────────────────────────── helpers ─────────────────────────────

    private ResponseEntity<Object> guarded(java.util.function.Supplier<Object> action) {
        UserMaster user = currentUser();
        if (user == null) return error(HttpStatus.UNAUTHORIZED, "Please log in again.");
        if (!service.isSuperAdmin(user)) return error(HttpStatus.FORBIDDEN, "Only Super Admin can use this screen.");
        try {
            return ResponseEntity.ok(Map.of("success", true, "data", action.get()));
        } catch (IllegalArgumentException e) {
            return error(HttpStatus.BAD_REQUEST, e.getMessage());
        } catch (NoSuchElementException e) {
            return error(HttpStatus.NOT_FOUND, e.getMessage());
        } catch (IllegalStateException e) {
            return error(HttpStatus.CONFLICT, e.getMessage());
        } catch (Exception e) {
            log.error("Super Admin transaction API failed", e);
            return error(HttpStatus.INTERNAL_SERVER_ERROR, "Something went wrong. Nothing was deleted.");
        }
    }

    private static ResponseEntity<Object> error(HttpStatus status, String message) {
        return ResponseEntity.status(status).body(Map.of("success", false, "message", message == null ? "Error" : message));
    }

    private static UserMaster currentUser() {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.getPrincipal() instanceof UserMaster u) return u;
        } catch (Exception ignored) { }
        return null;
    }
}
