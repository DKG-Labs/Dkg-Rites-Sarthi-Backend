package com.sarthi.udmcomplaint.service;

import com.sarthi.udmcomplaint.dto.*;
import org.springframework.data.domain.Page;

import java.util.List;

public interface UdmComplaintWorkflowService {

    /** Complaint Cell: forward the complaint to a Controlling Manager of the chosen RIO. */
    UdmComplaintStatusDto sendToCm(Long udmRejectionId, SendToCmRequest request, Integer actionByUserId);

    /** Complaint Cell: close the complaint (remarks mandatory). */
    UdmComplaintStatusDto close(Long udmRejectionId, CloseComplaintRequest request, Integer actionByUserId);

    /** RIOs that have at least one Controlling Manager. */
    List<String> getRios();

    /** Controlling Managers of a RIO. */
    List<CmOptionDto> getCmsByRio(String rio);

    /** Complaints assigned to the logged-in CM (paginated). */
    Page<UdmComplaintStatusDto> getAssignedToCm(Integer cmUserId, int page, int size);

    /** Timeline of a complaint. */
    List<UdmComplaintHistoryDto> getHistory(Long udmRejectionId);
}
