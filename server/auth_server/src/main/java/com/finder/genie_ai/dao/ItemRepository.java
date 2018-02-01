package com.finder.genie_ai.dao;

import com.finder.genie_ai.model.game.Item.ItemModel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ItemRepository extends JpaRepository<ItemModel, Integer> {

    Optional<ItemModel> findByPlayerId(int playerId);

}
