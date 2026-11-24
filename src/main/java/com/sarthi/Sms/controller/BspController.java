// // package com.rites.ritesbackend.controller;

// // import java.util.Date;
// // import java.util.HashMap;
// // import java.util.Map;
// // import java.util.Optional;

// // import org.quartz.JobKey;
// // import org.quartz.Scheduler;
// // import org.quartz.SchedulerException;
// // import org.quartz.Trigger;
// // import org.quartz.TriggerKey;
// // import org.springframework.beans.factory.annotation.Autowired;
// // import org.springframework.http.ResponseEntity;
// // import org.springframework.web.bind.annotation.GetMapping;
// // import org.springframework.web.bind.annotation.PostMapping;
// // import org.springframework.web.bind.annotation.RequestMapping;
// // import org.springframework.web.bind.annotation.RestController;

// // import com.sarthi.Sms.entity.bsp.FwtTestRecord;
// // import com.sarthi.Sms.repository.bsp.FwtTestRecordRepository;

// // @RestController
// // @RequestMapping({"/sms/api/fwt"})
// // public class BspController {

// //     @Autowired
// //     private Scheduler scheduler;

// //     @Autowired
// //     private FwtTestRecordRepository repo;

// //     @PostMapping("/trigger")
// //     public ResponseEntity<String> triggerJob() throws SchedulerException {
// //         scheduler.triggerJob(JobKey.jobKey("fwtJob"));
// //         return ResponseEntity.ok("Job triggered manually.");
// //     }

// //     @GetMapping("/status")
// // public ResponseEntity<Map<String, Object>> getStatus() throws SchedulerException {
// //     Trigger trigger = scheduler.getTrigger(TriggerKey.triggerKey("fwtTrigger"));

// //     Date nextFire = (trigger != null) ? trigger.getNextFireTime() : null;
// //     Date lastFire = (trigger != null) ? trigger.getPreviousFireTime() : null;

// //     Optional<FwtTestRecord> latest = repo.findTopByOrderByCreatedAtDesc();

// //     boolean isRunning = scheduler.getCurrentlyExecutingJobs().stream()
// //         .anyMatch(ctx -> ctx.getJobDetail().getKey().getName().equals("fwtJob"));

// //     Map<String, Object> status = new HashMap<>();
// //     status.put("lastRun", lastFire);
// //     status.put("nextRun", nextFire);
// //     status.put("lastDateSynced", latest.map(FwtTestRecord::getCreatedAt).orElse(null));
// //     status.put("status", isRunning ? "Running" : "Idle");

// //     return ResponseEntity.ok(status);
// // }

// // }


// package com.rites.ritesbackend.controller;

// import java.util.*;

// import org.quartz.*;
// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.http.HttpStatus;
// import org.springframework.http.ResponseEntity;
// import org.springframework.web.bind.annotation.*;

// import com.sarthi.Sms.entity.bsp.ChemTestRecord;
// import com.sarthi.Sms.entity.bsp.FwtTestRecord;
// import com.sarthi.Sms.entity.bsp.MacroTestRecord;
// import com.sarthi.Sms.entity.bsp.TensileTestRecord;
// import com.sarthi.Sms.repository.bsp.ChemTestRecordRepository;
// import com.sarthi.Sms.repository.bsp.FwtTestRecordRepository;
// import com.sarthi.Sms.repository.bsp.MacroTestRecordRepository;
// import com.sarthi.Sms.repository.bsp.TensileTestRecordRepository;
// import com.rites.ritesbackend.util.ResponseBuilder;

// import java.time.LocalDate;

// @RestController
// @RequestMapping("/bsp")
// public class BspController {

//     @Autowired
//     private Scheduler scheduler;

//     @Autowired
//     private FwtTestRecordRepository fwtRepo;

//     @Autowired
//     private MacroTestRecordRepository macroRepo;

//     @Autowired
//     private ChemTestRecordRepository chemRepo;

