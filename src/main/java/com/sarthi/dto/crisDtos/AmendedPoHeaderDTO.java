package com.sarthi.dto.crisDtos;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class AmendedPoHeaderDTO {


        @JsonProperty("POKEY")
        private String poKey;

        @JsonProperty("PO_NO")
        private String poNo;

        @JsonProperty("RLY")
        private String rlyCd;

        @JsonProperty("VCODE")
        private String vendorCode;

        @JsonProperty("INSP_AGENCY")
        private String inspectingAgency;

        @JsonProperty("PO_STATUS")
        private String poStatus;

        @JsonProperty("BILL_PAY_OFF")
        private String billPayOff;

        @JsonProperty("PUR_DIV")
        private String purDiv;

        @JsonProperty("PUR_SEC")
        private String purSec;

        @JsonProperty("PO_DATE")
        private String poDate;

        @JsonProperty("DATE_OF_TRN")
        private String crisTimestamp;
    }
