package com.sarthi.dto.processmaterial;

import lombok.Data;
import java.util.List;

/**
 * DTO for Process Material Inspection Initiation Data
 * Contains Section A (PO Information) and Section B (Inspection Call Details)
 * Similar to Raw Material inspection initiation
 */
@Data
public class ProcessInitiationDataDto {

    // ==================== SECTION A: Main PO Information ====================
    private String poNo;
    private String poDate;
    private String poDescription;
    private Integer poQty;
    private String poUnit;
    private String amendmentNo;
    private String amendmentDate;
    private String vendorName;
    private String vendorCode;
    private String consignee;
    private String deliveryDate;
    private String purchasingAuthority;
    private String billPayingOfficer;
    private String rlyCd;
    private String rlyShortName;
    private String poSerialNo;
    private String rlyPoNo;
    private String rlyPoNoSerial;
    private Integer poSrQty;
    
    // ==================== SECTION B: Inspection Call Details ====================
    private String callNo;
    private String callDate;
    private String desiredInspectionDate;
    private String typeOfCall; // "Process"
    private String typeOfErc; // Type of ERC from inspection_calls.erc_type
    private String placeOfInspection;
    private String companyName;
    private String unitName;
    private String unitAddress;
    private String rmIcNumber; // RM IC number from process_inspection_details (primary lot)
    private String lotNumber; // Lot number from process_inspection_details (primary lot)
    private String heatNumber; // Heat number from process_inspection_details (primary lot)
    private Integer offeredQty; // Offered quantity from process_inspection_details (CALL QTY for Section B - primary lot)

    // ==================== MULTIPLE LOTS SUPPORT ====================
    // List of all lots linked to this inspection call
    private List<LotDetailsInfo> lotDetailsList;

    // ==================== SECTION C: RM IC Details (Heat Numbers from Inventory) ====================
    // List of heat number details from inventory_entry table
    private List<RmIcHeatInfo> rmIcHeatInfoList;

    @Data
    public static class LotDetailsInfo {
        private String rmIcNumber;          // RM IC number from process_inspection_details
        private String lotNumber;           // Lot number from process_inspection_details
        private String heatNumber;          // Heat number from process_inspection_details
        private String manufacturer;        // Manufacturer from process_inspection_details
        private String manufacturerHeat;    // Manufacturer heat from process_inspection_details
        private Integer offeredQty;         // Offered quantity from process_inspection_details
        private Integer totalAcceptedQtyRm; // Total accepted quantity from process_inspection_details
    }

    @Data
    public static class RmIcHeatInfo {
        private String rmIcNumber;          // RM IC number from process_rm_ic_mapping
        private String heatNumber;          // Heat number from inventory_entry
        private String manufacturer;        // Supplier name from inventory_entry
        private String rawMaterialName;     // Raw material from inventory_entry
        private String gradeSpec;           // Grade specification from inventory_entry
        private String tcNumber;            // TC number from inventory_entry
        private String tcDate;              // TC date from inventory_entry
        private String subPoNumber;         // Sub PO number from inventory_entry
        private String subPoDate;           // Sub PO date from inventory_entry
        private String invoiceNumber;       // Invoice number from inventory_entry
        private String invoiceDate;         // Invoice date from inventory_entry
        private String subPoQty;            // Sub PO quantity from inventory_entry
        private String tcQuantity;          // TC quantity from inventory_entry
        private String unit;                // Unit of measurement from inventory_entry
        private Integer qtyAccepted;        // Qty accepted from process_rm_ic_mapping
    }
}

