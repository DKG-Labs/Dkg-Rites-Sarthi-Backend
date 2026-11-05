package com.sarthi.service.ercreport;

import com.sarthi.repository.InventoryEntryRepository;
import com.sarthi.repository.PincodePoIMappingRepository;
import com.sarthi.repository.rawmaterial.InspectionCallRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.*;

/**
 * ERC Raw Material Status (Railway Board): combined by Vendor Code.
 * a. Plant Name (vendor company name & associated units)
 * b. Quantity of Raw Material Procured (MT) - Inventory added by vendor (inventory_entries.tc_quantity)
 * c. Quantity of Raw Material Inspected (MT) - Raw Material offered by vendor for inspection (rm_inspection_details.total_offered_qty_mt)
 * d. Quantity of Raw Material Pending for Inspection (MT) = (b - c)
 * e. Quantity Processed (MT) = Process Calls raised converted to MT (MK-III 0.91, J 0.915, MK-V 1.14 kg)
 * f. Total Raw Material with plant pending for Processing (MT) = (b - e)
 */
@Service
public class ErcRawMaterialReportService {

    private final InspectionCallRepository inspectionCallRepository;
    private final InventoryEntryRepository inventoryEntryRepository;
    private final PincodePoIMappingRepository poiMappingRepository;

    public ErcRawMaterialReportService(InspectionCallRepository inspectionCallRepository,
                                       InventoryEntryRepository inventoryEntryRepository,
                                       PincodePoIMappingRepository poiMappingRepository) {
        this.inspectionCallRepository = inspectionCallRepository;
        this.inventoryEntryRepository = inventoryEntryRepository;
        this.poiMappingRepository = poiMappingRepository;
    }

