package com.sarthi.Sleeper.service.Impl;

import com.sarthi.Sleeper.dto.BatchWithIdProjection;
import com.sarthi.Sleeper.dto.BenchDetailsResponseDto;
import com.sarthi.Sleeper.dto.ProductionDeclaration.*;
import com.sarthi.Sleeper.entity.FinalInspection.WaterCubeStrengthTest;
import com.sarthi.Sleeper.entity.PlantProfile;
import com.sarthi.Sleeper.entity.ProductionDeclaration.*;
import com.sarthi.Sleeper.entity.SleeperWorkflowTransaction;
import com.sarthi.Sleeper.repository.FinalInspectionRepository.WaterCubeStrengthTestRepository;
import com.sarthi.Sleeper.repository.ProductionDeclaration.ProductionBenchGroupRepository;
import com.sarthi.Sleeper.repository.ProductionDeclaration.ProductionDeclarationRepository;
import com.sarthi.Sleeper.repository.ProductionDeclaration.ProductionSleeperRepository;
import com.sarthi.Sleeper.repository.SleeperWorkflowRepository;
import com.sarthi.Sleeper.service.ProductionDeclarationService;
import com.sarthi.constant.AppConstant;
import com.sarthi.exception.BusinessException;
import com.sarthi.exception.ErrorDetails;
import com.sarthi.util.CommonUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ProductionDeclarationServiceImpl implements ProductionDeclarationService {

    @Autowired
    private ProductionDeclarationRepository repository;
    @Autowired
    private SleeperWorkflowRepository sleeperWorkflowRepository;
    @Autowired
    private ProductionBenchGroupRepository productionBenchGroupRepository;
    @Autowired
    private ProductionSleeperRepository productionSleeperRepository;
    @Autowired
    private WaterCubeStrengthTestRepository waterCubeStrengthTestRepository;

    @Override
    @Transactional
    public ProductionDeclarationResponseDto create(
            ProductionDeclarationRequestDto dto) {

        ProductionDeclaration entity = new ProductionDeclaration();

        entity.setPlantType(dto.getPlantType());
        entity.setProductionUnit(dto.getProductionUnit());
        LocalDate cDate = CommonUtils.convertStringToDateObject(dto.getCastingDate());

        entity.setCastingDate(cDate);
        entity.setShift(dto.getShift());
        entity.setBatchNumber(dto.getBatchNumber());
        entity.setMixDesignReference(dto.getMixDesignReference());
        LocalTime pTime = CommonUtils.convertStringToTimeObject(dto.getLbcTime());

        entity.setPlantId(dto.getPlantId());
        entity.setVendorCode(dto.getVendorCode());

        entity.setLbcTime(pTime);

        entity.setTotalCastedSleepers(dto.getTotalCastedSleepers());
        entity.setTotalSleeperTypes(dto.getTotalSleeperTypes());
        entity.setTotalRft(dto.getTotalRft());

        entity.setRemarks(dto.getRemarks());

        entity.setCreatedBy(dto.getCreatedBy());
        entity.setCreatedDate(LocalDateTime.now());

        List<ProductionStressChamber> chamberList = new ArrayList<>();

        if (dto.getChambers() != null) {

            for (ProductionStressChamberRequestDto chamberDto : dto.getChambers()) {

                ProductionStressChamber chamber = new ProductionStressChamber();

                chamber.setChamberNo(chamberDto.getChamberNo());
                chamber.setDeclaration(entity);

                List<ProductionBenchGroup> benchList = new ArrayList<>();

                for (ProductionBenchGroupRequestDto benchDto : chamberDto.getBenchGroups()) {

                    ProductionBenchGroup bench = new ProductionBenchGroup();

                    bench.setBenchNo(benchDto.getBenchNo());
                    bench.setSleeperType(benchDto.getSleeperType());
                    bench.setMouldPerBench(benchDto.getMouldPerBench());
                    bench.setRft(benchDto.getRft());
                    bench.setSleeperCategory(benchDto.getSleeperCategory());
                    bench.setTotalSleepers(benchDto.getTotalSleepers());

                    bench.setChamber(chamber);

                    List<ProductionSleeper> sleepers = new ArrayList<>();

                    for (String sleeperNo : benchDto.getSleepers()) {

                        ProductionSleeper sleeper = new ProductionSleeper();

                        sleeper.setSleeperNo(sleeperNo);
                        sleeper.setBenchGroup(bench);
                        sleeper.setSleeperType(benchDto.getSleeperType());

                        sleepers.add(sleeper);
                    }

                    bench.setSleepers(sleepers);

                    benchList.add(bench);
                }

                chamber.setBenchGroups(benchList);

                chamberList.add(chamber);
            }
        }

        entity.setChambers(chamberList);

        List<ProductionLongLineGang> gangList = new ArrayList<>();

        if (dto.getGangs() != null) {

           /* for (ProductionLongLineGangRequestDto gangDto : dto.getGangs()) {

                ProductionLongLineGang gang = new ProductionLongLineGang();

                gang.setMode(gangDto.getMode());
                gang.setGangFrom(gangDto.getGangFrom());
                gang.setGangTo(gangDto.getGangTo());
                gang.setGangNo(gangDto.getGangNo());
                gang.setSleeperType(gangDto.getSleeperType());
                gang.setMouldsPerGang(gangDto.getMouldsPerGang());

                gang.setDeclaration(entity);

                gangList.add(gang);
            } */
            for (ProductionLongLineGangRequestDto gangDto : dto.getGangs()) {

                ProductionLongLineGang gang = new ProductionLongLineGang();

                gang.setMode(gangDto.getMode());
                gang.setGangFrom(gangDto.getGangFrom());
                gang.setGangTo(gangDto.getGangTo());
                gang.setGangNo(gangDto.getGangNo());
                gang.setSleeperType(gangDto.getSleeperType());
                gang.setMouldsPerGang(gangDto.getMouldsPerGang());
                gang.setSleeperCategory(gangDto.getSleeperCategory());
                gang.setTotalSleepers(gangDto.getTotalSleepers());
                gang.setRft(gangDto.getRft());

                gang.setDeclaration(entity);

                // ADD THIS (same like bench logic)
                List<ProductionSleeper> sleepers = new ArrayList<>();

                if (gangDto.getSleepers() != null) {

                    for (String sleeperNo : gangDto.getSleepers()) {

                        ProductionSleeper sleeper = new ProductionSleeper();

                        sleeper.setSleeperNo(sleeperNo);

                        sleeper.setGang(gang);
                        sleeper.setSleeperType(gangDto.getSleeperType());

                        sleepers.add(sleeper);
                    }
                }

                gang.setSleepers(sleepers);

                gangList.add(gang);
            }
        }

        entity.setGangs(gangList);

        repository.save(entity);

        return getById(entity.getId());
    }
/*
    @Override
    public ProductionDeclarationResponseDto update(
            Long id,
            ProductionDeclarationRequestDto dto) {

        ProductionDeclaration entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Record not found"));

        // ===== HEADER =====

        entity.setPlantType(dto.getPlantType());
        entity.setProductionUnit(dto.getProductionUnit());

        LocalDate cDate = CommonUtils.convertStringToDateObject(dto.getCastingDate());
        entity.setCastingDate(cDate);

        entity.setPlantType(dto.getPlantType());
        entity.setVendorCode(dto.getVendorCode());

        entity.setShift(dto.getShift());
        entity.setBatchNumber(dto.getBatchNumber());
        entity.setMixDesignReference(dto.getMixDesignReference());

        LocalTime pTime = CommonUtils.convertStringToTimeObject(dto.getLbcTime());
        entity.setLbcTime(pTime);

        entity.setTotalCastedSleepers(dto.getTotalCastedSleepers());
        entity.setTotalSleeperTypes(dto.getTotalSleeperTypes());
        entity.setTotalRft(dto.getTotalRft());

        entity.setRemarks(dto.getRemarks());
        entity.setUpdatedBy(dto.getUpdatedBy());
        entity.setUpdatedDate(LocalDateTime.now());

        // ===== STRESS CHAMBERS =====

        List<ProductionStressChamber> chamberList = new ArrayList<>();

        if (dto.getChambers() != null) {

            for (ProductionStressChamberRequestDto chamberDto : dto.getChambers()) {

                ProductionStressChamber chamber = new ProductionStressChamber();

                chamber.setChamberNo(chamberDto.getChamberNo());
                chamber.setDeclaration(entity);

                List<ProductionBenchGroup> benchList = new ArrayList<>();

                if (chamberDto.getBenchGroups() != null) {

                    for (ProductionBenchGroupRequestDto benchDto : chamberDto.getBenchGroups()) {

                        ProductionBenchGroup bench = new ProductionBenchGroup();

                        bench.setBenchNo(benchDto.getBenchNo());
                        bench.setSleeperType(benchDto.getSleeperType());
                        bench.setMouldPerBench(benchDto.getMouldPerBench());
                        bench.setRft(benchDto.getRft());

                        bench.setChamber(chamber);

                        List<ProductionSleeper> sleepers = new ArrayList<>();

                        if (benchDto.getSleepers() != null) {

                            for (String sleeperNo : benchDto.getSleepers()) {

                                ProductionSleeper sleeper = new ProductionSleeper();

                                sleeper.setSleeperNo(sleeperNo);
                                sleeper.setBenchGroup(bench);

                                sleepers.add(sleeper);
                            }
                        }

                        bench.setSleepers(sleepers);
                        benchList.add(bench);
                    }
                }

                chamber.setBenchGroups(benchList);
                chamberList.add(chamber);
            }
        }

        entity.setChambers(chamberList);

        // ===== LONG LINE =====

        List<ProductionLongLineGang> gangList = new ArrayList<>();

        if (dto.getGangs() != null) {

            for (ProductionLongLineGangRequestDto gangDto : dto.getGangs()) {

                ProductionLongLineGang gang = new ProductionLongLineGang();

                gang.setMode(gangDto.getMode());
                gang.setGangFrom(gangDto.getGangFrom());
                gang.setGangTo(gangDto.getGangTo());
                gang.setGangNo(gangDto.getGangNo());
                gang.setSleeperType(gangDto.getSleeperType());
                gang.setMouldsPerGang(gangDto.getMouldsPerGang());

                gang.setDeclaration(entity);

                gangList.add(gang);
            }
        }

        entity.setGangs(gangList);

        repository.save(entity);

        return getById(entity.getId());
    }
*/

    /*
@Override
public ProductionDeclarationResponseDto update(Long id, ProductionDeclarationRequestDto dto) {

    ProductionDeclaration entity = repository.findById(id)
            .orElseThrow(() -> new RuntimeException("Record not found"));

    // ===== HEADER =====
    entity.setPlantType(dto.getPlantType());
    entity.setProductionUnit(dto.getProductionUnit());
    entity.setCastingDate(CommonUtils.convertStringToDateObject(dto.getCastingDate()));
    entity.setVendorCode(dto.getVendorCode());
    entity.setPlantId(dto.getPlantId());
    entity.setShift(dto.getShift());
    entity.setBatchNumber(dto.getBatchNumber());
    entity.setMixDesignReference(dto.getMixDesignReference());
    entity.setLbcTime(CommonUtils.convertStringToTimeObject(dto.getLbcTime()));
    entity.setTotalCastedSleepers(dto.getTotalCastedSleepers());
    entity.setTotalSleeperTypes(dto.getTotalSleeperTypes());
    entity.setTotalRft(dto.getTotalRft());
    entity.setRemarks(dto.getRemarks());
    entity.setUpdatedBy(dto.getUpdatedBy());
    entity.setUpdatedDate(LocalDateTime.now());


    entity.getChambers().clear();
    entity.getGangs().clear();

    // ===== REBUILD CHAMBERS =====
    if (dto.getChambers() != null) {

        for (ProductionStressChamberRequestDto chamberDto : dto.getChambers()) {

            ProductionStressChamber chamber = new ProductionStressChamber();
            chamber.setChamberNo(chamberDto.getChamberNo());
            chamber.setDeclaration(entity);

            List<ProductionBenchGroup> benchList = new ArrayList<>();

            if (chamberDto.getBenchGroups() != null) {

                for (ProductionBenchGroupRequestDto benchDto : chamberDto.getBenchGroups()) {

                    ProductionBenchGroup bench = new ProductionBenchGroup();
                    bench.setBenchNo(benchDto.getBenchNo());
                    bench.setSleeperType(benchDto.getSleeperType());
                    bench.setMouldPerBench(benchDto.getMouldPerBench());
                    bench.setRft(benchDto.getRft());
                    bench.setChamber(chamber);

                    List<ProductionSleeper> sleepers = new ArrayList<>();

                    if (benchDto.getSleepers() != null) {
                        for (String sleeperNo : benchDto.getSleepers()) {

                            ProductionSleeper sleeper = new ProductionSleeper();
                            sleeper.setSleeperNo(sleeperNo);
                            sleeper.setBenchGroup(bench);

                            sleepers.add(sleeper);
                        }
                    }

                    bench.setSleepers(sleepers);
                    benchList.add(bench);
                }
            }

            chamber.setBenchGroups(benchList);
            entity.getChambers().add(chamber);
        }
    }

    // ===== REBUILD GANGS =====
    if (dto.getGangs() != null) {

        for (ProductionLongLineGangRequestDto gangDto : dto.getGangs()) {

            ProductionLongLineGang gang = new ProductionLongLineGang();

            gang.setMode(gangDto.getMode());
            gang.setGangFrom(gangDto.getGangFrom());
            gang.setGangTo(gangDto.getGangTo());
            gang.setGangNo(gangDto.getGangNo());
            gang.setSleeperType(gangDto.getSleeperType());
            gang.setMouldsPerGang(gangDto.getMouldsPerGang());
            gang.setDeclaration(entity);

            List<ProductionSleeper> sleepers = new ArrayList<>();

            if (gangDto.getSleepers() != null) {
                for (String sleeperNo : gangDto.getSleepers()) {

                    ProductionSleeper sleeper = new ProductionSleeper();
                    sleeper.setSleeperNo(sleeperNo);
                    sleeper.setGang(gang);

                    sleepers.add(sleeper);
                }
            }

            gang.setSleepers(sleepers);
            entity.getGangs().add(gang);
        }
    }

    repository.save(entity);

    return getById(entity.getId());
}*/

    @Override
    @Transactional
    public ProductionDeclarationResponseDto update(Long id, ProductionDeclarationRequestDto dto) {

        ProductionDeclaration entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Record not found"));

        // ===== HEADER =====
        entity.setPlantType(dto.getPlantType());
        entity.setProductionUnit(dto.getProductionUnit());
        entity.setCastingDate(CommonUtils.convertStringToDateObject(dto.getCastingDate()));
        entity.setVendorCode(dto.getVendorCode());
        entity.setPlantId(dto.getPlantId());
        entity.setShift(dto.getShift());
        entity.setBatchNumber(dto.getBatchNumber());
        entity.setMixDesignReference(dto.getMixDesignReference());
        entity.setLbcTime(CommonUtils.convertStringToTimeObject(dto.getLbcTime()));
        entity.setTotalCastedSleepers(dto.getTotalCastedSleepers());
        entity.setTotalSleeperTypes(dto.getTotalSleeperTypes());
        entity.setTotalRft(dto.getTotalRft());
        entity.setRemarks(dto.getRemarks());
        entity.setUpdatedBy(dto.getUpdatedBy());
        entity.setUpdatedDate(LocalDateTime.now());

        // =========================================================
        // 🔥 CHAMBER MERGE
        // =========================================================

        Map<Long, ProductionStressChamber> chamberMap =
                entity.getChambers().stream()
                        .collect(Collectors.toMap(ProductionStressChamber::getId, c -> c));

        List<ProductionStressChamber> finalChambers = new ArrayList<>();

        for (ProductionStressChamberRequestDto chamberDto : dto.getChambers()) {

            ProductionStressChamber chamber;

            if (chamberDto.getId() != null && chamberMap.containsKey(chamberDto.getId())) {
                chamber = chamberMap.get(chamberDto.getId());
                chamberMap.remove(chamberDto.getId());
            } else {
                chamber = new ProductionStressChamber();
                chamber.setDeclaration(entity);
            }

            chamber.setChamberNo(chamberDto.getChamberNo());

            // ================= BENCH =================

            Map<Long, ProductionBenchGroup> benchMap =
                    chamber.getBenchGroups() == null ? new HashMap<>() :
                            chamber.getBenchGroups().stream()
                                    .collect(Collectors.toMap(ProductionBenchGroup::getId, b -> b));

            List<ProductionBenchGroup> finalBenches = new ArrayList<>();

            for (ProductionBenchGroupRequestDto benchDto : chamberDto.getBenchGroups()) {

                ProductionBenchGroup bench;

                if (benchDto.getId() != null && benchMap.containsKey(benchDto.getId())) {
                    bench = benchMap.get(benchDto.getId());
                    benchMap.remove(benchDto.getId());
                } else {
                    bench = new ProductionBenchGroup();
                    bench.setChamber(chamber);
                }

                bench.setBenchNo(benchDto.getBenchNo());
                bench.setSleeperType(benchDto.getSleeperType());
                bench.setMouldPerBench(benchDto.getMouldPerBench());
                bench.setRft(benchDto.getRft());
                bench.setSleeperCategory(benchDto.getSleeperCategory());
                bench.setTotalSleepers(benchDto.getTotalSleepers());

                // ================= SLEEPERS =================

                Map<Long, ProductionSleeper> sleeperMap =
                        bench.getSleepers() == null ? new HashMap<>() :
                                bench.getSleepers().stream()
                                        .collect(Collectors.toMap(ProductionSleeper::getId, s -> s));

                List<ProductionSleeper> finalSleepers = new ArrayList<>();

                if (benchDto.getSleeperList() != null) {
                    for (ProductionSleeperResponseDto sDto : benchDto.getSleeperList()) {

                        ProductionSleeper sleeper;

                        if (sDto.getId() != null && sleeperMap.containsKey(sDto.getId())) {
                            sleeper = sleeperMap.get(sDto.getId());
                            sleeperMap.remove(sDto.getId());
                        } else {
                            sleeper = new ProductionSleeper();
                            sleeper.setBenchGroup(bench);
                        }

                        sleeper.setSleeperNo(sDto.getSleeperNo());
                        sleeper.setSleeperType(benchDto.getSleeperType());

                        finalSleepers.add(sleeper);
                    }
                }

                // remove deleted sleepers
                sleeperMap.values().forEach(s -> bench.getSleepers().remove(s));

                // 🔥 IMPORTANT (do NOT replace list)
                if (bench.getSleepers() == null) bench.setSleepers(new ArrayList<>());
                bench.getSleepers().clear();
                bench.getSleepers().addAll(finalSleepers);

                finalBenches.add(bench);
            }

            // remove deleted benches
            benchMap.values().forEach(b -> chamber.getBenchGroups().remove(b));

            if (chamber.getBenchGroups() == null) chamber.setBenchGroups(new ArrayList<>());
            chamber.getBenchGroups().clear();
            chamber.getBenchGroups().addAll(finalBenches);

            finalChambers.add(chamber);
        }

        // remove deleted chambers
        chamberMap.values().forEach(c -> entity.getChambers().remove(c));

        entity.getChambers().clear();
        entity.getChambers().addAll(finalChambers);

        // =========================================================
        // 🔥 GANG MERGE
        // =========================================================

        Map<Long, ProductionLongLineGang> gangMap =
                entity.getGangs().stream()
                        .collect(Collectors.toMap(ProductionLongLineGang::getId, g -> g));

        List<ProductionLongLineGang> finalGangs = new ArrayList<>();

        for (ProductionLongLineGangRequestDto gangDto : dto.getGangs()) {

            ProductionLongLineGang gang;

            if (gangDto.getId() != null && gangMap.containsKey(gangDto.getId())) {
                gang = gangMap.get(gangDto.getId());
                gangMap.remove(gangDto.getId());
            } else {
                gang = new ProductionLongLineGang();
                gang.setDeclaration(entity);
            }

            gang.setMode(gangDto.getMode());
            gang.setGangFrom(gangDto.getGangFrom());
            gang.setGangTo(gangDto.getGangTo());
            gang.setGangNo(gangDto.getGangNo());
            gang.setSleeperType(gangDto.getSleeperType());
            gang.setMouldsPerGang(gangDto.getMouldsPerGang());
            gang.setSleeperCategory(gangDto.getSleeperCategory());
            gang.setTotalSleepers(gangDto.getTotalSleepers());
            gang.setRft(gangDto.getRft());

            // ================= SLEEPERS =================

            Map<Long, ProductionSleeper> sleeperMap =
                    gang.getSleepers() == null ? new HashMap<>() :
                            gang.getSleepers().stream()
                                    .collect(Collectors.toMap(ProductionSleeper::getId, s -> s));

            List<ProductionSleeper> finalSleepers = new ArrayList<>();

            if (gangDto.getSleeperList() != null) {
                for (ProductionSleeperResponseDto sDto : gangDto.getSleeperList()) {

                    ProductionSleeper sleeper;

                    if (sDto.getId() != null && sleeperMap.containsKey(sDto.getId())) {
                        sleeper = sleeperMap.get(sDto.getId());
                        sleeperMap.remove(sDto.getId());
                    } else {
                        sleeper = new ProductionSleeper();
                        sleeper.setGang(gang);
                    }

                    sleeper.setSleeperNo(sDto.getSleeperNo());
                    sleeper.setSleeperType(gangDto.getSleeperType());

                    finalSleepers.add(sleeper);
                }
            }

            sleeperMap.values().forEach(s -> gang.getSleepers().remove(s));

            if (gang.getSleepers() == null) gang.setSleepers(new ArrayList<>());
            gang.getSleepers().clear();
            gang.getSleepers().addAll(finalSleepers);

            finalGangs.add(gang);
        }

        gangMap.values().forEach(g -> entity.getGangs().remove(g));

        entity.getGangs().clear();
        entity.getGangs().addAll(finalGangs);

        repository.save(entity);

        return getById(entity.getId());
    }
    @Override
    public ProductionDeclarationResponseDto getById(Long id) {

        ProductionDeclaration entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Record not found"));

        ProductionDeclarationResponseDto response = new ProductionDeclarationResponseDto();

        response.setId(entity.getId());
        response.setPlantType(entity.getPlantType());
        response.setProductionUnit(entity.getProductionUnit());
        response.setCastingDate(CommonUtils.convertDateToString(entity.getCastingDate()));
        response.setShift(entity.getShift());
        response.setBatchNumber(entity.getBatchNumber());
        response.setMixDesignReference(entity.getMixDesignReference());
        response.setLbcTime(entity.getLbcTime());

        response.setVendorCode(entity.getVendorCode());
        response.setPlantId(entity.getPlantId());

        response.setTotalCastedSleepers(entity.getTotalCastedSleepers());
        response.setTotalSleeperTypes(entity.getTotalSleeperTypes());
        response.setTotalRft(entity.getTotalRft());

        response.setRemarks(entity.getRemarks());

        response.setCreatedBy(entity.getCreatedBy());
        response.setCreatedDate(entity.getCreatedDate());
        response.setUpdatedBy(entity.getUpdatedBy());
        response.setUpdatedDate(entity.getUpdatedDate());

        String status = sleeperWorkflowRepository
                .findLatestStatusByRequestIdAndModuleId(String.valueOf(entity.getId()), 11L)
                .orElse("NOT_STARTED");
        if (status != null) {
            response.setStatus(status);
        }

        // ================= STRESS BENCH =================

        if (entity.getChambers() != null) {

            List<ProductionStressChamberResponseDto> chamberList = new ArrayList<>();

            for (ProductionStressChamber chamber : entity.getChambers()) {

                ProductionStressChamberResponseDto chamberDto = new ProductionStressChamberResponseDto();

                chamberDto.setId(chamber.getId());
                chamberDto.setChamberNo(chamber.getChamberNo());

                List<ProductionBenchGroupResponseDto> benchList = new ArrayList<>();

                if (chamber.getBenchGroups() != null) {

                    for (ProductionBenchGroup bench : chamber.getBenchGroups()) {

                        ProductionBenchGroupResponseDto benchDto = new ProductionBenchGroupResponseDto();

                        benchDto.setId(bench.getId());
                        benchDto.setBenchNo(bench.getBenchNo());
                        benchDto.setSleeperType(bench.getSleeperType());
                        benchDto.setMouldPerBench(bench.getMouldPerBench());
                        benchDto.setRft(bench.getRft());
                        benchDto.setSleeperCategory(bench.getSleeperCategory());
                        benchDto.setTotalSleepers(bench.getTotalSleepers());

                       /* List<String> sleeperNumbers = new ArrayList<>();

                        if (bench.getSleepers() != null) {

                            for (ProductionSleeper sleeper : bench.getSleepers()) {

                                sleeperNumbers.add(sleeper.getSleeperNo());
                            }
                        }

                        benchDto.setSleepers(sleeperNumbers);*/
                        List<ProductionSleeperResponseDto> sleeperList = new ArrayList<>();

                        for (ProductionSleeper sleeper : bench.getSleepers()) {

                            ProductionSleeperResponseDto sDto = new ProductionSleeperResponseDto();
                            sDto.setId(sleeper.getId());
                            sDto.setSleeperNo(sleeper.getSleeperNo());

                            sleeperList.add(sDto);
                        }

                        benchDto.setSleeperList(sleeperList);

                        benchList.add(benchDto);
                    }
                }

                chamberDto.setBenchGroups(benchList);

                chamberList.add(chamberDto);
            }

            response.setChambers(chamberList);
        }

        // ================= LONG LINE =================

            if (entity.getGangs() != null) {

                List<ProductionLongLineGangResponseDto> gangList = new ArrayList<>();

                for (ProductionLongLineGang gang : entity.getGangs()) {

                    ProductionLongLineGangResponseDto gangDto = new ProductionLongLineGangResponseDto();

                    gangDto.setId(gang.getId());
                    gangDto.setMode(gang.getMode());
                    gangDto.setGangFrom(gang.getGangFrom());
                    gangDto.setGangTo(gang.getGangTo());
                    gangDto.setGangNo(gang.getGangNo());
                    gangDto.setSleeperType(gang.getSleeperType());
                    gangDto.setMouldsPerGang(gang.getMouldsPerGang());
                    gangDto.setSleeperCategory(gang.getSleeperCategory());
                    gangDto.setTotalSleepers(gang.getTotalSleepers());
                    gangDto.setRft(gang.getRft());


                  /*  List<String> sleeperNumbers = new ArrayList<>();

                    if (gang.getSleepers() != null) {

                        for (ProductionSleeper sleeper : gang.getSleepers()) {
                            sleeperNumbers.add(sleeper.getSleeperNo());
                        }
                    }

                   */

                    List<ProductionSleeperResponseDto> sleeperList = new ArrayList<>();

                    for (ProductionSleeper sleeper : gang.getSleepers()) {

                        ProductionSleeperResponseDto sDto = new ProductionSleeperResponseDto();
                        sDto.setId(sleeper.getId());
                        sDto.setSleeperNo(sleeper.getSleeperNo());

                        sleeperList.add(sDto);
                    }

                    gangDto.setSleeperList(sleeperList);
                  //  gangDto.setSleepers(sleeperNumbers);

                    gangList.add(gangDto);
                }

                response.setGangs(gangList);
            }


        return response;
    }

    @Override
    public void delete(Long id) {
        ProductionDeclaration entity = repository.findById(id)
                .orElseThrow(() -> new BusinessException(
                        new ErrorDetails(
                                AppConstant.ERROR_CODE_RESOURCE,
                                AppConstant.ERROR_TYPE_CODE_RESOURCE,
                                AppConstant.ERROR_TYPE_VALIDATION,
                                "Production not found")));

        repository.deleteById(entity.getId());
        Long moduleId = 11L;

        SleeperWorkflowTransaction lastWorkflow =
                sleeperWorkflowRepository
                        .findTopByModuleIdAndRequestIdOrderByWorkflowTransitionIdDesc(
                                moduleId,
                                String.valueOf(entity.getId())
                        );

        SleeperWorkflowTransaction newWorkflow = new SleeperWorkflowTransaction();

        newWorkflow.setModuleId(moduleId);
        newWorkflow.setRequestId(String.valueOf(entity.getId()));

        newWorkflow.setAction("DELETE");
        newWorkflow.setStatus("DELETED");

        if (lastWorkflow != null) {
            newWorkflow.setWorkflowId(lastWorkflow.getWorkflowId());
            newWorkflow.setCurrentRole(lastWorkflow.getCurrentRole());
            newWorkflow.setNextRole(null);
            newWorkflow.setAssignedToUser(lastWorkflow.getAssignedToUser());
        }

        newWorkflow.setModifiedBy(Long.valueOf(entity.getCreatedBy()));
        newWorkflow.setCreatedDate(LocalDateTime.now());

        sleeperWorkflowRepository.save(newWorkflow);
    }

//    @Override
//    public List<ProductionDeclarationResponseDto> getAll() {
//
//        List<ProductionDeclarationResponseDto> list = new ArrayList<>();
//
//        for (ProductionDeclaration entity : repository.findAll()) {
//
//            list.add(getById(entity.getId()));
//        }
//
//        return list;
//    }
//@Override
//public List<ProductionDeclarationResponseDto> getAll() {
//
//    return repository.findAll()
//            .stream()
//            .map(this::mapToResponse)
//            .toList();
//}
@Override
public List<ProductionDeclarationResponseDto> getAll() {

    List<ProductionDeclaration> entities = repository.findAll();


    Map<String, String> statusMap = sleeperWorkflowRepository
            .findAllLatestStatuses(11L)
            .stream()
            .collect(Collectors.toMap(
                    obj -> String.valueOf(obj[0]),
                    obj -> String.valueOf(obj[1])
            ));

    return entities.stream()
            .map(entity -> mapToResponse(entity, statusMap))
            .toList();
}

    private ProductionDeclarationResponseDto mapToResponse(
            ProductionDeclaration entity,
            Map<String, String> statusMap) {

        ProductionDeclarationResponseDto response = new ProductionDeclarationResponseDto();

        response.setId(entity.getId());
        response.setPlantType(entity.getPlantType());
        response.setProductionUnit(entity.getProductionUnit());
        response.setCastingDate(CommonUtils.convertDateToString(entity.getCastingDate()));
        response.setShift(entity.getShift());
        response.setBatchNumber(entity.getBatchNumber());
        response.setMixDesignReference(entity.getMixDesignReference());
        response.setLbcTime(entity.getLbcTime());
        response.setVendorCode(entity.getVendorCode());
        response.setPlantId(entity.getPlantId());
        response.setTotalCastedSleepers(entity.getTotalCastedSleepers());
        response.setTotalSleeperTypes(entity.getTotalSleeperTypes());
        response.setTotalRft(entity.getTotalRft());
        response.setRemarks(entity.getRemarks());
        response.setCreatedBy(entity.getCreatedBy());
        response.setCreatedDate(entity.getCreatedDate());
        response.setUpdatedBy(entity.getUpdatedBy());
        response.setUpdatedDate(entity.getUpdatedDate());


        String status = statusMap.getOrDefault(String.valueOf(entity.getId()), "NOT_STARTED");
        response.setStatus(status);

        return response;
    }


    @Override
    public List<ProductionDeclarationResponseDto> getAllWithWaterCubeStatus() {

       // List<ProductionDeclaration> entities = repository.findAll();
        List<ProductionDeclaration> entities = repository.findAllExcludingMR();
        // Existing status map
        Map<String, String> statusMap = sleeperWorkflowRepository
                .findAllLatestStatuses(11L)
                .stream()
                .collect(Collectors.toMap(
                        obj -> String.valueOf(obj[0]),
                        obj -> String.valueOf(obj[1])
                ));

        // Get all batch numbers from water cube test
        Set<String> waterCubeBatchSet = new HashSet<>(
                waterCubeStrengthTestRepository.findAllBatchNumbers()
        );

        return entities.stream()
                .map(entity -> mapToResponseWithWaterCube(entity, statusMap, waterCubeBatchSet))
                .toList();
    }

    private ProductionDeclarationResponseDto mapToResponseWithWaterCube(
            ProductionDeclaration entity,
            Map<String, String> statusMap,
            Set<String> waterCubeBatchSet) {

        ProductionDeclarationResponseDto response = new ProductionDeclarationResponseDto();

        response.setId(entity.getId());
        response.setPlantType(entity.getPlantType());
        response.setProductionUnit(entity.getProductionUnit());
        response.setCastingDate(CommonUtils.convertDateToString(entity.getCastingDate()));
        response.setShift(entity.getShift());
        response.setBatchNumber(entity.getBatchNumber());
        response.setMixDesignReference(entity.getMixDesignReference());
        response.setLbcTime(entity.getLbcTime());
        response.setVendorCode(entity.getVendorCode());
        response.setPlantId(entity.getPlantId());
        response.setTotalCastedSleepers(entity.getTotalCastedSleepers());
        response.setTotalSleeperTypes(entity.getTotalSleeperTypes());
        response.setTotalRft(entity.getTotalRft());
        response.setRemarks(entity.getRemarks());
        response.setCreatedBy(entity.getCreatedBy());
        response.setCreatedDate(entity.getCreatedDate());
        response.setUpdatedBy(entity.getUpdatedBy());
        response.setUpdatedDate(entity.getUpdatedDate());


        String status = statusMap.getOrDefault(String.valueOf(entity.getId()), "NOT_STARTED");
        response.setStatus(status);

        boolean exists = waterCubeBatchSet.contains(entity.getBatchNumber());
        response.setWaterCubeTestStatus(exists);

        return response;
    }
   /* private ProductionDeclarationResponseDto mapToResponse(ProductionDeclaration entity) {

        ProductionDeclarationResponseDto response = new ProductionDeclarationResponseDto();

        response.setId(entity.getId());
        response.setPlantType(entity.getPlantType());
        response.setProductionUnit(entity.getProductionUnit());
        response.setCastingDate(CommonUtils.convertDateToString(entity.getCastingDate()));
        response.setShift(entity.getShift());
        response.setBatchNumber(entity.getBatchNumber());
        response.setMixDesignReference(entity.getMixDesignReference());
        response.setLbcTime(entity.getLbcTime());
        response.setVendorCode(entity.getVendorCode());

        response.setPlantId(entity.getPlantId());

        response.setTotalCastedSleepers(entity.getTotalCastedSleepers());
        response.setTotalSleeperTypes(entity.getTotalSleeperTypes());
        response.setTotalRft(entity.getTotalRft());

        response.setRemarks(entity.getRemarks());

        response.setCreatedBy(entity.getCreatedBy());
        response.setCreatedDate(entity.getCreatedDate());
        response.setUpdatedBy(entity.getUpdatedBy());
        response.setUpdatedDate(entity.getUpdatedDate());


        String status = sleeperWorkflowRepository
                .findLatestStatusByRequestIdAndModuleId(String.valueOf(entity.getId()), 11L)
                .orElse("NOT_STARTED");

        response.setStatus(status);
/*
        // ================= STRESS BENCH =================
        if (entity.getChambers() != null) {

            List<ProductionStressChamberResponseDto> chamberList = entity.getChambers().stream().map(chamber -> {

                ProductionStressChamberResponseDto chamberDto = new ProductionStressChamberResponseDto();
                chamberDto.setId(chamber.getId());
                chamberDto.setChamberNo(chamber.getChamberNo());

                if (chamber.getBenchGroups() != null) {

                    List<ProductionBenchGroupResponseDto> benchList = chamber.getBenchGroups().stream().map(bench -> {

                        ProductionBenchGroupResponseDto benchDto = new ProductionBenchGroupResponseDto();

                        benchDto.setId(bench.getId());
                        benchDto.setBenchNo(bench.getBenchNo());
                        benchDto.setSleeperType(bench.getSleeperType());
                        benchDto.setMouldPerBench(bench.getMouldPerBench());
                        benchDto.setRft(bench.getRft());

                        if (bench.getSleepers() != null) {
                            benchDto.setSleepers(
                                    bench.getSleepers().stream()
                                            .map(ProductionSleeper::getSleeperNo)
                                            .toList()
                            );
                        }

                        return benchDto;

                    }).toList();

                    chamberDto.setBenchGroups(benchList);
                }

                return chamberDto;

            }).toList();

            response.setChambers(chamberList);
        }

        // ================= LONG LINE =================
        if (entity.getGangs() != null) {

            List<ProductionLongLineGangResponseDto> gangList = entity.getGangs().stream().map(gang -> {

                ProductionLongLineGangResponseDto gangDto = new ProductionLongLineGangResponseDto();

                gangDto.setId(gang.getId());
                gangDto.setMode(gang.getMode());
                gangDto.setGangFrom(gang.getGangFrom());
                gangDto.setGangTo(gang.getGangTo());
                gangDto.setGangNo(gang.getGangNo());
                gangDto.setSleeperType(gang.getSleeperType());
                gangDto.setMouldsPerGang(gang.getMouldsPerGang());

                return gangDto;

            }).toList();

            response.setGangs(gangList);
        }*/
/*
        return response;
    }*/

    @Override
    public Page<ProductionDeclarationResponseDto> getAllProductions(int page, int size) {


        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());

        Page<ProductionDeclaration> entityPage = repository.findAll(pageable);

        List<ProductionDeclaration> entities = entityPage.getContent();

        // Fetch all statuses in one query
        Map<String, String> statusMap = sleeperWorkflowRepository
                .findAllStatusesByModuleId(11L)
                .stream()
                .collect(Collectors.toMap(
                        obj -> String.valueOf(obj[0]),
                        obj -> String.valueOf(obj[1])
                ));

        //  Map to DTO
        List<ProductionDeclarationResponseDto> responseList = entities.stream()
                .map(entity -> mapToResp(entity, statusMap))
                .toList();

        //  Return paginated response
        return new PageImpl<>(responseList, pageable, entityPage.getTotalElements());
    }
    private ProductionDeclarationResponseDto mapToResp(
            ProductionDeclaration entity,
            Map<String, String> statusMap) {

        ProductionDeclarationResponseDto response = new ProductionDeclarationResponseDto();

        response.setId(entity.getId());
        response.setPlantType(entity.getPlantType());
        response.setProductionUnit(entity.getProductionUnit());
        response.setCastingDate(CommonUtils.convertDateToString(entity.getCastingDate()));
        response.setShift(entity.getShift());
        response.setBatchNumber(entity.getBatchNumber());
        response.setMixDesignReference(entity.getMixDesignReference());
        response.setLbcTime(entity.getLbcTime());
        response.setVendorCode(entity.getVendorCode());
        response.setPlantId(entity.getPlantId());
        response.setTotalCastedSleepers(entity.getTotalCastedSleepers());
        response.setTotalSleeperTypes(entity.getTotalSleeperTypes());
        response.setTotalRft(entity.getTotalRft());
        response.setRemarks(entity.getRemarks());
        response.setCreatedBy(entity.getCreatedBy());
        response.setCreatedDate(entity.getCreatedDate());
        response.setUpdatedBy(entity.getUpdatedBy());
        response.setUpdatedDate(entity.getUpdatedDate());

        // STATUS
        String status = statusMap.getOrDefault(String.valueOf(entity.getId()), "NOT_STARTED");
        response.setStatus(status);

        // STRESS BENCH
        if (entity.getChambers() != null) {
            response.setChambers(
                    entity.getChambers().stream().map(chamber -> {

                        ProductionStressChamberResponseDto chamberDto =
                                new ProductionStressChamberResponseDto();

                        chamberDto.setId(chamber.getId());
                        chamberDto.setChamberNo(chamber.getChamberNo());

                        if (chamber.getBenchGroups() != null) {
                            chamberDto.setBenchGroups(
                                    chamber.getBenchGroups().stream().map(bench -> {

                                        ProductionBenchGroupResponseDto benchDto =
                                                new ProductionBenchGroupResponseDto();

                                        benchDto.setId(bench.getId());
                                        benchDto.setBenchNo(bench.getBenchNo());
                                        benchDto.setSleeperType(bench.getSleeperType());
                                        benchDto.setMouldPerBench(bench.getMouldPerBench());
                                        benchDto.setRft(bench.getRft());

//                                        if (bench.getSleepers() != null) {
//                                            benchDto.setSleepers(
//                                                    bench.getSleepers().stream()
//                                                            .map(ProductionSleeper::getSleeperNo)
//                                                            .toList()
//                                            );
//                                        }

                                        if (bench.getSleepers() != null) {
                                            benchDto.setSleeperList(
                                                    bench.getSleepers().stream()
                                                            .map(s -> {
                                                                ProductionSleeperResponseDto dto = new ProductionSleeperResponseDto();
                                                                dto.setId(s.getId());
                                                                dto.setSleeperNo(s.getSleeperNo());
                                                                return dto;
                                                            })
                                                            .toList()
                                            );
                                        }

                                        return benchDto;

                                    }).toList()
                            );
                        }

                        return chamberDto;

                    }).toList()
            );
        }

        // LONG LINE
        if (entity.getGangs() != null) {
            response.setGangs(
                    entity.getGangs().stream().map(gang -> {

                        ProductionLongLineGangResponseDto gangDto =
                                new ProductionLongLineGangResponseDto();

                        gangDto.setId(gang.getId());
                        gangDto.setMode(gang.getMode());
                        gangDto.setGangFrom(gang.getGangFrom());
                        gangDto.setGangTo(gang.getGangTo());
                        gangDto.setGangNo(gang.getGangNo());
                        gangDto.setSleeperType(gang.getSleeperType());
                        gangDto.setMouldsPerGang(gang.getMouldsPerGang());

                        //ADD THIS (IMPORTANT)
//                        if (gang.getSleepers() != null) {
//                            gangDto.setSleepers(
//                                    gang.getSleepers().stream()
//                                            .map(ProductionSleeper::getSleeperNo)
//                                            .toList()
//                            );
//                        }

                        if (gang.getSleepers() != null) {
                            gangDto.setSleeperList(
                                    gang.getSleepers().stream()
                                            .map(s -> {
                                                ProductionSleeperResponseDto dto = new ProductionSleeperResponseDto();
                                                dto.setId(s.getId());
                                                dto.setSleeperNo(s.getSleeperNo());
                                                return dto;
                                            })
                                            .toList()
                            );
                        }

                        return gangDto;

                    }).toList()
            );
        }

        return response;
    }

    @Override
    public List<ProductionDeclarationResponseDto> getByUser(Long userId) {

        List<ProductionDeclarationResponseDto> list = new ArrayList<>();

        for (ProductionDeclaration entity : repository.findByCreatedBy(userId)) {
            list.add(getById(entity.getId()));
        }

        return list;
    }

    @Override
    public List<String> getVerifiedProductionDeclarations() {
        List<String> requestIds = sleeperWorkflowRepository.findCompletedRequestIdsByModuleId(11L);
        if (requestIds == null || requestIds.isEmpty()) {
            return new ArrayList<>();
        }

        List<Long> ids = requestIds.stream()
                .map(Long::valueOf)
                .toList();

        List<ProductionDeclaration> verifiedDeclarations = repository.findByIdIn(ids);

        return verifiedDeclarations.stream()
                .map(v -> v.getPlantType() + "/" + v.getCastingDate() + "/" + v.getBatchNumber() + "(" + v.getCreatedBy() + ")")
                .toList();
    }


