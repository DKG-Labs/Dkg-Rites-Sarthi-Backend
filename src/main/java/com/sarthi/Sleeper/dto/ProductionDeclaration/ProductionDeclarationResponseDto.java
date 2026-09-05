package com.sarthi.Sleeper.dto.ProductionDeclaration;

import lombok.Data;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Data
public class ProductionDeclarationResponseDto {

    private Long id;

    private String plantType;
    private String productionUnit;

    private String castingDate;
    private String shift;

    private String batchNumber;
    private String mixDesignReference;
    private String sleeperType;
    private String drawingNo;

    private LocalTime lbcTime;
    private String poNo;

    private Integer totalCastedSleepers;
    private Integer totalSleeperTypes;
    private Double totalRft;

    private String remarks;

    private Long createdBy;
    private LocalDateTime createdDate;

    private Long updatedBy;
    private LocalDateTime updatedDate;

    private String status;

    private String vendorCode;
    private String plantId;
    private List<ProductionStressChamberResponseDto> chambers;

    private List<ProductionLongLineGangResponseDto> gangs;


    private Boolean waterCubeTestStatus;
    private Boolean condition2;
    private Integer mrSamplesRequired;

}
