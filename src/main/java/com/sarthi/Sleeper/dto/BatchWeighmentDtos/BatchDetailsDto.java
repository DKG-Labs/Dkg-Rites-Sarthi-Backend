package com.sarthi.Sleeper.dto.BatchWeighmentDtos;


import lombok.Data;

@Data
public class BatchDetailsDto {

    private Long id;
    private String batchNo;
    private String proportionStatus;

    private Double ca1Ref;
    private Double ca2Ref;
    private Double faRef;
    private Double cementRef;
    private Double waterRef;
    private Double admixtureRef;

    private Double ca1Set;
    private Double ca2Set;
    private Double faSet;
    private Double cementSet;
    private Double waterSet;
    private Double admixtureSet;
}
