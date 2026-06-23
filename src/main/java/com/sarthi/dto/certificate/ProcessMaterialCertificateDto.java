package com.sarthi.dto.certificate;

import lombok.Data;
import lombok.Builder;
import java.util.List;

/**
 * DTO for Process Material Inspection Certificate (IC) Data.
 * Aggregates data from multiple tables to generate the Process IC certificate.
 */
@Data
@Builder
public class ProcessMaterialCertificateDto {

    /* ==================== Certificate Header ==================== */
    
    /**
     * Certificate Number Format: Region/Product/Railway/Year/Sequence
     * Example: SM/EP1/001 (E-ERC, P1-Process 1)
     */
    private String certificateNo;
    
    /**
     * Certificate Date: Today's date (date of IC generation)
     */
    private String certificateDate;
    
    private String bookNo;
    private String setNo;
    private String placeOfInspection;
    
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
     * Manufacturer: Name of Manufacturer with address
     */
    private String manufacturer;
    
    /* ==================== Contract References ==================== */
    
    /**
     * Contract Ref. & Date (Rly.): Purchase Order No. & Date for which call has been marked
     * Number & date of all modification advise issued for that PO
     */
    private String contractRef;
    
    /**
     * PO Details: PO number & date for which call has been marked for inspection
     */
    private String poDetails;
    
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
    
    private String maNumberAndDate;
    
    private String facsimileText;
    private String reasonsForRejection;
    private String inspectingEngineer;
    private String datesOfInspection;
    
    /* ==================== Product Information ==================== */
    
    /**
     * Description: As per Product Type
     * Example: PROCESS INSPECTION OF ELASTIC RAIL CLIP MK-V
     */
    private String description;
    
    /**
     * Drawing Number: Leave it blank or from PO details
     */
    private String drgNo;
    
    /**
     * Specification Number: IRS T-31-2025
     */
    private String specNo;
    
    /**
     * QAP Number: Clause No. of QAP
     */
    private String qapNo;
    
    /**
     * CHP Clause Number of QAP
     */
    private String chpClause;
    
    /* ==================== Inspection Details ==================== */
    
    /**
     * Inspection Type: Type of inspection/tests conducted
     * Example: Checking Length of cut bars/ Turning length/ MPI Test/  Checking of Die/ Quenching temperature & duration/ Quenching hardness/ Tempering temperature & duration/ Dimensional check/ Hardness of finished ERC/ Documentaion
     */
    private String inspectionType;
    
    /**
     * Type of ERC: comes from `InspectionCall.ercType` field
     */
    private String ercType;
    /* ==================== Lot Details ==================== */
    
    /**
     * List of lot-wise details for the certificate
     */
    private List<LotDetailDto> lots;
    
    /* ==================== Reference & Dates ==================== */
    
    /**
     * Reference: Reference information
     */
    private String reference;
    
    /**
     * Date of call: Call Date + Desired Date
     */
    private String dateOfCall;
    
    /**
     * Inspection Date: Dates on which inspection done for that Inspection call number
     */
    private String inspectionDate;
    
    /**
     * Man Days: Total No. of Man-days engaged
     */
    private String manDays;
    
    /**
     * No. of visits: Derived from WorkflowTransitions
     */
    private String noOfVisits;
    
    /* ==================== Sealing & Signature ==================== */
    
    /**
     * Pattern of sealing/stamping or identification
     */
    private String sealingPattern;

    /**
     * Inner class for Lot-wise details
     */
    @Data
    @Builder
    public static class LotDetailDto {
        /**
         * Heat Number / Lot Number
         */
        private String heatNo;
        
        /**
         * Total Processed Quantity (in numbers)
         */
        private Integer totalProcessed;
        
        /**
         * Accepted Quantity (in numbers)
         */
        private Integer acceptedQty;
        
        /**
         * Rejected Quantity (in numbers)
         */
        private Integer rejectedQty;
    }
}

