package com.sarthi.udmcomplaint.dto;

import lombok.Data;

/** Complete Audit (Audit Report PDF is the multipart "file" part). */
@Data
public class CompleteAuditRequest {
    private String auditType;       // Audit of Record | Audit of Firm | Both
    private String auditDate;       // yyyy-MM-dd
    private String findings;
    private String attributableTo;
    private String remarks;
}
