package edu.cit.pangilinan.stillness.admin;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import edu.cit.pangilinan.stillness.session.SessionService;
import edu.cit.pangilinan.stillness.session.dto.SessionDto;
import edu.cit.pangilinan.stillness.shared.config.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/admin")
@PreAuthorize("hasAuthority('ROLE_INSTRUCTOR')")
public class AdminController {

    @Autowired
    private SessionService sessionService;

    @GetMapping("/sessions")
    public ResponseEntity<?> getAllSessions(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "100") int limit) {
        try {
            Pageable pageable = PageRequest.of(page, limit);
            Page<SessionDto> sessionPage = sessionService.getAllSessionsAdmin(pageable);
            
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
}
