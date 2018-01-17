package com.finder.genie_ai.model.session_manage;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;

public class SessionTokenModel implements Serializable {

    private static final long serialVersionUID = 1L;
    private String token;
    private LocalDateTime signinAt;

    public SessionTokenModel(String token, LocalDateTime signinAt) {
        this.token = token;
        this.signinAt = signinAt;
    }

    public SessionTokenModel() {}

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
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
                Objects.equals(signinAt, that.signinAt);
    }

    @Override
    public int hashCode() {

        return Objects.hash(token, signinAt);
    }

    @Override
    public String toString() {
        return "SessionTokenModel{" +
                "session_manage='" + token + '\'' +
                ", signinAt=" + signinAt +
                '}';
    }
}
