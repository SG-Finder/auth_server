package com.finder.genie_ai.model.session_manage;

import java.io.Serializable;
import java.util.Objects;

public class SessionUserInfoModel implements Serializable {

    private static final long serialVersionUID = 2L;
    private String userId;
    private String token;
    private String userIp;

    public SessionUserInfoModel(String userId, String token, String userIp) {
        this.userId = userId;
        this.token = token;
        this.userIp = userIp;
    }

    public SessionUserInfoModel() {}

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

    public String getUserIp() {
        return userIp;
    }

    public void setUserIp(String userIp) {
        this.userIp = userIp;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SessionUserInfoModel that = (SessionUserInfoModel) o;
        return Objects.equals(userId, that.userId) &&
                Objects.equals(token, that.token) &&
                Objects.equals(userIp, that.userIp);
    }

    @Override
    public int hashCode() {

        return Objects.hash(userId, token, userIp);
    }

    @Override
    public String toString() {
        return "SessionUserInfoModel{" +
                "userId='" + userId + '\'' +
                ", token='" + token + '\'' +
                ", userIp='" + userIp + '\'' +
                '}';
    }

}
