package com.sarthi.dto.crisDtos;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class PoRequestDto {

    @JsonProperty("poHdr")
    @JsonAlias({"PoHdr", "PO_HDR", "po_hdr", "pohdr", "POHDR"})
    private PoHeaderDto poHdr;

    @JsonProperty("poDtl")
    @JsonAlias({"PoDtl", "PO_DTL", "po_dtl", "podtl", "PODTL"})
    private List<PoItemDto> poDtl;

}
