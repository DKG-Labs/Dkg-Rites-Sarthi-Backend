package com.sarthi.service.Impl;

import com.sarthi.dto.IBS.IbsInspectionDto;
import com.sarthi.entity.IBS.IbsCallRegistration;
import com.sarthi.repository.IbsCallRegistrationRepository;
import com.sarthi.service.IbsService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

/**
 * Completed IBS calls screen with normal database paging.
 *
 * 1. ibs_call_registration picks the page (latest row per call, filters, newest first, LIMIT/OFFSET) and the total.
 * 2. Only those calls go through the detail queries (getCompletedIbsCallsFor), so each request works on ~25 calls.
 *
 * Filters: category / product (call-number prefix), IBS status, search (call no or SR no), acknowledged date range.
 */
@Service
@RequiredArgsConstructor
public class IbsCompletedCallsQueryService {

    private static final Map<String, String> CATEGORY_RX = Map.of(
            "ER", "^ER", "EP", "^EP", "EF", "^EF", "SF", "^SF", "RPP", "^RPP", "RPF", "^RPF");
    private static final Map<String, String> PRODUCT_RX = Map.of(
            "ERC", "^E[RPF]", "SLEEPER", "^SF", "RAILPAD", "^RP[PF]");

    private final IbsCallRegistrationRepository registrationRepository;
    private final IbsService ibsService;

    public Map<String, Object> search(int page, int size, String category, String product, String status,
                                      String search, String from, String to) {
        long startNs = System.nanoTime();
        int p = Math.max(page, 1);
        int s = size <= 0 ? 0 : Math.min(size, 500);   // size=0: every matching row (CSV export)

        String prefixRx = has(category) ? CATEGORY_RX.get(category.toUpperCase())
                : has(product) ? PRODUCT_RX.get(product.toUpperCase()) : null;
        String st = has(status) ? status.trim().toUpperCase() : null;
        String q = notBlank(search) ? search.trim() : null;
        LocalDateTime fromTs = notBlank(from) ? LocalDate.parse(from.trim()).atStartOfDay() : null;
        LocalDateTime toTs = notBlank(to) ? LocalDate.parse(to.trim()).plusDays(1).atStartOfDay() : null;

        long total = registrationRepository.countCompleted(prefixRx, st, q, fromTs, toTs);
        List<Long> ids = registrationRepository.findCompletedPageIds(prefixRx, st, q, fromTs, toTs,
                s == 0 ? (int) Math.max(total, 1) : s, s == 0 ? 0 : (p - 1) * s);

        List<IbsInspectionDto> content = List.of();
        if (!ids.isEmpty()) {
            List<IbsCallRegistration> regs = new ArrayList<>(registrationRepository.findAllById(ids));
            regs.sort(Comparator.comparing(IbsCallRegistration::getId).reversed()); // newest first, like findAllCalls()
            content = new ArrayList<>(ibsService.getCompletedIbsCallsFor(regs));
            // keep the page order (newest acknowledgement first)
            content.sort(Comparator.comparing(IbsInspectionDto::getAcknowledgedAt,
                    Comparator.nullsLast(Comparator.reverseOrder())));
        }

        Map<String, Object> out = new LinkedHashMap<>();
        out.put("content", content);
        out.put("total", total);
        out.put("page", p);
        out.put("size", s);
        out.put("totalPages", s == 0 ? 1 : Math.max(1, (int) Math.ceil(total / (double) s)));
        out.put("stats", stats());
        out.put("serverMs", (System.nanoTime() - startNs) / 1_000_000);
        return out;
    }

    private Map<String, Object> stats() {
        Map<String, Object> st = new LinkedHashMap<>();
        long all = 0;
        Map<String, Long> by = new HashMap<>();
        for (Object[] row : registrationRepository.countCompletedByCategory()) {
            long c = ((Number) row[1]).longValue();
            by.put(String.valueOf(row[0]), c);
            all += c;
        }
        st.put("TOTAL", all);
        for (String k : List.of("ER", "EP", "EF", "SF", "RPP", "RPF", "OTHER")) st.put(k, by.getOrDefault(k, 0L));
        return st;
    }

    private static boolean notBlank(String v) { return v != null && !v.isBlank(); }
    private static boolean has(String v) { return notBlank(v) && !"ALL".equalsIgnoreCase(v); }
}
