package com.sarthi.Sms.controller;

import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PathVariable;

import com.sarthi.Sms.dto.dashboard.AiDashboardSummaryDto;
import com.sarthi.Sms.dto.dashboard.AiSystemRailDto;
import com.sarthi.Sms.dto.dashboard.DimensionalInspectionReqDto;
import com.sarthi.Sms.dto.dashboard.DimensionalInspectionResDto;
import com.sarthi.Sms.dto.dashboard.SurfaceInspectionReqDto;
import com.sarthi.Sms.dto.dashboard.SurfaceInspectionResDto;
import com.sarthi.Sms.service.RailMillDashboardService;
import com.sarthi.Sms.util.SmsResponseBuilder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import jakarta.servlet.http.HttpServletRequest;


@RestController
@RequestMapping({"/sms/dashboard"})
public class DashboardController {

    @Autowired
    RailMillDashboardService ds;

    @PostMapping("/getDimensionalInspectionDtls")
    public ResponseEntity<Object> getDimensionalInspectionDtls(@RequestBody DimensionalInspectionReqDto req ) {
        List<String> res = ds.getDimensionalInspectionDtls(req);
        Map<String, List<String> > res1 = new HashMap<>();
        res1.put("railIdList", res);
        return new ResponseEntity<Object>(SmsResponseBuilder.getSuccessResponse(res1), HttpStatus.OK);
    }

    @GetMapping("/getDimensionalInspectionDtlByRailId")
    public ResponseEntity<Object> getDimensionalInspectionDtlByRailId(@RequestParam String railId) {
        List<DimensionalInspectionResDto> res = ds.getDimensionalInspectionDtlByRailId(railId);
        return new ResponseEntity<Object>(SmsResponseBuilder.getSuccessResponse(res), HttpStatus.OK);
    }

    @PostMapping("/getSurfaceInspectionDtls")
    public ResponseEntity<Object> getSurfaceInspectionDtls(@RequestBody SurfaceInspectionReqDto req ) {
        List<String> res = ds.getSurfaceInspectionDtls(req);
        Map<String, List<String> > res1 = new HashMap<>();
        res1.put("railIdList", res);
        return new ResponseEntity<Object>(SmsResponseBuilder.getSuccessResponse(res1), HttpStatus.OK);
    }

    @GetMapping("/getSurfaceInspectionDtlByRailId")
    public ResponseEntity<Object> getSurfaceInspectionDtlByRailId(@RequestParam String railId) {
        List<SurfaceInspectionResDto> res = ds.getSurfaceInspectionDtlByRailId(railId);
        return new ResponseEntity<Object>(SmsResponseBuilder.getSuccessResponse(res), HttpStatus.OK);
    }

    @PostMapping("/getAiSystemRailsWithHeatNumbers")
    public ResponseEntity<Object> getAiSystemRailsWithHeatNumbers(@RequestBody DimensionalInspectionReqDto req) {
        List<AiSystemRailDto> res = ds.getAiSystemRailsWithHeatNumbers(req);
        return new ResponseEntity<Object>(SmsResponseBuilder.getSuccessResponse(res), HttpStatus.OK);
    }

    @PostMapping("/getAiAccuracyMetrics")
    public ResponseEntity<Object> getAiAccuracyMetrics(@RequestBody DimensionalInspectionReqDto req) {
        AiDashboardSummaryDto res = ds.getAiAccuracyMetrics(req);
        return new ResponseEntity<Object>(SmsResponseBuilder.getSuccessResponse(res), HttpStatus.OK);
    }

    @GetMapping("/images")
    public ResponseEntity<Resource> getImage(@RequestParam String path) {
        try {
            // URL decode the path first (frontend sends URL-encoded paths)
            String decodedPath = java.net.URLDecoder.decode(path, "UTF-8");

            // Remove quotes if present
            String cleanPath = decodedPath.replace("'", "").replace("\"", "");

            // Create Path object from the absolute path
            Path imagePath = Paths.get(cleanPath).normalize();

            // Security check: prevent directory traversal attacks
            if (!imagePath.isAbsolute()) {
                return ResponseEntity.badRequest().build();
            }

            Resource resource = new UrlResource(imagePath.toUri());

            if (resource.exists() && resource.isReadable()) {
                String contentType = Files.probeContentType(imagePath);
                if (contentType == null) {
                    // Fallback based on file extension
                    String fileName = imagePath.getFileName().toString().toLowerCase();
                    if (fileName.endsWith(".png")) {
                        contentType = "image/png";
                    } else if (fileName.endsWith(".jpg") || fileName.endsWith(".jpeg")) {
                        contentType = "image/jpeg";
                    } else if (fileName.endsWith(".gif")) {
                        contentType = "image/gif";
                    } else if (fileName.endsWith(".bmp")) {
                        contentType = "image/bmp";
                    } else if (fileName.endsWith(".webp")) {
                        contentType = "image/webp";
                    } else {
                        contentType = "image/jpeg"; // Ultimate fallback
                    }
                }

                return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + resource.getFilename() + "\"")
                    .header(HttpHeaders.CACHE_CONTROL, "max-age=3600")
                    .header(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "*")
                    .header(HttpHeaders.ACCESS_CONTROL_ALLOW_METHODS, "GET")
                    .body(resource);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }


}