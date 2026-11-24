package com.sarthi.Sms.dto.testing;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CompletedTestDto {
    private Integer id;
    private String dutyId;
    private String testType;
    private String heatNumber;
    private Integer strandNumber;
    private String mill;
    private String railGrade;
    private String sampleType;
    private String sampleLot;
    private String sampleId;
    private String sampleIdentification;
    private String chemicalLadleStatus;
    private String chemicalProductStatus;
    private String fwtStatus;
    private String fwtTestStatus;
    private String macroStatus;
    private String microStatus;
    private Boolean microStructurePearlitic;
    private String tensileStatus;
    private String tensileTestStatus; // Frontend compatibility field
    private BigDecimal tensileYield;
    private BigDecimal tensileUts;
    private BigDecimal tensileEi;
    private String tensileFootStatus;
    private BigDecimal tensileFootYield;
    private BigDecimal tensileFootUts;
    private BigDecimal tensileFootEi;
    private String harnessStatus;
    private BigDecimal hardness1a;
    private BigDecimal hardness1b;
    private BigDecimal hardness1c;
    private BigDecimal hardness2a;
    private BigDecimal hardness2b;
    private BigDecimal hardness3;
    private BigDecimal hardness4a;
    private BigDecimal hardness4b;
    private BigDecimal hardnessRs;
    private String irStatus;
    private BigDecimal irSulphide;
    private BigDecimal irAluminate;
    private BigDecimal irSilicate;
    private BigDecimal irOxide;
    private String decarbStatus;
    private BigDecimal decarb1;
    private BigDecimal decarb2;
    private BigDecimal decarb3;
    private String n2Status;
    private BigDecimal n2;
    private String o2Status;
    private BigDecimal o2;
    private String fwtStStatus;
    private String rshStatus;
    private BigDecimal rshValue;
    private String phStatus;
    private BigDecimal phValue;
    private String spStatus;
    private BigDecimal spValue;
    private String mechanicalTestStatus;
    private String remarks;
    private LocalDateTime createdAt;
}