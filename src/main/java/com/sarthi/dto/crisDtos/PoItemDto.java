package com.sarthi.dto.crisDtos;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class PoItemDto {

        @JsonProperty("RLY")
        @JsonAlias({"rly", "RLY_CD", "rlyCd"})
        private String RLY;

        @JsonProperty("POKEY")
        @JsonAlias({"poKey", "PO_KEY", "CASE_NO", "caseNo"})
        private String POKEY;

        @JsonProperty("PO_NO")
        @JsonAlias({"poNo", "PONO", "PO_NUMBER", "PO_NUM", "po_no"})
        private String PO_NO;

        @JsonProperty("PL_NO")
        @JsonAlias({"plNo", "PLNO", "pl_no"})
        private String PL_NO;

        @JsonProperty("ITEM_SRNO")
        @JsonAlias({"PO_SR", "ITEM_SR_NO", "PO_SERIAL_NO", "SLNO", "itemSrNo", "item_sr_no", "itemSrno"})
        private String ITEM_SRNO;

        @JsonProperty("ITEM_DESC")
        @JsonAlias({"itemDesc", "item_desc", "ITEM_DESCRIPTION"})
        private String ITEM_DESC;

        @JsonProperty("CONSIGNEE_CD")
        @JsonAlias({"consigneeCd", "consignee_cd"})
        private String CONSIGNEE_CD;

        @JsonProperty("IMMS_CONSIGNEE_CD")
        @JsonAlias({"immsConsigneeCd", "imms_consignee_cd"})
        private String IMMS_CONSIGNEE_CD;

        @JsonProperty("IMMS_CONSIGNEE_NAME")
        @JsonAlias({"immsConsigneeName", "imms_consignee_name"})
        private String IMMS_CONSIGNEE_NAME;

        @JsonProperty("CONSIGNEE_DETAIL")
        @JsonAlias({"consigneeDetail", "consignee_detail"})
        private String CONSIGNEE_DETAIL;

        @JsonProperty("QTY")
        @JsonAlias({"qty", "PO_QTY", "poQty", "po_qty"})
        private String QTY;

        @JsonProperty("QTY_CANCELLED")
        @JsonAlias({"qtyCancelled", "qty_cancelled", "QTY_CANC"})
        private String QTY_CANCELLED;

        @JsonProperty("RATE")
        @JsonAlias({"rate", "PO_RATE", "poRate", "po_rate"})
        private String RATE;

        @JsonProperty("UOM_CD")
        @JsonAlias({"uomCd", "uom_cd", "UOM_CODE"})
        private String UOM_CD;

        @JsonProperty("UOM")
        @JsonAlias({"uom", "UOM_NAME"})
        private String UOM;

        @JsonProperty("BASIC_VALUE")
        @JsonAlias({"basicValue", "basic_value"})
        private String BASIC_VALUE;

        @JsonProperty("SALES_TAX_PER")
        @JsonAlias({"salesTaxPercent", "sales_tax_percent", "SALES_TAX_PERCENT", "salesTaxPer"})
        private String SALES_TAX_PER;

        @JsonProperty("SALES_TAX")
        @JsonAlias({"salesTax", "sales_tax"})
        private String SALES_TAX;

        @JsonProperty("EXCISE_TYPE")
        @JsonAlias({"exciseType"})
        private String EXCISE_TYPE;

        @JsonProperty("EXCISE_PER")
        @JsonAlias({"excisePer"})
        private String EXCISE_PER;

        @JsonProperty("EXCISE")
        @JsonAlias({"excise"})
        private String EXCISE;

        @JsonProperty("DISCOUNT_TYPE")
        @JsonAlias({"discountType", "discount_type"})
        private String DISCOUNT_TYPE;

        @JsonProperty("DISCOUNT_PER")
        @JsonAlias({"discountPercent", "discount_percent", "DISCOUNT_PERCENT", "discountPer"})
        private String DISCOUNT_PER;

        @JsonProperty("DISCOUNT")
        @JsonAlias({"discount"})
        private String DISCOUNT;

        @JsonProperty("OT_CHARGE_TYPE")
        @JsonAlias({"otChargeType", "ot_charge_type"})
        private String OT_CHARGE_TYPE;

        @JsonProperty("OT_CHARGE_PER")
        @JsonAlias({"otChargePercent", "ot_charge_percent", "OT_CHARGE_PERCENT", "otChargePer"})
        private String OT_CHARGE_PER;

        @JsonProperty("OTHER_CHARGES")
        @JsonAlias({"otherCharges", "other_charges"})
        private String OTHER_CHARGES;

        @JsonProperty("VALUE")
        @JsonAlias({"value", "TOTAL_VALUE", "total_value"})
        private String VALUE;

        @JsonProperty("DELV_DT")
        @JsonAlias({"deliveryDate", "delivery_date", "DELIVERY_DATE", "delvDt", "DELV_DATE"})
        private String DELV_DT;

        @JsonProperty("EXT_DELV_DT")
        @JsonAlias({"extendedDeliveryDate", "extended_delivery_date", "EXT_DELIVERY_DATE", "extDelvDt"})
        private String EXT_DELV_DT;

        @JsonProperty("USER_ID")
        @JsonAlias({"userId", "user_id"})
        private String USER_ID;

        @JsonProperty("DATETIME")
        @JsonAlias({"crisTimestamp", "cris_timestamp", "DATETIME", "datetime", "DATE_OF_TRN"})
        private String DATETIME;

        @JsonProperty("ALLOCATION")
        @JsonAlias({"allocation"})
        private String ALLOCATION;

        @JsonProperty("CONSIGNEE_RLY")
        @JsonAlias({"consigneeRly", "consignee_rly"})
        private String CONSIGNEE_RLY;

        @JsonProperty("CONSIGNEE_RLY_SHORTNAME")
        @JsonAlias({"consigneeRlyShortName", "consignee_rly_short_name", "CONSIGNEE_RLY_SHORT_NAME"})
        private String CONSIGNEE_RLY_SHORTNAME;

        @JsonProperty("P_RLY")
        @JsonAlias({"pRly", "p_rly", "PRLY"})
        private String P_RLY;

        @JsonProperty("BILL_PAY_OFF")
        @JsonAlias({"billPayOff", "bill_pay_off"})
        private String BILL_PAY_OFF;

        @JsonProperty("BILL_PAY_OFF_DESC")
        @JsonAlias({"billPayOffDesc", "bill_pay_off_desc", "BILL_PAY_OFF_NAME"})
        private String BILL_PAY_OFF_DESC;

        @JsonProperty("BILL_PASS_OFF")
        @JsonAlias({"billPassOff", "bill_pass_off"})
        private String BILL_PASS_OFF;

}
