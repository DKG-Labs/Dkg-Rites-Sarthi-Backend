package com.sarthi.dto.crisDtos;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class AmendedPoItemDTO {

        @JsonProperty("RLY")
        @JsonAlias({"rly", "RLY_CD", "rlyCd"})
        private String rly;

        @JsonProperty("POKEY")
        @JsonAlias({"poKey", "PO_KEY", "CASE_NO", "caseNo"})
        private String poKey;

        @JsonProperty("PO_NO")
        @JsonAlias({"PO_NO", "PONO", "PO_NUMBER", "PO_NUM", "poNo"})
        private String poNo;

        @JsonProperty("PL_NO")
        @JsonAlias({"plNo", "PLNO", "pl_no"})
        private String plNo;

        @JsonProperty("ITEM_SRNO")
        @JsonAlias({"PO_SR", "ITEM_SR_NO", "PO_SERIAL_NO", "SLNO", "itemSrNo", "item_sr_no", "itemSrno"})
        private String itemSrNo;

        @JsonProperty("ITEM_DESC")
        @JsonAlias({"itemDesc", "item_desc", "ITEM_DESCRIPTION"})
        private String itemDesc;

        @JsonProperty("CONSIGNEE_CD")
        @JsonAlias({"consigneeCd", "consignee_cd"})
        private String consigneeCd;

        @JsonProperty("IMMS_CONSIGNEE_CD")
        @JsonAlias({"immsConsigneeCd", "imms_consignee_cd"})
        private String immsConsigneeCd;

        @JsonProperty("IMMS_CONSIGNEE_NAME")
        @JsonAlias({"immsConsigneeName", "imms_consignee_name"})
        private String immsConsigneeName;

        @JsonProperty("CONSIGNEE_DETAIL")
        @JsonAlias({"consigneeDetail", "consignee_detail"})
        private String consigneeDetail;

        @JsonProperty("QTY")
        @JsonAlias({"qty", "PO_QTY", "poQty", "po_qty"})
        private String qty;

        @JsonProperty("QTY_CANCELLED")
        @JsonAlias({"qtyCancelled", "qty_cancelled", "QTY_CANC"})
        private String qtyCancelled;

        @JsonProperty("RATE")
        @JsonAlias({"rate", "PO_RATE", "poRate", "po_rate"})
        private String rate;

        @JsonProperty("UOM_CD")
        @JsonAlias({"uomCd", "uom_cd", "UOM_CODE"})
        private String uomCd;

        @JsonProperty("UOM")
        @JsonAlias({"uom", "UOM_NAME"})
        private String uom;

        @JsonProperty("BASIC_VALUE")
        @JsonAlias({"basicValue", "basic_value"})
        private String basicValue;

        @JsonProperty("SALES_TAX_PER")
        @JsonAlias({"salesTaxPercent", "sales_tax_percent", "SALES_TAX_PERCENT", "salesTaxPer"})
        private String salesTaxPercent;

        @JsonProperty("SALES_TAX")
        @JsonAlias({"salesTax", "sales_tax"})
        private String salesTax;

        @JsonProperty("EXCISE_TYPE")
        @JsonAlias({"exciseType", "excise_type"})
        private String exciseType;

        @JsonProperty("EXCISE_PER")
        @JsonAlias({"excisePercent", "excise_percent", "EXCISE_PERCENT", "excisePer"})
        private String excisePercent;

        @JsonProperty("EXCISE")
        @JsonAlias({"excise"})
        private String excise;

        @JsonProperty("DISCOUNT_TYPE")
        @JsonAlias({"discountType", "discount_type"})
        private String discountType;

        @JsonProperty("DISCOUNT_PER")
        @JsonAlias({"discountPercent", "discount_percent", "DISCOUNT_PERCENT", "discountPer"})
        private String discountPercent;

        @JsonProperty("DISCOUNT")
        @JsonAlias({"discount"})
        private String discount;

        @JsonProperty("VALUE")
        @JsonAlias({"value", "TOTAL_VALUE", "total_value"})
        private String value;

        @JsonProperty("OT_CHARGE_TYPE")
        @JsonAlias({"otChargeType", "ot_charge_type"})
        private String otChargeType;

        @JsonProperty("OT_CHARGE_PER")
        @JsonAlias({"otChargePercent", "ot_charge_percent", "OT_CHARGE_PERCENT", "otChargePer"})
        private String otChargePercent;

        @JsonProperty("OTHER_CHARGES")
        @JsonAlias({"otherCharges", "other_charges"})
        private String otherCharges;

        @JsonProperty("DELV_DT")
        @JsonAlias({"deliveryDate", "delivery_date", "DELIVERY_DATE", "delvDt", "DELV_DATE"})
        private String deliveryDate;

        @JsonProperty("EXT_DELV_DT")
        @JsonAlias({"extendedDeliveryDate", "extended_delivery_date", "EXT_DELIVERY_DATE", "extDelvDt"})
        private String extendedDeliveryDate;

        @JsonProperty("USER_ID")
        @JsonAlias({"userId", "user_id"})
        private String userId;

        @JsonProperty("DATETIME")
        @JsonAlias({"crisTimestamp", "cris_timestamp", "DATETIME", "datetime", "DATE_OF_TRN"})
        private String crisTimestamp;

        @JsonProperty("ALLOCATION")
        @JsonAlias({"allocation"})
        private String allocation;

        @JsonProperty("CONSIGNEE_RLY")
        @JsonAlias({"consigneeRly", "consignee_rly"})
        private String consigneeRly;

        @JsonProperty("CONSIGNEE_RLY_SHORTNAME")
        @JsonAlias({"consigneeRlyShortName", "consignee_rly_short_name", "CONSIGNEE_RLY_SHORT_NAME"})
        private String consigneeRlyShortName;

        @JsonProperty("P_RLY")
        @JsonAlias({"pRly", "p_rly", "PRLY"})
        private String pRly;

        @JsonProperty("BILL_PAY_OFF")
        @JsonAlias({"billPayOff", "bill_pay_off"})
        private String billPayOff;

        @JsonProperty("BILL_PAY_OFF_DESC")
        @JsonAlias({"billPayOffDesc", "bill_pay_off_desc", "BILL_PAY_OFF_NAME"})
        private String billPayOffDesc;

        @JsonProperty("BILL_PASS_OFF")
        @JsonAlias({"billPassOff", "bill_pass_off"})
        private String billPassOff;

}