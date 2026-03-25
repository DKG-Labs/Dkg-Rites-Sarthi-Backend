package com.sarthi.Sleeper.dto;

import lombok.Data;

import java.util.List;

@Data
public class BenchQueryRequestDTO {
    private String plantType;   // STRESS / LONG_LINE
    private List<Integer> benchNumbers; // for STRESS
    private List<Integer> gangNumbers;  // for LONG_LINE
}
