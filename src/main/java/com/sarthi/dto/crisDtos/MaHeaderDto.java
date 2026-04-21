package com.sarthi.dto.crisDtos;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class MaHeaderDto {

                @JsonProperty("MAKEY")
                private String MAKEY;

                @JsonProperty("RLY")
                private String RLY;

                @JsonProperty("MAKEY_DATE")
                private String MAKEY_DATE;

                @JsonProperty("POKEY")
                private String POKEY;

                @JsonProperty("PO_NO")
                private String PO_NO;

                @JsonProperty("MA_NO")
                private String MA_NO;

                @JsonProperty("MA_DATE")
                private String MA_DATE;

                @JsonProperty("MA_TYPE")
                private String MA_TYPE;

                @JsonProperty("VCODE")
                private String VCODE;

                @JsonProperty("SUBJECT")
                private String SUBJECT;

                @JsonProperty("REF_NO")
                private String REF_NO;

                @JsonProperty("REF_DATE")
                private String REF_DATE;

                @JsonProperty("REMARKS")
                private String REMARKS;

                @JsonProperty("MA_SIGN_OFF")
                private String MA_SIGN_OFF;

                @JsonProperty("REQUEST_ID")
                private String REQUEST_ID;

                @JsonProperty("STATUS")
                private String STATUS;

                @JsonProperty("PUR_DIV")
                private String PUR_DIV;

                @JsonProperty("PUR_SEC")
                private String PUR_SEC;

                @JsonProperty("OLD_PO_VALUE")
                private String OLD_PO_VALUE;

                @JsonProperty("NEW_PO_VALUE")
                private String NEW_PO_VALUE;

                @JsonProperty("PO_MA_SRNO")
                private String PO_MA_SRNO;

                @JsonProperty("PUBLISH_FLAG")
                private String PUBLISH_FLAG;

    @JsonProperty("AUTH_SEQ")
    private String AUTH_SEQ;

    @JsonProperty("AUTH_SEQ_FIN")
    private String AUTH_SEQ_FIN;

    @JsonProperty("CURUSER")
    private String CURUSER;

    @JsonProperty("CURUSER_IND")
    private String CURUSER_IND;

    @JsonProperty("SIGN_ID")
    private String SIGN_ID;

    @JsonProperty("REQ_ID")
    private String REQ_ID;

    @JsonProperty("REC_IND")
    private String REC_IND;

    @JsonProperty("FLAG")
    private String FLAG;

    @JsonProperty("REQ_FLAG")
    private String REQ_FLAG;


}
