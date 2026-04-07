package edu.cit.pangilinan.stillness.security.oauth2;

import edu.cit.pangilinan.stillness.model.User;
import edu.cit.pangilinan.stillness.repository.UserRepository;
import edu.cit.pangilinan.stillness.security.jwt.JwtProvider;
import edu.cit.pangilinan.stillness.service.EmailService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Collections;

@Component
@RequiredArgsConstructor
@SuppressWarnings("unchecked")
public class OAuth2SuccessHandler implements AuthenticationSuccessHandler {

    private final UserRepository userRepository;
    private final JwtProvider jwtProvider;
    private final EmailService emailService;

    @Value("${app.frontend-url}")
    private String frontendUrl;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException {
        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();

        String email = oAuth2User.getAttribute("email");
        String name = oAuth2User.getAttribute("name");
        String googleId = oAuth2User.getAttribute("sub");

        boolean isNewUser = !userRepository.existsByGoogleId(googleId);

        User user = userRepository.findByGoogleId(googleId)
                .orElseGet(() -> {
                    User newUser = User.builder()
                            .email(email)
                            .fullName(name)
                            .googleId(googleId)
                            .role("ROLE_USER")
                            .emailVerified(true)
                            .build();
                    return userRepository.save(newUser);
                });

        // Send welcome email only for new users
        if (isNewUser) {
            try {
                emailService.sendWelcomeEmail(user.getEmail(), user.getFullName());
            } catch (Exception ignored) {
                // Email failure should not prevent OAuth login
            }
        }

        org.springframework.security.core.userdetails.User userDetails =
                new org.springframework.security.core.userdetails.User(
                        user.getEmail(),
                        "",
                        Collections.singletonList(new SimpleGrantedAuthority(user.getRole()))
                );

        String jwt = jwtProvider.generateToken(userDetails);

        response.sendRedirect(frontendUrl + "/oauth2/callback?token=" + jwt);
    }
}
