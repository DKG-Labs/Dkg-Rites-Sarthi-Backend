package com.sarthi.dto.crisDtos;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class MaPoDataDTO {

    @JsonProperty("MMP_POMA_HDR")
    private MaPoHeaderDTO mmpPomaHdr;

    @JsonProperty("MMP_POMA_DTL")
    private List<MaPoItemDTO> mmpPomaDtl;

//    @JsonProperty("mmpPoHdr")
//    private AmendedPoHeaderDTO mmpPoHdr;
//
//    @JsonProperty("mmpPoItem")
//    private List<AmendedPoItemDTO> mmpPoItem;

    @JsonProperty("PoHdr")
    private AmendedPoHeaderDTO poHdr;

    @JsonProperty("PoDtl")
    private List<AmendedPoItemDTO> poDtl;
}
