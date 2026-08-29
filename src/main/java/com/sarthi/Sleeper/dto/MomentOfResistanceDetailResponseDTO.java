package com.sarthi.Sleeper.dto;

import lombok.Data;

@Data
public class MomentOfResistanceDetailResponseDTO {

    private Long id;

    private String dataType;

    private Double ct;
    private Double cb;
    private Double rs1;
    private Double rs2;
}
