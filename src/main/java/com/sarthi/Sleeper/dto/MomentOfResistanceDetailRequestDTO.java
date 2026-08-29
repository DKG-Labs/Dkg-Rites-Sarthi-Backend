package com.sarthi.Sleeper.dto;

import lombok.Data;

@Data
public class MomentOfResistanceDetailRequestDTO {

    private String dataType; // SCADA / MANUAL

    private Double ct;
    private Double cb;
    private Double rs1;
    private Double rs2;
}
