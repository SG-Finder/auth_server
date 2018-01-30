package com.finder.genie_ai.model.game.history;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class History {

    private int win;
    private int lose;
    private int oneShot;
    private int finder;

}
