package edu.cit.pangilinan.stillness.dto.response;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SessionDto {
    private UUID id;
    private String title;
    private String description;
    private InstructorDto instructor;
    private String type;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Integer capacity;
    private Integer bookedCount;
    private BigDecimal price;
    private String thumbnailUrl;
    private String location;
    private String status;
    private LocalDateTime createdAt;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class InstructorDto {
        private UUID id;
        private String fullName;
        private String profileImageUrl;
    }
}
