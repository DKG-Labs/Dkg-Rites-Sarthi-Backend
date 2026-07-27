package com.sarthi.dto;

import lombok.Data;

/**
 * DTO returned by the Call Letter Details API.
 * Aggregates data from inspection_calls, po_header, po_item and
 * the type-specific details tables (RM / Process / Final).
 */
@Data
public class CallLetterDetailsDto {

    // ---- Call identification ----
    private String requestId; // IC Number e.g. "ER-03280001"
    private String typeOfCall; // "Raw Material" | "Process" | "Final"
    private String productType; // Same as typeOfCall (for PDF label)

    // ---- PO Header fields ----
    private String rlyShortName; // e.g. "WR"
    private String poNo; // Raw PO number e.g. "26255265205057"
    private String rlyPoSr; // Composite e.g. "WR / 26255265205057 / 012"
    private String poDate; // Formatted PO date
    private String purchaserDetail; // Purchaser (from po_header)

    // ---- PO Item fields ----
    private String itemSrNo; // Serial number e.g. "012"
    private String itemDesc; // Item description from po_item
    private Integer poQty; // PO quantity
    private String uom; // Unit of measure e.g. "Nos."
    private String consigneeDetail; // Consignee (from po_item)
    private String billPayOffDesc; // Bill Paying Authority (from po_item)
    private String deliveryDate; // Original DP Date (formatted)
    private String extendedDeliveryDate;// Extended DP Date (formatted)

    // ---- Call-type specific fields ----
    private String callQty; // Offered / call quantity as string with unit
    private String callUnit; // Unit for call qty

    // ---- Manufacturer & Place ----
    private String manufacturerName; // Company / unit name
    private String placeOfInspection; // Full place address
    private String vendorName; // Vendor name

    // ---- Inspection dates ----
    private String desiredInspectionDate;

    // ---- Installment ----
    private String offeredInstallmentNo; // IC number used as installment reference

    // ---- RIO Information ----
    private String rio; // RIO assigned to this call (e.g., ERIO, WRIO)

    // ---- Enriched PDF fields ----
    private String rawMaterialQtyPassed; // Raw Material Qty Already Passed for this PO Sr. No.
    private String finalAcceptedQty; // Final Accepted Qty of this PO Sr. No.
    private Integer poQuantity; // Total PO Quantity
    private String poValue; // Total PO Value

    // ---- Contact Details ----
    private String contactPersonName;
    private String contactMobile;
    private String contactEmail;

    // ---- IE Details ----
    private String ieName;
    private String ieMobile;

    // ---- Heat Details List ----
    private java.util.List<HeatDetail> heatDetails;

    @Data
    public static class HeatDetail {
        private String heatNo;
        private String tcNo;
        private String qtyOffered;
    }
}
