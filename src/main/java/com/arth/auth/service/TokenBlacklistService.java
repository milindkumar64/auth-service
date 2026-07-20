package com.arth.auth.service;

import com.arth.auth.config.RedisConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
public class TokenBlacklistService {

    private static final Logger log = LoggerFactory.getLogger(TokenBlacklistService.class);
    private static final String KEY_PREFIX = "blacklist:jti:";

    private final RedisTemplate<String, String> redisTemplate;

    public TokenBlacklistService(
            @Qualifier(RedisConfig.TOKEN_BLACKLIST_REDIS_TEMPLATE)
            RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public void blacklist(String jti, long ttlSeconds) {
        try {
            redisTemplate.opsForValue().set(
                    KEY_PREFIX + jti,
                    "1",
                    Duration.ofSeconds(ttlSeconds));
            log.debug("Blacklisted access token jti={} for {}s", jti, ttlSeconds);
        } catch (Exception e) {
            log.error("Failed to blacklist jti={}: {}", jti, e.getMessage());
            throw new IllegalStateException("Unable to revoke access token", e);
        }
    }

    public boolean isBlacklisted(String jti) {
        try {
            return Boolean.TRUE.equals(redisTemplate.hasKey(KEY_PREFIX + jti));
        } catch (Exception e) {
            log.error("Redis blacklist check failed for jti={}: {}", jti, e.getMessage());
            throw new BadCredentialsException("Token validation unavailable");
        }
    }
}
