package com.sarthi.SRailPad.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RailpadIcCertificateDto {
    private String certificateNo;
    private String bookNo; // blank
    private String setNo; // blank
    private String certificateDate; // today's date
    private String offeredInsttNo; // blank
    private String passedInsttNo; // blank
    private String contractorName; // Name of vendor with complete address
    private String placeOfInspection; // Name of vendor with complete address
    private String contractReferences; // PO No + Date from po_header
    private List<String> latest4Amendments; // from po_ma_header
    private String billPayingOfficer; // bill_pay_off_desc from po_item
    private String consignee; // consignee_detail from po_item
    private String purchasingAuthority; // purchaser_detail from po_header
    private String itemNo; // PO Sr No
    private String descriptionOfStores; // item_desc from po_item
    private Double quantityOnOrder; // qty from po_item
    private Double cumulativeQtyOfferedPreviously; // calc
    private Double qtyPrevPassed; // calc
    private Double qtyNowOffered; // calc
    private Double qtyNowPassed; // calc
    private Double qtyNowRejected; // calc
    private Double qtyStillDue; // calc
    private String uom; // from po_item
    private String quantityNowPassedInWords; // Editable pre-filled
    private String noOfItemsChecked; // "ONE"
    private String dateOfCall; // from rail_inspection_call
    private String noOfVisits; // days between INITIATE_CALL and FINISH
    private String dateOfInspection; // string of dates
    private String trRecDt; // blank
    private String reasonOfRejection; // editable
}
