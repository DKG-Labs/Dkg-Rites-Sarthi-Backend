package com.sarthi.dto.crisDtos;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class PoCancellationDetailDto {


        @JsonProperty("RLY")
        private String rly;

        @JsonProperty("CAKEY")
        private String cakey;

        @JsonProperty("SLNO")
        private String slno;

        @JsonProperty("PL_NO")
        private String plNo;

        @JsonProperty("PO_SR")
        private String poSr;

        @JsonProperty("PO_BAL_QTY")
        private String poBalQty;

        @JsonProperty("CANC_QTY")
        private String cancQty;

        @JsonProperty("STATUS")
        private String status;

        @JsonProperty("DEM_STATUS")
        private String demStatus;

}
