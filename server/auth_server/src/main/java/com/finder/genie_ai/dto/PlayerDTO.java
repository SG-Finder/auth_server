package com.finder.genie_ai.dto;

import com.finder.genie_ai.enumdata.Tier;
import com.finder.genie_ai.enumdata.Weapon;
import com.finder.genie_ai.model.game.history.HistoryModel;
import com.google.gson.annotations.SerializedName;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class PlayerDTO {

    @SerializedName("nickname")
    private String nickname;

    @SerializedName("tier")
    private Tier tier;

    @SerializedName("score")
    private int score;

    @SerializedName("history")
    private HistoryDTO history;

    @SerializedName("weapon")
    private List<Weapon> weapons;

    @SerializedName("point")
    private int point;

}
