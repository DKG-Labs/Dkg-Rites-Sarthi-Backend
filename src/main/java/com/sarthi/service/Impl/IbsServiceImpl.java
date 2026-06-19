package com.sarthi.service.Impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sarthi.constant.AppConstant;
import com.sarthi.dto.IBS.*;
import com.sarthi.dto.ibsDtos.AuthRequestDto;
import com.sarthi.dto.ibsDtos.AuthResponseDto;
import com.sarthi.entity.IBS.IbsCallRegistration;
import com.sarthi.entity.IBS.IbsCaseIntegration;
import com.sarthi.entity.PoHeader;
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

    private final IbsCallRegistrationRepository ibsCallRegistrationRepository;


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
        if (!request.getPassword().equals(user.getPassword())) {

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
              integration.setRlyCd(po.getRlyCd());
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
                                integration.getRlyCd()
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

                        integration.setCaseNo(
                                response.getData().getCaseNo()
                        );

                        integration.setCompleted(true);

                        PoHeader po =
                                integration.getPoHeader();

                        po.setCaseNo(
                                response.getData().getCaseNo()
                        );

                        po.setCaseStatus(status);

                        poHeaderRepository.save(po);

                    }  else {

                        integration.setStatus("PENDING");

                        integration.setRetryCount(
                                integration.getRetryCount() + 1
                        );

                        // Retry after 2 days
                        integration.setNextRetryTime(
                                LocalDateTime.now().plusDays(2)
                        );
                    }

                } else {

                    integration.setStatus("FAILED");

                    integration.setRetryCount(
                            integration.getRetryCount() + 1
                    );

                    integration.setNextRetryTime(
                            LocalDateTime.now().plusDays(2)
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
                        LocalDateTime.now().plusDays(2)
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

        List<IbsInspectionDto> responseList =
                new ArrayList<>();


        responseList.addAll(
                mapResult(
                        rmHeatFinalResultRepository.getRmInspectionCalls()
                )
        );

        responseList.addAll(
                mapResult(
                        processLineFinalResultRepository.getProcessInspectionCalls()
                )
        );

        responseList.addAll(
                mapResult(
                        finalCumulativeResultsRepository.getFinalInspectionCalls()
                )
        );

        return responseList;
    }

    private List<IbsInspectionDto> mapResult(
            List<Object[]> rows
    ) {

        List<IbsInspectionDto> list =
                new ArrayList<>();

        for (Object[] row : rows) {

            IbsInspectionDto dto =
                    new IbsInspectionDto();

            dto.setCaseNumber(
                    (String) row[0]
            );

            dto.setCallDate(
                    ((java.sql.Date) row[1]).toLocalDate()
            );

            dto.setPlaceOfInspection(
                    (String) row[2]
            );
            dto.setIbsManufacturedCode(
                    row[3] != null ? row[3].toString() : null
            );

            dto.setIeEmployeeNumber(
                    (String) row[4]
            );

            dto.setCallStatus(
                    String.valueOf(row[5])
            );

            dto.setTypeOfCall(
                    String.valueOf(row[6])
            );

            dto.setPoItemSerialNumbers(
                    List.of((String) row[7])
            );

            dto.setBkNumber(
                    (String) row[8]
            );

            dto.setSetNumber(
                    (String) row[9]
            );

            dto.setIcDate(
                    ((java.sql.Date) row[10]).toLocalDate()
            );

            dto.setQuantityOffered(
                    ((Number) row[11]).intValue()
            );

            dto.setQuantityPassed(
                    ((Number) row[12]).intValue()
            );

            dto.setQuantityRejected(
                    ((Number) row[13]).intValue()
            );

            String callNumber = (String) row[14];

            dto.setIcFileLink(
                    "https://sarthibackendservice-bfe2eag3byfkbsa6.canadacentral-01.azurewebsites.net"
                            + "/sarthi-backend/api/certificate-storage/view/"
                            + callNumber
                            + ".pdf"
            );

            list.add(dto);
        }

        return list;
    }

    private String extractIcNumber(String icNumber) {

        if (icNumber == null || icNumber.isBlank()) {
            return null;
        }

        String[] parts = icNumber.split("/");

        return parts.length > 1 ? parts[1] : icNumber;
    }



    @Override
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
    }




}
