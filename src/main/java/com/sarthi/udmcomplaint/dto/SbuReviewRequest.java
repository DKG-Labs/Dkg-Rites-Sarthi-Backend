package com.sarthi.udmcomplaint.dto;

import lombok.Data;

/** SBU Head review: FORWARD (to Railway Board) or RETURN (to CM for rework; remarks mandatory). */
@Data
public class SbuReviewRequest {
    private String decision;   // FORWARD | RETURN
    private String remarks;
}
