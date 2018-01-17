package com.finder.genie_ai.redis_dao;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

@Repository
@Transactional
public class SessionTokenRedisRepository {

    private static final String TOKEN = "sessions";
    private static final String USERID = "users";

    @Autowired
    private RedisTemplate<String, String> redisTemplate;
    private HashOperations<String, String, String> hashOps;

    @PostConstruct
    public void init() {
        this.hashOps = redisTemplate.opsForHash();
    }

    public void saveSessionToken(String token, String userId, String userInfo) {
        hashOps.putIfAbsent(TOKEN, token, LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'hh:mm:ss")));
        hashOps.putIfAbsent(USERID, userId, userInfo);
    }

    public void updateSessionToken(String token, String userId, String userInfo) {
        hashOps.put(TOKEN, token, LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'hh:mm:ss")));
        hashOps.put(USERID, userId, userInfo);
    }

    public String findSessionToken(String token) {
        return hashOps.get(TOKEN, token);
    }

    public String findSessionUserInfo(String userId) {
        return hashOps.get(USERID, userId);
    }

    public Map<String, String> findAllSessionTokens() {
        return hashOps.entries(TOKEN);
    }

    public Map<String, String> findAllSessionUserInfo() {
        return hashOps.entries(USERID);
    }

    public long deleteSessionToken(String token) {
        return hashOps.delete(TOKEN, token);
    }

    public long deleteSessionUserInfo(String userId) {
        return hashOps.delete(USERID, userId);
    }

    public boolean deleteSessionInfo(String token, String userId) {
        long count = deleteSessionToken(token) + deleteSessionUserInfo(userId);
        if (count >= 2) {
            return true;
        }
        else {
            return false;
        }
    }
}
