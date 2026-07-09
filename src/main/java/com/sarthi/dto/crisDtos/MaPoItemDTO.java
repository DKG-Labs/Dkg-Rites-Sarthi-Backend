package com.sarthi.dto.crisDtos;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class MaPoItemDTO {

    @JsonProperty("RLY")
    private String rly;

    @JsonProperty("MAKEY")
    private String maKey;

    @JsonProperty("SLNO")
    private String slNo;

    @JsonProperty("MA_FLD")
    private String maFld;

    @JsonProperty("MA_FLD_DESCR")
    private String maFldDescr;

    @JsonProperty("OLD_VALUE")
    private String oldValue;

    @JsonProperty("NEW_VALUE")
    private String newValue;

    @JsonProperty("NEW_VALUE_IND")
    private String newValueInd;

    @JsonProperty("NEW_VALUE_FLAG")
    private String newValueFlag;

    @JsonProperty("PL_NO")
    private String plNo;

    @JsonProperty("PO_SR")
    private String poSr;

    @JsonProperty("EXP_SR")
    private String expSr;

    @JsonProperty("EXP_CODE")
    private String expCode;

    @JsonProperty("COND_SLNO")
    private String condSlNo;

    @JsonProperty("COND_NO")
    private String condNo;

    @JsonProperty("COND_CODE")
    private String condCode;

    @JsonProperty("STATUS")
    private String status;

    @JsonProperty("MA_SR_NO")
    private String maSrNo;

    @JsonProperty("ORIG_DP")
    private String origDp;

    @JsonProperty("PAYMENT_YEAR")
    private String paymentYear;

    @JsonProperty("NEW_POSR_DATA")
    private String newPoSrData;

    @JsonProperty("REF_PONO")
    private String refPoNo;

    @JsonProperty("CONSIGNEE_RLY")
    private String consigneeRly;
}