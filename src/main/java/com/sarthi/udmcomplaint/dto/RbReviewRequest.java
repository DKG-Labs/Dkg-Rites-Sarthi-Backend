package com.sarthi.udmcomplaint.dto;

import lombok.Data;

/** Railway Board review: APPROVE (close) or RETURN (to SBU Head; remarks mandatory). */
@Data
public class RbReviewRequest {
    private String decision;   // APPROVE | RETURN
    private String remarks;
}
