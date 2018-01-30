package com.finder.genie_ai.redis_dao;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.ScanParams;
import redis.clients.jedis.ScanResult;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Repository
@Transactional
public class SessionTokenRedisRepository {

    private static final String SESSION = "session";
    public static final int TOKENKEY = 0;
    public static final int USERKEY = 1;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;
    private ValueOperations<String, String> valueOps;

    @PostConstruct
    public void init() {
        this.valueOps = redisTemplate.opsForValue();
    }

    private List<String> scanRedis(String value, int method) {
        StringBuffer key = new StringBuffer();
        key.append("session:");
        if (method == TOKENKEY) {
            key.append(value).append(":*");
        }
        else if (method == USERKEY) {
            key.append("*:").append(value);
        }

        Jedis jedis = new Jedis("localhost", 6379);
        ScanParams scanParams = new ScanParams().count(20).match(key.toString());
        String cursor = ScanParams.SCAN_POINTER_START;
        do {
            ScanResult<String> scanResult = jedis.scan(cursor, scanParams);
            if (!scanResult.getResult().isEmpty()) {
                scanResult.getResult().stream().forEach(System.out::println);
                return scanResult.getResult();
            }
            cursor = scanResult.getStringCursor();
        }
        while (!cursor.equals(ScanParams.SCAN_POINTER_START));

        return null;
    }

    public void saveSessionToken(String token, String userId, String data) {
        String key = SESSION + ":"  + token + ":" + userId;
        valueOps.set(key, data, 20L, TimeUnit.MINUTES);
    }

    public void updateSessionToken(String token, String userId, String data) {
        String key = SESSION + ":"  + token + ":" + userId;
        valueOps.set(key, data, 20L, TimeUnit.MINUTES);
    }

    public String findSessionToken(String token, String userId) {
        String key = SESSION + ":"  + token + ":" + userId;
        return valueOps.get(key);
    }

    public boolean isSessionValid(String token, String userId) {
        String key = SESSION + ":"  + token + ":" + userId;
        if (valueOps.get(key) == null) {
            return false;
        }
        else {
            return true;
        }
    }

    public boolean isSessionValidByUserId(String userId) {
        if (scanRedis(userId, USERKEY) == null) {
            return false;
        }
        else {
            return true;
        }
    }

    public boolean isSessionValidByToken(String token) {
        if (scanRedis(token, TOKENKEY) == null) {
            return false;
        }
        else {
            return true;
        }
    }

    public List<String> findAllSessionInfo() {
        //todo
        return null;
    }

    public boolean deleteSession(String token, String userId) {
        String key = SESSION + ":" + token + ":" + userId;
        return valueOps.getOperations().expire(key, 1L, TimeUnit.NANOSECONDS);
    }

}
