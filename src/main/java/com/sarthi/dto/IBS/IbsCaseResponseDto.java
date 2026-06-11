package com.sarthi.dto.IBS;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class IbsCaseResponseDto {
    private Integer resultFlag;
    private String message;
    private DataDto data;
    @Data
    public static class DataDto {
        @JsonProperty("POKEY")
        private String poKey;
        @JsonProperty("PO_NO")
        private String poNo;
        @JsonProperty("PO_DT")
        private String poDate;
        @JsonProperty("RLY_CD")
        private String rlyCd;
        @JsonProperty("CASE_NO")
        private String caseNo;
        @JsonProperty("STATUS")
        private String status;
        @JsonProperty("UPDATED_DT")
        private String updatedDt;
    }

    }