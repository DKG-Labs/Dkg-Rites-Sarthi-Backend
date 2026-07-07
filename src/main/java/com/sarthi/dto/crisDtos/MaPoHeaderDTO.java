package com.sarthi.dto.crisDtos;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class MaPoHeaderDTO {

    @JsonProperty("RLY")
    private String rly;

    @JsonProperty("MAKEY")
    private String maKey;

    @JsonProperty("MAKEY_DATE")
    private String maKeyDate;

    @JsonProperty("POKEY")
    private String poKey;

    @JsonProperty("PO_NO")
    private String poNo;

    @JsonProperty("MA_NO")
    private String maNo;

    @JsonProperty("MA_DATE")
    private String maDate;

    @JsonProperty("MA_TYPE")
    private String maType;

    @JsonProperty("VCODE")
    private String vendorCode;

    @JsonProperty("SUBJECT")
    private String subject;

    @JsonProperty("REF_NO")
    private String refNo;

    @JsonProperty("REF_DATE")
    private String refDate;

    @JsonProperty("REMARKS")
    private String remarks;

    @JsonProperty("MA_SIGN_OFF")
    private String maSignOff;

    @JsonProperty("REQUEST_ID")
    private String requestId;

    @JsonProperty("AUTH_SEQ")
    private String authSeq;

    @JsonProperty("AUTH_SEQ_FIN")
    private String authSeqFin;

    @JsonProperty("CURUSER")
    private String curUser;

    @JsonProperty("CURUSER_IND")
    private String curUserInd;

    @JsonProperty("SIGN_ID")
    private String signId;

    @JsonProperty("REQ_ID")
    private String reqId;

    @JsonProperty("FIN_STATUS")
    private String finStatus;

    @JsonProperty("REC_IND")
    private String recInd;

    @JsonProperty("FLAG")
    private String flag;

    @JsonProperty("STATUS")
    private String status;

    @JsonProperty("PUR_DIV")
    private String purDiv;

    @JsonProperty("PUR_SEC")
    private String purSec;

    @JsonProperty("OLD_PO_VALUE")
    private String oldPoValue;

    @JsonProperty("NEW_PO_VALUE")
    private String newPoValue;

    @JsonProperty("PO_MA_SRNO")
    private String poMaSrNo;

    @JsonProperty("PUBLISH_FLAG")
    private String publishFlag;

    @JsonProperty("SENT4VET")
    private String sent4Vet;

    @JsonProperty("VET_DATE")
    private String vetDate;

    @JsonProperty("VET_BY")
    private String vetBy;

    @JsonProperty("REQ_FLAG")
    private String reqFlag;
}
