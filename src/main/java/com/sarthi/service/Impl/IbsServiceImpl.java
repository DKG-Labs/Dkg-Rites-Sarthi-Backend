package com.sarthi.service.Impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sarthi.constant.AppConstant;
import com.sarthi.dto.IBS.IbsCaseRequestDto;
import com.sarthi.dto.IBS.IbsCaseResponseDto;
import com.sarthi.dto.ibsDtos.AuthRequestDto;
import com.sarthi.dto.ibsDtos.AuthResponseDto;
import com.sarthi.entity.IBS.IbsCaseIntegration;
import com.sarthi.entity.PoHeader;
import com.sarthi.entity.UserMaster;
import com.sarthi.exception.BusinessException;
import com.sarthi.exception.ErrorDetails;
import com.sarthi.repository.IbsCaseIntegrationRepository;
import com.sarthi.repository.PoHeaderRepository;
import com.sarthi.repository.UserMasterRepository;
import com.sarthi.service.IbsService;
import com.sarthi.service.JwtService;

import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

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





}
