package com.finder.genie_ai.dao;

import com.finder.genie_ai.model.game.item_relation.WeaponRelation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import javax.swing.text.html.Option;
import java.util.List;
import java.util.Optional;

@Repository
public interface WeaponRelationRepository extends JpaRepository<WeaponRelation, Integer> {

    List<WeaponRelation> findByPlayerId(int playerId);
    List<WeaponRelation> findByWeaponId(int weaponId);
    Optional<WeaponRelation> findByPlayerIdAndWeaponId(int playerId, int weaponId);

    @Modifying
    @Query(value = "UPDATE weapon_relation SET usable_count = :usableCount WHERE player_id = :playerId AND weapon_id = :weaponId", nativeQuery = true)
    int updateWeaponRelation(@Param("usableCount") int usableCount,
                             @Param("playerId") int playerId,
                             @Param("weaponId") int weaponId);

}
