package com.hrmf.hrms_backend.filter;

import com.hrmf.hrms_backend.service.JwtService;
import com.hrmf.hrms_backend.util.SecurityUtil;
import io.jsonwebtoken.MalformedJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;
    private final SecurityUtil securityUtil;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        final String authHeader = request.getHeader("Authorization");
        final String jwt;
        final String userEmail;

        String requestPath = request.getRequestURI();
        if (
                requestPath.startsWith("/swagger-ui/") ||
                requestPath.startsWith("/v3/api-docs/") ||
                requestPath.startsWith("/api/v1/files")
        )
        {
            filterChain.doFilter(request, response);
            return;
        }

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            log.warn("Missing or invalid Authorization header for request: {} {}",
                    request.getMethod(), request.getRequestURI());

            if (requiresAuthentication(request)) {
                sendUnauthorizedError(response, "Missing or invalid Authorization header");
                return;
            }

            filterChain.doFilter(request, response);
            return;
        }

        try {
            jwt = authHeader.substring(7);

            if (jwt == null || jwt.trim().isEmpty() || jwt.length() < 10) {
                log.warn("Invalid JWT token: token is empty or too short");
                sendUnauthorizedError(response, "Invalid token format");
                return;
            }

            userEmail = jwtService.extractUsername(jwt);

            if (userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                UserDetails userDetails = this.userDetailsService.loadUserByUsername(userEmail);

                if (jwtService.isTokenValid(jwt, userDetails)) {
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            userDetails,
                            null,
                            userDetails.getAuthorities()
                    );
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                } else {
                    log.warn("Invalid JWT token for user: {}", userEmail);
                    sendUnauthorizedError(response, "Token is invalid or expired");
                    return;
                }
            }
        } catch (MalformedJwtException e) {
            log.error("Malformed JWT token: {}", e.getMessage());
            sendUnauthorizedError(response, "Invalid token format. Token must be a valid JWT");
            return;
        } catch (io.jsonwebtoken.ExpiredJwtException e) {
            log.warn("Expired JWT token: {}", e.getMessage());
            sendUnauthorizedError(response, "Token has expired");
            return;
        } catch (io.jsonwebtoken.UnsupportedJwtException e) {
            log.error("Unsupported JWT token: {}", e.getMessage());
            sendUnauthorizedError(response, "Unsupported token format");
            return;
        } catch (io.jsonwebtoken.security.SignatureException e) {
            log.error("Invalid JWT signature: {}", e.getMessage());
            sendUnauthorizedError(response, "Invalid token signature");
            return;
        } catch (Exception e) {
            log.error("JWT processing error: {}", e.getMessage(), e);
            sendUnauthorizedError(response, "Token validation failed: " + e.getMessage());
            return;
        }

        filterChain.doFilter(request, response);
    }

    private boolean requiresAuthentication(HttpServletRequest request) {
        String path = request.getRequestURI();
        return path.startsWith("/api/v1/") &&
                !path.startsWith("/api/v1/auth/");
    }

    private void sendUnauthorizedError(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        String jsonResponse = String.format(
                "{\"timestamp\": \"%s\", \"status\": 401, \"error\": \"Unauthorized\", \"message\": \"%s\", \"path\": \"%s\"}",
                java.time.Instant.now().toString(),
                message,
                ""
        );

        response.getWriter().write(jsonResponse);
    }
}