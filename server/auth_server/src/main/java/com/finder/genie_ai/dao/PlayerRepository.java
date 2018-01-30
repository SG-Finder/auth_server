package com.finder.genie_ai.dao;

import com.finder.genie_ai.model.game.player.Players;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PlayerRepository extends MongoRepository<Players, String> {

    Players findByUserId(String userId);
    int deleteByUserId(String userId);

}
