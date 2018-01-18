package com.finder.genie_ai.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.finder.genie_ai.dao.UserRepository;
import com.finder.genie_ai.exception.*;
import com.finder.genie_ai.model.session_manage.SessionUserInfoModel;
import com.finder.genie_ai.model.user.UserModel;
import com.finder.genie_ai.model.user.command.UserChangeInfoCommand;
import com.finder.genie_ai.model.user.command.UserSignInCommand;
import com.finder.genie_ai.model.user.command.UserSignUpCommand;
import com.finder.genie_ai.redis_dao.SessionTokenRedisRepository;
import com.finder.genie_ai.util.TokenGenerator;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
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
import java.util.Optional;

@RestController
@RequestMapping(value = "/user")
public class UserController {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ObjectMapper mapper;
    @Autowired
    private SessionTokenRedisRepository sessionTokenRedisRepository;
    @Autowired
    private BCryptPasswordEncoder bCryptPasswordEncoder;

    @Transactional
    @RequestMapping(value = "/signup", method = RequestMethod.POST, produces = "application/json")
    public @ResponseBody JsonObject signupUser(@RequestBody @Valid UserSignUpCommand command,
                                               BindingResult bindingResult) throws JsonProcessingException, UnsupportedEncodingException {
        if (bindingResult.hasErrors()) {
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
            user.setGender(command.getGender());
            user.setIntroduce(command.getIntroduce());

            return (JsonObject) new JsonParser().parse(mapper.writeValueAsString(userRepository.save(user)));
        }

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

            SessionUserInfoModel userInfoModel = new SessionUserInfoModel(user.getUserId(), token, request.getRemoteAddr());
            sessionTokenRedisRepository.saveSessionToken(token, user.getUserId(), mapper.writeValueAsString(userInfoModel));
        }
        else {
            throw new UnauthorizedException();
        }

    }

    @RequestMapping(value = "/signout", method = RequestMethod.DELETE)
    public void signoutUser(@RequestHeader(name = "session-token") String token,
                            @RequestHeader(name = "userId") String userId,
                            HttpServletResponse response) {
        if (sessionTokenRedisRepository.deleteSessionInfo(token, userId)) {
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
    public @ResponseBody JsonObject getUserInfo(@PathVariable("userId") String userId,
                                                @RequestHeader(name = "session-token") String token) throws JsonProcessingException, UnsupportedEncodingException {
        if (sessionTokenRedisRepository.findSessionToken(token) == null) {
            throw new UnauthorizedException();
        }

        UserModel user =  userRepository
                            .findByUserId(userId)
                            .orElseThrow(() -> new NotFoundException("Doesn't find user by userId"));

        return (JsonObject) new JsonParser().parse(mapper.writeValueAsString(user));
    }

    @RequestMapping(value = "/{userId}", method = RequestMethod.PUT, produces = "application/json")
    public @ResponseBody JsonObject updateUserInfo(@PathVariable("userId") String userId,
                                                   @RequestBody UserChangeInfoCommand command,
                                                   @RequestHeader(name = "session-token") String token) throws JsonProcessingException {
        if (sessionTokenRedisRepository.findSessionToken(token) == null) {
            throw new UnauthorizedException();
        }

        Optional<UserModel> user = userRepository.findByUserId(userId);
        if (!user.isPresent()) {
            throw new NotFoundException("Doesn't find user by userId. Please register first.");
        }

        int resCount = userRepository.updateUserInfo(userId,
                bCryptPasswordEncoder.encode(command.getPasswd() + user.get().getSalt()),
                command.getUserName(), command.getEmail(),
                LocalDate.parse(command.getBirth()),
                command.getGender(),
                command.getIntroduce());
        if (resCount == 0) {
            throw new ServerException("doesn't execute query");
        }

        return (JsonObject) new JsonParser().parse(mapper.writeValueAsString(userRepository.findByUserId(userId).get()));
    }

    @RequestMapping(value = "/{userId}", method = RequestMethod.DELETE)
    public void deleteUser(@PathVariable("userId") String userId,
                           @RequestHeader(name = "session-token") String token) {
        if (userId == null) {
            throw new BadRequestException("doesn't exist path variable");
        }
        if (sessionTokenRedisRepository.findSessionToken(token) == null) {
            throw new UnauthorizedException();
        }

        userRepository.deleteByUserId(userId);
    }

}

