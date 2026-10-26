package com.sarthi.service.Impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sarthi.dto.crisDtos.UdmRejectionDTO;
import com.sarthi.entity.UdmRejection;
import com.sarthi.repository.UdmRejectionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class UdmRejectionSyncService {

    @Autowired
    private CrisAuthServic authService;

    @Autowired
    private UdmRejectionRepository repository;

    @Value("${cris.base-url}")
    private String baseUrl;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    private static final DateTimeFormatter DT_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * Fetch rejections for the given date from CRIS and upsert into DB.
     * Returns count of records saved.
     */
    public int syncForDate(LocalDate date) {
        String dateStr = date.format(DateTimeFormatter.ISO_LOCAL_DATE);
        System.out.println("[UdmSync] Syncing rejections for date: " + dateStr);

        List<UdmRejectionDTO> records = fetchFromCris(dateStr);
        if (records.isEmpty()) {
            System.out.println("[UdmSync] No records returned from CRIS for " + dateStr);
            return 0;
        }

        int saved = 0;
        for (UdmRejectionDTO dto : records) {
            if (dto.getRejectionNumber() == null || dto.getRejectionNumber().isBlank()) continue;

            UdmRejection entity = repository.findByRejectionNumber(dto.getRejectionNumber())
                    .orElse(new UdmRejection());

            mapDtoToEntity(dto, entity, date);
            repository.save(entity);
            saved++;
        }

        System.out.println("[UdmSync] Saved " + saved + " records for " + dateStr);
        return saved;
    }

    private List<UdmRejectionDTO> fetchFromCris(String dateStr) {
        try {
            String url = baseUrl + "/purchase/getConsigneeRejections";
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(authService.getToken());

            Map<String, String> body = new HashMap<>();
            body.put("rejDate", dateStr);

            HttpEntity<?> request = new HttpEntity<>(body, headers);
            ResponseEntity<Map> response = restTemplate.postForEntity(url, request, Map.class);

            Map<String, Object> responseBody = response.getBody();
            if (responseBody == null) return Collections.emptyList();

            Object dataObj = responseBody.get("data");
            if (!(dataObj instanceof List)) return Collections.emptyList();

            return objectMapper.convertValue(dataObj, new TypeReference<List<UdmRejectionDTO>>() {});
        } catch (Exception e) {
            System.err.println("[UdmSync] CRIS API error for " + dateStr + ": " + e.getMessage());
            return Collections.emptyList();
        }
    }

    private void mapDtoToEntity(UdmRejectionDTO dto, UdmRejection entity, LocalDate fetchedDate) {
        entity.setPoNumber(dto.getPoNumber());
        entity.setPoDate(parseDateTime(dto.getPoDate()));
        entity.setPlNumber(dto.getPlNumber());
        entity.setPoSerialNumber(dto.getPoSerialNumber());
        entity.setRejectionNumber(dto.getRejectionNumber());
        entity.setRejectionDate(parseDateTime(dto.getRejectionDate()));
        entity.setTypeOfRejection(dto.getTypeOfRejection());
        entity.setRejectedQty(parseBigDecimal(dto.getRejectedQty()));
        entity.setUom(dto.getUom());
        entity.setReasonForRejection(dto.getReasonForRejection());
        entity.setRejectionValue(parseBigDecimal(dto.getRejectionValue()));
        entity.setStatus(dto.getStatus());
        entity.setItemDescription(dto.getItemDescription());
        entity.setIcNumber(dto.getIcNumber());
        entity.setIcDate(parseDateTime(dto.getIcDate()));
        entity.setInspectionAgency(dto.getInspectionAgency());
        entity.setVendorCode(dto.getVendorCode());
        entity.setVendorName(dto.getVendorName());
        entity.setConsignee(dto.getConsignee());
        entity.setMaterialReceivedDate(parseDateTime(dto.getMaterialReceivedDate()));
        entity.setSource(dto.getSource());
        entity.setPdfPath(dto.getPdfPath());
        entity.setFetchedDate(fetchedDate);
    }

    private LocalDateTime parseDateTime(String value) {
        if (value == null || value.isBlank()) return null;
        try {
            return LocalDateTime.parse(value.trim(), DT_FORMATTER);
        } catch (Exception e) {
            try {
                return LocalDate.parse(value.trim()).atStartOfDay();
            } catch (Exception ex) {
                return null;
            }
        }
    }

    private BigDecimal parseBigDecimal(String value) {
        if (value == null || value.isBlank()) return null;
        try {
            return new BigDecimal(value.trim().replace(",", ""));
        } catch (Exception e) {
            return null;
        }
    }
}
