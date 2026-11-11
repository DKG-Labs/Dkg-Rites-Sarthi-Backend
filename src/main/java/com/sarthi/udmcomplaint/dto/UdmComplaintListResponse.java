package com.sarthi.udmcomplaint.dto;

import com.sarthi.dto.crisDtos.UdmRejectionDTO;
import lombok.Data;

import java.util.List;

/** One page of the role-scoped UDM complaint list + tab badge counts. */
@Data
public class UdmComplaintListResponse {
    private List<UdmRejectionDTO> content;
    private int page;            // 0-based
    private int size;
    private long totalElements;  // rows for the current scope + tab + search
    private int totalPages;

    /** Pending / Closed tab counts within the current scope (search applied). */
    private long pendingCount;
    private long closedCount;
    /** "Assigned to Me" (CM) or "Pending with Me" (SBU / Railway Board) badge. */
    private long mineCount;
    /** "All Rejections" badge (search applied). */
    private long allCount;
}
