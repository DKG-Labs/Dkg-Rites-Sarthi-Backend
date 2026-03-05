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
     * Consignee (Railway): From po_item table in consignee_detail column
     */
    private String consigneeRailway;
    
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
     * Total Offered Quantity: Total quantity offered for inspection
     */
    private Integer totalOfferedQty;
    
    /**
     * Total Accepted Quantity: Total quantity accepted
     */
    private Integer totalAcceptedQty;
    
    /**
     * Total Rejected Quantity: Total quantity rejected
     */
    private Integer totalRejectedQty;
    
    /**
     * Remarks: Certificate remarks
     */
    private String remarks;

    /**
     * TR Rec. Dt.: Technical Record Receipt Date (for now display blank)
     */
    private String trRecDate;

    /**
     * No. of visits: Derived from WorkflowTransitions
     */
    private String noOfVisits;

    /**
     * Quantity now passed in words and details
     */
    private String quantityNowPassedText;
    
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

