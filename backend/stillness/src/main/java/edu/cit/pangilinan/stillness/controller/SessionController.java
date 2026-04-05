package edu.cit.pangilinan.stillness.controller;

import edu.cit.pangilinan.stillness.dto.request.CreateSessionRequest;
import edu.cit.pangilinan.stillness.dto.response.ApiResponse;
import edu.cit.pangilinan.stillness.dto.response.SessionDto;
import edu.cit.pangilinan.stillness.dto.response.SessionDetailDto;
import edu.cit.pangilinan.stillness.model.User;
import edu.cit.pangilinan.stillness.repository.UserRepository;
import edu.cit.pangilinan.stillness.service.SessionService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/sessions")
public class SessionController {

    @Autowired
    private SessionService sessionService;

    @Autowired
    private UserRepository userRepository;

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
}
