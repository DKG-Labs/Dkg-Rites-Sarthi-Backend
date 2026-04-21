package com.sarthi.dto.crisDtos;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class MaRequestDto {

    @JsonProperty("MMP_POMA_HDR")
    private MaHeaderDto MMP_POMA_HDR;

    @JsonProperty("MMP_POMA_DTL")
    private List<MaDetailDto> MMP_POMA_DTL;

}