//    @Override
//    public List<String> getBatchNumbers(Long vendorId, LocalDate castingDate) {
//        return repository.findBatchNumbers(vendorId, castingDate);
//    }
@Override
public List<String> getBatchNumbers(Long vendorId,
                                    LocalDate castingDate,
                                    String plantId,
                                    String productionUnit) {

    return repository.findValidBatchNumbers(
            vendorId, castingDate, plantId, productionUnit
    );
}
  /*  @Override
    public List<String> getBenchNumbers(String batchNo) {
        //return repository.findBenchNumbers(batchNo);


            List<Object[]> results = repository.findBenchAndGangRaw(batchNo);

            Set<String> finalSet = new LinkedHashSet<>();

            for (Object[] row : results) {

                String bench = row[0] != null ? String.valueOf(row[0]) : null;

                Integer gangFrom = row[1] != null ? ((Number) row[1]).intValue() : null;
                Integer gangTo = row[2] != null ? ((Number) row[2]).intValue() : null;

                // STRESS
                if (bench != null) {
                    finalSet.add(bench);
                }

                // LONG_LINE
                else if (gangFrom != null && gangTo != null) {
                    for (int i = gangFrom; i <= gangTo; i++) {
                        finalSet.add(String.valueOf(i));
                    }
                }
            }

            return new ArrayList<>(finalSet);
        }*/
  @Override
  public List<String> getBenchNumbers(String batchNo, String productionUnit) {

      Set<String> finalSet = new LinkedHashSet<>();

      //  Bench Numbers (STRESS)
      List<String> benches = repository.findBenchNumbers(batchNo, productionUnit);
      finalSet.addAll(benches);

      //  Gang Ranges (LONG LINE)
      List<Object[]> gangResults = repository.findGangRanges(batchNo, productionUnit);

     /* for (Object[] row : gangResults) {

          Integer gangFrom = row[0] != null ? ((Number) row[0]).intValue() : null;
          Integer gangTo = row[1] != null ? ((Number) row[1]).intValue() : null;

          if (gangFrom != null && gangTo != null) {
              for (int i = gangFrom; i <= gangTo; i++) {
                  finalSet.add(String.valueOf(i));
              }
          }
      }
      */
      for (Object[] row : gangResults) {

          String mode = row[0] != null ? row[0].toString() : null;
          Integer gangFrom = row[1] != null ? ((Number) row[1]).intValue() : null;
          Integer gangTo = row[2] != null ? ((Number) row[2]).intValue() : null;
          Integer gangNo = row[3] != null ? ((Number) row[3]).intValue() : null;

          if ("RANGE".equalsIgnoreCase(mode)) {

              if (gangFrom != null && gangTo != null) {
                  for (int i = gangFrom; i <= gangTo; i++) {
                      finalSet.add(String.valueOf(i));
                  }
              }

          } else if ("SINGLE".equalsIgnoreCase(mode)) {

              if (gangNo != null) {
                  finalSet.add(String.valueOf(gangNo));
              }
          }
      }

      return new ArrayList<>(finalSet);
  }

