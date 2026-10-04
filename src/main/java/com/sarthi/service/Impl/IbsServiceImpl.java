package com.sarthi.service.Impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sarthi.constant.AppConstant;
import com.sarthi.dto.IBS.*;
import com.sarthi.dto.ibsDtos.AuthRequestDto;
import com.sarthi.dto.ibsDtos.AuthResponseDto;
import com.sarthi.entity.IBS.*;
import com.sarthi.entity.PoHeader;
import com.sarthi.entity.PoItem;
import com.sarthi.entity.RmHeatFinalResult;
import com.sarthi.entity.UserMaster;
import com.sarthi.entity.finalmaterial.FinalCumulativeResults;
import com.sarthi.entity.finalmaterial.FinalIcEdit;
import com.sarthi.entity.processmaterial.ProcessFinalCheckData;
import com.sarthi.entity.processmaterial.ProcessIcEdit;
import com.sarthi.entity.processmaterial.ProcessLineFinalResult;
import com.sarthi.entity.rawmaterial.InspectionCall;
import com.sarthi.entity.rawmaterial.RmIcEdit;
import com.sarthi.exception.BusinessException;
import com.sarthi.exception.ErrorDetails;
import com.sarthi.repository.*;
import com.sarthi.repository.finalmaterial.FinalCumulativeResultsRepository;
import com.sarthi.repository.finalmaterial.FinalIcEditRepository;
import com.sarthi.repository.processmaterial.ProcessIcEditRepository;
import com.sarthi.repository.processmaterial.ProcessLineFinalResultRepository;
import com.sarthi.repository.rawmaterial.InspectionCallRepository;
import com.sarthi.repository.rawmaterial.RmIcEditRepository;
import com.sarthi.service.IbsService;
import com.sarthi.service.JwtService;

import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Function;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
@Slf4j
public class IbsServiceImpl implements IbsService {

    private UserMasterRepository userMasterRepository;

    private JwtService jwtService;

    private final WebClient webClient;

    private final PoHeaderRepository poHeaderRepository;

    private final IbsCaseIntegrationRepository integrationRepository;

    private final ObjectMapper objectMapper;

    private final RmIcEditRepository rmIcEditRepository;
    private final ProcessIcEditRepository processIcEditRepository;
    private final FinalIcEditRepository finalIcEditRepository;
    private final InspectionCallRepository inspectionCallsRepository;

    private final RmHeatFinalResultRepository rmHeatFinalResultRepository;
    private final ProcessLineFinalResultRepository processLineFinalResultRepository;
    private final FinalCumulativeResultsRepository finalCumulativeResultsRepository;

    private final com.sarthi.SRailPad.repository.inspectionCall.RailpadProcessIcEditRepository railpadProcessIcEditRepository;
    private final com.sarthi.SRailPad.repository.inspectionCall.RailpadFinalIcEditRepository railpadFinalIcEditRepository;
    private final com.sarthi.SRailPad.repository.inspectionCall.RailInspectionCallRepository railInspectionCallRepository;

    private final com.sarthi.Sleeper.repository.FinalInspectionRepository.SleeperFinalIcEditRepository sleeperFinalIcEditRepository;
    private final com.sarthi.Sleeper.repository.FinalInspectionRepository.SleeperInspectionCallRepository sleeperInspectionCallRepository;
    private final com.sarthi.Sleeper.repository.VendorPlantRepository vendorPlantRepository;
    private final com.sarthi.SRailPad.repository.RailVendorPlantsRepository railVendorPlantsRepository;

    private final IbsCallRegistrationRepository ibsCallRegistrationRepository;

    private final IbsBillDetailsRepository ibsBillDetailsRepository;
    private final IbsPaymentDetailsRepository ibsPaymentDetailsRepository;

    private final IbsBillingClient  billingClient;
    @Override
    public AuthResponseDto integrationLogin(
            AuthRequestDto request) {

        UserMaster user = userMasterRepository
                .findFirstByEmployeeCode(request.getLoginId())
                .orElseThrow(() -> new BusinessException(
                        new ErrorDetails(
                                AppConstant.ERROR_CODE_INVALID,
                                AppConstant.ERROR_TYPE_CODE_INVALID,
                                AppConstant.ERROR_TYPE_INVALID,
                                "Invalid credentials."
                        )));

        // Password Validation
        if (!com.sarthi.util.PasswordEncryptionUtil.matches(request.getPassword(), user.getPassword())) {

            throw new BusinessException(
                    new ErrorDetails(
                            AppConstant.ERROR_CODE_INVALID,
                            AppConstant.ERROR_TYPE_CODE_INVALID,
                            AppConstant.ERROR_TYPE_INVALID,
                            "Invalid credentials."
                    ));
        }

        // Generate JWT Token
        String token = jwtService.generateToken(user);

        return new AuthResponseDto(
                token,
                "Bearer",
                3600L
        );
    }




      /*  public void createInitialEntries() {

            var poList = poHeaderRepository.findByCaseNoIsNull();

            for (PoHeader po : poList) {

                boolean alreadyExists =
                        integrationRepository
                                .existsByPoKeyAndCompletedFalse(
                                        po.getPoKey()
                                );

                if (alreadyExists) {
                    continue;
                }

                IbsCaseIntegration integration =
                        new IbsCaseIntegration();

                integration.setPoHeader(po);
                integration.setPoKey(po.getPoKey());
                integration.setPoNo(po.getPoNo());
                integration.setRlyCd(po.getRlyCd());
                integration.setPoDate(po.getPoDate());

                integration.setStatus("NEW");

                integration.setNextRetryTime(LocalDateTime.now());

                integrationRepository.save(integration);
            }
        }*/
      public void createInitialEntries() {

          var poList = poHeaderRepository.findByCaseNoIsNull();

          for (PoHeader po : poList) {

              boolean alreadyExists =
                      integrationRepository
                              .existsByPoKeyAndCompletedFalse(
                                      po.getPoKey()
                              );

              if (alreadyExists) {
                  continue;
              }

              IbsCaseIntegration integration =
                      new IbsCaseIntegration();

              integration.setPoHeader(po);
              integration.setPoKey(po.getPoKey());
              integration.setPoNo(po.getPoNo());
              integration.setRlyCd(po.getRlyShortName() != null && !po.getRlyShortName().trim().isEmpty() ? po.getRlyShortName().trim() : po.getRlyCd());
              integration.setPoDate(po.getPoDate());

              integration.setStatus("NEW");

              integration.setNextRetryTime(LocalDateTime.now());

              // INITIAL API CALL
              processIntegration(integration);

              integrationRepository.save(integration);
          }
      }

