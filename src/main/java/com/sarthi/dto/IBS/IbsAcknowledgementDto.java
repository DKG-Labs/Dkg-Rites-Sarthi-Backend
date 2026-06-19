package com.sarthi.dto.IBS;


import lombok.Data;

@Data
public class IbsAcknowledgementDto {

    private String callNumber;

    private String status;
    private String reason;
}
