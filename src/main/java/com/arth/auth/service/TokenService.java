package com.arth.auth.service;

import com.arth.auth.dto.LoginResponse;
import com.arth.auth.model.RefreshToken;
import com.arth.auth.model.User;
import com.arth.auth.persist.RefreshTokenRepository;
import com.arth.auth.persist.UserRepository;
import com.arth.auth.utility.JwtUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;
import java.util.HexFormat;
import java.util.UUID;

@Service
public class TokenService {

    private static final Logger log = LoggerFactory.getLogger(TokenService.class);
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private final JwtUtil jwtUtil;
    private final RefreshTokenRepository refreshTokenRepository;
    private final UserRepository userRepository;

    @Value("${security.jwt.refresh-expiration-ms}")
    private long refreshExpirationMs;

    public TokenService(JwtUtil jwtUtil,
                        RefreshTokenRepository refreshTokenRepository,
                        UserRepository userRepository) {
        this.jwtUtil = jwtUtil;
        this.refreshTokenRepository = refreshTokenRepository;
        this.userRepository = userRepository;
    }

    public long getRefreshExpirationMs() {
        return refreshExpirationMs;
    }

    @Transactional
    public LoginResponse issueTokenPair(User user) {
        String accessToken = jwtUtil.generateToken(user);
        String rawRefreshToken = generateRawRefreshToken();
        String familyId = UUID.randomUUID().toString();
        persistRefreshToken(user.getId(), rawRefreshToken, familyId);
        return LoginResponse.of(
                accessToken,
                rawRefreshToken,
                user.getId(),
                jwtUtil.getExpirationMs(),
                refreshExpirationMs);
    }

    @Transactional
    public LoginResponse refresh(String rawRefreshToken) {
        RefreshToken stored = findStoredToken(rawRefreshToken);

        if (stored.isRevoked()) {
            refreshTokenRepository.revokeAllByFamilyId(stored.getFamilyId());
            log.warn("Refresh token reuse detected for user id {}", stored.getUserId());
            throw new BadCredentialsException("Invalid refresh token");
        }

        if (stored.getExpiresAt().isBefore(Instant.now())) {
            stored.setRevoked(true);
            refreshTokenRepository.save(stored);
            throw new BadCredentialsException("Refresh token expired");
        }

        stored.setRevoked(true);
        refreshTokenRepository.save(stored);

        User user = userRepository.findByIdWithRoles(stored.getUserId())
                .orElseThrow(() -> new BadCredentialsException("User not found"));

        String accessToken = jwtUtil.generateToken(user);
        String newRawRefreshToken = generateRawRefreshToken();
        persistRefreshToken(user.getId(), newRawRefreshToken, stored.getFamilyId());

        return LoginResponse.of(
                accessToken,
                newRawRefreshToken,
                user.getId(),
                jwtUtil.getExpirationMs(),
                refreshExpirationMs);
    }

    @Transactional
    public void revoke(String rawRefreshToken) {
        refreshTokenRepository.findByTokenHash(hashToken(rawRefreshToken))
                .ifPresent(token -> {
                    token.setRevoked(true);
                    refreshTokenRepository.save(token);
                });
    }

    private RefreshToken findStoredToken(String rawRefreshToken) {
        return refreshTokenRepository.findByTokenHash(hashToken(rawRefreshToken))
                .orElseThrow(() -> new BadCredentialsException("Invalid refresh token"));
    }

    private void persistRefreshToken(Long userId, String rawRefreshToken, String familyId) {
        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setUserId(userId);
        refreshToken.setTokenHash(hashToken(rawRefreshToken));
        refreshToken.setFamilyId(familyId);
        refreshToken.setExpiresAt(Instant.now().plusMillis(refreshExpirationMs));
        refreshTokenRepository.save(refreshToken);
    }

    private String generateRawRefreshToken() {
        byte[] bytes = new byte[32];
        SECURE_RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private String hashToken(String rawToken) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(rawToken.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }
}
