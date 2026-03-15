package com.sarthi.Sleeper.service.Impl;

import com.sarthi.Sleeper.dto.FinalInspectionDtos.*;
import com.sarthi.Sleeper.entity.FinalInspection.*;
import com.sarthi.Sleeper.entity.ProductionDeclaration.ProductionDeclaration;
import com.sarthi.Sleeper.entity.ProductionDeclaration.ProductionSleeper;
import com.sarthi.Sleeper.repository.FinalInspectionRepository.*;
import com.sarthi.Sleeper.repository.ProductionDeclaration.ProductionBenchGroupRepository;
import com.sarthi.Sleeper.repository.ProductionDeclaration.ProductionDeclarationRepository;
import com.sarthi.Sleeper.repository.ProductionDeclaration.ProductionSleeperRepository;
import com.sarthi.Sleeper.service.ProductionFinalInspectionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ProductionFinalInspectionServiceImpl implements ProductionFinalInspectionService {


    @Autowired
    private InspectionModuleRepository moduleRepository;

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

    @Override
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
    }

    @Override
    public List<BatchTestingListResponseDto> getAllBatchTesting() {

        List<BatchTestingListResponseDto> list =
                productionDeclarationRepository.getAllBatchTesting();

        for (BatchTestingListResponseDto dto : list) {

            Long testedCount =
                    resultRepository.countTestedSleepers(dto.getBatchId());

            double percent =
                    (testedCount * 100.0) / dto.getNoOfSleepers();

            dto.setTestedPercentage(percent);

            if (percent == 0)
                dto.setTestingStatus("Pending");
            else if (percent == 100)
                dto.setTestingStatus("Completed");
            else
                dto.setTestingStatus("Under Inspection");
        }

        return list;
    }

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
    }
}
