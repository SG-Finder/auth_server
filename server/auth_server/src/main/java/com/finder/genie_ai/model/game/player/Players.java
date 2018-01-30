package com.finder.genie_ai.model.game.player;

import com.finder.genie_ai.enumdata.Tier;
import com.finder.genie_ai.model.game.Item.Item;
import com.finder.genie_ai.model.game.history.History;
import lombok.Data;
import org.springframework.data.annotation.Id;

@Data
public class Players {

    @Id
    private String id;
    private String userId;
    private Tier tier;
    private int score;
    private History history;
    private int rank;
    private Item item;
    private int point;

}
