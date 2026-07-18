package com.sarthi.service.Impl;

import com.sarthi.dto.IBS.IbsBillingRequest;
import com.sarthi.dto.IBS.IbsBillingResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@RequiredArgsConstructor
public class IbsBillingClient {

    @Value("${ibs.billing.url}")
    private String billingUrl;

    private final RestTemplate restTemplate;


    public IbsBillingResponse fetchBilling(
            IbsBillingRequest request
          //  String token
    ) {

        HttpHeaders headers = new HttpHeaders();

        headers.setContentType(MediaType.APPLICATION_JSON);

       // headers.setBearerAuth(token);

        HttpEntity<IbsBillingRequest> entity =
                new HttpEntity<>(request, headers);

        ResponseEntity<IbsBillingResponse> response =
                restTemplate.exchange(
                        billingUrl,
                        HttpMethod.POST,
                        entity,
                        IbsBillingResponse.class
                );

        return response.getBody();
    }
}