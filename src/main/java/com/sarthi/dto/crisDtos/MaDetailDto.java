package com.sarthi.dto.crisDtos;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class MaDetailDto {



                @JsonProperty("RLY")
                private String RLY;

                @JsonProperty("MAKEY")
                private String MAKEY;

                @JsonProperty("SLNO")
                private String SLNO;

                @JsonProperty("MA_FLD")
                private String MA_FLD;

                @JsonProperty("MA_FLD_DESCR")
                private String MA_FLD_DESCR;

                @JsonProperty("OLD_VALUE")
                private String OLD_VALUE;

                @JsonProperty("NEW_VALUE")
                private String NEW_VALUE;

                @JsonProperty("NEW_VALUE_IND")
                private String NEW_VALUE_IND;

                @JsonProperty("NEW_VALUE_FLAG")
                private String NEW_VALUE_FLAG;

                @JsonProperty("PL_NO")
                private String PL_NO;

                @JsonProperty("PO_SR")
                private String PO_SR;

                @JsonProperty("COND_SLNO")
                private String COND_SLNO;

                @JsonProperty("COND_CODE")
                private String COND_CODE;

                @JsonProperty("MA_SR_NO")
                private String MA_SR_NO;

                @JsonProperty("STATUS")
                private String STATUS;

                @JsonProperty("ORIG_DP")
                private String ORIG_DP;
    @JsonProperty("EXP_SR")
    private String EXP_SR;

    @JsonProperty("EXP_CODE")
    private String EXP_CODE;

    @JsonProperty("COND_NO")
    private String COND_NO;

    @JsonProperty("PAYMENT_YEAR")
    private String PAYMENT_YEAR;

    @JsonProperty("NEW_POSR_DATA")
    private String NEW_POSR_DATA;

    @JsonProperty("REF_PONO")
    private String REF_PONO;

    @JsonProperty("CONSIGNEE_RLY")
    private String CONSIGNEE_RLY;

}
