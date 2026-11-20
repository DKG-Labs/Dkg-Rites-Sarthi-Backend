package com.sarthi.service.Impl;

import com.sarthi.dto.IBS.IbsInspectionDto;
import com.sarthi.service.IbsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Completed IBS calls screen: filter, sort and page on the server instead of shipping every call to the browser.
 *
 * Building the full list (getCompletedIbsCalls) is heavy, so the result is kept in memory for CACHE_TTL_MS and
 * shared by all users. It is dropped after an acknowledgement / revert, and the screen's Refresh forces a rebuild.
 * Filter rules mirror IbsInspectionDataView.jsx so counts stay the same as before.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class IbsCompletedCallsQueryService {

    private static final long CACHE_TTL_MS = 5 * 60 * 1000L;

    private final IbsService ibsService;

    private volatile List<IbsInspectionDto> cached;
    private volatile long cachedAt;
    private final Object lock = new Object();

    public void invalidate() {
        cached = null;
    }

    private List<IbsInspectionDto> all(boolean refresh) {
        List<IbsInspectionDto> c = cached;
        if (!refresh && c != null && System.currentTimeMillis() - cachedAt < CACHE_TTL_MS) return c;
        synchronized (lock) {
            c = cached;
            if (!refresh && c != null && System.currentTimeMillis() - cachedAt < CACHE_TTL_MS) return c;
            long t = System.currentTimeMillis();
            List<IbsInspectionDto> fresh = ibsService.getCompletedIbsCalls();
            cached = fresh == null ? List.of() : List.copyOf(fresh);
            cachedAt = System.currentTimeMillis();
            log.info("[IBS completed] rebuilt {} calls in {} ms", cached.size(), cachedAt - t);
            return cached;
        }
    }

    public Map<String, Object> search(int page, int size, String category, String product, String region,
                                      String status, String search, String dateField, String from, String to,
                                      String sortKey, String sortDir, boolean refresh) {
        List<IbsInspectionDto> source = all(refresh);

        List<IbsInspectionDto> rows = source.stream()
                .filter(d -> matches(d, category, product, region, status, search, dateField, from, to))
                .collect(Collectors.toCollection(ArrayList::new));

        Comparator<IbsInspectionDto> cmp = comparator(sortKey);
        if (cmp != null) {
            if ("desc".equalsIgnoreCase(sortDir)) cmp = cmp.reversed();
            rows.sort(cmp);
        }

        int total = rows.size();
        List<IbsInspectionDto> content;
        if (size <= 0) {                       // size=0: everything (CSV export)
            content = rows;
        } else {
            int p = Math.max(page, 1);
            int start = Math.min((p - 1) * size, total);
            content = rows.subList(start, Math.min(start + size, total));
        }

        Map<String, Object> out = new LinkedHashMap<>();
        out.put("content", content);
        out.put("total", total);
        out.put("page", Math.max(page, 1));
        out.put("size", size);
        out.put("totalPages", size <= 0 ? 1 : Math.max(1, (int) Math.ceil(total / (double) size)));
        out.put("stats", stats(source));
        out.put("cachedAt", cachedAt);
        return out;
    }

    // ---------- filter rules (same as the screen) ----------

    static String category(IbsInspectionDto d) {
        String call = up(d.getCallNumber()), ic = up(d.getIcNumber());
        if (call.startsWith("RPP") || ic.contains("RPP")) return "RPP";
        if (call.startsWith("RPF") || ic.contains("RPF")) return "RPF";
        if (call.startsWith("SF") || ic.contains("SF")) return "SF";
        if (call.startsWith("ER") || ic.contains("ER")) return "ER";
        if (call.startsWith("EP") || ic.contains("EP")) return "EP";
        if (call.startsWith("EF") || ic.contains("EF")) return "EF";
        String t = up(d.getTypeOfCall());
        if (t.equals("S")) return "ER";
        if (t.equals("P")) return "EP";
        if (t.equals("F")) return "EF";
        return "OTHER";
    }

    private static final Map<String, String> PRODUCT = Map.of(
            "ER", "ERC", "EP", "ERC", "EF", "ERC", "SF", "SLEEPER", "RPP", "RAILPAD", "RPF", "RAILPAD", "OTHER", "OTHER");

    private static final Map<String, String> LABEL = Map.of(
            "ER", "ERC Raw Material (ER)", "EP", "ERC Process (EP)", "EF", "ERC Final (EF)",
            "SF", "Sleeper Final (SF)", "RPP", "Railpad Process (RPP)", "RPF", "Railpad Final (RPF)");

    static String region(IbsInspectionDto d) {
        String ic = d.getIcNumber(), cs = d.getCaseNumber();
        String code = "";
        if (ic != null && ic.contains("/")) code = ic.split("/")[0].trim().toUpperCase();
        else if (cs != null && !cs.trim().isEmpty()) code = cs.trim().substring(0, 1).toUpperCase();
        return switch (code) { case "C", "W", "N", "E", "S" -> code; default -> code.isEmpty() ? "-" : code; };
    }

    static String statusOf(IbsInspectionDto d) {
        String s = d.getIbsStatus() != null ? d.getIbsStatus() : d.getCallStatus();
        return (s == null || s.isEmpty() ? "SUCCESS" : s).toUpperCase();
    }

    private boolean matches(IbsInspectionDto d, String category, String product, String region, String status,
                            String search, String dateField, String from, String to) {
        String cat = category(d);
        if (has(category) && !cat.equalsIgnoreCase(category)) return false;
        if (has(product) && !PRODUCT.get(cat).equalsIgnoreCase(product)) return false;
        if (has(region) && !region(d).equalsIgnoreCase(region)) return false;
        if (has(status) && !statusOf(d).equalsIgnoreCase(status)) return false;

        if (notBlank(from) || notBlank(to)) {
            String ack = d.getAcknowledgedAt() == null ? null : d.getAcknowledgedAt().toLocalDate().toString();
            String ic = iso(d.getIcDate()), call = iso(d.getCallDate());
            List<String> dates;
            String f = dateField == null ? "ACK" : dateField.toUpperCase();
            switch (f) {
                case "IC" -> dates = Collections.singletonList(ic);
                case "CALL" -> dates = Collections.singletonList(call);
                case "ANY" -> dates = Arrays.asList(ack, ic, call);
                default -> dates = Collections.singletonList(ack != null ? ack : ic != null ? ic : call);
            }
            boolean ok = dates.stream().anyMatch(s -> s != null
                    && (!notBlank(from) || s.compareTo(from) >= 0)
                    && (!notBlank(to) || s.compareTo(to) <= 0));
            if (!ok) return false;
        }

        if (notBlank(search)) {
            String t = search.trim().toLowerCase();
            return java.util.stream.Stream.of(d.getCallNumber(), d.getCaseNumber(), d.getIcNumber(), d.getSrNo(),
                            d.getPlaceOfInspection(), d.getIeEmployeeNumber(), d.getIbsManufacturedCode(),
                            d.getBkNumber(), d.getSetNumber(), LABEL.get(cat), d.getReason())
                    .anyMatch(v -> v != null && v.toLowerCase().contains(t));
        }
        return true;
    }

    private Map<String, Object> stats(List<IbsInspectionDto> source) {
        Map<String, Object> s = new LinkedHashMap<>();
        Map<String, Long> byCat = source.stream().collect(Collectors.groupingBy(
                IbsCompletedCallsQueryService::category, Collectors.counting()));
        s.put("TOTAL", source.size());
        for (String k : List.of("ER", "EP", "EF", "SF", "RPP", "RPF", "OTHER")) s.put(k, byCat.getOrDefault(k, 0L));
        return s;
    }

    // ---------- sorting (same keys as the screen) ----------

    private Comparator<IbsInspectionDto> comparator(String key) {
        if (!notBlank(key)) return null;
        return switch (key) {
            case "srNo" -> numThenText(IbsInspectionDto::getSrNo);
            case "ieEmployeeNumber" -> numThenText(IbsInspectionDto::getIeEmployeeNumber);
            case "category" -> text(IbsCompletedCallsQueryService::category);
            case "callNumber" -> text(IbsInspectionDto::getCallNumber);
            case "caseNumber" -> text(IbsInspectionDto::getCaseNumber);
            case "icNumber" -> text(IbsInspectionDto::getIcNumber);
            case "placeOfInspection" -> text(IbsInspectionDto::getPlaceOfInspection);
            case "callDate" -> text(d -> d.getCallDate() != null ? d.getCallDate().toString()
                    : d.getAcknowledgedAt() != null ? d.getAcknowledgedAt().toString() : iso(d.getIcDate()));
            case "quantityOffered" -> Comparator.comparingDouble(d -> nz(d.getQuantityOffered()));
            case "quantityPassed" -> Comparator.comparingDouble(d -> nz(d.getQuantityPassed()));
            case "quantityRejected" -> Comparator.comparingDouble(d -> nz(d.getQuantityRejected()));
            case "status" -> text(d -> d.getIbsStatus() != null ? d.getIbsStatus() : d.getCallStatus());
            default -> null;
        };
    }

    private static Comparator<IbsInspectionDto> text(Function<IbsInspectionDto, String> f) {
        return Comparator.comparing(d -> { String v = f.apply(d); return v == null ? "" : v.toLowerCase(); });
    }

    private static Comparator<IbsInspectionDto> numThenText(Function<IbsInspectionDto, String> f) {
        return (a, b) -> {
            Double x = num(f.apply(a)), y = num(f.apply(b));
            if (x != null && y != null) return Double.compare(x, y);
            String s = f.apply(a) == null ? "" : f.apply(a).toLowerCase();
            String t = f.apply(b) == null ? "" : f.apply(b).toLowerCase();
            return s.compareTo(t);
        };
    }

    private static Double num(String s) {
        try { return s == null || s.isBlank() ? null : Double.valueOf(s.trim()); } catch (NumberFormatException e) { return null; }
    }

    private static double nz(Double d) { return d == null ? 0 : d; }
    private static String up(String s) { return s == null ? "" : s.trim().toUpperCase(); }
    private static String iso(LocalDate d) { return d == null ? null : d.toString(); }
    private static boolean notBlank(String s) { return s != null && !s.isBlank(); }
    private static boolean has(String s) { return notBlank(s) && !"ALL".equalsIgnoreCase(s); }
}
