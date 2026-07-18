package com.sarthi.dto.IBS;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class PaymentDetailDto {

    private String caseNo;

    private String callRecvDt;

    private Integer callSno;

    private String description;

    private String merTxnId;

    private BigDecimal amount;

    private String custEmail;

    private String custMobile;

    private String txnCompleteDate;
}
