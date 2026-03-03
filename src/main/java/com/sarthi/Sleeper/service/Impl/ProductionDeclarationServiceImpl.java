package com.sarthi.Sleeper.service.Impl;

import com.sarthi.Sleeper.dto.ProductionDeclaration.*;
import com.sarthi.Sleeper.entity.ProductionDeclaration.ProductionBench;
import com.sarthi.Sleeper.entity.ProductionDeclaration.ProductionChamber;
import com.sarthi.Sleeper.entity.ProductionDeclaration.ProductionDeclaration;
import com.sarthi.Sleeper.repository.ProductionDeclaration.ProductionDeclarationRepository;
import com.sarthi.Sleeper.service.ProductionDeclarationService;
import com.sarthi.constant.AppConstant;
import com.sarthi.exception.BusinessException;
import com.sarthi.exception.ErrorDetails;
import com.sarthi.util.CommonUtils;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class ProductionDeclarationServiceImpl implements ProductionDeclarationService {

    private final ProductionDeclarationRepository repository;

    public ProductionDeclarationServiceImpl(ProductionDeclarationRepository repository) {
        this.repository = repository;
    }


    // ================= CREATE =================

    @Override
    public ProductionDeclarationResponseDto create(
            ProductionDeclarationRequestDto dto) {

        ProductionDeclaration entity =
                new ProductionDeclaration();

        entity.setPlantType(dto.getPlantType());
        entity.setProductionUnit(dto.getProductionUnit());

        if (dto.getCastingDate() != null) {
            entity.setCastingDate(
                    CommonUtils.convertStringToDateObject(
                            dto.getCastingDate()));
        }

        entity.setShift(dto.getShift());
        entity.setBatchNumber(dto.getBatchNumber());
        entity.setMixDesignReference(dto.getMixDesignReference());

        if (dto.getLbcTime() != null) {
            entity.setLbcTime(
                    CommonUtils.convertStringToTimeObject(
                            dto.getLbcTime()));
        }

        entity.setRemarks(dto.getRemarks());

        entity.setCreatedBy(dto.getCreatedBy());
        entity.setCreatedDate(LocalDateTime.now());


        // ===== Chambers =====

        if (dto.getChambers() != null) {

            for (ProductionChamberRequestDto cDto :
                    dto.getChambers()) {

                ProductionChamber chamber =
                        new ProductionChamber();

                chamber.setChamberNo(cDto.getChamberNo());
                chamber.setDeclaration(entity);

                entity.getChambers().add(chamber);


                if (cDto.getBenches() != null) {

                    for (ProductionBenchRequestDto bDto :
                            cDto.getBenches()) {

                        ProductionBench bench =
                                new ProductionBench();

                        bench.setBenchNo(
                                bDto.getBenchNumbers());
                        bench.setCount(bDto.getCount());
                        bench.setSleeperType(
                                bDto.getSleeperType());
                        bench.setMouldPerBench(
                                bDto.getMouldPerBench());
                        bench.setRftMeters(
                                bDto.getRftMeters());

                        bench.setChamber(chamber);

                        chamber.getBenches().add(bench);
                    }
                }
            }
        }


        // ===== SUMMARY CALCULATION =====

        calculateSummary(entity);


        ProductionDeclaration saved =
                repository.save(entity);

        return mapToResponse(saved);
    }


    // ================= UPDATE =================

    @Override
    public ProductionDeclarationResponseDto update(
            Long id,
            ProductionDeclarationRequestDto dto) {

        ProductionDeclaration entity =
                repository.findById(id)
                        .orElseThrow(() -> new BusinessException(
                                new ErrorDetails(
                                        AppConstant.ERROR_CODE_RESOURCE,
                                        AppConstant.ERROR_TYPE_CODE_RESOURCE,
                                        AppConstant.ERROR_TYPE_VALIDATION,
                                        "Record not found")));


        entity.setPlantType(dto.getPlantType());
        entity.setProductionUnit(dto.getProductionUnit());

        if (dto.getCastingDate() != null) {
            entity.setCastingDate(
                    CommonUtils.convertStringToDateObject(
                            dto.getCastingDate()));
        }

        entity.setShift(dto.getShift());
        entity.setBatchNumber(dto.getBatchNumber());
        entity.setMixDesignReference(dto.getMixDesignReference());

        if (dto.getLbcTime() != null) {
            entity.setLbcTime(
                    CommonUtils.convertStringToTimeObject(
                            dto.getLbcTime()));
        }

        entity.setRemarks(dto.getRemarks());

        entity.setUpdatedBy(dto.getUpdatedBy());
        entity.setUpdatedDate(LocalDateTime.now());


        entity.getChambers().clear();


        if (dto.getChambers() != null) {

            for (ProductionChamberRequestDto cDto :
                    dto.getChambers()) {

                ProductionChamber chamber =
                        new ProductionChamber();

                chamber.setChamberNo(cDto.getChamberNo());
                chamber.setDeclaration(entity);

                entity.getChambers().add(chamber);


                if (cDto.getBenches() != null) {

                    for (ProductionBenchRequestDto bDto :
                            cDto.getBenches()) {

                        ProductionBench bench =
                                new ProductionBench();

                        bench.setBenchNo(
                                bDto.getBenchNumbers());
                        bench.setCount(bDto.getCount());
                        bench.setSleeperType(
                                bDto.getSleeperType());
                        bench.setMouldPerBench(
                                bDto.getMouldPerBench());
                        bench.setRftMeters(
                                bDto.getRftMeters());

                        bench.setChamber(chamber);

                        chamber.getBenches().add(bench);
                    }
                }
            }
        }


        calculateSummary(entity);


        ProductionDeclaration updated =
                repository.save(entity);

        return mapToResponse(updated);
    }


    // ================= GET =================

    @Override
    public ProductionDeclarationResponseDto getById(
            Long id) {

        ProductionDeclaration entity =
                repository.findById(id)
                        .orElseThrow(() -> new BusinessException(
                                new ErrorDetails(
                                        AppConstant.ERROR_CODE_RESOURCE,
                                        AppConstant.ERROR_TYPE_CODE_RESOURCE,
                                        AppConstant.ERROR_TYPE_VALIDATION,
                                        "Record not found")));

        return mapToResponse(entity);
    }


    @Override
    public List<ProductionDeclarationResponseDto> getAll() {

        return repository.findAll()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }


    @Override
    public void delete(Long id) {

        ProductionDeclaration entity =
                repository.findById(id)
                        .orElseThrow(() -> new BusinessException(
                                new ErrorDetails(
                                        AppConstant.ERROR_CODE_RESOURCE,
                                        AppConstant.ERROR_TYPE_CODE_RESOURCE,
                                        AppConstant.ERROR_TYPE_VALIDATION,
                                        "Record not found")));
        repository.deleteById(entity.getId());
    }


    // ================= SUMMARY =================

    private void calculateSummary(
            ProductionDeclaration entity) {

        int totalSleepers = 0;
        double totalRft = 0;
        Set<String> sleeperTypes = new HashSet<>();

        for (ProductionChamber chamber :
                entity.getChambers()) {

            for (ProductionBench bench :
                    chamber.getBenches()) {

                if (bench.getMouldPerBench() != null)
                    totalSleepers += bench.getMouldPerBench();

                if (bench.getRftMeters() != null)
                    totalRft += bench.getRftMeters();

                if (bench.getSleeperType() != null)
                    sleeperTypes.add(
                            bench.getSleeperType());
            }
        }

        entity.setTotalCastedSleepers(totalSleepers);
        entity.setTotalRftCasted(totalRft);
        entity.setTotalSleeperTypes(sleeperTypes.size());
    }


    // ================= RESPONSE MAPPER =================

    private ProductionDeclarationResponseDto mapToResponse(
            ProductionDeclaration entity) {

        ProductionDeclarationResponseDto dto =
                new ProductionDeclarationResponseDto();

        dto.setId(entity.getId());
        dto.setPlantType(entity.getPlantType());
        dto.setProductionUnit(entity.getProductionUnit());

        if (entity.getCastingDate() != null) {
            dto.setCastingDate(
                    CommonUtils.convertDateToString(
                            entity.getCastingDate()));
        }

        dto.setShift(entity.getShift());
        dto.setBatchNumber(entity.getBatchNumber());
        dto.setMixDesignReference(
                entity.getMixDesignReference());

        if (entity.getLbcTime() != null) {
            dto.setLbcTime(entity.getLbcTime().toString());
        }

        // ===== SUMMARY =====

        dto.setTotalCastedSleepers(
                entity.getTotalCastedSleepers());

        dto.setTotalRftCasted(
                entity.getTotalRftCasted());

        dto.setTotalSleeperTypes(
                entity.getTotalSleeperTypes());

        dto.setRemarks(entity.getRemarks());


        // ===== CHAMBERS =====

        if (entity.getChambers() != null) {

            List<ProductionChamberResponseDto> chamberDtos =
                    entity.getChambers()
                            .stream()
                            .map(chamber -> {

                                ProductionChamberResponseDto cDto =
                                        new ProductionChamberResponseDto();

                                cDto.setId(chamber.getId());
                                cDto.setChamberNo(
                                        chamber.getChamberNo());


                                // ===== BENCHES =====

                                if (chamber.getBenches() != null) {

                                    List<ProductionBenchResponseDto> benchDtos =
                                            chamber.getBenches()
                                                    .stream()
                                                    .map(bench -> {

                                                        ProductionBenchResponseDto bDto =
                                                                new ProductionBenchResponseDto();

                                                        bDto.setId(
                                                                bench.getId());

                                                        bDto.setBenchNumbers(
                                                                bench.getBenchNo());

                                                        bDto.setCount(
                                                                bench.getCount());

                                                        bDto.setSleeperType(
                                                                bench.getSleeperType());

                                                        bDto.setMouldPerBench(
                                                                bench.getMouldPerBench());

                                                        bDto.setRftMeters(
                                                                bench.getRftMeters());

                                                        return bDto;

                                                    })
                                                    .toList();

                                    cDto.setBenches(benchDtos);
                                }

                                return cDto;

                            })
                            .toList();

            dto.setChambers(chamberDtos);
        }

        return dto;
    }

    }

