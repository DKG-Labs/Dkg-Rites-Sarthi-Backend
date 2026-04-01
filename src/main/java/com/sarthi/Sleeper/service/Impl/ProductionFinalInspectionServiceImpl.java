package com.sarthi.Sleeper.service.Impl;

import com.sarthi.Sleeper.dto.BadSleeperDto;
import com.sarthi.Sleeper.dto.BatchInspectionResponseDto;
import com.sarthi.Sleeper.dto.FinalInspectionDtos.*;
import com.sarthi.Sleeper.entity.FinalInspection.*;
import com.sarthi.Sleeper.entity.ProductionDeclaration.ProductionDeclaration;
import com.sarthi.Sleeper.entity.ProductionDeclaration.ProductionSleeper;
import com.sarthi.Sleeper.repository.DemouldingDefectiveSleeperRepository;
import com.sarthi.Sleeper.repository.FinalInspectionRepository.*;
import com.sarthi.Sleeper.repository.ProductionDeclaration.ProductionBenchGroupRepository;
import com.sarthi.Sleeper.repository.ProductionDeclaration.ProductionDeclarationRepository;
import com.sarthi.Sleeper.repository.ProductionDeclaration.ProductionSleeperRepository;
import com.sarthi.Sleeper.repository.SleeperWorkflowRepository;
import com.sarthi.Sleeper.service.ProductionFinalInspectionService;
import com.sarthi.entity.UserMaster;
import com.sarthi.repository.UserMasterRepository;

import java.util.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.stream.Collectors;

@Service
public class ProductionFinalInspectionServiceImpl implements ProductionFinalInspectionService {


    @Autowired
    private InspectionModuleRepository moduleRepository;
    
    @Autowired
    private UserMasterRepository userMasterRepository;

    @Autowired
    private InspectionTestHeaderRepository headerRepository;

    @Autowired
    private InspectionTestResultRepository resultRepository;

    @Autowired
    private InspectionParameterRepository parameterRepository;

    @Autowired
    private InspectionParameterResultRepository parameterResultRepository;
    @Autowired
    private ProductionDeclarationRepository productionDeclarationRepository;
    @Autowired
    private ProductionSleeperRepository productionSleeperRepository;
    @Autowired
    private SleeperWorkflowRepository sleeperWorkflowRepository;
    @Autowired
    private DemouldingDefectiveSleeperRepository demouldingDefectiveSleeperRepository;