//     @Autowired
//     private TensileTestRecordRepository tensileRepo;

//     // ----------- FWT ENDPOINTS -----------

//     @PostMapping("/fwt/trigger")
//     public ResponseEntity<String> triggerFwtJob() throws SchedulerException {
//         scheduler.triggerJob(JobKey.jobKey("fwtJob"));
//         return ResponseEntity.ok("FWT job triggered manually.");
//     }

//     @GetMapping("/fwt/status")
//     public ResponseEntity<Map<String, Object>> getFwtStatus() throws SchedulerException {
//         return getJobStatus("fwtJob", "fwtTrigger", fwtRepo.findTopByOrderByCreatedAtDesc().map(FwtTestRecord::getCreatedAt).orElse(null));
//     }

//     // ----------- MACRO ENDPOINTS -----------

//     @PostMapping("/macro/trigger")
//     public ResponseEntity<String> triggerMacroJob() throws SchedulerException {
//         scheduler.triggerJob(JobKey.jobKey("macroJob"));
//         return ResponseEntity.ok("MACRO job triggered manually.");
//     }

//     @GetMapping("/macro/status")
//     public ResponseEntity<Map<String, Object>> getMacroStatus() throws SchedulerException {
//         return getJobStatus("macroJob", "macroTrigger", macroRepo.findTopByOrderByCreatedAtDesc().map(MacroTestRecord::getCreatedAt).orElse(null));
//     }

//     // ----------- COMMON STATUS HANDLER -----------

//     private ResponseEntity<Map<String, Object>> getJobStatus(String jobName, String triggerName, LocalDate lastDateSynced) throws SchedulerException {
//         Trigger trigger = scheduler.getTrigger(TriggerKey.triggerKey(triggerName));

//         Date nextFire = (trigger != null) ? trigger.getNextFireTime() : null;
//         Date lastFire = (trigger != null) ? trigger.getPreviousFireTime() : null;

//         boolean isRunning = scheduler.getCurrentlyExecutingJobs().stream()
//             .anyMatch(ctx -> ctx.getJobDetail().getKey().getName().equals(jobName));

//         Map<String, Object> status = new HashMap<>();
//         status.put("lastRun", lastFire);
//         status.put("nextRun", nextFire);
//         status.put("lastDateSynced", lastDateSynced);
//         status.put("status", isRunning ? "Running" : "Idle");

//         return ResponseEntity.ok(status);
//     }

//     @PostMapping("/chem/trigger")
//         public ResponseEntity<String> triggerChemJob() throws SchedulerException {
//             scheduler.triggerJob(JobKey.jobKey("chemJob"));
//             return ResponseEntity.ok("Chem job triggered manually.");
//     }

//     @GetMapping("/chem/status")
//     public ResponseEntity<Map<String, Object>> getChemStatus() throws SchedulerException {
//         return getJobStatus("chemJob", "chemTrigger", chemRepo.findTopByOrderByCreatedAtDesc().map(ChemTestRecord::getCreatedAt).orElse(null));
//     }

//     @PostMapping("/tensile/trigger")
//     public ResponseEntity<String> triggerTensileJob() throws SchedulerException {
//         scheduler.triggerJob(JobKey.jobKey("tensileJob"));
//         return ResponseEntity.ok("TENSILE job triggered manually.");
//     }

//     @GetMapping("/tensile/status")
//     public ResponseEntity<Object> getTensileStatus() throws SchedulerException {
//         ResponseEntity<Map<String, Object>> response = getJobStatus(
//             "tensileJob",
//             "tensileTrigger",
//             tensileRepo.findTopByOrderByCreatedAtDesc()
//                     .map(TensileTestRecord::getCreatedAt)
//                     .orElse(null)
//         );

//         return new ResponseEntity<>(SmsResponseBuilder.getSuccessResponse(response.getBody()), HttpStatus.OK);
//     }

// }



package com.sarthi.Sms.controller;

import java.util.*;