        public void processIntegration(IbsCaseIntegration integration) {

            try {

                String rlyCdToSend = (integration.getPoHeader() != null && integration.getPoHeader().getRlyShortName() != null && !integration.getPoHeader().getRlyShortName().trim().isEmpty())
                        ? integration.getPoHeader().getRlyShortName().trim()
                        : integration.getRlyCd();

                IbsCaseRequestDto request =
                        new IbsCaseRequestDto(
                                integration.getPoKey(),
                                integration.getPoNo(),
                                integration.getPoDate()
                                        .format(
                                                DateTimeFormatter.ofPattern(
                                                        "yyyy-MM-dd HH:mm:ss"
                                                )
                                        ),
                                rlyCdToSend
                        );

                String requestJson =
                        objectMapper.writeValueAsString(request);

                integration.setRequestJson(requestJson);

                IbsCaseResponseDto response =
                        webClient.post()
                                .uri("/IBS2MobileAPI/Sarthi/get-case-no")
                                .bodyValue(request)
                                .retrieve()
                                .bodyToMono(IbsCaseResponseDto.class)
                                .block();

                String responseJson =
                        objectMapper.writeValueAsString(response);

                integration.setResponseJson(responseJson);

                integration.setLastAttemptTime(LocalDateTime.now());

                if (response != null &&
                        response.getData() != null) {

                    String status =
                            response.getData().getStatus();

                    integration.setStatus(status);

                    if ("AVAILABLE".equalsIgnoreCase(status)) {

                        String cleanCaseNo = formatCaseNumbers(response.getData().getCaseNo());

                        integration.setCaseNo(cleanCaseNo);

                        integration.setCompleted(true);

                        PoHeader po =
                                integration.getPoHeader();

                        if (po != null) {
                            po.setCaseNo(cleanCaseNo);

                            po.setCaseStatus(status);

                            if (po.getItems() != null && !po.getItems().isEmpty()) {
                                for (PoItem item : po.getItems()) {
                                    item.setCaseNo(cleanCaseNo);
                                }
                            }

                            poHeaderRepository.save(po);
                        }

                    }  else {

                        integration.setStatus("PENDING");

                        integration.setRetryCount(
                                integration.getRetryCount() + 1
                        );

                        // Retry after 2 days
                        integration.setNextRetryTime(
                                LocalDateTime.now().plusDays(1)
                        );
                    }

                } else {

                    integration.setStatus("FAILED");

                    integration.setRetryCount(
                            integration.getRetryCount() + 1
                    );

                    integration.setNextRetryTime(
                            LocalDateTime.now().plusDays(1)
                    );
                }

                integrationRepository.save(integration);

            } catch (Exception ex) {

                log.error("IBS API ERROR", ex);

                integration.setStatus("FAILED");

                integration.setErrorMessage(ex.getMessage());

                integration.setRetryCount(
                        integration.getRetryCount() + 1
                );

                integration.setLastAttemptTime(
                        LocalDateTime.now()
                );

                integration.setNextRetryTime(
                        LocalDateTime.now().plusDays(1)
                );

                integrationRepository.save(integration);
            }
        }
/*
    public List<IbsInspectionDto> getAllGeneratedIcCalls() {

        List<IbsInspectionDto> responseList =
                new ArrayList<>();


        // FETCH ALL ACKNOWLEDGED CALLS ONCE
        Set<String> acknowledgedCalls =
                ibsCallRegistrationRepository
                        .findAllCallNumbers();


        // ================= RAW MATERIAL =================

        rmIcEditRepository.findAll()
                .forEach(rm -> {

                    String icNumber =
                            extractIcNumber(
                                    rm.getIcNumber()
                            );

                    if (!acknowledgedCalls.contains(icNumber)) {

                        responseList.add(
                                buildDto(
                                        rm.getIcNumber(),
                                        rm.getCreatedBy(),
                                        rm.getBookNo(),
                                        rm.getSetNo(),
                                        rm.getCreatedAt(),
                                        "RM"
                                )
                        );
                    }
                });


        // ================= PROCESS =================

        processIcEditRepository.findAll()
                .forEach(process -> {

                    String icNumber =
                            extractIcNumber(
                                    process.getIcNumber()
                            );

                    if (!acknowledgedCalls.contains(icNumber)) {

                        responseList.add(
                                buildDto(
                                        process.getIcNumber(),
                                        process.getCreatedBy(),
                                        process.getBookNo(),
                                        process.getSetNo(),
                                        process.getCreatedAt(),
                                        "PROCESS"
                                )
                        );
                    }
                });


        // ================= FINAL =================

        finalIcEditRepository.findAll()
                .forEach(finalIc -> {

                    String icNumber =
                            extractIcNumber(
                                    finalIc.getIcNumber()
                            );

                    if (!acknowledgedCalls.contains(icNumber)) {

                        responseList.add(
                                buildDto(
                                        finalIc.getIcNumber(),
                                        finalIc.getCreatedBy(),
                                        finalIc.getBookNo(),
                                        finalIc.getSetNo(),
                                        finalIc.getCreatedAt(),
                                        "FINAL"
                                )
                        );
                    }
                });

        return responseList;
    }


    private IbsInspectionDto buildDto(
            String fullIcNumber,
            Object createdBy,
            Object bookNo,
            Object setNo,
            LocalDateTime createdAt,
            String type
    ) {

        String icNumber = extractIcNumber(fullIcNumber);

        InspectionCall inspectionCall =
                inspectionCallsRepository.findByIcNumber(icNumber)
                        .orElseThrow(() -> new BusinessException(
                                new ErrorDetails(
                                        AppConstant.ERROR_CODE_INVALID,
                                        AppConstant.ERROR_TYPE_CODE_INVALID,
                                        AppConstant.ERROR_TYPE_INVALID,
                                        "Invalid call no."
                                )));

        PoHeader poHeader =
                poHeaderRepository.findByPoNo(
                                inspectionCall.getPoNo()
                        )
                        .orElseThrow(() -> new BusinessException(
                                new ErrorDetails(
                                        AppConstant.ERROR_CODE_INVALID,
                                        AppConstant.ERROR_TYPE_CODE_INVALID,
                                        AppConstant.ERROR_TYPE_INVALID,
                                        "Invalid po no."
                                )));

        QuantityResult quantityResult =
                getQuantityDetails(icNumber, type);

        IbsInspectionDto dto = new IbsInspectionDto();

        dto.setCaseNumber(
                poHeader.getCaseNo()
        );

        dto.setCallDate(
                inspectionCall.getDesiredInspectionDate()
        );

        dto.setPlaceOfInspection(
                inspectionCall.getPlaceOfInspection()
        );

        dto.setIeEmployeeNumber(
                String.valueOf(createdBy)
        );

        dto.setCallStatus("IC Generated");

        dto.setPoItemSerialNumbers(
                List.of(inspectionCall.getPoSerialNo())
        );

        dto.setBkNumber(
                String.valueOf(bookNo)
        );

        dto.setSetNumber(
                String.valueOf(setNo)
        );

        dto.setIcDate(
                createdAt.toLocalDate()
        );

        dto.setQuantityOffered(
                quantityResult.getQuantityOffered()
        );

        dto.setQuantityPassed(
                quantityResult.getQuantityPassed()
        );

        dto.setQuantityRejected(
                quantityResult.getQuantityRejected()
        );

        return dto;
    }


    private QuantityResult getQuantityDetails(
            String icNumber,
            String type
    ) {

        // ================= RAW MATERIAL =================

        if ("RM".equals(type)) {

            List<RmHeatFinalResult> results =
                    rmHeatFinalResultRepository
                            .findByInspectionCallNo(icNumber);

            Map<String, Integer> offeredMap =
                    new HashMap<>();

            int passedQty = 0;
            int rejectedQty = 0;

            for (RmHeatFinalResult rm : results) {

                String heatNo = rm.getHeatNo();

                int offered =
                        rm.getTotalQtyOfferedMt() != null
                                ? rm.getTotalQtyOfferedMt().intValue()
                                : 0;

                int accepted =
                        rm.getAcceptedQtyMt() != null
                                ? rm.getAcceptedQtyMt().intValue()
                                : 0;

                int rejected =
                        rm.getWeightRejectedMt() != null
                                ? rm.getWeightRejectedMt().intValue()
                                : 0;

                // SAME HEAT NO -> TAKE ONLY ONE OFFERED QTY
                offeredMap.putIfAbsent(
                        heatNo,
                        offered
                );

                // SUM OF ALL ACCEPTED
                passedQty += accepted;

                // SUM OF ALL REJECTED
                rejectedQty += rejected;
            }

            int offeredQty =
                    offeredMap.values()
                            .stream()
                            .mapToInt(Integer::intValue)
                            .sum();

            return new QuantityResult(
                    offeredQty,
                    passedQty,
                    rejectedQty
            );
        }


        // ================= PROCESS =================

        else if ("PROCESS".equals(type)) {

            List<ProcessLineFinalResult> results =
                    processLineFinalResultRepository
                            .findByInspectionCallNo(icNumber);

            Map<String, Integer> offeredMap =
                    new HashMap<>();

            int acceptedQty = 0;
            int rejectedQty = 0;

            for (ProcessLineFinalResult process : results) {

                String uniqueKey =
                        icNumber + "_" + process.getLotNumber();

                int offered =
                        process.getOfferedQty() != null
                                ? process.getOfferedQty()
                                : 0;

                int accepted =
                        process.getTotalAccepted() != null
                                ? process.getTotalAccepted()
                                : 0;

                int rejected =
                        process.getTotalRejected() != null
                                ? process.getTotalRejected()
                                : 0;

                // SAME CALL + SAME LOT -> TAKE ONLY ONE OFFERED QTY
                offeredMap.putIfAbsent(
                        uniqueKey,
                        offered
                );

                // SUM OF ALL ACCEPTED
                acceptedQty += accepted;

                // SUM OF ALL REJECTED
                rejectedQty += rejected;
            }

            int offeredQty =
                    offeredMap.values()
                            .stream()
                            .mapToInt(Integer::intValue)
                            .sum();

            return new QuantityResult(
                    offeredQty,
                    acceptedQty,
                    rejectedQty
            );
        }


        // ================= FINAL =================

        else {

            FinalCumulativeResults results =
                    finalCumulativeResultsRepository
                            .findByInspectionCallNo(icNumber).orElse(null);;

            int offeredQty = 0;
            int passedQty = 0;
            int rejectedQty = 0;



                offeredQty +=
                        results.getQtyNowOffered() != null
                                ? results.getQtyNowOffered()
                                : 0;

                passedQty +=
                        results.getQtyNowPassed() != null
                                ? results.getQtyNowPassed()
                                : 0;

                rejectedQty +=
                        results.getQtyNowRejected() != null
                                ? results.getQtyNowRejected()
                                : 0;


            return new QuantityResult(
                    offeredQty,
                    passedQty,
                    rejectedQty
            );
        }
    }
*/

