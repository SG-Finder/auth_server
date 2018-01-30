package com.finder.genie_ai.model.game.Item;

import com.finder.genie_ai.model.game.weapon.Gun;
import com.finder.genie_ai.model.game.weapon.Knife;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Item {

    private Knife knife;
    private Gun gun;

}
