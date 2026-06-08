package com.sarthi.dto.IBS;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class IbsCaseRequestDto {
    @JsonProperty("POKEY")
    private String poKey;
    @JsonProperty("PO_NO")
    private String poNo;
    @JsonProperty("PO_DT")
    private String poDate;
    @JsonProperty("RLY_CD")
    private String rlyCd;
}