package com.sarthi.service.ercreport;

import com.sarthi.repository.IePincodePoiMappingRepository;
import com.sarthi.repository.rawmaterial.InspectionCallRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.*;

/**
 * ERC Raw Material Status (Railway Board): one row per unit (POI code).
 * b procured = RM offered (MT), c inspected = RM accepted + rejected (MT), d = b - c,
 * e processed = process call offered qty converted to MT, f = b - e.
 * Three grouped queries in total (no per-unit queries).
 */
@Service
public class ErcRawMaterialReportService {

    private final InspectionCallRepository inspectionCallRepository;
    private final IePincodePoiMappingRepository poiMappingRepository;

    public ErcRawMaterialReportService(InspectionCallRepository inspectionCallRepository,
                                       IePincodePoiMappingRepository poiMappingRepository) {
        this.inspectionCallRepository = inspectionCallRepository;
        this.poiMappingRepository = poiMappingRepository;
    }

    public Map<String, Object> getReport(LocalDate fromDate, LocalDate toDate) {
        Map<String, Row> rows = new LinkedHashMap<>();

        for (Object[] r : inspectionCallRepository.sumRmOfferedMtByPoi(fromDate, toDate)) {
            row(rows, r).procuredMt = dec(r[3]);
        }
        for (Object[] r : inspectionCallRepository.sumRmInspectedMtByPoi(fromDate, toDate)) {
            Row row = row(rows, r);
            row.inspectedMt = dec(r[3]);
            row.inspectedPieces = r.length > 4 ? dec(r[4]).longValue() : 0;
        }
        for (Object[] r : inspectionCallRepository.sumProcessOfferedByPoi(fromDate, toDate)) {
            Row row = row(rows, r);
            row.processedPieces = dec(r[3]).longValue();
            row.processedMt = dec(r[4]);
        }

        // Plant (unit) names from the POI master, falling back to the names on the calls
        if (!rows.isEmpty()) {
            List<Object[]> info = poiMappingRepository.findAllPlantInfoForPois(new ArrayList<>(rows.keySet()));
            for (Object[] i : info) {
                Row row = rows.get(str(i[0]));
                if (row == null) continue;
                if (!str(i[1]).isEmpty()) row.companyName = str(i[1]);
                if (!str(i[2]).isEmpty()) row.unitName = str(i[2]);
            }
        }

        List<Map<String, Object>> out = new ArrayList<>();
        BigDecimal tB = BigDecimal.ZERO, tC = BigDecimal.ZERO, tE = BigDecimal.ZERO, tF = BigDecimal.ZERO;
        long tPcs = 0, tInsPcs = 0;
        for (Row r : rows.values()) {
            BigDecimal d = r.procuredMt.subtract(r.inspectedMt);
            if (d.compareTo(BigDecimal.ZERO) < 0) {
                d = BigDecimal.ZERO;
            }
            BigDecimal f = r.procuredMt.subtract(r.processedMt);
            if (f.compareTo(BigDecimal.ZERO) < 0) {
                f = BigDecimal.ZERO;
            }
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("poiCode", r.poiCode);
            m.put("companyName", r.companyName);
            m.put("unitName", r.unitName);
            m.put("procuredMt", mt(r.procuredMt));
            m.put("inspectedMt", mt(r.inspectedMt));
            m.put("inspectedPieces", r.inspectedPieces);
            m.put("pendingInspectionMt", mt(d));
            m.put("processedPieces", r.processedPieces);
            m.put("processedMt", mt(r.processedMt));
            m.put("pendingProcessingMt", mt(f));
            out.add(m);
            tB = tB.add(r.procuredMt);
            tC = tC.add(r.inspectedMt);
            tE = tE.add(r.processedMt);
            tF = tF.add(f);
            tPcs += r.processedPieces;
            tInsPcs += r.inspectedPieces;
        }
        out.sort(Comparator.comparing((Map<String, Object> m) -> String.valueOf(m.get("companyName")), String.CASE_INSENSITIVE_ORDER)
                .thenComparing(m -> String.valueOf(m.get("unitName")), String.CASE_INSENSITIVE_ORDER));

        Map<String, Object> totals = new LinkedHashMap<>();
        totals.put("procuredMt", mt(tB));
        totals.put("inspectedMt", mt(tC));
        totals.put("inspectedPieces", tInsPcs);
        totals.put("pendingInspectionMt", mt(tB.subtract(tC).max(BigDecimal.ZERO)));
        totals.put("processedPieces", tPcs);
        totals.put("processedMt", mt(tE));
        totals.put("pendingProcessingMt", mt(tF));

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("rows", out);
        result.put("totals", totals);
        return result;
    }

    private static Row row(Map<String, Row> rows, Object[] r) {
        String poi = str(r[0]);
        Row row = rows.computeIfAbsent(poi, k -> new Row(k));
        if (row.companyName.isEmpty()) row.companyName = str(r[1]);
        if (row.unitName.isEmpty()) row.unitName = str(r[2]);
        return row;
    }

    private static BigDecimal dec(Object v) {
        if (v == null) return BigDecimal.ZERO;
        if (v instanceof BigDecimal b) return b;
        if (v instanceof Number n) return new BigDecimal(n.toString());
        try { return new BigDecimal(v.toString().trim()); } catch (Exception e) { return BigDecimal.ZERO; }
    }

    private static BigDecimal mt(BigDecimal v) {
        return v.setScale(3, RoundingMode.HALF_UP);
    }

    private static String str(Object v) {
        return v == null ? "" : v.toString().trim();
    }

    private static final class Row {
        final String poiCode;
        String companyName = "";
        String unitName = "";
        BigDecimal procuredMt = BigDecimal.ZERO;
        BigDecimal inspectedMt = BigDecimal.ZERO;
        BigDecimal processedMt = BigDecimal.ZERO;
        long processedPieces = 0;
        long inspectedPieces = 0;

        Row(String poiCode) { this.poiCode = poiCode; }
    }
}
