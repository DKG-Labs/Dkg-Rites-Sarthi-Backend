package com.sarthi.Sleeper.service.Impl;

import com.sarthi.Sleeper.dto.BenchDetailsResponseDto;
import com.sarthi.Sleeper.dto.ProductionDeclaration.*;
import com.sarthi.Sleeper.entity.PlantProfile;
import com.sarthi.Sleeper.entity.ProductionDeclaration.*;
import com.sarthi.Sleeper.entity.SleeperWorkflowTransaction;
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
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    @Override
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

        entity.setPlantType(dto.getPlantType());
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

                    bench.setChamber(chamber);

                    List<ProductionSleeper> sleepers = new ArrayList<>();

                    for (String sleeperNo : benchDto.getSleepers()) {

                        ProductionSleeper sleeper = new ProductionSleeper();

                        sleeper.setSleeperNo(sleeperNo);
                        sleeper.setBenchGroup(bench);

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
    /*
     * @Override
     * public ProductionDeclarationResponseDto update(
     * Long id,
     * ProductionDeclarationRequestDto dto) {
     * 
     * ProductionDeclaration entity = repository.findById(id)
     * .orElseThrow(() -> new RuntimeException("Record not found"));
     * 
     * // ===== Header Fields =====
     * 
     * entity.setPlantType(dto.getPlantType());
     * entity.setProductionUnit(dto.getProductionUnit());
     * LocalDate cDate =
     * CommonUtils.convertStringToDateObject(dto.getCastingDate());
     * 
     * entity.setCastingDate(cDate);
     * entity.setShift(dto.getShift());
     * entity.setBatchNumber(dto.getBatchNumber());
     * entity.setMixDesignReference(dto.getMixDesignReference());
     * LocalTime pTime = CommonUtils.convertStringToTimeObject(dto.getLbcTime());
     * 
     * 
     * entity.setLbcTime(pTime);
     * 
     * entity.setTotalCastedSleepers(dto.getTotalCastedSleepers());
     * entity.setTotalSleeperTypes(dto.getTotalSleeperTypes());
     * entity.setTotalRft(dto.getTotalRft());
     * 
     * entity.setRemarks(dto.getRemarks());
     * 
     * entity.setUpdatedBy(dto.getUpdatedBy());
     * entity.setUpdatedDate(LocalDateTime.now());
     * 
     * 
     * // ===== CLEAR OLD DATA =====
     * 
     * entity.getChambers().clear();
     * entity.getGangs().clear();
     * 
     * 
     * // ===== STRESS CHAMBERS =====
     * 
     * if (dto.getChambers() != null) {
     * 
     * for (ProductionStressChamberRequestDto chamberDto : dto.getChambers()) {
     * 
     * ProductionStressChamber chamber = new ProductionStressChamber();
     * 
     * chamber.setChamberNo(chamberDto.getChamberNo());
     * chamber.setDeclaration(entity);
     * 
     * entity.getChambers().add(chamber);
     * 
     * 
     * if (chamberDto.getBenchGroups() != null) {
     * 
     * for (ProductionBenchGroupRequestDto benchDto : chamberDto.getBenchGroups()) {
     * 
     * ProductionBenchGroup bench = new ProductionBenchGroup();
     * 
     * bench.setBenchNo(benchDto.getBenchNo());
     * bench.setSleeperType(benchDto.getSleeperType());
     * bench.setMouldPerBench(benchDto.getMouldPerBench());
     * bench.setRft(benchDto.getRft());
     * 
     * bench.setChamber(chamber);
     * 
     * chamber.getBenchGroups().add(bench);
     * 
     * 
     * if (benchDto.getSleepers() != null) {
     * 
     * for (String sleeperNo : benchDto.getSleepers()) {
     * 
     * ProductionSleeper sleeper = new ProductionSleeper();
     * 
     * sleeper.setSleeperNo(sleeperNo);
     * sleeper.setBenchGroup(bench);
     * 
     * bench.getSleepers().add(sleeper);
     * }
     * }
     * }
     * }
     * }
     * }
     * 
     * 
     * // ===== LONG LINE =====
     * 
     * if (dto.getGangs() != null) {
     * 
     * for (ProductionLongLineGangRequestDto gangDto : dto.getGangs()) {
     * 
     * ProductionLongLineGang gang = new ProductionLongLineGang();
     * 
     * gang.setMode(gangDto.getMode());
     * gang.setGangFrom(gangDto.getGangFrom());
     * gang.setGangTo(gangDto.getGangTo());
     * gang.setGangNo(gangDto.getGangNo());
     * gang.setSleeperType(gangDto.getSleeperType());
     * gang.setMouldsPerGang(gangDto.getMouldsPerGang());
     * 
     * gang.setDeclaration(entity);
     * 
     * entity.getGangs().add(gang);
     * }
     * }
     * 
     * repository.save(entity);
     * 
     * // return full response
     * return getById(entity.getId());
     * }
     * 
     * 
     */

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

                        List<String> sleeperNumbers = new ArrayList<>();

                        if (bench.getSleepers() != null) {

                            for (ProductionSleeper sleeper : bench.getSleepers()) {

                                sleeperNumbers.add(sleeper.getSleeperNo());
                            }
                        }

                        benchDto.setSleepers(sleeperNumbers);

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
@Override
public List<ProductionDeclarationResponseDto> getAll() {

    return repository.findAll()
            .stream()
            .map(this::mapToResponse)
            .toList();
}
    private ProductionDeclarationResponseDto mapToResponse(ProductionDeclaration entity) {

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


    @Override
    public List<String> getBatchNumbers(Long vendorId, LocalDate castingDate) {
        return repository.findBatchNumbers(vendorId, castingDate);
    }
    @Override
    public List<String> getBenchNumbers(String batchNo) {
        return repository.findBenchNumbers(batchNo);
    }

    @Override
    public List<String> getSleeperTypes(String batchNo, Integer benchNo) {
        return productionBenchGroupRepository.findSleeperTypes(batchNo, benchNo);
    }

    @Override
    public List<String> getSleepers(String batchNo, Integer benchNo, String sleeperType) {
        return productionSleeperRepository.findSleepers(batchNo, benchNo, sleeperType);
    }


}