import org.quartz.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import java.time.format.DateTimeFormatter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.sarthi.Sms.entity.bsp.*;
import com.sarthi.Sms.repository.bsp.*;
import com.sarthi.Sms.util.SmsResponseBuilder;
import java.time.LocalDate;

@RestController
@RequestMapping({"/sms/bsp"})
public class BspController {

    @Autowired
    private Scheduler scheduler;

    @Autowired
    private FwtTestRecordRepository fwtRepo;

    @Autowired
    private MacroTestRecordRepository macroRepo;

    @Autowired
    private ChemTestRecordRepository chemRepo;

    @Autowired
    private TensileTestRecordRepository tensileRepo;

    // ----------- FWT ENDPOINTS -----------

    @PostMapping("/fwt/trigger")
    public ResponseEntity<Object> triggerFwtJob() throws SchedulerException {
        scheduler.triggerJob(JobKey.jobKey("fwtJob"));
        return new ResponseEntity<>(SmsResponseBuilder.getSuccessResponse("FWT job triggered manually."), HttpStatus.OK);
    }

    @GetMapping("/fwt/status")
    public ResponseEntity<Object> getFwtStatus() throws SchedulerException {
        Map<String, Object> res = getJobStatus("fwtJob", "fwtTrigger", 
            fwtRepo.findTopByOrderByCreatedAtDesc().map(FwtTestRecord::getCreatedAt).orElse(null));
        return new ResponseEntity<>(SmsResponseBuilder.getSuccessResponse(res), HttpStatus.OK);
    }

    // ----------- MACRO ENDPOINTS -----------

    @PostMapping("/macro/trigger")
    public ResponseEntity<Object> triggerMacroJob() throws SchedulerException {
        scheduler.triggerJob(JobKey.jobKey("macroJob"));
        return new ResponseEntity<>(SmsResponseBuilder.getSuccessResponse("MACRO job triggered manually."), HttpStatus.OK);
    }

    @GetMapping("/macro/status")
    public ResponseEntity<Object> getMacroStatus() throws SchedulerException {
        Map<String, Object> res = getJobStatus("macroJob", "macroTrigger", 
            macroRepo.findTopByOrderByCreatedAtDesc().map(MacroTestRecord::getCreatedAt).orElse(null));
        return new ResponseEntity<>(SmsResponseBuilder.getSuccessResponse(res), HttpStatus.OK);
    }

    // ----------- CHEM ENDPOINTS -----------

    @PostMapping("/chem/trigger")
    public ResponseEntity<Object> triggerChemJob() throws SchedulerException {
        scheduler.triggerJob(JobKey.jobKey("chemJob"));
        return new ResponseEntity<>(SmsResponseBuilder.getSuccessResponse("CHEM job triggered manually."), HttpStatus.OK);
    }

    @GetMapping("/chem/status")
    public ResponseEntity<Object> getChemStatus() throws SchedulerException {
        Map<String, Object> res = getJobStatus("chemJob", "chemTrigger", 
            chemRepo.findTopByOrderByCreatedAtDesc().map(ChemTestRecord::getCreatedAt).orElse(null));
        return new ResponseEntity<>(SmsResponseBuilder.getSuccessResponse(res), HttpStatus.OK);
    }

    // ----------- TENSILE ENDPOINTS -----------

    @PostMapping("/tensile/trigger")
    public ResponseEntity<Object> triggerTensileJob() throws SchedulerException {
        scheduler.triggerJob(JobKey.jobKey("tensileJob"));
        return new ResponseEntity<>(SmsResponseBuilder.getSuccessResponse("TENSILE job triggered manually."), HttpStatus.OK);
    }

    @GetMapping("/tensile/status")
    public ResponseEntity<Object> getTensileStatus() throws SchedulerException {
        Map<String, Object> res = getJobStatus("tensileJob", "tensileTrigger", 
            tensileRepo.findTopByOrderByCreatedAtDesc().map(TensileTestRecord::getCreatedAt).orElse(null));
        return new ResponseEntity<>(SmsResponseBuilder.getSuccessResponse(res), HttpStatus.OK);
    }

