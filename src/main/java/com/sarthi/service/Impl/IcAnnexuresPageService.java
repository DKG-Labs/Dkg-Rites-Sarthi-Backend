package com.sarthi.service.Impl;

import com.sarthi.SRailPad.repository.RailWorkflowTransactionRepository;
import com.sarthi.Sleeper.repository.SleeperWorkflowRepository;
import com.sarthi.dto.reports.IcAnnexuresReportDto;
import com.sarthi.repository.WorkflowTransitionRepository;
import com.sarthi.service.reports;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;
import java.util.function.Function;

/**
 * Download IC & Annexures with normal paging.
 *
 * 1. A light query per product lists every issued IC (same issued rule and dashboard filters as the full report)
 *    with cheap columns only. Stage, search, IC date range, PO, sorting and paging are applied on that list, so a
 *    search finds a call on any page.
 * 2. The full report query (quantities, units) runs only for the calls on the requested page.
 *
 * Stage by call-number prefix: ERC ER (Raw Material) / EP (Process) / EF (Final); Rail Pad RPP / RPF; Sleeper SF.
 */
@Service
@RequiredArgsConstructor
public class IcAnnexuresPageService {

    private final WorkflowTransitionRepository workflowTransitionRepository;
    private final RailWorkflowTransactionRepository railWorkflowTransactionRepository;
    private final SleeperWorkflowRepository sleeperWorkflowRepository;
    private final reports reportService;

    /** One issued IC, cheap columns only. */
    private record Key(String callNo, String icNo, String vendor, String poNo, String poSr, String stage,
                       String itemCat, String callAt, String sortDate, String icDate) {
        String upperCall() { return callNo == null ? "" : callNo.toUpperCase().trim(); }
        String poFull() { return (poNo == null ? "" : poNo) + (poSr == null || poSr.isBlank() ? "" : "/" + poSr); }
    }

    public Map<String, Object> page(String product, String vendorPlantCode, String zonalRailway,
                                    LocalDate startDate, LocalDate endDate,
                                    String stage, String search, String searchType,
                                    LocalDate icFrom, LocalDate icTo, String poNumber,
                                    String sortKey, String sortDir, int page, int size) {
        long t0 = System.nanoTime();
        String p = product == null ? "" : product.trim().toLowerCase();
        boolean isRailPad = p.contains("pad");
        boolean isSleeper = p.contains("sleeper");

        // ---- 1. every issued IC for the product + dashboard filters (cheap) ----
        List<Object[]> raw = isRailPad
                ? railWorkflowTransactionRepository.findRailPadDownloadIcAnnexuresKeys(vendorPlantCode, zonalRailway, startDate, endDate)
                : isSleeper
                ? sleeperWorkflowRepository.findSleeperDownloadIcAnnexuresKeys(vendorPlantCode, zonalRailway, startDate, endDate)
                : workflowTransitionRepository.findDownloadIcAnnexuresKeys(vendorPlantCode, zonalRailway);
        long keysMs = (System.nanoTime() - t0) / 1_000_000;

        Map<String, Key> byCall = new LinkedHashMap<>();
        for (Object[] r : raw) {
            Key k = new Key(s(r[0]), s(r[1]), s(r[2]), s(r[3]), s(r[4]), s(r[5]), s(r[6]), s(r[7]), s(r[8]), s(r[9]));
            if (k.callNo() == null || k.callNo().isBlank()) continue;
            if (!isRailPad && !isSleeper) {
                // ERC: same product (item category) and IC-date rules as the full report
                if (!ercProductMatches(p, k.itemCat())) continue;
                if (!inRange(k.icDate(), startDate, endDate)) continue;
            }
            byCall.putIfAbsent(k.callNo().trim(), k);   // the PO join can repeat a call; keep one row per IC
        }
        List<Key> base = new ArrayList<>(byCall.values());
        if (poNumber != null && !poNumber.isBlank()) {
            String po = poNumber.trim();
            base.removeIf(k -> !po.equals(k.poNo() == null ? "" : k.poNo().trim()));
        }

        // ---- counts for the stage cards (before stage / search) ----
        Map<String, Object> counts = new LinkedHashMap<>();
        counts.put("total", base.size());
        counts.put("rawMaterial", base.stream().filter(k -> stageOf(k).equals("RAW MATERIAL")).count());
        counts.put("process", base.stream().filter(k -> stageOf(k).equals("PROCESS")).count());
        counts.put("final", base.stream().filter(k -> stageOf(k).equals("FINAL")).count());

        // ---- 2. stage, IC date range, search ----
        String st = stage == null ? "" : stage.trim().toUpperCase();
        String q = search == null ? "" : search.trim().toLowerCase();
        String type = searchType == null ? "global" : searchType.trim();
        List<Key> rows = new ArrayList<>();
        for (Key k : base) {
            if (!st.isEmpty() && !st.equals("ALL") && !stageOf(k).equals(st)) continue;
            if (!inRange(k.icDate(), icFrom, icTo)) continue;
            if (!q.isEmpty() && !matches(k, q, type)) continue;
            rows.add(k);
        }

        // ---- 3. sort ----
        Comparator<Key> cmp = comparator(sortKey);
        if ("desc".equalsIgnoreCase(sortDir) && !"poWise".equals(sortKey == null ? "poWise" : sortKey)) cmp = cmp.reversed();
        rows.sort(cmp);

        // ---- 4. page ----
        int total = rows.size();
        List<Key> pageKeys;
        int pg = Math.max(page, 0);
        if (size <= 0) {
            pageKeys = rows;                                  // export: every matching row
        } else {
            int from = Math.min(pg * size, total);
            pageKeys = rows.subList(from, Math.min(from + size, total));
        }

        // ---- 5. full rows (quantities, units) for the page only, in page order ----
        List<IcAnnexuresReportDto> content = new ArrayList<>();
        long tDetails = System.nanoTime();
        if (!pageKeys.isEmpty()) {
            List<String> callNos = pageKeys.stream().map(k -> k.callNo().trim()).toList();
            List<IcAnnexuresReportDto> full = reportService.getDownloadIcAnnexuresReportForCalls(
                    product, vendorPlantCode, zonalRailway, startDate, endDate, callNos);
            Map<String, IcAnnexuresReportDto> fullByCall = new HashMap<>();
            for (IcAnnexuresReportDto d : full) {
                if (d.getCallNumber() != null) fullByCall.putIfAbsent(d.getCallNumber().trim(), d);
            }
            for (String c : callNos) {
                IcAnnexuresReportDto d = fullByCall.get(c);
                if (d != null) content.add(d);
            }
        }

        Map<String, Object> out = new LinkedHashMap<>();
        out.put("content", content);
        out.put("total", total);
        out.put("page", pg);
        out.put("size", size);
        out.put("totalPages", size <= 0 ? 1 : Math.max(1, (int) Math.ceil(total / (double) size)));
        out.put("counts", counts);
        out.put("serverMs", (System.nanoTime() - t0) / 1_000_000);
        out.put("keysMs", keysMs);                                         // list of all issued ICs (cheap columns)
        out.put("detailsMs", (System.nanoTime() - tDetails) / 1_000_000);  // full rows for this page
        return out;
    }

