package com.sarthi.util;

import com.sarthi.SRailPad.repository.RailVendorPlantsRepository;
import com.sarthi.Sleeper.repository.VendorPlantRepository;
import com.sarthi.repository.PincodePoIMappingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * "Place of Inspection" for e-mails: name and address instead of a bare POI code / plant id.
 * ERC: pincode_poi_mapping (company, unit, address) by POI code.
 * Sleeper: vendor_plant, Rail Pad: rail_vendor_plant (company / plant name, address) by plant id.
 * If nothing is found the code itself is shown, so the field is never empty. Never throws.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class MailPlaceResolver {

    private final PincodePoIMappingRepository pincodePoIMappingRepository;
    private final VendorPlantRepository vendorPlantRepository;
    private final RailVendorPlantsRepository railVendorPlantsRepository;

    /** Result: "poi" = company / unit line, "poiAddress" = address line (may be empty). */
    public Map<String, String> erc(String poiCode) {
        try {
            if (notBlank(poiCode)) {
                var m = pincodePoIMappingRepository.findFirstByPoiCode(poiCode.trim());
                if (m.isPresent()) {
                    var p = m.get();
                    return place(join(p.getCompanyName(), p.getUnitName()), join(p.getAddress(), p.getState()), poiCode);
                }
            }
        } catch (Exception e) {
            log.warn("[Mail] POI {} lookup failed: {}", poiCode, e.getMessage());
        }
        return place(null, null, poiCode);
    }

    public Map<String, String> sleeper(String plantId) {
        try {
            for (String id : idVariants(plantId)) {
                var vp = vendorPlantRepository.findByPlantId(id);
                if (vp.isPresent()) {
                    var p = vp.get();
                    Map<String, String> r = place(join(p.getCompanyName(), p.getPlantName()), join(p.getPlantAddress(), p.getPlantPincode()), plantId);
                    if (notBlank(p.getVendorCode())) r.put("vendorCode", p.getVendorCode().trim());
                    return r;
                }
            }
        } catch (Exception e) {
            log.warn("[Mail] Sleeper plant {} lookup failed: {}", plantId, e.getMessage());
        }
        return place(null, null, plantId);
    }

    public Map<String, String> railpad(String plantId) {
        try {
            for (String id : idVariants(plantId)) {
                var rp = railVendorPlantsRepository.findByPlantId(id);
                if (rp.isPresent()) {
                    var p = rp.get();
                    Map<String, String> r = place(join(p.getCompanyName(), p.getPlantName()), join(p.getPlantAddress(), p.getPlantPincode()), plantId);
                    if (notBlank(p.getVendorCode())) r.put("vendorCode", p.getVendorCode().trim());
                    return r;
                }
            }
        } catch (Exception e) {
            log.warn("[Mail] Rail Pad plant {} lookup failed: {}", plantId, e.getMessage());
        }
        return place(null, null, plantId);
    }

    private static Map<String, String> place(String name, String address, String code) {
        Map<String, String> out = new HashMap<>();
        String c = code == null ? "" : code.trim();
        out.put("poi", notBlank(name) ? name : c);
        out.put("poiAddress", notBlank(address) ? address : "");
        out.put("poiCode", c);
        return out;
    }

    /** Plant ids are stored both as "1007406/Mohali" and ":1007406/Mohali". */
    private static List<String> idVariants(String id) {
        if (!notBlank(id)) return List.of();
        String clean = id.trim().replaceFirst("^:", "");
        return new ArrayList<>(new LinkedHashSet<>(List.of(id.trim(), clean, ":" + clean)));
    }

    /** Non-blank parts joined with ", ", skipping repeats (e.g. company = plant name). */
    private static String join(String... parts) {
        return Stream.of(parts).filter(MailPlaceResolver::notBlank).map(String::trim).distinct()
                .collect(Collectors.joining(", "));
    }

    private static boolean notBlank(String s) { return s != null && !s.isBlank(); }
}
