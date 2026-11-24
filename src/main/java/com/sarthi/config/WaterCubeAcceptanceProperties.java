package com.sarthi.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Water cube (water cured cube) lot acceptance rules, bound from {@code sleeper.water-cube.*} in application.properties.
 * <p>
 * X = mean of the sample averages, Y = lowest sample average, Fck = characteristic strength of the concrete grade.
 * <ul>
 *   <li>Any sample's max deviation from its own average above {@link #variationLimitPercent} → FAIL</li>
 *   <li>Condition 1: X ≥ Fck + cond1XMargin and Y ≥ Fck − cond1YMargin → PASS, {@link #mrSamplesCond1} MR samples</li>
 *   <li>Condition 2: X ≥ Fck and Y ≥ Fck − cond2YMargin (but not condition 1) → PASS, {@link #mrSamplesCond2} MR samples</li>
 *   <li>Condition 3: X &lt; Fck or Y &lt; Fck − cond2YMargin → FAIL</li>
 * </ul>
 * A FAIL rejects the whole lot (batch returned to vendor, every sleeper rejected).
 */
@Data
@Component
@ConfigurationProperties(prefix = "sleeper.water-cube")
public class WaterCubeAcceptanceProperties {

    /** Number of cube samples (sets) per lot. */
    private Integer sampleCount;

    /** Cubes in each sample. */
    private Integer cubesPerSample;

    /** Max allowed deviation (%) of a cube from its sample average. */
    private Double variationLimitPercent;

    private Double cond1XMargin;
    private Double cond1YMargin;
    private Double cond2YMargin;

    private Integer mrSamplesCond1;
    private Integer mrSamplesCond2;

    /** Result values written to water_cube_strength_test.final_test_result. */
    private String passResult;
    private String failResult;

    /** Reason stored on the rejected batch and shown to the IE and the vendor. */
    private String rejectionReason;

    /** Inspection call statuses that no longer hold the batch (not reported as affected calls). */
    private List<String> closedCallStatuses = new ArrayList<>();

    public int requiredCubeCount() {
        return sampleCount * cubesPerSample;
    }
}