    @Transactional(readOnly = true)
    public List<IbsInspectionDto> getAllGeneratedIcCalls() {

        CompletableFuture<List<IbsInspectionDto>> f1 = CompletableFuture.supplyAsync(() -> {
            try {
                return mapResult(rmHeatFinalResultRepository.getRmInspectionCalls(), "ERC");
            } catch (Exception e) {
                log.error("Error fetching ERC RM inspection calls: {}", e.getMessage(), e);
                return Collections.emptyList();
            }
        });

        CompletableFuture<List<IbsInspectionDto>> f2 = CompletableFuture.supplyAsync(() -> {
            try {
                return mapResult(processLineFinalResultRepository.getProcessInspectionCalls(), "ERC");
            } catch (Exception e) {
                log.error("Error fetching ERC Process inspection calls: {}", e.getMessage(), e);
                return Collections.emptyList();
            }
        });

        CompletableFuture<List<IbsInspectionDto>> f3 = CompletableFuture.supplyAsync(() -> {
            try {
                return mapResult(finalCumulativeResultsRepository.getFinalInspectionCalls(), "ERC");
            } catch (Exception e) {
                log.error("Error fetching ERC Final inspection calls: {}", e.getMessage(), e);
                return Collections.emptyList();
            }
        });

        CompletableFuture<List<IbsInspectionDto>> f4 = CompletableFuture.supplyAsync(() -> {
            try {
                return mapResult(railpadProcessIcEditRepository.getRailpadProcessInspectionCalls(), "RAILPAD");
            } catch (Exception e) {
                log.error("Error fetching Railpad Process inspection calls: {}", e.getMessage(), e);
                return Collections.emptyList();
            }
        });

        CompletableFuture<List<IbsInspectionDto>> f5 = CompletableFuture.supplyAsync(() -> {
            try {
                return mapResult(railpadFinalIcEditRepository.getRailpadFinalInspectionCalls(), "RAILPAD");
            } catch (Exception e) {
                log.error("Error fetching Railpad Final inspection calls: {}", e.getMessage(), e);
                return Collections.emptyList();
            }
        });

        CompletableFuture<List<IbsInspectionDto>> f6 = CompletableFuture.supplyAsync(() -> {
            try {
                return mapResult(railInspectionCallRepository.getRailpadCancelledInspectionCalls(), "RAILPAD");
            } catch (Exception e) {
                log.error("Error fetching Railpad Cancelled inspection calls: {}", e.getMessage(), e);
                return Collections.emptyList();
            }
        });

        CompletableFuture<List<IbsInspectionDto>> f7 = CompletableFuture.supplyAsync(() -> {
            try {
                return mapResult(sleeperFinalIcEditRepository.getSleeperFinalInspectionCalls(), "SLEEPER");
            } catch (Exception e) {
                log.error("Error fetching Sleeper Final inspection calls: {}", e.getMessage(), e);
                return Collections.emptyList();
            }
        });

        CompletableFuture<List<IbsInspectionDto>> f8 = CompletableFuture.supplyAsync(() -> {
            try {
                return mapResult(sleeperInspectionCallRepository.getSleeperCancelledInspectionCalls(), "SLEEPER");
            } catch (Exception e) {
                log.error("Error fetching Sleeper Cancelled inspection calls: {}", e.getMessage(), e);
                return Collections.emptyList();
            }
        });

        List<IbsInspectionDto> responseList = new ArrayList<>();
        try {
            CompletableFuture.allOf(f1, f2, f3, f4, f5, f6, f7, f8).join();
            responseList.addAll(f1.get());
            responseList.addAll(f2.get());
            responseList.addAll(f3.get());
            responseList.addAll(f4.get());
            responseList.addAll(f5.get());
            responseList.addAll(f6.get());
            responseList.addAll(f7.get());
            responseList.addAll(f8.get());
        } catch (Exception e) {
            log.error("Error aggregating IBS inspection calls: {}", e.getMessage(), e);
        }

        // Deduplicate across all product queries by callNumber
        Map<String, IbsInspectionDto> uniqueCalls = new LinkedHashMap<>();
        for (IbsInspectionDto dto : responseList) {
            String callNo = dto.getCallNumber();
            if (callNo == null || callNo.trim().isEmpty()) {
                callNo = dto.getIcNumber();
            }
            if (callNo == null || callNo.trim().isEmpty()) {
                continue;
            }
            callNo = callNo.trim();
            if (!uniqueCalls.containsKey(callNo)) {
                uniqueCalls.put(callNo, dto);
            } else {
                IbsInspectionDto existing = uniqueCalls.get(callNo);
                if ((existing.getCaseNumber() == null || existing.getCaseNumber().trim().isEmpty())
                        && (dto.getCaseNumber() != null && !dto.getCaseNumber().trim().isEmpty())) {
                    uniqueCalls.put(callNo, dto);
                }
            }
        }

        return new ArrayList<>(uniqueCalls.values());
    }

