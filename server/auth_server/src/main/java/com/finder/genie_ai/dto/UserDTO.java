package com.finder.genie_ai.dto;

import com.google.gson.annotations.SerializedName;
import lombok.Data;

import java.time.LocalDate;

@Data
public class UserDTO {

    @SerializedName("userId")
    private String userId;

    @SerializedName("userName")
    private String userName;

    @SerializedName("nickname")
    private String nickname;

    @SerializedName("email")
    private String email;

    @SerializedName("birth")
    private LocalDate birth;

    @SerializedName("introduce")
    private String introduce;

}
