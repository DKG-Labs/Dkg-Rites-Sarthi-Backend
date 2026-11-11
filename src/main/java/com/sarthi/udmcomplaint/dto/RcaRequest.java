package com.sarthi.udmcomplaint.dto;

import lombok.Data;

/** Root Cause Analysis (optional evidence PDF is the multipart "file" part). */
@Data
public class RcaRequest {
    private String rootCause;
    private String remarks;
}