    @Override
    @Transactional(readOnly = true)
    public List<IbsInspectionDto> getCompletedIbsCalls() {
        List<IbsCallRegistration> allRegistrations = ibsCallRegistrationRepository.findAllCalls();
        log.info("Total registrations found in ibs_call_registration: {}", allRegistrations != null ? allRegistrations.size() : 0);

        if (allRegistrations == null || allRegistrations.isEmpty()) {
            return Collections.emptyList();
        }

        Map<String, IbsCallRegistration> regMap = new LinkedHashMap<>();
        List<IbsCallRegistration> completedList = new ArrayList<>();
        Set<String> seenCallNumbers = new HashSet<>();
        Set<String> targetCallNumbers = new HashSet<>();

        // Because allRegistrations is ordered by id DESC, the first time we see any callNumber,
        // it is strictly the latest registration record for that call number.
        for (IbsCallRegistration reg : allRegistrations) {
            if (reg.getCallNumber() != null && !reg.getCallNumber().trim().isEmpty()) {
                String c = reg.getCallNumber().trim();
                String upperC = c.toUpperCase();
                if (!seenCallNumbers.contains(upperC)) {
                    seenCallNumbers.add(upperC);
                    completedList.add(reg);
                    regMap.put(c, reg);
                    regMap.put(upperC, reg);
                    targetCallNumbers.add(c);
                    targetCallNumbers.add(upperC);
                    if (c.contains("/")) {
                        for (String part : c.split("/")) {
                            String p = part.trim();
                            if (!p.isEmpty() && p.length() > 4) {
                                regMap.putIfAbsent(p, reg);
                                regMap.putIfAbsent(p.toUpperCase(), reg);
                                targetCallNumbers.add(p);
                                targetCallNumbers.add(p.toUpperCase());
                            }
                        }
                    }
                }
            }
        }

        if (targetCallNumbers.isEmpty()) {
            return Collections.emptyList();
        }

        CompletableFuture<List<IbsInspectionDto>> f1 = CompletableFuture.supplyAsync(() -> {
            try {
                return mapResult(rmHeatFinalResultRepository.getRmCompletedInspectionCalls(targetCallNumbers), "ERC");
            } catch (Exception e) {
                log.error("Error fetching ERC RM completed inspection calls: {}", e.getMessage(), e);
                return Collections.emptyList();
            }
        });

        CompletableFuture<List<IbsInspectionDto>> f2 = CompletableFuture.supplyAsync(() -> {
            try {
                return mapResult(processLineFinalResultRepository.getProcessCompletedInspectionCalls(targetCallNumbers), "ERC");
            } catch (Exception e) {
                log.error("Error fetching ERC Process completed inspection calls: {}", e.getMessage(), e);
                return Collections.emptyList();
            }
        });

        CompletableFuture<List<IbsInspectionDto>> f3 = CompletableFuture.supplyAsync(() -> {
            try {
                return mapResult(finalCumulativeResultsRepository.getFinalCompletedInspectionCalls(targetCallNumbers), "ERC");
            } catch (Exception e) {
                log.error("Error fetching ERC Final completed inspection calls: {}", e.getMessage(), e);
                return Collections.emptyList();
            }
        });

        CompletableFuture<List<IbsInspectionDto>> f4 = CompletableFuture.supplyAsync(() -> {
            try {
                return mapResult(railpadProcessIcEditRepository.getRailpadProcessCompletedInspectionCalls(targetCallNumbers), "RAILPAD");
            } catch (Exception e) {
                log.error("Error fetching Railpad Process completed inspection calls: {}", e.getMessage(), e);
                return Collections.emptyList();
            }
        });

        CompletableFuture<List<IbsInspectionDto>> f5 = CompletableFuture.supplyAsync(() -> {
            try {
                return mapResult(railpadFinalIcEditRepository.getRailpadFinalCompletedInspectionCalls(targetCallNumbers), "RAILPAD");
            } catch (Exception e) {
                log.error("Error fetching Railpad Final completed inspection calls: {}", e.getMessage(), e);
                return Collections.emptyList();
            }
        });

        CompletableFuture<List<IbsInspectionDto>> f6 = CompletableFuture.supplyAsync(() -> {
            try {
                return mapResult(sleeperFinalIcEditRepository.getSleeperFinalCompletedInspectionCalls(targetCallNumbers), "SLEEPER");
            } catch (Exception e) {
                log.error("Error fetching Sleeper Final completed inspection calls: {}", e.getMessage(), e);
                return Collections.emptyList();
            }
        });

        List<IbsInspectionDto> rawList = new ArrayList<>();
        try {
            CompletableFuture.allOf(f1, f2, f3, f4, f5, f6).join();
            rawList.addAll(f1.get());
            rawList.addAll(f2.get());
            rawList.addAll(f3.get());
            rawList.addAll(f4.get());
            rawList.addAll(f5.get());
            rawList.addAll(f6.get());
        } catch (Exception e) {
            log.error("Error aggregating completed IBS inspection calls: {}", e.getMessage(), e);
        }

        // Deduplicate across queries by callNumber
        Map<String, IbsInspectionDto> uniqueCalls = new LinkedHashMap<>();
        for (IbsInspectionDto dto : rawList) {
            String callNo = dto.getCallNumber();
            if (callNo == null || callNo.trim().isEmpty()) {
                callNo = dto.getIcNumber();
            }
            if (callNo == null || callNo.trim().isEmpty()) {
                continue;
            }
            callNo = callNo.trim();
            if (!uniqueCalls.containsKey(callNo)) {
                uniqueCalls.put(callNo, dto);
            } else {
                IbsInspectionDto existing = uniqueCalls.get(callNo);
                if ((existing.getCaseNumber() == null || existing.getCaseNumber().trim().isEmpty())
                        && (dto.getCaseNumber() != null && !dto.getCaseNumber().trim().isEmpty())) {
                    uniqueCalls.put(callNo, dto);
                }
            }
        }

        Set<Long> matchedRegIds = new HashSet<>();
        // Enrich every matched call with IBS registration details
        for (IbsInspectionDto dto : uniqueCalls.values()) {
            String callNo = dto.getCallNumber() != null ? dto.getCallNumber().trim() : "";
            String icNo = dto.getIcNumber() != null ? dto.getIcNumber().trim() : "";

            IbsCallRegistration reg = regMap.get(callNo);
            if (reg == null && !callNo.isEmpty()) {
                reg = regMap.get(callNo.toUpperCase());
            }
            if (reg == null && !icNo.isEmpty()) {
                reg = regMap.get(icNo);
            }
            if (reg == null && !icNo.isEmpty()) {
                reg = regMap.get(icNo.toUpperCase());
            }
            if (reg == null && callNo.contains("/")) {
                for (String part : callNo.split("/")) {
                    String p = part.trim();
                    if (!p.isEmpty() && p.length() > 4) {
                        reg = regMap.get(p.toUpperCase());
                        if (reg != null) break;
                    }
                }
            }
            if (reg == null && icNo.contains("/")) {
                for (String part : icNo.split("/")) {
                    String p = part.trim();
                    if (!p.isEmpty() && p.length() > 4) {
                        reg = regMap.get(p.toUpperCase());
                        if (reg != null) break;
                    }
                }
            }

            if (reg != null) {
                matchedRegIds.add(reg.getId());
                dto.setSrNo(reg.getSrNo());
                dto.setIbsStatus(reg.getStatus());
                dto.setCallStatus(reg.getStatus() != null ? reg.getStatus() : "SUCCESS");
                dto.setReason(reg.getReason());
                dto.setVersion(reg.getVersion());
                dto.setBillingStatus(reg.getBillingStatus());
                dto.setAcknowledgedAt(reg.getAcknowledgedAt());
            }
        }

        // For any remaining completed calls from ibs_call_registration not covered by queries
        for (IbsCallRegistration reg : completedList) {
            if (matchedRegIds.contains(reg.getId())) {
                continue;
            }
            String callNo = reg.getCallNumber() != null ? reg.getCallNumber().trim() : "";
            if (callNo.isEmpty() || uniqueCalls.containsKey(callNo) || uniqueCalls.containsKey(callNo.toUpperCase())) {
                continue;
            }
            IbsInspectionDto dto = new IbsInspectionDto();
            dto.setCallNumber(callNo);
            dto.setSrNo(reg.getSrNo());
            dto.setIbsStatus(reg.getStatus());
            dto.setReason(reg.getReason());
            dto.setVersion(reg.getVersion());
            dto.setBillingStatus(reg.getBillingStatus());
            dto.setAcknowledgedAt(reg.getAcknowledgedAt());
            dto.setCallStatus(reg.getStatus() != null ? reg.getStatus() : "SUCCESS");
            if (reg.getAcknowledgedAt() != null) {
                dto.setCallDate(reg.getAcknowledgedAt().toLocalDate());
                dto.setIcDate(reg.getAcknowledgedAt().toLocalDate());
            }
            dto.setIcFileLink(
                    "https://api.ritesqasarthi.com"
                            + "/sarthi-backend/api/certificate-storage/view/"
                            + callNo
                            + ".pdf"
            );
            dto.setIsBlocked(0);
            dto.setCancellationCharges(0.0);
            dto.setRejectionCharges(0.0);
            uniqueCalls.put(callNo, dto);
        }

        return new ArrayList<>(uniqueCalls.values());
    }

