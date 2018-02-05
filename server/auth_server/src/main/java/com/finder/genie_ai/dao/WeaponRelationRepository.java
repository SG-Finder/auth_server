package com.finder.genie_ai.dao;

import com.finder.genie_ai.model.game.item_relation.WeaponRelation;
import com.finder.genie_ai.model.game.player.PlayerModel;
import com.finder.genie_ai.model.game.weapon.WeaponModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WeaponRelationRepository extends JpaRepository<WeaponRelation, Integer> {

    List<WeaponRelation> findByPlayerId(PlayerModel playerId);
    List<WeaponRelation> findByWeaponId(WeaponModel weaponId);
    Optional<WeaponRelation> findByPlayerIdAndWeaponId(PlayerModel playerId, WeaponModel weaponId);

}
