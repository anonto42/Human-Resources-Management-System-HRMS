package com.hrmf.hrms_backend.service;

import com.hrmf.hrms_backend.exception.CustomException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Service
@Slf4j
public class FileStorageService {

    @Value("${file.upload-dir:uploads}")
    private String uploadDir;

    @Value("${app.base-url:http://localhost:8080}")
    private String baseUrl;

    // Allowed file extensions
    private static final List<String> ALLOWED_EXTENSIONS = Arrays.asList(
            ".pdf", ".jpg", ".jpeg", ".png", ".doc", ".docx"
    );

    // Maximum file size (10MB)
    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024;

    // Store a file and return the generated filename
    public String storeFile(MultipartFile file, String subFolder) {
        try {
            // Validate file
            validateFile(file);

            // Generate unique filename with original extension
            String originalFileName = StringUtils.cleanPath(file.getOriginalFilename());
            String fileExtension = getFileExtension(originalFileName).toLowerCase();
            String uniqueFileName = generateUniqueFileName(fileExtension);

            // Create directory path
            Path uploadPath = Paths.get(uploadDir);
            if (subFolder != null && !subFolder.isEmpty()) {
                uploadPath = uploadPath.resolve(subFolder);
            }

            // Create directories if they don't exist
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
                log.info("Created directory: {}", uploadPath.toString());
            }

            // Copy file to target location
            Path targetLocation = uploadPath.resolve(uniqueFileName);
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

            log.info("File saved successfully: {}", targetLocation.toString());
            return uniqueFileName;

        } catch (IOException ex) {
            log.error("Could not store file: {}", ex.getMessage());
            throw new CustomException("Could not store file: " + ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // Generate file URL for accessing the file
    public String getFileUrl(String fileName, String subFolder) {
        String path = subFolder != null && !subFolder.isEmpty()
                ? subFolder + "/" + fileName
                : fileName;
        return baseUrl + "/api/v1/files/download/" + path;
    }

    // Get file path on server
    public Path getFilePath(String fileName, String subFolder) {
        Path uploadPath = Paths.get(uploadDir);
        if (subFolder != null && !subFolder.isEmpty()) {
            uploadPath = uploadPath.resolve(subFolder);
        }
        return uploadPath.resolve(fileName).normalize();
    }

    // Load file as byte array
    public byte[] loadFile(String fileName, String subFolder) throws IOException {
        Path filePath = getFilePath(fileName, subFolder);
        if (!Files.exists(filePath)) {
            throw new CustomException("File not found: " + fileName, HttpStatus.NOT_FOUND);
        }
        return Files.readAllBytes(filePath);
    }

    // Delete a file
    public void deleteFile(String fileName, String subFolder) {
        try {
            Path filePath = getFilePath(fileName, subFolder);
            if (Files.exists(filePath)) {
                Files.delete(filePath);
                log.info("File deleted: {}", filePath.toString());
            }
        } catch (IOException ex) {
            log.error("Could not delete file: {}", ex.getMessage());
        }
    }

    // Validate file before storing
    private void validateFile(MultipartFile file) {
        // Check if file is empty
        if (file.isEmpty()) {
            throw new CustomException("File is empty", HttpStatus.BAD_REQUEST);
        }

        // Check file size
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new CustomException("File size exceeds maximum limit of 10MB", HttpStatus.BAD_REQUEST);
        }

        // Check file extension
        String originalFileName = StringUtils.cleanPath(file.getOriginalFilename());
        String fileExtension = getFileExtension(originalFileName).toLowerCase();

        if (!ALLOWED_EXTENSIONS.contains(fileExtension)) {
            throw new CustomException("File type not allowed. Allowed types: PDF, JPG, JPEG, PNG, DOC, DOCX",
                    HttpStatus.BAD_REQUEST);
        }

        // Check file content type
        String contentType = file.getContentType();
        if (contentType == null ||
                (!contentType.startsWith("image/") &&
                        !contentType.equals("application/pdf") &&
                        !contentType.equals("application/msword") &&
                        !contentType.equals("application/vnd.openxmlformats-officedocument.wordprocessingml.document"))) {
            throw new CustomException("Invalid file content type", HttpStatus.BAD_REQUEST);
        }
    }

    // Generate unique filename with timestamp
    private String generateUniqueFileName(String fileExtension) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String uuid = UUID.randomUUID().toString().substring(0, 8);
        return "doc_" + timestamp + "_" + uuid + fileExtension;
    }

    // Get file extension
    private String getFileExtension(String fileName) {
        if (StringUtils.hasText(fileName) && fileName.contains(".")) {
            return fileName.substring(fileName.lastIndexOf("."));
        }
        return "";
    }
}