    // ----------- DATA RETRIEVAL ENDPOINTS -----------

    @GetMapping("/fwt/data")
    public ResponseEntity<Object> getFwtData(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<FwtTestRecord> data;

        if (startDate != null && endDate != null) {
            LocalDate start = parseDate(startDate);
            LocalDate end = parseDate(endDate);
            data = fwtRepo.findByCreatedAtBetween(start, end, pageable);
        } else {
            data = fwtRepo.findAll(pageable);
        }

        return new ResponseEntity<>(SmsResponseBuilder.getSuccessResponse(data), HttpStatus.OK);
    }

    @GetMapping("/macro/data")
    public ResponseEntity<Object> getMacroData(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<MacroTestRecord> data;

        if (startDate != null && endDate != null) {
            LocalDate start = parseDate(startDate);
            LocalDate end = parseDate(endDate);
            data = macroRepo.findByCreatedAtBetween(start, end, pageable);
        } else {
            data = macroRepo.findAll(pageable);
        }

        return new ResponseEntity<>(SmsResponseBuilder.getSuccessResponse(data), HttpStatus.OK);
    }

    @GetMapping("/chem/data")
    public ResponseEntity<Object> getChemData(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<ChemTestRecord> data;

        if (startDate != null && endDate != null) {
            LocalDate start = parseDate(startDate);
            LocalDate end = parseDate(endDate);
            data = chemRepo.findByCreatedAtBetween(start, end, pageable);
        } else {
            data = chemRepo.findAll(pageable);
        }

        return new ResponseEntity<>(SmsResponseBuilder.getSuccessResponse(data), HttpStatus.OK);
    }

    @GetMapping("/tensile/data")
    public ResponseEntity<Object> getTensileData(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<TensileTestRecord> data;

        if (startDate != null && endDate != null) {
            LocalDate start = parseDate(startDate);
            LocalDate end = parseDate(endDate);
            data = tensileRepo.findByCreatedAtBetween(start, end, pageable);
        } else {
            data = tensileRepo.findAll(pageable);
        }

        return new ResponseEntity<>(SmsResponseBuilder.getSuccessResponse(data), HttpStatus.OK);
    }

    // ----------- COMMON STATUS HANDLER -----------

    private Map<String, Object> getJobStatus(String jobName, String triggerName, LocalDate lastDateSynced) throws SchedulerException {
        Trigger trigger = scheduler.getTrigger(TriggerKey.triggerKey(triggerName));
        Date nextFire = (trigger != null) ? trigger.getNextFireTime() : null;
        Date lastFire = (trigger != null) ? trigger.getPreviousFireTime() : null;

        boolean isRunning = scheduler.getCurrentlyExecutingJobs().stream()
            .anyMatch(ctx -> ctx.getJobDetail().getKey().getName().equals(jobName));

        Map<String, Object> status = new HashMap<>();
        status.put("lastRun", lastFire);
        status.put("nextRun", nextFire);
        status.put("lastDateSynced", lastDateSynced);
        status.put("status", isRunning ? "Running" : "Idle");

        return status;
    }

    // Helper method to parse date in multiple formats
    private LocalDate parseDate(String dateStr) {
        if (dateStr == null || dateStr.trim().isEmpty()) {
            return null;
        }

        try {
            // Try DD/MM/YYYY format first (from frontend)
            if (dateStr.contains("/")) {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
                return LocalDate.parse(dateStr, formatter);
            }
            // Try YYYY-MM-DD format (ISO format)
            else if (dateStr.contains("-")) {
                return LocalDate.parse(dateStr);
            }
            // Default to ISO format
            else {
                return LocalDate.parse(dateStr);
            }
        } catch (Exception e) {
            throw new RuntimeException("Invalid date format: " + dateStr + ". Expected DD/MM/YYYY or YYYY-MM-DD");
        }
    }
}
