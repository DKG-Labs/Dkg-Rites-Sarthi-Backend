package com.sarthi.service.Impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sarthi.constant.AppConstant;
import com.sarthi.dto.IBS.*;
import com.sarthi.dto.ibsDtos.AuthRequestDto;
import com.sarthi.dto.ibsDtos.AuthResponseDto;
import com.sarthi.entity.IBS.*;
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
                    "https://api.ritesqasarthi.com"
                            + "/sarthi-backend/api/certificate-storage/view/"
                            + callNumber
                            + ".pdf"
            );

            dto.setCallNumber(callNumber);
            dto.setIcNumber((String) row[15]);
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
               InspectionCall ic = null;
                Optional<InspectionCall> inspectionCall =

                               inspectionCallsRepository.findByIcNumber(
                                        registration.getCallNumber()
                                );

                if (inspectionCall.isPresent()) {
                    ic = inspectionCall.get();
                }
                PoHeader poHeader = null;
              Optional<PoHeader> po=  poHeaderRepository.findByPoNo(ic.getPoNo());
if(po.isPresent()){
    poHeader= po.get();
}
                IbsBillingRequest request =
                        new IbsBillingRequest();

                request.setCaseNo(
                        poHeader.getCaseNo()
                );

                request.setCallRecvDt(
                        ic.getCreatedAt()
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

            String caseNo = dataMap.get("CASE_NO") != null ? dataMap.get("CASE_NO").toString() :
                    (dataMap.get("caseNo") != null ? dataMap.get("caseNo").toString() : null);

            String caseStatus = dataMap.get("STATUS") != null ? dataMap.get("STATUS").toString() :
                    (dataMap.get("status") != null ? dataMap.get("status").toString() : "AVAILABLE");

            String poNo = dataMap.get("PO_NO") != null ? dataMap.get("PO_NO").toString() :
                    (payload.get("PO_NO") != null ? payload.get("PO_NO").toString() :
                    (payload.get("poNo") != null ? payload.get("poNo").toString() : null));

            String poKey = dataMap.get("POKEY") != null ? dataMap.get("POKEY").toString() :
                    (payload.get("POKEY") != null ? payload.get("POKEY").toString() :
                    (payload.get("poKey") != null ? payload.get("poKey").toString() : null));

            if (caseNo != null && !caseNo.trim().isEmpty() && !"N/A".equalsIgnoreCase(caseNo)) {
                saveCaseNoToPoHeader(poNo, poKey, caseNo.trim(), caseStatus);
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
            String caseNo = payload.get("caseNo") != null ? payload.get("caseNo").toString() :
                    (payload.get("CASE_NO") != null ? payload.get("CASE_NO").toString() : null);
            String caseStatus = payload.get("caseStatus") != null ? payload.get("caseStatus").toString() :
                    (payload.get("STATUS") != null ? payload.get("STATUS").toString() : "AVAILABLE");

            if (caseNo == null || caseNo.trim().isEmpty()) {
                Map<String, Object> err = new HashMap<>();
                err.put("status", "error");
                err.put("message", "Case number is mandatory.");
                return err;
            }

            boolean saved = saveCaseNoToPoHeader(poNo, poKey, caseNo.trim(), caseStatus);
            Map<String, Object> res = new HashMap<>();
            if (saved) {
                res.put("status", "success");
                res.put("message", "IBS Case Number saved to PO Header successfully.");
                res.put("caseNo", caseNo);
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
            po.setCaseNo(caseNo);
            po.setCaseStatus(caseStatus != null ? caseStatus : "AVAILABLE");
            poHeaderRepository.save(po);
            log.info("Saved Case No [{}] and Case Status [{}] to PO Header [ID: {}, PO No: {}]", caseNo, caseStatus, po.getId(), po.getPoNo());
            return true;
        } else {
            log.warn("PoHeader not found for poNo [{}] or poKey [{}] when saving caseNo [{}]", poNo, poKey, caseNo);
            return false;
        }
    }

}
