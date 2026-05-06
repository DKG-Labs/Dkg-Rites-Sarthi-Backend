package com.sarthi.dto.processmaterial;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ProcessStaticPeriodicCheckDTO {

    private Long id;
    private String inspectionCallNo;
    private String poNo;
    private String lineNo;
    private String shift;
    private String lotNo;
    private java.time.LocalDate dateOfInspection;
    @com.fasterxml.jackson.annotation.JsonProperty("shearingPressCapacityOk")
    private Boolean shearingPressCheck;
    
    @com.fasterxml.jackson.annotation.JsonProperty("forgingPressCapacityOk")
    private Boolean forgingPressCheck;
    
    @com.fasterxml.jackson.annotation.JsonProperty("reheatingFurnaceInductionType")
    private Boolean reheatingFurnaceCheck;
    
    @com.fasterxml.jackson.annotation.JsonProperty("quenchingWithin20Seconds")
    private Boolean quenchingTimeCheck;
    
    private Boolean forgingDieCheck;
    
    @com.fasterxml.jackson.annotation.JsonProperty("oilTankCounterValue")
    private Integer oilTankCounterValue;
    
    private Boolean allChecksPassed;
    private String remarks;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String createdBy;
    private String updatedBy;
}

