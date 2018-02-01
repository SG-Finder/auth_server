package com.finder.genie_ai.model.game.Item;

import com.finder.genie_ai.enumdata.Weapon;
import com.finder.genie_ai.model.game.player.PlayerModel;
import lombok.Data;

import javax.persistence.*;

@Entity
@Table(name = "items")
@Data
public class ItemModel {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    protected int id;
    @Enumerated(EnumType.STRING)
    @Column(name = "name", nullable = false)
    protected Weapon name;
    @Column(name = "damage", nullable = false)
    protected int damage;
    @Column(name = "price", nullable = false)
    protected int price;
    @Column(name = "usable_count", nullable = false)
    protected int usableCount;
    @ManyToOne
    @JoinColumn(name = "player_id", foreignKey = @ForeignKey(name = "FK_Items_Players"))
    protected PlayerModel playerId;

}
