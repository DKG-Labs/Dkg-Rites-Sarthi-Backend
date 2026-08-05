package com.sarthi.Sleeper.service.Impl;

import com.sarthi.Sleeper.dto.BadSleeperDto;
import com.sarthi.Sleeper.dto.BatchInspectionResponseDto;
import com.sarthi.Sleeper.dto.FinalInspectionDtos.*;
import com.sarthi.Sleeper.entity.EtSleeperDetails;
import com.sarthi.Sleeper.entity.FinalInspection.*;
import com.sarthi.Sleeper.entity.InspectionReasonMaster;
import com.sarthi.Sleeper.entity.ProductionDeclaration.ProductionDeclaration;
import com.sarthi.Sleeper.entity.ProductionDeclaration.ProductionSleeper;
import com.sarthi.Sleeper.repository.*;
import com.sarthi.Sleeper.repository.FinalInspectionRepository.*;
import com.sarthi.Sleeper.repository.ProductionDeclaration.ProductionBenchGroupRepository;
import com.sarthi.Sleeper.repository.ProductionDeclaration.ProductionDeclarationRepository;
import com.sarthi.Sleeper.repository.ProductionDeclaration.ProductionSleeperRepository;
import com.sarthi.Sleeper.service.ProductionFinalInspectionService;
import com.sarthi.entity.UserMaster;
import com.sarthi.repository.UserMasterRepository;

import java.util.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;

