package com.sarthi.Sms.controller;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping({"/sms/public"})
public class PublicImageController {

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
                // Determine content type
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
                    .body(resource);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}
