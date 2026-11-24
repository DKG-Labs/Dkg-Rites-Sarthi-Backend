package com.sarthi.Sms.job;

import com.sarthi.Sms.entity.bsp.ChemTestRecord;
import com.sarthi.Sms.repository.bsp.ChemTestRecordRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.util.*;

@Component
@RequiredArgsConstructor
public class ChemJob implements Job {

    private final RestTemplate restTemplate;
    private final ChemTestRecordRepository repo;

    // @Override
    // @Transactional
    // public void execute(JobExecutionContext context) {
    // try {
    // // Get latest synced date
    // LocalDate lastDate = repo.findTopByOrderByCreatedAtDesc()
    // .map(ChemTestRecord::getCreatedAt)
    // .orElse(LocalDate.now().minusDays(1));

    // LocalDate nextDate = lastDate.plusDays(1);

    // // Get access token
    // String tokenUrl = "https://bspapp.sail-bhilaisteel.com/setu/api/token/";
    // Map<String, String> body = Map.of("username", "ritesuser", "password",
    // "bsprites1234#");

    // HttpHeaders headers = new HttpHeaders();
    // headers.setContentType(MediaType.APPLICATION_JSON);
    // HttpEntity<Map<String, String>> tokenReq = new HttpEntity<>(body, headers);

    // ResponseEntity<Map> tokenResp = restTemplate.exchange(tokenUrl,
    // HttpMethod.POST, tokenReq, Map.class);
    // String accessToken = (String) tokenResp.getBody().get("access");

    // // Fetch chem data
    // String dateStr = nextDate.toString();
    // String url =
    // "https://bspapp.sail-bhilaisteel.com/setu/public_api/shift_chem/" + dateStr;

    // headers = new HttpHeaders();
    // headers.setBearerAuth(accessToken);
    // HttpEntity<Void> entity = new HttpEntity<>(headers);

    // ResponseEntity<ChemTestRecord[]> dataResp = restTemplate.exchange(
    // url, HttpMethod.GET, entity, ChemTestRecord[].class);

    // ChemTestRecord[] records = dataResp.getBody();
    // if (records != null) {
    // Arrays.stream(records).forEach(r -> r.setCreatedAt(nextDate));
    // repo.saveAll(Arrays.asList(records));
    // }
    // } catch (Exception e) {
    // e.printStackTrace();
    // }
    // }

    @Override
    @Transactional
    public void execute(JobExecutionContext context) {
        try {
            // Step 1: Get latest synced date
            LocalDate lastDate = repo.findTopByOrderByCreatedAtDesc()
                    .map(ChemTestRecord::getCreatedAt)
                    .orElse(LocalDate.now().minusDays(1));

            LocalDate today = LocalDate.now();

            // Step 2: Get access token
            String tokenUrl = "https://bspapp.sail-bhilaisteel.com/setu/api/token/";
            Map<String, String> body = Map.of("username", "ritesuser", "password", "bsprites1234#");

            HttpHeaders tokenHeaders = new HttpHeaders();
            tokenHeaders.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, String>> tokenReq = new HttpEntity<>(body, tokenHeaders);

            ResponseEntity<Map> tokenResp = restTemplate.exchange(tokenUrl, HttpMethod.POST, tokenReq, Map.class);
            String accessToken = (String) tokenResp.getBody().get("access");

            // Step 3: Loop through each missing date
            for (LocalDate date = lastDate.plusDays(1); !date.isAfter(today); date = date.plusDays(1)) {
                int retries = 0;
                boolean success = false;

                while (!success && retries < 5) {
                    try {
                        String url = "https://bspapp.sail-bhilaisteel.com/setu/public_api/shift_chem/" + date;

                        HttpHeaders headers = new HttpHeaders();
                        headers.setBearerAuth(accessToken);
                        HttpEntity<Void> entity = new HttpEntity<>(headers);

                        ResponseEntity<ChemTestRecord[]> dataResp = restTemplate.exchange(
                                url, HttpMethod.GET, entity, ChemTestRecord[].class);

                        ChemTestRecord[] records = dataResp.getBody();

                        if (records != null && records.length > 0) {
                            for (ChemTestRecord record : records) {
                                record.setCreatedAt(date);
                            }
                            repo.saveAll(Arrays.asList(records));
                            System.out
                                    .println("✅ Saved ChemTestRecord for " + date + ": " + records.length + " entries");
                        } else {
                            System.out.println("⚠️ No ChemTestRecord data for " + date);
                        }

                        success = true;

                    } catch (Exception ex) {
                        retries++;
                        System.err.println("Attempt " + retries + " failed for date " + date + ": " + ex.getMessage());
                        if (retries == 5) {
                            System.err.println("Skipping " + date + " after 5 failed attempts.");
                        }
                        try {
                            Thread.sleep(2000); // Wait before retrying
                        } catch (InterruptedException ie) {
                            Thread.currentThread().interrupt(); // Restore interrupt flag
                            return;
                        }
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Chem job failed: " + e.getMessage());
        }
    }

}
