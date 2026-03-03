package com.sarthi.Sleeper.service;

import com.sarthi.Sleeper.dto.Cement.CementReceiptRequestDto;
import com.sarthi.Sleeper.dto.Cement.CementReceiptResponseDto;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface CementService {


    CementReceiptResponseDto create(CementReceiptRequestDto dto);

   public CementReceiptResponseDto update(Long id, CementReceiptRequestDto dto);

  public  CementReceiptResponseDto getById(Long id);

   public List<CementReceiptResponseDto> getAll();

   public void delete(Long id);

}
