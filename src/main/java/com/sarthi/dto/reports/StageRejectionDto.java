package com.sarthi.dto.reports;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StageRejectionDto {
    private String name;
    private double value;
    private String color;
    private double cumulative; // used for Pareto chart cumulative % line (0 by default for other usages)

    // Convenience 3-arg constructor so existing callers are unchanged
    public StageRejectionDto(String name, double value, String color) {
        this.name = name;
        this.value = value;
        this.color = color;
        this.cumulative = 0.0;
    }
}
