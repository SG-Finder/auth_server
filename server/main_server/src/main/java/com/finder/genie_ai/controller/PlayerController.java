package com.finder.genie_ai.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.finder.genie_ai.dao.*;
import com.finder.genie_ai.dto.HistoryDTO;
import com.finder.genie_ai.dto.PlayerDTO;
import com.finder.genie_ai.dto.PlayerWeaponDTO;
import com.finder.genie_ai.exception.NotFoundException;
import com.finder.genie_ai.exception.UnauthorizedException;
import com.finder.genie_ai.model.game.history.HistoryModel;
import com.finder.genie_ai.model.game.item_relation.WeaponRelation;
import com.finder.genie_ai.model.game.player.PlayerModel;
import com.finder.genie_ai.model.session.SessionModel;
import com.finder.genie_ai.redis_dao.SessionTokenRedisRepository;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping(value = "finder/player")
public class PlayerController {

    private PlayerRepository playerRepository;
    private UserRepository userRepository;
    private WeaponRelationRepository weaponRelationRepository;
    private WeaponRepository weaponRepository;
    private HistoryRepository historyRepository;
    private SessionTokenRedisRepository sessionTokenRedisRepository;
    private ObjectMapper mapper;
    private ModelMapper modelMapper = new ModelMapper();

    @Autowired
    public PlayerController(PlayerRepository playerRepository,
                            UserRepository userRepository,
                            WeaponRelationRepository weaponRelationRepository,
                            WeaponRepository weaponRepository,
                            HistoryRepository historyRepository,
                            SessionTokenRedisRepository sessionTokenRedisRepository,
                            ObjectMapper mapper) {
        this.playerRepository = playerRepository;
        this.userRepository = userRepository;
        this.weaponRelationRepository = weaponRelationRepository;
        this.weaponRepository = weaponRepository;
        this.historyRepository = historyRepository;
        this.sessionTokenRedisRepository = sessionTokenRedisRepository;
        this.mapper = mapper;
    }


    @RequestMapping(value = "/{nickname}", method = RequestMethod.GET, produces = "application/json")
    public PlayerDTO getPlayerInfo(@PathVariable("nickname") String nickname,
                                   @RequestHeader("session-token") String token,
                                   HttpServletRequest request) throws JsonProcessingException {
        if (!sessionTokenRedisRepository.isSessionValid(token)) {
            throw new UnauthorizedException();
        }
        JsonElement element = new JsonParser().parse(sessionTokenRedisRepository.findSessionToken(token));
        SessionModel sessionModel = new SessionModel(request.getRemoteAddr(), LocalDateTime.parse(element.getAsJsonObject().get("signin_at").getAsString()), LocalDateTime.now());
        sessionTokenRedisRepository.updateSessionToken(token, mapper.writeValueAsString(sessionModel));

        PlayerModel playerModel = playerRepository
                .findByNickname(nickname)
                .orElseThrow(() -> new NotFoundException("Doesn't find player"));

        List<WeaponRelation> listWeaponRelation = weaponRelationRepository.findByPlayerId(playerModel);

        modelMapper.getConfiguration().setMatchingStrategy(MatchingStrategies.LOOSE);

        PlayerDTO playerDTO = new PlayerDTO();
        playerDTO.setNickname(playerModel.getNickname());

        HistoryModel historyModel = historyRepository.findByPlayerId(playerModel).get();

        playerDTO.setHistory(modelMapper.map(historyModel, HistoryDTO.class));
        playerDTO.setPoint(playerModel.getPoint());
        playerDTO.setTier(playerModel.getTier());

        if (listWeaponRelation.size() != 0) {
            List<PlayerWeaponDTO> playerWeaponDTOs = new ArrayList<>(listWeaponRelation.size());

            for (WeaponRelation data : listWeaponRelation) {
                PlayerWeaponDTO dto = modelMapper.map(data.getWeaponId(), PlayerWeaponDTO.class);
                dto.setUsableCount(data.getUsableCount());
                playerWeaponDTOs.add(dto);
            }
            playerDTO.setWeapons(playerWeaponDTOs);
        }

        return playerDTO;
    }

}
