package com.sarthi.Sleeper.service.Impl;

import com.sarthi.Sleeper.dto.PlantProfile.PlantProfileRequestDto;
import com.sarthi.Sleeper.dto.PlantProfile.PlantProfileResponseDto;
import com.sarthi.Sleeper.entity.PlantProfile;
import com.sarthi.Sleeper.repository.PlantProfileRepository;
import com.sarthi.Sleeper.service.PlantProfileService;
import com.sarthi.constant.AppConstant;
import com.sarthi.exception.BusinessException;
import com.sarthi.exception.ErrorDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class PlantProfileServiceImpl implements PlantProfileService {

    @Autowired
    private PlantProfileRepository repository;

        // CREATE
        @Override
        public PlantProfileResponseDto create(PlantProfileRequestDto dto) {

            PlantProfile entity = new PlantProfile();

            entity.setPlantNameLocation(dto.getPlantNameLocation());
            entity.setVendorCode(dto.getVendorCode());
            entity.setPlantType(dto.getPlantType());
            entity.setNumberOfSheds(dto.getNumberOfSheds());

            entity.setCreatedBy(dto.getCreatedBy());
            entity.setCreatedDate(LocalDateTime.now());

            repository.save(entity);

            return buildResponse(entity);
        }

        // UPDATE
        @Override
        public PlantProfileResponseDto update(Long id, PlantProfileRequestDto dto) {

            PlantProfile entity = repository.findById(id)
                    .orElseThrow(() -> new BusinessException(
                            new ErrorDetails(
                                    AppConstant.ERROR_CODE_RESOURCE,
                                    AppConstant.ERROR_TYPE_CODE_RESOURCE,
                                    AppConstant.ERROR_TYPE_VALIDATION,
                                    "Plant Profile not found")));


            entity.setPlantNameLocation(dto.getPlantNameLocation());
            entity.setVendorCode(dto.getVendorCode());
            entity.setPlantType(dto.getPlantType());
            entity.setNumberOfSheds(dto.getNumberOfSheds());

            entity.setUpdatedBy(dto.getUpdatedBy());
            entity.setUpdatedDate(LocalDateTime.now());

            repository.save(entity);

            return buildResponse(entity);
        }

        // GET BY ID
        @Override
        public PlantProfileResponseDto getById(Long id) {

            PlantProfile entity = repository.findById(id)
                    .orElseThrow(() -> new BusinessException(
                            new ErrorDetails(
                                    AppConstant.ERROR_CODE_RESOURCE,
                                    AppConstant.ERROR_TYPE_CODE_RESOURCE,
                                    AppConstant.ERROR_TYPE_VALIDATION,
                                    "Plant Profile not found")));


            return buildResponse(entity);
        }

        // GET ALL
        @Override
        public List<PlantProfileResponseDto> getAll() {

            List<PlantProfileResponseDto> list = new ArrayList<>();

            for (PlantProfile entity : repository.findAll()) {
                list.add(buildResponse(entity));
            }

            return list;
        }

        // DELETE
        @Override
        public void delete(Long id) {
            PlantProfile entity = repository.findById(id)
                    .orElseThrow(() -> new BusinessException(
                            new ErrorDetails(
                                    AppConstant.ERROR_CODE_RESOURCE,
                                    AppConstant.ERROR_TYPE_CODE_RESOURCE,
                                    AppConstant.ERROR_TYPE_VALIDATION,
                                    "Plant Profile not found")));

            repository.deleteById(entity.getId());
        }

        private PlantProfileResponseDto buildResponse(PlantProfile entity) {

            PlantProfileResponseDto dto = new PlantProfileResponseDto();

            dto.setId(entity.getId());
            dto.setPlantNameLocation(entity.getPlantNameLocation());
            dto.setVendorCode(entity.getVendorCode());
            dto.setPlantType(entity.getPlantType());
            dto.setNumberOfSheds(entity.getNumberOfSheds());
            dto.setCreatedBy(entity.getCreatedBy());
            dto.setCreatedDate(entity.getCreatedDate());
            dto.setUpdatedBy(entity.getUpdatedBy());
            dto.setUpdatedDate(entity.getUpdatedDate());

            return dto;
        }

}
