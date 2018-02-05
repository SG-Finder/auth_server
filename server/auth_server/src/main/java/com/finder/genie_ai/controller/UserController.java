package com.finder.genie_ai.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.finder.genie_ai.dao.*;
import com.finder.genie_ai.dto.HistoryDTO;
import com.finder.genie_ai.dto.PlayerDTO;
import com.finder.genie_ai.dto.PlayerWeaponDTO;
import com.finder.genie_ai.dto.UserDTO;
import com.finder.genie_ai.enumdata.Weapon;
import com.finder.genie_ai.model.game.history.HistoryModel;
import com.finder.genie_ai.model.game.item_relation.WeaponRelation;
import com.finder.genie_ai.model.game.player.PlayerModel;
import com.finder.genie_ai.exception.*;
import com.finder.genie_ai.model.game.player.command.PlayerRegisterCommand;
import com.finder.genie_ai.model.game.weapon.WeaponModel;
import com.finder.genie_ai.model.session.SessionModel;
import com.finder.genie_ai.model.user.UserModel;
import com.finder.genie_ai.model.user.command.UserChangeInfoCommand;
import com.finder.genie_ai.model.user.command.UserDeleteCommand;
import com.finder.genie_ai.model.user.command.UserSignInCommand;
import com.finder.genie_ai.model.user.command.UserSignUpCommand;
import com.finder.genie_ai.redis_dao.SessionTokenRedisRepository;
import com.finder.genie_ai.util.TokenGenerator;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;


import javax.naming.Binding;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.transaction.Transactional;
import javax.validation.Valid;
import java.io.UnsupportedEncodingException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping(value = "/user")
public class UserController {

    private UserRepository userRepository;
    private PlayerRepository playerRepository;
    private HistoryRepository historyRepository;
    private ObjectMapper mapper;
    private SessionTokenRedisRepository sessionTokenRedisRepository;
    private BCryptPasswordEncoder bCryptPasswordEncoder;
    private WeaponRelationRepository weaponRelationRepository;
    private WeaponRepository weaponRepository;

    private final ModelMapper modelMapper = new ModelMapper();

    @Autowired
    public UserController(UserRepository userRepository,
                          PlayerRepository playerRepository,
                          HistoryRepository historyRepository,
                          SessionTokenRedisRepository sessionTokenRedisRepository,
                          ObjectMapper mapper,
                          BCryptPasswordEncoder bCryptPasswordEncoder,
                          WeaponRelationRepository weaponRelationRepository,
                          WeaponRepository weaponRepository) {
        this.userRepository = userRepository;
        this.playerRepository = playerRepository;
        this.historyRepository = historyRepository;
        this.sessionTokenRedisRepository = sessionTokenRedisRepository;
        this.mapper = mapper;
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
        this.weaponRelationRepository = weaponRelationRepository;
        this.weaponRepository = weaponRepository;
    }

    @Transactional
    @RequestMapping(value = "/signup", method = RequestMethod.POST, produces = "application/json")
    public @ResponseBody UserDTO signupUser(@RequestBody @Valid UserSignUpCommand command,
                                            BindingResult bindingResult) throws JsonProcessingException, UnsupportedEncodingException {
        if (bindingResult.hasErrors()) {
            System.out.println(command.toString());
            throw new BadRequestException("invalid parameter form");
        }
        System.out.println(command.toString());
        Optional<UserModel> userModel = userRepository.findByUserId(command.getUserId());
        if (userModel.isPresent()) {
            throw new DuplicateException("already exist userId");
        }
        else {
            UserModel user = new UserModel();
            String salt = TokenGenerator.generateSaltValue();

            user.setUserId(command.getUserId());
            System.out.println(bCryptPasswordEncoder.encode(command.getPasswd() + salt));
            user.setPasswd(bCryptPasswordEncoder.encode(command.getPasswd() + salt));
            user.setSalt(salt);
            user.setUserName(command.getUserName());
            user.setEmail(command.getEmail());
            user.setBirth(LocalDate.parse(command.getBirth()));
            user.setIntroduce(command.getIntroduce());

            user = userRepository.save(user);
            UserDTO userDTO = modelMapper.map(user, UserDTO.class);

            return userDTO;
        }

    }

