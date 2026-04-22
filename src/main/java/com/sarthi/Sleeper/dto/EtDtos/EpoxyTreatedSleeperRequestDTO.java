package com.sarthi.Sleeper.dto.EtDtos;

import lombok.Data;

import java.util.List;
@Data
public class EpoxyTreatedSleeperRequestDTO {

    private String location;          // Shed / Line
    private String dateOfCasting;     // dd/MM/yyyy
    private String batchNumber;
    private String sleeperType;

    private String remark;
    private Boolean isConfirmed;


    private List<EtSleeperDTO> sleepers;

    // Extra fields
    private String shift;             // A/B/C
    private String vendorCode;
    private String plantId;

    private Long createdBy;           // for create
    private Long updatedBy;
}
