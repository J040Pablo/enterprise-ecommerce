package com.joaopablo.ecommerce.auth.repository;

import com.joaopablo.ecommerce.auth.dto.internal.OAuthLoginCodePayload;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.time.Duration;

@Repository
@RequiredArgsConstructor
public class OAuthLoginCodeRedisRepository {

    private static final String PREFIX = "oauth_login_code:";

    private final RedisTemplate<String, Object> redisTemplate;

    public void save(String code, OAuthLoginCodePayload payload, Duration ttl) {
        redisTemplate.opsForValue().set(PREFIX + code, payload, ttl);
    }

    /**
     * Atomically reads and deletes the payload (single-use).
     *
     * @return payload or {@code null} if missing/expired
     */
    public OAuthLoginCodePayload consume(String code) {
        Object value = redisTemplate.opsForValue().getAndDelete(PREFIX + code);
        if (value == null) {
            return null;
        }
        if (value instanceof OAuthLoginCodePayload payload) {
            return payload;
        }
        // GenericJackson2JsonRedisSerializer may restore as LinkedHashMap depending on type info
        if (value instanceof java.util.Map<?, ?> map) {
            return mapToPayload(map);
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    private static OAuthLoginCodePayload mapToPayload(java.util.Map<?, ?> map) {
        Object userId = map.get("userId");
        Object roles = map.get("roles");
        return OAuthLoginCodePayload.builder()
                .accessToken((String) map.get("accessToken"))
                .refreshToken((String) map.get("refreshToken"))
                .type((String) map.get("type"))
                .expiresIn(map.get("expiresIn") instanceof Number n ? n.longValue() : 0L)
                .userId(userId == null ? null : java.util.UUID.fromString(userId.toString()))
                .firstName((String) map.get("firstName"))
                .lastName((String) map.get("lastName"))
                .email((String) map.get("email"))
                .roles(roles instanceof java.util.List<?> list
                        ? (java.util.List<String>) list
                        : java.util.List.of())
                .build();
    }
}
