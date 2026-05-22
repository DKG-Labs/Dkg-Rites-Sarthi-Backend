package com.sarthi.dto.reports;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SqcReportDto {
    private int slNo;
    private String companyName;
    private String companyUnit;
    private double cp;
    private double cpk;
    private double sqcRating;
    private int sampleCount;
    private java.util.List<Double> diaValues;
    private double ucl;
    private double lcl;
}
