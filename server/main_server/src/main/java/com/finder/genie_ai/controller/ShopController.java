package com.finder.genie_ai.controller;

import com.finder.genie_ai.controller.command.DealCommand;
import com.finder.genie_ai.dao.PlayerRepository;
import com.finder.genie_ai.dao.WeaponRelationRepository;
import com.finder.genie_ai.dao.WeaponRepository;
import com.finder.genie_ai.dto.PlayerWeaponDTO;
import com.finder.genie_ai.dto.ShopDealDTO;
import com.finder.genie_ai.enumdata.Weapon;
import com.finder.genie_ai.exception.BadRequestException;
import com.finder.genie_ai.exception.UnauthorizedException;
import com.finder.genie_ai.model.game.item_relation.WeaponRelation;
import com.finder.genie_ai.model.game.player.PlayerModel;
import com.finder.genie_ai.model.game.weapon.WeaponModel;
import com.finder.genie_ai.redis_dao.SessionTokenRedisRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.transaction.Transactional;
import javax.validation.Valid;
import java.util.*;

@RestController
@RequestMapping(value = "/finder/shop")
public class ShopController {

    private SessionTokenRedisRepository sessionTokenRedisRepository;
    private PlayerRepository playerRepository;
    private WeaponRepository weaponRepository;
    private WeaponRelationRepository weaponRelationRepository;
    private ModelMapper modelMapper = new ModelMapper();

    @Autowired
    public ShopController(SessionTokenRedisRepository sessionTokenRedisRepository,
                          PlayerRepository playerRepository,
                          WeaponRepository weaponRepository,
                          WeaponRelationRepository weaponRelationRepository) {
        this.sessionTokenRedisRepository = sessionTokenRedisRepository;
        this.playerRepository = playerRepository;
        this.weaponRepository = weaponRepository;
        this.weaponRelationRepository = weaponRelationRepository;
    }

    @Transactional
    @RequestMapping(value = "", method = RequestMethod.POST)
     public ShopDealDTO activeShop(@RequestHeader("session-token") String token,
                                   @RequestHeader("userId") String userId,
                                   @RequestBody @Valid DealCommand command,
                                   BindingResult bindingResult) {
        if (!sessionTokenRedisRepository.isSessionValid(token, userId)) {
            throw new UnauthorizedException();
        }

        if (bindingResult.hasErrors()) {
            throw new BadRequestException("Not suitable parameter form");
        }
        Optional<PlayerModel> player = playerRepository.findByNickname(command.getNickname());
        Optional<WeaponModel> weapon = weaponRepository.findByName(command.getItem());
        Optional<WeaponRelation> weaponRelation = weaponRelationRepository.findByPlayerIdAndWeaponId(player.get().getId(), weapon.get().getId());
        int playerPoint = player.get().getPoint();
        int totalPrice = command.getCount() * weapon.get().getPrice();

        if (playerPoint < totalPrice) {
            throw new UnauthorizedException("doesn't enough money");
        }

        playerRepository.updatePlayerPoint(playerPoint - totalPrice, player.get().getId());

        if (weaponRelation.isPresent()) {
            weaponRelationRepository.updateWeaponRelation(weaponRelation.get().getUsableCount() + command.getCount(),
                    player.get().getId(),
                    weapon.get().getId());
        }
        else {
            WeaponRelation newWeaponRelation = new WeaponRelation();
            newWeaponRelation.setPlayer_id(player.get());
            newWeaponRelation.setWeapon_id(weapon.get());
            newWeaponRelation.setUsableCount(command.getCount());
            weaponRelationRepository.save(newWeaponRelation);
        }

        //TODO return new weapon info

//        ShopDealDTO shopDealDTO = new ShopDealDTO();
//        shopDealDTO.setNickname(player.get().getNickname());
//        shopDealDTO.setPoint(playerPoint - totalPrice);
//        List<WeaponRelation> listWeaponRelation = weaponRelationRepository.findByPlayerId(player.get().getId());
//        List<PlayerWeaponDTO> datas = new ArrayList<>(listWeaponRelation.size());

//        for (Iterator<WeaponRelation> iter = listWeaponRelation.iterator(); iter.hasNext();) {
//            WeaponRelation weaponRelationData = iter.next();
//            PlayerWeaponDTO data = modelMapper.map(weaponRelationData.getWeapon_id(), PlayerWeaponDTO.class);
//            data.setUsableCount();
//        }

        return null;
    }

}
