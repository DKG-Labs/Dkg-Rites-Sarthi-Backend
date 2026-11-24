package com.sarthi.Sleeper.dto;

import lombok.Data;

@Data
public class MomentOfResistanceRequestDTO {

    private String batchNumber;
    private String sleeperType;
    private String benchNumber;
    private String sleeperNo;

    private String testResult;
    private String remarks;

    private String vendorCode;
    private String plantId;
    private String shift;

    private Long createdBy;
    private Long updatedBy;

    /** Fresh or Retest (double testing); stored on create only */
    private String mrTestType;

    /** production_declaration.id (the lot) the samples are taken from */
    private Long productionDeclarationId;
}
