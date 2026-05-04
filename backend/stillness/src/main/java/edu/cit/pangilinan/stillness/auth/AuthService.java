package edu.cit.pangilinan.stillness.auth;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import edu.cit.pangilinan.stillness.auth.dto.LoginRequest;
import edu.cit.pangilinan.stillness.auth.dto.RegisterRequest;
import edu.cit.pangilinan.stillness.auth.dto.AuthResponse;
import edu.cit.pangilinan.stillness.auth.dto.UserDto;
import edu.cit.pangilinan.stillness.shared.exception.StillnessException;
import edu.cit.pangilinan.stillness.shared.model.Instructor;
import edu.cit.pangilinan.stillness.shared.model.RefreshToken;
import edu.cit.pangilinan.stillness.shared.model.User;
import edu.cit.pangilinan.stillness.shared.repository.InstructorRepository;
import edu.cit.pangilinan.stillness.shared.repository.RefreshTokenRepository;
import edu.cit.pangilinan.stillness.shared.repository.UserRepository;
import edu.cit.pangilinan.stillness.shared.security.jwt.JwtProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
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
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@SuppressWarnings("unchecked")
public class AuthService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final InstructorRepository instructorRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;
    private final AuthenticationManager authenticationManager;
    private final EmailService emailService;

    @Value("${spring.security.oauth2.client.registration.google.client-id}")
    private String googleClientId;

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

        // Send welcome email
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
    public AuthResponse googleLogin(Map<String, String> body) {
        String idTokenString = body.get("idToken");
        if (idTokenString == null) {
            throw new StillnessException("AUTH-002", "ID Token is missing", HttpStatus.BAD_REQUEST);
        }

        GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(new NetHttpTransport(), new GsonFactory())
                .setAudience(Collections.singletonList(googleClientId))
                .build();

        try {
            GoogleIdToken idToken = verifier.verify(idTokenString);
            if (idToken == null) {
                throw new StillnessException("AUTH-003", "Invalid ID Token", HttpStatus.UNAUTHORIZED);
            }

            GoogleIdToken.Payload payload = idToken.getPayload();
            String email = payload.getEmail();
            String name = (String) payload.get("name");
            String pictureUrl = (String) payload.get("picture");

            Optional<User> userOptional = userRepository.findByEmail(email);
            User user;
            if (userOptional.isPresent()) {
                user = userOptional.get();
                user.setLastLoginAt(LocalDateTime.now());
                userRepository.save(user);
            } else {
                user = User.builder()
                        .email(email)
                        .fullName(name)
                        .profileImageUrl(pictureUrl)
                        .passwordHash(passwordEncoder.encode(UUID.randomUUID().toString()))
                        .role("ROLE_USER")
                        .emailVerified(true)
                        .build();
                user = userRepository.save(user);
            }

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

        } catch (Exception e) {
            throw new StillnessException("AUTH-004", "Google verification failed: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
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
