package com.sarthi.service;

import com.sarthi.dto.ibsDtos.AuthRequestDto;
import com.sarthi.dto.ibsDtos.AuthResponseDto;
import org.springframework.stereotype.Service;

@Service
public interface ibsService {

    public AuthResponseDto integrationLogin(
            AuthRequestDto request);
}
