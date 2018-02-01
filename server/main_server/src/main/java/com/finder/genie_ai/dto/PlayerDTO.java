package com.finder.genie_ai.dto;

import com.finder.genie_ai.enumdata.Tier;
import com.google.gson.annotations.SerializedName;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class PlayerDTO {

    @SerializedName("nickname")
    private String nickname;

    @SerializedName("tier")
    private Tier tier;

    @SerializedName("score")
    private int score;

    @SerializedName("win")
    private int win;

    @SerializedName("lose")
    private int lose;

    @SerializedName("oneShot")
    private int oneShot;

    @SerializedName("finder")
    private int finder;

    @SerializedName("lastWeekRank")
    private int lastWeekRank;

    @SerializedName("knife")
    private int knife;

    @SerializedName("gun")
    private int gun;

    @SerializedName("point")
    private int point;

}
