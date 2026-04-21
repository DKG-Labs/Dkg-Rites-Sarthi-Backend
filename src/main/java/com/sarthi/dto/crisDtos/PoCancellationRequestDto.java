package com.sarthi.dto.crisDtos;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class PoCancellationRequestDto {

    @JsonProperty("MMP_POCA_HDR")
    private PoCancellationHeaderDto header;

    @JsonProperty("MMP_POCA_DTL")
    private List<PoCancellationDetailDto> details;

}
