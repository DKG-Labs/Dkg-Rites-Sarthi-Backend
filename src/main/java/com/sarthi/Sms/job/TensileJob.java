package com.sarthi.Sms.job;

import com.sarthi.Sms.entity.bsp.TensileTestRecord;
import com.sarthi.Sms.repository.bsp.TensileTestRecordRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Component
@RequiredArgsConstructor
@Slf4j
public class TensileJob implements Job {

    private final RestTemplate restTemplate;
    private final TensileTestRecordRepository repository;

    private static final String LOGIN_URL = "https://bspapp.sail-bhilaisteel.com/setu/api/token/";
    private static final String API_URL_PREFIX = "https://bspapp.sail-bhilaisteel.com/setu/public_api/shift_tensile/";

    @Override
    @Transactional
    public void execute(JobExecutionContext context) throws JobExecutionException {
        try {
            LocalDate startDate = getNextSyncDate();
            LocalDate today = LocalDate.now();
            String accessToken = getAccessToken();

            for (LocalDate date = startDate; !date.isAfter(today); date = date.plusDays(1)) {
                int attempts = 0;
                boolean success = false;

                while (!success && attempts < 5) {
                    try {
                        String formattedDate = date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                        HttpHeaders headers = new HttpHeaders();
                        headers.setBearerAuth(accessToken);
                        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

                        HttpEntity<Void> entity = new HttpEntity<>(headers);
                        ResponseEntity<TensileTestRecord[]> response = restTemplate.exchange(
                                API_URL_PREFIX + formattedDate,
                                HttpMethod.GET,
                                entity,
                                TensileTestRecord[].class);

                        TensileTestRecord[] records = response.getBody();
                        if (records != null && records.length > 0) {
                            for (TensileTestRecord record : records) {
                                record.setCreatedAt(date);
                            }
                            repository.saveAll(Arrays.asList(records));
                            log.info("✅ Saved {} TENSILE records for {}", records.length, formattedDate);
                        } else {
                            log.warn("⚠️ No TENSILE records found for {}", formattedDate);
                        }

                        success = true;
                    } catch (Exception ex) {
                        attempts++;
                        log.error("❌ Attempt {} failed for {}: {}", attempts, date, ex.getMessage());
                        if (attempts == 5) {
                            log.error("⛔ Giving up on {} after 5 attempts", date);
                        }
                        try {
                            Thread.sleep(2000); // Optional wait before retry
                        } catch (InterruptedException ie) {
                            Thread.currentThread().interrupt();
                            throw new JobExecutionException("Interrupted while retrying", ie);
                        }
                    }
                }
            }

        } catch (Exception e) {
            log.error("🚨 Error running TENSILE job", e);
            throw new JobExecutionException(e);
        }
    }

    private LocalDate getNextSyncDate() {
        return repository.findTopByOrderByCreatedAtDesc()
                .map(r -> r.getCreatedAt().plusDays(1))
                .orElse(LocalDate.now().minusDays(1));
    }

    private String getAccessToken() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, String> creds = Map.of(
                "username", "ritesuser",
                "password", "bsprites1234#");

        HttpEntity<Map<String, String>> entity = new HttpEntity<>(creds, headers);
        Map<String, String> response = restTemplate.postForObject(LOGIN_URL, entity, Map.class);
        return response.get("access");
    }
}
