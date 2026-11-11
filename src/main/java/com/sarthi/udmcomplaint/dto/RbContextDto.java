package com.sarthi.udmcomplaint.dto;

import lombok.Data;

/** What the Railway Board review needs: can the user act now and the current round. */
@Data
public class RbContextDto {
    private String complaintStatus;
    private boolean canAct;
    private Integer currentRound;
}