  /*  @Override
    public void saveInspection(InspectionSaveRequestDto dto) {

        InspectionModule module =
                moduleRepository.findById(dto.getModuleId())
                        .orElseThrow();

        InspectionTestHeader header = new InspectionTestHeader();

        header.setBatchId(dto.getBatchId());
        header.setModule(module);
        header.setShift(dto.getShift());
        header.setCreatedBy(dto.getCreatedBy());
        header.setTestDate(LocalDate.now());
        header.setCreatedDate(LocalDateTime.now());

        headerRepository.save(header);

        for (SleeperInspectionDto sleeperDto : dto.getSleepers()) {

            InspectionTestResult result = new InspectionTestResult();

            result.setTestHeader(header);
            result.setSleeperId(sleeperDto.getSleeperId());
            result.setSleeperNo(sleeperDto.getSleeperNo());
            result.setResult(sleeperDto.getResult());
            result.setRejectionReason(sleeperDto.getRejectionReason());

            resultRepository.save(result);

            if (sleeperDto.getParameters() != null) {

                for (ParameterInspectionDto paramDto : sleeperDto.getParameters()) {

                    InspectionParameter parameter =
                            parameterRepository.findById(paramDto.getParameterId())
                                    .orElseThrow();

                    InspectionParameterResult paramResult =
                            new InspectionParameterResult();

                    paramResult.setTestResult(result);
                    paramResult.setParameter(parameter);
                    paramResult.setParameterResult(paramDto.getResult());

                    parameterResultRepository.save(paramResult);
                }
            }
        }
        checkAndUpdateModuleCompletion(dto.getBatchId(), dto.getModuleId());
    }*/
  @Transactional
  @Override
  public void saveInspection(InspectionSaveRequestDto dto) {

      InspectionModule module = moduleRepository
              .findById(dto.getModuleId())
              .orElseThrow();

      InspectionTestHeader header = new InspectionTestHeader();
      header.setBatchId(dto.getBatchId());
      header.setModule(module);
      header.setShift(dto.getShift());
      header.setCreatedBy(dto.getCreatedBy());
      header.setTestDate(LocalDate.now());
      header.setCreatedDate(LocalDateTime.now());

      headerRepository.save(header);

      Map<Long, InspectionParameter> parameterMap =
              parameterRepository.findAll()
                      .stream()
                      .collect(Collectors.toMap(InspectionParameter::getId, p -> p));

      List<InspectionParameterResult> parameterResults = new ArrayList<>();

   /*   for (SleeperInspectionDto sleeperDto : dto.getSleepers()) {

          InspectionTestResult result = new InspectionTestResult();

          result.setTestHeader(header);
          result.setSleeperId(sleeperDto.getSleeperId());
          result.setSleeperNo(sleeperDto.getSleeperNo());
          result.setResult(sleeperDto.getResult());
          result.setRejectionReason(sleeperDto.getRejectionReason());

          resultRepository.save(result);

          if (sleeperDto.getParameters() != null) {

              for (ParameterInspectionDto paramDto : sleeperDto.getParameters()) {

                  InspectionParameter parameter =
                          parameterMap.get(paramDto.getParameterId());

                  InspectionParameterResult paramResult =
                          new InspectionParameterResult();

                  paramResult.setTestResult(result);
                  paramResult.setParameter(parameter);
                  paramResult.setParameterResult(paramDto.getResult());

                  parameterResults.add(paramResult);
              }
          }
      } */
      for (SleeperInspectionDto sleeperDto : dto.getSleepers()) {

          //  CHECK if already tested
          boolean alreadyTested =
                  resultRepository.existsByBatchIdAndModuleIdAndSleeperId(
                          dto.getBatchId(),
                          dto.getModuleId(),
                          sleeperDto.getSleeperId()
                  );

          if (alreadyTested) {
              continue; //  skip already tested sleeper
          }

          //  Only create if not exists
          InspectionTestResult result = new InspectionTestResult();

          result.setTestHeader(header);
          result.setSleeperId(sleeperDto.getSleeperId());
          result.setSleeperNo(sleeperDto.getSleeperNo());
          result.setResult(sleeperDto.getResult());
          result.setRejectionReason(sleeperDto.getRejectionReason());

          resultRepository.save(result);
          //  parameters logic (no change)
          if (sleeperDto.getParameters() != null) {

              for (ParameterInspectionDto paramDto : sleeperDto.getParameters()) {

                  InspectionParameter parameter =
                          parameterMap.get(paramDto.getParameterId());

                  InspectionParameterResult paramResult =
                          new InspectionParameterResult();

                  paramResult.setTestResult(result);
                  paramResult.setParameter(parameter);
                  paramResult.setParameterResult(paramDto.getResult());

                  parameterResults.add(paramResult);
              }
          }
      }

      parameterResultRepository.saveAll(parameterResults);

      checkAndUpdateModuleCompletion(dto.getBatchId(), dto.getModuleId());
  }

    private void checkAndUpdateModuleCompletion(Long batchId, Long moduleId) {

        Long totalSleepers =
                productionSleeperRepository.countByBatchId(batchId);

        Long testedSleepers =
                resultRepository.countTestedSleepers(batchId, moduleId);

        String sleeperType =
                productionSleeperRepository.getSleeperTypeByBatch(batchId);

        double testedPercentage =
                ((double) testedSleepers / totalSleepers) * 100;

        boolean completed = false;

        // MODULE 1 → VISUAL
        if(moduleId == 1){

            if(testedPercentage == 100){
                completed = true;
            }

        }

        // MODULE 2 → CRITICAL DIMENSION
        if(moduleId == 2){

            if("RT-8521".equalsIgnoreCase(sleeperType) && testedPercentage >= 10){
                completed = true;
            }

            if("RT-8746".equalsIgnoreCase(sleeperType) && testedPercentage >= 20){
                completed = true;
            }

        }

        // MODULE 3 → NON CRITICAL
        if(moduleId == 3){

            if("RT-8521".equalsIgnoreCase(sleeperType) && testedPercentage >= 1){
                completed = true;
            }

            if("RT-8746".equalsIgnoreCase(sleeperType) && testedPercentage >= 5){
                completed = true;
            }

        }

        if(completed){
            updateModuleStatus(batchId,moduleId);
        }
    }


