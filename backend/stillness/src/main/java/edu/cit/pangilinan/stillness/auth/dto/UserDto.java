package edu.cit.pangilinan.stillness.auth.dto;

import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserDto {
    private UUID id;
    private String email;
    private String fullName;
    private String role;
    private String profileImageUrl;
    private LocalDateTime createdAt;
}
