package com.sarthi.service;

import com.sarthi.dto.reports.InspectionCallsReportDto;
import com.sarthi.entity.UserMaster;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Restricts the CM dashboard call lists (ERC / Sleeper / Rail Pad, incl. overdue) to the logged-in
 * Controlling Manager: only calls whose IE is mapped to that CM (the row's cmName code = CM's employee code).
 * The user and role come from the JWT login, never from request params.
 * Admin / SBU Head / Railway Board and every other role keep seeing all rows (unchanged behaviour).
 */
@Service
public class CmCallScopeService {

    private static final Set<String> CM_ROLES = Set.of("cm", "control manager", "controlling manager");
    private static final Set<String> SEE_ALL_ROLES = Set.of("admin", "super admin", "sbu head", "railway board");

    private final com.sarthi.repository.UserMasterRepository userMasterRepository;

    public CmCallScopeService(com.sarthi.repository.UserMasterRepository userMasterRepository) {
        this.userMasterRepository = userMasterRepository;
    }

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(CmCallScopeService.class);

    /**
     * Runs the DB fetch, scopes it to the logged-in CM and logs the timings, e.g.
     * "CM-DASH /api/sleeper-dashboard/cm-inspection-calls cmUserId=842 db=812ms rows=96 kept=96 total=815ms".
     * Diagnostic only: behaviour is identical to scopeToLoggedInCm(fetch.get()).
     */
    public List<InspectionCallsReportDto> scopeTimed(java.util.function.Supplier<List<InspectionCallsReportDto>> fetch) {
        long t0 = System.nanoTime();
        List<InspectionCallsReportDto> rows = fetch.get();
        long t1 = System.nanoTime();
        List<InspectionCallsReportDto> out = scopeToLoggedInCm(rows);
        long t2 = System.nanoTime();
        String uri = "";
        try {
            var attrs = (org.springframework.web.context.request.ServletRequestAttributes)
                    org.springframework.web.context.request.RequestContextHolder.getRequestAttributes();
            if (attrs != null) uri = attrs.getRequest().getRequestURI();
        } catch (Exception ignored) { }
        log.info("CM-DASH {} cmUserId={} db={}ms rows={} kept={} total={}ms", uri, cmUserIdFilter(),
                (t1 - t0) / 1_000_000, rows == null ? 0 : rows.size(), out == null ? 0 : out.size(), (t2 - t0) / 1_000_000);
        return out;
    }

    public List<InspectionCallsReportDto> scopeToLoggedInCm(List<InspectionCallsReportDto> rows) {
        if (rows == null || rows.isEmpty()) return rows;

        UserMaster user = currentUser();
        if (user == null || !isCmOnly(user)) return rows;

        String myCode = normalize(user.getEmployeeCode());
        if (myCode.isEmpty()) return List.of();

        // cmName may list several CMs ("10723 - A, 11055 - B") when a process call's POI has IEs under different CMs
        List<InspectionCallsReportDto> mine = rows.stream()
                .filter(r -> cmCodesOf(r.getCmName()).contains(myCode))
                .collect(Collectors.toList());

        // Hide other CMs' names: keep only my IEs (one lookup per request) and only my own CM entry
        Set<String> myIeCodes = userMasterRepository.findByControllingManagerId(user.getUserId()).stream()
                .map(u -> normalize(u.getEmployeeCode()))
                .filter(c -> !c.isEmpty())
                .collect(Collectors.toSet());

        for (InspectionCallsReportDto r : mine) {
            r.setCmName(keepEntries(r.getCmName(), Set.of(myCode)));
            String myIes = keepEntries(r.getIeName(), myIeCodes);
            // Safety: never leave the IE column blank
            if (myIes != null && !myIes.isEmpty()) r.setIeName(myIes);
        }
        return mine;
    }

    /** Keeps only the "name - code" entries of a comma-separated list whose code is in allowedCodes. */
    private static String keepEntries(String list, Set<String> allowedCodes) {
        if (list == null || list.isBlank()) return list;
        return Arrays.stream(list.split(","))
                .map(String::trim)
                .filter(e -> !e.isEmpty() && allowedCodes.contains(cmCodeOf(e)))
                .distinct()
                .collect(Collectors.joining(", "));
    }

    /**
     * CM filter for the cm-* SQL queries: the logged-in CM's user id, or 0 = no filter
     * (Admin / SBU Head / Railway Board / other roles). Lets the DB process only that CM's calls.
     */
    public int cmUserIdFilter() {
        UserMaster user = currentUser();
        return (user != null && user.getUserId() != null && isCmOnly(user)) ? user.getUserId() : 0;
    }

    private UserMaster currentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return (auth != null && auth.getPrincipal() instanceof UserMaster um) ? um : null;
    }

    /** A CM who does not also hold a "see everything" role. role_name can hold several comma-separated roles. */
    private boolean isCmOnly(UserMaster user) {
        if (user.getRoleName() == null) return false;
        Set<String> roles = Arrays.stream(user.getRoleName().split(","))
                .map(r -> r.trim().toLowerCase(Locale.ROOT))
                .collect(Collectors.toSet());
        boolean isCm = roles.stream().anyMatch(CM_ROLES::contains);
        boolean seesAll = roles.stream().anyMatch(SEE_ALL_ROLES::contains);
        return isCm && !seesAll;
    }

    /** All CM codes in a (possibly comma-separated) cmName. */
    private static Set<String> cmCodesOf(String cmName) {
        if (cmName == null || cmName.isBlank()) return Set.of();
        return Arrays.stream(cmName.split(","))
                .map(CmCallScopeService::cmCodeOf)
                .filter(c -> !c.isEmpty())
                .collect(Collectors.toSet());
    }

    /** Employee code from "11055 - T VIJAY SHANKAR" or "T Vijay Shankar - 11055" (numeric part preferred). */
    private static String cmCodeOf(String cmName) {
        if (cmName == null) return "";
        String[] parts = cmName.split(" - ");
        for (String p : parts) {
            String t = p.trim();
            if (!t.isEmpty() && t.chars().allMatch(Character::isDigit)) return normalize(t);
        }
        return normalize(parts[0]);
    }

    private static String normalize(String s) {
        return s == null ? "" : s.trim().toLowerCase(Locale.ROOT);
    }
}
