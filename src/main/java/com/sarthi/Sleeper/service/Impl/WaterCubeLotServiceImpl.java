package com.sarthi.Sleeper.service.Impl;

import com.sarthi.Sleeper.dto.FinalInspectionDtos.WaterCubeStrengthDetailRequestDto;
import com.sarthi.Sleeper.dto.FinalInspectionDtos.WaterCubeStrengthTestRequestDto;
import com.sarthi.Sleeper.entity.FinalInspection.SleeperInspectionCall;
import com.sarthi.Sleeper.entity.FinalInspection.WaterCubeStrengthTest;
import com.sarthi.Sleeper.entity.ProductionDeclaration.LotRejectionTestType;
import com.sarthi.Sleeper.entity.ProductionDeclaration.LotStatus;
import com.sarthi.Sleeper.entity.ProductionDeclaration.ProductionDeclaration;
import com.sarthi.Sleeper.repository.FinalInspectionRepository.SleeperInspectionCallRepository;
import com.sarthi.Sleeper.repository.ProductionDeclaration.ProductionDeclarationRepository;
import com.sarthi.Sleeper.service.WaterCubeLotService;
import com.sarthi.config.WaterCubeAcceptanceProperties;
import com.sarthi.constant.AppConstant;
import com.sarthi.exception.BusinessException;
import com.sarthi.exception.ErrorDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class WaterCubeLotServiceImpl implements WaterCubeLotService {

    /** Numeric part of a concrete grade such as "M55" or "M 60". */
    private static final Pattern GRADE_STRENGTH = Pattern.compile("(\\d+(?:\\.\\d+)?)");

    /** Keeps IN (...) lists within a safe size. */
    private static final int ID_CHUNK = 1000;

    @Autowired
    private WaterCubeAcceptanceProperties rules;

    @Autowired
    private ProductionDeclarationRepository productionDeclarationRepository;

    @Autowired
    private SleeperInspectionCallRepository inspectionCallRepository;

    // ================= EVALUATE =================

    @Override
    public Evaluation evaluate(WaterCubeStrengthTestRequestDto dto) {
        double fck = resolveFck(dto);
        int sampleCount = rules.getSampleCount();
        int cubesPerSample = rules.getCubesPerSample();

        List<List<Double>> samples = new ArrayList<>();
        for (int i = 0; i < sampleCount; i++) {
            samples.add(new ArrayList<>());
        }
        int validCubes = 0;
        if (dto.getDetails() != null) {
            for (WaterCubeStrengthDetailRequestDto d : dto.getDetails()) {
                if (d == null || d.getSampleNumber() == null || d.getStrengthNmm2() == null || d.getStrengthNmm2() <= 0) {
                    continue;
                }
                int idx = d.getSampleNumber() - 1;
                if (idx < 0 || idx >= sampleCount) {
                    continue;
                }
                samples.get(idx).add(d.getStrengthNmm2());
                validCubes++;
            }
        }
        if (validCubes != rules.requiredCubeCount()) {
            throw validation("All " + rules.requiredCubeCount() + " cube strengths (" + sampleCount + " samples x "
                    + cubesPerSample + " cubes) are required to evaluate the water cube test.");
        }

        List<Double> averages = new ArrayList<>();
        List<Double> variations = new ArrayList<>();
        boolean excessiveVariation = false;
        for (List<Double> cubes : samples) {
            double avg = cubes.isEmpty() ? 0 : cubes.stream().mapToDouble(Double::doubleValue).average().orElse(0);
            double variation = maxVariationPercent(cubes, avg, cubesPerSample);
            averages.add(avg);
            variations.add(variation);
            if (cubes.size() == cubesPerSample && variation > rules.getVariationLimitPercent()) {
                excessiveVariation = true;
            }
        }

        boolean allSamplesPresent = averages.stream().allMatch(a -> a > 0);
        double x = allSamplesPresent ? averages.stream().mapToDouble(Double::doubleValue).average().orElse(0) : 0;
        double y = allSamplesPresent ? averages.stream().mapToDouble(Double::doubleValue).min().orElse(0) : 0;

        double c1X = rules.getCond1XMargin();
        double c1Y = rules.getCond1YMargin();
        double c2Y = rules.getCond2YMargin();

        boolean condition1 = x >= fck + c1X && y >= fck - c1Y;
        boolean condition2 = !condition1 && x >= fck && y >= fck - c2Y
                && ((x < fck + c1X && x >= fck) || (y < fck - c1Y && y >= fck - c2Y));
        boolean condition3 = !condition1 && !condition2 && (x < fck || y < fck - c2Y);

        boolean failed;
        int mrSamples;
        if (excessiveVariation) {
            failed = true;
            mrSamples = 0;
        } else if (condition1) {
            failed = false;
            mrSamples = rules.getMrSamplesCond1();
        } else if (condition2) {
            failed = false;
            mrSamples = rules.getMrSamplesCond2();
        } else {
            // condition 3 (or any case not accepted above) fails the lot
            failed = true;
            mrSamples = 0;
        }

        return new Evaluation(
                fck,
                sampleValue(averages, 0), sampleValue(averages, 1),
                x, y,
                sampleValue(variations, 0), sampleValue(variations, 1),
                condition1, condition2, condition3,
                mrSamples,
                failed ? rules.getFailResult() : rules.getPassResult(),
                failed);
    }

    private double resolveFck(WaterCubeStrengthTestRequestDto dto) {
        if (dto.getFckTarget() != null && dto.getFckTarget() > 0) {
            return dto.getFckTarget();
        }
        if (dto.getConcreteGrade() != null) {
            Matcher m = GRADE_STRENGTH.matcher(dto.getConcreteGrade());
            if (m.find()) {
                double parsed = Double.parseDouble(m.group(1));
                if (parsed > 0) {
                    return parsed;
                }
            }
        }
        throw validation("Characteristic strength (Fck) could not be determined: send fckTarget or a concrete grade such as M55.");
    }

    /** Largest deviation (%) of a cube from its sample average; 0 until the sample is complete. */
    private static double maxVariationPercent(List<Double> cubes, double avg, int cubesPerSample) {
        if (cubes.size() < cubesPerSample || avg == 0) {
            return 0;
        }
        return cubes.stream().mapToDouble(s -> Math.abs((s - avg) / avg) * 100).max().orElse(0);
    }

    private static Double sampleValue(List<Double> values, int index) {
        return index < values.size() ? values.get(index) : null;
    }

    // ================= REJECT =================

    @Override
    public Rejection rejectLot(WaterCubeStrengthTest test, Long fallbackProductionDeclarationId, Long userId) {
        Long pdId = test.getWaterCubeSampleDeclaration() != null
                ? test.getWaterCubeSampleDeclaration().getProductionDeclarationId()
                : null;
        if (pdId == null) {
            pdId = fallbackProductionDeclarationId;
        }
        if (pdId == null) {
            throw validation("Water cube test is not linked to a production batch, so the lot cannot be rejected.");
        }
        final Long batchId = pdId;
        ProductionDeclaration pd = productionDeclarationRepository.findById(batchId)
                .orElseThrow(() -> validation("Production batch " + batchId + " not found for the failed water cube test."));

        return reject(pd, rules.getRejectionReason(), LotRejectionTestType.WATER_CUBE, test.getId(), userId);
    }

    @Override
    public Rejection rejectBatch(Long productionDeclarationId, String reason, LotRejectionTestType testType,
                                 Long testId, Long userId) {
        if (productionDeclarationId == null) {
            throw validation("The failed test is not linked to a production batch, so the lot cannot be rejected.");
        }
        final Long batchId = productionDeclarationId;
        ProductionDeclaration pd = productionDeclarationRepository.findById(batchId)
                .orElseThrow(() -> validation("Production batch " + batchId + " not found for the failed test."));

        return reject(pd, reason, testType, testId, userId);
    }

    private Rejection reject(ProductionDeclaration pd, String reason, LotRejectionTestType testType,
                             Long testId, Long userId) {
        // An already rejected batch keeps its original rejection (and rejecting test)
        if (!isRejected(pd)) {
            pd.setLotStatus(LotStatus.REJECTED);
            pd.setLotRejectionReason(reason);
            pd.setLotRejectedAt(LocalDateTime.now());
            pd.setLotRejectedBy(userId);
            pd.setLotRejectedByTestId(testId);
            pd.setLotRejectedByTestType(testType);
            productionDeclarationRepository.save(pd);
        }

        return new Rejection(pd.getId(), pd.getBatchNumber(), findAffectedCallNos(pd));
    }

    /** Calls of the batch's plant that offered it and are not cancelled / withdrawn / rejected. */
    private List<String> findAffectedCallNos(ProductionDeclaration pd) {
        if (isBlank(pd.getBatchNumber()) || isBlank(pd.getPlantId())) {
            return Collections.emptyList();
        }
        List<String> callNos = inspectionCallRepository
                .findCallNosByBatchNoAndPlant(pd.getBatchNumber().trim(), pd.getPlantId().trim());
        if (callNos == null || callNos.isEmpty()) {
            return Collections.emptyList();
        }
        Set<String> closed = rules.getClosedCallStatuses().stream()
                .filter(Objects::nonNull)
                .map(s -> s.trim().toUpperCase())
                .collect(Collectors.toSet());
        return inspectionCallRepository.findByCallNoIn(callNos).stream()
                .filter(c -> c.getStatus() == null || !closed.contains(c.getStatus().trim().toUpperCase()))
                .map(SleeperInspectionCall::getCallNo)
                .filter(Objects::nonNull)
                .distinct()
                .sorted()
                .toList();
    }

    // ================= QUERIES / GUARDS =================

    @Override
    public boolean isRejected(ProductionDeclaration declaration) {
        return declaration != null && declaration.getLotStatus() == LotStatus.REJECTED;
    }

    @Override
    public boolean isPassResult(String finalTestResult) {
        return finalTestResult != null && !isBlank(rules.getPassResult())
                && finalTestResult.trim().toUpperCase().startsWith(rules.getPassResult().trim().toUpperCase());
    }

    @Override
    public boolean isRejectingTest(Long testId) {
        return isRejectingTest(LotRejectionTestType.WATER_CUBE, testId);
    }

    @Override
    public boolean isRejectingTest(LotRejectionTestType testType, Long testId) {
        return testId != null && testType != null && productionDeclarationRepository
                .existsByLotRejectedByTestIdAndLotRejectedByTestType(testId, testType);
    }

    @Override
    public void assertNotRejected(String batchNumber, String plantId) {
        if (isBlank(batchNumber)) {
            return;
        }
        List<ProductionDeclaration> rejected = productionDeclarationRepository
                .findByBatchAndPlantAndLotStatus(batchNumber.trim(), plantId, LotStatus.REJECTED);
        if (!rejected.isEmpty()) {
            throw rejectedBatch(rejected.get(0));
        }
    }

    @Override
    public void assertNotRejected(Long productionDeclarationId) {
        if (productionDeclarationId == null) {
            return;
        }
        productionDeclarationRepository.findById(productionDeclarationId)
                .filter(this::isRejected)
                .ifPresent(pd -> {
                    throw rejectedBatch(pd);
                });
    }

    private BusinessException rejectedBatch(ProductionDeclaration pd) {
        String reason = !isBlank(pd.getLotRejectionReason()) ? pd.getLotRejectionReason() : rules.getRejectionReason();
        String batch = pd.getBatchNumber() != null ? pd.getBatchNumber().trim() : String.valueOf(pd.getId());
        return validation("Batch " + batch + " is rejected (" + reason
                + ") and returned to the vendor. It cannot be offered or tested further.");
    }

    @Override
    public Set<Long> findRejectedIds(Collection<Long> batchIds) {
        if (batchIds == null || batchIds.isEmpty()) {
            return Collections.emptySet();
        }
        List<Long> ids = batchIds.stream().filter(Objects::nonNull).distinct().toList();
        Set<Long> rejected = new HashSet<>();
        for (int i = 0; i < ids.size(); i += ID_CHUNK) {
            rejected.addAll(productionDeclarationRepository.findIdsByIdInAndLotStatus(
                    ids.subList(i, Math.min(i + ID_CHUNK, ids.size())), LotStatus.REJECTED));
        }
        return rejected;
    }

    // ================= HELPERS =================

    private static BusinessException validation(String message) {
        return new BusinessException(new ErrorDetails(
                AppConstant.ERROR_CODE_RESOURCE,
                AppConstant.ERROR_TYPE_CODE_RESOURCE,
                AppConstant.ERROR_TYPE_VALIDATION,
                message));
    }

    private static boolean isBlank(String s) {
        return s == null || s.isBlank();
    }
}
