package com.sarthi.service;

import com.sarthi.dto.crisDtos.MaRequestDto;
import com.sarthi.dto.crisDtos.PoCancellationRequestDto;
import com.sarthi.dto.crisDtos.PoRequestDto;
import org.springframework.stereotype.Service;

@Service
public interface crisService {

    public void savePoFromFrontend(PoRequestDto request);

    public void saveMaFromFrontend(MaRequestDto request);

    public void savePoCancellationFromFrontend(PoCancellationRequestDto request);

    public String getImmsToken();

    public Object fetchPoData(java.util.Map<String, String> requestValues);
}
