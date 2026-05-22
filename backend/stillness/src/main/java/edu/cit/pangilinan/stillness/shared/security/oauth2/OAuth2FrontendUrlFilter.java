package edu.cit.pangilinan.stillness.shared.security.oauth2;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.net.URI;

@Component
@Slf4j
public class OAuth2FrontendUrlFilter extends OncePerRequestFilter {

    public static final String COOKIE_NAME = "oauth2_frontend_url";

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String requestURI = request.getRequestURI();
        if (requestURI.contains("/oauth2/authorization/")) {
            String origin = null;
            String frontendUrlParam = request.getParameter("frontend_url");

            if (frontendUrlParam != null && !frontendUrlParam.trim().isEmpty()) {
                origin = frontendUrlParam;
                log.info("OAuth2: Found frontendUrl parameter: {}", origin);
            } else {
                String referer = request.getHeader("Referer");
                if (referer != null && !referer.trim().isEmpty()) {
                    try {
                        URI uri = new URI(referer);
                        origin = uri.getScheme() + "://" + uri.getAuthority();
                        log.info("OAuth2: Extracted origin from Referer: {}", origin);
                    } catch (Exception e) {
                        log.warn("OAuth2: Failed to parse Referer header: {}", referer, e);
                    }
                }
            }

            if (origin != null && isValidFrontendUrl(origin)) {
                Cookie cookie = new Cookie(COOKIE_NAME, origin);
                cookie.setPath("/");
                cookie.setMaxAge(300); // 5 minutes
                cookie.setSecure(request.isSecure());
                cookie.setHttpOnly(true);
                response.addCookie(cookie);
                log.info("OAuth2: Successfully set oauth2_frontend_url cookie: {}", origin);
            } else if (origin != null) {
                log.warn("OAuth2: Blocked invalid/untrusted frontend URL: {}", origin);
            }
        }

        filterChain.doFilter(request, response);
    }

    private boolean isValidFrontendUrl(String url) {
        if (url == null) return false;
        try {
            URI uri = new URI(url);
            String host = uri.getHost();
            if (host == null) return false;
            return host.equals("localhost") ||
                   host.equals("127.0.0.1") ||
                   host.endsWith(".vercel.app") ||
                   host.equals("still-ness.vercel.app");
        } catch (Exception e) {
            return false;
        }
    }
}