    private String safeString(Object val) {
        if (val == null) return null;
        if (val instanceof byte[]) return new String((byte[]) val, java.nio.charset.StandardCharsets.UTF_8);
        return val.toString();
    }

    private LocalDate safeDate(Object val) {
        if (val == null) return null;
        if (val instanceof java.sql.Date) return ((java.sql.Date) val).toLocalDate();
        if (val instanceof java.time.LocalDate) return (java.time.LocalDate) val;
        if (val instanceof java.time.LocalDateTime) return ((java.time.LocalDateTime) val).toLocalDate();
        if (val instanceof java.sql.Timestamp) return ((java.sql.Timestamp) val).toLocalDateTime().toLocalDate();
        if (val instanceof java.util.Date) return ((java.util.Date) val).toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDate();
        if (val instanceof byte[]) {
            val = new String((byte[]) val, java.nio.charset.StandardCharsets.UTF_8);
        }
        String s = val.toString().trim();
        if (s.isEmpty()) return null;
        try {
            if (s.length() >= 10) {
                return LocalDate.parse(s.substring(0, 10));
            }
            return LocalDate.parse(s);
        } catch (Exception e) {
            return null;
        }
    }

    private int safeInt(Object val) {
        if (val == null) return 0;
        if (val instanceof Number) return ((Number) val).intValue();
        if (val instanceof byte[]) val = new String((byte[]) val, java.nio.charset.StandardCharsets.UTF_8);
        try {
            return (int) Double.parseDouble(val.toString().trim());
        } catch (Exception e) {
            return 0;
        }
    }

    private double safeDouble(Object val) {
        if (val == null) return 0.0;
        if (val instanceof Number) return ((Number) val).doubleValue();
        if (val instanceof byte[]) val = new String((byte[]) val, java.nio.charset.StandardCharsets.UTF_8);
        try {
            return Double.parseDouble(val.toString().trim());
        } catch (Exception e) {
            return 0.0;
        }
    }

    private Character getRioInitial(String rio, String icNumber, String placeOfInspection, String productType) {
        // 1. Highest priority: The RIO prefix directly from the certificate/IC number (e.g. C/SF-..., W/ER-...)
        if (icNumber != null && icNumber.contains("/")) {
            String prefix = icNumber.substring(0, icNumber.indexOf('/')).trim().toUpperCase();
            if (!prefix.isEmpty()) {
                char ch = prefix.charAt(0);
                if (ch == 'C' || ch == 'W' || ch == 'N' || ch == 'E' || ch == 'S') {
                    return ch;
                }
            }
        }
        // 2. Explicit rio parameter
        if (rio != null && !rio.trim().isEmpty()) {
            String r = rio.trim().toUpperCase();
            if (r.contains("CENTRAL") || r.startsWith("C")) {
                return 'C';
            } else if (r.contains("WESTERN") || r.startsWith("W")) {
                return 'W';
            } else if (r.contains("NORTHERN") || r.startsWith("N")) {
                return 'N';
            } else if (r.contains("EASTERN") || r.startsWith("E")) {
                return 'E';
            } else if (r.contains("SOUTHERN") || r.startsWith("S")) {
                return 'S';
            }
        }
        if (placeOfInspection != null && !placeOfInspection.trim().isEmpty()) {
            String lookedUpRio = null;
            if ("SLEEPER".equalsIgnoreCase(productType)) {
                lookedUpRio = lookupSleeperRio(placeOfInspection);
            } else if ("RAILPAD".equalsIgnoreCase(productType)) {
                lookedUpRio = lookupRailpadRio(placeOfInspection);
            }
            if (lookedUpRio != null && !lookedUpRio.trim().isEmpty()) {
                String r = lookedUpRio.trim().toUpperCase();
                if (r.contains("CENTRAL") || r.startsWith("C")) {
                    return 'C';
                } else if (r.contains("WESTERN") || r.startsWith("W")) {
                    return 'W';
                } else if (r.contains("NORTHERN") || r.startsWith("N")) {
                    return 'N';
                } else if (r.contains("EASTERN") || r.startsWith("E")) {
                    return 'E';
                } else if (r.contains("SOUTHERN") || r.startsWith("S")) {
                    return 'S';
                }
            }
        }
        return null;
    }