    @Transactional
    @RequestMapping(value = "/register/game", method = RequestMethod.POST, produces = "application/json")
    public @ResponseBody PlayerDTO registerPlayer(@RequestHeader(name = "session-token") String token,
                                                  @RequestBody @Valid PlayerRegisterCommand command,
                                                  BindingResult bindingResult,
                                                  HttpServletRequest request) throws JsonProcessingException {
        if (!sessionTokenRedisRepository.isSessionValid(token)) {
            throw new UnauthorizedException();
        }

        JsonElement element = new JsonParser().parse(sessionTokenRedisRepository.findSessionToken(token));
        SessionModel sessionModel = new SessionModel(request.getRemoteAddr(), LocalDateTime.parse(element.getAsJsonObject().get("signin_at").getAsString()), LocalDateTime.now());
        System.out.println(mapper.writeValueAsString(sessionModel));
        sessionTokenRedisRepository.updateSessionToken(token, mapper.writeValueAsString(sessionModel));

        if (bindingResult.hasErrors()) {
            throw new BadRequestException("invalid parameter form");
        }

        // check duplicated nickname
        if (playerRepository.findByNickname(command.getNickname()).isPresent()) {
            throw new DuplicateException("already exist nickname");
        }

        PlayerModel player = new PlayerModel();
        player.setNickname(command.getNickname());
        player.setUserId(userRepository.findByUserId(command.getUserId()).get());
        player = playerRepository.save(player);

        HistoryModel history = new HistoryModel();
        history.setPlayerId(player);
        history = historyRepository.save(history);

        WeaponRelation weaponRelation = new WeaponRelation();
        weaponRelation.setPlayerId(player);
        weaponRelation.setUsableCount(Integer.MAX_VALUE);
        weaponRelation.setWeaponId(weaponRepository.findOne(1));
        weaponRelation = weaponRelationRepository.save(weaponRelation);

        modelMapper.getConfiguration().setMatchingStrategy(MatchingStrategies.LOOSE);
        HistoryDTO historyDTO = modelMapper.map(history, HistoryDTO.class);

        PlayerWeaponDTO playerWeaponDTO = modelMapper.map(weaponRelation.getWeaponId(), PlayerWeaponDTO.class);
        playerWeaponDTO.setUsableCount(weaponRelation.getUsableCount());

        List<PlayerWeaponDTO> weaponList = new ArrayList<>(1);
        weaponList.add(playerWeaponDTO);

        return new PlayerDTO(player.getNickname(),
                player.getTier(),
                player.getScore(),
                historyDTO,
                weaponList,
                player.getPoint());
    }

    @RequestMapping(value = "/signin", method = RequestMethod.POST)
    public void signinUser(@RequestBody @Valid UserSignInCommand command,
                           BindingResult bindingResult,
                           HttpServletRequest request,
                           HttpServletResponse response) throws JsonProcessingException, UnsupportedEncodingException {
        if (bindingResult.hasErrors()) {
            throw new BadRequestException("invalid signin form");
        }

        UserModel user = userRepository
                            .findByUserId(command.getUserId())
                            .orElseThrow(() -> new NotFoundException("Doesn't find user by userId. Please register first."));
        if (bCryptPasswordEncoder.matches(command.getPasswd() + user.getSalt(), user.getPasswd())) {
            response.setStatus(204);

            String newToken = TokenGenerator.generateSessionToken(user.getUserId());
            SessionModel sessionModel = new SessionModel(request.getRemoteAddr(), LocalDateTime.now(), LocalDateTime.now());
            String data = mapper.writeValueAsString(sessionModel);
            String oldToken = sessionTokenRedisRepository.findSessionUserId(command.getUserId());
            if (oldToken != null) {
                sessionTokenRedisRepository.updateSessionTokenOnDupLogin(newToken, oldToken, command.getUserId(), data);
            }
            else {
                sessionTokenRedisRepository.saveSessionToken(newToken, command.getUserId(), data);
            }

            response.setHeader("session-token", newToken);
        }
        else {
            throw new UnauthorizedException();
        }

    }

    @RequestMapping(value = "/signout", method = RequestMethod.DELETE)
    public void signoutUser(@RequestHeader(name = "session-token") String token,
                            @RequestHeader(name = "userId") String userId,
                            HttpServletResponse response) {
        if (sessionTokenRedisRepository.expireSession(token, userId)) {
            response.setHeader("expired-token", Boolean.TRUE.toString());
        }
        else {
            response.setHeader("expired-token", Boolean.FALSE.toString());
        }
        response.setStatus(204);
    }

    @RequestMapping(value = "/checkDup/{userId}", method = RequestMethod.GET)
    public void checkDup(@PathVariable("userId") String userId, HttpServletResponse response) {
        if (userId == null) {
            throw new BadRequestException("doesn't exist path variable");
        }

        if (userRepository.findByUserId(userId).isPresent()) {
            response.setHeader("isDup", Boolean.toString(true));
        }
        else {
            response.setHeader("isDup", Boolean.toString(false));
        }
    }

