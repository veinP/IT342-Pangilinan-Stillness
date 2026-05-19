package edu.cit.pangilinan.stillness.session;

import edu.cit.pangilinan.stillness.session.dto.CreateSessionRequest;
import edu.cit.pangilinan.stillness.shared.config.ApiResponse;
import edu.cit.pangilinan.stillness.session.dto.SessionDto;
import edu.cit.pangilinan.stillness.session.dto.SessionDetailDto;
import edu.cit.pangilinan.stillness.shared.model.User;
import edu.cit.pangilinan.stillness.shared.repository.UserRepository;
import edu.cit.pangilinan.stillness.session.SessionService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@RestController
@RequestMapping("/sessions")
public class SessionController {

    @Autowired
    private SessionService sessionService;

    @Autowired
    private UserRepository userRepository;

    private static final Set<String> ALLOWED_IMAGE_TYPES = Set.of(
            "image/jpeg", "image/png", "image/webp", "image/gif"
    );
    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024; // 5MB

    /**
     * Get current authenticated user from security context
     */
    private User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated()) {
            String username = auth.getName();
            return userRepository.findByEmail(username).orElse(null);
        }
        return null;
    }

    @GetMapping
    public ResponseEntity<?> getAllSessions(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int limit) {
        
        try {
            Pageable pageable = PageRequest.of(page, limit);
            Page<SessionDto> sessionPage = sessionService.getAllSessions(pageable);
            
            Map<String, Object> response = new HashMap<>();
            response.put("sessions", sessionPage.getContent());
            response.put("pagination", new HashMap<String, Object>() {{
                put("page", page);
                put("limit", limit);
                put("total", sessionPage.getTotalElements());
                put("pages", sessionPage.getTotalPages());
            }});
            
            return ResponseEntity.ok(ApiResponse.builder()
                    .success(true)
                    .data(response)
                    .timestamp(LocalDateTime.now().toString())
                    .build());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("SESSION_FETCH_FAILED", e.getMessage()));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getSessionById(@PathVariable UUID id) {
        try {
            SessionDetailDto session = sessionService.getSessionByIdDetail(id);
            if (session == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.error("SESSION_NOT_FOUND", "Session not found"));
            }
            return ResponseEntity.ok(ApiResponse.builder()
                    .success(true)
                    .data(session)
                    .timestamp(LocalDateTime.now().toString())
                    .build());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("SESSION_FETCH_FAILED", e.getMessage()));
        }
    }

    @PostMapping
    public ResponseEntity<?> createSession(@Valid @RequestBody CreateSessionRequest request) {
        try {
            User currentUser = getCurrentUser();
            if (currentUser == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(ApiResponse.error("UNAUTHORIZED", "User must be authenticated to create sessions"));
            }

            SessionDetailDto session = sessionService.createSession(request, currentUser);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.builder()
                            .success(true)
                            .data(session)
                            .timestamp(LocalDateTime.now().toString())
                            .build());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("SESSION_CREATION_FAILED", e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateSession(
            @PathVariable UUID id,
            @Valid @RequestBody CreateSessionRequest request) {
        try {
            User currentUser = getCurrentUser();
            SessionDetailDto session = sessionService.updateSession(id, request, currentUser);
            if (session == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.error("SESSION_NOT_FOUND", "Session not found"));
            }
            return ResponseEntity.ok(ApiResponse.builder()
                    .success(true)
                    .data(session)
                    .timestamp(LocalDateTime.now().toString())
                    .build());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("SESSION_UPDATE_FAILED", e.getMessage()));
        }
    }

    @GetMapping(value = "/{id}/thumbnail", produces = {MediaType.IMAGE_JPEG_VALUE, MediaType.IMAGE_PNG_VALUE, "image/webp", "image/gif"})
    public ResponseEntity<byte[]> getThumbnail(@PathVariable UUID id) {
        String dataUrl = sessionService.getThumbnailData(id);
        if (dataUrl == null || !dataUrl.startsWith("data:")) {
            return ResponseEntity.notFound().build();
        }

        try {
            // Parse "data:image/jpeg;base64,/9j/4AAQ..."
            int commaIndex = dataUrl.indexOf(',');
            if (commaIndex < 0) return ResponseEntity.notFound().build();

            String meta = dataUrl.substring(5, commaIndex); // "image/jpeg;base64"
            String contentType = meta.split(";")[0];          // "image/jpeg"
            String base64Data = dataUrl.substring(commaIndex + 1);

            byte[] imageBytes = Base64.getDecoder().decode(base64Data);

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header("Cache-Control", "public, max-age=86400") // cache 24h
                    .body(imageBytes);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteSession(@PathVariable UUID id) {
        try {
            boolean deleted = sessionService.deleteSession(id);
            if (!deleted) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.error("SESSION_NOT_FOUND", "Session not found"));
            }
            return ResponseEntity.ok(ApiResponse.builder()
                    .success(true)
                    .data(null)
                    .timestamp(LocalDateTime.now().toString())
                    .build());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("SESSION_DELETE_FAILED", e.getMessage()));
        }
    }

    @PostMapping(value = "/{id}/thumbnail", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> uploadThumbnail(
            @PathVariable UUID id,
            @RequestParam("file") MultipartFile file) {
        try {
            // Validate the file
            if (file.isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(ApiResponse.error("INVALID_FILE", "File is empty"));
            }
            String contentType = file.getContentType();
            if (contentType == null || !ALLOWED_IMAGE_TYPES.contains(contentType)) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(ApiResponse.error("INVALID_FILE", "Only JPEG, PNG, WebP, and GIF images are allowed"));
            }
            if (file.getSize() > MAX_FILE_SIZE) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(ApiResponse.error("INVALID_FILE", "File too large. Maximum size is 5MB"));
            }

            // Convert to base64 data URL and store directly in the database
            byte[] bytes = file.getBytes();
            String base64 = Base64.getEncoder().encodeToString(bytes);
            String thumbnailUrl = "data:" + contentType + ";base64," + base64;

            SessionDetailDto updated = sessionService.updateThumbnailUrl(id, thumbnailUrl);
            if (updated == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.error("SESSION_NOT_FOUND", "Session not found"));
            }

            return ResponseEntity.ok(ApiResponse.builder()
                    .success(true)
                    .data(updated)
                    .timestamp(LocalDateTime.now().toString())
                    .build());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("UPLOAD_FAILED", e.getMessage()));
        }
    }
}