    // ---------- rules (same as the screen's previous client-side filters) ----------

    /** RAW MATERIAL / PROCESS / FINAL from the call-number prefix, falling back to the stage text. */
    private static String stageOf(Key k) {
        String c = k.upperCall();
        String s = k.stage() == null ? "" : k.stage().toUpperCase();
        if (c.startsWith("ER") || c.startsWith("RPRM") || s.contains("RAW") || s.equals("RM")) return "RAW MATERIAL";
        if (c.startsWith("EP") || c.startsWith("RPP") || s.contains("PROCESS")) return "PROCESS";
        if (c.startsWith("EF") || c.startsWith("RPF") || c.startsWith("SF") || s.contains("FINAL")) return "FINAL";
        return s;
    }

    private static boolean matches(Key k, String q, String type) {
        switch (type) {
            case "callNo": return has(k.callNo(), q);
            case "po": return has(k.poFull(), q) || has(k.poNo(), q);
            case "vendor": return has(k.vendor(), q);
            default: return has(k.callNo(), q) || has(k.vendor(), q) || has(k.icNo(), q)
                    || has(k.poFull(), q) || has(k.poNo(), q) || has(k.stage(), q);
        }
    }

    private static Comparator<Key> comparator(String key) {
        if (key == null || key.isBlank() || key.equals("poWise")
                || key.equals("callQtySet") || key.equals("callQtyNos")) {
            // PO-wise: PO, then Process before Final, then call date (quantity columns are sorted per page on screen)
            return Comparator.comparing((Key k) -> lower(k.poFull()))
                    .thenComparingInt(k -> switch (stageOf(k)) { case "PROCESS" -> 0; case "FINAL" -> 1; default -> 2; })
                    .thenComparing(k -> nz(k.callAt()));
        }
        Function<Key, String> f = switch (key) {
            case "vendorName" -> Key::vendor;
            case "poNumber" -> Key::poFull;
            case "callNumber" -> Key::callNo;
            case "icNumber" -> Key::icNo;
            case "stage" -> Key::stage;
            case "callSubmissionDateTime" -> Key::callAt;
            case "icIssuedDate" -> Key::icDate;
            default -> Key::callNo;
        };
        return Comparator.comparing(k -> lower(f.apply(k)));
    }

    private static boolean ercProductMatches(String product, String itemCat) {
        if (product.isEmpty()) return true;
        String cat = itemCat == null ? "" : itemCat.toLowerCase();
        if (product.contains("erc")) return cat.contains("clip") || cat.contains("erc");
        return cat.contains(product);
    }

    private static boolean inRange(String isoDate, LocalDate from, LocalDate to) {
        if (from == null && to == null) return true;
        if (isoDate == null || isoDate.length() < 10) return true;          // unreadable date is kept, as before
        try {
            LocalDate d = LocalDate.parse(isoDate.substring(0, 10));
            return (from == null || !d.isBefore(from)) && (to == null || !d.isAfter(to));
        } catch (Exception e) {
            return true;
        }
    }

    private static boolean has(String v, String q) { return v != null && v.toLowerCase().contains(q); }
    private static String lower(String v) { return v == null ? "" : v.toLowerCase(); }
    private static String nz(String v) { return v == null ? "" : v; }
    private static String s(Object o) { return o == null ? null : o.toString(); }
}
