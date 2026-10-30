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
import com.sarthi.entity.InspectionCompleteDetails;
import com.sarthi.entity.CallCancellationDetail;
import com.sarthi.entity.WorkflowTransition;
import com.sarthi.entity.WorkflowDeleteHistory;
import com.sarthi.Sleeper.entity.SleeperWorkflowTransaction;
import com.sarthi.SRailPad.entity.RailWorkflowTransaction;
import com.sarthi.exception.BusinessException;
import com.sarthi.exception.ErrorDetails;
import com.sarthi.repository.*;
import com.sarthi.repository.finalmaterial.FinalCumulativeResultsRepository;
import com.sarthi.repository.finalmaterial.FinalIcEditRepository;
import com.sarthi.repository.processmaterial.ProcessIcEditRepository;
import com.sarthi.repository.processmaterial.ProcessLineFinalResultRepository;
import com.sarthi.repository.rawmaterial.InspectionCallRepository;
import com.sarthi.repository.rawmaterial.RmIcEditRepository;
import com.sarthi.repository.certificate.CaseLetterDocumentRepository;
import com.sarthi.repository.certificate.IcAnnexureDocumentRepository;
import com.sarthi.entity.certificate.CaseLetterDocument;
import com.sarthi.entity.certificate.IcAnnexureDocument;
import com.sarthi.entity.processmaterial.ProcessRmIcMapping;
import com.sarthi.entity.processmaterial.ProcessInspectionDetails;
import com.sarthi.entity.finalmaterial.FinalProcessIcMapping;
import com.sarthi.entity.finalmaterial.FinalInspectionDetails;
import com.sarthi.repository.processmaterial.ProcessRmIcMappingRepository;
import com.sarthi.repository.processmaterial.ProcessInspectionDetailsRepository;
import com.sarthi.repository.finalmaterial.FinalProcessIcMappingRepository;
import com.sarthi.repository.finalmaterial.FinalInspectionDetailsRepository;
import com.sarthi.SRailPad.entity.inspectionCall.RailInspectionCall;
import com.sarthi.service.AzureBlobStorageService;
import com.sarthi.service.IbsService;
import com.sarthi.service.JwtService;

import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;

