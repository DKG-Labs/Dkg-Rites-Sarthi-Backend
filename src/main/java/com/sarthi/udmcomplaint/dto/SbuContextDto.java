package com.sarthi.udmcomplaint.dto;

import lombok.Data;

/** What the SBU Head review needs: can the user act now, round, and Railway Board remarks if returned. */
@Data
public class SbuContextDto {
    private String complaintStatus;
    private boolean canAct;
    private Integer currentRound;
    /** Railway Board remarks when the complaint came back from Railway Board; else null. */
    private String returnedRemarks;
}