    @RequestMapping(value = "/{userId}", method = RequestMethod.GET, produces = "application/json")
    public @ResponseBody UserDTO getUserInfo(@PathVariable("userId") String userId,
                                             @RequestHeader(name = "session-token") String token,
                                                HttpServletRequest request) throws JsonProcessingException, UnsupportedEncodingException {
        if (!sessionTokenRedisRepository.isSessionValid(token)) {
            throw new UnauthorizedException();
        }

        JsonElement element = new JsonParser().parse(sessionTokenRedisRepository.findSessionToken(token));
        SessionModel sessionModel = new SessionModel(request.getRemoteAddr(), LocalDateTime.parse(element.getAsJsonObject().get("signin_at").getAsString()), LocalDateTime.now());
        sessionTokenRedisRepository.updateSessionToken(token, mapper.writeValueAsString(sessionModel));

        UserModel user =  userRepository
                            .findByUserId(userId)
                            .orElseThrow(() -> new NotFoundException("Doesn't find user by userId"));

        UserDTO userDTO = modelMapper.map(user, UserDTO.class);

        Optional<PlayerModel> playerModel = playerRepository.findByUserId(user);
        if (playerModel.isPresent()) {
            userDTO.setNickname(playerModel.get().getNickname());
        }
        else {
            userDTO.setNickname("0");
        }

        return userDTO;
    }

    @RequestMapping(value = "/{userId}", method = RequestMethod.PUT, produces = "application/json")
    public @ResponseBody UserDTO updateUserInfo(@PathVariable("userId") String userId,
                                                   @RequestBody @Valid UserChangeInfoCommand command,
                                                   BindingResult bindingResult,
                                                   @RequestHeader(name = "session-token") String token,
                                                   HttpServletRequest request) throws JsonProcessingException {
        if (bindingResult.hasErrors()) {
            throw new BadRequestException("Please follow data form");
        }

        UserModel user = userRepository
                .findByUserId(userId)
                .orElseThrow(() -> new NotFoundException("Doesn't find user by userId. Please register first."));

        if (!sessionTokenRedisRepository.isSessionValid(token)
                || !bCryptPasswordEncoder.matches(command.getPasswd() + user.getSalt(), user.getPasswd())) {
            throw new UnauthorizedException();
        }

        JsonElement element = new JsonParser().parse(sessionTokenRedisRepository.findSessionToken(token));
        SessionModel sessionModel = new SessionModel(request.getRemoteAddr(), LocalDateTime.parse(element.getAsJsonObject().get("signin_at").getAsString()), LocalDateTime.now());
        sessionTokenRedisRepository.updateSessionToken(token, mapper.writeValueAsString(sessionModel));

        int resCount = userRepository.updateUserInfo(
                command.getUserName(),
                command.getEmail(),
                LocalDate.parse(command.getBirth()),
                command.getIntroduce(),
                userId);

        if (resCount == 0) {
            throw new ServerException("doesn't execute query");
        }

        UserDTO userDTO = new UserDTO();
        userDTO.setUserName(command.getUserName());
        userDTO.setEmail(command.getEmail());
        userDTO.setBirth(LocalDate.parse(command.getBirth()));
        userDTO.setIntroduce(command.getIntroduce());

        return userDTO;
    }

    @RequestMapping(value = "/{userId}", method = RequestMethod.DELETE)
    public void deleteUser(@PathVariable("userId") String userId,
                           @RequestHeader(name = "session-token") String token,
                           @RequestBody @Valid UserDeleteCommand command,
                           BindingResult bindingResult,
                           HttpServletRequest request) throws JsonProcessingException {
        if (userId == null || bindingResult.hasErrors()) {
            throw new BadRequestException("Please follow data form");
        }
        UserModel user = userRepository
                .findByUserId(userId)
                .orElseThrow(() -> new NotFoundException("Doesn't find user by userId. Please register first."));

        if (!sessionTokenRedisRepository.isSessionValid(token)
                || !bCryptPasswordEncoder.matches(command.getPasswd() + user.getSalt(), user.getPasswd())) {
            throw new UnauthorizedException();
        }
        JsonElement element = new JsonParser().parse(sessionTokenRedisRepository.findSessionToken(token));
        SessionModel sessionModel = new SessionModel(request.getRemoteAddr(), LocalDateTime.parse(element.getAsJsonObject().get("signin_at").getAsString()), LocalDateTime.now());
        sessionTokenRedisRepository.updateSessionToken(token, mapper.writeValueAsString(sessionModel));

        userRepository.deleteByUserId(userId);
        sessionTokenRedisRepository.expireSession(token, userId);
    }

}