import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import java.sql.Statement;

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

    @Autowired
    private SteamCubeSampleDeclarationRepository steamCubeSampleDeclarationRepository;
    @Autowired
    private DemouldingInspectionRepository demouldingInspectionRepository;
    @Autowired
    private WaterCubeStrengthTestRepository waterCubeStrengthTestRepository;

    @Autowired
    private EtSleeperDetailsRepository etSleeperDetailsRepository;

    @Autowired
    private SleeperInspectionCallRepository inspectionCallRepository;

    @Autowired
    private InspectionReasonMasterRepository reasonRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;


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
        try {
            InspectionModule module = moduleRepository
                    .findById(dto.getModuleId())
                    .orElseThrow(() -> new IllegalArgumentException("Module not found: " + dto.getModuleId()));

            InspectionTestHeader header = new InspectionTestHeader();
            header.setBatchId(dto.getBatchId());
            header.setModule(module);
            header.setShift(dto.getShift());
            header.setCreatedBy(dto.getCreatedBy());
            header.setTestDate(LocalDate.now());
            header.setCreatedDate(LocalDateTime.now());
            header.setSleeperType(dto.getSleeperType());
            headerRepository.save(header);

            // Pre-fetch parameters to a map
            Map<Long, InspectionParameter> parameterMap = parameterRepository.findAll()
                    .stream()
                    .collect(Collectors.toMap(InspectionParameter::getId, p -> p));

            // Pre-fetch reasons to a map (Eliminate N+1 query)
            Map<Long, InspectionReasonMaster> reasonMap = reasonRepository.findAll()
                    .stream()
                    .collect(Collectors.toMap(InspectionReasonMaster::getId, r -> r));

            // Pre-fetch existing active records for the batch/module (Eliminate N+1 query)
            List<InspectionTestResult> allExistingActive = resultRepository
                    .findByTestHeader_BatchIdAndModuleIdAndActiveTrue(dto.getBatchId(), dto.getModuleId());
            Map<Long, List<InspectionTestResult>> existingBySleeper = allExistingActive.stream()
                    .collect(Collectors.groupingBy(InspectionTestResult::getSleeperId));

            // Collections for bulk save
            List<InspectionTestResult> oldResultsToDeactivate = new ArrayList<>();
            List<InspectionTestResult> newResultsToSave = new ArrayList<>();
            List<InspectionParameterResult> newParameterResults = new ArrayList<>();

            // Process sleepers sequentially in memory
            for (SleeperInspectionDto sleeperDto : dto.getSleepers()) {
                // STEP 1 & 2: Get and deactivate old records
                List<InspectionTestResult> existing = existingBySleeper.getOrDefault(sleeperDto.getSleeperId(), Collections.emptyList());
                for (InspectionTestResult old : existing) {
                    old.setActive(false);
                    old.setUpdatedBy(dto.getCreatedBy());
                    old.setUpdatedDate(LocalDateTime.now());
                    oldResultsToDeactivate.add(old);
                }

                // STEP 3: Create new record
                InspectionTestResult result = new InspectionTestResult();
                result.setTestHeader(header);
                result.setSleeperId(sleeperDto.getSleeperId());
                result.setSleeperNo(sleeperDto.getSleeperNo());
                result.setResult(sleeperDto.getResult());
                result.setRejectionReason(sleeperDto.getRejectionReason());
                result.setModuleId(module.getId());
                result.setActive(true);
                newResultsToSave.add(result);

                // Process parameters
                if (sleeperDto.getParameters() != null) {
                    for (ParameterInspectionDto paramDto : sleeperDto.getParameters()) {
                        InspectionParameter parameter = parameterMap.get(paramDto.getParameterId());
                        if (parameter == null) continue; // Skip if parameter mapping fails

                        Long reasonId = null;
                        if (paramDto.getSubReasonId() != null) {
                            reasonId = paramDto.getSubReasonId();
                        } else if (paramDto.getMainReasonId() != null) {
                            reasonId = paramDto.getMainReasonId();
                        }

                        InspectionReasonMaster reason = null;
                        if (reasonId != null) {
                            reason = reasonMap.get(reasonId);
                            if (reason == null) {
                                throw new IllegalArgumentException("Reason not found for ID: " + reasonId);
                            }
                        }

                        InspectionParameterResult paramResult = new InspectionParameterResult();
                        paramResult.setTestResult(result);
                        paramResult.setParameter(parameter);
                        paramResult.setParameterResult(paramDto.getResult());
                        paramResult.setReasonMaster(reason);
                        
                        newParameterResults.add(paramResult);
                    }
                }
            }

            // STEP 4: Bulk save all processed data sequentially
            if (!oldResultsToDeactivate.isEmpty()) {
                String ids = oldResultsToDeactivate.stream()
                        .map(r -> String.valueOf(r.getId()))
                        .collect(Collectors.joining(","));
                String updateSql = "UPDATE inspection_test_result SET active = false, updated_by = ?, updated_date = ? WHERE id IN (" + ids + ")";
                jdbcTemplate.update(updateSql, dto.getCreatedBy(), LocalDateTime.now());
            }

            if (!newResultsToSave.isEmpty()) {
                StringBuilder sql = new StringBuilder("INSERT INTO inspection_test_result (test_header_id, sleeper_id, sleeper_no, result, rejection_reason, module_id, active) VALUES ");
                Object[] params = new Object[newResultsToSave.size() * 7];
                int pIdx = 0;
                for (int i = 0; i < newResultsToSave.size(); i++) {
                    if (i > 0) sql.append(", ");
                    sql.append("(?, ?, ?, ?, ?, ?, ?)");
                    InspectionTestResult r = newResultsToSave.get(i);
                    params[pIdx++] = r.getTestHeader().getId();
                    params[pIdx++] = r.getSleeperId();
                    params[pIdx++] = r.getSleeperNo();
                    params[pIdx++] = r.getResult();
                    params[pIdx++] = r.getRejectionReason();
                    params[pIdx++] = r.getModuleId();
                    params[pIdx++] = r.getActive();
                }

                KeyHolder keyHolder = new GeneratedKeyHolder();
                jdbcTemplate.update(connection -> {
                    PreparedStatement ps = connection.prepareStatement(sql.toString(), Statement.RETURN_GENERATED_KEYS);
                    for (int i = 0; i < params.length; i++) {
                        ps.setObject(i + 1, params[i]);
                    }
                    return ps;
                }, keyHolder);

                List<Map<String, Object>> keys = keyHolder.getKeyList();
                for (int i = 0; i < newResultsToSave.size(); i++) {
                    Map<String, Object> keyMap = keys.get(i);
                    Long generatedId = ((Number) keyMap.values().iterator().next()).longValue();
                    newResultsToSave.get(i).setId(generatedId);
                }
            }
            
            bulkInsertParameterResults(newParameterResults);

            checkAndUpdateModuleCompletion(dto.getBatchId(), dto.getModuleId(), dto.getSleeperType());

        } catch (Exception e) {
            throw new RuntimeException("Failed to save inspection data: " + e.getMessage(), e);
        }
    }

    private void checkAndUpdateModuleCompletion(Long batchId, Long moduleId,String sleeperT) {

        Long totalSleepers = productionSleeperRepository.countByBatchId(batchId);

        Long testedSleepers = resultRepository.countTestedSleepers(batchId, moduleId, sleeperT);

      //  String sleeperType = productionSleeperRepository.getSleeperTypeByBatch(batchId);

        String sleeperType = sleeperT;
        String batchNo = productionDeclarationRepository.getBatchNoById(batchId);

        Long demouldRejected =
                demouldingInspectionRepository.countDemouldingRejected(batchNo);

      //  double validSleepers = totalSleepers - demouldRejected;
      //  double testedPercentage = ((double) testedSleepers / totalSleepers) * 100;
        double validSleepers = totalSleepers - demouldRejected;

        double testedPercentage = 0;

        if (validSleepers > 0) {
            testedPercentage = (testedSleepers * 100.0) / validSleepers;
        }
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

            if("RT-8746".equalsIgnoreCase(sleeperType) && testedPercentage >= 10){
                completed = true;
            }

        }

        // MODULE 3 → NON CRITICAL
        if(moduleId == 3){

            if("RT-8521".equalsIgnoreCase(sleeperType) && testedPercentage >= 1){
                completed = true;
            }

            if("RT-8746".equalsIgnoreCase(sleeperType) && testedPercentage >= 1){
                completed = true;
            }

        }

        if (testedPercentage >= 99.99) {
            completed = true;
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

    @Transactional
    @Override
    public void updateInspection(InspectionSaveRequestDto dto) {

        InspectionModule module = moduleRepository
                .findById(dto.getModuleId())
                .orElseThrow();

        // Get existing header (no new header)
        InspectionTestHeader header =
                headerRepository.findTopByBatchIdAndModuleIdOrderByIdDesc(
                        dto.getBatchId(), dto.getModuleId()
                );

        if (header == null) {
            throw new RuntimeException("No existing inspection found for update");
        }

        // get ALL active results (any module, any status)
        List<InspectionTestResult> allResults =
                resultRepository.findByTestHeader_BatchIdAndActiveTrue(
                        dto.getBatchId()
                );

        //  map sleeperId → moduleId
        Map<Long, Long> sleeperModuleMap = allResults.stream()
                .collect(Collectors.toMap(
                        InspectionTestResult::getSleeperId,
                        InspectionTestResult::getModuleId,
                        (a, b) -> b
                ));

        //  Existing records for current module
        List<InspectionTestResult> existing =
                resultRepository.findByTestHeader_BatchIdAndModuleIdAndActiveTrue(
                        dto.getBatchId(), dto.getModuleId()
                );

        //  Incoming sleeperIds
        Set<Long> incomingIds = dto.getSleepers().stream()
                .map(SleeperInspectionDto::getSleeperId)
                .collect(Collectors.toSet());

        //  Deactivate only updating sleepers (NOT all)
        List<InspectionTestResult> toDeactivate = existing.stream()
                .filter(r -> incomingIds.contains(r.getSleeperId()))
                .toList();

        if (!toDeactivate.isEmpty()) {
            String ids = toDeactivate.stream()
                    .map(r -> String.valueOf(r.getId()))
                    .collect(Collectors.joining(","));
            String updateSql = "UPDATE inspection_test_result SET active = false, updated_by = ?, updated_date = ? WHERE id IN (" + ids + ")";
            jdbcTemplate.update(updateSql, dto.getCreatedBy(), LocalDateTime.now());
        }

        //  Parameter map
        Map<Long, InspectionParameter> parameterMap =
                parameterRepository.findAll()
                        .stream()
                        .collect(Collectors.toMap(InspectionParameter::getId, p -> p));

        List<InspectionParameterResult> parameterResults = new ArrayList<>();
        List<InspectionTestResult> newResultsToSave = new ArrayList<>();

        //  INSERT new records
        for (SleeperInspectionDto sleeperDto : dto.getSleepers()) {

            //  BLOCK if sleeper already exists in OTHER module
            Long existingModuleId = sleeperModuleMap.get(sleeperDto.getSleeperId());

            if (existingModuleId != null && !existingModuleId.equals(dto.getModuleId())) {
                throw new RuntimeException(
                        "Sleeper " + sleeperDto.getSleeperNo() +
                                " is already inspected in another module"
                );
            }

            //  Create new record
            InspectionTestResult result = new InspectionTestResult();

            result.setTestHeader(header);
            result.setSleeperId(sleeperDto.getSleeperId());
            result.setSleeperNo(sleeperDto.getSleeperNo());
            result.setModuleId(module.getId());
            result.setResult(sleeperDto.getResult());
            result.setRejectionReason(sleeperDto.getRejectionReason());
            result.setActive(true);

            newResultsToSave.add(result);

            //  parameters
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
        
        if (!newResultsToSave.isEmpty()) {
            StringBuilder sql = new StringBuilder("INSERT INTO inspection_test_result (test_header_id, sleeper_id, sleeper_no, result, rejection_reason, module_id, active) VALUES ");
            Object[] params = new Object[newResultsToSave.size() * 7];
            int pIdx = 0;
            for (int i = 0; i < newResultsToSave.size(); i++) {
                if (i > 0) sql.append(", ");
                sql.append("(?, ?, ?, ?, ?, ?, ?)");
                InspectionTestResult r = newResultsToSave.get(i);
                params[pIdx++] = r.getTestHeader().getId();
                params[pIdx++] = r.getSleeperId();
                params[pIdx++] = r.getSleeperNo();
                params[pIdx++] = r.getResult();
                params[pIdx++] = r.getRejectionReason();
                params[pIdx++] = r.getModuleId();
                params[pIdx++] = r.getActive();
            }

            KeyHolder keyHolder = new GeneratedKeyHolder();
            jdbcTemplate.update(connection -> {
                PreparedStatement ps = connection.prepareStatement(sql.toString(), Statement.RETURN_GENERATED_KEYS);
                for (int i = 0; i < params.length; i++) {
                    ps.setObject(i + 1, params[i]);
                }
                return ps;
            }, keyHolder);

            List<Map<String, Object>> keys = keyHolder.getKeyList();
            for (int i = 0; i < newResultsToSave.size(); i++) {
                Map<String, Object> keyMap = keys.get(i);
                Long generatedId = ((Number) keyMap.values().iterator().next()).longValue();
                newResultsToSave.get(i).setId(generatedId);
            }
        }

        bulkInsertParameterResults(parameterResults);

        //  Completion logic unchanged
        checkAndUpdateModuleCompletion(dto.getBatchId(), dto.getModuleId(), dto.getSleeperType());
    }

    private void bulkInsertParameterResults(List<InspectionParameterResult> parameterResults) {
        if (parameterResults == null || parameterResults.isEmpty()) {
            return;
        }

        int batchSize = 300; // Optimal chunk size to avoid packet limit
        for (int i = 0; i < parameterResults.size(); i += batchSize) {
            int end = Math.min(i + batchSize, parameterResults.size());
            List<InspectionParameterResult> chunk = parameterResults.subList(i, end);

            StringBuilder sql = new StringBuilder("INSERT INTO inspection_parameter_result (parameter_result, parameter_id, test_result_id, reason_master_id) VALUES ");
            Object[] params = new Object[chunk.size() * 4];
            int[] types = new int[chunk.size() * 4];

            int paramIndex = 0;
            for (int j = 0; j < chunk.size(); j++) {
                if (j > 0) sql.append(", ");
                sql.append("(?, ?, ?, ?)");

                InspectionParameterResult pr = chunk.get(j);
                
                params[paramIndex] = pr.getParameterResult();
                types[paramIndex++] = Types.VARCHAR;

                params[paramIndex] = pr.getParameter() != null ? pr.getParameter().getId() : null;
                types[paramIndex++] = Types.BIGINT;

                params[paramIndex] = pr.getTestResult() != null ? pr.getTestResult().getId() : null;
                types[paramIndex++] = Types.BIGINT;

                params[paramIndex] = pr.getReasonMaster() != null ? pr.getReasonMaster().getId() : null;
                types[paramIndex++] = Types.BIGINT;
            }

            jdbcTemplate.update(sql.toString(), params, types);
        }
    }
    /*
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
        } */
    @Override
  //  public List<BatchTestingListResponseDto> getAllBatchTesting(Long moduleId) {
    public List<BatchTestingListResponseDto> getAllBatchTesting(
            Long moduleId,
            String plantId){
        List<BatchTestingListResponseDto> list = new ArrayList<>();

        // STRESS (existing)
        list.addAll(productionDeclarationRepository.getAllBatchTesting(plantId));

        // LONG_LINE (new)
        list.addAll(productionDeclarationRepository.getLongLineBatchTesting(plantId));

        if (list.isEmpty()) {
            return list;
        }

        List<String> allBatchIdsStr = list.stream()
                .map(d -> String.valueOf(d.getBatchId()))
                .distinct()
                .collect(Collectors.toList());
                
        Set<String> completedBatchIds = new java.util.HashSet<>();
        
        for (int i = 0; i < allBatchIdsStr.size(); i += 1000) {
            List<String> chunk = allBatchIdsStr.subList(i, Math.min(i + 1000, allBatchIdsStr.size()));
            completedBatchIds.addAll(sleeperWorkflowRepository.findCompletedWorkflowsByRequestIds(chunk));
        }

        List<BatchTestingListResponseDto> workflowsCompletedList = list.stream()
                .filter(dto -> completedBatchIds.contains(String.valueOf(dto.getBatchId())))
                .collect(Collectors.toList());

        if (workflowsCompletedList.isEmpty()) {
            return workflowsCompletedList;
        }

        List<Long> batchIds = workflowsCompletedList.stream()
                .map(BatchTestingListResponseDto::getBatchId)
                .distinct()
                .collect(Collectors.toList());
                
        List<String> batchNumbers = workflowsCompletedList.stream()
                .map(BatchTestingListResponseDto::getBatchNumber)
                .distinct()
                .collect(Collectors.toList());

        Map<Long, Long> testedCounts = new java.util.HashMap<>();
        for (int i = 0; i < batchIds.size(); i += 1000) {
            List<Long> chunk = batchIds.subList(i, Math.min(i + 1000, batchIds.size()));
            List<Object[]> results = resultRepository.countTestedSleepersByBatchIds(chunk, moduleId);
            for (Object[] row : results) {
                testedCounts.put(((Number) row[0]).longValue(), ((Number) row[1]).longValue());
            }
        }

        Map<String, Long> demouldRejectedCounts = new java.util.HashMap<>();
        for (int i = 0; i < batchNumbers.size(); i += 1000) {
            List<String> chunk = batchNumbers.subList(i, Math.min(i + 1000, batchNumbers.size()));
            List<Object[]> results = demouldingInspectionRepository.countDemouldingRejectedByBatchNos(chunk);
            for (Object[] row : results) {
                demouldRejectedCounts.put((String) row[0], ((Number) row[1]).longValue());
            }
        }

        List<BatchTestingListResponseDto> filteredList = new ArrayList<>();

        for (BatchTestingListResponseDto dto : workflowsCompletedList) {

            Long testedCount = testedCounts.getOrDefault(dto.getBatchId(), 0L);
            Long demouldRejected = demouldRejectedCounts.getOrDefault(dto.getBatchNumber(), 0L);

            double denominator = dto.getNoOfSleepers() - demouldRejected;

            double percent = 0;

            if (denominator > 0) {
                percent = (testedCount * 100.0) / denominator;
            }

            dto.setTestedPercentage(percent);

         /*   if (percent == 0)
                dto.setTestingStatus("Pending");
            else if (percent == 100)
                dto.setTestingStatus("Completed");
            else
                dto.setTestingStatus("Under Inspection"); */

            boolean completed = false;

// MODULE 1 → VISUAL
            if (moduleId == 1) {

//                if (testedCount.equals(denominator)) {
//                    completed = true;
//                }

                if (percent >= 99.99) {
                    completed = true;
                }


            }

// MODULE 2 → CRITICAL DIMENSION
            if (moduleId == 2) {

                if ("RT-8521".equalsIgnoreCase(dto.getSleeperType()) && percent >= 10) {
                    completed = true;
                }

                if ("RT-8746".equalsIgnoreCase(dto.getSleeperType()) && percent >= 10) {
                    completed = true;
                }

            }

// MODULE 3 → NON CRITICAL
            if (moduleId == 3) {

                if ("RT-8521".equalsIgnoreCase(dto.getSleeperType()) && percent >= 1) {
                    completed = true;
                }

                if ("RT-8746".equalsIgnoreCase(dto.getSleeperType()) && percent >= 1) {
                    completed = true;
                }

            }

            if (percent >= 99.99) {
                completed = true;
            }


            if (percent == 0) {
                dto.setTestingStatus("Pending");
            } else if (completed) {
                dto.setTestingStatus("Completed");
            } else {
                dto.setTestingStatus("Under Inspection");
            }

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

    /*
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

     Map<Long, Long> moduleMap = results.stream()
             .filter(r -> r.getSleeperId() != null && r.getModuleId() != null)
             .collect(Collectors.toMap(
                     InspectionTestResult::getSleeperId,
                     InspectionTestResult::getModuleId,
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
                     sd.setModuleId(4L);
                 } else {
                     // Existing logic
                     String status = resultMap.get(s.getId());
                     sd.setStatus(status != null ? status : "PENDING");
                     sd.setModuleId(moduleMap.get(s.getId()));
                 }

                 return sd;

             }).toList();

     dto.setSleepers(sleeperDtos);

     return dto;
 }*/
    @Override
//    public BatchInspectionDetailDto getBatchInspection(Long batchId, Long moduleId) {
    public BatchInspectionDetailDto getBatchInspection(Long batchId, Long moduleId, String sleeperType){

        ProductionDeclaration declaration =
                productionDeclarationRepository.findBatchById(batchId);

        //  List<ProductionSleeper> sleepers = productionSleeperRepository.getSleepersByBatch(batchId);
        List<ProductionSleeper> sleepers;

      /*  if ("STRESS".equalsIgnoreCase(declaration.getPlantType())) {

            sleepers = productionSleeperRepository.getSleepersByBatch(batchId);

        } else { // LONG_LINE

            sleepers = productionSleeperRepository.getSleepersFromGang(batchId);
        }*/
        if ("STRESS".equalsIgnoreCase(declaration.getPlantType())) {

            sleepers = productionSleeperRepository
                    .getSleepersByBatchAndType(batchId, sleeperType);

        } else {

            sleepers = productionSleeperRepository
                    .getSleepersFromGangAndType(batchId, sleeperType);
        }

        // Fetch ALL inspection results
        //   List<InspectionTestResult> results =resultRepository.findByBatchId(batchId);

        //ONLY ACTIVE RECORDS
        List<InspectionTestResult> results =
                resultRepository.findByTestHeader_BatchIdAndActiveTrue(batchId);

        // Existing map (NO CHANGE)
        Map<Long, String> resultMap = results.stream()
                .collect(Collectors.toMap(
                        InspectionTestResult::getSleeperId,
                        InspectionTestResult::getResult,
                        (a, b) -> b
                ));

        // Existing module map (NO CHANGE)
        Map<Long, Long> moduleMap = results.stream()
                .filter(r -> r.getSleeperId() != null && r.getModuleId() != null)
                .collect(Collectors.toMap(
                        InspectionTestResult::getSleeperId,
                        InspectionTestResult::getModuleId,
                        (a, b) -> b
                ));

        //  rejected map (store moduleId)
        Map<Long, Long> rejectedMap = results.stream()
                .filter(r -> "REJECTED".equalsIgnoreCase(r.getResult()))
                .filter(r -> r.getSleeperId() != null && r.getModuleId() != null)
                .collect(Collectors.toMap(
                        InspectionTestResult::getSleeperId,
                        InspectionTestResult::getModuleId,
                        (a, b) -> b
                ));

        //   fetch only selected module results
        //   List<InspectionTestResult> moduleResults =resultRepository.findByTestHeader_BatchIdAndModuleId(batchId, moduleId);

        // ONLY ACTIVE RECORDS FOR MODULE
        List<InspectionTestResult> moduleResults =
                resultRepository.findByTestHeader_BatchIdAndModuleIdAndActiveTrue(batchId, moduleId);


        Map<Long, InspectionTestResult> moduleResultMap = moduleResults.stream()
                .collect(Collectors.toMap(
                        InspectionTestResult::getSleeperId,
                        r -> r,
                        (a, b) -> b
                ));

        // Demoulding rejected
        Set<String> rejectedSet =
                demouldingDefectiveSleeperRepository
                        .findRejectedSleeperNos(declaration.getBatchNumber());

        //  String sleeperType = productionSleeperRepository.getSleeperTypeByBatch(batchId);
       /* String sleeperType;

        if ("STRESS".equalsIgnoreCase(declaration.getPlantType())) {

            sleeperType = productionSleeperRepository.getSleeperTypeByBatch(batchId);

        } else {

            sleeperType = productionSleeperRepository.getLongLineSleeperType(batchId);
        }*/
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

                    //  DEMOULDING rejection
                    if (rejectedSet.contains(s.getSleeperNo())) {

                        sd.setStatus("REJECTED");
                        sd.setModuleId(4L);

                    }
                    // REJECTED in ANY inspection module
                    else if (rejectedMap.containsKey(s.getId())) {

                        sd.setStatus("REJECTED");
                        sd.setModuleId(rejectedMap.get(s.getId()));

                    }
                    // Selected module result
                    else {

                        InspectionTestResult result = moduleResultMap.get(s.getId());

                        if (result != null) {
                            sd.setStatus(result.getResult());
                            sd.setModuleId(result.getModuleId());
                        } else {
                            sd.setStatus("PENDING");
                            sd.setModuleId(moduleId);
                        }
                    }

                    return sd;

                }).toList();

        dto.setSleepers(sleeperDtos);

        return dto;
    }


    @Override
    public List<BatchInspectionResponseDto> getCompletedBatches(String sleeperType, String userId) {

        String parsedUserId = userId.replace(":", "");
        //  Long vendorId = Long.parseLong(parsedUserId);

        Optional<UserMaster> userOpt = userMasterRepository.findFirstByUserName(userId);
        if (userOpt.isEmpty()) {
            userOpt = userMasterRepository.findFirstByUserName(parsedUserId);
        }

        Long vendorId =0L;
        if (userOpt.isPresent()) {
            vendorId = userOpt.get().getUserId().longValue();
        }

        List<Long> batchIds = headerRepository.findCompletedBatchIdsBySleeperTypeAndUserId(sleeperType, vendorId);

        List<BatchInspectionResponseDto> responseList = new ArrayList<>();

        Set<Long> raisedSleeperIds = new HashSet<>();

        raisedSleeperIds.addAll(inspectionCallRepository.findAllGoodSleeperIds());
        raisedSleeperIds.addAll(inspectionCallRepository.findAllBadSleeperIds());

        for (Long batchId : batchIds) {

            List<InspectionTestResult> results =
                    resultRepository.findAllResultsByBatchId(batchId);

            Map<Long, List<InspectionTestResult>> grouped =
                    results.stream().collect(Collectors.groupingBy(InspectionTestResult::getSleeperId));

            List<SleeperDto> goodSleepers = new ArrayList<>();
            List<BadSleeperDto> badSleepers = new ArrayList<>();

            ProductionDeclaration declaration =
                    productionDeclarationRepository.findBatchById(batchId);

            List<EtSleeperDetails> etSleepersList =
                    etSleeperDetailsRepository.findByEt_BatchNumber(declaration.getBatchNumber());

            Set<Long> etSleeperIds = etSleepersList.stream()
                    .map(EtSleeperDetails::getSleeperId)
                    .collect(Collectors.toSet());

            for (Map.Entry<Long, List<InspectionTestResult>> entry : grouped.entrySet()) {

                List<InspectionTestResult> sleeperResults = entry.getValue();

                boolean isRejected = sleeperResults.stream()
                        .anyMatch(r -> "REJECTED".equalsIgnoreCase(r.getResult()));

                InspectionTestResult first = sleeperResults.get(0);

                if (isRejected) {

                    BadSleeperDto bad = new BadSleeperDto();
                    bad.setSleeperId(first.getSleeperId());
                    bad.setSleeperNo(first.getSleeperNo());
                    bad.setCallRaised(raisedSleeperIds.contains(first.getSleeperId()));

                    sleeperResults.stream()
                            .filter(r -> "REJECTED".equalsIgnoreCase(r.getResult()))
                            .findFirst()
                            .ifPresent(r -> bad.setReason(r.getRejectionReason()));

                    badSleepers.add(bad);

                } else {

                   // SleeperDto dto = new SleeperDto();
                   // dto.setSleeperId(first.getSleeperId());
                  //  dto.setSleeperNo(first.getSleeperNo());

                   // goodSleepers.add(dto);

                    SleeperDto dto = new SleeperDto();
                    dto.setSleeperId(first.getSleeperId());
                    dto.setSleeperNo(first.getSleeperNo());
                    dto.setCallRaised(raisedSleeperIds.contains(first.getSleeperId()));

                    if (etSleeperIds.contains(first.getSleeperId())) {
                        dto.setModuleId(5L);   // ET sleeper
                    }

                    goodSleepers.add(dto);
                }
            }

            BatchInspectionResponseDto response =
                    new BatchInspectionResponseDto();

          //  ProductionDeclaration declaration = productionDeclarationRepository.findBatchById(batchId);


//            response.setBatchId(batchId);
//            if (declaration != null) {
//                response.setBatchNumber(declaration.getBatchNumber());
//                response.setCastDate(declaration.getCastingDate() != null ? declaration.getCastingDate().toString() : "");
//            }


            if (declaration == null) continue;

            String batchNo = declaration.getBatchNumber();

            boolean steamDone = steamCubeSampleDeclarationRepository.existsSteamCube(batchNo);
            boolean demouldingDone = demouldingInspectionRepository.existsDemoulding(batchNo);
            boolean waterDone = waterCubeStrengthTestRepository.existsWaterCube(batchNo);
           // boolean modulusDone = modulusRepo.existsModulus(batchNo);

//  If any missing → skip batch
            if (!steamDone || !demouldingDone || !waterDone ) {
                continue;
            }


            response.setBatchId(batchId);
            response.setBatchNumber(batchNo);
            response.setCastDate(
                    declaration.getCastingDate() != null
                            ? declaration.getCastingDate().toString()
                            : ""
            );

            //  response.setTotalSleepers((long) results.size());
            response.setTotalSleepers((long) grouped.size());
            response.setGoodCount((long) goodSleepers.size());
            response.setBadCount((long) badSleepers.size());
            response.setGoodSleepers(goodSleepers);
            response.setBadSleepers(badSleepers);
            response.setPlantId(declaration.getPlantId());

            responseList.add(response);
        }

        return responseList;
    }

    @Override
    public List<String> getDistinctSleeperTypes(String userId) {
        String parsedUserId = userId.replace(":", "");

        Optional<UserMaster> userOpt = userMasterRepository.findFirstByUserName(userId);
        if (userOpt.isEmpty()) {
            userOpt = userMasterRepository.findFirstByUserName(parsedUserId);
        }

        Long vendorId = 0L;
        if (userOpt.isPresent()) {
            vendorId = userOpt.get().getUserId().longValue();
        }

        return headerRepository.findDistinctSleeperTypesByUserId(vendorId);
    }


    @Override
    // REMOVE moduleId (no longer needed)
    public BatchInspectionDetailDto getBatchaForET(Long batchId){

         ProductionDeclaration declaration =
                productionDeclarationRepository.findBatchById(batchId);

        //  List<ProductionSleeper> sleepers = productionSleeperRepository.getSleepersByBatch(batchId);
        List<ProductionSleeper> sleepers;

        if ("STRESS".equalsIgnoreCase(declaration.getPlantType())) {

            sleepers = productionSleeperRepository.getSleepersByBatch(batchId);

        } else { // LONG_LINE

            sleepers = productionSleeperRepository.getSleepersFromGang(batchId);
        }

        // Fetch ALL inspection results
        //   List<InspectionTestResult> results =resultRepository.findByBatchId(batchId);

        //ONLY ACTIVE RECORDS
        List<InspectionTestResult> results =
                resultRepository.findByTestHeader_BatchIdAndActiveTrue(batchId);

        Map<Long, String> resultMap = results.stream()
                .collect(Collectors.toMap(
                        InspectionTestResult::getSleeperId,
                        InspectionTestResult::getResult,
                        (a, b) -> b
                ));

        // Existing module map (NO CHANGE)
        Map<Long, Long> moduleMap = results.stream()
                .filter(r -> r.getSleeperId() != null && r.getModuleId() != null)
                .collect(Collectors.toMap(
                        InspectionTestResult::getSleeperId,
                        InspectionTestResult::getModuleId,
                        (a, b) -> b
                ));

        //  rejected map (store moduleId)
        Map<Long, Long> rejectedMap = results.stream()
                .filter(r -> "REJECTED".equalsIgnoreCase(r.getResult()))
                .filter(r -> r.getSleeperId() != null && r.getModuleId() != null)
                .collect(Collectors.toMap(
                        InspectionTestResult::getSleeperId,
                        InspectionTestResult::getModuleId,
                        (a, b) -> b
                ));

        //   fetch only selected module results
        //   List<InspectionTestResult> moduleResults =resultRepository.findByTestHeader_BatchIdAndModuleId(batchId, moduleId);

        // ONLY ACTIVE RECORDS FOR MODULE
      //  List<InspectionTestResult> moduleResults = resultRepository.findByTestHeader_BatchIdAndModuleIdAndActiveTrue(batchId, moduleId);
        List<InspectionTestResult> moduleResults =
                resultRepository.findByTestHeader_BatchIdAndModuleIdInAndActiveTrue(
                        batchId, List.of(1L, 2L, 3L)
                );

     /*   Map<Long, InspectionTestResult> moduleResultMap = moduleResults.stream()
                .collect(Collectors.toMap(
                        InspectionTestResult::getSleeperId,
                        r -> r,
                        (a, b) -> b
                ));*/

        Map<Long, List<InspectionTestResult>> moduleResultMap =
                moduleResults.stream()
                        .collect(Collectors.groupingBy(InspectionTestResult::getSleeperId));

        // Demoulding rejected
        Set<String> rejectedSet =
                demouldingDefectiveSleeperRepository
                        .findRejectedSleeperNos(declaration.getBatchNumber());

        //  String sleeperType = productionSleeperRepository.getSleeperTypeByBatch(batchId);
        List<String> sleeperTypes;

        if ("STRESS".equalsIgnoreCase(declaration.getPlantType())) {

            sleeperTypes = productionSleeperRepository.getSleeperTypeByBatch(batchId);

        } else {

            sleeperTypes = productionSleeperRepository.getLongLineSleeperType(batchId);
        }
        String sleeperType = String.join(", ", sleeperTypes);
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

                    //  DEMOULDING rejection
                    if (rejectedSet.contains(s.getSleeperNo())) {

                        sd.setStatus("REJECTED");
                        sd.setModuleId(4L);

                    }
                    // REJECTED in ANY inspection module
                    else if (rejectedMap.containsKey(s.getId())) {

                        sd.setStatus("REJECTED");
                        sd.setModuleId(rejectedMap.get(s.getId()));

                    }
                    // Selected module result
                    else {

                        List<InspectionTestResult> resultsList = moduleResultMap.get(s.getId());

                        if (resultsList != null) {


                            InspectionTestResult selected = resultsList.stream()
                                    .sorted(Comparator.comparing(InspectionTestResult::getModuleId).reversed())
                                    .findFirst()
                                    .orElse(null);

                            if (selected != null) {
                                sd.setStatus(selected.getResult());
                                sd.setModuleId(selected.getModuleId());
                            }

                        } else {
                            sd.setStatus("PENDING");
                            sd.setModuleId(null); // CHANGE: no moduleId now
                        }
                    }

                    return sd;

                }).toList();

        dto.setSleepers(sleeperDtos);

        return dto;
    }


}