    private List<IbsInspectionDto> mapResult(
            List<Object[]> rows,
            String productType
    ) {
        List<IbsInspectionDto> list = new ArrayList<>();
        if (rows == null || rows.isEmpty()) {
            return list;
        }

        Map<String, List<Object[]>> groupedByCall = new LinkedHashMap<>();
        for (Object[] row : rows) {
            String callNumber = row[14] != null ? safeString(row[14]).trim() : "";
            if (callNumber.isEmpty()) {
                callNumber = row[15] != null ? safeString(row[15]).trim() : "";
            }
            if (callNumber.isEmpty()) {
                callNumber = "UNKNOWN_" + java.util.UUID.randomUUID();
            }
            groupedByCall.computeIfAbsent(callNumber, k -> new ArrayList<>()).add(row);
        }

        for (Map.Entry<String, List<Object[]>> entry : groupedByCall.entrySet()) {
            String callNumber = entry.getKey();
            List<Object[]> candidateRows = entry.getValue();

            Character rioInitial = null;
            for (Object[] r : candidateRows) {
                String rowRio = (r.length > 18 && r[18] != null) ? safeString(r[18]) : null;
                String icNumber = r[15] != null ? safeString(r[15]) : null;
                String poi = safeString(r[2]);
                rioInitial = getRioInitial(rowRio, icNumber, poi, productType);
                if (rioInitial != null) {
                    break;
                }
            }

            Object[] bestRow = null;
            String resolvedCaseNo = null;

            // 1. Look for a candidate row whose case number starts with rioInitial
            if (rioInitial != null) {
                for (Object[] r : candidateRows) {
                    String rawCase = safeString(r[0]);
                    if (rawCase != null && !rawCase.trim().isEmpty()) {
                        String[] parts = rawCase.trim().split("[,/;]+");
                        for (String part : parts) {
                            String p = part.trim();
                            if (p.toUpperCase().startsWith(String.valueOf(rioInitial))) {
                                resolvedCaseNo = p;
                                bestRow = r;
                                break;
                            }
                        }
                    }
                    if (bestRow != null) {
                        break;
                    }
                }
            }

            // 2. If no candidate matched RIO initial, pick first candidate with non-empty case number
            if (bestRow == null) {
                for (Object[] r : candidateRows) {
                    String rawCase = safeString(r[0]);
                    if (rawCase != null && !rawCase.trim().isEmpty()) {
                        String[] parts = rawCase.trim().split("[,/;]+");
                        for (String part : parts) {
                            String p = part.trim();
                            if (!p.isEmpty()) {
                                resolvedCaseNo = p;
                                bestRow = r;
                                break;
                            }
                        }
                    }
                    if (bestRow != null) {
                        break;
                    }
                }
            }

            // 3. Fallback to first row
            if (bestRow == null) {
                bestRow = candidateRows.get(0);
                resolvedCaseNo = safeString(bestRow[0]);
            }

            IbsInspectionDto dto = new IbsInspectionDto();
            dto.setCaseNumber(resolvedCaseNo != null ? resolvedCaseNo.trim() : "");

            LocalDate callDate = safeDate(bestRow[1]);
            if (callDate != null) {
                dto.setCallDate(callDate);
            }

            dto.setPlaceOfInspection(safeString(bestRow[2]));
            dto.setIbsManufacturedCode(bestRow[3] != null ? safeString(bestRow[3]) : null);
            dto.setIeEmployeeNumber(bestRow[4] != null ? safeString(bestRow[4]) : null);
            dto.setCallStatus(bestRow[5] != null ? safeString(bestRow[5]) : "A");
            dto.setTypeOfCall(bestRow[6] != null ? safeString(bestRow[6]) : "");

            // Collect distinct PO item serial numbers
            List<String> poItemSerialNumbers = new ArrayList<>();
            Set<String> seenSr = new LinkedHashSet<>();
            for (Object[] r : candidateRows) {
                if (r[7] != null) {
                    String sr = safeString(r[7]).trim();
                    if (!sr.isEmpty() && seenSr.add(sr)) {
                        poItemSerialNumbers.add(sr);
                    }
                }
            }
            if (poItemSerialNumbers.isEmpty()) {
                poItemSerialNumbers.add("1");
            }
            dto.setPoItemSerialNumbers(poItemSerialNumbers);

            dto.setBkNumber(bestRow[8] != null ? safeString(bestRow[8]) : "");
            dto.setSetNumber(bestRow[9] != null ? safeString(bestRow[9]) : "");

            LocalDate icDate = safeDate(bestRow[10]);
            if (icDate != null) {
                dto.setIcDate(icDate);
            }

            dto.setQuantityOffered(bestRow[11] != null ? safeInt(bestRow[11]) : 0);
            dto.setQuantityPassed(bestRow[12] != null ? safeInt(bestRow[12]) : 0);
            dto.setQuantityRejected(bestRow[13] != null ? safeInt(bestRow[13]) : 0);

            dto.setIcFileLink(
                    "https://api.ritesqasarthi.com"
                            + "/sarthi-backend/api/certificate-storage/view/"
                            + callNumber
                            + ".pdf"
            );

            dto.setCallNumber(callNumber);
            dto.setIcNumber(bestRow[15] != null ? safeString(bestRow[15]) : callNumber);

            double cancelCharges = 0.0;
            double rejectCharges = 0.0;
            if (bestRow.length > 16 && bestRow[16] != null) {
                cancelCharges = safeDouble(bestRow[16]);
            }
            if (bestRow.length > 17 && bestRow[17] != null) {
                rejectCharges = safeDouble(bestRow[17]);
            }

            dto.setCancellationCharges(cancelCharges);
            dto.setRejectionCharges(rejectCharges);

            if (cancelCharges > 0.0 || rejectCharges > 0.0) {
                dto.setIsBlocked(1);
            } else {
                dto.setIsBlocked(0);
            }

            list.add(dto);
        }

        return list;
    }

    private String resolveCaseNumberByRio(String rawCaseNo, String rio) {
        if (rawCaseNo == null || rawCaseNo.trim().isEmpty()) {
            return "";
        }
        String trimmed = rawCaseNo.trim();
        if (!trimmed.contains(",") && !trimmed.contains("/") && !trimmed.contains(";")) {
            return trimmed;
        }

        String[] parts = trimmed.split("[,/;]+");
        if (rio != null && !rio.trim().isEmpty()) {
            String cleanRio = rio.trim().toUpperCase();
            String firstLetter = cleanRio.substring(0, 1);
            for (String part : parts) {
                String p = part.trim();
                if (p.toUpperCase().startsWith(firstLetter)) {
                    return p;
                }
            }
        }

        for (String part : parts) {
            String p = part.trim();
            if (!p.isEmpty()) {
                return p;
            }
        }
        return trimmed;
    }

    private final Map<String, String> sleeperRioCache = new java.util.concurrent.ConcurrentHashMap<>();
    private final Map<String, String> railpadRioCache = new java.util.concurrent.ConcurrentHashMap<>();

    private String lookupSleeperRio(String plantId) {
        if (plantId == null || plantId.trim().isEmpty()) return null;
        String key = plantId.trim();
        if (sleeperRioCache.containsKey(key)) {
            String val = sleeperRioCache.get(key);
            return (val == null || val.isEmpty()) ? null : val;
        }
        String foundRio = "";
        try {
            List<com.sarthi.Sleeper.entity.VendorPlant> matchingPlants = vendorPlantRepository.findMatchingPlants(key);
            if (matchingPlants != null && !matchingPlants.isEmpty()) {
                for (com.sarthi.Sleeper.entity.VendorPlant vp : matchingPlants) {
                    if (vp.getRio() != null && !vp.getRio().trim().isEmpty()) {
                        foundRio = vp.getRio().trim();
                        break;
                    }
                }
            }
            if (foundRio.isEmpty()) {
                String cleanPlant = key.replaceAll("^[:\\s]+", "").trim();
                var vpOpt = vendorPlantRepository.findByPlantId(key);
                if (vpOpt.isEmpty() && !cleanPlant.isEmpty()) {
                    vpOpt = vendorPlantRepository.findByPlantId(cleanPlant);
                }
                if (vpOpt.isPresent() && vpOpt.get().getRio() != null && !vpOpt.get().getRio().trim().isEmpty()) {
                    foundRio = vpOpt.get().getRio().trim();
                }
            }
        } catch (Exception e) {
            log.warn("Could not lookup Sleeper RIO for plantId: {}", key, e);
        }
        sleeperRioCache.put(key, foundRio);
        return foundRio.isEmpty() ? null : foundRio;
    }

    private String lookupRailpadRio(String plantId) {
        if (plantId == null || plantId.trim().isEmpty()) return null;
        String key = plantId.trim();
        if (railpadRioCache.containsKey(key)) {
            String val = railpadRioCache.get(key);
            return (val == null || val.isEmpty()) ? null : val;
        }
        String foundRio = "";
        try {
            String cleanPlant = key.replaceAll("^[:\\s]+", "").trim();
            var rvpOpt = railVendorPlantsRepository.findByPlantId(key);
            if (rvpOpt.isEmpty() && !cleanPlant.isEmpty()) {
                rvpOpt = railVendorPlantsRepository.findByPlantId(cleanPlant);
            }
            if (rvpOpt.isPresent() && rvpOpt.get().getRio() != null && !rvpOpt.get().getRio().trim().isEmpty()) {
                foundRio = rvpOpt.get().getRio().trim();
            }
        } catch (Exception e) {
            log.warn("Could not lookup Railpad RIO for plantId: {}", key, e);
        }
        railpadRioCache.put(key, foundRio);
        return foundRio.isEmpty() ? null : foundRio;
    }