    public Map<String, Object> getReport(LocalDate fromDate, LocalDate toDate) {
        Map<String, VendorRow> rows = new LinkedHashMap<>();

        // b. Inventory added by vendor (MT)
        for (Object[] r : inventoryEntryRepository.sumInventoryByVendor(fromDate, toDate)) {
            String vendorCode = str(r[0]);
            if (vendorCode.isEmpty()) continue;
            VendorRow row = rows.computeIfAbsent(vendorCode, VendorRow::new);
            if (!str(r[1]).isEmpty() && row.companyName.isEmpty()) {
                row.companyName = str(r[1]);
            }
            row.procuredMt = dec(r[2]);
        }

        // c. RM offered by vendor for inspection (MT & nos)
        for (Object[] r : inspectionCallRepository.sumRmOfferedMtByVendor(fromDate, toDate)) {
            String vendorCode = str(r[0]);
            if (vendorCode.isEmpty()) continue;
            VendorRow row = rows.computeIfAbsent(vendorCode, VendorRow::new);
            if (!str(r[1]).isEmpty() && row.companyName.isEmpty()) {
                row.companyName = str(r[1]);
            }
            row.inspectedMt = dec(r[2]);
            if (r.length > 3) {
                row.inspectedPieces = dec(r[3]).longValue();
            }
        }

        // e. Process calls offered qty (nos & MT)
        for (Object[] r : inspectionCallRepository.sumProcessOfferedByVendor(fromDate, toDate)) {
            String vendorCode = str(r[0]);
            if (vendorCode.isEmpty()) continue;
            VendorRow row = rows.computeIfAbsent(vendorCode, VendorRow::new);
            if (!str(r[1]).isEmpty() && row.companyName.isEmpty()) {
                row.companyName = str(r[1]);
            }
            row.processedPieces = dec(r[2]).longValue();
            row.processedMt = dec(r[3]);
        }

        // Enrich company name, units list, and POI codes from pincode_poi_mapping
        if (!rows.isEmpty()) {
            List<String> vendorCodes = new ArrayList<>(rows.keySet());
            List<Object[]> unitsInfo = poiMappingRepository.findVendorUnitsInfo(vendorCodes);
            Map<String, List<String>> vendorUnitsMap = new LinkedHashMap<>();
            Map<String, Set<String>> vendorPoisMap = new LinkedHashMap<>();
            Map<String, String> vendorCompanyMap = new HashMap<>();

            for (Object[] u : unitsInfo) {
                String vCode = str(u[0]);
                String comp = str(u[1]);
                String unit = str(u[2]);
                String poi = str(u[3]);

                String matchKey = rows.containsKey(vCode) ? vCode : (rows.containsKey(poi) ? poi : null);
                if (matchKey == null) continue;

                if (!comp.isEmpty() && !vendorCompanyMap.containsKey(matchKey)) {
                    vendorCompanyMap.put(matchKey, comp);
                }

                if (!poi.isEmpty()) {
                    vendorPoisMap.computeIfAbsent(matchKey, k -> new LinkedHashSet<>()).add(poi);
                }

                String unitLabel = "";
                if (!unit.isEmpty() && !poi.isEmpty()) {
                    unitLabel = unit + " (" + poi + ")";
                } else if (!unit.isEmpty()) {
                    unitLabel = unit;
                } else if (!poi.isEmpty()) {
                    unitLabel = poi;
                }

                if (!unitLabel.isEmpty()) {
                    List<String> list = vendorUnitsMap.computeIfAbsent(matchKey, k -> new ArrayList<>());
                    if (!list.contains(unitLabel)) {
                        list.add(unitLabel);
                    }
                }
            }

            for (Map.Entry<String, VendorRow> entry : rows.entrySet()) {
                String vCode = entry.getKey();
                VendorRow row = entry.getValue();

                if (vendorCompanyMap.containsKey(vCode)) {
                    row.companyName = vendorCompanyMap.get(vCode);
                }
                if (vendorUnitsMap.containsKey(vCode)) {
                    row.unitSummary = String.join(", ", vendorUnitsMap.get(vCode));
                }
                if (vendorPoisMap.containsKey(vCode)) {
                    row.poiCodes = new ArrayList<>(vendorPoisMap.get(vCode));
                } else if (vCode.startsWith("POI")) {
                    row.poiCodes = Collections.singletonList(vCode);
                }
            }
        }

        List<Map<String, Object>> out = new ArrayList<>();
        BigDecimal tB = BigDecimal.ZERO, tC = BigDecimal.ZERO, tE = BigDecimal.ZERO, tF = BigDecimal.ZERO, tD = BigDecimal.ZERO;
        long tPcs = 0, tInsPcs = 0;

        for (VendorRow r : rows.values()) {
            // d = b - c (pending inspection)
            BigDecimal d = r.procuredMt.subtract(r.inspectedMt);
            if (d.compareTo(BigDecimal.ZERO) < 0) {
                d = BigDecimal.ZERO;
            }

            // f = b - e (pending processing)
            BigDecimal f = r.procuredMt.subtract(r.processedMt);
            if (f.compareTo(BigDecimal.ZERO) < 0) {
                f = BigDecimal.ZERO;
            }

            Map<String, Object> m = new LinkedHashMap<>();
            m.put("vendorCode", cleanCode(r.vendorCode));
            m.put("companyName", !r.companyName.isEmpty() ? r.companyName : cleanCode(r.vendorCode));
            m.put("unitName", r.unitSummary);
            m.put("poiCodes", r.poiCodes);
            m.put("poiCode", !r.poiCodes.isEmpty() ? cleanCode(r.poiCodes.get(0)) : cleanCode(r.vendorCode));
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
            tD = tD.add(d);
            tE = tE.add(r.processedMt);
            tF = tF.add(f);
            tPcs += r.processedPieces;
            tInsPcs += r.inspectedPieces;
        }

        out.sort(Comparator.comparing((Map<String, Object> m) -> String.valueOf(m.get("companyName")), String.CASE_INSENSITIVE_ORDER)
                .thenComparing(m -> String.valueOf(m.get("vendorCode")), String.CASE_INSENSITIVE_ORDER));

        Map<String, Object> totals = new LinkedHashMap<>();
        totals.put("procuredMt", mt(tB));
        totals.put("inspectedMt", mt(tC));
        totals.put("inspectedPieces", tInsPcs);
        totals.put("pendingInspectionMt", mt(tD));
        totals.put("processedPieces", tPcs);
        totals.put("processedMt", mt(tE));
        totals.put("pendingProcessingMt", mt(tF));

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("rows", out);
        result.put("totals", totals);
        return result;
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

    private static String cleanCode(String s) {
        return s == null ? "" : s.replaceAll("^:+", "").trim();
    }

    private static String str(Object v) {
        return v == null ? "" : v.toString().trim();
    }

    private static final class VendorRow {
        final String vendorCode;
        String companyName = "";
        String unitSummary = "";
        List<String> poiCodes = new ArrayList<>();
        BigDecimal procuredMt = BigDecimal.ZERO;
        BigDecimal inspectedMt = BigDecimal.ZERO;
        BigDecimal processedMt = BigDecimal.ZERO;
        long processedPieces = 0;
        long inspectedPieces = 0;

        VendorRow(String vendorCode) { this.vendorCode = vendorCode; }
    }
}
