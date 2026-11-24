package com.sarthi.Sms.job;

import org.springframework.http.HttpHeaders;
import java.time.LocalDate;
import java.util.Map;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import com.sarthi.Sms.entity.bsp.FwtTestRecord;
import com.sarthi.Sms.repository.bsp.FwtTestRecordRepository;
@Component
public class FwtJob implements Job {

    @Autowired
    private FwtTestRecordRepository repo;

    @Autowired
    private RestTemplate restTemplate;

    @Override
public void execute(JobExecutionContext context) {
    try {
        // Step 1: Authenticate and get access token
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        Map<String, String> body = Map.of("username", "ritesuser", "password", "bsprites1234#");
        HttpEntity<Map<String, String>> loginEntity = new HttpEntity<>(body, headers);

        ResponseEntity<Map> loginResp = restTemplate.postForEntity(
            "https://bspapp.sail-bhilaisteel.com/setu/api/token/",
            loginEntity, Map.class
        );

        String accessToken = (String) loginResp.getBody().get("access");

        // Step 2: Determine range of dates to process
        LocalDate lastDate = repo.findTopByOrderByCreatedAtDesc()
            .map(r -> r.getCreatedAt())
            .orElse(LocalDate.now().minusDays(1)); // default to yesterday

        LocalDate today = LocalDate.now();
        for (LocalDate date = lastDate.plusDays(1); !date.isAfter(today); date = date.plusDays(1)) {
            boolean success = false;
            int attempts = 0;

            while (!success && attempts < 5) {
                try {
                    // Step 3: Fetch data for current date
                    HttpHeaders authHeaders = new HttpHeaders();
                    authHeaders.setBearerAuth(accessToken);
                    HttpEntity<?> entity = new HttpEntity<>(authHeaders);

                    String url = "https://bspapp.sail-bhilaisteel.com/setu/public_api/shift_fwt/" + date;
                    ResponseEntity<FwtTestRecord[]> dataResp = restTemplate.exchange(
                        url, HttpMethod.GET, entity, FwtTestRecord[].class);

                    FwtTestRecord[] records = dataResp.getBody();

                    if (records != null) {
                        for (FwtTestRecord record : records) {
                            record.setCreatedAt(date);
                            repo.save(record);
                        }
                    }

                    System.out.println("✅ Shift FWT data for " + date + " saved successfully.");
                    success = true;

                } catch (Exception ex) {
                    attempts++;
                    System.out.println("Attempt " + attempts + " failed for " + date + ": " + ex.getMessage());
                    if (attempts == 5) {
                        System.out.println("Giving up on " + date + " after 5 retries.");
                    }
                    Thread.sleep(2000); // wait 2 seconds before retry
                }
            }
        }
    } catch (Exception e) {
        e.printStackTrace();
        System.out.println("FWT job failed: " + e.getMessage());
    }
}

}