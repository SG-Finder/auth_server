package com.finder.genie_ai.dao;

import com.finder.genie_ai.model.game.Item.ItemModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ItemRepository extends JpaRepository<ItemModel, Integer> {

    Optional<ItemModel> findByPlayerId(int playerId);

}
