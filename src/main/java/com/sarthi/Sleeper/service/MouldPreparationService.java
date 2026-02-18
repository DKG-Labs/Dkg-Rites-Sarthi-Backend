package com.sarthi.Sleeper.service;

import com.sarthi.Sleeper.dto.MouldPreparationRequestDTO;
import com.sarthi.Sleeper.dto.MouldPreparationResponseDTO;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface MouldPreparationService {

   public MouldPreparationResponseDTO create(
            MouldPreparationRequestDTO dto);

  public  MouldPreparationResponseDTO getById(Long id);

   public MouldPreparationResponseDTO update(
            Long id,
            MouldPreparationRequestDTO dto);

   public List<MouldPreparationResponseDTO> getAll();;

  public void delete(Long id);


}
