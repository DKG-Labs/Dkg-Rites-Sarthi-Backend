package com.sarthi.dto.crisDtos;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class PoHeaderDto {

    @JsonProperty("POKEY")
    private String POKEY;

    @JsonProperty("PURCHASER_CD")
    private String PURCHASER_CD;

    @JsonProperty("IMMS_PURCHASER_CODE")
    private String IMMS_PURCHASER_CODE;

    @JsonProperty("IMMS_PURCHASER_DETAIL")
    private String IMMS_PURCHASER_DETAIL;

    @JsonProperty("STOCK_NONSTOCK")
    private String STOCK_NONSTOCK;

    @JsonProperty("RLY_NONRLY")
    private String RLY_NONRLY;

    @JsonProperty("PO_OR_LETTER")
    private String PO_OR_LETTER;

    @JsonProperty("PO_NO")
    private String PO_NO;

    @JsonProperty("L5NO_PO")
    private String L5NO_PO;

    @JsonProperty("PO_DT")
    private String PO_DT;

    @JsonProperty("RECV_DT")
    private String RECV_DT;

    @JsonProperty("VEND_CD")
    private String VEND_CD;

    @JsonProperty("IMMS_VENDOR_CODE")
    private String IMMS_VENDOR_CODE;

    @JsonProperty("VENDOR_DETAILS")
    private String VENDOR_DETAILS;

    @JsonProperty("FIRM_DETAILS")
    private String FIRM_DETAILS;

    @JsonProperty("RLY_CD")
    private String RLY_CD;

    @JsonProperty("RLY_SHORTNAME")
    private String RLY_SHORTNAME;

    @JsonProperty("REGION_CODE")
    private String REGION_CODE;

    @JsonProperty("REMARKS")
    private String REMARKS;

    @JsonProperty("BILL_PAY_OFF")
    private String BILL_PAY_OFF;

    @JsonProperty("BILL_PAY_OFF_NAME")
    private String BILL_PAY_OFF_NAME;

    @JsonProperty("USER_ID")
    private String USER_ID;

    @JsonProperty("DATETIME")
    private String DATETIME;

    @JsonProperty("INSPECTING_AGENCY")
    private String INSPECTING_AGENCY;

    @JsonProperty("POI_CD")
    private String POI_CD;

    @JsonProperty("PO_STATUS")
    private String PO_STATUS;

    @JsonProperty("PO_PDF_PATH")
    private String PO_PDF_PATH;

    @JsonProperty("ITEM_CAT")
    private String ITEM_CAT;

    @JsonProperty("ITEM_CAT_DESCR")
    private String ITEM_CAT_DESCR;

}
