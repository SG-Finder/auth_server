package com.finder.genie_ai.model.user.command;

import com.finder.genie_ai.enumdata.Gender;

import javax.validation.constraints.NotNull;
import java.util.Objects;

public class UserChangeInfoCommand {

    @NotNull
    private String userName;
    @NotNull
    private String email;
    @NotNull
    private String birth;
    @NotNull
    private Gender gender;
    private String introduce;

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getBirth() {
        return birth;
    }

    public void setBirth(String birth) {
        this.birth = birth;
    }

    public Gender getGender() {
        return gender;
    }

    public void setGender(Gender gender) {
        this.gender = gender;
    }

    public String getIntroduce() {
        return introduce;
    }

    public void setIntroduce(String introduce) {
        this.introduce = introduce;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserChangeInfoCommand that = (UserChangeInfoCommand) o;
        return Objects.equals(userName, that.userName) &&
                Objects.equals(email, that.email) &&
                Objects.equals(birth, that.birth) &&
                gender == that.gender &&
                Objects.equals(introduce, that.introduce);
    }

    @Override
    public int hashCode() {

        return Objects.hash(userName, email, birth, gender, introduce);
    }

    @Override
    public String toString() {
        return "UserChangeInfoCommand{" +
                "userName='" + userName + '\'' +
                ", email='" + email + '\'' +
                ", birth='" + birth + '\'' +
                ", gender='" + gender + '\'' +
                ", introduce='" + introduce + '\'' +
                '}';
    }

}
