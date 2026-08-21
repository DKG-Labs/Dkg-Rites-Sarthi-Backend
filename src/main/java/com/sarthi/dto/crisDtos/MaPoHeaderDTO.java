package com.sarthi.dto.crisDtos;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class MaPoHeaderDTO {

    @JsonProperty("RLY")
    @JsonAlias({"RLY", "rly", "rlyCd", "rly_cd"})
    private String rly;

    @JsonProperty("MAKEY")
    @JsonAlias({"MAKEY", "MA_KEY", "maKey", "ma_key"})
    private String maKey;

    @JsonProperty("MAKEY_DATE")
    @JsonAlias({"MAKEY_DATE", "maKeyDate", "ma_key_date"})
    private String maKeyDate;

    @JsonProperty("POKEY")
    @JsonAlias({"POKEY", "PO_KEY", "poKey", "po_key"})
    private String poKey;

    @JsonProperty("PO_NO")
    @JsonAlias({"PO_NO", "PONO", "poNo", "po_no", "PO_NUM", "PO_NUMBER"})
    private String poNo;

    @JsonProperty("MA_NO")
    @JsonAlias({"MA_NO", "MANO", "maNo", "ma_no"})
    private String maNo;

    @JsonProperty("MA_DATE")
    @JsonAlias({"MA_DATE", "MADATE", "maDate", "ma_date"})
    private String maDate;

    @JsonProperty("MA_TYPE")
    @JsonAlias({"MA_TYPE", "maType", "ma_type"})
    private String maType;

    @JsonProperty("VCODE")
    @JsonAlias({"VCODE", "vendorCode", "vendor_code", "vcode"})
    private String vendorCode;

    @JsonProperty("SUBJECT")
    @JsonAlias({"SUBJECT", "subject"})
    private String subject;

    @JsonProperty("REF_NO")
    @JsonAlias({"REF_NO", "refNo", "ref_no"})
    private String refNo;

    @JsonProperty("REF_DATE")
    @JsonAlias({"REF_DATE", "refDate", "ref_date"})
    private String refDate;

    @JsonProperty("REMARKS")
    @JsonAlias({"REMARKS", "remarks"})
    private String remarks;

    @JsonProperty("MA_SIGN_OFF")
    @JsonAlias({"MA_SIGN_OFF", "maSignOff", "ma_sign_off"})
    private String maSignOff;

    @JsonProperty("REQUEST_ID")
    @JsonAlias({"REQUEST_ID", "requestId", "request_id"})
    private String requestId;

    @JsonProperty("AUTH_SEQ")
    @JsonAlias({"AUTH_SEQ", "authSeq", "auth_seq"})
    private String authSeq;

    @JsonProperty("AUTH_SEQ_FIN")
    @JsonAlias({"AUTH_SEQ_FIN", "authSeqFin", "auth_seq_fin"})
    private String authSeqFin;

    @JsonProperty("CURUSER")
    @JsonAlias({"CURUSER", "curUser", "cur_user"})
    private String curUser;

    @JsonProperty("CURUSER_IND")
    @JsonAlias({"CURUSER_IND", "curUserInd", "cur_user_ind"})
    private String curUserInd;

    @JsonProperty("SIGN_ID")
    @JsonAlias({"SIGN_ID", "signId", "sign_id"})
    private String signId;

    @JsonProperty("REQ_ID")
    @JsonAlias({"REQ_ID", "reqId", "req_id"})
    private String reqId;

    @JsonProperty("FIN_STATUS")
    @JsonAlias({"FIN_STATUS", "finStatus", "fin_status"})
    private String finStatus;

    @JsonProperty("REC_IND")
    @JsonAlias({"REC_IND", "recInd", "rec_ind"})
    private String recInd;

    @JsonProperty("FLAG")
    @JsonAlias({"FLAG", "flag"})
    private String flag;

    @JsonProperty("STATUS")
    @JsonAlias({"STATUS", "status"})
    private String status;

    @JsonProperty("PUR_DIV")
    @JsonAlias({"PUR_DIV", "purDiv", "pur_div"})
    private String purDiv;

    @JsonProperty("PUR_SEC")
    @JsonAlias({"PUR_SEC", "purSec", "pur_sec"})
    private String purSec;

    @JsonProperty("OLD_PO_VALUE")
    @JsonAlias({"OLD_PO_VALUE", "oldPoValue", "old_po_value"})
    private String oldPoValue;

    @JsonProperty("NEW_PO_VALUE")
    @JsonAlias({"NEW_PO_VALUE", "newPoValue", "new_po_value"})
    private String newPoValue;

    @JsonProperty("PO_MA_SRNO")
    @JsonAlias({"PO_MA_SRNO", "poMaSrNo", "po_ma_sr_no"})
    private String poMaSrNo;

    @JsonProperty("PUBLISH_FLAG")
    @JsonAlias({"PUBLISH_FLAG", "publishFlag", "publish_flag"})
    private String publishFlag;

    @JsonProperty("SENT4VET")
    @JsonAlias({"SENT4VET", "sent4Vet", "sent4_vet"})
    private String sent4Vet;

    @JsonProperty("VET_DATE")
    @JsonAlias({"VET_DATE", "vetDate", "vet_date"})
    private String vetDate;

    @JsonProperty("VET_BY")
    @JsonAlias({"VET_BY", "vetBy", "vet_by"})
    private String vetBy;

    @JsonProperty("REQ_FLAG")
    @JsonAlias({"REQ_FLAG", "reqFlag", "req_flag"})
    private String reqFlag;
}
