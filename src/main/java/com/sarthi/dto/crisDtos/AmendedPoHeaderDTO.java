package com.sarthi.dto.crisDtos;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class AmendedPoHeaderDTO {

        @JsonProperty("POKEY")
        @JsonAlias({"poKey", "PO_KEY", "CASE_NO", "caseNo"})
        private String poKey;

        @JsonProperty("PO_NO")
        @JsonAlias({"poNo", "PONO", "PO_NUMBER", "PO_NUM", "po_no"})
        private String poNo;

        @JsonProperty("RLY")
        @JsonAlias({"rlyCd", "RLY_CD", "rly", "RLY_CODE"})
        private String rlyCd;

        @JsonProperty("VCODE")
        @JsonAlias({"vendorCode", "IMMS_VENDOR_CODE", "VENDOR_CODE", "vendor_code"})
        private String vendorCode;

        @JsonProperty("INSP_AGENCY")
        @JsonAlias({"inspectingAgency", "INSPECTING_AGENCY", "inspAgency"})
        private String inspectingAgency;

        @JsonProperty("PO_STATUS")
        @JsonAlias({"poStatus", "status"})
        private String poStatus;

        @JsonProperty("BILL_PAY_OFF")
        @JsonAlias({"billPayOff", "BILL_PAYING_OFFICER"})
        private String billPayOff;

        @JsonProperty("PUR_DIV")
        @JsonAlias({"purDiv"})
        private String purDiv;

        @JsonProperty("PUR_SEC")
        @JsonAlias({"purSec"})
        private String purSec;

        @JsonProperty("PO_DATE")
        @JsonAlias({"poDate", "PO_DT", "po_dt", "PO_DATETIME"})
        private String poDate;

        @JsonProperty("DATE_OF_TRN")
        @JsonAlias({"crisTimestamp", "DATETIME", "datetime", "CRIS_TIMESTAMP"})
        private String crisTimestamp;
}

