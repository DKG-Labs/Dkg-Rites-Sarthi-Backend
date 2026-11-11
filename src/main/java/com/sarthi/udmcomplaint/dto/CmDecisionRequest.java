package com.sarthi.udmcomplaint.dto;

import lombok.Data;

import java.util.List;

/** CM Decision: path JI (with schedule) or AUDIT, plus the recommendation flags the CM confirmed. */
@Data
public class CmDecisionRequest {
    private String path;            // JI | AUDIT
    private String jiDate;          // yyyy-MM-dd, required for JI
    /** JI team: user ids from /cm/ji-team-options (IEs + CMs of the RIO); at least one for JI. */
    private List<Integer> jiTeamUserIds;
    /** Optional other members not in SARTHI (e.g. consignee / vendor representatives). */
    private String jiTeamOthers;
    private Boolean flagFailed90;
    private Boolean flagQtyOver5;
    private Boolean flagCritical;
    private String remarks;
}
