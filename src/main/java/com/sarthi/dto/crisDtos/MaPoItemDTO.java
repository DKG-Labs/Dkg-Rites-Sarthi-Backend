package com.sarthi.dto.crisDtos;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class MaPoItemDTO {

    @JsonProperty("RLY")
    @JsonAlias({"RLY", "rly", "rlyCd", "rly_cd"})
    private String rly;

    @JsonProperty("MAKEY")
    @JsonAlias({"MAKEY", "MA_KEY", "maKey", "ma_key"})
    private String maKey;

    @JsonProperty("SLNO")
    @JsonAlias({"SLNO", "SL_NO", "slNo", "sl_no"})
    private String slNo;

    @JsonProperty("MA_FLD")
    @JsonAlias({"MA_FLD", "MAFLD", "maFld", "ma_fld"})
    private String maFld;

    @JsonProperty("MA_FLD_DESCR")
    @JsonAlias({"MA_FLD_DESCR", "MAFLDDESCR", "maFldDescr", "ma_fld_descr"})
    private String maFldDescr;

    @JsonProperty("OLD_VALUE")
    @JsonAlias({"OLD_VALUE", "OLDVALUE", "oldValue", "old_value"})
    private String oldValue;

    @JsonProperty("NEW_VALUE")
    @JsonAlias({"NEW_VALUE", "NEWVALUE", "newValue", "new_value"})
    private String newValue;

    @JsonProperty("NEW_VALUE_IND")
    @JsonAlias({"NEW_VALUE_IND", "newValueInd", "new_value_ind"})
    private String newValueInd;

    @JsonProperty("NEW_VALUE_FLAG")
    @JsonAlias({"NEW_VALUE_FLAG", "newValueFlag", "new_value_flag"})
    private String newValueFlag;

    @JsonProperty("PL_NO")
    @JsonAlias({"PL_NO", "PLNO", "plNo", "pl_no"})
    private String plNo;

    @JsonProperty("PO_SR")
    @JsonAlias({"PO_SR", "POSR", "poSr", "po_sr", "ITEM_SRNO", "itemSrNo", "ITEM_SR_NO"})
    private String poSr;

    @JsonProperty("EXP_SR")
    @JsonAlias({"EXP_SR", "expSr", "exp_sr"})
    private String expSr;

    @JsonProperty("EXP_CODE")
    @JsonAlias({"EXP_CODE", "expCode", "exp_code"})
    private String expCode;

    @JsonProperty("COND_SLNO")
    @JsonAlias({"COND_SLNO", "condSlNo", "cond_sl_no"})
    private String condSlNo;

    @JsonProperty("COND_NO")
    @JsonAlias({"COND_NO", "condNo", "cond_no"})
    private String condNo;

    @JsonProperty("COND_CODE")
    @JsonAlias({"COND_CODE", "condCode", "cond_code"})
    private String condCode;

    @JsonProperty("STATUS")
    @JsonAlias({"STATUS", "status"})
    private String status;

    @JsonProperty("MA_SR_NO")
    @JsonAlias({"MA_SR_NO", "maSrNo", "ma_sr_no"})
    private String maSrNo;

    @JsonProperty("ORIG_DP")
    @JsonAlias({"ORIG_DP", "origDp", "orig_dp"})
    private String origDp;

    @JsonProperty("PAYMENT_YEAR")
    @JsonAlias({"PAYMENT_YEAR", "paymentYear", "payment_year"})
    private String paymentYear;

    @JsonProperty("NEW_POSR_DATA")
    @JsonAlias({"NEW_POSR_DATA", "newPoSrData", "new_po_sr_data"})
    private String newPoSrData;

    @JsonProperty("REF_PONO")
    @JsonAlias({"REF_PONO", "refPoNo", "ref_po_no", "REF_PO_NO"})
    private String refPoNo;

    @JsonProperty("CONSIGNEE_RLY")
    @JsonAlias({"CONSIGNEE_RLY", "consigneeRly", "consignee_rly"})
    private String consigneeRly;
}