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

    public String acknowledgeCallData(
            IbsAcknowledgementDto dto
    );

    public Object getIbsCaseNo(java.util.Map<String, Object> payload);

    public Object saveIbsCaseNo(java.util.Map<String, Object> payload);
}
