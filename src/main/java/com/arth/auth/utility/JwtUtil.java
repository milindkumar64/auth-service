package com.arth.auth.utility;

import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.crypto.SecretKey;

import com.arth.auth.model.AuthProviderType.AuthProviderType;
import com.arth.auth.model.Role;
import com.arth.auth.model.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Component;

@Component
public class JwtUtil {

    public static final String CLAIM_USERNAME = "username";
    public static final String CLAIM_EMAIL = "email";
    public static final String CLAIM_ROLES = "roles";

    @Value("${security.jwt.secret}")
    private String jwtSecret;

    @Value("${security.jwt.expiration-ms}")
    private long jwtExpirationMs;

    @Value("${security.jwt.issuer}")
    private String issuer;

    private static final Logger log = LoggerFactory.getLogger(JwtUtil.class);

    public long getExpirationMs() {
        return jwtExpirationMs;
    }

    public String generateToken(User user) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + jwtExpirationMs);
        List<String> roles = extractRoleNames(user);

        return Jwts.builder()
                .setSubject(String.valueOf(user.getId()))
                .setId(UUID.randomUUID().toString())
                .setIssuer(issuer)
                .setIssuedAt(now)
                .setExpiration(expiry)
                .claim(CLAIM_USERNAME, user.getUsername())
                .claim(CLAIM_EMAIL, user.getEmail())
                .claim(CLAIM_ROLES, roles)
                .signWith(getSigningKey())
                .compact();
    }

    public Long extractUserId(String token) {
        return Long.parseLong(extractAllClaims(token).getSubject());
    }

    public String extractUsername(String token) {
        return extractAllClaims(token).get(CLAIM_USERNAME, String.class);
    }

    @SuppressWarnings("unchecked")
    public List<String> extractRoles(String token) {
        Object roles = extractAllClaims(token).get(CLAIM_ROLES);
        if (roles instanceof List<?> roleList) {
            return roleList.stream().map(String::valueOf).collect(Collectors.toList());
        }
        return Collections.emptyList();
    }

    public String extractJti(String token) {
        return extractAllClaims(token).getId();
    }

    public boolean validateToken(String token) {
        try {
            Claims claims = extractAllClaims(token);
            if (!issuer.equals(claims.getIssuer())) {
                log.warn("JWT issuer mismatch: expected {}, got {}", issuer, claims.getIssuer());
                return false;
            }
            return true;
        } catch (Exception e) {
            log.debug("JWT validation failed: {}", e.getMessage());
            return false;
        }
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .requireIssuer(issuer)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
    }

    private List<String> extractRoleNames(User user) {
        if (user.getRoles() == null || user.getRoles().isEmpty()) {
            return Collections.emptyList();
        }
        return user.getRoles().stream()
                .map(Role::getName)
                .collect(Collectors.toList());
    }

    public AuthProviderType getProviderTypeFromRegistrationId(String registrationId) {
        return switch (registrationId.toLowerCase()) {
            case "google" -> AuthProviderType.GOOGLE;
            case "github" -> AuthProviderType.GITHUB;
            case "facebook" -> AuthProviderType.FACEBOOK;
            default -> {
                log.error("Unsupported OAuth2 provider: {}", registrationId);
                throw new IllegalArgumentException("Unsupported OAuth2 provider: " + registrationId);
            }
        };
    }

    public String determineProviderIdFromOAuth2User(OAuth2User oAuth2User, String registrationId) {
        String providerId = switch (registrationId.toLowerCase()) {
            case "google" -> oAuth2User.getAttribute("sub");
            case "github" -> oAuth2User.getAttribute("id").toString();
            default -> {
                log.error("Unsupported OAuth2 provider: {}", registrationId);
                throw new IllegalArgumentException("Unsupported OAuth2 provider: " + registrationId);
            }
        };

        if (providerId == null || providerId.isBlank()) {
            log.error("Unable to determine providerId for provider: {}", registrationId);
            throw new IllegalArgumentException("Unable to determine providerId for OAuth2 login");
        }
        return providerId;
    }

    public String determineUsernameFromOAuth2User(OAuth2User oAuth2User, String registrationId, String providerId) {
        return switch (registrationId.toLowerCase()) {
            case "google" -> oAuth2User.getAttribute("name");
            case "github" -> oAuth2User.getAttribute("login");
            default -> providerId;
        };
    }

    public String determineEmailFromOAuth2User(OAuth2User oAuth2User, String registrationId, String providerId) {
        return switch (registrationId.toLowerCase()) {
            case "google" -> oAuth2User.getAttribute("email");
            case "github" -> oAuth2User.getAttribute("email");
            default -> providerId;
        };
    }
}
