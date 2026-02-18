package com.sarthi.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for Vendor Inspection Call with Workflow Status.
 * Used to display inspection calls in vendor dashboard with current workflow status.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VendorInspectionCallStatusDto {

    /* ==================== Inspection Call Information ==================== */
    
    private String icNumber;           // Inspection Call Number
    private String poNo;               // Purchase Order Number
    private String poSerialNo;         // PO Serial Number
    private String typeOfCall;         // Stage: Raw Material, Process, Final
    private String desiredInspectionDate;  // Call Date
    private String placeOfInspection;  // Location
    
    /* ==================== Item Information ==================== */
    
    private String itemName;           // Item description/name
    private Integer quantityOffered;   // Quantity offered for inspection
    
    /* ==================== Workflow Status ==================== */
    
    private String workflowStatus;     // Latest workflow status from workflow_transition
    private String currentRoleName;    // Current role in workflow
    private String nextRoleName;       // Next role in workflow
    private String jobStatus;          // Job status from workflow
    
    /* ==================== Additional Information ==================== */
    
    private String companyName;
    private String unitName;
    private String createdAt;
    private String updatedAt;
}

