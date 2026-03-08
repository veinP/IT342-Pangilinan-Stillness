package edu.cit.pangilinan.stillness.dto.response;

import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookingDto {
    private UUID id;
    private String bookingNumber;
    private UUID userId;
    private UUID sessionId;
    private String status;
    private LocalDateTime bookedAt;
    private LocalDateTime cancelledAt;
    private String cancellationReason;
    private String attendeeNotes;
}
