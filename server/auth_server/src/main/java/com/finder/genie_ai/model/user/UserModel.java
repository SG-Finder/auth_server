package com.finder.genie_ai.model.user;

import com.finder.genie_ai.enumdata.Gender;

import javax.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "users")
public class UserModel {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int id;
    @Column(name = "user_id", length = 60, nullable = false, unique = true)
    private String userId;
    @Column(name = "passwd", length = 200, nullable = false)
    private String passwd;
    @Column(name = "salt", length = 60, nullable = false)
    private String salt;
    @Column(name = "user_name", length = 40, nullable = false)
    private String userName;
    @Column(name = "email", length = 60, nullable = false)
    private String email;
    @Column(name = "birth", columnDefinition = "date", nullable = false)
    private LocalDate birth;
    @Column(name = "gender", nullable = false)
    @Enumerated(EnumType.STRING)
    private Gender gender;
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    @Column(name = "introduce", length = 128)
    private String introduce;

    @PrePersist
    public void persist() {
        this.createdAt = LocalDateTime.now();
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

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

    public String getSalt() {
        return salt;
    }

    public void setSalt(String salt) {
        this.salt = salt;
    }

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

    public LocalDate getBirth() {
        return birth;
    }

    public void setBirth(LocalDate birth) {
        this.birth = birth;
    }

    public Gender getGender() {
        return gender;
    }

    public void setGender(Gender gender) {
        this.gender = gender;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
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
        UserModel userModel = (UserModel) o;
        return id == userModel.id &&
                Objects.equals(userId, userModel.userId) &&
                Objects.equals(passwd, userModel.passwd) &&
                Objects.equals(salt, userModel.salt) &&
                Objects.equals(userName, userModel.userName) &&
                Objects.equals(email, userModel.email) &&
                Objects.equals(birth, userModel.birth) &&
                gender == userModel.gender &&
                Objects.equals(createdAt, userModel.createdAt) &&
                Objects.equals(introduce, userModel.introduce);
    }

    @Override
    public int hashCode() {

        return Objects.hash(id, userId, passwd, salt, userName, email, birth, gender, createdAt, introduce);
    }

    @Override
    public String toString() {
        return "UserModel{" +
                "id=" + id +
                ", userId='" + userId + '\'' +
                ", passwd='" + passwd + '\'' +
                ", salt='" + salt + '\'' +
                ", userName='" + userName + '\'' +
                ", email='" + email + '\'' +
                ", birth=" + birth +
                ", gender=" + gender +
                ", createdAt=" + createdAt +
                ", introduce='" + introduce + '\'' +
                '}';
    }
}