import com.sarthi.entity.ercdiversion.ErcDiversionRequest;
import com.sarthi.entity.ercdiversion.ErcDiversionCallAllocation;
import com.sarthi.repository.ercdiversion.ErcDiversionRequestRepository;
import com.sarthi.repository.ercdiversion.ErcDiversionCallAllocationRepository;
import com.sarthi.repository.PincodePoIMappingRepository;
import com.sarthi.repository.SarthiIbsPoiMappingRepository;
import java.math.BigDecimal;
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
    private final CaseLetterDocumentRepository caseLetterDocumentRepository;
    private final IcAnnexureDocumentRepository icAnnexureDocumentRepository;
    private final ProcessRmIcMappingRepository processRmIcMappingRepository;
    private final ProcessInspectionDetailsRepository processInspectionDetailsRepository;
    private final FinalProcessIcMappingRepository finalProcessIcMappingRepository;
    private final FinalInspectionDetailsRepository finalInspectionDetailsRepository;
    private final AzureBlobStorageService azureBlobStorageService;
    private final InspectionCompleteDetailsRepository inspectionCompleteDetailsRepository;
    private final com.sarthi.SRailPad.repository.inspectionCall.RailInspectionCompleteDetailsRepository railInspectionCompleteDetailsRepository;
    private final CallCancellationDetailRepository callCancellationDetailRepository;

    private final WorkflowTransitionRepository workflowTransitionRepository;
    private final com.sarthi.Sleeper.repository.SleeperWorkflowRepository sleeperWorkflowRepository;
    private final com.sarthi.SRailPad.repository.RailWorkflowTransactionRepository railWorkflowTransactionRepository;
    private final WorkflowDeleteHistoryRepository workflowDeleteHistoryRepository;
    private final ErcDiversionRequestRepository ercDiversionRequestRepository;
    private final ErcDiversionCallAllocationRepository ercDiversionCallAllocationRepository;
    private final PincodePoIMappingRepository pincodePoIMappingRepository;
    private final SarthiIbsPoiMappingRepository sarthiIbsPoiMappingRepository;

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

        // ERC does not require RIO-based case number resolution — use raw case number
        dto.setCaseNumber(poHeader.getCaseNo());

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
                quantityResult != null && quantityResult.getQuantityOffered() != null 
                        ? quantityResult.getQuantityOffered().doubleValue() : 0.0
        );

        dto.setQuantityPassed(
                quantityResult != null && quantityResult.getQuantityPassed() != null 
                        ? quantityResult.getQuantityPassed().doubleValue() : 0.0
        );

        dto.setQuantityRejected(
                quantityResult != null && quantityResult.getQuantityRejected() != null 
                        ? quantityResult.getQuantityRejected().doubleValue() : 0.0
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

    @Override
    @Transactional(readOnly = true)
    public List<IbsInspectionDto> getAllGeneratedIcCalls() {
        long overallStart = System.currentTimeMillis();
        try {
            // 1. Fetch all registrations once to find already registered / successful calls
            List<IbsCallRegistration> allRegistrations = ibsCallRegistrationRepository.findAllCalls();
            Set<String> successfulCalls = new HashSet<>();
            if (allRegistrations != null) {
                for (IbsCallRegistration reg : allRegistrations) {
                    if (reg.getCallNumber() != null && !"FAILED".equalsIgnoreCase(reg.getStatus())) {
                        String c = reg.getCallNumber().trim().toUpperCase();
                        successfulCalls.add(c);
                        if (c.contains("/")) {
                            for (String part : c.split("/")) {
                                String p = part.trim();
                                if (!p.isEmpty()) {
                                    successfulCalls.add(p);
                                }
                            }
                        }
                    }
                }
            }

            // 2. Fetch pending candidate call numbers across ERC, Sleeper, and Railpad (parallel)
            Set<String> ercPending = Collections.synchronizedSet(new HashSet<>());
            Set<String> sleeperPending = Collections.synchronizedSet(new HashSet<>());
            Set<String> railpadPending = Collections.synchronizedSet(new HashSet<>());

            CompletableFuture<Void> cErc = CompletableFuture.runAsync(() -> {
                try {
                    for (String cn : inspectionCallsRepository.findPendingWorkflowCallNumbers()) {
                        if (cn != null && !cn.trim().isEmpty() && !successfulCalls.contains(cn.trim().toUpperCase())) {
                            ercPending.add(cn.trim());
                        }
                    }
                } catch (Exception e) {
                    log.error("Error fetching ERC pending call numbers: {}", e.getMessage(), e);
                }
            });

            CompletableFuture<Void> cSleeper = CompletableFuture.runAsync(() -> {
                try {
                    for (String cn : sleeperInspectionCallRepository.findPendingWorkflowCallNumbers()) {
                        if (cn != null && !cn.trim().isEmpty() && !successfulCalls.contains(cn.trim().toUpperCase())) {
                            sleeperPending.add(cn.trim());
                        }
                    }
                } catch (Exception e) {
                    log.error("Error fetching Sleeper pending call numbers: {}", e.getMessage(), e);
                }
            });

            CompletableFuture<Void> cRailpad = CompletableFuture.runAsync(() -> {
                try {
                    for (String cn : railInspectionCallRepository.findPendingWorkflowCallNumbers()) {
                        if (cn != null && !cn.trim().isEmpty() && !successfulCalls.contains(cn.trim().toUpperCase())) {
                            railpadPending.add(cn.trim());
                        }
                    }
                } catch (Exception e) {
                    log.error("Error fetching Railpad pending call numbers: {}", e.getMessage(), e);
                }
            });

            CompletableFuture.allOf(cErc, cSleeper, cRailpad).join();

            long tCandidates = System.currentTimeMillis();
            log.info("[IBS-FETCH] Candidates pending registration: ERC={}, Sleeper={}, Railpad={} (took {} ms)",
                    ercPending.size(), sleeperPending.size(), railpadPending.size(), (tCandidates - overallStart));

            List<String> ercRmPending = ercPending.stream()
                    .filter(c -> c != null && (c.toUpperCase().startsWith("ER") || (!c.toUpperCase().startsWith("EP") && !c.toUpperCase().startsWith("EF"))))
                    .toList();
            List<String> ercProcessPending = ercPending.stream()
                    .filter(c -> c != null && (c.toUpperCase().startsWith("EP") || (!c.toUpperCase().startsWith("ER") && !c.toUpperCase().startsWith("EF"))))
                    .toList();
            List<String> ercFinalPending = ercPending.stream()
                    .filter(c -> c != null && (c.toUpperCase().startsWith("EF") || (!c.toUpperCase().startsWith("ER") && !c.toUpperCase().startsWith("EP"))))
                    .toList();

            List<IbsInspectionDto> responseList = new ArrayList<>();
            long tFuturesStart = System.currentTimeMillis();

            CompletableFuture<List<IbsInspectionDto>> f1 = ercRmPending.isEmpty() ? CompletableFuture.completedFuture(Collections.emptyList()) : CompletableFuture.supplyAsync(() -> {
                long s = System.currentTimeMillis();
                try {
                    List<IbsInspectionDto> res = mapResult(rmHeatFinalResultRepository.getRmCompletedInspectionCalls(ercRmPending), "ERC");
                    log.info("[IBS-FETCH] f1 (ERC RM) took {} ms (items={})", System.currentTimeMillis() - s, res.size());
                    return res;
                } catch (Exception e) {
                    log.error("Error fetching ERC RM pending calls (took {} ms): {}", System.currentTimeMillis() - s, e.getMessage(), e);
                    return Collections.emptyList();
                }
            });

            CompletableFuture<List<IbsInspectionDto>> f2 = ercProcessPending.isEmpty() ? CompletableFuture.completedFuture(Collections.emptyList()) : CompletableFuture.supplyAsync(() -> {
                long s = System.currentTimeMillis();
                try {
                    List<IbsInspectionDto> res = mapResult(processLineFinalResultRepository.getProcessCompletedInspectionCalls(ercProcessPending), "ERC");
                    log.info("[IBS-FETCH] f2 (ERC Process) took {} ms (items={})", System.currentTimeMillis() - s, res.size());
                    return res;
                } catch (Exception e) {
                    log.error("Error fetching ERC Process pending calls (took {} ms): {}", System.currentTimeMillis() - s, e.getMessage(), e);
                    return Collections.emptyList();
                }
            });

            CompletableFuture<List<IbsInspectionDto>> f3 = ercFinalPending.isEmpty() ? CompletableFuture.completedFuture(Collections.emptyList()) : CompletableFuture.supplyAsync(() -> {
                long s = System.currentTimeMillis();
                try {
                    List<IbsInspectionDto> res = mapResult(finalCumulativeResultsRepository.getFinalCompletedInspectionCalls(ercFinalPending), "ERC");
                    log.info("[IBS-FETCH] f3 (ERC Final) took {} ms (items={})", System.currentTimeMillis() - s, res.size());
                    return res;
                } catch (Exception e) {
                    log.error("Error fetching ERC Final pending calls (took {} ms): {}", System.currentTimeMillis() - s, e.getMessage(), e);
                    return Collections.emptyList();
                }
            });

            CompletableFuture<List<IbsInspectionDto>> f4 = railpadPending.isEmpty() ? CompletableFuture.completedFuture(Collections.emptyList()) : CompletableFuture.supplyAsync(() -> {
                long s = System.currentTimeMillis();
                try {
                    List<IbsInspectionDto> res = mapResult(railpadProcessIcEditRepository.getRailpadProcessCompletedInspectionCalls(railpadPending), "RAILPAD");
                    log.info("[IBS-FETCH] f4 (Railpad Process) took {} ms (items={})", System.currentTimeMillis() - s, res.size());
                    return res;
                } catch (Exception e) {
                    log.error("Error fetching Railpad Process pending calls (took {} ms): {}", System.currentTimeMillis() - s, e.getMessage(), e);
                    return Collections.emptyList();
                }
            });

            CompletableFuture<List<IbsInspectionDto>> f5 = railpadPending.isEmpty() ? CompletableFuture.completedFuture(Collections.emptyList()) : CompletableFuture.supplyAsync(() -> {
                long s = System.currentTimeMillis();
                try {
                    List<IbsInspectionDto> res = mapResult(railpadFinalIcEditRepository.getRailpadFinalCompletedInspectionCalls(railpadPending), "RAILPAD");
                    log.info("[IBS-FETCH] f5 (Railpad Final) took {} ms (items={})", System.currentTimeMillis() - s, res.size());
                    return res;
                } catch (Exception e) {
                    log.error("Error fetching Railpad Final pending calls (took {} ms): {}", System.currentTimeMillis() - s, e.getMessage(), e);
                    return Collections.emptyList();
                }
            });

            CompletableFuture<List<IbsInspectionDto>> f6 = sleeperPending.isEmpty() ? CompletableFuture.completedFuture(Collections.emptyList()) : CompletableFuture.supplyAsync(() -> {
                long s = System.currentTimeMillis();
                try {
                    List<IbsInspectionDto> res = mapResult(sleeperFinalIcEditRepository.getSleeperFinalCompletedInspectionCalls(sleeperPending), "SLEEPER");
                    log.info("[IBS-FETCH] f6 (Sleeper Final) took {} ms (items={})", System.currentTimeMillis() - s, res.size());
                    return res;
                } catch (Exception e) {
                    log.error("Error fetching Sleeper Final pending calls (took {} ms): {}", System.currentTimeMillis() - s, e.getMessage(), e);
                    return Collections.emptyList();
                }
            });

            CompletableFuture<List<IbsInspectionDto>> f7 = CompletableFuture.supplyAsync(() -> {
                long s = System.currentTimeMillis();
                try {
                    List<IbsInspectionDto> res = mapResult(railInspectionCallRepository.getRailpadCancelledInspectionCalls(), "RAILPAD");
                    log.info("[IBS-FETCH] f7 (Railpad Cancelled) took {} ms (items={})", System.currentTimeMillis() - s, res.size());
                    return res;
                } catch (Exception e) {
                    log.error("Error fetching Railpad cancelled inspection calls (took {} ms): {}", System.currentTimeMillis() - s, e.getMessage(), e);
                    return Collections.emptyList();
                }
            });

            CompletableFuture<List<IbsInspectionDto>> f8 = CompletableFuture.supplyAsync(() -> {
                long s = System.currentTimeMillis();
                try {
                    List<IbsInspectionDto> res = mapResult(sleeperInspectionCallRepository.getSleeperCancelledInspectionCalls(), "SLEEPER");
                    log.info("[IBS-FETCH] f8 (Sleeper Cancelled) took {} ms (items={})", System.currentTimeMillis() - s, res.size());
                    return res;
                } catch (Exception e) {
                    log.error("Error fetching Sleeper cancelled inspection calls (took {} ms): {}", System.currentTimeMillis() - s, e.getMessage(), e);
                    return Collections.emptyList();
                }
            });

            CompletableFuture<List<IbsInspectionDto>> f9 = CompletableFuture.supplyAsync(() -> {
                long s = System.currentTimeMillis();
                try {
                    List<IbsInspectionDto> res = mapResult(inspectionCallsRepository.getErcCancelledInspectionCalls(), "ERC");
                    log.info("[IBS-FETCH] f9 (ERC Cancelled) took {} ms (items={})", System.currentTimeMillis() - s, res.size());
                    return res;
                } catch (Exception e) {
                    log.error("Error fetching ERC cancelled inspection calls (took {} ms): {}", System.currentTimeMillis() - s, e.getMessage(), e);
                    return Collections.emptyList();
                }
            });

            try {
                CompletableFuture.allOf(f1, f2, f3, f4, f5, f6, f7, f8, f9).join();
                if (f1.isDone() && !f1.isCompletedExceptionally()) responseList.addAll(f1.get());
                if (f2.isDone() && !f2.isCompletedExceptionally()) responseList.addAll(f2.get());
                if (f3.isDone() && !f3.isCompletedExceptionally()) responseList.addAll(f3.get());
                if (f4.isDone() && !f4.isCompletedExceptionally()) responseList.addAll(f4.get());
                if (f5.isDone() && !f5.isCompletedExceptionally()) responseList.addAll(f5.get());
                if (f6.isDone() && !f6.isCompletedExceptionally()) responseList.addAll(f6.get());
                if (f7.isDone() && !f7.isCompletedExceptionally()) responseList.addAll(f7.get());
                if (f8.isDone() && !f8.isCompletedExceptionally()) responseList.addAll(f8.get());
                if (f9.isDone() && !f9.isCompletedExceptionally()) responseList.addAll(f9.get());
            } catch (Exception e) {
                log.error("Error aggregating IBS pending inspection calls: {}", e.getMessage(), e);
            }
            long tFuturesEnd = System.currentTimeMillis();
            log.info("[IBS-FETCH] Futures f1-f9 joined in {} ms (raw items={})",
                    (tFuturesEnd - tFuturesStart), responseList.size());

            // Deduplicate across all product queries by callNumber
            Map<String, IbsInspectionDto> uniqueCalls = new LinkedHashMap<>();
            for (IbsInspectionDto dto : responseList) {
                if (dto == null) continue;
                String callNo = dto.getCallNumber();
                if (callNo == null || callNo.trim().isEmpty()) {
                    callNo = dto.getIcNumber();
                }
                if (callNo == null || callNo.trim().isEmpty()) {
                    continue;
                }
                callNo = callNo.trim();
                if ("C".equalsIgnoreCase(dto.getCallStatus())) {
                    dto.setIcNumber(null);
                }
                if (!uniqueCalls.containsKey(callNo)) {
                    uniqueCalls.put(callNo, dto);
                } else {
                    IbsInspectionDto existing = uniqueCalls.get(callNo);
                    if ("C".equalsIgnoreCase(dto.getCallStatus()) && !"C".equalsIgnoreCase(existing.getCallStatus())) {
                        if ((dto.getTypeOfCall() == null || dto.getTypeOfCall().trim().isEmpty() || "C".equalsIgnoreCase(dto.getTypeOfCall()))
                                && existing.getTypeOfCall() != null && !existing.getTypeOfCall().trim().isEmpty() && !"C".equalsIgnoreCase(existing.getTypeOfCall())) {
                            dto.setTypeOfCall(existing.getTypeOfCall());
                        }
                        dto.setIcNumber(null);
                        uniqueCalls.put(callNo, dto);
                    } else if (!"C".equalsIgnoreCase(existing.getCallStatus())
                            && (existing.getCaseNumber() == null || existing.getCaseNumber().trim().isEmpty())
                            && (dto.getCaseNumber() != null && !dto.getCaseNumber().trim().isEmpty())) {
                        uniqueCalls.put(callNo, dto);
                    }
                }
            }

            long tEnrichStart = System.currentTimeMillis();
            // Single-batch enrich parent IC links for EP, EF, RPF calls
            enrichLinkedParentIcs(uniqueCalls.values());
            long tLinkedIcs = System.currentTimeMillis();

            // Single-batch enrich uploaded case letters / IC annexures
            enrichUploadedCallDocuments(uniqueCalls.values());
            long tDocs = System.currentTimeMillis();

            // Batch-enrich cancelled calls: zero N+1 queries (batched via IN-clause or fast-path in-memory)
            batchEnrichCancelledCallEmployeeCodes(uniqueCalls.values());
            long tCancelEmp = System.currentTimeMillis();

            // Enrich diverted calls data (placeOfInspection, ibsManufacturedCode, quantities)
            enrichDivertedCallsData(uniqueCalls.values());

            log.info("[IBS-FETCH] Breakdown: Candidates={} ms, Futures={} ms, LinkedIcs={} ms, Docs={} ms, CancelEmp={} ms, Total={} ms",
                    (tCandidates - overallStart), (tFuturesEnd - tFuturesStart), (tLinkedIcs - tEnrichStart), (tDocs - tLinkedIcs), (tCancelEmp - tDocs), (tCancelEmp - overallStart));

            log.info("[IBS-FETCH] getAllGeneratedIcCalls completed in {} ms returning {} calls",
                    System.currentTimeMillis() - overallStart, uniqueCalls.size());

            // RM calls have no previous stage, so ic_linked_for_current_call must strictly be null
            for (IbsInspectionDto dto : uniqueCalls.values()) {
                if (dto != null && isRawMaterialCall(dto)) {
                    dto.setIcLinkedForCurrentCall(null);
                }
            }

            return new ArrayList<>(uniqueCalls.values());
        } catch (Throwable t) {
            log.error("Error in getAllGeneratedIcCalls: {}", t.getMessage(), t);
            return Collections.emptyList();
        }
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

        // Single-batch enrich parent IC links for completed calls
        enrichLinkedParentIcs(uniqueCalls.values());

        // Enrich diverted calls data (placeOfInspection, ibsManufacturedCode, quantities)
        enrichDivertedCallsData(uniqueCalls.values());

        Set<Long> matchedRegIds = new HashSet<>();
        Set<String> completedCallTokens = new HashSet<>();
        Set<String> completedIcTokens = new HashSet<>();
        for (IbsInspectionDto d : uniqueCalls.values()) {
            if (d.getCallNumber() != null) completedCallTokens.add(d.getCallNumber());
            if (d.getIcNumber() != null) completedIcTokens.add(d.getIcNumber());
        }
        for (IbsCallRegistration reg : completedList) {
            if (reg.getCallNumber() != null) completedCallTokens.add(reg.getCallNumber());
        }
        Map<String, String> completedDocLinks = prefetchUploadedCallDocuments(completedCallTokens, completedIcTokens);

        // Enrich every matched call with IBS registration details
        for (IbsInspectionDto dto : uniqueCalls.values()) {
            if ("C".equalsIgnoreCase(dto.getCallStatus())) {
                dto.setIcNumber(null);
            }
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
            if (dto.getUploadedInspectionCallDocument() == null) {
                String docLink = completedDocLinks.get(callNo);
                if (docLink == null && !icNo.isEmpty()) docLink = completedDocLinks.get(icNo);
                dto.setUploadedInspectionCallDocument(docLink);
            }
        }

        // For any remaining completed calls from ibs_call_registration not covered by queries
        Set<String> fallbackEpCallNos = new HashSet<>();
        Set<String> fallbackEfCallNos = new HashSet<>();
        Set<String> fallbackRpfCallNos = new HashSet<>();
        for (IbsCallRegistration reg : completedList) {
            if (matchedRegIds.contains(reg.getId())) continue;
            String cNo = reg.getCallNumber() != null ? reg.getCallNumber().trim() : "";
            if (cNo.isEmpty() || uniqueCalls.containsKey(cNo) || uniqueCalls.containsKey(cNo.toUpperCase())) continue;
            if (isRmCallString(cNo)) continue;
            String upper = cNo.toUpperCase();
            if (upper.startsWith("EP") || upper.contains("EP-")) fallbackEpCallNos.add(cNo);
            else if (upper.startsWith("EF") || upper.contains("EF-")) fallbackEfCallNos.add(cNo);
            else if (upper.startsWith("RPF") || upper.contains("RPF-")) fallbackRpfCallNos.add(cNo);
        }
        Map<String, String> fallbackLinkedIcs = (fallbackEpCallNos.isEmpty() && fallbackEfCallNos.isEmpty() && fallbackRpfCallNos.isEmpty())
                ? Collections.emptyMap()
                : prefetchLinkedParentIcNumbers(fallbackEpCallNos, fallbackEfCallNos, fallbackRpfCallNos);

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
            String docLink = completedDocLinks.get(callNo);
            dto.setUploadedInspectionCallDocument(docLink);
            String upperCall = callNo.toUpperCase();
            if (isRawMaterialCall(dto) || isRmCallString(callNo)
                    || upperCall.startsWith("ER") || upperCall.contains("ER-") || upperCall.contains("/ER")
                    || "RM".equalsIgnoreCase(dto.getTypeOfCall())
                    || "RAW MATERIAL".equalsIgnoreCase(dto.getTypeOfCall())) {
                dto.setIcLinkedForCurrentCall(null);
            } else if (fallbackLinkedIcs.containsKey(callNo)) {
                dto.setIcLinkedForCurrentCall(fallbackLinkedIcs.get(callNo));
            } else if (fallbackLinkedIcs.containsKey(callNo.toUpperCase())) {
                dto.setIcLinkedForCurrentCall(fallbackLinkedIcs.get(callNo.toUpperCase()));
            }
            dto.setIsBlocked(0);
            dto.setCancellationCharges(0.0);
            dto.setRejectionCharges(0.0);
            uniqueCalls.put(callNo, dto);
        }

        // Batch-enrich cancelled calls: zero N+1 queries (batched via IN-clause or fast-path in-memory)
        batchEnrichCancelledCallEmployeeCodes(uniqueCalls.values());

        // Enrich diverted calls data (placeOfInspection, ibsManufacturedCode, quantities)
        enrichDivertedCallsData(uniqueCalls.values());

        // Strictly enforce null linked IC for any RM calls
        for (IbsInspectionDto dto : uniqueCalls.values()) {
            if (dto != null && isRawMaterialCall(dto)) {
                dto.setIcLinkedForCurrentCall(null);
            }
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

    private Double safeQuantity(Object val) {
        if (val == null) return 0.0;
        if (val instanceof byte[]) val = new String((byte[]) val, java.nio.charset.StandardCharsets.UTF_8);
        try {
            double d = (val instanceof Number) ? ((Number) val).doubleValue() : Double.parseDouble(val.toString().trim());
            return java.math.BigDecimal.valueOf(d)
                    .setScale(3, java.math.RoundingMode.HALF_UP)
                    .doubleValue();
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

    private void addTokenVariants(Set<String> allTokens, String raw) {
        if (raw == null || raw.trim().isEmpty()) return;
        String clean = raw.trim();
        if (clean.toLowerCase().endsWith(".pdf")) {
            clean = clean.substring(0, clean.length() - 4).trim();
        }
        if (clean.isEmpty()) return;

        allTokens.add(clean);
        allTokens.add(clean.toUpperCase());
        allTokens.add(clean.toLowerCase());
        allTokens.add(clean.replace("-", "/"));
        allTokens.add(clean.replace("/", "-"));
        allTokens.add(clean.replace("-", "/").toUpperCase());
        allTokens.add(clean.replace("/", "-").toUpperCase());

        if (clean.contains("/") || clean.contains("-")) {
            for (String part : clean.split("[/-]")) {
                String p = part.trim();
                if ((p.length() >= 4 && p.matches(".*\\d+.*")) || p.length() >= 6) {
                    allTokens.add(p);
                    allTokens.add(p.toUpperCase());
                    allTokens.add(p.toLowerCase());
                    allTokens.add(p.replace("-", "/"));
                    allTokens.add(p.replace("/", "-"));
                    allTokens.add(p.replace("-", "/").toUpperCase());
                    allTokens.add(p.replace("/", "-").toUpperCase());
                }
            }
        }
    }

    private Map<String, String> prefetchUploadedCallDocuments(Collection<String> callNumbers, Collection<String> icNumbers) {
        if ((callNumbers == null || callNumbers.isEmpty()) && (icNumbers == null || icNumbers.isEmpty())) {
            return Collections.emptyMap();
        }

        Set<String> allTokens = new HashSet<>();
        if (callNumbers != null) {
            for (String cn : callNumbers) {
                addTokenVariants(allTokens, cn);
            }
        }
        if (icNumbers != null) {
            for (String ic : icNumbers) {
                addTokenVariants(allTokens, ic);
            }
        }
        allTokens.remove("");
        if (allTokens.isEmpty()) {
            return Collections.emptyMap();
        }

        Map<String, String> resultMap = new HashMap<>();

        // 1. Batch fetch Case Letter Documents (Priority 1)
        if (caseLetterDocumentRepository != null) {
            try {
                List<CaseLetterDocument> caseLetters = caseLetterDocumentRepository.findActiveByCallTokens(allTokens);
                for (CaseLetterDocument cld : caseLetters) {
                    String ref = (cld.getCallNo() != null && !cld.getCallNo().trim().isEmpty())
                            ? cld.getCallNo().trim()
                            : (cld.getIcNumber() != null ? cld.getIcNumber().trim() : null);
                    if (ref == null) continue;
                    String link = "https://api.ritesqasarthi.com/sarthi-backend/api/case-letter/view/" + ref + ".pdf";

                    if (cld.getCallNo() != null && !cld.getCallNo().trim().isEmpty()) {
                        String cNo = cld.getCallNo().trim();
                        resultMap.putIfAbsent(cNo, link);
                        resultMap.putIfAbsent(cNo.toUpperCase(), link);
                        resultMap.putIfAbsent(cNo.toLowerCase(), link);
                        resultMap.putIfAbsent(cNo.replace("-", "/"), link);
                        resultMap.putIfAbsent(cNo.replace("/", "-"), link);
                        resultMap.putIfAbsent(cNo.replace("-", "/").toUpperCase(), link);
                        resultMap.putIfAbsent(cNo.replace("/", "-").toUpperCase(), link);
                    }
                    if (cld.getIcNumber() != null && !cld.getIcNumber().trim().isEmpty()) {
                        String ic = cld.getIcNumber().trim();
                        resultMap.putIfAbsent(ic, link);
                        resultMap.putIfAbsent(ic.toUpperCase(), link);
                        resultMap.putIfAbsent(ic.toLowerCase(), link);
                        resultMap.putIfAbsent(ic.replace("-", "/"), link);
                        resultMap.putIfAbsent(ic.replace("/", "-"), link);
                        resultMap.putIfAbsent(ic.replace("-", "/").toUpperCase(), link);
                        resultMap.putIfAbsent(ic.replace("/", "-").toUpperCase(), link);
                    }
                }
            } catch (Exception e) {
                log.warn("Error prefetching case letters: {}", e.getMessage());
            }
        }

        // 2. Batch fetch IC Annexures (Priority 2)
        if (icAnnexureDocumentRepository != null) {
            try {
                List<IcAnnexureDocument> annexures = icAnnexureDocumentRepository.findActiveAnnexuresByCallTokens(allTokens);
                for (IcAnnexureDocument doc : annexures) {
                    String link = "https://api.ritesqasarthi.com/sarthi-backend/api/ic-annexures/download/" + doc.getId();

                    if (doc.getCallNo() != null && !doc.getCallNo().trim().isEmpty()) {
                        String cNo = doc.getCallNo().trim();
                        resultMap.putIfAbsent(cNo, link);
                        resultMap.putIfAbsent(cNo.toUpperCase(), link);
                        resultMap.putIfAbsent(cNo.toLowerCase(), link);
                        resultMap.putIfAbsent(cNo.replace("-", "/"), link);
                        resultMap.putIfAbsent(cNo.replace("/", "-"), link);
                    }
                    if (doc.getIcNumber() != null && !doc.getIcNumber().trim().isEmpty()) {
                        String ic = doc.getIcNumber().trim();
                        resultMap.putIfAbsent(ic, link);
                        resultMap.putIfAbsent(ic.toUpperCase(), link);
                        resultMap.putIfAbsent(ic.toLowerCase(), link);
                        resultMap.putIfAbsent(ic.replace("-", "/"), link);
                        resultMap.putIfAbsent(ic.replace("/", "-"), link);
                    }
                }
            } catch (Exception e) {
                log.warn("Error prefetching ic annexures: {}", e.getMessage());
            }
        }

        return resultMap;
    }

    private void enrichUploadedCallDocuments(Collection<IbsInspectionDto> dtos) {
        if (dtos == null || dtos.isEmpty()) return;
        Set<String> callNumbers = new HashSet<>();
        Set<String> icNumbers = new HashSet<>();
        for (IbsInspectionDto dto : dtos) {
            if (dto == null || "C".equalsIgnoreCase(dto.getCallStatus())) continue;
            if (dto.getCallNumber() != null && !dto.getCallNumber().isBlank()) {
                callNumbers.add(dto.getCallNumber().trim());
            }
            if (dto.getIcNumber() != null && !dto.getIcNumber().isBlank()) {
                icNumbers.add(dto.getIcNumber().trim());
            }
        }
        if (callNumbers.isEmpty() && icNumbers.isEmpty()) return;

        Map<String, String> docLinks = prefetchUploadedCallDocuments(callNumbers, icNumbers);
        if (docLinks.isEmpty()) return;

        for (IbsInspectionDto dto : dtos) {
            if (dto == null || "C".equalsIgnoreCase(dto.getCallStatus())) continue;
            String callNo = dto.getCallNumber() != null ? dto.getCallNumber().trim() : "";
            String icNo = dto.getIcNumber() != null ? dto.getIcNumber().trim() : "";

            String docLink = docLinks.get(callNo);
            if (docLink == null && !callNo.isEmpty()) docLink = docLinks.get(callNo.toUpperCase());
            if (docLink == null && !callNo.isEmpty()) docLink = docLinks.get(callNo.replace("-", "/"));
            if (docLink == null && !callNo.isEmpty()) docLink = docLinks.get(callNo.replace("/", "-"));
            if (docLink == null && !icNo.isEmpty()) docLink = docLinks.get(icNo);
            if (docLink == null && !icNo.isEmpty()) docLink = docLinks.get(icNo.toUpperCase());
            if (docLink == null && !icNo.isEmpty()) docLink = docLinks.get(icNo.replace("-", "/"));
            if (docLink == null && !icNo.isEmpty()) docLink = docLinks.get(icNo.replace("/", "-"));

            if (docLink == null && callNo.contains("/")) {
                for (String part : callNo.split("/")) {
                    String p = part.trim();
                    if (!p.isEmpty()) {
                        docLink = docLinks.get(p);
                        if (docLink == null) docLink = docLinks.get(p.toUpperCase());
                        if (docLink == null) docLink = docLinks.get(p.replace("-", "/"));
                        if (docLink == null) docLink = docLinks.get(p.replace("/", "-"));
                        if (docLink != null) break;
                    }
                }
            }
            if (docLink == null && icNo.contains("/")) {
                for (String part : icNo.split("/")) {
                    String p = part.trim();
                    if (!p.isEmpty()) {
                        docLink = docLinks.get(p);
                        if (docLink == null) docLink = docLinks.get(p.toUpperCase());
                        if (docLink == null) docLink = docLinks.get(p.replace("-", "/"));
                        if (docLink == null) docLink = docLinks.get(p.replace("/", "-"));
                        if (docLink != null) break;
                    }
                }
            }

            if (docLink != null) {
                dto.setUploadedInspectionCallDocument(docLink);
            }
        }
    }

    private boolean isRawMaterialCall(IbsInspectionDto dto) {
        if (dto == null) return false;
        String typeOfCall = dto.getTypeOfCall() != null ? dto.getTypeOfCall().trim().toUpperCase() : "";
        if ("RM".equals(typeOfCall) || "RAW MATERIAL".equals(typeOfCall)
                || typeOfCall.contains("RAW MATERIAL") || typeOfCall.contains("RAW_MATERIAL")) {
            return true;
        }
        String callNo = dto.getCallNumber() != null ? dto.getCallNumber() : "";
        if (isRmCallString(callNo)) {
            return true;
        }
        String icNo = dto.getIcNumber() != null ? dto.getIcNumber() : "";
        if (isRmCallString(icNo)) {
            return true;
        }
        return false;
    }

    private boolean isRmCallString(String raw) {
        if (raw == null || raw.isBlank()) return false;
        String s = raw.trim().toUpperCase().replaceAll("\\s+", "");
        if (s.startsWith("ER-") || s.contains("ER-") || s.contains("/ER-") || s.contains("-ER-")) {
            return true;
        }
        if (s.startsWith("ER") && s.length() > 2 && Character.isDigit(s.charAt(2))) {
            return true;
        }
        if (s.contains("/ER") && (s.contains("/ER/") || s.matches(".*\\/ER\\d+.*"))) {
            return true;
        }
        if (s.contains("/")) {
            for (String part : s.split("/")) {
                String p = part.trim();
                if (p.startsWith("ER-") || (p.startsWith("ER") && p.length() > 2 && Character.isDigit(p.charAt(2)))) {
                    return true;
                }
            }
        }
        return false;
    }

    private void enrichLinkedParentIcs(Collection<IbsInspectionDto> dtos) {
        if (dtos == null || dtos.isEmpty()) return;

        // Raw material calls (e.g. ER-123) have no parent inspection stage, so linked IC must strictly be null
        for (IbsInspectionDto dto : dtos) {
            if (dto != null && isRawMaterialCall(dto)) {
                dto.setIcLinkedForCurrentCall(null);
            }
        }

        Set<String> epCallNos = new HashSet<>();
        Set<String> efCallNos = new HashSet<>();
        Set<String> rpfCallNos = new HashSet<>();

        for (IbsInspectionDto dto : dtos) {
            if (dto == null || "C".equalsIgnoreCase(dto.getCallStatus()) || isRawMaterialCall(dto)) continue;
            String cNo = dto.getCallNumber() != null ? dto.getCallNumber().trim() : "";
            if (cNo.isBlank()) cNo = dto.getIcNumber() != null ? dto.getIcNumber().trim() : "";
            if (cNo.isBlank() || isRmCallString(cNo)) continue;

            String upper = cNo.toUpperCase();
            if (upper.startsWith("EP") || upper.contains("EP-") || upper.contains("/EP")) {
                epCallNos.add(cNo);
                if (cNo.contains("/")) {
                    for (String part : cNo.split("/")) {
                        if (part.trim().toUpperCase().startsWith("EP")) epCallNos.add(part.trim());
                    }
                }
            } else if (upper.startsWith("EF") || upper.contains("EF-") || upper.contains("/EF")) {
                efCallNos.add(cNo);
                if (cNo.contains("/")) {
                    for (String part : cNo.split("/")) {
                        if (part.trim().toUpperCase().startsWith("EF")) efCallNos.add(part.trim());
                    }
                }
            } else if (upper.startsWith("RPF") || upper.contains("RPF-") || upper.contains("/RPF")) {
                rpfCallNos.add(cNo);
                if (cNo.contains("/")) {
                    for (String part : cNo.split("/")) {
                        if (part.trim().toUpperCase().startsWith("RPF")) rpfCallNos.add(part.trim());
                    }
                }
            }
        }

        if (epCallNos.isEmpty() && efCallNos.isEmpty() && rpfCallNos.isEmpty()) return;

        Map<String, String> batchLinkedIcs = prefetchLinkedParentIcNumbers(epCallNos, efCallNos, rpfCallNos);
        if (batchLinkedIcs.isEmpty()) return;

        for (IbsInspectionDto dto : dtos) {
            if (dto == null || "C".equalsIgnoreCase(dto.getCallStatus())) continue;
            if (isRawMaterialCall(dto)) {
                dto.setIcLinkedForCurrentCall(null);
                continue;
            }
            String callNo = dto.getCallNumber() != null ? dto.getCallNumber().trim() : "";
            String icNo = dto.getIcNumber() != null ? dto.getIcNumber().trim() : "";
            if (isRmCallString(callNo) || isRmCallString(icNo)) {
                dto.setIcLinkedForCurrentCall(null);
                continue;
            }

            String linkedIc = batchLinkedIcs.get(callNo);
            if (linkedIc == null && !callNo.isEmpty()) linkedIc = batchLinkedIcs.get(callNo.toUpperCase());
            if (linkedIc == null && !icNo.isEmpty()) linkedIc = batchLinkedIcs.get(icNo);
            if (linkedIc == null && !icNo.isEmpty()) linkedIc = batchLinkedIcs.get(icNo.toUpperCase());

            if (linkedIc == null && callNo.contains("/")) {
                for (String part : callNo.split("/")) {
                    String p = part.trim();
                    if (!p.isEmpty()) {
                        linkedIc = batchLinkedIcs.get(p);
                        if (linkedIc == null) linkedIc = batchLinkedIcs.get(p.toUpperCase());
                        if (linkedIc != null) break;
                    }
                }
            }
            if (linkedIc == null && icNo.contains("/")) {
                for (String part : icNo.split("/")) {
                    String p = part.trim();
                    if (!p.isEmpty()) {
                        linkedIc = batchLinkedIcs.get(p);
                        if (linkedIc == null) linkedIc = batchLinkedIcs.get(p.toUpperCase());
                        if (linkedIc != null) break;
                    }
                }
            }

            if (linkedIc != null) {
                dto.setIcLinkedForCurrentCall(linkedIc);
            }
        }
    }

    private String resolveUploadedCallDocument(String callNumber) {
        if (callNumber == null || callNumber.trim().isEmpty()) {
            return null;
        }
        String clean = callNumber.trim();
        if (clean.toLowerCase().endsWith(".pdf")) {
            clean = clean.substring(0, clean.length() - 4).trim();
        }
        try {
            // 1. Priority 1: Check for Case Letter Document
            if (caseLetterDocumentRepository != null) {
                Optional<CaseLetterDocument> cldOpt =
                        caseLetterDocumentRepository.findActiveByCallNoVariants(clean, "ACTIVE");
                if (cldOpt.isPresent()) {
                    return "https://api.ritesqasarthi.com/sarthi-backend/api/case-letter/view/" + clean + ".pdf";
                }
            }
            // 2. Priority 2: Fallback to Uploaded IC Annexures
            if (icAnnexureDocumentRepository != null) {
                List<IcAnnexureDocument> annexures =
                        icAnnexureDocumentRepository.findActiveAnnexuresByCallVariants(clean);
                if (!annexures.isEmpty()) {
                    IcAnnexureDocument doc = annexures.get(0);
                    return "https://api.ritesqasarthi.com/sarthi-backend/api/ic-annexures/download/" + doc.getId();
                }
            }
            // 3. Priority 3: Fallback check in Azure container directly
            if (azureBlobStorageService != null) {
                String blobName = azureBlobStorageService.findBlobNameByCallToken(clean);
                if (blobName != null) {
                    return "https://api.ritesqasarthi.com/sarthi-backend/api/ic-annexures/stream?blobName="
                            + java.net.URLEncoder.encode(blobName, java.nio.charset.StandardCharsets.UTF_8);
                }
            }
        } catch (Exception e) {
            log.warn("Error resolving uploaded inspection document for call {}: {}", clean, e.getMessage());
        }
        return null;
    }

    private String resolveUploadedCallDocument(String callNumber, String icNumber) {
        String doc = resolveUploadedCallDocument(callNumber);
        if (doc != null) {
            return doc;
        }
        if (icNumber != null && !icNumber.trim().isEmpty() && !icNumber.trim().equalsIgnoreCase(callNumber)) {
            doc = resolveUploadedCallDocument(icNumber);
            if (doc != null) {
                return doc;
            }
        }
        // Fallback to Azure container search if still not found
        if (azureBlobStorageService != null) {
            String blobName = azureBlobStorageService.findBlobNameByCallToken(callNumber);
            if (blobName != null) {
                return "https://api.ritesqasarthi.com/sarthi-backend/api/ic-annexures/stream?blobName="
                        + java.net.URLEncoder.encode(blobName, java.nio.charset.StandardCharsets.UTF_8);
            }
            if (icNumber != null && !icNumber.trim().isEmpty()) {
                blobName = azureBlobStorageService.findBlobNameByCallToken(icNumber);
                if (blobName != null) {
                    return "https://api.ritesqasarthi.com/sarthi-backend/api/ic-annexures/stream?blobName="
                            + java.net.URLEncoder.encode(blobName, java.nio.charset.StandardCharsets.UTF_8);
                }
            }
        }
        return null;
    }

    private String extractCallToken(String raw, String prefix) {
        if (raw == null || raw.trim().isEmpty()) return null;
        String s = raw.trim();
        if (s.toLowerCase().endsWith(".pdf")) {
            s = s.substring(0, s.length() - 4).trim();
        }
        // If it looks like a full certificate number (e.g. W/ER-0915260001/SWR), preserve the whole certificate number!
        if (s.contains("/") && !s.startsWith("http://") && !s.startsWith("https://")) {
            String[] parts = s.split("/");
            if (parts.length >= 2 && prefix != null) {
                for (String p : parts) {
                    if (p.trim().toUpperCase().startsWith(prefix.toUpperCase()) && p.trim().length() >= 6) {
                        return s; // Preserves the full certificate number
                    }
                }
            }
        }
        if (s.contains("/") || s.contains("_")) {
            String[] tokens = s.split("[/_]");
            for (String t : tokens) {
                String trimmedT = t.trim();
                if (prefix != null && trimmedT.toUpperCase().startsWith(prefix.toUpperCase()) && trimmedT.length() >= 6) {
                    return trimmedT;
                }
            }
        }
        return s;
    }

    private Map<String, String> prefetchLinkedParentIcNumbers(Set<String> epCallNos, Set<String> efCallNos, Set<String> rpfCallNos) {
        Map<String, String> resultMap = new HashMap<>();
        Map<String, Set<String>> rawMap = new HashMap<>();
        Set<String> callsToResolveSet = new HashSet<>();

        // 1. ERC Process (EP) -> Raw Material IC (starts with ER)
        if (epCallNos != null && !epCallNos.isEmpty() && processRmIcMappingRepository != null) {
            Set<String> cleanEpCallNos = epCallNos.stream()
                    .filter(c -> !isRmCallString(c))
                    .collect(Collectors.toSet());
            if (!cleanEpCallNos.isEmpty()) {
                try {
                    List<Object[]> rows = processRmIcMappingRepository.findBatchRmIcByProcessCallNos(cleanEpCallNos);
                if (rows != null) {
                    for (Object[] r : rows) {
                        if (r == null || r.length < 2) continue;
                        String processCall = safeString(r[0]);
                        String rmVal = safeString(r[1]);
                        if (processCall != null && rmVal != null && !rmVal.isBlank()) {
                            for (String p : rmVal.split("[,;]+")) {
                                String token = extractCallToken(p, "ER");
                                if (token != null && !token.isEmpty()) {
                                    rawMap.computeIfAbsent(processCall.trim(), k -> new LinkedHashSet<>()).add(token);
                                    if (!token.contains("/")) {
                                        callsToResolveSet.add(token);
                                    }
                                }
                            }
                        }
                    }
                }
            } catch (Exception e) {
                log.warn("Error in batch prefetching EP linked RM IC numbers: {}", e.getMessage());
            }

                // Diverted call fallback for any EP calls that did not resolve an RM IC
                resolveDivertedEpCalls(cleanEpCallNos, rawMap, callsToResolveSet);
            }
        }

        // 2. ERC Final (EF) -> Process IC (starts with EP)
        if (efCallNos != null && !efCallNos.isEmpty() && finalProcessIcMappingRepository != null) {
            try {
                List<Object[]> rows = finalProcessIcMappingRepository.findBatchProcessIcByFinalCallNos(efCallNos);
                if (rows != null) {
                    for (Object[] r : rows) {
                        if (r == null || r.length < 2) continue;
                        String finalCall = safeString(r[0]);
                        String procVal = safeString(r[1]);
                        if (finalCall != null && procVal != null && !procVal.isBlank()) {
                            for (String p : procVal.split("[,;]+")) {
                                String token = extractCallToken(p, "EP");
                                if (token != null && !token.isEmpty()) {
                                    rawMap.computeIfAbsent(finalCall.trim(), k -> new LinkedHashSet<>()).add(token);
                                    if (!token.contains("/")) {
                                        callsToResolveSet.add(token);
                                    }
                                }
                            }
                        }
                    }
                }
            } catch (Exception e) {
                log.warn("Error in batch prefetching EF linked Process IC numbers: {}", e.getMessage());
            }

            // Diverted call fallback for any EF calls that did not resolve a Process IC
            resolveDivertedEfCalls(efCallNos, rawMap, callsToResolveSet);
        }

        // 3. Railpad Final (RPF) -> Process IC (starts with RPP)
        if (rpfCallNos != null && !rpfCallNos.isEmpty() && railInspectionCallRepository != null) {
            try {
                List<Object[]> rows = railInspectionCallRepository.findBatchProcessIcByRailpadCallNos(rpfCallNos);
                if (rows != null) {
                    for (Object[] r : rows) {
                        if (r == null || r.length < 2) continue;
                        String rpfCall = safeString(r[0]);
                        String procVal = safeString(r[1]);
                        if (rpfCall != null && procVal != null && !procVal.isBlank()) {
                            for (String p : procVal.split("[,;]+")) {
                                String token = extractCallToken(p, "RPP");
                                if (token != null && !token.isEmpty()) {
                                    rawMap.computeIfAbsent(rpfCall.trim(), k -> new LinkedHashSet<>()).add(token);
                                    if (!token.contains("/")) {
                                        callsToResolveSet.add(token);
                                    }
                                }
                            }
                        }
                    }
                }
            } catch (Exception e) {
                log.warn("Error in batch prefetching RPF linked Process IC numbers: {}", e.getMessage());
            }
        }

        // 4. Batch resolve any plain call numbers to their full Certificate numbers
        Map<String, String> certLookupMap = new HashMap<>();
        if (!callsToResolveSet.isEmpty()) {
            if (inspectionCompleteDetailsRepository != null) {
                try {
                    List<Object[]> certRows = inspectionCompleteDetailsRepository.findCertificateNosByCallNos(new ArrayList<>(callsToResolveSet));
                    if (certRows != null) {
                        for (Object[] cr : certRows) {
                            if (cr != null && cr.length >= 2 && cr[0] != null && cr[1] != null) {
                                String cn = safeString(cr[0]).trim();
                                String cert = safeString(cr[1]).trim();
                                if (!cn.isEmpty() && !cert.isEmpty()) {
                                    certLookupMap.put(cn.toUpperCase(), cert);
                                    certLookupMap.put(cn, cert);
                                }
                            }
                        }
                    }
                } catch (Exception e) {
                    log.warn("Error resolving certificate numbers for calls: {}", e.getMessage());
                }
            }
            if (railInspectionCompleteDetailsRepository != null) {
                try {
                    List<Object[]> railCertRows = railInspectionCompleteDetailsRepository.findCertificateNosByCallNos(callsToResolveSet);
                    if (railCertRows != null) {
                        for (Object[] cr : railCertRows) {
                            if (cr != null && cr.length >= 2 && cr[0] != null && cr[1] != null) {
                                String cn = safeString(cr[0]).trim();
                                String cert = safeString(cr[1]).trim();
                                if (!cn.isEmpty() && !cert.isEmpty()) {
                                    certLookupMap.put(cn.toUpperCase(), cert);
                                    certLookupMap.put(cn, cert);
                                }
                            }
                        }
                    }
                } catch (Exception e) {
                    log.warn("Error resolving railpad certificate numbers: {}", e.getMessage());
                }
            }

            // Fallback lookup via RmIcEditRepository and ProcessIcEditRepository for remaining plain call numbers
            for (String cn : callsToResolveSet) {
                if (cn == null || cn.isBlank() || certLookupMap.containsKey(cn.toUpperCase())) continue;
                String upperCn = cn.toUpperCase();
                if (upperCn.startsWith("ER") && rmIcEditRepository != null) {
                    try {
                        Optional<RmIcEdit> rieOpt = rmIcEditRepository.findByIcNumber(cn.trim());
                        if (rieOpt.isPresent() && rieOpt.get().getIcNumber() != null && !rieOpt.get().getIcNumber().isBlank()) {
                            String cert = rieOpt.get().getIcNumber().trim();
                            certLookupMap.put(cn.toUpperCase(), cert);
                            certLookupMap.put(cn, cert);
                        }
                    } catch (Exception ignored) {}
                } else if (upperCn.startsWith("EP") && processIcEditRepository != null) {
                    try {
                        Optional<ProcessIcEdit> pieOpt = processIcEditRepository.findByIcNumber(cn.trim());
                        if (pieOpt.isPresent() && pieOpt.get().getIcNumber() != null && !pieOpt.get().getIcNumber().isBlank()) {
                            String cert = pieOpt.get().getIcNumber().trim();
                            certLookupMap.put(cn.toUpperCase(), cert);
                            certLookupMap.put(cn, cert);
                        }
                    } catch (Exception ignored) {}
                }
            }
        }

        // 5. Populate final result map with full certificate numbers
        for (Map.Entry<String, Set<String>> entry : rawMap.entrySet()) {
            if (isRmCallString(entry.getKey())) continue;
            Set<String> resolvedCertNums = new LinkedHashSet<>();
            for (String token : entry.getValue()) {
                if (token.contains("/")) {
                    resolvedCertNums.add(token);
                } else if (certLookupMap.containsKey(token.toUpperCase())) {
                    resolvedCertNums.add(certLookupMap.get(token.toUpperCase()));
                } else {
                    resolvedCertNums.add(token);
                }
            }
            if (!resolvedCertNums.isEmpty()) {
                String joined = String.join(", ", resolvedCertNums);
                resultMap.put(entry.getKey(), joined);
                if (entry.getKey().contains("/")) {
                    for (String part : entry.getKey().split("/")) {
                        resultMap.putIfAbsent(part.trim(), joined);
                    }
                }
            }
        }

        return resultMap;
    }

    private String extractCallNo(String certNo) {
        if (certNo == null || certNo.trim().isEmpty()) {
            return null;
        }
        String clean = certNo.trim();
        if (clean.toLowerCase().endsWith(".pdf")) {
            clean = clean.substring(0, clean.length() - 4).trim();
        }
        try {
            if (inspectionCompleteDetailsRepository != null) {
                Optional<InspectionCompleteDetails> icd = inspectionCompleteDetailsRepository.findByCertificateNo(clean);
                if (icd.isPresent() && icd.get().getCallNo() != null && !icd.get().getCallNo().trim().isEmpty()) {
                    return icd.get().getCallNo().trim();
                }
            }
        } catch (Exception e) {
            log.debug("Could not find call number for certificate {}: {}", clean, e.getMessage());
        }
        if (clean.contains("/")) {
            String[] parts = clean.split("/");
            for (String p : parts) {
                String trimmed = p.trim();
                String upper = trimmed.toUpperCase();
                if (upper.startsWith("EP-") || upper.startsWith("ER-") || upper.startsWith("EF-")
                        || upper.startsWith("RPP-") || upper.startsWith("RPF-") || upper.startsWith("SF-")) {
                    return trimmed;
                }
            }
            if (parts.length >= 2 && !parts[1].trim().isEmpty()) {
                return parts[1].trim();
            }
        }
        return clean;
    }

    private void resolveDivertedEpCalls(Set<String> epCallNos, Map<String, Set<String>> rawMap, Set<String> callsToResolveSet) {
        if (inspectionCallsRepository == null || epCallNos == null || epCallNos.isEmpty()) return;

        try {
            List<String> unresolvedCalls = epCallNos.stream()
                    .filter(c -> c != null && !c.isBlank() && !isRmCallString(c) && !rawMap.containsKey(c.trim()) && !rawMap.containsKey(c.trim().toUpperCase()))
                    .map(String::trim)
                    .distinct()
                    .collect(Collectors.toList());

            if (unresolvedCalls.isEmpty()) return;

            List<InspectionCall> calls = inspectionCallsRepository.findByIcNumberIn(unresolvedCalls);
            if (calls == null || calls.isEmpty()) return;

            for (InspectionCall call : calls) {
                if (call == null) continue;
                boolean isDiverted = Boolean.TRUE.equals(call.getIsDivertedMaterial())
                        || (call.getSourceIcNo() != null && !call.getSourceIcNo().isBlank())
                        || "DIVERTED_PASSED".equalsIgnoreCase(call.getCallCategory());

                if (!isDiverted) continue;

                String callNo = call.getIcNumber() != null ? call.getIcNumber().trim() : "";
                String sourceIcNo = call.getSourceIcNo() != null ? call.getSourceIcNo().trim() : "";
                if (callNo.isEmpty() || sourceIcNo.isEmpty()) continue;

                log.info("[IBS-FETCH] Resolving linked RM IC for diverted call {} with sourceIcNo={}", callNo, sourceIcNo);

                // Case 1: sourceIcNo itself is already an RM IC (starts with ER or contains ER)
                String upperSource = sourceIcNo.toUpperCase();
                if (upperSource.contains("ER-") || upperSource.contains("/ER") || upperSource.startsWith("ER")) {
                    String token = extractCallToken(sourceIcNo, "ER");
                    if (token != null && !token.isEmpty()) {
                        rawMap.computeIfAbsent(callNo, k -> new LinkedHashSet<>()).add(token);
                        if (!token.contains("/")) {
                            callsToResolveSet.add(token);
                        }
                        continue;
                    }
                }

                // Case 2: sourceIcNo is a Process IC -> find RM IC linked to that source Process IC
                Set<String> foundRmIcs = findLinkedRmIcsForProcessSource(sourceIcNo, 0);
                if (foundRmIcs != null && !foundRmIcs.isEmpty()) {
                    for (String rmVal : foundRmIcs) {
                        String token = extractCallToken(rmVal, "ER");
                        if (token != null && !token.isEmpty()) {
                            rawMap.computeIfAbsent(callNo, k -> new LinkedHashSet<>()).add(token);
                            if (!token.contains("/")) {
                                callsToResolveSet.add(token);
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.warn("Error resolving diverted EP calls linked RM IC: {}", e.getMessage(), e);
        }
    }

    private Set<String> findLinkedRmIcsForProcessSource(String sourceIcNo, int depth) {
        Set<String> result = new LinkedHashSet<>();
        if (sourceIcNo == null || sourceIcNo.isBlank() || depth > 5) {
            return result;
        }

        String sourceCallNo = extractCallNo(sourceIcNo);
        if (sourceCallNo == null || sourceCallNo.isBlank()) {
            sourceCallNo = sourceIcNo.trim();
        }

        // 1. Check process_rm_ic_mapping
        if (processRmIcMappingRepository != null) {
            List<String> candidates = new ArrayList<>();
            candidates.add(sourceCallNo);
            if (!sourceCallNo.equalsIgnoreCase(sourceIcNo.trim())) {
                candidates.add(sourceIcNo.trim());
            }
            try {
                List<Object[]> rows = processRmIcMappingRepository.findBatchRmIcByProcessCallNos(candidates);
                if (rows != null) {
                    for (Object[] r : rows) {
                        if (r == null || r.length < 2) continue;
                        String rmVal = safeString(r[1]);
                        if (rmVal != null && !rmVal.isBlank()) {
                            for (String p : rmVal.split("[,;]+")) {
                                if (!p.trim().isEmpty()) {
                                    result.add(p.trim());
                                }
                            }
                        }
                    }
                }
            } catch (Exception e) {
                log.warn("Error querying processRmIcMappingRepository for source {}: {}", sourceCallNo, e.getMessage());
            }
        }

        // 2. Check process_inspection_details
        if (result.isEmpty() && inspectionCallsRepository != null && processInspectionDetailsRepository != null) {
            try {
                Optional<InspectionCall> srcCallOpt = inspectionCallsRepository.findByIcNumber(sourceCallNo);
                if (srcCallOpt.isPresent()) {
                    InspectionCall srcCall = srcCallOpt.get();
                    List<ProcessInspectionDetails> pids = processInspectionDetailsRepository.findByIcId(
                            Long.valueOf(srcCall.getId()));
                    if (pids != null) {
                        for (ProcessInspectionDetails pid : pids) {
                            String rmVal = pid.getRmIcNumber();
                            if (rmVal != null && !rmVal.isBlank()) {
                                for (String p : rmVal.split("[,;]+")) {
                                    if (!p.trim().isEmpty()) {
                                        result.add(p.trim());
                                    }
                                }
                            }
                        }
                    }

                    // 3. Diversion chain recursion
                    if (result.isEmpty() && (Boolean.TRUE.equals(srcCall.getIsDivertedMaterial()) || srcCall.getSourceIcNo() != null)) {
                        String parentSource = srcCall.getSourceIcNo();
                        if (parentSource != null && !parentSource.isBlank() && !parentSource.equalsIgnoreCase(sourceIcNo)) {
                            result.addAll(findLinkedRmIcsForProcessSource(parentSource, depth + 1));
                        }
                    }
                }
            } catch (Exception e) {
                log.warn("Error checking processInspectionDetailsRepository for source {}: {}", sourceCallNo, e.getMessage());
            }
        }

        return result;
    }

    private void resolveDivertedEfCalls(Set<String> efCallNos, Map<String, Set<String>> rawMap, Set<String> callsToResolveSet) {
        if (inspectionCallsRepository == null || efCallNos == null || efCallNos.isEmpty()) return;

        try {
            List<String> unresolvedCalls = efCallNos.stream()
                    .filter(c -> c != null && !c.isBlank() && !rawMap.containsKey(c.trim()) && !rawMap.containsKey(c.trim().toUpperCase()))
                    .map(String::trim)
                    .distinct()
                    .collect(Collectors.toList());

            if (unresolvedCalls.isEmpty()) return;

            List<InspectionCall> calls = inspectionCallsRepository.findByIcNumberIn(unresolvedCalls);
            if (calls == null || calls.isEmpty()) return;

            for (InspectionCall call : calls) {
                if (call == null) continue;
                boolean isDiverted = Boolean.TRUE.equals(call.getIsDivertedMaterial())
                        || (call.getSourceIcNo() != null && !call.getSourceIcNo().isBlank())
                        || "DIVERTED_PASSED".equalsIgnoreCase(call.getCallCategory());

                if (!isDiverted) continue;

                String callNo = call.getIcNumber() != null ? call.getIcNumber().trim() : "";
                String sourceIcNo = call.getSourceIcNo() != null ? call.getSourceIcNo().trim() : "";
                if (callNo.isEmpty() || sourceIcNo.isEmpty()) continue;

                log.info("[IBS-FETCH] Resolving linked Process IC for diverted Final call {} with sourceIcNo={}", callNo, sourceIcNo);

                String upperSource = sourceIcNo.toUpperCase();
                if (upperSource.contains("EP-") || upperSource.contains("/EP") || upperSource.startsWith("EP")) {
                    String token = extractCallToken(sourceIcNo, "EP");
                    if (token != null && !token.isEmpty()) {
                        rawMap.computeIfAbsent(callNo, k -> new LinkedHashSet<>()).add(token);
                        if (!token.contains("/")) {
                            callsToResolveSet.add(token);
                        }
                        continue;
                    }
                }

                Set<String> foundProcIcs = findLinkedProcessIcsForFinalSource(sourceIcNo, 0);
                if (foundProcIcs != null && !foundProcIcs.isEmpty()) {
                    for (String procVal : foundProcIcs) {
                        String token = extractCallToken(procVal, "EP");
                        if (token != null && !token.isEmpty()) {
                            rawMap.computeIfAbsent(callNo, k -> new LinkedHashSet<>()).add(token);
                            if (!token.contains("/")) {
                                callsToResolveSet.add(token);
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.warn("Error resolving diverted EF calls linked Process IC: {}", e.getMessage(), e);
        }
    }

    private Set<String> findLinkedProcessIcsForFinalSource(String sourceIcNo, int depth) {
        Set<String> result = new LinkedHashSet<>();
        if (sourceIcNo == null || sourceIcNo.isBlank() || depth > 5) {
            return result;
        }

        String sourceCallNo = extractCallNo(sourceIcNo);
        if (sourceCallNo == null || sourceCallNo.isBlank()) {
            sourceCallNo = sourceIcNo.trim();
        }

        if (finalProcessIcMappingRepository != null) {
            List<String> candidates = new ArrayList<>();
            candidates.add(sourceCallNo);
            if (!sourceCallNo.equalsIgnoreCase(sourceIcNo.trim())) {
                candidates.add(sourceIcNo.trim());
            }
            try {
                List<Object[]> rows = finalProcessIcMappingRepository.findBatchProcessIcByFinalCallNos(candidates);
                if (rows != null) {
                    for (Object[] r : rows) {
                        if (r == null || r.length < 2) continue;
                        String procVal = safeString(r[1]);
                        if (procVal != null && !procVal.isBlank()) {
                            for (String p : procVal.split("[,;]+")) {
                                if (!p.trim().isEmpty()) {
                                    result.add(p.trim());
                                }
                            }
                        }
                    }
                }
            } catch (Exception e) {
                log.warn("Error querying finalProcessIcMappingRepository for source {}: {}", sourceCallNo, e.getMessage());
            }
        }

        if (result.isEmpty() && inspectionCallsRepository != null && finalInspectionDetailsRepository != null) {
            try {
                Optional<InspectionCall> srcCallOpt = inspectionCallsRepository.findByIcNumber(sourceCallNo);
                if (srcCallOpt.isPresent()) {
                    InspectionCall srcCall = srcCallOpt.get();
                    Optional<FinalInspectionDetails> fidOpt = finalInspectionDetailsRepository.findByIcId(
                            Long.valueOf(srcCall.getId()));
                    if (fidOpt.isPresent()) {
                        String procVal = fidOpt.get().getProcessIcNumber();
                        if (procVal != null && !procVal.isBlank()) {
                            for (String p : procVal.split("[,;]+")) {
                                if (!p.trim().isEmpty()) {
                                    result.add(p.trim());
                                }
                            }
                        }
                    }

                    if (result.isEmpty() && (Boolean.TRUE.equals(srcCall.getIsDivertedMaterial()) || srcCall.getSourceIcNo() != null)) {
                        String parentSource = srcCall.getSourceIcNo();
                        if (parentSource != null && !parentSource.isBlank() && !parentSource.equalsIgnoreCase(sourceIcNo)) {
                            result.addAll(findLinkedProcessIcsForFinalSource(parentSource, depth + 1));
                        }
                    }
                }
            } catch (Exception e) {
                log.warn("Error checking finalInspectionDetailsRepository for source {}: {}", sourceCallNo, e.getMessage());
            }
        }

        return result;
    }

    private void enrichDivertedCallsData(Collection<IbsInspectionDto> dtos) {
        if (dtos == null || dtos.isEmpty()) return;

        List<IbsInspectionDto> candidates = new ArrayList<>();
        Set<String> callNos = new HashSet<>();
        Map<String, IbsInspectionDto> callDtoMap = new HashMap<>();

        for (IbsInspectionDto dto : dtos) {
            if (dto == null) continue;
            String callNo = dto.getCallNumber() != null ? dto.getCallNumber().trim() : "";
            if (callNo.isEmpty() && dto.getIcNumber() != null) {
                callNo = extractCallNo(dto.getIcNumber());
            }
            if (callNo == null || callNo.isEmpty()) continue;

            callDtoMap.put(callNo, dto);
            callDtoMap.put(callNo.toUpperCase(), dto);
            candidates.add(dto);
            callNos.add(callNo);
            if (callNo.contains("/")) {
                for (String p : callNo.split("/")) {
                    if (!p.trim().isEmpty()) {
                        callNos.add(p.trim());
                        callDtoMap.putIfAbsent(p.trim(), dto);
                        callDtoMap.putIfAbsent(p.trim().toUpperCase(), dto);
                    }
                }
            }
        }

        if (callNos.isEmpty()) return;

        List<InspectionCall> icList = Collections.emptyList();
        if (inspectionCallsRepository != null) {
            try {
                icList = inspectionCallsRepository.findByIcNumberIn(new ArrayList<>(callNos));
            } catch (Exception e) {
                log.warn("Error fetching inspection calls for diverted calls: {}", e.getMessage());
            }
        }
        Map<String, InspectionCall> icMap = new HashMap<>();
        if (icList != null) {
            for (InspectionCall ic : icList) {
                if (ic != null && ic.getIcNumber() != null) {
                    icMap.put(ic.getIcNumber().trim(), ic);
                    icMap.put(ic.getIcNumber().trim().toUpperCase(), ic);
                }
            }
        }

        Map<String, ErcDiversionRequest> divMap = new HashMap<>();
        if (ercDiversionRequestRepository != null) {
            for (String cNo : callNos) {
                try {
                    Optional<ErcDiversionRequest> reqOpt = ercDiversionRequestRepository.findByRequestNo(cNo);
                    reqOpt.ifPresent(req -> {
                        divMap.put(cNo, req);
                        divMap.put(cNo.toUpperCase(), req);
                    });
                } catch (Exception ignored) {}
            }
        }

        for (IbsInspectionDto dto : candidates) {
            String callNo = dto.getCallNumber() != null ? dto.getCallNumber().trim() : "";
            if (callNo.isEmpty() && dto.getIcNumber() != null) {
                callNo = extractCallNo(dto.getIcNumber());
            }
            if (callNo == null || callNo.isEmpty()) continue;

            InspectionCall call = icMap.get(callNo);
            if (call == null) call = icMap.get(callNo.toUpperCase());
            if (call == null && inspectionCallsRepository != null) {
                try {
                    call = inspectionCallsRepository.findByIcNumber(callNo).orElse(null);
                    if (call != null) icMap.put(callNo, call);
                } catch (Exception ignored) {}
            }

            ErcDiversionRequest divReq = divMap.get(callNo);
            if (divReq == null) divReq = divMap.get(callNo.toUpperCase());
            if (divReq == null && call != null && call.getDiversionRequestNo() != null && !call.getDiversionRequestNo().isBlank()) {
                String dNo = call.getDiversionRequestNo().trim();
                divReq = divMap.get(dNo);
                if (divReq == null && ercDiversionRequestRepository != null) {
                    try {
                        divReq = ercDiversionRequestRepository.findByRequestNo(dNo).orElse(null);
                        if (divReq != null) divMap.put(dNo, divReq);
                    } catch (Exception ignored) {}
                }
            }

            boolean isDiverted = (divReq != null)
                    || (call != null && (Boolean.TRUE.equals(call.getIsDivertedMaterial())
                        || (call.getSourceIcNo() != null && !call.getSourceIcNo().isBlank())
                        || (call.getDiversionRequestNo() != null && !call.getDiversionRequestNo().isBlank())
                        || "DIVERTED_PASSED".equalsIgnoreCase(call.getCallCategory())));

            if (!isDiverted) continue;

            log.info("[IBS-FETCH] Enriching diverted call {}", callNo);

            // Determine Source IC Call Number
            String srcCallNo = null;
            if (divReq != null && divReq.getSourceCallNo() != null && !divReq.getSourceCallNo().isBlank()) {
                srcCallNo = divReq.getSourceCallNo().trim();
            } else if (call != null && call.getSourceIcNo() != null && !call.getSourceIcNo().isBlank()) {
                srcCallNo = extractCallNo(call.getSourceIcNo());
            } else if (divReq != null && divReq.getSourceIcNo() != null && !divReq.getSourceIcNo().isBlank()) {
                srcCallNo = extractCallNo(divReq.getSourceIcNo());
            }
            if (srcCallNo != null) {
                while (srcCallNo.startsWith(":")) srcCallNo = srcCallNo.substring(1).trim();
            }

            // A. Enrich Quantities (Fetch Offered, Passed, Rejected from Source IC)
            boolean quantitiesResolved = false;
            if (srcCallNo != null && !srcCallNo.isBlank()) {
                // Check if source call is already in memory DTOs
                IbsInspectionDto srcDto = callDtoMap.get(srcCallNo);
                if (srcDto == null) srcDto = callDtoMap.get(srcCallNo.toUpperCase());
                if (srcDto != null && srcDto.getQuantityOffered() != null && srcDto.getQuantityOffered() > 0) {
                    dto.setQuantityOffered(srcDto.getQuantityOffered());
                    dto.setQuantityPassed(srcDto.getQuantityPassed());
                    dto.setQuantityRejected(srcDto.getQuantityRejected() != null ? srcDto.getQuantityRejected() : 0.0);
                    quantitiesResolved = true;
                    log.info("[IBS-FETCH] Set source IC {} quantities from existing DTO for diverted call {}: offered={}, passed={}, rejected={}",
                            srcCallNo, callNo, dto.getQuantityOffered(), dto.getQuantityPassed(), dto.getQuantityRejected());
                } else {
                    double[] srcQtys = fetchSourceIcQuantities(srcCallNo);
                    if (srcQtys != null && (srcQtys[0] > 0 || srcQtys[1] > 0 || srcQtys[2] > 0)) {
                        dto.setQuantityOffered(srcQtys[0]);
                        dto.setQuantityPassed(srcQtys[1]);
                        dto.setQuantityRejected(srcQtys[2]);
                        quantitiesResolved = true;
                        log.info("[IBS-FETCH] Set source IC {} quantities for diverted call {}: offered={}, passed={}, rejected={}",
                                srcCallNo, callNo, srcQtys[0], srcQtys[1], srcQtys[2]);
                    }
                }
            }

            // Fallback: If source IC quantities were not found, use diversion quantity
            if (!quantitiesResolved && (dto.getQuantityOffered() == null || dto.getQuantityOffered() == 0.0)) {
                Double divertedQty = null;
                if (divReq != null && divReq.getTotalDiversionQty() != null) {
                    divertedQty = divReq.getTotalDiversionQty().doubleValue();
                }
                if (divertedQty == null && ercDiversionCallAllocationRepository != null) {
                    try {
                        List<ErcDiversionCallAllocation> allocs = ercDiversionCallAllocationRepository.findByCallNo(callNo);
                        if (allocs != null && !allocs.isEmpty()) {
                            double sum = allocs.stream()
                                    .map(ErcDiversionCallAllocation::getAllocatedQty)
                                    .filter(Objects::nonNull)
                                    .mapToDouble(BigDecimal::doubleValue)
                                    .sum();
                            if (sum > 0) divertedQty = sum;
                        }
                    } catch (Exception ignored) {}
                }
                if (divertedQty != null && divertedQty > 0) {
                    dto.setQuantityOffered(divertedQty);
                    dto.setQuantityPassed(divertedQty);
                    dto.setQuantityRejected(0.0);
                }
            }

            // B. Enrich Place of Inspection
            String poi = dto.getPlaceOfInspection();
            if (poi != null) {
                while (poi.startsWith(":")) poi = poi.substring(1).trim();
            }
            boolean isCleanPoi = poi != null && !poi.trim().isEmpty() && !poi.contains(",") && poi.trim().length() <= 20;
            if (!isCleanPoi) {
                poi = null;
                if (call != null && call.getPlaceOfInspection() != null && !call.getPlaceOfInspection().isBlank()) {
                    String candidate = call.getPlaceOfInspection().trim();
                    while (candidate.startsWith(":")) candidate = candidate.substring(1).trim();
                    if (!candidate.contains(",") && candidate.length() <= 20) {
                        poi = candidate;
                    }
                }
                if ((poi == null || poi.isEmpty()) && srcCallNo != null && !srcCallNo.isEmpty()) {
                    InspectionCall srcCall = icMap.get(srcCallNo);
                    if (srcCall == null && inspectionCallsRepository != null) {
                        try {
                            srcCall = inspectionCallsRepository.findByIcNumber(srcCallNo).orElse(null);
                        } catch (Exception ignored) {}
                    }
                    if (srcCall != null && srcCall.getPlaceOfInspection() != null && !srcCall.getPlaceOfInspection().isBlank()) {
                        poi = srcCall.getPlaceOfInspection().trim();
                    }
                }
                if ((poi == null || poi.isEmpty()) && pincodePoIMappingRepository != null) {
                    String compName = (call != null && call.getCompanyName() != null) ? call.getCompanyName()
                            : (divReq != null ? divReq.getCompanyName() : null);
                    String unitName = (call != null && call.getUnitName() != null) ? call.getUnitName()
                            : (divReq != null ? divReq.getPlantName() : null);
                    if (compName != null && unitName != null) {
                        try {
                            var m = pincodePoIMappingRepository.findByCompanyNameAndUnitName(compName, unitName);
                            if (m.isPresent() && m.get().getPoiCode() != null && !m.get().getPoiCode().isBlank()) {
                                poi = m.get().getPoiCode().trim();
                            }
                        } catch (Exception ignored) {}
                    }
                    if ((poi == null || poi.isEmpty())) {
                        String vCode = (call != null && call.getVendorId() != null) ? call.getVendorId()
                                : (divReq != null ? divReq.getVendorCode() : null);
                        if (vCode != null && !vCode.isBlank()) {
                            try {
                                var list = pincodePoIMappingRepository.findByVendorCodeWithColonHandling(vCode);
                                if (list != null && !list.isEmpty() && list.get(0).getPoiCode() != null) {
                                    poi = list.get(0).getPoiCode().trim();
                                }
                            } catch (Exception ignored) {}
                        }
                    }
                }

                if (poi != null && !poi.isEmpty()) {
                    while (poi.startsWith(":")) poi = poi.substring(1).trim();
                    dto.setPlaceOfInspection(poi);
                    if (call != null && (call.getPlaceOfInspection() == null || call.getPlaceOfInspection().isBlank())) {
                        call.setPlaceOfInspection(poi);
                        try {
                            inspectionCallsRepository.save(call);
                            log.info("[IBS-FETCH] Backfilled placeOfInspection={} for call {}", poi, callNo);
                        } catch (Exception e) {
                            log.warn("Could not backfill placeOfInspection on call {}: {}", callNo, e.getMessage());
                        }
                    }
                }
            }

            // C. Enrich IBS Manufactured Code
            if (dto.getIbsManufacturedCode() == null || dto.getIbsManufacturedCode().trim().isEmpty()) {
                String poiForIbs = dto.getPlaceOfInspection();
                if (poiForIbs != null && !poiForIbs.isBlank() && sarthiIbsPoiMappingRepository != null) {
                    try {
                        var ibsOpt = sarthiIbsPoiMappingRepository.findBestMatch(poiForIbs, "erc");
                        if (ibsOpt.isPresent() && ibsOpt.get().getIbsVendorCode() != null && !ibsOpt.get().getIbsVendorCode().isBlank()) {
                            dto.setIbsManufacturedCode(ibsOpt.get().getIbsVendorCode().trim());
                        } else {
                            var fallbackOpt = sarthiIbsPoiMappingRepository.findBestMatch(poiForIbs, null);
                            if (fallbackOpt.isPresent() && fallbackOpt.get().getIbsVendorCode() != null && !fallbackOpt.get().getIbsVendorCode().isBlank()) {
                                dto.setIbsManufacturedCode(fallbackOpt.get().getIbsVendorCode().trim());
                            } else {
                                dto.setIbsManufacturedCode(poiForIbs);
                            }
                        }
                    } catch (Exception e) {
                        log.warn("Error looking up IBS manufactured code for POI {}: {}", poiForIbs, e.getMessage());
                    }
                }
            }
        }
    }

    private double[] fetchSourceIcQuantities(String srcCallNo) {
        if (srcCallNo == null || srcCallNo.trim().isEmpty()) {
            return null;
        }
        String cleanSrc = extractCallNo(srcCallNo);
        if (cleanSrc == null || cleanSrc.trim().isEmpty()) {
            cleanSrc = srcCallNo.trim();
        }
        cleanSrc = cleanSrc.trim();
        while (cleanSrc.startsWith(":")) cleanSrc = cleanSrc.substring(1).trim();

        List<String> searchNos = new ArrayList<>();
        searchNos.add(cleanSrc);
        if (!searchNos.contains(cleanSrc.toUpperCase())) searchNos.add(cleanSrc.toUpperCase());
        if (cleanSrc.contains("/")) {
            for (String p : cleanSrc.split("/")) {
                String pt = p.trim();
                if (!pt.isEmpty() && !searchNos.contains(pt)) searchNos.add(pt);
                if (!pt.isEmpty() && !searchNos.contains(pt.toUpperCase())) searchNos.add(pt.toUpperCase());
            }
        }

        String upperSrc = cleanSrc.toUpperCase();

        try {
            // 1. ERC Process Call (EP-)
            if (upperSrc.startsWith("EP") || upperSrc.contains("EP-") || upperSrc.contains("/EP")) {
                if (processLineFinalResultRepository != null) {
                    try {
                        List<Object[]> rows = processLineFinalResultRepository.getProcessCompletedInspectionCalls(searchNos);
                        if (rows != null && !rows.isEmpty()) {
                            for (Object[] r : rows) {
                                Double off = safeQuantity(r[11]);
                                Double pass = safeQuantity(r[12]);
                                Double rej = safeQuantity(r[13]);
                                if (off > 0 || pass > 0 || rej > 0) {
                                    log.info("[IBS-FETCH] Resolved source IC {} quantities from getProcessCompletedInspectionCalls: offered={}, passed={}, rejected={}",
                                            cleanSrc, off, pass, rej);
                                    return new double[]{off, pass, rej};
                                }
                            }
                        }
                    } catch (Exception e) {
                        log.debug("Error in getProcessCompletedInspectionCalls for source {}: {}", cleanSrc, e.getMessage());
                    }

                    try {
                        List<ProcessLineFinalResult> plResults = Collections.emptyList();
                        for (String sNo : searchNos) {
                            plResults = processLineFinalResultRepository.findByInspectionCallNo(sNo);
                            if (plResults != null && !plResults.isEmpty()) break;
                        }
                        if (plResults != null && !plResults.isEmpty()) {
                            double totalOffered = 0.0;
                            double totalPassed = 0.0;
                            double totalRejected = 0.0;
                            for (ProcessLineFinalResult pl : plResults) {
                                double off = pl.getTotalManufactured() != null && pl.getTotalManufactured() > 0
                                        ? pl.getTotalManufactured().doubleValue()
                                        : (pl.getOfferedQty() != null ? pl.getOfferedQty().doubleValue() : 0.0);
                                double rej = pl.getTotalRejected() != null ? pl.getTotalRejected().doubleValue() : 0.0;
                                double pass = pl.getTotalAccepted() != null && pl.getTotalAccepted() > 0
                                        ? pl.getTotalAccepted().doubleValue()
                                        : Math.max(0.0, off - rej);
                                totalOffered += off;
                                totalPassed += pass;
                                totalRejected += rej;
                            }
                            if (totalOffered > 0 || totalPassed > 0 || totalRejected > 0) {
                                log.info("[IBS-FETCH] Resolved source IC {} quantities from findByInspectionCallNo: offered={}, passed={}, rejected={}",
                                        cleanSrc, totalOffered, totalPassed, totalRejected);
                                return new double[]{totalOffered, totalPassed, totalRejected};
                            }
                        }
                    } catch (Exception e) {
                        log.debug("Error in processLineFinalResultRepository.findByInspectionCallNo for {}: {}", cleanSrc, e.getMessage());
                    }

                    if (inspectionCallsRepository != null && processInspectionDetailsRepository != null) {
                        try {
                            InspectionCall sc = null;
                            for (String sNo : searchNos) {
                                sc = inspectionCallsRepository.findByIcNumber(sNo).orElse(null);
                                if (sc != null) break;
                            }
                            if (sc != null) {
                                List<ProcessInspectionDetails> pids = processInspectionDetailsRepository.findByIcId(sc.getId());
                                if (pids != null && !pids.isEmpty()) {
                                    double pOff = 0.0;
                                    double pPass = 0.0;
                                    double pRej = 0.0;
                                    for (ProcessInspectionDetails pid : pids) {
                                        pOff += pid.getOfferedQty() != null ? pid.getOfferedQty().doubleValue() : 0.0;
                                        pPass += pid.getQtyAccepted() != null ? pid.getQtyAccepted().doubleValue() : 0.0;
                                        pRej += pid.getQtyRejected() != null ? pid.getQtyRejected().doubleValue() : 0.0;
                                    }
                                    if (pOff > 0 || pPass > 0 || pRej > 0) {
                                        if (pPass == 0 && pRej == 0 && pOff > 0) pPass = pOff;
                                        log.info("[IBS-FETCH] Resolved source IC {} quantities from ProcessInspectionDetails: offered={}, passed={}, rejected={}",
                                                cleanSrc, pOff, pPass, pRej);
                                        return new double[]{pOff, pPass, pRej};
                                    }
                                }
                            }
                        } catch (Exception ignored) {}
                    }
                }
            }

            // 2. ERC Raw Material Call (ER-)
            if (upperSrc.startsWith("ER") || upperSrc.contains("ER-") || upperSrc.contains("/ER")) {
                if (rmHeatFinalResultRepository != null) {
                    try {
                        List<Object[]> rows = rmHeatFinalResultRepository.getRmCompletedInspectionCalls(searchNos);
                        if (rows != null && !rows.isEmpty()) {
                            for (Object[] r : rows) {
                                Double off = safeQuantity(r[11]);
                                Double pass = safeQuantity(r[12]);
                                Double rej = safeQuantity(r[13]);
                                if (off > 0 || pass > 0 || rej > 0) {
                                    log.info("[IBS-FETCH] Resolved source IC {} quantities from getRmCompletedInspectionCalls: offered={}, passed={}, rejected={}",
                                            cleanSrc, off, pass, rej);
                                    return new double[]{off, pass, rej};
                                }
                            }
                        }
                    } catch (Exception e) {
                        log.debug("Error in getRmCompletedInspectionCalls for source {}: {}", cleanSrc, e.getMessage());
                    }

                    try {
                        List<RmHeatFinalResult> rmResults = Collections.emptyList();
                        for (String sNo : searchNos) {
                            rmResults = rmHeatFinalResultRepository.findByInspectionCallNo(sNo);
                            if (rmResults != null && !rmResults.isEmpty()) break;
                        }
                        if (rmResults != null && !rmResults.isEmpty()) {
                            Map<String, Double> heatOfferedMap = new HashMap<>();
                            double totalPassed = 0.0;
                            double totalRejected = 0.0;
                            for (RmHeatFinalResult rm : rmResults) {
                                String heatNo = rm.getHeatNo() != null ? rm.getHeatNo().trim() : "";
                                double off = rm.getTotalQtyOfferedMt() != null ? rm.getTotalQtyOfferedMt().doubleValue() : 0.0;
                                double acc = rm.getAcceptedQtyMt() != null ? rm.getAcceptedQtyMt().doubleValue() : 0.0;
                                double rej = rm.getWeightRejectedMt() != null ? rm.getWeightRejectedMt().doubleValue() : 0.0;
                                heatOfferedMap.putIfAbsent(heatNo, off);
                                totalPassed += acc;
                                totalRejected += rej;
                            }
                            double totalOffered = heatOfferedMap.values().stream().mapToDouble(Double::doubleValue).sum();
                            if (totalOffered > 0 || totalPassed > 0 || totalRejected > 0) {
                                log.info("[IBS-FETCH] Resolved source IC {} quantities from findByInspectionCallNo (RM): offered={}, passed={}, rejected={}",
                                        cleanSrc, totalOffered, totalPassed, totalRejected);
                                return new double[]{totalOffered, totalPassed, totalRejected};
                            }
                        }
                    } catch (Exception e) {
                        log.debug("Error in rmHeatFinalResultRepository.findByInspectionCallNo for {}: {}", cleanSrc, e.getMessage());
                    }
                }
            }

            // 3. ERC Final Call (EF-)
            if (upperSrc.startsWith("EF") || upperSrc.contains("EF-") || upperSrc.contains("/EF")) {
                if (finalCumulativeResultsRepository != null) {
                    try {
                        List<Object[]> rows = finalCumulativeResultsRepository.getFinalCompletedInspectionCalls(searchNos);
                        if (rows != null && !rows.isEmpty()) {
                            for (Object[] r : rows) {
                                Double off = safeQuantity(r[11]);
                                Double pass = safeQuantity(r[12]);
                                Double rej = safeQuantity(r[13]);
                                if (off > 0 || pass > 0 || rej > 0) {
                                    log.info("[IBS-FETCH] Resolved source IC {} quantities from getFinalCompletedInspectionCalls: offered={}, passed={}, rejected={}",
                                            cleanSrc, off, pass, rej);
                                    return new double[]{off, pass, rej};
                                }
                            }
                        }
                    } catch (Exception e) {
                        log.debug("Error in getFinalCompletedInspectionCalls for source {}: {}", cleanSrc, e.getMessage());
                    }

                    try {
                        for (String sNo : searchNos) {
                            var resOpt = finalCumulativeResultsRepository.findByInspectionCallNo(sNo);
                            if (resOpt.isPresent()) {
                                var fr = resOpt.get();
                                double off = fr.getQtyNowOffered() != null ? fr.getQtyNowOffered().doubleValue() : 0.0;
                                double pass = fr.getQtyNowPassed() != null ? fr.getQtyNowPassed().doubleValue() : 0.0;
                                double rej = fr.getQtyNowRejected() != null ? fr.getQtyNowRejected().doubleValue() : 0.0;
                                if (off > 0 || pass > 0 || rej > 0) {
                                    log.info("[IBS-FETCH] Resolved source IC {} quantities from findByInspectionCallNo (Final): offered={}, passed={}, rejected={}",
                                            cleanSrc, off, pass, rej);
                                    return new double[]{off, pass, rej};
                                }
                            }
                        }
                    } catch (Exception e) {
                        log.debug("Error in finalCumulativeResultsRepository.findByInspectionCallNo for {}: {}", cleanSrc, e.getMessage());
                    }
                }
            }

            // 4. Railpad Process (RPP-)
            if (upperSrc.startsWith("RPP") || upperSrc.contains("RPP-")) {
                if (railpadProcessIcEditRepository != null) {
                    try {
                        List<Object[]> rows = railpadProcessIcEditRepository.getRailpadProcessCompletedInspectionCalls(searchNos);
                        if (rows != null && !rows.isEmpty()) {
                            for (Object[] r : rows) {
                                Double off = safeQuantity(r[11]);
                                Double pass = safeQuantity(r[12]);
                                Double rej = safeQuantity(r[13]);
                                if (off > 0 || pass > 0 || rej > 0) {
                                    return new double[]{off, pass, rej};
                                }
                            }
                        }
                    } catch (Exception ignored) {}
                }
            }

            // 5. Railpad Final (RPF-)
            if (upperSrc.startsWith("RPF") || upperSrc.contains("RPF-")) {
                if (railpadFinalIcEditRepository != null) {
                    try {
                        List<Object[]> rows = railpadFinalIcEditRepository.getRailpadFinalCompletedInspectionCalls(searchNos);
                        if (rows != null && !rows.isEmpty()) {
                            for (Object[] r : rows) {
                                Double off = safeQuantity(r[11]);
                                Double pass = safeQuantity(r[12]);
                                Double rej = safeQuantity(r[13]);
                                if (off > 0 || pass > 0 || rej > 0) {
                                    return new double[]{off, pass, rej};
                                }
                            }
                        }
                    } catch (Exception ignored) {}
                }
            }

            // 6. Sleeper Final (SF-)
            if (upperSrc.startsWith("SF") || upperSrc.contains("SF-")) {
                if (sleeperFinalIcEditRepository != null) {
                    try {
                        List<Object[]> rows = sleeperFinalIcEditRepository.getSleeperFinalCompletedInspectionCalls(searchNos);
                        if (rows != null && !rows.isEmpty()) {
                            for (Object[] r : rows) {
                                Double off = safeQuantity(r[11]);
                                Double pass = safeQuantity(r[12]);
                                Double rej = safeQuantity(r[13]);
                                if (off > 0 || pass > 0 || rej > 0) {
                                    return new double[]{off, pass, rej};
                                }
                            }
                        }
                    } catch (Exception ignored) {}
                }
            }

            // 7. Generic Fallback: InspectionCall details (Final, Process, RM)
            if (inspectionCallsRepository != null) {
                try {
                    InspectionCall sc = null;
                    for (String sNo : searchNos) {
                        sc = inspectionCallsRepository.findByIcNumber(sNo).orElse(null);
                        if (sc != null) break;
                    }
                    if (sc != null) {
                        if (sc.getFinalInspectionDetails() != null) {
                            var fid = sc.getFinalInspectionDetails();
                            double off = fid.getTotalOfferedQty() != null ? fid.getTotalOfferedQty().doubleValue() : 0.0;
                            double pass = fid.getTotalAcceptedQty() != null ? fid.getTotalAcceptedQty().doubleValue() : 0.0;
                            double rej = fid.getTotalRejectedQty() != null ? fid.getTotalRejectedQty().doubleValue() : 0.0;
                            if (off > 0 || pass > 0 || rej > 0) {
                                return new double[]{off, pass, rej};
                            }
                        }
                        if (sc.getProcessInspectionDetails() != null && !sc.getProcessInspectionDetails().isEmpty()) {
                            double pOff = 0.0;
                            double pPass = 0.0;
                            double pRej = 0.0;
                            for (var pid : sc.getProcessInspectionDetails()) {
                                pOff += pid.getOfferedQty() != null ? pid.getOfferedQty().doubleValue() : 0.0;
                                pPass += pid.getQtyAccepted() != null ? pid.getQtyAccepted().doubleValue() : 0.0;
                                pRej += pid.getQtyRejected() != null ? pid.getQtyRejected().doubleValue() : 0.0;
                            }
                            if (pOff > 0 || pPass > 0 || pRej > 0) {
                                return new double[]{pOff, pPass, pRej};
                            }
                        }
                        if (sc.getRmInspectionDetails() != null) {
                            var rmd = sc.getRmInspectionDetails();
                            double off = rmd.getTotalOfferedQtyMt() != null ? rmd.getTotalOfferedQtyMt().doubleValue() : 0.0;
                            if (off > 0) {
                                return new double[]{off, off, 0.0};
                            }
                        }
                    }
                } catch (Exception ignored) {}
            }
        } catch (Exception e) {
            log.warn("Error fetching source IC quantities for {}: {}", cleanSrc, e.getMessage());
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

            // ERC does not require RIO-based case number resolution — only Sleeper and RailPad do
            Character rioInitial = null;
            if (!"ERC".equalsIgnoreCase(productType)) {
                for (Object[] r : candidateRows) {
                    String rowRio = (r.length > 18 && r[18] != null) ? safeString(r[18]) : null;
                    String icNumber = r[15] != null ? safeString(r[15]) : null;
                    String poi = safeString(r[2]);
                    rioInitial = getRioInitial(rowRio, icNumber, poi, productType);
                    if (rioInitial != null) {
                        break;
                    }
                }
            }

            Object[] bestRow = null;
            String resolvedCaseNo = null;

            // 1. For Sleeper/RailPad: match case number (single or comma-separated) by RIO initial
            if (rioInitial != null) {
                for (Object[] r : candidateRows) {
                    String rawCase = safeString(r[0]);
                    if (rawCase != null && !rawCase.trim().isEmpty()) {
                        // Split on comma only — slashes may be part of the case number itself
                        String[] parts = rawCase.trim().split(",");
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

            // 2. Fallback: pick first non-empty case number part (for ERC or when RIO lookup fails)
            if (bestRow == null) {
                for (Object[] r : candidateRows) {
                    String rawCase = safeString(r[0]);
                    if (rawCase != null && !rawCase.trim().isEmpty()) {
                        String[] parts = rawCase.trim().split(",");
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

            String rawPoi = safeString(bestRow[2]);
            if (rawPoi != null) {
                while (rawPoi.startsWith(":")) {
                    rawPoi = rawPoi.substring(1).trim();
                }
            }
            dto.setPlaceOfInspection(rawPoi);

            String rawIbsMfc = bestRow[3] != null ? safeString(bestRow[3]).trim() : null;
            if (rawIbsMfc != null) {
                while (rawIbsMfc.startsWith(":")) {
                    rawIbsMfc = rawIbsMfc.substring(1).trim();
                }
                if (rawIbsMfc.isEmpty()) {
                    rawIbsMfc = null;
                }
            }
            dto.setIbsManufacturedCode(rawIbsMfc);

            String rawIeEmp = bestRow[4] != null ? safeString(bestRow[4]).trim() : null;
            if (rawIeEmp != null) {
                while (rawIeEmp.startsWith(":")) {
                    rawIeEmp = rawIeEmp.substring(1).trim();
                }
                if (rawIeEmp.isEmpty()) {
                    rawIeEmp = null;
                }
            }
            dto.setIeEmployeeNumber(rawIeEmp);
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

            dto.setQuantityOffered(safeQuantity(bestRow[11]));
            dto.setQuantityPassed(safeQuantity(bestRow[12]));
            dto.setQuantityRejected(safeQuantity(bestRow[13]));

            dto.setIcFileLink(
                    "https://api.ritesqasarthi.com"
                            + "/sarthi-backend/api/certificate-storage/view/"
                            + callNumber
                            + ".pdf"
            );

            boolean isCancelled = "C".equalsIgnoreCase(dto.getCallStatus())
                    || (bestRow[5] != null && "C".equalsIgnoreCase(safeString(bestRow[5])));

            String icNo = null;
            if (!isCancelled) {
                String chosenIc = bestRow[15] != null ? safeString(bestRow[15]).trim() : null;
                if (chosenIc == null || !chosenIc.contains("/")) {
                    for (Object[] r : candidateRows) {
                        if (r != null && r.length > 15 && r[15] != null) {
                            String c = safeString(r[15]).trim();
                            if (c.contains("/")) {
                                chosenIc = c;
                                break;
                            }
                        }
                    }
                }
                if (chosenIc != null && !chosenIc.isBlank() && !chosenIc.equalsIgnoreCase(callNumber) && chosenIc.contains("/")) {
                    icNo = chosenIc;
                }
            }

            dto.setCallNumber(callNumber);
            dto.setIcNumber(icNo);

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
        String[] parts = trimmed.split(",");

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

    /**
     * Batch-enrich cancelled calls with employee code (Eliminates N+1 queries).
     * 1. Fast-path: if employee code is already resolved (e.g. from SQL query), 0 queries are executed.
     * 2. If any cancelled call has missing or numeric userId employee number, does at most 1 IN-batch query
     *    on call_cancellation_details and 1 IN-batch query on user_master, then resolves entirely in-memory.
     */
    private void batchEnrichCancelledCallEmployeeCodes(Collection<IbsInspectionDto> dtos) {
        if (dtos == null || dtos.isEmpty()) return;

        List<IbsInspectionDto> targetDtos = new ArrayList<>();
        Set<String> callNosToLookup = new HashSet<>();
        Set<Integer> userIdsToFetch = new HashSet<>();

        for (IbsInspectionDto dto : dtos) {
            if (dto == null || !"C".equalsIgnoreCase(dto.getCallStatus())) continue;
            // Certificate number (icNumber) must be null for cancelled calls
            dto.setIcNumber(null);
            String emp = dto.getIeEmployeeNumber();
            boolean isNumericUserId = emp != null && emp.trim().matches("^\\d+$");
            boolean isMissing = emp == null || emp.trim().isEmpty();

            if (isNumericUserId) {
                try {
                    userIdsToFetch.add(Integer.parseInt(emp.trim()));
                    targetDtos.add(dto);
                } catch (Exception ignored) {}
            } else if (isMissing) {
                String cNo = extractCleanCallNo(dto);
                if (!cNo.isEmpty()) {
                    callNosToLookup.add(cNo);
                    if (dto.getCallNumber() != null && !dto.getCallNumber().isBlank()) {
                        callNosToLookup.add(dto.getCallNumber().trim());
                    }
                    if (dto.getIcNumber() != null && !dto.getIcNumber().isBlank()) {
                        callNosToLookup.add(dto.getIcNumber().trim());
                    }
                    targetDtos.add(dto);
                }
            }
        }

        // Fast-path: all cancelled calls already have real employee codes from SQL -> 0 queries executed!
        if (targetDtos.isEmpty()) {
            return;
        }

        // Batch lookup call_cancellation_details in a single query with IN clause
        Map<String, CallCancellationDetail> cdMap = new HashMap<>();
        if (!callNosToLookup.isEmpty() && callCancellationDetailRepository != null) {
            try {
                List<CallCancellationDetail> cds = callCancellationDetailRepository.findByCallNumberIn(callNosToLookup);
                if (cds != null) {
                    for (CallCancellationDetail cd : cds) {
                        if (cd == null || cd.getCallNumber() == null) continue;
                        String cn = cd.getCallNumber().trim();
                        cdMap.put(cn, cd);
                        cdMap.put(cn.toUpperCase(), cd);
                        if (cd.getActionBy() != null && cd.getActionBy() > 0) {
                            userIdsToFetch.add(cd.getActionBy().intValue());
                        }
                        if (cd.getCreatedBy() != null && cd.getCreatedBy().trim().matches("^\\d+$")) {
                            try {
                                userIdsToFetch.add(Integer.parseInt(cd.getCreatedBy().trim()));
                            } catch (Exception ignored) {}
                        }
                    }
                }
            } catch (Exception e) {
                log.warn("Error in batch fetching call_cancellation_details: {}", e.getMessage());
            }
        }

        // Batch lookup user_master in a single query with IN clause
        Map<Integer, String> userIdToEmpCode = new HashMap<>();
        if (!userIdsToFetch.isEmpty() && userMasterRepository != null) {
            try {
                List<UserMaster> users = userMasterRepository.findByUserIdIn(new ArrayList<>(userIdsToFetch));
                if (users != null) {
                    for (UserMaster u : users) {
                        if (u != null && u.getUserId() != null) {
                            String resolved = resolveEmployeeCode(u);
                            if (resolved != null && !resolved.isBlank()) {
                                userIdToEmpCode.put(u.getUserId(), resolved);
                            }
                        }
                    }
                }
            } catch (Exception e) {
                log.warn("Error in batch fetching user_master by user IDs: {}", e.getMessage());
            }
        }

        // In-memory assignment - zero extra queries!
        for (IbsInspectionDto dto : targetDtos) {
            String emp = dto.getIeEmployeeNumber();
            if (emp != null && emp.trim().matches("^\\d+$")) {
                try {
                    int uid = Integer.parseInt(emp.trim());
                    String resolved = userIdToEmpCode.get(uid);
                    if (resolved != null && !resolved.isBlank()) {
                        dto.setIeEmployeeNumber(resolved);
                        continue;
                    }
                } catch (Exception ignored) {}
            }

            String cNo = extractCleanCallNo(dto);
            CallCancellationDetail cd = cdMap.get(cNo);
            if (cd == null && dto.getCallNumber() != null) cd = cdMap.get(dto.getCallNumber().trim());
            if (cd == null && dto.getIcNumber() != null) cd = cdMap.get(dto.getIcNumber().trim());
            if (cd != null) {
                if (cd.getActionBy() != null && cd.getActionBy() > 0) {
                    String resolved = userIdToEmpCode.get(cd.getActionBy().intValue());
                    if (resolved != null && !resolved.isBlank()) {
                        dto.setIeEmployeeNumber(resolved);
                        continue;
                    }
                }
                if (cd.getCreatedBy() != null && cd.getCreatedBy().trim().matches("^\\d+$")) {
                    try {
                        int uid = Integer.parseInt(cd.getCreatedBy().trim());
                        String resolved = userIdToEmpCode.get(uid);
                        if (resolved != null && !resolved.isBlank()) {
                            dto.setIeEmployeeNumber(resolved);
                            continue;
                        }
                    } catch (Exception ignored) {}
                }
            }
        }
    }

    private String extractCleanCallNo(IbsInspectionDto dto) {
        if (dto == null) return "";
        String callNo = dto.getCallNumber();
        if (callNo == null || callNo.trim().isEmpty()) {
            callNo = dto.getIcNumber();
        }
        if (callNo == null) return "";
        callNo = callNo.trim();
        if (callNo.contains("/")) {
            for (String part : callNo.split("/")) {
                String p = part.trim();
                if (p.startsWith("ER-") || p.startsWith("EP-") || p.startsWith("EF-")
                        || p.startsWith("ER") || p.startsWith("EP") || p.startsWith("EF")
                        || p.startsWith("RPP-") || p.startsWith("RPF-") || p.startsWith("RP")
                        || p.startsWith("SF-") || p.startsWith("SF")) {
                    return p;
                }
            }
        }
        return callNo;
    }

    @Override
    @Transactional
    public String revertIbsCallToCompleted(String callNumber, Integer deletedBy) {
        if (callNumber == null || callNumber.isBlank()) {
            throw new BusinessException(new ErrorDetails(
                    AppConstant.ERROR_CODE_INVALID, AppConstant.ERROR_TYPE_CODE_INVALID,
                    AppConstant.ERROR_TYPE_INVALID, "Call number is required."));
        }

        String upper = callNumber.toUpperCase().trim();
        java.util.Date now = new java.util.Date();

        if (upper.startsWith("SF-")) {
            // Sleeper Final
            List<SleeperWorkflowTransaction> records = sleeperWorkflowRepository
                    .findByRequestIdAndStatus(callNumber.trim(), "SEND_CALL_TO_IBS");
            if (records.isEmpty()) {
                records = sleeperWorkflowRepository
                        .findByRequestIdAndStatus(callNumber.trim(), "SENT_TO_IBS");
            }
            if (records.isEmpty()) {
                throw new BusinessException(new ErrorDetails(
                        AppConstant.ERROR_CODE_INVALID, AppConstant.ERROR_TYPE_CODE_INVALID,
                        AppConstant.ERROR_TYPE_INVALID,
                        "No SEND_CALL_TO_IBS workflow transition found for: " + callNumber));
            }
            for (SleeperWorkflowTransaction swt : records) {
                WorkflowDeleteHistory hist = new WorkflowDeleteHistory();
                hist.setRequestId(swt.getRequestId());
                hist.setRequestType("SLEEPER");
                hist.setWorkflowTransitionId(swt.getWorkflowTransitionId() != null ? swt.getWorkflowTransitionId().intValue() : null);
                hist.setWorkflowId(swt.getWorkflowId() != null ? swt.getWorkflowId().intValue() : null);
                hist.setStatus(swt.getStatus());
                hist.setJobStatus(swt.getJobStatus());
                hist.setAction(swt.getAction());
                hist.setCurrentRole(swt.getCurrentRole());
                hist.setNextRole(swt.getNextRole());
                hist.setRemarks(swt.getRemarks());
                hist.setCreatedBy(swt.getCreatedBy() != null ? swt.getCreatedBy().intValue() : null);
                hist.setAssignedToUser(swt.getAssignedToUser() != null ? swt.getAssignedToUser().intValue() : null);
                hist.setRio(swt.getRio());
                hist.setTransitionCreatedDate(swt.getCreatedDate() != null ? java.sql.Date.valueOf(swt.getCreatedDate().toLocalDate()) : null);
                hist.setDeletedBy(deletedBy);
                hist.setDeletedOn(now);
                workflowDeleteHistoryRepository.save(hist);
                sleeperWorkflowRepository.delete(swt);
            }

        } else if (upper.startsWith("RPP-") || upper.startsWith("RPF-")) {
            // Railpad Process / Final
            List<RailWorkflowTransaction> records = railWorkflowTransactionRepository
                    .findByRequestIdAndStatus(callNumber.trim(), "SEND_CALL_TO_IBS");
            if (records.isEmpty()) {
                records = railWorkflowTransactionRepository
                        .findByRequestIdAndStatus(callNumber.trim(), "SENT_TO_IBS");
            }
            if (records.isEmpty()) {
                throw new BusinessException(new ErrorDetails(
                        AppConstant.ERROR_CODE_INVALID, AppConstant.ERROR_TYPE_CODE_INVALID,
                        AppConstant.ERROR_TYPE_INVALID,
                        "No SEND_CALL_TO_IBS workflow transition found for: " + callNumber));
            }
            for (RailWorkflowTransaction rwt : records) {
                WorkflowDeleteHistory hist = new WorkflowDeleteHistory();
                hist.setRequestId(rwt.getRequestId());
                hist.setRequestType("RAILPAD");
                hist.setWorkflowTransitionId(rwt.getWorkflowTransitionId() != null ? rwt.getWorkflowTransitionId().intValue() : null);
                hist.setWorkflowId(rwt.getWorkflowId() != null ? rwt.getWorkflowId().intValue() : null);
                hist.setStatus(rwt.getStatus());
                hist.setJobStatus(rwt.getJobStatus());
                hist.setAction(rwt.getAction());
                hist.setCurrentRole(rwt.getCurrentRole());
                hist.setNextRole(rwt.getNextRole());
                hist.setRemarks(rwt.getRemarks());
                hist.setCreatedBy(rwt.getCreatedBy() != null ? rwt.getCreatedBy().intValue() : null);
                hist.setAssignedToUser(rwt.getAssignedToUser() != null ? rwt.getAssignedToUser().intValue() : null);
                hist.setRio(rwt.getRio());
                hist.setTransitionCreatedDate(rwt.getCreatedDate() != null ? java.sql.Date.valueOf(rwt.getCreatedDate().toLocalDate()) : null);
                hist.setDeletedBy(deletedBy);
                hist.setDeletedOn(now);
                workflowDeleteHistoryRepository.save(hist);
                railWorkflowTransactionRepository.delete(rwt);
            }

        } else {
            // ERC — ER / EP / EF
            List<WorkflowTransition> records = workflowTransitionRepository
                    .findByRequestIdAndStatus(callNumber.trim(), "SEND_CALL_TO_IBS");
            if (records.isEmpty()) {
                records = workflowTransitionRepository
                        .findByRequestIdAndStatus(callNumber.trim(), "SENT_TO_IBS");
            }
            if (records.isEmpty()) {
                throw new BusinessException(new ErrorDetails(
                        AppConstant.ERROR_CODE_INVALID, AppConstant.ERROR_TYPE_CODE_INVALID,
                        AppConstant.ERROR_TYPE_INVALID,
                        "No SEND_CALL_TO_IBS workflow transition found for: " + callNumber));
            }
            for (WorkflowTransition wt : records) {
                WorkflowDeleteHistory hist = new WorkflowDeleteHistory();
                hist.setRequestId(wt.getRequestId());
                hist.setRequestType("ERC");
                hist.setWorkflowTransitionId(wt.getWorkflowTransitionId());
                hist.setWorkflowId(wt.getWorkflowId());
                hist.setTransitionId(wt.getTransitionId());
                hist.setStatus(wt.getStatus());
                hist.setJobStatus(wt.getJobStatus());
                hist.setAction(wt.getAction());
                hist.setCurrentRole(wt.getCurrentRole());
                hist.setNextRole(wt.getNextRole());
                hist.setCurrentRoleName(wt.getCurrentRoleName());
                hist.setNextRoleName(wt.getNextRoleName());
                hist.setRemarks(wt.getRemarks());
                hist.setCreatedBy(wt.getCreatedBy());
                hist.setModifiedBy(wt.getModifiedBy());
                hist.setAssignedToUser(wt.getAssignedToUser());
                hist.setProcessIeUserId(wt.getProcessIeUserId());
                hist.setWorkflowSequence(wt.getWorkflowSequence());
                hist.setRio(wt.getRio());
                hist.setSwiftCode(wt.getSwiftCode());
                hist.setPrimarySwift(wt.getPrimarySwift());
                hist.setTransitionCreatedDate(wt.getCreatedDate());
                hist.setDeletedBy(deletedBy);
                hist.setDeletedOn(now);
                workflowDeleteHistoryRepository.save(hist);
                workflowTransitionRepository.delete(wt);
            }
        }

        log.info("[IBS-REVERT] Reverted call {} to completed (deleted SEND_CALL_TO_IBS transition)", callNumber);
        return "SUCCESS";
    }

    private String resolveEmployeeCode(UserMaster user) {
        if (user == null) return null;
        if (user.getEmployeeCode() != null && !user.getEmployeeCode().trim().isEmpty()) {
            return user.getEmployeeCode().trim();
        }
        if (user.getRitesEmployeeCode() != null) {
            return String.valueOf(user.getRitesEmployeeCode()).trim();
        }
        if (user.getEmployeeId() != null && !user.getEmployeeId().trim().isEmpty()) {
            return user.getEmployeeId().trim();
        }
        return null;
    }
}
