package com.finder.genie_ai.dao;

import com.finder.genie_ai.model.game.player.PlayerModel;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PlayerRepository extends MongoRepository<PlayerModel, Integer> {

    Optional<PlayerModel> findByNickname(String nickname);
    int deleteByUserId(String userId);
    int deleteByNickname(String nickname);

    //TODO make query for taking item
    @Modifying
    @Query(value = "UPDATE players SET point = :point WHERE player_id = :playerId", nativeQuery = true)
    int updatePlayerPoint(@Param("point") int point,
                          @Param("playerId") int playerId);
}
