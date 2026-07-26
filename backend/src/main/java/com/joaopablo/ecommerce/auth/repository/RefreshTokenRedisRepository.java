package com.joaopablo.ecommerce.auth.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.time.Duration;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class RefreshTokenRedisRepository {

    private final RedisTemplate<String, Object> redisTemplate;


    private static final String PREFIX = "refresh_token:";


    public void save(
            String token,
            UUID userId,
            Duration expiration
    ) {

        redisTemplate.opsForValue()
                .set(
                        PREFIX + token,
                        userId,
                        expiration
                );
    }


    public UUID findUserIdByToken(String token) {

        Object value = redisTemplate.opsForValue()
                .get(PREFIX + token);

        if(value == null){
            return null;
        }

        return UUID.fromString(value.toString());
    }


    public void delete(String token){

        redisTemplate.delete(
                PREFIX + token
        );
    }
}