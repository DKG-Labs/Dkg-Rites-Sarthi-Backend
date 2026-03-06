package com.sarthi.Sleeper.service.Impl;

import com.sarthi.Sleeper.dto.ProductionDeclaration.*;
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
    public PProductionDeclarationRequestDto create(
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

                    for (ProductionDeclarationResponseDto bDto :
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
    public PProductionDeclarationRequestDto update(
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

                    for (ProductionDeclarationResponseDto bDto :
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
    public PProductionDeclarationRequestDto getById(
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
    public List<PProductionDeclarationRequestDto> getAll() {

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

    private PProductionDeclarationRequestDto mapToResponse(
            ProductionDeclaration entity) {

        PProductionDeclarationRequestDto dto =
                new PProductionDeclarationRequestDto();

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

            List<ProductionStressChamberRequestDto> chamberDtos =
                    entity.getChambers()
                            .stream()
                            .map(chamber -> {

                                ProductionStressChamberRequestDto cDto =
                                        new ProductionStressChamberRequestDto();

                                cDto.setId(chamber.getId());
                                cDto.setChamberNo(
                                        chamber.getChamberNo());


                                // ===== BENCHES =====

                                if (chamber.getBenches() != null) {

                                    List<ProductionBenchGroupRequestDto> benchDtos =
                                            chamber.getBenches()
                                                    .stream()
                                                    .map(bench -> {

                                                        ProductionBenchGroupRequestDto bDto =
                                                                new ProductionBenchGroupRequestDto();

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

