package com.sarthi.service.ercdiversion;

import com.sarthi.dto.ercdiversion.*;
import com.sarthi.enums.DiversionRequestStatus;
import com.sarthi.enums.DiversionStage;

import java.util.List;

public interface ErcDiversionService {

    List<SourceIcEligibilityDto> getEligibleSourceIcs(String vendorCode, String plantId, DiversionStage stage);

    List<SourceIcLineItemDto> getSourceIcLineItems(String icNo, DiversionStage stage);

    DiversionRequestDto saveDraft(DiversionRequestDto dto);

    DiversionRequestDto submitRequest(DiversionRequestDto dto);

    DiversionRequestDto getRequestById(Long id);

    DiversionRequestDto getRequestByRequestNo(String requestNo);

    List<DiversionRequestDto> getVendorRequests(String vendorCode, DiversionRequestStatus status);

    List<DiversionRequestDto> getPendingRequestsForCm(String cmUserId);

    List<DiversionRequestDto> getPendingRequestsForSbu(String rioId);

    DiversionRequestDto processWorkflowAction(WorkflowActionDto actionDto);

    List<BasketItemDto> getAvailableBasketItems(String vendorCode, DiversionStage stage, String targetPoNo, String targetPoSrNo);

    CallSourceReferenceDto getCallSourceReference(Long callId);
}
