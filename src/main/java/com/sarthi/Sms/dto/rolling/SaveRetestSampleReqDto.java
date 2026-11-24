package com.sarthi.Sms.dto.rolling;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SaveRetestSampleReqDto {
    private String railGrade;
    private String heatNumber;
    private List<String> strandNumber; // Array of selected strands from multiple retest samples
    private String sampleId;
    private String sampleLot;
    private String testType;
    private String dutyId;
    private List<RetestSampleDetail> retestSamples; // Individual retest sample details

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RetestSampleDetail {
        private String retestNumber; // "Retest 1", "Retest 2", "Retest 3"
        private String selectedStrand; // "I", "II", "III", "IV", "V", "VI"
    }
}