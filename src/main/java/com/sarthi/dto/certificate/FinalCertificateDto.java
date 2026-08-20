package com.sarthi.dto.certificate;

import lombok.Data;
import lombok.Builder;
import java.util.List;

/**
 * DTO for Final Material Inspection Certificate Data.
 * Aggregates data from multiple tables to generate the Final IC certificate.
 */
@Data
@Builder
public class FinalCertificateDto {

    /* ==================== Certificate Header ==================== */
    
    /**
     * Certificate Number: Same format as RM IC Number
     * Example: N/RM-IC-1767618858167/RAJK
     */
    private String certificateNo;
    
    /**
     * Certificate Date: Today's date (date of certificate generation)
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
     * Place of Inspection: Name of vendor with complete address
     */
    private String placeOfInspection;
    
    /* ==================== Contract References ==================== */
    
    /**
     * Contract Ref. & Date (Rly.): Purchase Order No. & Date for which call has been marked
     * Number & date of all modification advise issued for that PO
     */
    private String contractRef;
    
    /**
     * Contract Ref Date: Date of the Purchase Order
     */
    private String contractRefDate;
    
    /**
     * Bill Paying Officer: From PO Details fetched through API from IREPS (for now display blank)
     */
    private String billPayingOfficer;
    
    /**
     * Consignee: From po_item table in consignee_detail column
     */
    private String consignee;
    private String consigneeRailway;
    private String consigneeManufacturer;
    
    private String maNumberAndDate;
    
    /**
     * Purchasing Authority: Same as RM IC (for now display blank)
     */
    private String purchasingAuthority;
    
    /* ==================== Product Information ==================== */
    
    /**
     * Item No: Continuous Serial No. (for now display blank)
     */
    private String itemNo;
    
    /**
     * Description: Product description from inspection call
     */
    private String description;
    
    /* ==================== Inspection Results ==================== */
    
    /**
     * Total Lots: Total number of lots inspected
     */
    private Integer totalLots;
    
    /**
     * Quantity on Order: Total quantity from PO for this item serial number
     */
    private Integer qtyOnOrder;

    /**
     * Quantity Offered Previously: Sum of totalOfferedQty from previous EF- calls
     */
    private Integer qtyOfferedPreviously;

    /**
     * Quantity Passed Previously: Sum of totalAcceptedQty from previous EF- calls
     */
    private Integer qtyPassedPreviously;

    /**
     * Quantity Now Offered: Current call's offered quantity
     */
    private Integer qtyNowOffered;

    /**
     * Quantity Now Passed: Current call's accepted quantity
     */
    private Integer qtyNowPassed;

    /**
     * Quantity Now Rejected: Current call's rejected quantity
     */
    private Integer qtyNowRejected;

    /**
     * Quantity Still Due: qtyOnOrder - cumulative passed quantity
     */
    private Integer qtyStillDue;

    /**
     * Remarks: Certificate remarks
     */
    private String remarks;

    /**
     * TR Rec. Dt.: Technical Record Receipt Date (for now display blank)
     */
    private String trRecDate;

    /**
     * Total ERC used for testing across all lots
     */
    private Integer ercUsedForTesting;

    /**
     * No. of items checked: Fixed value of 1 as per requirement
     */
    private String noOfItemsChecked;

    /**
     * Date of call: Derived from InspectionCall (format: X, Desired Date: Y)
     */
    private String dateOfCall;

    /**
     * No. of visits: Derived from WorkflowTransitions
     */
    private String noOfVisits;

    /**
     * Date(s) of inspection: From WorkflowTransitions
     */
    private String inspectionDates;
    private String datesOfInspection;

    /**
     * Quantity now passed in words and details
     */
    private String quantityNowPassedText;

    /**
     * Sealing Pattern: Dynamic text generated from hologram details
     */
    private String sealingPattern;
    private String facsimileText;
    private String reasonsForRejection;
    private String inspectingEngineer;
    private String rmIcNo;
    private String rmIcDate;
    private String processIcNo;
    private String processIcDate;
    private String rejectedReason;
    
    /* ==================== Lot Details ==================== */
    
    /**
     * List of lot-wise details for the certificate
     */
    private List<LotDetailDto> lotDetails;

    /**
     * Inner class for Lot-wise details
     */
    @Data
    @Builder
    public static class LotDetailDto {
        private String lotNo;
        private String heatNo;
        private String manufacturer;
        private Integer offeredQty;
        private Integer acceptedQty;
        private Integer rejectedQty;
        private String status;
    }
}

