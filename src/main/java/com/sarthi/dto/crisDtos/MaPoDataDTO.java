package com.sarthi.dto.crisDtos;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class MaPoDataDTO {

    @JsonProperty("MMP_POMA_HDR")
    @JsonAlias({"MMP_POMA_HDR", "mmpPomaHdr", "MmpPomaHdr", "mmp_poma_hdr", "MA_HDR", "maHdr", "ma_hdr", "MMP_PO_HDR"})
    private MaPoHeaderDTO mmpPomaHdr;

    @JsonProperty("MMP_POMA_DTL")
    @JsonAlias({"MMP_POMA_DTL", "mmpPomaDtl", "MmpPomaDtl", "mmp_poma_dtl", "MA_DTL", "maDtl", "ma_dtl", "MMP_PO_DTL"})
    private List<MaPoItemDTO> mmpPomaDtl;

    @JsonProperty("PoHdr")
    @JsonAlias({"PoHdr", "PO_HDR", "poHdr", "po_hdr", "MMP_PO_HDR", "mmpPoHdr"})
    private AmendedPoHeaderDTO poHdr;

    @JsonProperty("PoDtl")
    @JsonAlias({"PoDtl", "PO_DTL", "poDtl", "po_dtl", "MMP_PO_DTL", "mmpPoDtl"})
    private List<AmendedPoItemDTO> poDtl;

    @JsonProperty("maNo")
    @JsonAlias({"maNo", "ma_no", "MA_NO", "MANO"})
    private String maNo;

    @JsonProperty("maDate")
    @JsonAlias({"maDate", "ma_date", "MA_DATE", "MADATE"})
    private String maDate;
}
