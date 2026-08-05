package com.sarthi.SRailPad.dto;

import lombok.Data;

/**
 * Railpad-specific DTO for the Inspection Initiation Summary screen.
 * This is completely isolated from the shared Sleeper portal
 * PoDataForSectionsDto.
 *
 * Data sources:
 * Section A -> po_header table
 * Section B -> po_item table (matched by item_sr_no)
 * Section C -> rail_inspection_call table
 */
@Data
public class RailPoSummaryDto {

    // ---- Section A: Main PO Information (po_header) ----

    /** RLY/PO_NO format: SER/60260074102063 */
    private String rlyPoNo;

    /** Railway short name e.g. SER */
    private String rlyShortName;

    /** Raw PO number e.g. 60260074102063 */
    private String poNo;

    /** PO Date in dd/MM/yyyy format */
    private String poDate;

    /** Total PO Quantity (sum of all po_item.qty) */
    private Integer poQty;

    /** Vendor name extracted from vendorDetails / firmDetails */
    private String vendorName;

    /** SCR Code / Railway short name e.g. SER, SCR */
    private String scrCode;

    /** Vendor code e.g. :1007406 */
    private String vendorCode;

    /** MA Number (from po_ma_header, N/A if none) */
    private String maNo;

    /** MA Date in dd/MM/yyyy format (N/A if none) */
    private String maDate;

    /** Purchasing authority e.g. APURBA SAHA~SMM/P/G */
    private String purchasingAuthority;

    /** Bill paying officer e.g. FA&CAO(S)/GRC */
    private String billPayingOfficer;

    // ---- Section B: Inspection Call Details (po_item + rail_inspection_call) ----

    /** Full serial: SER/60260074102063/001 */
    private String rlyPoNoSerial;

    /** Item description from po_item */
    private String itemDesc;

    /** PO serial number e.g. 001 */
    private String poSerialNo;

    /**
     * Quantity for this specific PO serial number (from po_item.qty where
     * item_sr_no matches)
     */
    private Integer poSrQty;

    /** UOM from po_item.uom e.g. Nos. */
    private String unit;

    /** Consignee name from po_item.imms_consignee_name */
    private String consignee;

    /** Original Delivery Period from po_item.delivery_date */
    private String origDp;

    /** Extended Delivery Period from po_item.extended_delivery_date */
    private String extDp;

    /** Place of inspection derived from firmDetails/vendorDetails */
    private String placeOfInspection;

    /** ERC type from rail_inspection_call.rail_pad_type */
    private String ercType;

    /** Total quantity offered in this call from rail_inspection_call.total_qty */
    private Integer totalOfferedQty;

    /**
     * Drawing number from rail_process_call_details.drawing_no (set during vendor
     * call raising)
     */
    private String drawingNo;
}
