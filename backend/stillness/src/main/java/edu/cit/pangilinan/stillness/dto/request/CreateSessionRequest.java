package edu.cit.pangilinan.stillness.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateSessionRequest {

    @NotBlank(message = "Title is required")
    private String title;

    private String description;

    private UUID instructorId;

    private String sessionType;

    @NotNull(message = "Start time is required")
    private LocalDateTime startTime;

    @NotNull(message = "End time is required")
    private LocalDateTime endTime;

    @NotNull(message = "Capacity is required")
    private Integer capacity;

    @NotNull(message = "Price is required")
    private BigDecimal price;

    private String location;
}
