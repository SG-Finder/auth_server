package com.finder.genie_ai.dao;

import com.finder.genie_ai.model.game.player.PlayerModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PlayerRepository extends JpaRepository<PlayerModel, Integer> {

    PlayerModel findByNickname(String nickname);
    int deleteByNickname(String nickname);

}
