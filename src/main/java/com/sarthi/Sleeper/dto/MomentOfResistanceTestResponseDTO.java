package com.sarthi.Sleeper.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class MomentOfResistanceTestResponseDTO {

    private Long id;

    private String batchNumber;
    private String sleeperType;
    private String castingDate;

    private String vendorCode;
    private String plantId;
    private String shift;

    private Long createdBy;
    private LocalDateTime createdDate;

    private Long updatedBy;
    private LocalDateTime updatedDate;

    private List<MomentOfResistanceDetailResponseDTO> details;
}
