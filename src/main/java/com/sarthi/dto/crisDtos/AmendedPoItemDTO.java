package com.sarthi.dto.crisDtos;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class AmendedPoItemDTO {

    @JsonProperty("RLY")
    private String rly;

    @JsonProperty("POKEY")
    private String poKey;

    @JsonProperty("PO_SR")
    private String poSr;

    @JsonProperty("PL_NO")
    private String plNo;

    @JsonProperty("PO_QTY")
    private String poQty;

    @JsonProperty("UNIT")
    private String unit;

    @JsonProperty("CONSIGNEE")
    private String consigneeCd;

    @JsonProperty("RATE")
    private String rate;

    @JsonProperty("ITEM_VALUE")
    private String itemValue;

    @JsonProperty("QTY_CANCELLED")
    private String qtyCancelled;

    @JsonProperty("ALLOCATION")
    private String allocation;

    @JsonProperty("ORIG_DP")
    private String origDp;

    @JsonProperty("EXT_DP")
    private String extDp;

    @JsonProperty("BILL_PASS_OFF")
    private String billPassOff;

    @JsonProperty("BILL_PAY_OFF")
    private String billPayOff;

    @JsonProperty("CONSIGNEE_RLY")
    private String consigneeRly;

    @JsonProperty("P_RLY")
    private String pRly;

    @JsonProperty("INSP_AGENCY")
    private String inspAgency;

    @JsonProperty("DATE_OF_TRN")
    private String crisTimestamp;
}