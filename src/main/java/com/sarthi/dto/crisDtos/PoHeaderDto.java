package com.sarthi.dto.crisDtos;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class PoHeaderDto {

    @JsonProperty("POKEY")
    @JsonAlias({"poKey", "PO_KEY", "CASE_NO", "caseNo"})
    private String POKEY;

    @JsonProperty("PURCHASER_CD")
    @JsonAlias({"purchaserCode", "purchaserCd"})
    private String PURCHASER_CD;

    @JsonProperty("IMMS_PURCHASER_CODE")
    @JsonAlias({"immsPurchaserCode"})
    private String IMMS_PURCHASER_CODE;

    @JsonProperty("IMMS_PURCHASER_DETAIL")
    @JsonAlias({"purchaserDetail", "immsPurchaserDetail"})
    private String IMMS_PURCHASER_DETAIL;

    @JsonProperty("STOCK_NONSTOCK")
    @JsonAlias({"stockNonStock"})
    private String STOCK_NONSTOCK;

    @JsonProperty("RLY_NONRLY")
    @JsonAlias({"rlyNonRly"})
    private String RLY_NONRLY;

    @JsonProperty("PO_OR_LETTER")
    @JsonAlias({"poOrLetter"})
    private String PO_OR_LETTER;

    @JsonProperty("PO_NO")
    @JsonAlias({"poNo", "PONO", "PO_NUMBER"})
    private String PO_NO;

    @JsonProperty("L5NO_PO")
    @JsonAlias({"l5PoNo", "l5noPo", "L5PO_NO"})
    private String L5NO_PO;

    @JsonProperty("PO_DT")
    @JsonAlias({"poDate", "PO_DATE", "po_dt"})
    private String PO_DT;

    @JsonProperty("RECV_DT")
    @JsonAlias({"receivedDate", "RECV_DATE", "recvDt"})
    private String RECV_DT;

    @JsonProperty("VEND_CD")
    @JsonAlias({"vendCd"})
    private String VEND_CD;

    @JsonProperty("IMMS_VENDOR_CODE")
    @JsonAlias({"vendorCode", "VCODE", "vendor_code", "IMMS_VEND_CD"})
    private String IMMS_VENDOR_CODE;

    @JsonProperty("VENDOR_DETAILS")
    @JsonAlias({"vendorDetails", "vendor_details"})
    private String VENDOR_DETAILS;

    @JsonProperty("FIRM_DETAILS")
    @JsonAlias({"firmDetails", "firm_details"})
    private String FIRM_DETAILS;

    @JsonProperty("RLY_CD")
    @JsonAlias({"rlyCd", "RLY", "rly", "RLY_CODE"})
    private String RLY_CD;

    @JsonProperty("RLY_SHORTNAME")
    @JsonAlias({"rlyShortName", "rlyShortname"})
    private String RLY_SHORTNAME;

    @JsonProperty("REGION_CODE")
    @JsonAlias({"regionCode"})
    private String REGION_CODE;

    @JsonProperty("REMARKS")
    @JsonAlias({"remarks"})
    private String REMARKS;

    @JsonProperty("BILL_PAY_OFF")
    @JsonAlias({"billPayOff"})
    private String BILL_PAY_OFF;

    @JsonProperty("BILL_PAY_OFF_NAME")
    @JsonAlias({"billPayOffName"})
    private String BILL_PAY_OFF_NAME;

    @JsonProperty("USER_ID")
    @JsonAlias({"userId"})
    private String USER_ID;

    @JsonProperty("DATETIME")
    @JsonAlias({"crisTimestamp", "datetime"})
    private String DATETIME;

    @JsonProperty("INSPECTING_AGENCY")
    @JsonAlias({"inspectingAgency", "INSP_AGENCY"})
    private String INSPECTING_AGENCY;

    @JsonProperty("POI_CD")
    @JsonAlias({"poiCd"})
    private String POI_CD;

    @JsonProperty("PO_STATUS")
    @JsonAlias({"poStatus"})
    private String PO_STATUS;

    @JsonProperty("PO_PDF_PATH")
    @JsonAlias({"pdfPath", "poPdfPath"})
    private String PO_PDF_PATH;

    @JsonProperty("ITEM_CAT")
    @JsonAlias({"itemCat"})
    private String ITEM_CAT;

    @JsonProperty("ITEM_CAT_DESCR")
    @JsonAlias({"itemCatDescr", "itemCategory", "category"})
    private String ITEM_CAT_DESCR;

}
