package com.sarthi.dto.po;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * DTO for PO Data for Inspection Initiation Sections A, B, C
 * Fetches data from po_header, po_item, po_ma_header, po_ma_detail tables
 * Maps to actual database fields
 */
@Data
public class PoDataForSectionsDto {

    // Section A: Main PO Information (from po_header)
    private String rlyPoNo;           // RLY/PO_NO (combined field with / separator)
    private String rlyCd;             // Railway code
    private String rlyShortName;      // Railway short name
    private String poNo;              // PO Number
    private String poSerialNo;        // PO Serial Number (from inspection_calls table)
    private String poDate;            // PO Date (dd/MM/yyyy format)
    private Integer poQty;            // Total PO Quantity (from po_item)
    private Integer poSrQty;          // Specific PO Serial Qty (from po_item)

    private String inspPlace;         // Inspection Place (from inspection_calls table)
    private String placeOfInspection; // Formatted Place of Inspection (Company Name + Address)
    private String vendorName;        // Vendor Name (extracted from vendorDetails)
    private String vendorCode;        // Vendor Code
    private String vendorDetails;     // Full vendor details

    private String maNo;              // MA Number (from po_ma_header)
    private String maDate;            // MA Date (from po_ma_header)

    private String purchasingAuthority;  // Purchasing Authority (from purchaserDetail)
    private String billPayingOfficer;    // Bill Paying Officer (default or from config)

    private String poCondSrNo;        // PO Condition Serial Number (from po_ma_detail)
    private String condTitle;         // Condition Title (from po_ma_detail.maFldDescr)
    private String condText;          // Condition Text (from po_ma_detail.newValue)

    // Section B: Additional fields (from po_item)
    private String rlyPoNoSerial;     // RLY/PO_NO/PO_SR (combined field for Section B)
    private String itemDesc;          // Item Description
    private String consignee;         // Consignee Name
    private String consigneeDetail;   // Consignee Details
    private String unit;              // Unit of Measurement (UOM)
    private String deliveryDate;      // Delivery Date
    private String extendedDeliveryDate; // Extended Delivery Date
    private String plNo;              // PL Number

    // Section B: Additional fields from inspection_calls and rm_inspection_details
    private String ercType;           // Type of ERC (from inspection_calls.erc_type)
    private BigDecimal totalOfferedQtyMt; // Call Qty (from rm_inspection_details.total_offered_qty_mt)

    // Section C: Additional fields
    private String productType;       // Product Type (default: Raw Material)
    private String origDp;            // Original DP
    private String extDp;             // Extended DP
    private String origDpStart;       // Original DP Start Date

    // MA (Amendment) List - all amendments for this PO
    private List<MaInfoDto> maList;

    // Section C: RM Inspection Details (from rm_inspection_details, rm_heat_quantities)
    private List<RmHeatDetailsDto> rmHeatDetails;

    /**
     * Inner class for MA (Amendment) information
     */
    @Data
    public static class MaInfoDto {
        private String maNo;          // MA Number
        private String maDate;        // MA Date (dd/MM/yyyy)
        private String subject;       // MA Subject
        private BigDecimal oldPoValue; // Old PO Value
        private BigDecimal newPoValue; // New PO Value
        private String maType;        // MA Type
        private String status;        // MA Status
    }

    /**
     * Inner class for RM Heat Details (Section C)
     * Combines data from rm_inspection_details and rm_heat_quantities
     */
    @Data
    public static class RmHeatDetailsDto {
        // From rm_inspection_details
        private String rawMaterialName;    // item_description
        private String grade;              // grade/spec
        private String manufacturer;       // manufacturer
        private String subPoNumber;        // sub_po_number
        private String subPoDate;          // sub_po_date (dd/MM/yyyy)
        private Integer subPoQty;          // sub_po_qty
        private String invoiceNumber;      // invoice_number
        private String invoiceDate;        // invoice_date (dd/MM/yyyy)

        // From rm_heat_quantities
        private String heatNumber;         // heat_number
        private String tcNumber;           // tc_number
        private String tcDate;             // tc_date (dd/MM/yyyy)
        private BigDecimal offeredQty;     // offered_qty
        private String colorCode;          // color_code (manually entered by inspector)

        // From inventory_entries (fetched by heat_number + tc_number combination)
        private BigDecimal totalValueOfPo; // total_po from inventory_entries
        private BigDecimal tcQuantity;     // tc_quantity from inventory_entries
    }
}

