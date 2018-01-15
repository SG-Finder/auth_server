package com.finder.genie_ai.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.finder.genie_ai.dao.UserRepository;
import com.finder.genie_ai.exception.*;
import com.finder.genie_ai.model.user.UserModel;
import com.finder.genie_ai.model.user.command.UserChangeInfoCommand;
import com.finder.genie_ai.model.user.command.UserSignInCommand;
import com.finder.genie_ai.model.user.command.UserSignUpCommand;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import javax.transaction.Transactional;
import javax.validation.Valid;
import java.time.LocalDate;
import java.util.Optional;

@RestController
@RequestMapping(value = "/user")
public class UserController {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ObjectMapper mapper;

    @Transactional
    @RequestMapping(value = "/signup", method = RequestMethod.POST, produces = "application/json")
    public @ResponseBody JsonObject signupUser(@RequestBody @Valid UserSignUpCommand command,
                                               BindingResult bindingResult) throws JsonProcessingException {
        if (bindingResult.hasErrors()) {
            throw new BadRequestException("invalid parameter form");
        }

        Optional<UserModel> userModel = userRepository.findByUserId(command.getUserId());
        if (userModel.isPresent()) {
            throw new DuplicateException("already exist userId");
        }
        else {
            UserModel user = new UserModel();
            user.setUserId(command.getUserId());
            user.setPasswd(command.getPasswd());
            //todo generate salt value
            user.setSalt("dummy_salt_&!@3");
            user.setUserName(command.getUserName());
            user.setEmail(command.getEmail());
            System.out.println(LocalDate.parse(command.getBirth()));
            user.setBirth(LocalDate.parse(command.getBirth()));
            user.setGender(command.getGender());
            user.setIntroduce(command.getIntroduce());

            return (JsonObject) new JsonParser().parse(mapper.writeValueAsString(userRepository.save(user)));
        }

    }

    @RequestMapping(value = "/signin", method = RequestMethod.POST)
    public void signinUser(@RequestBody @Valid UserSignInCommand command,
                           BindingResult bindingResult,
                           HttpServletResponse response) {
        if (bindingResult.hasErrors()) {
            throw new BadRequestException("invalid signin form");
        }

        UserModel user = userRepository
                            .findByUserId(command.getUserId())
                            .orElseThrow(() -> new NotFoundException("Doesn't find user by userId. Please register first."));

        if (user.getPasswd().equals(command.getPasswd())) {
            response.setStatus(204);
            //todo generate sessionToken
            response.setHeader("sessionToken", "akjiodfj-asdkjf");
        }
        else {
            throw new UnauthorizedException();
        }

    }

    @RequestMapping(value = "checkDup/{userId}", method = RequestMethod.GET)
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
    public @ResponseBody JsonObject getUserInfo(@PathVariable("userId") String userId) throws JsonProcessingException {
        UserModel user =  userRepository
                            .findByUserId(userId)
                            .orElseThrow(() -> new NotFoundException("Doesn't find user by userId"));

        return (JsonObject) new JsonParser().parse(mapper.writeValueAsString(user));
    }

    @RequestMapping(value = "/{userId}", method = RequestMethod.PUT, produces = "application/json")
    public @ResponseBody JsonObject changeUserInfo(@PathVariable("userId") String userId,
                                                   @RequestBody UserChangeInfoCommand command) throws JsonProcessingException {

        if (!userRepository.findByUserId(userId).isPresent()) {
            throw new NotFoundException("Doesn't find user by userId. Please register first.");
        }

        int resCount = userRepository.updateUserInfo(userId, command.getUserName(), command.getEmail(), LocalDate.parse(command.getBirth()), command.getGender(), command.getIntroduce());
        if (resCount == 0) {
            throw new ServerException("doesn't execute query");
        }

        return (JsonObject) new JsonParser().parse(mapper.writeValueAsString(userRepository.findByUserId(userId).get()));
    }

    @RequestMapping(value = "/{userId}", method = RequestMethod.DELETE)
    public void deleteUser(@PathVariable("userId") String userId, @RequestHeader(name = "sessionToken") String sessionToken ) {
        if (userId == null) {
            throw new BadRequestException("doesn't exist path variable");
        }
        //todo check in redis server
        if (sessionToken == null) {
            throw new UnauthorizedException();
        }

        userRepository.deleteByUserId(userId);
    }
}

