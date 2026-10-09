package com.sarthi.dto.ercdiversion;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkflowActionDto {
    private Long requestId;
    private String action; // APPROVE, RETURN, RESUBMIT
    private String remarks;
    private String userId;
    private String userName;
    private String role; // CONTROLLING_MANAGER, SBU_HEAD, VENDOR
}
