package com.finder.genie_ai.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.finder.genie_ai.dao.HistoryRepository;
import com.finder.genie_ai.dao.WeaponRepository;
import com.finder.genie_ai.dao.PlayerRepository;
import com.finder.genie_ai.dao.UserRepository;
import com.finder.genie_ai.dto.HistoryDTO;
import com.finder.genie_ai.dto.PlayerDTO;
import com.finder.genie_ai.dto.UserDTO;
import com.finder.genie_ai.model.game.history.HistoryModel;
import com.finder.genie_ai.model.game.player.PlayerModel;
import com.finder.genie_ai.exception.*;
import com.finder.genie_ai.model.game.player.command.PlayerRegisterCommand;
import com.finder.genie_ai.model.session.SessionModel;
import com.finder.genie_ai.model.user.UserModel;
import com.finder.genie_ai.model.user.command.UserChangeInfoCommand;
import com.finder.genie_ai.model.user.command.UserSignInCommand;
import com.finder.genie_ai.model.user.command.UserSignUpCommand;
import com.finder.genie_ai.redis_dao.SessionTokenRedisRepository;
import com.finder.genie_ai.util.TokenGenerator;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;


import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.transaction.Transactional;
import javax.validation.Valid;
import java.io.UnsupportedEncodingException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

@RestController
@RequestMapping(value = "/user")
public class UserController {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PlayerRepository playerRepository;
    @Autowired
    private HistoryRepository historyRepository;
    @Autowired
    private ObjectMapper mapper;
    @Autowired
    private SessionTokenRedisRepository sessionTokenRedisRepository;
    @Autowired
    private BCryptPasswordEncoder bCryptPasswordEncoder;

    private final ModelMapper modelMapper = new ModelMapper();


    @Transactional
    @RequestMapping(value = "/signup", method = RequestMethod.POST, produces = "application/json")
    public @ResponseBody UserDTO signupUser(@RequestBody @Valid UserSignUpCommand command,
                                               BindingResult bindingResult) throws JsonProcessingException, UnsupportedEncodingException {
        if (bindingResult.hasErrors()) {
            System.out.println(command.toString());
            throw new BadRequestException("invalid parameter form");
        }

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
                                                   @RequestHeader(name = "userId") String userId,
                                                   @RequestBody @Valid PlayerRegisterCommand command,
                                                   BindingResult bindingResult) throws JsonProcessingException {
        if (!sessionTokenRedisRepository.isSessionValid(token, userId)) {
            throw new UnauthorizedException();
        }

        if (bindingResult.hasErrors()) {
            throw new BadRequestException("invalid parameter form");
        }

        // check duplicated nickname
        if (playerRepository.findByNickname(command.getNickname()).isPresent()) {
            throw new DuplicateException("already exist nickname");
        }

        PlayerModel player = new PlayerModel();
        player.setNickname(command.getNickname());
        player.setUserId(userRepository.findByUserId(userId).get());
        player = playerRepository.save(player);

        HistoryModel history = new HistoryModel();
        history.setPlayerId(player);
        history = historyRepository.save(history);

        HistoryDTO historyDTO = modelMapper.map(history, HistoryDTO.class);

        return new PlayerDTO(player.getNickname(),
                player.getTier(),
                player.getScore(),
                historyDTO,
                null,
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
            String token = TokenGenerator.generateSessionToken(user.getUserId());
            response.setHeader("session-token", token);

            SessionModel sessionModel = new SessionModel(request.getRemoteAddr(), LocalDateTime.now(), LocalDateTime.now());
            //todo delete duplicate session
            sessionTokenRedisRepository.saveSessionToken(token, user.getUserId(), mapper.writeValueAsString(sessionModel));
        }
        else {
            throw new UnauthorizedException();
        }

    }

    @RequestMapping(value = "/signout", method = RequestMethod.DELETE)
    public void signoutUser(@RequestHeader(name = "session-token") String token,
                            @RequestHeader(name = "userId") String userId,
                            HttpServletResponse response) {
        if (sessionTokenRedisRepository.deleteSession(token, userId)) {
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
                                                @RequestHeader(name = "userId") String activeUserId,
                                                HttpServletRequest request) throws JsonProcessingException, UnsupportedEncodingException {
        if (!sessionTokenRedisRepository.isSessionValid(token, userId)) {
            throw new UnauthorizedException();
        }

        JsonElement element = new JsonParser().parse(sessionTokenRedisRepository.findSessionToken(token, activeUserId));
        SessionModel sessionModel = new SessionModel(request.getRemoteAddr(), LocalDateTime.parse(element.getAsJsonObject().get("signin_at").getAsString()), LocalDateTime.now());
        sessionTokenRedisRepository.updateSessionToken(token, userId, mapper.writeValueAsString(sessionModel));

        UserModel user =  userRepository
                            .findByUserId(userId)
                            .orElseThrow(() -> new NotFoundException("Doesn't find user by userId"));

        UserDTO userDTO = modelMapper.map(user, UserDTO.class);

        return userDTO;
    }

    @RequestMapping(value = "/{userId}", method = RequestMethod.PUT, produces = "application/json")
    public @ResponseBody UserDTO updateUserInfo(@PathVariable("userId") String userId,
                                                   @RequestBody UserChangeInfoCommand command,
                                                   @RequestHeader(name = "session-token") String token,
                                                   @RequestHeader(name = "userId") String activeUserId,
                                                   HttpServletRequest request) throws JsonProcessingException {
        if (!sessionTokenRedisRepository.isSessionValid(token, userId) ) {
            throw new UnauthorizedException();
        }

        JsonElement element = new JsonParser().parse(sessionTokenRedisRepository.findSessionToken(token, activeUserId));
        SessionModel sessionModel = new SessionModel(request.getRemoteAddr(), LocalDateTime.parse(element.getAsJsonObject().get("signin_at").getAsString()), LocalDateTime.now());
        sessionTokenRedisRepository.updateSessionToken(token, userId, mapper.writeValueAsString(sessionModel));

        Optional<UserModel> user = userRepository.findByUserId(userId);
        if (!user.isPresent()) {
            throw new NotFoundException("Doesn't find user by userId. Please register first.");
        }

        int resCount = userRepository.updateUserInfo(
                userId,
                bCryptPasswordEncoder.encode(command.getPasswd() + user.get().getSalt()),
                command.getUserName(),
                command.getEmail(),
                LocalDate.parse(command.getBirth()),
                command.getIntroduce());

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
                           @RequestHeader(name = "userId") String activeUserId,
                           HttpServletRequest request) throws JsonProcessingException {
        if (userId == null) {
            throw new BadRequestException("doesn't exist path variable");
        }
        if (!sessionTokenRedisRepository.isSessionValid(token, userId)) {
            throw new UnauthorizedException();
        }
        JsonElement element = new JsonParser().parse(sessionTokenRedisRepository.findSessionToken(token, activeUserId));
        SessionModel sessionModel = new SessionModel(request.getRemoteAddr(), LocalDateTime.parse(element.getAsJsonObject().get("signin_at").getAsString()), LocalDateTime.now());
        sessionTokenRedisRepository.updateSessionToken(token, userId, mapper.writeValueAsString(sessionModel));

        userRepository.deleteByUserId(userId);;
        sessionTokenRedisRepository.deleteSession(token, userId);
    }

}