    private void updateModuleStatus(Long batchId, Long moduleId){

        InspectionTestHeader header =
                headerRepository
                        .findTopByBatchIdAndModuleIdOrderByIdDesc(batchId,moduleId);


        header.setStatus("Completed");

        headerRepository.save(header);
    }

    @Override
    public List<BatchTestingListResponseDto> getAllBatchTesting(Long moduleId) {

        List<BatchTestingListResponseDto> list =
                productionDeclarationRepository.getAllBatchTesting();

        List<BatchTestingListResponseDto> filteredList = new ArrayList<>();

        for (BatchTestingListResponseDto dto : list) {

            //  Check workflow completed
            Long isCompleted =
                   sleeperWorkflowRepository
                            .isWorkflowCompleted(dto.getBatchId());

            if (isCompleted!=1) {
                continue; // skip this batch
            }

            Long testedCount =
                    resultRepository.countTestedSleepers(dto.getBatchId(),moduleId);

            double percent =
                    (testedCount * 100.0) / dto.getNoOfSleepers();

            dto.setTestedPercentage(percent);

            if (percent == 0)
                dto.setTestingStatus("Pending");
            else if (percent == 100)
                dto.setTestingStatus("Completed");
            else
                dto.setTestingStatus("Under Inspection");

            filteredList.add(dto);
        }

        return filteredList;
    }

 /*   @Override
    public BatchInspectionDetailDto getBatchInspection(Long batchId) {

        ProductionDeclaration declaration =
                productionDeclarationRepository.findBatchById(batchId);

        List<ProductionSleeper> sleepers =
                productionSleeperRepository.getSleepersByBatch(batchId);

        // Fetch inspection results
        List<InspectionTestResult> results =
                resultRepository.findByBatchId(batchId);

        // Map sleeperId -> result
        Map<Long, String> resultMap = results.stream()
                .collect(Collectors.toMap(
                        InspectionTestResult::getSleeperId,
                        InspectionTestResult::getResult,
                        (a, b) -> b
                ));

        BatchInspectionDetailDto dto = new BatchInspectionDetailDto();

        dto.setBatchId(declaration.getId());
        dto.setBatchNumber(declaration.getBatchNumber());
        dto.setCastingDate(declaration.getCastingDate());
        dto.setTotalSleepers((long) sleepers.size());

        List<SleeperDto> sleeperDtos = sleepers.stream()
                .map(s -> {

                    SleeperDto sd = new SleeperDto();

                    sd.setSleeperId(s.getId());
                    sd.setSleeperNo(s.getSleeperNo());

                    String status = resultMap.get(s.getId());

                    if (status == null)
                        sd.setStatus("PENDING");
                    else
                        sd.setStatus(status);

                    return sd;

                }).toList();

        dto.setSleepers(sleeperDtos);

        return dto;
    } */
 @Override
 public BatchInspectionDetailDto getBatchInspection(Long batchId) {

     ProductionDeclaration declaration =
             productionDeclarationRepository.findBatchById(batchId);

     List<ProductionSleeper> sleepers =
             productionSleeperRepository.getSleepersByBatch(batchId);

     // Fetch inspection results
     List<InspectionTestResult> results =
             resultRepository.findByBatchId(batchId);

     // Map sleeperId -> result
     Map<Long, String> resultMap = results.stream()
             .collect(Collectors.toMap(
                     InspectionTestResult::getSleeperId,
                     InspectionTestResult::getResult,
                     (a, b) -> b
             ));

     // Fetch rejected sleepers from demoulding (Optimized Set)


     Set<String> rejectedSet =
             demouldingDefectiveSleeperRepository
                     .findRejectedSleeperNos(declaration.getBatchNumber());

     String sleeperType =
             productionSleeperRepository.getSleeperTypeByBatch(batchId);



     BatchInspectionDetailDto dto = new BatchInspectionDetailDto();

     dto.setBatchId(declaration.getId());
     dto.setBatchNumber(declaration.getBatchNumber());
     dto.setCastingDate(declaration.getCastingDate());
     dto.setTotalSleepers((long) sleepers.size());
     dto.setSleeperType(sleeperType);
     List<SleeperDto> sleeperDtos = sleepers.stream()
             .map(s -> {

                 SleeperDto sd = new SleeperDto();

                 sd.setSleeperId(s.getId());
                 sd.setSleeperNo(s.getSleeperNo());



                 if (rejectedSet.contains(s.getSleeperNo())) {
                     sd.setStatus("REJECTED");
                 } else {
                     // Existing logic
                     String status = resultMap.get(s.getId());
                     sd.setStatus(status != null ? status : "PENDING");
                 }

                 return sd;

             }).toList();

     dto.setSleepers(sleeperDtos);

     return dto;
 }


