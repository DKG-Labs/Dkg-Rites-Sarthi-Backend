package com.sarthi.Sms.dto.qct;

import lombok.Data;

@Data
public class QctTestingMasterDto {

    // Composite Key Fields
    private String dutyId;
    private String qctType;

    // Table Columns
    private Float strain;
    private Integer frequencyHz;
    private Float stress;
    private Float cycleEndured;
    private String remarks;
    private Integer numberOfSamplesReportedAsKq;
    private Integer meanOfValuesKq;
}
