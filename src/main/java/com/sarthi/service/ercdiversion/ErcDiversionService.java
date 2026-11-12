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

    List<DiversionRequestDto> getApprovedRequestsForCm(String cmUserId);

    List<DiversionRequestDto> getPendingRequestsForSbu(String rioId);

    List<DiversionRequestDto> getApprovedRequestsForSbu(String rioId);

    DiversionRequestDto processWorkflowAction(WorkflowActionDto actionDto);

    List<BasketItemDto> getAvailableBasketItems(String vendorCode, DiversionStage stage, String targetPoNo, String targetPoSrNo);

    /** Vendor's Material Basket tab: all available basket rows of the vendor (every stage / target PO). */
    List<BasketItemDto> getVendorBasketItems(String vendorCode);

    CallSourceReferenceDto getCallSourceReference(Long callId);

    List<String> getCompleteDiversionSourceSrNos(String vendorCode);

    String getRailwayDocUrl(Long id);

    void deleteRequest(Long id);

    DiversionRequestDto updateRequest(DiversionRequestDto dto);
}
