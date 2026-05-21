package com.sarthi.dto.summaryDtos;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProcessSummaryDto {

    private Integer shearingProductionQty;
    private Integer shearingRejectionQty;

    private Integer turningProductionQty;
    private Integer turningRejectionQty;

    private Integer mpiProductionQty;
    private Integer mpiRejectionQty;

    private Integer forgingProductionQty;
    private Integer forgingRejectionQty;

    private Integer quenchingProductionQty;
    private Integer quenchingRejectionQty;

    private Integer temperingProductionQty;
    private Integer temperingRejectionQty;
}