package com.sarthi.dto.crisDtos;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class PoHeaderDto {

    @JsonProperty("POKEY")
    @JsonAlias({"poKey", "PO_KEY", "CASE_NO", "caseNo", "pokey", "Pokey"})
    private String POKEY;

    @JsonProperty("PURCHASER_CD")
    @JsonAlias({"purchaserCode", "purchaserCd", "purchaser_cd", "PURCHASER_CODE"})
    private String PURCHASER_CD;

    @JsonProperty("IMMS_PURCHASER_CODE")
    @JsonAlias({"immsPurchaserCode", "imms_purchaser_code", "PURCHASER_CODE", "purchaserCode"})
    private String IMMS_PURCHASER_CODE;

    @JsonProperty("IMMS_PURCHASER_DETAIL")
    @JsonAlias({"purchaserDetail", "immsPurchaserDetail", "imms_purchaser_detail", "purchaser_detail", "PURCHASER_DETAIL"})
    private String IMMS_PURCHASER_DETAIL;

    @JsonProperty("STOCK_NONSTOCK")
    @JsonAlias({"stockNonStock", "stock_nonstock", "STOCK_NON_STOCK"})
    private String STOCK_NONSTOCK;

    @JsonProperty("RLY_NONRLY")
    @JsonAlias({"rlyNonRly", "rly_nonrly", "RLY_NON_RLY"})
    private String RLY_NONRLY;

    @JsonProperty("PO_OR_LETTER")
    @JsonAlias({"poOrLetter", "po_or_letter", "PO_OR_LTR"})
    private String PO_OR_LETTER;

    @JsonProperty("PO_NO")
    @JsonAlias({"poNo", "PONO", "PO_NUMBER", "po_no", "poNumber", "PoNo"})
    private String PO_NO;

    @JsonProperty("L5NO_PO")
    @JsonAlias({"l5PoNo", "l5noPo", "L5PO_NO", "l5no_po", "L5NO_PONO", "l5noPono"})
    private String L5NO_PO;

    @JsonProperty("PO_DT")
    @JsonAlias({"poDate", "PO_DATE", "po_dt", "poDt", "PoDt", "PoDate", "po_date"})
    private String PO_DT;

    @JsonProperty("RECV_DT")
    @JsonAlias({"receivedDate", "RECV_DATE", "recvDt", "recv_dt", "RecvDt", "recvDate", "received_date"})
    private String RECV_DT;

    @JsonProperty("VEND_CD")
    @JsonAlias({"vendCd", "vend_cd", "VEND_CODE"})
    private String VEND_CD;

    @JsonProperty("IMMS_VENDOR_CODE")
    @JsonAlias({"vendorCode", "VCODE", "vendor_code", "IMMS_VEND_CD", "immsVendorCode", "imms_vendor_code", "VENDOR_CODE"})
    private String IMMS_VENDOR_CODE;

    @JsonProperty("VENDOR_DETAILS")
    @JsonAlias({"vendorDetails", "vendor_details", "VendorDetails"})
    private String VENDOR_DETAILS;

    @JsonProperty("FIRM_DETAILS")
    @JsonAlias({"firmDetails", "firm_details", "FirmDetails"})
    private String FIRM_DETAILS;

    @JsonProperty("RLY_CD")
    @JsonAlias({"rlyCd", "RLY", "rly", "RLY_CODE", "rly_cd", "RlyCd"})
    private String RLY_CD;

    @JsonProperty("RLY_SHORTNAME")
    @JsonAlias({"rlyShortName", "rlyShortname", "rly_short_name", "rly_shortname", "RLY_SHORT_NAME", "RlyShortname", "RLY_SHORTNAME"})
    private String RLY_SHORTNAME;

    @JsonProperty("REGION_CODE")
    @JsonAlias({"regionCode", "region_code", "RegionCode"})
    private String REGION_CODE;

    @JsonProperty("REMARKS")
    @JsonAlias({"remarks", "Remarks"})
    private String REMARKS;

    @JsonProperty("BILL_PAY_OFF")
    @JsonAlias({"billPayOff", "bill_pay_off", "BillPayOff"})
    private String BILL_PAY_OFF;

    @JsonProperty("BILL_PAY_OFF_NAME")
    @JsonAlias({"billPayOffName", "bill_pay_off_name", "BillPayOffName"})
    private String BILL_PAY_OFF_NAME;

    @JsonProperty("USER_ID")
    @JsonAlias({"userId", "user_id", "UserId"})
    private String USER_ID;

    @JsonProperty("DATETIME")
    @JsonAlias({"crisTimestamp", "datetime", "dateTime", "DATETIME_PO"})
    private String DATETIME;

    @JsonProperty("INSPECTING_AGENCY")
    @JsonAlias({"inspectingAgency", "INSP_AGENCY", "inspecting_agency", "InspectingAgency"})
    private String INSPECTING_AGENCY;

    @JsonProperty("POI_CD")
    @JsonAlias({"poiCd", "poi_cd", "PoiCd"})
    private String POI_CD;

    @JsonProperty("PO_STATUS")
    @JsonAlias({"poStatus", "po_status", "PoStatus"})
    private String PO_STATUS;

    @JsonProperty("PO_PDF_PATH")
    @JsonAlias({"pdfPath", "poPdfPath", "po_pdf_path", "PO_PDF_URL", "poPdfUrl", "pdf_path"})
    private String PO_PDF_PATH;

    @JsonProperty("ITEM_CAT")
    @JsonAlias({"itemCat", "item_cat", "ItemCat"})
    private String ITEM_CAT;

    @JsonProperty("ITEM_CAT_DESCR")
    @JsonAlias({"itemCatDescr", "itemCategory", "category", "item_cat_descr", "ItemCatDescr"})
    private String ITEM_CAT_DESCR;

}
