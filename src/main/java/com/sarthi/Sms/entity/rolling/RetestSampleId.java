package com.sarthi.Sms.entity.rolling;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RetestSampleId implements Serializable {
    
    private String heatNumber;
    private String strandNumber;
    private String testType;
    private String sampleLot;
}