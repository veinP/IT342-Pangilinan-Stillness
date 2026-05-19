package edu.cit.pangilinan.stillness.shared.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Set;
import java.util.UUID;

@Service
public class FileStorageService {

    private static final Set<String> ALLOWED_TYPES = Set.of(
            "image/jpeg", "image/png", "image/webp", "image/gif"
    );
    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024; // 5MB

    @Value("${app.file.upload-dir:uploads}")
    private String uploadDir;

    private Path uploadPath;

    @PostConstruct
    public void init() {
        uploadPath = Paths.get(uploadDir).toAbsolutePath().normalize();
        try {
            Files.createDirectories(uploadPath);
        } catch (IOException e) {
            throw new RuntimeException("Could not create upload directory: " + uploadPath, e);
        }
    }

    /**
     * Store a file and return the generated filename (not the full path).
     */
    public String store(MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("Cannot store empty file");
        }

        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_TYPES.contains(contentType)) {
            throw new IllegalArgumentException("File type not allowed. Accepted: JPEG, PNG, WebP, GIF");
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException("File too large. Maximum size is 5MB");
        }

        // Generate a unique filename preserving the original extension
        String originalFilename = file.getOriginalFilename();
        String extension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        String storedFilename = UUID.randomUUID() + extension;

        Path targetLocation = uploadPath.resolve(storedFilename).normalize();

        // Security: ensure we're writing inside the upload directory
        if (!targetLocation.startsWith(uploadPath)) {
            throw new IllegalArgumentException("Cannot store file outside upload directory");
        }

        Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);
        return storedFilename;
    }

    /**
     * Delete a previously stored file by its filename.
     */
    public void delete(String filename) {
        if (filename == null || filename.isBlank()) return;
        try {
            Path filePath = uploadPath.resolve(filename).normalize();
            if (filePath.startsWith(uploadPath)) {
                Files.deleteIfExists(filePath);
            }
        } catch (IOException ignored) {
            // Best-effort deletion
        }
    }

    public Path getUploadPath() {
        return uploadPath;
    }
}
