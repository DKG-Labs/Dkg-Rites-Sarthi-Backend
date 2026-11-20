package com.sarthi.SRailPad.diversion.service;

import com.sarthi.SRailPad.diversion.dto.RailDiversionDtos.*;
import com.sarthi.SRailPad.diversion.entity.RailDiversionRequest;
import com.sarthi.SRailPad.diversion.entity.RailDiversionWorkflowHistory;
import com.sarthi.SRailPad.diversion.entity.RailDivertedPassedBasket;

import java.math.BigDecimal;
import java.util.List;

/** Rail Pad material diversion (Process / Final). Acting user always comes from the login. */
public interface RailDiversionService {

    // Vendor
    List<SourceIcDto> getEligibleSourceIcs(String plantId, String stage);
    List<SourceLineDto> getSourceLines(String sourceIcNo, String stage, Long excludeRequestId);
    RailDiversionRequest submit(RequestDto dto);
    RailDiversionRequest update(Long id, RequestDto dto);
    RailDiversionRequest resubmit(Long id);
    void delete(Long id);
    RailDiversionRequest getById(Long id);
    List<RailDiversionRequest> getVendorRequests();
    List<RailDivertedPassedBasket> getVendorBasket();
    List<String> getCompleteSourceSrNos();
    List<DivertedIcBalanceDto> getDivertedIcBalances(String poNo, String poSrNo, String stage);

    // CM / SBU
    List<RailDiversionRequest> getCmPending();
    List<RailDiversionRequest> getCmActioned();
    List<RailDiversionRequest> getSbuPending();
    List<RailDiversionRequest> getSbuActioned();
    RailDiversionRequest cmApprove(ActionDto action);
    RailDiversionRequest cmSendToSbu(ActionDto action);
    RailDiversionRequest cmReturn(ActionDto action);
    RailDiversionRequest sbuApprove(ActionDto action);
    RailDiversionRequest sbuReturn(ActionDto action);
    List<RailDiversionWorkflowHistory> getHistory(Long requestId);

    // Quantity tracking for DIVERTED calls (called from Rail Pad call / workflow services)
    void reserveForCall(String callNo, String divertedCallNo, BigDecimal qty);
    void consumeForCall(String callNo, BigDecimal acceptedQty);
    void releaseForCall(String callNo);
}
