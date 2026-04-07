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
public class BookingDto {
    private UUID id;
    private String bookingNumber;
    private UUID userId;
    private SessionDto session;
    private String status;
    private LocalDateTime bookedAt;
    private LocalDateTime cancelledAt;
    private String cancellationReason;
    private String attendeeNotes;
    private BigDecimal amount;
    private String paymentStatus;
    private String paymentIntentId;
}
