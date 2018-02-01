package com.finder.genie_ai.dao;

import com.finder.genie_ai.model.game.player.PlayerModel;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PlayerRepository extends MongoRepository<PlayerModel, Integer> {

    Optional<PlayerModel> findByNickname(String nickname);
    int deleteByUserId(String userId);
    int deleteByNickname(String nickname);

}
