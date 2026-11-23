package com.sarthi.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Sleeper quantity UOM rules, bound from {@code sleeper.qty.*} in application.properties.
 * <p>
 * A "measured" UOM (e.g. running metres) is one whose call quantity is the declared, possibly fractional,
 * offered quantity instead of a count of sleepers or sets. The list mirrors QTY_UOM_RULES aliases in the
 * Sleeper frontend. When the property is missing the list is empty and every UOM keeps its existing behaviour.
 */
@Data
@Component
@ConfigurationProperties(prefix = "sleeper.qty")
public class SleeperQtyUomProperties {

    /** UOM spellings treated as measured quantities (compared ignoring case and extra spaces). */
    private List<String> measuredUoms = new ArrayList<>();

    /** True when the given UOM is one of the configured measured UOMs. */
    public boolean isMeasuredUom(String uom) {
        String value = normalize(uom);
        if (value.isEmpty() || measuredUoms == null) {
            return false;
        }
        return measuredUoms.stream().anyMatch(alias -> normalize(alias).equals(value));
    }

    private static String normalize(String s) {
        return s == null ? "" : s.trim().toLowerCase().replaceAll("\\s+", " ");
    }
}
