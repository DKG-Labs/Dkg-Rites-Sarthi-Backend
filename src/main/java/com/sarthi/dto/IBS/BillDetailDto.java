package com.sarthi.dto.IBS;

import lombok.Data;

@Data
public class BillDetailDto {

    private String billNo;

    private String invoiceNo;

    private String invoiceDate;

    private String caseNo;

    private String callDate;

    private Integer callSno;

    private String bkNo;

    private String setNo;

    private String invoicePdf;

    private String invoiceSuppDocs;
}
