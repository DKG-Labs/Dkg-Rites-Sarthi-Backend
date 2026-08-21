package com.sarthi.dto.crisDtos;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class AmendedPoItemDTO {

        @JsonProperty("RLY")
        private String rly;

        @JsonProperty("POKEY")
        private String poKey;

        @JsonProperty("PO_NO")
        @JsonAlias({"PO_NO", "PONO", "PO_NUMBER", "PO_NUM"})
        private String poNo;

        @JsonProperty("PL_NO")
        private String plNo;

        @JsonProperty("ITEM_SRNO")
        @JsonAlias({"PO_SR", "ITEM_SR_NO", "PO_SERIAL_NO", "SLNO"})
        private String itemSrNo;

        @JsonProperty("ITEM_DESC")
        private String itemDesc;

        @JsonProperty("CONSIGNEE_CD")
        private String consigneeCd;

        @JsonProperty("IMMS_CONSIGNEE_CD")
        private String immsConsigneeCd;

        @JsonProperty("IMMS_CONSIGNEE_NAME")
        private String immsConsigneeName;

        @JsonProperty("CONSIGNEE_DETAIL")
        private String consigneeDetail;

        @JsonProperty("QTY")
        private String qty;

        @JsonProperty("QTY_CANCELLED")
        private String qtyCancelled;

        @JsonProperty("RATE")
        private String rate;

        @JsonProperty("UOM_CD")
        private String uomCd;

        @JsonProperty("UOM")
        private String uom;

        @JsonProperty("BASIC_VALUE")
        private String basicValue;

        @JsonProperty("SALES_TAX_PER")
        private String salesTaxPercent;

        @JsonProperty("SALES_TAX")
        private String salesTax;

        @JsonProperty("DISCOUNT_TYPE")
        private String discountType;

        @JsonProperty("DISCOUNT_PER")
        private String discountPercent;

        @JsonProperty("DISCOUNT")
        private String discount;

        @JsonProperty("VALUE")
        private String value;

        @JsonProperty("OT_CHARGE_TYPE")
        private String otChargeType;

        @JsonProperty("OT_CHARGE_PER")
        private String otChargePercent;

        @JsonProperty("OTHER_CHARGES")
        private String otherCharges;

        @JsonProperty("DELV_DT")
        private String deliveryDate;

        @JsonProperty("EXT_DELV_DT")
        private String extendedDeliveryDate;

        @JsonProperty("USER_ID")
        private String userId;

        @JsonProperty("DATETIME")
        private String crisTimestamp;

        @JsonProperty("ALLOCATION")
        private String allocation;

        @JsonProperty("CONSIGNEE_RLY")
        private String consigneeRly;

        @JsonProperty("CONSIGNEE_RLY_SHORTNAME")
        private String consigneeRlyShortName;

        @JsonProperty("P_RLY")
        private String pRly;

        @JsonProperty("BILL_PAY_OFF")
        private String billPayOff;

        @JsonProperty("BILL_PAY_OFF_DESC")
        private String billPayOffDesc;

        @JsonProperty("BILL_PASS_OFF")
        private String billPassOff;

}