    private String extractIcNumber(String icNumber) {

        if (icNumber == null || icNumber.isBlank()) {
            return null;
        }

        String[] parts = icNumber.split("/");

        return parts.length > 1 ? parts[1] : icNumber;
    }



  /*  @Override
    public String acknowledgeCallData(
            IbsAcknowledgementDto dto
    ) {

        boolean alreadyExists =
                ibsCallRegistrationRepository
                        .existsByCallNumberAndStatus(
                                dto.getCallNumber(),
                                dto.getStatus()
                        );

        if (alreadyExists) {
            return "Acknowledgement already exists";
        }

        IbsCallRegistration entity =
                new IbsCallRegistration();

        entity.setCallNumber(
                dto.getCallNumber()
        );

        entity.setStatus(
                dto.getStatus()
        );

        entity.setReason(dto.getReason());

        entity.setAcknowledgedAt(
                LocalDateTime.now()
        );

        ibsCallRegistrationRepository.save(entity);

        return "Acknowledgement received successfully";
    }*/
  @Override
  @Transactional
  public String acknowledgeCallData(IbsAcknowledgementDto dto) {

      Integer latestVersion =
              ibsCallRegistrationRepository.getLatestVersion(dto.getCallNumber());

      IbsCallRegistration entity = new IbsCallRegistration();

      entity.setCallNumber(dto.getCallNumber());

      // Validate SR No for SUCCESS status
      if ("SUCCESS".equalsIgnoreCase(dto.getStatus())) {

          if (dto.getSrNo() == null || dto.getSrNo().trim().isEmpty()) {
              entity.setStatus("FAILED");
              entity.setReason("SR No is mandatory for SUCCESS status");
          } else {
              entity.setStatus("SUCCESS");
              entity.setSrNo(dto.getSrNo());
              entity.setReason(dto.getReason());
          }

      } else {
          entity.setStatus(dto.getStatus());
          entity.setReason(dto.getReason());
      }

      entity.setVersion((latestVersion == null ? 0 : latestVersion) + 1);
      entity.setAcknowledgedAt(LocalDateTime.now());

      IbsCallRegistration savedEntity =
              ibsCallRegistrationRepository.saveAndFlush(entity);

      if (savedEntity.getId() != null) {
          return "Acknowledgement saved successfully for Call No : "
                  + savedEntity.getCallNumber();
      }

      throw new RuntimeException(
              "Failed to save acknowledgement for Call No : "
                      + dto.getCallNumber()
      );
  }


    @Transactional
    public void processBilling() {

        List<IbsCallRegistration> calls =
                ibsCallRegistrationRepository.findPendingBillingCalls();

        for (IbsCallRegistration registration : calls) {

            try {
                String poNo = null;
                LocalDateTime createdAt = null;

                Optional<InspectionCall> ercCall =
                        inspectionCallsRepository.findByIcNumber(
                                registration.getCallNumber()
                        );

                if (ercCall.isPresent()) {
                    poNo = ercCall.get().getPoNo();
                    createdAt = ercCall.get().getCreatedAt();
                } else {
                    var railCall = railInspectionCallRepository.findByCallNo(registration.getCallNumber());
                    if (railCall.isPresent()) {
                        poNo = railCall.get().getPoNo();
                        createdAt = railCall.get().getCreatedAt();
                    } else {
                        var sleeperCall = sleeperInspectionCallRepository.findByCallNo(registration.getCallNumber());
                        if (sleeperCall.isPresent()) {
                            poNo = sleeperCall.get().getPoNo();
                            createdAt = sleeperCall.get().getCreatedAt();
                        }
                    }
                }

                if (poNo == null) {
                    continue;
                }

                String cleanPoNo = poNo.contains("/") ? poNo.split("/")[0].trim() : poNo.trim();
                PoHeader poHeader = poHeaderRepository.findByPoNo(cleanPoNo).orElse(null);
                if (poHeader == null) {
                    continue;
                }

                IbsBillingRequest request =
                        new IbsBillingRequest();

                request.setCaseNo(
                        poHeader.getCaseNo()
                );

                request.setCallRecvDt(
                        (createdAt != null ? createdAt : LocalDateTime.now())
                                .format(DateTimeFormatter.ofPattern("dd-MM-yyyy"))
                );

                request.setCallSno(
                        Integer.valueOf(registration.getSrNo())
                );

                IbsBillingResponse response =
                        billingClient.fetchBilling(
                                request
                              //  getToken()
                        );

                processResponse(
                        registration,
                        response
                );

            } catch (Exception ex) {

                registration.setBillingStatus(
                        BillingStatus.FAILED.name()
                );

                ibsCallRegistrationRepository.save(
                        registration
                );
            }
        }
    }

    private void processResponse(
            IbsCallRegistration registration,
            IbsBillingResponse response) {

        boolean billSaved = false;

        boolean paymentSaved = false;

        if(response.getBillDetails()!=null &&
                !response.getBillDetails().isEmpty()) {

            saveBillDetails(
                    registration,
                    response.getBillDetails()
            );

            billSaved = true;
        }

        if(response.getPaymentDetails()!=null &&
                !response.getPaymentDetails().isEmpty()) {

            savePaymentDetails(
                    registration,
                    response.getPaymentDetails()
            );

            paymentSaved = true;
        }

        if(billSaved && paymentSaved) {

            registration.setBillingStatus("COMPLETED");
        }
        else if(billSaved) {

            registration.setBillingStatus("BILL_FETCHED");
        }
        else if(paymentSaved) {

            registration.setBillingStatus("PAYMENT_FETCHED");
        }
        else {

            registration.setBillingStatus("FAILED");
        }

        ibsCallRegistrationRepository.save(registration);
    }

    private void saveBillDetails(
            IbsCallRegistration registration,
            List<BillDetailDto> bills) {

        for(BillDetailDto dto : bills) {

            boolean exists =
                    ibsBillDetailsRepository.existsByBillNoAndCallSno(
                            dto.getBillNo(),
                            dto.getCallSno()
                    );

            if(exists) {
                continue;
            }

            IbsBillDetails bill =
                    new IbsBillDetails();

            bill.setIbsCallRegistrationId(
                    registration.getId()
            );

            bill.setBillNo(dto.getBillNo());

            bill.setInvoiceNo(dto.getInvoiceNo());

            bill.setCaseNo(dto.getCaseNo());

            bill.setCallSno(dto.getCallSno());

            bill.setBkNo(dto.getBkNo());

            bill.setSetNo(dto.getSetNo());

            bill.setInvoicePdf(dto.getInvoicePdf());

            bill.setInvoiceSuppDocs(
                    dto.getInvoiceSuppDocs()
            );

            ibsBillDetailsRepository.save(bill);
        }
    }

    private void savePaymentDetails(
            IbsCallRegistration registration,
            List<PaymentDetailDto> payments) {

        for(PaymentDetailDto dto : payments) {

            boolean exists =
                    ibsPaymentDetailsRepository.existsByMerTxnId(
                            dto.getMerTxnId()
                    );

            if(exists) {
                continue;
            }

            IbsPaymentDetails payment =
                    new IbsPaymentDetails();

            payment.setIbsCallRegistrationId(
                    registration.getId()
            );

            payment.setCaseNo(dto.getCaseNo());

            payment.setCallSno(dto.getCallSno());

            payment.setDescription(
                    dto.getDescription()
            );

            payment.setMerTxnId(
                    dto.getMerTxnId()
            );

            payment.setAmount(
                    dto.getAmount()
            );

            payment.setCustEmail(
                    dto.getCustEmail()
            );

            payment.setCustMobile(
                    dto.getCustMobile()
            );

         ibsPaymentDetailsRepository.save(payment);
        }
    }