//    @Override
//    public List<String> getSleeperTypes(String batchNo, Integer benchNo) {
//        return productionBenchGroupRepository.findSleeperTypes(batchNo, benchNo);
//    }
    @Override
    public List<String> getSleeperTypes(String batchNo, Integer benchNo, String productionUnit) {

        ProductionDeclaration declaration = repository.findByBatchNumberAndProductionUnit(batchNo, productionUnit);

        Set<String> sleeperTypes = new LinkedHashSet<>();

        if ("STRESS".equalsIgnoreCase(declaration.getPlantType())) {
            if (declaration.getChambers() != null) {
                for (ProductionStressChamber chamber : declaration.getChambers()) {
                    if (chamber.getBenchGroups() != null) {
                        for (ProductionBenchGroup bench : chamber.getBenchGroups()) {
                            if (benchNo.equals(bench.getBenchNo()) && bench.getSleeperType() != null) {
                                sleeperTypes.add(bench.getSleeperType());
                            }
                        }
                    }
                }
            }
        } else { // LONG_LINE
            if (declaration.getGangs() != null) {
                for (ProductionLongLineGang gang : declaration.getGangs()) {
                    boolean matchesBench = false;
                    if (gang.getGangFrom() != null && gang.getGangTo() != null && benchNo >= gang.getGangFrom() && benchNo <= gang.getGangTo()) {
                        matchesBench = true;
                    } else if (gang.getGangNo() != null && gang.getGangNo().equals(benchNo)) {
                        matchesBench = true;
                    }
                    if (matchesBench && gang.getSleeperType() != null) {
                        sleeperTypes.add(gang.getSleeperType());
                    }
                }
            }
        }

        return new ArrayList<>(sleeperTypes);
    }

