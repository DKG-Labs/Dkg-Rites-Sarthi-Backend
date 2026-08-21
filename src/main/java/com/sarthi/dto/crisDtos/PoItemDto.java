package com.sarthi.dto.crisDtos;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class PoItemDto {


        @JsonProperty("RLY")
        private String RLY;

        @JsonProperty("POKEY")
        private String POKEY;

        @JsonProperty("PL_NO")
        private String PL_NO;

        @JsonProperty("ITEM_SRNO")
        @JsonAlias({"PO_SR", "ITEM_SR_NO", "PO_SERIAL_NO", "SLNO"})
        private String ITEM_SRNO;

        @JsonProperty("ITEM_DESC")
        private String ITEM_DESC;

        @JsonProperty("CONSIGNEE_CD")
        private String CONSIGNEE_CD;

        @JsonProperty("IMMS_CONSIGNEE_CD")
        private String IMMS_CONSIGNEE_CD;

        @JsonProperty("IMMS_CONSIGNEE_NAME")
        private String IMMS_CONSIGNEE_NAME;

        @JsonProperty("CONSIGNEE_DETAIL")
        private String CONSIGNEE_DETAIL;

        @JsonProperty("QTY")
        private String QTY;

        @JsonProperty("QTY_CANCELLED")
        private String QTY_CANCELLED;

        @JsonProperty("RATE")
        private String RATE;

        @JsonProperty("UOM_CD")
        private String UOM_CD;

        @JsonProperty("UOM")
        private String UOM;

        @JsonProperty("BASIC_VALUE")
        private String BASIC_VALUE;

        @JsonProperty("SALES_TAX_PER")
        private String SALES_TAX_PER;

        @JsonProperty("SALES_TAX")
        private String SALES_TAX;

        @JsonProperty("EXCISE_TYPE")
        private String EXCISE_TYPE;

        @JsonProperty("EXCISE_PER")
        private String EXCISE_PER;

        @JsonProperty("EXCISE")
        private String EXCISE;

        @JsonProperty("DISCOUNT_TYPE")
        private String DISCOUNT_TYPE;

        @JsonProperty("DISCOUNT_PER")
        private String DISCOUNT_PER;

        @JsonProperty("DISCOUNT")
        private String DISCOUNT;

        @JsonProperty("OT_CHARGE_TYPE")
        private String OT_CHARGE_TYPE;

        @JsonProperty("OT_CHARGE_PER")
        private String OT_CHARGE_PER;

        @JsonProperty("OTHER_CHARGES")
        private String OTHER_CHARGES;

        @JsonProperty("VALUE")
        private String VALUE;

        @JsonProperty("DELV_DT")
        private String DELV_DT;

        @JsonProperty("EXT_DELV_DT")
        private String EXT_DELV_DT;

        @JsonProperty("USER_ID")
        private String USER_ID;

        @JsonProperty("DATETIME")
        private String DATETIME;

        @JsonProperty("ALLOCATION")
        private String ALLOCATION;

        @JsonProperty("CONSIGNEE_RLY")
        private String CONSIGNEE_RLY;

        @JsonProperty("CONSIGNEE_RLY_SHORTNAME")
        private String CONSIGNEE_RLY_SHORTNAME;

        @JsonProperty("P_RLY")
        private String P_RLY;

        @JsonProperty("BILL_PAY_OFF")
        private String BILL_PAY_OFF;

        @JsonProperty("BILL_PAY_OFF_DESC")
        private String BILL_PAY_OFF_DESC;

        @JsonProperty("BILL_PASS_OFF")
        private String BILL_PASS_OFF;

}
