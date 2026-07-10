package com.sarthi.dto;

import lombok.Data;

@Data
public class PendingFeedbackRequestDto {

    private Integer roleId;
    private String productType;
    private String vendorCode;
    private Integer createdBy;

}
