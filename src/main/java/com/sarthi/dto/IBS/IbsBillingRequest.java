package com.sarthi.dto.IBS;

import lombok.Data;

@Data
public class IbsBillingRequest {

    private String caseNo;
    private String callRecvDt;
    private Integer callSno;
}
