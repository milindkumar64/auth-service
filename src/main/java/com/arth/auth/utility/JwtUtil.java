package com.arth.auth.utility;

import java.util.Date;

import com.arth.auth.model.AuthProviderType.AuthProviderType;
import com.arth.auth.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

@Component
public class JwtUtil {

    @Value("${security.jwt.secret}")
    private String jwtSecret;

    @Value("${security.jwt.expiration-ms}")
    private long jwtExpirationMs;

    private static final org.slf4j.Logger log = (Logger) LoggerFactory.getLogger(JwtUtil.class);

    public String generateToken(User user) {
        return Jwts.builder().setSubject(user.getUsername())
                .claim("role", user.getUsername())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis()+jwtExpirationMs))
                .signWith(SignatureAlgorithm.HS256, jwtSecret)
                .compact();
    }
    public String extractUsername(String token) {
        return Jwts.parser()
                .setSigningKey(jwtSecret)
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }
    public boolean validateToken(String token) {
        try {
            Jwts.parser().setSigningKey(jwtSecret).parseClaimsJws(token);
            return true;
        } catch (Exception e) {
            return false;
        }
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
/*        String email = oAuth2User.getAttribute("email");
        if (email != null && !email.isBlank()) {
            return email;
        }*/
        return switch (registrationId.toLowerCase()) {
            case "google" -> oAuth2User.getAttribute("name");
            case "github" -> oAuth2User.getAttribute("login");
            default -> providerId;
        };
    }

    public String determineEmailFromOAuth2User(OAuth2User oAuth2User, String registrationId, String providerId) {
        return switch (registrationId.toLowerCase()){
            case "google" -> oAuth2User.getAttribute("email");
            case "github" -> oAuth2User.getAttribute("email");
            default -> providerId;
        };
    }
}

