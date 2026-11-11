package com.sarthi.udmcomplaint.dto;

import lombok.Data;

@Data
public class SendToCmRequest {
    private String rio;
    private Integer cmUserId;
    private String remarks;
}
