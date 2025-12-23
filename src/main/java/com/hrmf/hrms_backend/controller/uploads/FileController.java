package com.hrmf.hrms_backend.controller.uploads;

import com.hrmf.hrms_backend.service.FileStorageService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Locale;

@RestController
@RequestMapping("/api/v1/files")
@Slf4j
public class FileController {

    private final FileStorageService fileStorageService;

    public FileController(FileStorageService fileStorageService) {
        this.fileStorageService = fileStorageService;
    }

    @GetMapping("/download/**")
    public ResponseEntity<Resource> downloadFile(HttpServletRequest request) {
        try {
            // Get the full path after /download/
            String fullPath = request.getRequestURI()
                    .substring(request.getRequestURI().indexOf("/download/") + "/download/".length());

            log.info("Download requested for path: {}", fullPath);

            // Parse folder and filename
            String[] pathParts = fullPath.split("/");

            if (pathParts.length == 1) {
                // Only filename, no folder
                return downloadFileInternal("", pathParts[0], request);
            } else {
                // Has folder structure
                String filename = pathParts[pathParts.length - 1];
                String folder = String.join("/",
                        Arrays.copyOfRange(pathParts, 0, pathParts.length - 1));

                return downloadFileInternal(folder, filename, request);
            }

        } catch (Exception ex) {
            log.error("Error downloading file: {}", ex.getMessage(), ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    private ResponseEntity<Resource> downloadFileInternal(String folder, String filename, HttpServletRequest request) {
        try {
            // Get file path
            Path filePath = fileStorageService.getFilePath(filename, folder);

            // Check if file exists
            if (!Files.exists(filePath)) {
                log.error("File not found: {}", filePath);
                return ResponseEntity.notFound().build();
            }

            // Load file as resource
            Resource resource = new UrlResource(filePath.toUri());

            // Check if resource is readable
            if (!resource.exists() || !resource.isReadable()) {
                log.error("File is not readable: {}", filePath);
                return ResponseEntity.notFound().build();
            }

            // Determine content type
            String contentType = determineContentType(filename, request);

            // Set headers
            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + filename + "\"");
            headers.add(HttpHeaders.CACHE_CONTROL, "no-cache, no-store, must-revalidate");
            headers.add(HttpHeaders.PRAGMA, "no-cache");
            headers.add(HttpHeaders.EXPIRES, "0");

            return ResponseEntity.ok()
                    .headers(headers)
                    .contentType(MediaType.parseMediaType(contentType))
                    .body(resource);

        } catch (Exception ex) {
            log.error("Could not download file: {}", ex.getMessage(), ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    private String determineContentType(String filename, HttpServletRequest request) {
        try {
            Path filePath = Paths.get(filename);
            String contentType = Files.probeContentType(filePath);

            if (contentType != null) {
                return contentType;
            }
        } catch (IOException ignored) {
        }

        // Try from request
        String contentType = request.getServletContext().getMimeType(filename);

        if (contentType == null) {

            String lowerFilename = filename.toLowerCase(Locale.ROOT);

            if (lowerFilename.endsWith(".pdf")) {
                return "application/pdf";
            } else if (lowerFilename.endsWith(".png")) {
                return "image/png";
            } else if (lowerFilename.endsWith(".jpg") || filename.toLowerCase().endsWith(".jpeg")) {
                return "image/jpeg";
            } else if (lowerFilename.endsWith(".gif")) {
                return "image/gif";
            } else if (lowerFilename.endsWith(".doc")) {
                return "application/msword";
            } else if (lowerFilename.endsWith(".docx")) {
                return "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
            }

            // Default to octet-stream
            return "application/octet-stream";
        }

        return contentType;
    }
}