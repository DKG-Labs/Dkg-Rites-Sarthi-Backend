package com.sarthi.dto.crisDtos;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class PoCancellationHeaderDto {

        @JsonProperty("RLY")
        private String rly;

        @JsonProperty("CAKEY")
        private String cakey;

        @JsonProperty("CAKEY_DATE")
        private String cakeyDate;

        @JsonProperty("POKEY")
        private String pokey;

        @JsonProperty("PO_NO")
        private String poNo;

        @JsonProperty("CA_NO")
        private String caNo;

        @JsonProperty("CA_DATE")
        private String caDate;

        @JsonProperty("CA_TYPE")
        private String caType;

        @JsonProperty("VCODE")
        private String vcode;

        @JsonProperty("REF_NO")
        private String refNo;

        @JsonProperty("REF_DATE")
        private String refDate;

        @JsonProperty("REMARKS")
        private String remarks;

        @JsonProperty("CA_SIGN_OFF")
        private String caSignOff;

        @JsonProperty("REQUEST_ID")
        private String requestId;

        @JsonProperty("AUTH_SEQ")
        private String authSeq;

        @JsonProperty("AUTH_SEQ_FIN")
        private String authSeqFin;

        @JsonProperty("CURUSER")
        private String curuser;

        @JsonProperty("CURUSER_IND")
        private String curuserInd;

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

        @JsonProperty("RECOVERY_AMT")
        private String recoveryAmt;

        @JsonProperty("RECADV_NO")
        private String recadvNo;

        @JsonProperty("PO_MA_SRNO")
        private String poMaSrno;

        @JsonProperty("CA_REASON")
        private String caReason;

        @JsonProperty("REINST_NO")
        private String reinstNo;

        @JsonProperty("REINST_DATE")
        private String reinstDate;

        @JsonProperty("REINST_REMARKS")
        private String reinstRemarks;

        @JsonProperty("PUBLISH_FLAG")
        private String publishFlag;

        @JsonProperty("SENT4VET")
        private String sent4vet;

        @JsonProperty("VET_DATE")
        private String vetDate;

        @JsonProperty("VET_BY")
        private String vetBy;


}
