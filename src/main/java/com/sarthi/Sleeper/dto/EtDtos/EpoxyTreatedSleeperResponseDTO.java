package com.sarthi.Sleeper.dto.EtDtos;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class EpoxyTreatedSleeperResponseDTO {

    private Long id;


    private String location;
    private String dateOfCasting;
    private String batchNumber;
    private String sleeperType;

    private String remark;
    private Boolean isConfirmed;

    // One-to-Many sleepers
    private List<EtSleeperDTO> sleepers;

    // Extra fields
    private String shift;
    private String vendorCode;
    private String plantId;

    private Long createdBy;
    private LocalDateTime createdDate;

    private Long updatedBy;
    private LocalDateTime updatedDate;

    private String status;

}
