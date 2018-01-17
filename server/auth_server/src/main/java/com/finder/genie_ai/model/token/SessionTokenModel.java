package com.finder.genie_ai.model.token;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;

public class SessionTokenModel implements Serializable {

    private static final long serialVersionUID = 1L;
    private String token;
    private String userId;
    private String ip;
    private LocalDateTime signinAt;

    public SessionTokenModel(String token, String userId, String ip, LocalDateTime signinAt) {
        this.token = token;
        this.userId = userId;
        this.ip = ip;
        this.signinAt = signinAt;
    }

    public SessionTokenModel() {}

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public LocalDateTime getSigninAt() {
        return signinAt;
    }

    public void setSigninAt(LocalDateTime signinAt) {
        this.signinAt = signinAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SessionTokenModel that = (SessionTokenModel) o;
        return Objects.equals(token, that.token) &&
                Objects.equals(userId, that.userId) &&
                Objects.equals(ip, that.ip) &&
                Objects.equals(signinAt, that.signinAt);
    }

    @Override
    public int hashCode() {

        return Objects.hash(token, userId, ip, signinAt);
    }

    @Override
    public String toString() {
        return "SessionTokenModel{" +
                "token='" + token + '\'' +
                ", userId='" + userId + '\'' +
                ", ip='" + ip + '\'' +
                ", signinAt=" + signinAt +
                '}';
    }
}
