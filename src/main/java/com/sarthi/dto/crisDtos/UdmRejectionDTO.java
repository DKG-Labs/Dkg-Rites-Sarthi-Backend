package com.sarthi.dto.crisDtos;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class UdmRejectionDTO {

    @JsonAlias("PO_NO")
    private String poNumber;

    @JsonAlias("PO_DATE")
    private String poDate;

    @JsonAlias("PL_NO")
    private String plNumber;

    @JsonAlias("PO_SR")
    private String poSerialNumber;

    @JsonAlias("REJECTION_TYPE")
    private String typeOfRejection;

    @JsonAlias("REJECTION_NO")
    private String rejectionNumber;

    @JsonAlias("REJECTION_DATE")
    private String rejectionDate;

    @JsonAlias("REJECTION_QTY")
    private String rejectedQty;

    @JsonAlias("UNIT")
    private String uom;

    @JsonAlias("REJECTION_RESION")
    private String reasonForRejection;

    @JsonAlias("ITEM_DESC")
    private String itemDescription;

    @JsonAlias("INSP_AGENCY")
    private String inspectionAgency;

    @JsonAlias("IC_NO")
    private String icNumber;

    @JsonAlias("IC_DATE")
    private String icDate;

    @JsonAlias("VCODE")
    private String vendorCode;

    @JsonAlias("FIRM_NAME")
    private String vendorName;

    @JsonAlias("PDF_PATH")
    private String pdfPath;

    @JsonAlias("STATUS")
    private String status;

    @JsonAlias("CNAME")
    private String consignee;

    @JsonAlias("VALUE")
    private String rejectionValue;

    @JsonAlias("SORCE")
    private String source;

    @JsonAlias("RECPT_DATE")
    private String materialReceivedDate;
}