//    @Override
//    public List<String> getSleepers(String batchNo, Integer benchNo, String sleeperType) {
//        return productionSleeperRepository.findSleepers(batchNo, benchNo, sleeperType);
//    }

    @Override
    public List<String> getSleepers(String batchNo, Integer benchNo, String sleeperType, String productionUnit) {

        ProductionDeclaration declaration;
        if (productionUnit != null && !productionUnit.isEmpty()) {
            declaration = repository.findByBatchNumberAndProductionUnit(batchNo, productionUnit);
        } else {
            declaration = repository.findByBatchNumber(batchNo);
        }

        List<String> sleepers = new ArrayList<>();

        if ("STRESS".equalsIgnoreCase(declaration.getPlantType())) {
            if (declaration.getChambers() != null) {
                for (ProductionStressChamber chamber : declaration.getChambers()) {
                    if (chamber.getBenchGroups() != null) {
                        for (ProductionBenchGroup bench : chamber.getBenchGroups()) {
                            if (benchNo.equals(bench.getBenchNo()) && sleeperType.equals(bench.getSleeperType())) {
                                if (bench.getSleepers() != null) {
                                    for (ProductionSleeper sleeper : bench.getSleepers()) {
                                        if (sleeper.getSleeperNo() != null) {
                                            sleepers.add(sleeper.getSleeperNo());
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } else { // LONG_LINE
            if (declaration.getGangs() != null) {
                for (ProductionLongLineGang gang : declaration.getGangs()) {
                    boolean matchesBench = false;
                    if (gang.getGangFrom() != null && gang.getGangTo() != null && benchNo >= gang.getGangFrom() && benchNo <= gang.getGangTo()) {
                        matchesBench = true;
                    } else if (gang.getGangNo() != null && gang.getGangNo().equals(benchNo)) {
                        matchesBench = true;
                    }
                    
                    if (matchesBench && sleeperType.equals(gang.getSleeperType())) {
                        if (gang.getSleepers() != null) {
                            for (ProductionSleeper sleeper : gang.getSleepers()) {
                                if (sleeper.getSleeperNo() != null) {
                                    sleepers.add(sleeper.getSleeperNo());
                                }
                            }
                        }
                    }
                }
            }
        }

        return sleepers;
    }


    @Override
    public List<Map<String, Object>> getBatchWithId(
            Long vendorId,
            LocalDate castingDate,
            String plantId,
            String productionUnit) {

        List<BatchWithIdProjection> list =
                repository.findBatchWithId(
                        vendorId, castingDate, plantId, productionUnit
                );

        List<Map<String, Object>> result = new ArrayList<>();

        for (BatchWithIdProjection p : list) {
            Map<String, Object> map = new HashMap<>();
            map.put("batchNumber", p.getBatchNumber());
            map.put("id", p.getId());
            result.add(map);
        }

        return result;
    }

}
