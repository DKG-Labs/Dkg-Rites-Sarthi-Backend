package com.sarthi.Sleeper.service.Impl;

import com.sarthi.Sleeper.dto.BadSleeperDto;
import com.sarthi.Sleeper.dto.BatchInspectionResponseDto;
import com.sarthi.Sleeper.dto.FinalInspectionDtos.*;
import com.sarthi.Sleeper.entity.DemouldingInspection;
import com.sarthi.Sleeper.entity.DemouldingDefectiveSleeper;
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
    private MomentOfResistanceTestRepository momentOfResistanceTestRepository;

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

    private void checkAndUpdateModuleCompletion(Long batchId, Long moduleId, String sleeperT) {

        Long totalSleepers = 0L;
        if (sleeperT != null && !sleeperT.isBlank()) {
            totalSleepers = productionSleeperRepository.countByBatchIdAndType(batchId, sleeperT);
        }
        if (totalSleepers == null || totalSleepers == 0L) {
            totalSleepers = productionSleeperRepository.countByBatchId(batchId);
        }

        Long testedSleepers = resultRepository.countTestedSleepers(batchId, moduleId, sleeperT);
        if (testedSleepers == null) testedSleepers = 0L;

        String batchNo = productionDeclarationRepository.getBatchNoById(batchId);

        Long demouldRejected =
                demouldingInspectionRepository.countDemouldingRejected(batchNo);
        if (demouldRejected == null) demouldRejected = 0L;

        double validSleepers = (totalSleepers != null ? totalSleepers : 0L) - demouldRejected;

        double testedPercentage = 0;

        if (validSleepers > 0) {
            testedPercentage = (testedSleepers * 100.0) / validSleepers;
        }

        if (testedSleepers + demouldRejected >= (totalSleepers != null ? totalSleepers : 0L) || testedPercentage >= 99.5) {
            testedPercentage = 100.0;
        }

        boolean completed = false;

        // MODULE 1 → VISUAL
        if (moduleId == 1) {
            if (testedPercentage >= 99.5 || testedPercentage >= 100) {
                completed = true;
            }
        }

        // MODULE 2 → CRITICAL DIMENSION (10% sampling)
        if (moduleId == 2) {
            if (testedPercentage >= 10) {
                completed = true;
            }
        }

        // MODULE 3 → NON CRITICAL (1% sampling)
        if (moduleId == 3) {
            if (testedPercentage >= 1) {
                completed = true;
            }
        }

        if (testedPercentage >= 99.5) {
            completed = true;
        }

        if(completed){
            updateModuleStatus(batchId, moduleId);
        }
    }


    private void updateModuleStatus(Long batchId, Long moduleId){

        InspectionTestHeader header =
                headerRepository
                        .findTopByBatchIdAndModuleIdOrderByIdDesc(batchId, moduleId);

        if (header != null) {
            header.setStatus("Completed");
            headerRepository.save(header);
        }
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

            if (testedCount + demouldRejected >= dto.getNoOfSleepers() || percent >= 99.5) {
                percent = 100.0;
            }

            dto.setTestedPercentage(Math.min(percent, 100.0));

            boolean completed = false;

            // MODULE 1 → VISUAL
            if (moduleId == 1) {
                if (percent >= 99.5 || percent >= 100.0) {
                    completed = true;
                }
            }

            // MODULE 2 → CRITICAL DIMENSION (10% sampling)
            if (moduleId == 2) {
                if (percent >= 10) {
                    completed = true;
                }
            }

            // MODULE 3 → NON CRITICAL (1% sampling)
            if (moduleId == 3) {
                if (percent >= 1) {
                    completed = true;
                }
            }

            if (percent >= 99.5) {
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
        dto.setSleeperType(sleeperType);
        dto.setTotalSleepers((long) sleepers.size());

        String category = null;
        if ("STRESS".equalsIgnoreCase(declaration.getPlantType()) && declaration.getChambers() != null) {
            category = declaration.getChambers().stream()
                    .filter(c -> c.getBenchGroups() != null)
                    .flatMap(c -> c.getBenchGroups().stream())
                    .filter(b -> sleeperType != null && sleeperType.equalsIgnoreCase(b.getSleeperType()))
                    .map(b -> b.getSleeperCategory())
                    .findFirst()
                    .orElse(null);
        } else if (declaration.getGangs() != null) {
            category = declaration.getGangs().stream()
                    .filter(g -> sleeperType != null && sleeperType.equalsIgnoreCase(g.getSleeperType()))
                    .map(g -> g.getSleeperCategory())
                    .findFirst()
                    .orElse(null);
        }
        dto.setSleeperCategory(category);

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
        Optional<UserMaster> userOpt = userMasterRepository.findFirstByUserName(userId);
        if (userOpt.isEmpty()) {
            userOpt = userMasterRepository.findFirstByUserName(parsedUserId);
        }

        Long vendorId = 0L;
        if (userOpt.isPresent()) {
            vendorId = userOpt.get().getUserId().longValue();
        }

        List<Long> batchIds = headerRepository.findCompletedBatchIdsBySleeperTypeAndUserId(sleeperType, vendorId);
        if (batchIds == null || batchIds.isEmpty()) {
            return Collections.emptyList();
        }

        // ── Bulk Upfront Fetch 1: Declarations ───────────────────────────────
        List<ProductionDeclaration> declarations = productionDeclarationRepository.findByIdIn(batchIds);
        if (declarations == null || declarations.isEmpty()) {
            return Collections.emptyList();
        }

        Map<Long, ProductionDeclaration> declMap = declarations.stream()
                .collect(Collectors.toMap(ProductionDeclaration::getId, d -> d, (a, b) -> a));

        Set<String> batchNumbers = declarations.stream()
                .map(ProductionDeclaration::getBatchNumber)
                .filter(Objects::nonNull)
                .map(String::trim)
                .collect(Collectors.toSet());

        if (batchNumbers.isEmpty()) {
            return Collections.emptyList();
        }

        // ── Bulk Upfront Fetch 2: Raised Sleeper & Batch IDs ─────────────────
        Set<Long> raisedSleeperIds = new HashSet<>();
        raisedSleeperIds.addAll(inspectionCallRepository.findAllGoodSleeperIds());
        raisedSleeperIds.addAll(inspectionCallRepository.findAllBadSleeperIds());

        Set<String> raisedBadSleeperKeys = new HashSet<>(inspectionCallRepository.findAllRaisedBadSleeperKeys());
        Set<String> raisedBadBatchNos = new HashSet<>(inspectionCallRepository.findAllRaisedBadBatchNos());

        // ── Bulk Upfront Fetch 3: Passed Lab Tests (Water Cube & MOR) ────────
        Set<String> passedWaterCubeBatchNos = new HashSet<>(waterCubeStrengthTestRepository.findAllPassedBatchNumbers());
        Set<String> passedMORBatchNos = new HashSet<>(momentOfResistanceTestRepository.findAllPassedBatchNumbers());

        // ── Bulk Upfront Fetch 4: Inspection Test Results for all batches ────
        List<InspectionTestResult> allResults = resultRepository.findAllResultsByBatchIds(batchIds);
        Map<Long, List<InspectionTestResult>> resultsByBatchId = (allResults != null)
                ? allResults.stream()
                .filter(r -> r.getTestHeader() != null && r.getTestHeader().getBatchId() != null)
                .collect(Collectors.groupingBy(r -> r.getTestHeader().getBatchId()))
                : Collections.emptyMap();

        // ── Bulk Upfront Fetch 5: ET Sleeper Details for all batch numbers ───
        List<EtSleeperDetails> allEtDetails = etSleeperDetailsRepository.findByEt_BatchNumberIn(batchNumbers);
        Map<String, Set<Long>> etSleeperIdsByBatchNo = (allEtDetails != null)
                ? allEtDetails.stream()
                .filter(e -> e.getEt() != null && e.getEt().getBatchNumber() != null && e.getSleeperId() != null)
                .collect(Collectors.groupingBy(
                        e -> e.getEt().getBatchNumber().trim(),
                        Collectors.mapping(EtSleeperDetails::getSleeperId, Collectors.toSet())
                ))
                : Collections.emptyMap();

        // ── Bulk Upfront Fetch 6: Demoulding Inspections with Defects ────────
        List<DemouldingInspection> allDemouldings = demouldingInspectionRepository.findByBatchNoInWithDefects(batchNumbers);
        Map<String, List<DemouldingInspection>> demouldingsByBatchNo = (allDemouldings != null)
                ? allDemouldings.stream()
                .filter(d -> d.getBatchNo() != null)
                .collect(Collectors.groupingBy(d -> d.getBatchNo().trim()))
                : Collections.emptyMap();

        // ── Bulk Upfront Fetch 7: Production Sleepers for all batches ────────
        List<Object[]> allProdSleeperRows = productionSleeperRepository.getSleepersWithBatchIdByBatchIds(batchIds);
        Map<Long, List<ProductionSleeper>> prodSleepersByBatchId = new HashMap<>();
        if (allProdSleeperRows != null) {
            for (Object[] row : allProdSleeperRows) {
                if (row != null && row.length >= 2 && row[0] instanceof ProductionSleeper && row[1] != null) {
                    ProductionSleeper ps = (ProductionSleeper) row[0];
                    Long bId = (row[1] instanceof Number)
                            ? ((Number) row[1]).longValue()
                            : Long.parseLong(row[1].toString().trim());
                    prodSleepersByBatchId.computeIfAbsent(bId, k -> new ArrayList<>()).add(ps);
                }
            }
        }

        List<BatchInspectionResponseDto> responseList = new ArrayList<>();

        // ── In-Memory Processing Loop (ZERO DB QUERIES INSIDE) ───────────────
        for (Long batchId : batchIds) {

            ProductionDeclaration declaration = declMap.get(batchId);
            if (declaration == null || declaration.getBatchNumber() == null) {
                continue;
            }

            String currentBatchNo = declaration.getBatchNumber().trim();

            // Lab test verification using in-memory pre-fetched sets
            boolean passedWaterCube = isBatchLabPassed(currentBatchNo, passedWaterCubeBatchNos);
            boolean passedMOR = isBatchLabPassed(currentBatchNo, passedMORBatchNos);

            if (!passedWaterCube || !passedMOR) {
                continue;
            }

            List<InspectionTestResult> results = resultsByBatchId.getOrDefault(batchId, Collections.emptyList());
            Map<Long, List<InspectionTestResult>> grouped = results.stream()
                    .collect(Collectors.groupingBy(InspectionTestResult::getSleeperId));

            List<SleeperDto> goodSleepers = new ArrayList<>();
            List<BadSleeperDto> badSleepers = new ArrayList<>();

            boolean isBatchBadAlreadyRaised = raisedBadBatchNos.contains(currentBatchNo);
            Set<Long> etSleeperIds = etSleeperIdsByBatchNo.getOrDefault(currentBatchNo, Collections.emptySet());

            for (Map.Entry<Long, List<InspectionTestResult>> entry : grouped.entrySet()) {
                List<InspectionTestResult> sleeperResults = entry.getValue();

                boolean isRejected = sleeperResults.stream()
                        .anyMatch(r -> "REJECTED".equalsIgnoreCase(r.getResult()));

                InspectionTestResult first = sleeperResults.get(0);

                if (isRejected) {
                    BadSleeperDto bad = new BadSleeperDto();
                    bad.setSleeperId(first.getSleeperId());
                    bad.setSleeperNo(first.getSleeperNo());

                    String badKey = (first.getSleeperNo() != null)
                            ? (currentBatchNo + "_" + first.getSleeperNo().trim()) : "";

                    boolean isRaised = raisedSleeperIds.contains(first.getSleeperId())
                            || (!badKey.isEmpty() && raisedBadSleeperKeys.contains(badKey))
                            || isBatchBadAlreadyRaised;

                    bad.setCallRaised(isRaised);

                    Optional<InspectionTestResult> rejOpt = sleeperResults.stream()
                            .filter(r -> "REJECTED".equalsIgnoreCase(r.getResult()))
                            .findFirst();

                    if (rejOpt.isPresent()) {
                        InspectionTestResult r = rejOpt.get();
                        bad.setReason(r.getRejectionReason());
                        Long modId = r.getModuleId();
                        if (modId == null && r.getTestHeader() != null && r.getTestHeader().getModule() != null) {
                            modId = r.getTestHeader().getModule().getId();
                        }
                        bad.setModuleId(modId);
                        if (modId != null) {
                            if (modId == 1L) bad.setModuleName("Visual");
                            else if (modId == 2L) bad.setModuleName("Critical Dim");
                            else if (modId == 3L) bad.setModuleName("Non-Critical Dim");
                            else bad.setModuleName("Module " + modId);
                        } else {
                            bad.setModuleName("Inspection");
                        }
                    }

                    badSleepers.add(bad);

                } else {
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

            // Production sleepers from pre-fetched map
            List<ProductionSleeper> allProdSleepers = prodSleepersByBatchId.getOrDefault(batchId, Collections.emptyList());

            // Process Demoulding Defective Sleepers from pre-fetched map
            List<DemouldingInspection> demouldings = demouldingsByBatchNo.getOrDefault(currentBatchNo, Collections.emptyList());
            for (DemouldingInspection di : demouldings) {
                if (di.getDefectiveSleepers() != null) {
                    for (DemouldingDefectiveSleeper dds : di.getDefectiveSleepers()) {
                        String visReason = dds.getVisualReason() != null ? dds.getVisualReason().trim() : "";
                        String dimReason = dds.getDimReason() != null ? dds.getDimReason().trim() : "";
                        
                        // Only count as defective if visual or dimensional defect reason exists
                        if (visReason.isEmpty() && dimReason.isEmpty()) {
                            continue;
                        }

                        String resolvedNo = dds.getSleeperNo() != null ? dds.getSleeperNo().trim() : "";
                        if (resolvedNo.isBlank()) {
                            String bNo = dds.getBenchGangNo() != null ? dds.getBenchGangNo().trim() : "";
                            String sNo = dds.getSequenceNo() != null ? dds.getSequenceNo().trim() : "";
                            resolvedNo = bNo + sNo;
                        }
                        final String rawSleeperNo = resolvedNo;

                        if (!rawSleeperNo.isBlank()) {
                            // Match against production sleepers
                            Optional<ProductionSleeper> prodMatch = allProdSleepers.stream()
                                    .filter(p -> isSleeperMatch(p.getSleeperNo(), rawSleeperNo, currentBatchNo))
                                    .findFirst();

                            // Match against already-tested good sleepers
                            Optional<SleeperDto> goodMatch = goodSleepers.stream()
                                    .filter(g -> isSleeperMatch(g.getSleeperNo(), rawSleeperNo, currentBatchNo))
                                    .findFirst();

                            // If it does not match any real sleeper in this batch (e.g. only bench/gang number without sleeper letter), ignore it
                            if (prodMatch.isEmpty() && goodMatch.isEmpty()) {
                                continue;
                            }

                            String actualSleeperNo = prodMatch.isPresent() && prodMatch.get().getSleeperNo() != null
                                    ? prodMatch.get().getSleeperNo()
                                    : (goodMatch.isPresent() && goodMatch.get().getSleeperNo() != null
                                            ? goodMatch.get().getSleeperNo()
                                            : rawSleeperNo);

                            boolean alreadyInBad = badSleepers.stream().anyMatch(b ->
                                    isSleeperMatch(b.getSleeperNo(), actualSleeperNo, currentBatchNo));
                            if (!alreadyInBad) {
                                BadSleeperDto bad = new BadSleeperDto();
                                bad.setReason(!visReason.isEmpty() ? visReason : dimReason);
                                bad.setModuleId(4L);
                                bad.setModuleName("Demoulding");

                                if (prodMatch.isPresent()) {
                                    ProductionSleeper matchedPs = prodMatch.get();
                                    bad.setSleeperId(matchedPs.getId());
                                    bad.setSleeperNo(matchedPs.getSleeperNo() != null ? matchedPs.getSleeperNo() : rawSleeperNo);
                                } else {
                                    bad.setSleeperId(goodMatch.get().getSleeperId());
                                    bad.setSleeperNo(goodMatch.get().getSleeperNo() != null ? goodMatch.get().getSleeperNo() : rawSleeperNo);
                                }

                                // Remove from goodSleepers if present
                                if (goodMatch.isPresent()) {
                                    goodSleepers.remove(goodMatch.get());
                                } else {
                                    goodSleepers.removeIf(g -> isSleeperMatch(g.getSleeperNo(), bad.getSleeperNo(), currentBatchNo)
                                            || (bad.getSleeperId() != null && bad.getSleeperId() != 0L && Objects.equals(g.getSleeperId(), bad.getSleeperId())));
                                }

                                String badKey = (bad.getSleeperNo() != null)
                                        ? (currentBatchNo + "_" + bad.getSleeperNo().trim()) : "";

                                boolean isRaised = (bad.getSleeperId() != null && bad.getSleeperId() != 0L && raisedSleeperIds.contains(bad.getSleeperId()))
                                        || (!badKey.isEmpty() && raisedBadSleeperKeys.contains(badKey))
                                        || isBatchBadAlreadyRaised;
                                bad.setCallRaised(isRaised);

                                badSleepers.add(bad);
                            }
                        }
                        }
                    }
                }

            // Uninspected Sleeper Recovery using in-memory list
            Set<Long> accountedBadIds = badSleepers.stream()
                    .map(BadSleeperDto::getSleeperId)
                    .filter(id -> id != null && id != 0L)
                    .collect(Collectors.toSet());

            Set<Long> accountedGoodIds = goodSleepers.stream()
                    .map(SleeperDto::getSleeperId)
                    .filter(id -> id != null && id != 0L)
                    .collect(Collectors.toSet());

            for (ProductionSleeper ps : allProdSleepers) {
                boolean isBad = (ps.getId() != null && accountedBadIds.contains(ps.getId()))
                        || badSleepers.stream().anyMatch(b -> isSleeperMatch(b.getSleeperNo(), ps.getSleeperNo(), currentBatchNo));

                if (isBad) {
                    continue;
                }

                boolean isAlreadyGood = (ps.getId() != null && accountedGoodIds.contains(ps.getId()))
                        || goodSleepers.stream().anyMatch(g -> isSleeperMatch(g.getSleeperNo(), ps.getSleeperNo(), currentBatchNo));

                if (!isAlreadyGood) {
                    SleeperDto dto = new SleeperDto();
                    dto.setSleeperId(ps.getId());
                    dto.setSleeperNo(ps.getSleeperNo() != null ? ps.getSleeperNo() : "");
                    dto.setCallRaised(ps.getId() != null && raisedSleeperIds.contains(ps.getId()));
                    if (etSleeperIds.contains(ps.getId())) {
                        dto.setModuleId(5L);
                    }
                    goodSleepers.add(dto);
                }
            }

            // Ensure bad sleepers are strictly excluded from goodSleepers
            goodSleepers.removeIf(g -> badSleepers.stream().anyMatch(b ->
                    (b.getSleeperId() != null && b.getSleeperId() != 0L && Objects.equals(b.getSleeperId(), g.getSleeperId()))
                            || isSleeperMatch(b.getSleeperNo(), g.getSleeperNo(), currentBatchNo)
            ));

            BatchInspectionResponseDto response = new BatchInspectionResponseDto();
            response.setBatchId(batchId);
            response.setBatchNumber(currentBatchNo);
            response.setCastDate(
                    declaration.getCastingDate() != null
                            ? declaration.getCastingDate().toString()
                            : ""
            );

            long effectiveTotalCasted = (declaration.getTotalCastedSleepers() != null && declaration.getTotalCastedSleepers() > 0)
                    ? declaration.getTotalCastedSleepers()
                    : (!allProdSleepers.isEmpty() ? (long) allProdSleepers.size() : (long) grouped.size());

            // Strict invariant: goodSleepers + badSleepers cannot exceed effectiveTotalCasted
            if (effectiveTotalCasted > 0 && (goodSleepers.size() + badSleepers.size()) > effectiveTotalCasted) {
                int maxGoodAllowed = Math.max(0, (int) effectiveTotalCasted - badSleepers.size());
                if (goodSleepers.size() > maxGoodAllowed) {
                    goodSleepers = new ArrayList<>(goodSleepers.subList(0, maxGoodAllowed));
                }
            } else if (effectiveTotalCasted > 0 && (goodSleepers.size() + badSleepers.size()) < effectiveTotalCasted) {
                // Auto-include uninspected sleepers as good sleepers up to effectiveTotalCasted (Option B)
                int missingCount = (int) effectiveTotalCasted - (goodSleepers.size() + badSleepers.size());
                Set<String> existingNumbers = new HashSet<>();
                goodSleepers.forEach(g -> { if (g.getSleeperNo() != null) existingNumbers.add(g.getSleeperNo().trim()); });
                badSleepers.forEach(b -> { if (b.getSleeperNo() != null) existingNumbers.add(b.getSleeperNo().trim()); });

                int seq = 1;
                while (missingCount > 0 && seq <= (effectiveTotalCasted + 1000)) {
                    String candidateNo = String.valueOf(seq);
                    final String sSeq = candidateNo;
                    boolean exists = existingNumbers.contains(candidateNo)
                            || goodSleepers.stream().anyMatch(g -> isSleeperMatch(g.getSleeperNo(), sSeq, currentBatchNo))
                            || badSleepers.stream().anyMatch(b -> isSleeperMatch(b.getSleeperNo(), sSeq, currentBatchNo));

                    if (!exists) {
                        SleeperDto uninspectedGood = new SleeperDto();
                        uninspectedGood.setSleeperId(0L);
                        uninspectedGood.setSleeperNo(candidateNo);
                        uninspectedGood.setCallRaised(false);
                        goodSleepers.add(uninspectedGood);
                        existingNumbers.add(candidateNo);
                        missingCount--;
                    }
                    seq++;
                }
            }

            response.setTotalSleepers(effectiveTotalCasted > 0 ? effectiveTotalCasted : (long) (goodSleepers.size() + badSleepers.size()));
            response.setGoodCount((long) goodSleepers.size());
            response.setBadCount((long) badSleepers.size());
            response.setGoodSleepers(goodSleepers);
            response.setBadSleepers(badSleepers);
            response.setPlantId(declaration.getPlantId());

            responseList.add(response);
        }

        return responseList;
    }

    private boolean isBatchLabPassed(String batchNo, Set<String> passedBatchNos) {
        if (batchNo == null || passedBatchNos == null) return false;
        String b = batchNo.trim();
        if (passedBatchNos.contains(b)) return true;
        if (b.startsWith("B-") && passedBatchNos.contains(b.substring(2).trim())) return true;
        if (!b.startsWith("B-") && passedBatchNos.contains("B-" + b)) return true;
        return false;
    }

    private boolean isSleeperMatch(String no1, String no2, String batchNo) {
        if (no1 == null || no2 == null) return false;
        String s1 = no1.trim().toUpperCase();
        String s2 = no2.trim().toUpperCase();
        if (s1.isEmpty() || s2.isEmpty()) return false;
        if (s1.equals(s2)) return true;

        String bPrefix = (batchNo != null) ? batchNo.trim().toUpperCase() : "";
        String stripped1 = s1;
        String stripped2 = s2;
        if (!bPrefix.isEmpty()) {
            if (stripped1.startsWith(bPrefix + "/") || stripped1.startsWith(bPrefix + "-") || stripped1.startsWith(bPrefix + "_")) {
                stripped1 = stripped1.substring(bPrefix.length() + 1).trim();
            }
            if (stripped2.startsWith(bPrefix + "/") || stripped2.startsWith(bPrefix + "-") || stripped2.startsWith(bPrefix + "_")) {
                stripped2 = stripped2.substring(bPrefix.length() + 1).trim();
            }
        }
        if (!stripped1.isEmpty() && stripped1.equals(stripped2)) return true;

        if (s1.endsWith("/" + s2) || s1.endsWith("-" + s2) || s1.endsWith("_" + s2)) return true;
        if (s2.endsWith("/" + s1) || s2.endsWith("-" + s1) || s2.endsWith("_" + s1)) return true;

        // Numeric match only if BOTH are purely numeric (e.g. "01" and "1", never "1A" and "1Z")
        if (stripped1.matches("\\d+") && stripped2.matches("\\d+")) {
            try {
                return Long.parseLong(stripped1) == Long.parseLong(stripped2);
            } catch (Exception ignored) {}
        }

        return false;
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