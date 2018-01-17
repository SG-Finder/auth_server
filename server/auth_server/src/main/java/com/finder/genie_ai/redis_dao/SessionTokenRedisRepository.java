package com.finder.genie_ai.redis_dao;

import com.finder.genie_ai.model.token.SessionTokenModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import java.util.Map;

@Repository
@Transactional
public class SessionTokenRedisRepository {

    private static final String KEY = "sessions";
    @Autowired
    private RedisTemplate<String, String> redisTemplate;
    private HashOperations<String, String, String> hashOps;

    @PostConstruct
    public void init() {
        this.hashOps = redisTemplate.opsForHash();
    }

    public void saveSessionToken(String token, String userInfo) {
        hashOps.putIfAbsent(KEY, token, userInfo);
    }

    public void updateSessionToken(String token, String userInfo) {
        hashOps.put(KEY, token, userInfo);
    }

    public String findSessionToken(String token) {
        return (String) redisTemplate.opsForHash().get(KEY, token);
    }

    public Map<String, String> findAllSessionTokens() {
        return hashOps.entries(KEY);
    }

    public long deleteSessionToken(String token) {
        return hashOps.delete(KEY, token);
    }
}
