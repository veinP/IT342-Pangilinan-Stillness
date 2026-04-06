package edu.cit.pangilinan.stillness.service;

import edu.cit.pangilinan.stillness.dto.request.LoginRequest;
import edu.cit.pangilinan.stillness.dto.request.RegisterRequest;
import edu.cit.pangilinan.stillness.dto.response.AuthResponse;
import edu.cit.pangilinan.stillness.dto.response.UserDto;
import edu.cit.pangilinan.stillness.exception.StillnessException;
import edu.cit.pangilinan.stillness.model.Instructor;
import edu.cit.pangilinan.stillness.model.RefreshToken;
import edu.cit.pangilinan.stillness.model.User;
import edu.cit.pangilinan.stillness.repository.InstructorRepository;
import edu.cit.pangilinan.stillness.repository.RefreshTokenRepository;
import edu.cit.pangilinan.stillness.repository.UserRepository;
import edu.cit.pangilinan.stillness.security.jwt.JwtProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final InstructorRepository instructorRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;
    private final AuthenticationManager authenticationManager;
    private final EmailService emailService;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw new StillnessException("VALID-001", "Passwords do not match", HttpStatus.BAD_REQUEST);
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new StillnessException("DB-002", "Email already registered", HttpStatus.CONFLICT);
        }

        String requestedRole = request.getRole() == null ? "ROLE_USER" : request.getRole().trim().toUpperCase();
        if ("INSTRUCTOR".equals(requestedRole)) requestedRole = "ROLE_INSTRUCTOR";
        if ("USER".equals(requestedRole)) requestedRole = "ROLE_USER";
        if (!"ROLE_INSTRUCTOR".equals(requestedRole)) requestedRole = "ROLE_USER";

        User user = User.builder()
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .fullName(request.getFullName())
                .role(requestedRole)
                .emailVerified(false)
                .build();

        user = userRepository.save(user);

        if ("ROLE_INSTRUCTOR".equals(requestedRole)) {
            instructorRepository.save(Instructor.builder()
                .user(user)
                .bio("")
                .specialty("Wellness")
                .profileImageUrl(null)
                .yearsExperience(0)
                .build());
        }

        UserDetails userDetails = new org.springframework.security.core.userdetails.User(
                user.getEmail(),
                user.getPasswordHash(),
                Collections.singletonList(new SimpleGrantedAuthority(user.getRole()))
        );

        String jwt = jwtProvider.generateToken(userDetails);
        String refreshToken = createRefreshToken(user);

        try {
            emailService.sendWelcomeEmail(user.getEmail(), user.getFullName());
        } catch (Exception ignored) {
            // Email failure should not prevent registration
        }

        return AuthResponse.builder()
                .user(mapToUserDto(user))
                .token(jwt)
                .refreshToken(refreshToken)
                .build();
    }

    @Transactional
    public AuthResponse login(LoginRequest request) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
            );
        } catch (BadCredentialsException e) {
            throw new StillnessException("AUTH-001", "Invalid credentials", HttpStatus.UNAUTHORIZED);
        }

        User user = userRepository.findByEmail(request.getEmail())
            .orElseThrow(() -> new StillnessException("AUTH-001", "Invalid credentials", HttpStatus.UNAUTHORIZED));

        user.setLastLoginAt(LocalDateTime.now());
        userRepository.save(user);

        UserDetails userDetails = new org.springframework.security.core.userdetails.User(
                user.getEmail(),
                user.getPasswordHash(),
                Collections.singletonList(new SimpleGrantedAuthority(user.getRole()))
        );

        String jwt = jwtProvider.generateToken(userDetails);
        String refreshToken = createRefreshToken(user);

        return AuthResponse.builder()
                .user(mapToUserDto(user))
                .token(jwt)
                .refreshToken(refreshToken)
                .build();
    }

    @Transactional
    public void logout(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new StillnessException("DB-001", "User not found", HttpStatus.NOT_FOUND));
        refreshTokenRepository.revokeAllByUserId(user.getId());
    }

    public UserDto getCurrentUser(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new StillnessException("DB-001", "User not found", HttpStatus.NOT_FOUND));
        return mapToUserDto(user);
    }

    private String createRefreshToken(User user) {
        RefreshToken refreshToken = RefreshToken.builder()
                .user(user)
                .token(UUID.randomUUID().toString())
                .expiryDate(LocalDateTime.now().plus(jwtProvider.getRefreshExpiration(), ChronoUnit.MILLIS))
                .revoked(false)
                .build();

        refreshTokenRepository.save(refreshToken);
        return refreshToken.getToken();
    }

    private UserDto mapToUserDto(User user) {
        return UserDto.builder()
                .id(user.getId())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .role(user.getRole())
                .profileImageUrl(user.getProfileImageUrl())
                .createdAt(user.getCreatedAt())
                .build();
    }
}
