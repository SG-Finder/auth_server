package com.finder.genie_ai.model.user.command;

import javax.validation.constraints.NotNull;
import java.util.Objects;

public class UserSignInCommand {

    @NotNull
    private String userId;
    @NotNull
    private String passwd;

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getPasswd() {
        return passwd;
    }

    public void setPasswd(String passwd) {
        this.passwd = passwd;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserSignInCommand that = (UserSignInCommand) o;
        return Objects.equals(userId, that.userId) &&
                Objects.equals(passwd, that.passwd);
    }

    @Override
    public int hashCode() {

        return Objects.hash(userId, passwd);
    }

    @Override
    public String toString() {
        return "UserSignInCommand{" +
                "userId='" + userId + '\'' +
                ", passwd='" + passwd + '\'' +
                '}';
    }
}
