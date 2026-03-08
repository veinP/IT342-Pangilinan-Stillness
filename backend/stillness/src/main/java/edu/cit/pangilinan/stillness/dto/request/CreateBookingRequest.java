package edu.cit.pangilinan.stillness.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateBookingRequest {

    @NotNull(message = "Session ID is required")
    private UUID sessionId;

    private String attendeeNotes;
}