    @Override
    public Object getIbsCaseNo(Map<String, Object> payload) {
        try {
            String poNo = payload.get("PO_NO") != null ? payload.get("PO_NO").toString() :
                    (payload.get("poNo") != null ? payload.get("poNo").toString() : null);
            String poKey = payload.get("POKEY") != null ? payload.get("POKEY").toString() :
                    (payload.get("poKey") != null ? payload.get("poKey").toString() : null);

            Optional<PoHeader> poOpt = Optional.empty();
            if (poNo != null && !poNo.trim().isEmpty()) {
                poOpt = poHeaderRepository.findFirstByPoNo(poNo.trim());
            }
            if (poOpt.isEmpty() && poKey != null && !poKey.trim().isEmpty()) {
                poOpt = poHeaderRepository.findByPoKey(poKey.trim());
            }

            if (poOpt.isPresent() && poOpt.get().getRlyShortName() != null && !poOpt.get().getRlyShortName().trim().isEmpty()) {
                payload.put("RLY_CD", poOpt.get().getRlyShortName().trim());
            }

            String url = "https://ritesinsp.com/IBS2MobileAPI/Sarthi/get-case-no";
            Object response = webClient.post()
                    .uri(url)
                    .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                    .bodyValue(payload)
                    .retrieve()
                    .bodyToMono(Object.class)
                    .block();

            autoSaveIbsCaseNo(payload, response);

            return response;
        } catch (Exception e) {
            log.error("Error fetching IBS case number: ", e);
            Map<String, Object> err = new HashMap<>();
            err.put("resultFlag", 0);
            err.put("message", "Failed to fetch IBS Case Number: " + e.getMessage());
            return err;
        }
    }

    @SuppressWarnings("unchecked")
    private void autoSaveIbsCaseNo(Map<String, Object> payload, Object response) {
        try {
            if (response == null) return;
            Map<String, Object> respMap = null;
            if (response instanceof Map) {
                respMap = (Map<String, Object>) response;
            } else {
                respMap = objectMapper.convertValue(response, Map.class);
            }

            if (respMap == null) return;

            Map<String, Object> dataMap = null;
            if (respMap.get("data") instanceof Map) {
                dataMap = (Map<String, Object>) respMap.get("data");
            } else {
                dataMap = respMap;
            }

            String rawCaseNo = dataMap.get("CASE_NO") != null ? dataMap.get("CASE_NO").toString() :
                    (dataMap.get("caseNo") != null ? dataMap.get("caseNo").toString() : null);

            String caseStatus = dataMap.get("STATUS") != null ? dataMap.get("STATUS").toString() :
                    (dataMap.get("status") != null ? dataMap.get("status").toString() : "AVAILABLE");

            String poNo = dataMap.get("PO_NO") != null ? dataMap.get("PO_NO").toString() :
                    (payload.get("PO_NO") != null ? payload.get("PO_NO").toString() :
                    (payload.get("poNo") != null ? payload.get("poNo").toString() : null));

            String poKey = dataMap.get("POKEY") != null ? dataMap.get("POKEY").toString() :
                    (payload.get("POKEY") != null ? payload.get("POKEY").toString() :
                    (payload.get("poKey") != null ? payload.get("poKey").toString() : null));

            String cleanCaseNo = formatCaseNumbers(rawCaseNo);
            if (cleanCaseNo != null && !cleanCaseNo.trim().isEmpty() && !"N/A".equalsIgnoreCase(cleanCaseNo)) {
                saveCaseNoToPoHeader(poNo, poKey, cleanCaseNo, caseStatus);
            }
        } catch (Exception ex) {
            log.error("Error auto-saving IBS case number to PoHeader: ", ex);
        }
    }

    @Override
    @Transactional
    public Object saveIbsCaseNo(Map<String, Object> payload) {
        try {
            String poNo = payload.get("poNo") != null ? payload.get("poNo").toString() :
                    (payload.get("PO_NO") != null ? payload.get("PO_NO").toString() : null);
            String poKey = payload.get("poKey") != null ? payload.get("poKey").toString() :
                    (payload.get("POKEY") != null ? payload.get("POKEY").toString() : null);
            String rawCaseNo = payload.get("caseNo") != null ? payload.get("caseNo").toString() :
                    (payload.get("CASE_NO") != null ? payload.get("CASE_NO").toString() : null);
            String caseStatus = payload.get("caseStatus") != null ? payload.get("caseStatus").toString() :
                    (payload.get("STATUS") != null ? payload.get("STATUS").toString() : "AVAILABLE");

            String cleanCaseNo = formatCaseNumbers(rawCaseNo);
            if (cleanCaseNo == null || cleanCaseNo.trim().isEmpty()) {
                Map<String, Object> err = new HashMap<>();
                err.put("status", "error");
                err.put("message", "Case number is mandatory.");
                return err;
            }

            boolean saved = saveCaseNoToPoHeader(poNo, poKey, cleanCaseNo, caseStatus);
            Map<String, Object> res = new HashMap<>();
            if (saved) {
                res.put("status", "success");
                res.put("message", "IBS Case Number saved to PO Header successfully.");
                res.put("caseNo", cleanCaseNo);
            } else {
                res.put("status", "error");
                res.put("message", "PO Header not found for PO No: " + poNo + " / PO Key: " + poKey);
            }
            return res;
        } catch (Exception e) {
            log.error("Error saving IBS Case No to PO Header: ", e);
            Map<String, Object> err = new HashMap<>();
            err.put("status", "error");
            err.put("message", "Failed to save IBS Case Number: " + e.getMessage());
            return err;
        }
    }

    private String formatCaseNumbers(String rawCaseNo) {
        if (rawCaseNo == null || rawCaseNo.trim().isEmpty() || "N/A".equalsIgnoreCase(rawCaseNo.trim()) || "null".equalsIgnoreCase(rawCaseNo.trim())) {
            return null;
        }
        String[] parts = rawCaseNo.split(",");
        List<String> list = Arrays.stream(parts)
                .map(String::trim)
                .filter(s -> !s.isEmpty() && !"N/A".equalsIgnoreCase(s) && !"null".equalsIgnoreCase(s))
                .distinct()
                .collect(Collectors.toList());
        return list.isEmpty() ? null : String.join(",", list);
    }

    private boolean saveCaseNoToPoHeader(String poNo, String poKey, String caseNo, String caseStatus) {
        Optional<PoHeader> poOpt = Optional.empty();
        if (poNo != null && !poNo.trim().isEmpty()) {
            poOpt = poHeaderRepository.findFirstByPoNo(poNo.trim());
        }
        if (poOpt.isEmpty() && poKey != null && !poKey.trim().isEmpty()) {
            poOpt = poHeaderRepository.findByPoKey(poKey.trim());
        }

        if (poOpt.isPresent()) {
            PoHeader po = poOpt.get();
            String cleanCaseNo = formatCaseNumbers(caseNo);
            po.setCaseNo(cleanCaseNo);
            po.setCaseStatus(caseStatus != null ? caseStatus : "AVAILABLE");

            if (po.getItems() != null && !po.getItems().isEmpty()) {
                for (PoItem item : po.getItems()) {
                    item.setCaseNo(cleanCaseNo);
                }
            }

            poHeaderRepository.save(po);
            log.info("Saved Case No [{}] and Case Status [{}] to PO Header [ID: {}, PO No: {}]", cleanCaseNo, caseStatus, po.getId(), po.getPoNo());
            return true;
        } else {
            log.warn("PoHeader not found for poNo [{}] or poKey [{}] when saving caseNo [{}]", poNo, poKey, caseNo);
            return false;
        }
    }

}