    @Override
    public List<BatchInspectionResponseDto> getCompletedBatches(String sleeperType, String userId) {

        String parsedUserId = userId.replace(":", "");
        Long vendorId = Long.parseLong(parsedUserId);

        Optional<UserMaster> userOpt = userMasterRepository.findByUserName(userId);
        if (userOpt.isEmpty()) {
            userOpt = userMasterRepository.findByUserName(parsedUserId);
        }

        if (userOpt.isPresent()) {
            vendorId = userOpt.get().getUserId().longValue();
        }

        List<Long> batchIds = headerRepository.findCompletedBatchIdsBySleeperTypeAndUserId(sleeperType, vendorId);

        List<BatchInspectionResponseDto> responseList = new ArrayList<>();

        for (Long batchId : batchIds) {

            List<InspectionTestResult> results =
                    resultRepository.findAllResultsByBatchId(batchId);

            Map<Long, List<InspectionTestResult>> grouped =
                    results.stream().collect(Collectors.groupingBy(InspectionTestResult::getSleeperId));

            List<SleeperDto> goodSleepers = new ArrayList<>();
            List<BadSleeperDto> badSleepers = new ArrayList<>();

            for (Map.Entry<Long, List<InspectionTestResult>> entry : grouped.entrySet()) {

                List<InspectionTestResult> sleeperResults = entry.getValue();

                boolean isRejected = sleeperResults.stream()
                        .anyMatch(r -> "REJECTED".equalsIgnoreCase(r.getResult()));

                InspectionTestResult first = sleeperResults.get(0);

                if (isRejected) {

                    BadSleeperDto bad = new BadSleeperDto();
                    bad.setSleeperId(first.getSleeperId());
                    bad.setSleeperNo(first.getSleeperNo());

                    sleeperResults.stream()
                            .filter(r -> "REJECTED".equalsIgnoreCase(r.getResult()))
                            .findFirst()
                            .ifPresent(r -> bad.setReason(r.getRejectionReason()));

                    badSleepers.add(bad);

                } else {

                    SleeperDto dto = new SleeperDto();
                    dto.setSleeperId(first.getSleeperId());
                    dto.setSleeperNo(first.getSleeperNo());

                    goodSleepers.add(dto);
                }
            }

            BatchInspectionResponseDto response =
                    new BatchInspectionResponseDto();

            ProductionDeclaration declaration =
                    productionDeclarationRepository.findBatchById(batchId);

            response.setBatchId(batchId);
            if (declaration != null) {
                response.setBatchNumber(declaration.getBatchNumber());
                response.setCastDate(declaration.getCastingDate() != null ? declaration.getCastingDate().toString() : "");
            }
          //  response.setTotalSleepers((long) results.size());
            response.setTotalSleepers((long) grouped.size());
            response.setGoodCount((long) goodSleepers.size());
            response.setBadCount((long) badSleepers.size());
            response.setGoodSleepers(goodSleepers);
            response.setBadSleepers(badSleepers);

            responseList.add(response);
        }

        return responseList;
    }

}
