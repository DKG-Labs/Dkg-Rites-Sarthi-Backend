package com.sarthi.Sleeper.dto.BatchWeighmentDtos;

import lombok.Data;
import java.util.List;

@Data
public class BatchWeighmentResponseDto {

    private Long id;
    private String lineNo;
    private String entryDate;
    private String sandType;
    private String moistureSensorStatus;

    private String verifiedBy;
    private String remarks;
    private String entryMode;

    private String vendorCode;
    private String plantId;
    private String shift;

    private List<BatchDetailsDto> batchDetails;
    private List<ScadaWeighmentDto> scadaRecords;
    private List<ManualWeighmentDto> manualRecords;
}
