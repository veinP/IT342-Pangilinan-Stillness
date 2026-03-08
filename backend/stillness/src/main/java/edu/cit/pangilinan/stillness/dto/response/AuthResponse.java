package edu.cit.pangilinan.stillness.dto.response;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuthResponse {
    private UserDto user;
    private String token;
    private String refreshToken;
}
