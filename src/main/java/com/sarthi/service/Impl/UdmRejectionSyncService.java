package com.sarthi.service.Impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sarthi.dto.crisDtos.UdmRejectionDTO;
import com.sarthi.entity.UdmRejection;
import com.sarthi.entity.UdmSyncLog;
import com.sarthi.repository.UdmRejectionRepository;
import com.sarthi.repository.UdmSyncLogRepository;
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

    @Autowired
    private UdmSyncLogRepository syncLogRepository;

    @Value("${cris.base-url}")
    private String baseUrl;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    private static final DateTimeFormatter DT_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private static final int MAX_ERROR_LENGTH = 4000;

    /** A CRIS call that failed, with a reason a person can read. */
    private static class CrisSyncException extends RuntimeException {
        CrisSyncException(String message, Throwable cause) { super(message, cause); }
    }

    /** What CRIS returned: the records, plus CRIS's own message when there were none. */
    private record CrisResult(List<UdmRejectionDTO> records, String note) {}

    /**
     * Kept for existing callers: runs a manual sync and returns the number of records stored.
     */
    public int syncForDate(LocalDate date) {
        UdmSyncLog log = runSync(date, UdmSyncLog.TRIGGER_MANUAL, null);
        return nz(log.getRecordsNew()) + nz(log.getRecordsUpdated());
    }

    /**
     * Fetch rejections for the given date from CRIS, upsert them, and record the run in udm_sync_log:
     * when it ran, who triggered it, how many records came and were stored, and why it failed.
     */
    public UdmSyncLog runSync(LocalDate date, String triggerType, String triggeredBy) {
        String dateStr = date.format(DateTimeFormatter.ISO_LOCAL_DATE);
        System.out.println("[UdmSync] Syncing rejections for date: " + dateStr + " (" + triggerType + ")");

        UdmSyncLog log = new UdmSyncLog();
        log.setSyncDate(date);
        log.setTriggerType(triggerType);
        log.setTriggeredBy(triggeredBy);
        log.setStartedAt(LocalDateTime.now());
        log.setStatus(UdmSyncLog.STATUS_RUNNING);
        log = saveLog(log); // saved first, so a run that crashes the server still leaves a trace

        long startMs = System.currentTimeMillis();
        int created = 0, updated = 0, failed = 0;
        String firstSaveError = null;
        try {
            CrisResult result = fetchFromCris(dateStr);
            List<UdmRejectionDTO> records = result.records();
            log.setRecordsReceived(records.size());

            if (records.isEmpty()) {
                log.setStatus(UdmSyncLog.STATUS_NO_DATA);
                log.setErrorMessage(result.note());
                System.out.println("[UdmSync] No records returned from CRIS for " + dateStr);
            } else {
                for (UdmRejectionDTO dto : records) {
                    if (dto.getRejectionNumber() == null || dto.getRejectionNumber().isBlank()) {
                        failed++;
                        if (firstSaveError == null) firstSaveError = "CRIS sent a record without a rejection number";
                        continue;
                    }
                    try {
                        java.util.Optional<UdmRejection> existing = repository.findByRejectionNumber(dto.getRejectionNumber());
                        UdmRejection entity = existing.orElse(new UdmRejection());
                        mapDtoToEntity(dto, entity, date);
                        repository.save(entity);
                        if (existing.isPresent()) updated++; else created++;
                    } catch (Exception e) {
                        failed++;
                        if (firstSaveError == null) {
                            firstSaveError = "Could not save rejection " + dto.getRejectionNumber() + ": " + rootMessage(e);
                        }
                    }
                }
                if (failed == 0) {
                    log.setStatus(UdmSyncLog.STATUS_SUCCESS);
                } else {
                    log.setStatus(created + updated > 0 ? UdmSyncLog.STATUS_PARTIAL : UdmSyncLog.STATUS_FAILED);
                    log.setErrorMessage(failed + " of " + records.size() + " record(s) not saved. First error: " + firstSaveError);
                }
                System.out.println("[UdmSync] Saved " + (created + updated) + " records for " + dateStr
                        + " (new " + created + ", updated " + updated + ", failed " + failed + ")");
            }
        } catch (Exception e) {
            log.setStatus(UdmSyncLog.STATUS_FAILED);
            log.setErrorMessage(e instanceof CrisSyncException ? e.getMessage() : "Unexpected error: " + rootMessage(e));
            System.err.println("[UdmSync] Sync failed for " + dateStr + ": " + log.getErrorMessage());
        } finally {
            log.setRecordsNew(created);
            log.setRecordsUpdated(updated);
            log.setRecordsFailed(failed);
            log.setFinishedAt(LocalDateTime.now());
            log.setDurationMs(System.currentTimeMillis() - startMs);
            log.setErrorMessage(truncate(log.getErrorMessage()));
            log = saveLog(log);
        }
        return log;
    }

    /** Latest sync runs, newest first. */
    public List<UdmSyncLog> getSyncHistory(int limit) {
        return syncLogRepository.findAllByOrderByStartedAtDesc(
                org.springframework.data.domain.PageRequest.of(0, Math.max(1, Math.min(limit, 200))));
    }

    private CrisResult fetchFromCris(String dateStr) {
        String token;
        try {
            token = authService.getToken();
        } catch (Exception e) {
            throw new CrisSyncException("CRIS login failed: " + describe(e), e);
        }
        if (token == null || token.isBlank()) {
            throw new CrisSyncException("CRIS login failed: no token returned by /authenticate", null);
        }

        Map<String, Object> responseBody;
        try {
            String url = baseUrl + "/purchase/getConsigneeRejections";
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(token);

            Map<String, String> body = new HashMap<>();
            body.put("rejDate", dateStr);

            HttpEntity<?> request = new HttpEntity<>(body, headers);
            ResponseEntity<Map> response = restTemplate.postForEntity(url, request, Map.class);
            responseBody = response.getBody();
        } catch (Exception e) {
            throw new CrisSyncException("CRIS getConsigneeRejections failed: " + describe(e), e);
        }

        if (responseBody == null) {
            throw new CrisSyncException("CRIS returned an empty response", null);
        }
        Object dataObj = responseBody.get("data");
        if (dataObj == null) {
            // No list at all: keep whatever CRIS said, so "no rejections" and errors can be told apart
            Object msg = responseBody.getOrDefault("message", responseBody.get("status"));
            return new CrisResult(Collections.emptyList(), msg != null ? "CRIS message: " + msg : null);
        }
        if (!(dataObj instanceof List)) {
            throw new CrisSyncException("Unexpected CRIS response format: 'data' is not a list. Response: "
                    + truncate(String.valueOf(responseBody), 500), null);
        }
        try {
            return new CrisResult(objectMapper.convertValue(dataObj, new TypeReference<List<UdmRejectionDTO>>() {}), null);
        } catch (Exception e) {
            throw new CrisSyncException("Could not read CRIS records: " + rootMessage(e), e);
        }
    }

    /** Readable reason for a failed HTTP call: status + CRIS's reply, or the network problem. */
    private String describe(Exception e) {
        if (e instanceof org.springframework.web.client.HttpStatusCodeException http) {
            String body = http.getResponseBodyAsString();
            return "HTTP " + http.getStatusCode().value() + " " + http.getStatusText()
                    + (body != null && !body.isBlank() ? " - " + truncate(body, 500) : "");
        }
        if (e instanceof org.springframework.web.client.ResourceAccessException) {
            return "could not reach CRIS (network or timeout): " + rootMessage(e);
        }
        return rootMessage(e);
    }

    private String rootMessage(Throwable e) {
        Throwable t = e;
        while (t.getCause() != null && t.getCause() != t) t = t.getCause();
        String msg = t.getMessage();
        return t.getClass().getSimpleName() + (msg != null ? ": " + msg : "");
    }

    private UdmSyncLog saveLog(UdmSyncLog log) {
        try {
            return syncLogRepository.save(log);
        } catch (Exception e) {
            // Logging must never stop the sync itself (e.g. if the udm_sync_log table is missing)
            System.err.println("[UdmSync] Could not write sync log: " + rootMessage(e));
            return log;
        }
    }

    private static String truncate(String s) { return truncate(s, MAX_ERROR_LENGTH); }

    private static String truncate(String s, int max) {
        return s == null || s.length() <= max ? s : s.substring(0, max) + "…";
    }

    private static int nz(Integer v) { return v != null ? v : 0; }

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
