package com.sarthi.dto.certificate;

import lombok.Data;
import lombok.Builder;
import java.util.List;

/**
 * DTO for Raw Material Inspection Certificate (IC) Data.
 * Aggregates data from multiple tables to generate the IC certificate.
 */
@Data
@Builder
public class RawMaterialCertificateDto {

    /* ==================== Certificate Header ==================== */
    
    /**
     * Certificate Number Format: Region/Product/Railway/Year/Sequence
     * Example: SM/ER1/001, S-SAARTHI, M-Manufacturer, E-ERC, G-GRSP, S-Sleeper
     * R1-Raw Material 1, R2-Raw Material 2, Sr. No. xxx
     */
    private String certificateNo;
    
    /**
     * Certificate Date: Today's date (date of IC generation)
     */
    private String certificateDate;
    
    private String bookNo;
    private String setNo;
    
    /* ==================== Installment Information ==================== */
    
    /**
     * Offered Installment Number: No. of Inspection Calls requested by Vendor for that PO Number
     */
    private String offeredInstNo;
    
    /**
     * Passed Installment Number: No. of IC with Acceptance issued by IE for that PO Number
     */
    private String passedInstNo;
    
    /* ==================== Party Information ==================== */
    
    /**
     * Contractor: Vendor Name with address
     */
    private String contractor;
    
    /**
     * Manufacturer: Name of Manufacturer of Steel Rounds / Supplier of Raw Material 
     * along with city name in address (may be one or multiple depending upon heats)
     */
    private String manufacturer;
    
    /**
     * Place of Inspection: Stored in inspection_call table
     */
    private String placeOfInspection;
    
    /* ==================== Contract References ==================== */
    
    /**
     * Contract Ref. & Date (Rly.): Purchase Order No. & Date for which call has been marked
     * Number & date of all modification advise issued for that PO
     */
    private String contractRef;
    
    /**
     * Contractor's PO Number & Date: PO number & date for which call has been marked for inspection
     */
    private String contractorPo;
    
    /**
     * Bill Paying Officer: For now keep it blank
     */
    private String billPayingOfficer;
    
    /**
     * Consignee (Railway): From po_item table in consignee_detail column
     */
    private String consigneeRailway;
    
    /**
     * Consignee (Manufacturer of Finished Product): Name of Vendor with complete address
     */
    private String consigneeManufacturer;
    
    /**
     * Purchasing Authority (Railway/Non Railway): For now keep it blank
     */
    private String purchasingAuthority;
    
    /* ==================== Product Information ==================== */
    
    /**
     * Description: As per Product Type
     * MK-III - 55Si7 SPRING STEEL ROUND 20.64MM
     * MK-V - 55Si7 SPRING STEEL ROUND 23MM
     * J Type Clip - (description)
     * From inspection_call table in erc_type column
     */
    private String description;
    
    /**
     * The exact ERC type from inspection_calls table (e.g. MK-III, MK-V)
     */
    private String ercType;
    
    /**
     * Drawing Number: Leave it blank
     */
    private String drgNo;
    
    /**
     * Specification Number: IRS T-31-2025
     */
    private String specNo;
    
    /**
     * QAP Number: Clause No.4.11.2 & 4.11.3 of Indian Railway Standard Specification 
     * for Elastic Rail Clip, IRS T-31-2025
     */
    private String qapNo;
    
    /* ==================== Inspection Details ==================== */
    
    /**
     * Inspection Type: Visual/Physical/Chemical/Metallurgical/Dimensional
     */
    private String inspectionType;
    
    /**
     * CHP Clause: Clause No.4.11.2 & 4.11.3 of Indian Railway Standard Specification 
     * for Elastic Rail Clip, IRS T-31-2025
     */
    private String chpClause;
    
    /**
     * Contract CHP Requirement for Test: Visual, Dimensional, Mechanical & Chemical
     */
    private String contractChpReq;
    
    /**
     * Details of Inspection: Visual, Dimensional, Mechanical & Chemical
     */
    private String detailsOfInspection;
    
    /* ==================== Inspection Results ==================== */
    
    /**
     * Result: Based on status from rm_heat_final_result table
     * If Accepted: "CONFIRMING TO THE SPECIFICATION IRS T–31-2025, GRADE 55SI7..."
     * If Partially Accepted: "CONFIRMING TO THE SPECIFICATION IRS T–31-2025, GRADE 55SI7..."
     * If Rejected: "NOT CONFIRMING TO THE SPECIFICATION IRS T–31-2025, GRADE 55SI7..."
     * For now keep it blank
     */
    private String result;
    
    /**
     * Qty. for which stage is cleared: List of Heat No. and Accepted Quantity
     * Format: Heat No. / Qty (MT) + Total + No. of bundles + ERC calculation
     * From rm_heat_final_result table
     */
    private String qtyCleared;
    
    /**
     * Qty. rejected: Nil or Wt. of rejected Quantity along with Heat no.
     * From rm_heat_final_result table
     */
    private String qtyRejected;

    /**
     * Remarks: LOT FOUND ACCEPTABLE AND CLEARED FOR MANUFACTURING OF ERC MK-III
     * (Whatever the type of ERC in PO Sr. NO.)
     */
    private String remarks;

    /* ==================== Dates ==================== */

    /**
     * Date of call: Call Date (Date on which call has been raised) +
     * Desired Date (Date on which inspection has been desired)
     * From inspection_call Table
     */
    private String dateOfCall;

    /**
     * No. of visits: Sum of dates in dateOfInspection
     * For now leave it blank
     */
    private String noOfVisits;

    /**
     * Date of inspection: Dates on which inspection done for that Inspection call number
     */
    private String dateOfInspection;

    /* ==================== Sealing & Signature ==================== */

    /**
     * Pattern of sealing/stamping or identification:
     * "RITES HOLOGRAM FROM SL. NO." + Hologram Range +
     * "AFFIXED WITH TAPE ON LEAD SEAL OR ON TAG OF EACH BUNDLE."
     * Example: RITES HOLOGRAM FROM SL. NO. W-0809613 TO W-0809618 AFFIXED WITH TAPE...
     */
    private String sealingPattern;

    /**
     * Facsimile of seal/stamp: Blank for stamp
     */
    private String sealFacsimile;

    /**
     * Inspecting Engineer: DSC Signature of that particular inspection engineer
     * For now keep it blank
     */
    private String inspectingEngineer;

    /* ==================== Heat Details (for detailed breakdown) ==================== */

    /**
     * List of heat-wise details for the certificate
     */
    private List<HeatDetailDto> heatDetails;

    /**
     * Inner class for Heat-wise details
     */
    @Data
    @Builder
    public static class HeatDetailDto {
        private String heatNo;
        private String manufacturer;
        private String weightOfferedMt;
        private String weightAcceptedMt;
        private String weightRejectedMt;
        private String status;
        private String tcNo;
        private String tcDate;
    }
}

