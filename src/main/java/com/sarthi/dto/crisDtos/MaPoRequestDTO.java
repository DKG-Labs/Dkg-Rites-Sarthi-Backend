package com.sarthi.dto.crisDtos;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class MaPoRequestDTO {

    private String status;

    private String message;

    private List<String> error;

    private String timestamp;

    private MaPoDataDTO data;

    @JsonProperty("maNo")
    @JsonAlias({"maNo", "ma_no", "MA_NO", "MANO"})
    private String maNo;

    @JsonProperty("maDate")
    @JsonAlias({"maDate", "ma_date", "MA_DATE", "MADATE"})
    private String maDate;

    @JsonProperty("rly")
    @JsonAlias({"rly", "RLY", "rlyCd", "rly_cd"})
    private String rly;

    @JsonProperty("poNo")
    @JsonAlias({"poNo", "po_no", "PO_NO", "PONO"})
    private String poNo;

    @JsonProperty("poKey")
    @JsonAlias({"poKey", "po_key", "POKEY", "PO_KEY"})
    private String poKey;

    @JsonProperty("vcode")
    @JsonAlias({"vcode", "vendorCode", "vendor_code", "VCODE"})
    private String vendorCode;

}
