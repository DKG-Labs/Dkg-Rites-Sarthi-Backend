package com.sarthi.service;

import com.sarthi.dto.IBS.IbsAcknowledgementDto;
import com.sarthi.dto.IBS.IbsInspectionDto;
import com.sarthi.dto.ibsDtos.AuthRequestDto;
import com.sarthi.dto.ibsDtos.AuthResponseDto;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface IbsService {

    public AuthResponseDto integrationLogin(AuthRequestDto request);

    public List<IbsInspectionDto> getAllGeneratedIcCalls();
    public List<IbsInspectionDto> getCompletedIbsCalls();

    /** Completed-call rows for just these registrations (used for paging). */
    List<IbsInspectionDto> getCompletedIbsCallsFor(java.util.List<com.sarthi.entity.IBS.IbsCallRegistration> registrations);

    public String acknowledgeCallData(
            IbsAcknowledgementDto dto
    );

    public Object getIbsCaseNo(java.util.Map<String, Object> payload);

    public Object saveIbsCaseNo(java.util.Map<String, Object> payload);

    public String revertIbsCallToCompleted(String callNumber, Integer deletedBy);
}
