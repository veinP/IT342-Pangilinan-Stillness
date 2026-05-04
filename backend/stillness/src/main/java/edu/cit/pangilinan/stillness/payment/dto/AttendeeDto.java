package edu.cit.pangilinan.stillness.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AttendeeDto {
    private String fullName;
    private String email;
    private String bookingNumber;
    private String status;
    private boolean paid;
}