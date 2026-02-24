package com.sarthi.Sleeper.dto.BatchWeighmentDtos;


import lombok.Data;
import java.util.List;

@Data
public class BatchWeighmentRequestDto {

    private String lineNo;
    private String entryDate;
    private String sandType;
    private String moistureSensorStatus;

    private String verifiedBy;
    private String remarks;
    private String entryMode;

    private int createdBy;
    private int updatedBy;

    private List<BatchDetailsDto> batchDetails;
    private List<ScadaWeighmentDto> scadaRecords;
    private List<ManualWeighmentDto> manualRecords;
}
