package com.finder.genie_ai.model.game.Item;

import com.finder.genie_ai.model.game.player.PlayerModel;
import lombok.Data;

import javax.persistence.*;

@Entity
@Table(name = "items")
@Data
public class ItemModel {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int id;
    @Column(name = "name", length = 50, nullable = false)
    private String name;
    @Column(name = "damage", nullable = false)
    private int damage;
    @Column(name = "price", nullable = false)
    private int price;
    @Column(name = "usable_count", nullable = false)
    private int usableCount;
    @ManyToOne
    @JoinColumn(name = "id", foreignKey = @ForeignKey(name = "FK_Items_Players"), nullable = false)
    private PlayerModel playerId;

    @PrePersist
    public void persist() {
        this.usableCount = 0;
    }

}
