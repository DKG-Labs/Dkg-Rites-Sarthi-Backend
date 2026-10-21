package com.sarthi.service.Impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sarthi.dto.crisDtos.UdmRejectionDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class UdmRejectionService {

    @Autowired
    private CrisAuthServic authService;

    @Value("${cris.base-url}")
    private String baseUrl;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Fetch consignee rejections from CRIS IREPS API.
     * The API accepts {"rejDate": "YYYY-MM-DD"} as the minimum request body.
     * Client-side filtering (poNo, vendorCode, type) is applied after fetch.
     */
    public List<UdmRejectionDTO> getConsigneeRejections(String rejDate, String poNo,
                                                         String vendorCode, String type) {
        String url = baseUrl + "/purchase/getConsigneeRejections";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(authService.getToken());

        // CRIS API requires rejDate — default to 6 months ago if not provided
        String effectiveDate = (rejDate != null && !rejDate.isBlank())
                ? rejDate
                : LocalDate.now().minusMonths(6).format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));

        Map<String, String> body = new HashMap<>();
        body.put("rejDate", effectiveDate);

        HttpEntity<?> request = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(url, request, Map.class);
            Map<String, Object> responseBody = response.getBody();
            if (responseBody == null) return Collections.emptyList();

            Object dataObj = responseBody.get("data");
            if (!(dataObj instanceof List)) return Collections.emptyList();

            List<UdmRejectionDTO> all = objectMapper.convertValue(
                    dataObj, new TypeReference<List<UdmRejectionDTO>>() {});

            // Apply optional client-side filters
            return all.stream()
                .filter(r -> poNo == null || poNo.isBlank() ||
                        (r.getPoNumber() != null && r.getPoNumber().contains(poNo)))
                .filter(r -> vendorCode == null || vendorCode.isBlank() ||
                        (r.getVendorCode() != null && r.getVendorCode().contains(vendorCode)))
                .filter(r -> type == null || type.isBlank() ||
                        type.equalsIgnoreCase(r.getTypeOfRejection()))
                .collect(java.util.stream.Collectors.toList());

        } catch (Exception e) {
            System.err.println("UDM Rejection API error: " + e.getMessage());
            return Collections.emptyList();
        }
    }
}
