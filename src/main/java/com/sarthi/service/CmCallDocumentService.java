package com.sarthi.service;

import com.sarthi.dto.reports.InspectionCallsReportDto;
import com.sarthi.service.VendorPoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.sql.Date;
import java.sql.Timestamp;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Shared batch enrichment and detail lookup for CM Call Monitoring documents
 * across ERC, Sleeper, and Rail Pad (PO PDF from po_header, signed IC from certificate_storage,
 * and uploaded annexures from IC_ANNEXURE_DOCUMENT).
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CmCallDocumentService {

    private static final Pattern PO_DIGITS = Pattern.compile("\\d{5,}");

    private final JdbcTemplate jdbcTemplate;
    private final VendorPoService vendorPoService;

    public List<InspectionCallsReportDto> enrich(List<InspectionCallsReportDto> list) {
        if (list == null || list.isEmpty()) {
            return list;
        }
        try {
            Map<String, String> poPdfByCandidate = loadPoPdfPaths(list);
            Map<String, String> certByCallToken = loadIssuedCertificates();
            Map<String, Integer> annexCountByToken = loadAnnexureCounts();
            Map<String, String> callToCertFromDetails = loadCertificateMappingsFromDetails(list);

            for (InspectionCallsReportDto dto : list) {
                String callNo = dto.getCallNumber() != null ? dto.getCallNumber().trim() : "";
                String callKey = callNo.toUpperCase();

                String pdf = null;
                for (String cand : poCandidates(dto.getPoNumber())) {
                    String found = poPdfByCandidate.get(cand.toUpperCase());
                    if (found != null && !found.isBlank()) {
                        pdf = found;
                        break;
                    }
                }
                dto.setPdfPath(pdf);

                String resolvedIc = certByCallToken.get(callKey);
                if (resolvedIc == null && callToCertFromDetails.containsKey(callKey)) {
                    String mappedCert = callToCertFromDetails.get(callKey);
                    if (mappedCert != null) {
                        resolvedIc = certByCallToken.get(mappedCert.toUpperCase());
                    }
                }
                dto.setHasIc(resolvedIc != null && !resolvedIc.isBlank());
                dto.setIcNumber(resolvedIc);

                int annCount = annexCountByToken.getOrDefault(callKey, 0);
                if (annCount == 0 && resolvedIc != null) {
                    annCount = annexCountByToken.getOrDefault(resolvedIc.toUpperCase(), 0);
                }
                dto.setAnnexureCount(annCount);
            }
        } catch (Exception e) {
            log.warn("Failed to enrich CM call documents: {}", e.getMessage());
        }
        return list;
    }

    public Map<String, Object> getCallDocuments(String callNo, String poNo) {
        String cleanCall = callNo != null ? callNo.trim() : "";
        String cleanPo = poNo != null ? poNo.trim() : "";

        Map<String, Object> out = new LinkedHashMap<>();
        out.put("callNumber", cleanCall);
        out.put("poNumber", cleanPo);

        String pdfPath = null;
        if (!cleanPo.isBlank()) {
            try {
                pdfPath = vendorPoService.getPdfPathByRawPoNo(cleanPo);
            } catch (Exception ignored) {
            }
        }
        Map<String, Object> poInfo = new LinkedHashMap<>();
        poInfo.put("available", !cleanPo.isBlank() && pdfPath != null && !pdfPath.isBlank());
        poInfo.put("hasPoNumber", !cleanPo.isBlank());
        poInfo.put("poNumber", cleanPo);
        poInfo.put("pdfPath", pdfPath != null ? pdfPath : "");
        out.put("po", poInfo);

        Map<String, Object> icInfo = findCertificateForCall(cleanCall);
        out.put("ic", icInfo);

        List<Map<String, Object>> annexures = findAnnexuresForCall(cleanCall, (String) icInfo.get("icNumber"));
        out.put("annexures", annexures);

        return out;
    }

    private Map<String, String> loadPoPdfPaths(List<InspectionCallsReportDto> list) {
        Set<String> allCandidates = new LinkedHashSet<>();
        for (InspectionCallsReportDto dto : list) {
            allCandidates.addAll(poCandidates(dto.getPoNumber()));
        }
        Map<String, String> map = new HashMap<>();
        if (allCandidates.isEmpty()) return map;

        List<String> candList = new ArrayList<>(allCandidates);
        for (int i = 0; i < candList.size(); i += 400) {
            List<String> sub = candList.subList(i, Math.min(i + 400, candList.size()));
            String placeholders = String.join(",", Collections.nCopies(sub.size(), "?"));
            String sql = "SELECT po_no, l5_po_no, pdf_path FROM po_header WHERE pdf_path IS NOT NULL AND TRIM(pdf_path) <> '' " +
                    "AND (po_no IN (" + placeholders + ") OR l5_po_no IN (" + placeholders + "))";
            Object[] params = new Object[sub.size() * 2];
            for (int j = 0; j < sub.size(); j++) {
                params[j] = sub.get(j);
                params[sub.size() + j] = sub.get(j);
            }
            jdbcTemplate.query(sql, rs -> {
                String po = rs.getString("po_no");
                String l5 = rs.getString("l5_po_no");
                String path = rs.getString("pdf_path");
                if (path != null && !path.isBlank()) {
                    if (po != null) map.putIfAbsent(po.trim().toUpperCase(), path.trim());
                    if (l5 != null) map.putIfAbsent(l5.trim().toUpperCase(), path.trim());
                }
            }, params);
        }
        return map;
    }

    private Map<String, String> loadCertificateMappingsFromDetails(List<InspectionCallsReportDto> list) {
        List<String> calls = list.stream()
                .map(InspectionCallsReportDto::getCallNumber)
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .distinct()
                .toList();
        Map<String, String> map = new HashMap<>();
        for (int i = 0; i < calls.size(); i += 400) {
            List<String> sub = calls.subList(i, Math.min(i + 400, calls.size()));
            String placeholders = String.join(",", Collections.nCopies(sub.size(), "?"));
            String sql = "SELECT CALL_NO, CERTIFICATE_NO FROM inspection_complete_details WHERE CALL_NO IN (" + placeholders + ") AND CERTIFICATE_NO IS NOT NULL";
            try {
                jdbcTemplate.query(sql, rs -> {
                    String c = rs.getString("CALL_NO");
                    String cert = rs.getString("CERTIFICATE_NO");
                    if (c != null && cert != null && !cert.isBlank()) {
                        map.putIfAbsent(c.trim().toUpperCase(), cert.trim());
                    }
                }, sub.toArray());
            } catch (Exception ignored) {
            }
        }
        return map;
    }

    private Map<String, String> loadIssuedCertificates() {
        Map<String, String> tokenToIc = new HashMap<>();
        jdbcTemplate.query("SELECT ic_number, file_name FROM certificate_storage WHERE ic_number IS NOT NULL", rs -> {
            String icNum = rs.getString("ic_number");
            String fileName = rs.getString("file_name");
            if (icNum == null || icNum.isBlank()) return;
            String cleanIc = icNum.trim();
            tokenToIc.putIfAbsent(cleanIc.toUpperCase(), cleanIc);
            indexCallTokens(cleanIc, cleanIc, tokenToIc);
            if (fileName != null && !fileName.isBlank()) {
                indexCallTokens(fileName.replaceFirst("(?i)\\.pdf$", ""), cleanIc, tokenToIc);
            }
        });
        return tokenToIc;
    }

    private Map<String, Integer> loadAnnexureCounts() {
        Map<String, Integer> counts = new HashMap<>();
        try {
            jdbcTemplate.query(
                    "SELECT CALL_NO, IC_NUMBER, COUNT(*) AS cnt FROM IC_ANNEXURE_DOCUMENT " +
                            "WHERE STATUS IS NULL OR UPPER(STATUS) = 'ACTIVE' GROUP BY CALL_NO, IC_NUMBER",
                    rs -> {
                        String callNo = rs.getString("CALL_NO");
                        String icNo = rs.getString("IC_NUMBER");
                        int cnt = rs.getInt("cnt");
                        if (cnt <= 0) return;
                        Set<String> seen = new HashSet<>();
                        addTokensForCount(callNo, seen);
                        addTokensForCount(icNo, seen);
                        for (String token : seen) {
                            counts.merge(token, cnt, Integer::sum);
                        }
                    });
        } catch (Exception ignored) {
        }
        return counts;
    }

    private Map<String, Object> findCertificateForCall(String cleanCall) {
        Map<String, Object> icInfo = new LinkedHashMap<>();
        icInfo.put("available", false);
        if (cleanCall.isEmpty()) return icInfo;

        List<String> lookupTokens = new ArrayList<>();
        lookupTokens.add(cleanCall);
        lookupTokens.add(cleanCall.replace("-", "/"));
        lookupTokens.add(cleanCall.replace("/", "-"));
        try {
            jdbcTemplate.query(
                    "SELECT CERTIFICATE_NO FROM inspection_complete_details WHERE CALL_NO = ? AND CERTIFICATE_NO IS NOT NULL LIMIT 1",
                    rs -> {
                        String cert = rs.getString("CERTIFICATE_NO");
                        if (cert != null && !cert.isBlank()) {
                            lookupTokens.add(cert.trim());
                        }
                    },
                    cleanCall);
        } catch (Exception ignored) {
        }

        for (String token : lookupTokens) {
            List<Map<String, Object>> rows = jdbcTemplate.queryForList(
                    "SELECT ic_number, file_name, ic_date, blob_url, uploaded_at FROM certificate_storage " +
                            "WHERE ic_number = ? OR ic_number LIKE CONCAT('%/', ?, '/%') OR ic_number LIKE CONCAT('%', ?, '%') " +
                            "   OR file_name LIKE CONCAT('%', ?, '%') ORDER BY uploaded_at DESC LIMIT 1",
                    token, token, token, token);
            if (!rows.isEmpty()) {
                Map<String, Object> row = rows.get(0);
                icInfo.put("available", true);
                icInfo.put("icNumber", row.get("ic_number"));
                icInfo.put("fileName", row.get("file_name"));
                Object dt = row.get("ic_date");
                icInfo.put("icDate", dt instanceof Date d ? d.toLocalDate().toString() : (dt != null ? dt.toString() : ""));
                icInfo.put("url", row.get("blob_url"));
                return icInfo;
            }
        }
        return icInfo;
    }

    private List<Map<String, Object>> findAnnexuresForCall(String cleanCall, String icNumber) {
        List<Map<String, Object>> list = new ArrayList<>();
        if (cleanCall.isEmpty() && (icNumber == null || icNumber.isBlank())) return list;

        Set<String> variants = new LinkedHashSet<>();
        if (!cleanCall.isEmpty()) {
            variants.add(cleanCall);
            variants.add(cleanCall.replace("-", "/"));
            variants.add(cleanCall.replace("/", "-"));
        }
        if (icNumber != null && !icNumber.isBlank()) {
            variants.add(icNumber.trim());
        }

        String placeholders = String.join(",", Collections.nCopies(variants.size(), "?"));
        String likeToken = !cleanCall.isEmpty() ? cleanCall : icNumber.trim();
        Object[] params = new Object[variants.size() * 2 + 2];
        int idx = 0;
        for (String v : variants) params[idx++] = v;
        for (String v : variants) params[idx++] = v;
        params[idx++] = "%" + likeToken + "%";
        params[idx] = "%" + likeToken + "%";

        String sql = "SELECT id, CALL_NO, IC_NUMBER, MODULE_TYPE, ORIGINAL_FILE_NAME, UPLOADED_BY, UPLOADED_AT " +
                "FROM IC_ANNEXURE_DOCUMENT " +
                "WHERE (STATUS IS NULL OR UPPER(STATUS) = 'ACTIVE') " +
                "  AND (CALL_NO IN (" + placeholders + ") OR IC_NUMBER IN (" + placeholders + ") " +
                "       OR CALL_NO LIKE ? OR IC_NUMBER LIKE ?) " +
                "ORDER BY UPLOADED_AT DESC";

        try {
            jdbcTemplate.query(sql, rs -> {
                Map<String, Object> m = new LinkedHashMap<>();
                m.put("id", rs.getLong("id"));
                m.put("callNo", rs.getString("CALL_NO"));
                m.put("icNumber", rs.getString("IC_NUMBER"));
                m.put("moduleType", rs.getString("MODULE_TYPE"));
                m.put("fileName", rs.getString("ORIGINAL_FILE_NAME"));
                m.put("uploadedBy", rs.getString("UPLOADED_BY"));
                Timestamp ts = rs.getTimestamp("UPLOADED_AT");
                m.put("uploadedAt", ts != null ? ts.toLocalDateTime().toString() : "");
                list.add(m);
            }, params);
        } catch (Exception e) {
            log.debug("Failed to load annexures for {}: {}", cleanCall, e.getMessage());
        }
        return list;
    }

    private static List<String> poCandidates(String rawPo) {
        if (rawPo == null || rawPo.isBlank()) return Collections.emptyList();
        String str = rawPo.trim();
        Set<String> cands = new LinkedHashSet<>();
        Matcher m = PO_DIGITS.matcher(str);
        if (m.find()) cands.add(m.group());
        if (str.contains("/")) {
            String beforeSlash = str.split("/")[0].trim();
            String stripped = beforeSlash.replaceFirst("^[A-Za-z\\s]+[-/]\\s*", "").trim();
            if (!stripped.isEmpty()) cands.add(stripped);
            if (!beforeSlash.isEmpty()) cands.add(beforeSlash);
        }
        String noPrefix = str.replaceFirst("^[A-Za-z\\s]+[-/]\\s*", "").trim();
        if (!noPrefix.isEmpty()) cands.add(noPrefix);
        cands.add(str);
        return new ArrayList<>(cands);
    }

    private static void indexCallTokens(String text, String value, Map<String, String> out) {
        if (text == null || text.isBlank()) return;
        for (String part : text.split("[/_]")) {
            String t = part.trim();
            if (t.length() >= 6 && t.matches(".*\\d+.*")) {
                out.putIfAbsent(t.toUpperCase(), value);
            }
        }
    }

    private static void addTokensForCount(String text, Set<String> out) {
        if (text == null || text.isBlank()) return;
        String clean = text.trim().toUpperCase();
        out.add(clean);
        for (String part : clean.split("[/_]")) {
            String t = part.trim();
            if (t.length() >= 6 && t.matches(".*\\d+.*")) {
                out.add(t);
            }
        }
    }
}
