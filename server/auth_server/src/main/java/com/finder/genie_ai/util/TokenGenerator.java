package com.finder.genie_ai.util;

import java.io.UnsupportedEncodingException;
import java.util.Base64;

public class TokenGenerator {

    public static String generateSessionToken(String userId) throws UnsupportedEncodingException {
        String seedValue = userId + System.currentTimeMillis();
        byte[] targetBytes = seedValue.getBytes("utf-8");
        Base64.Encoder encoder = Base64.getEncoder();
        return encoder.encodeToString(targetBytes);
    }

    public static String generateSaltValue(String userId, String passwd) {

        return null;
    }

}
