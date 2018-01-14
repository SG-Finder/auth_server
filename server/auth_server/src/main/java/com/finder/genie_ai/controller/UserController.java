package com.finder.genie_ai.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.finder.genie_ai.dao.UserRepository;
import com.finder.genie_ai.exception.BadRequestException;
import com.finder.genie_ai.exception.DuplicateException;
import com.finder.genie_ai.exception.NotFoundException;
import com.finder.genie_ai.exception.UnauthorizedException;
import com.finder.genie_ai.model.user.UserModel;
import com.finder.genie_ai.model.user.command.UserSignInCommand;
import com.finder.genie_ai.model.user.command.UserSignUpCommand;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
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
            user.setSalt("dummy_salt_&!@3");
            user.setUserName(command.getUserName());
            user.setEmail(command.getEmail());
            System.out.println(LocalDate.parse(command.getBirth()));
            user.setBirth(LocalDate.parse(command.getBirth()));
            user.setGender(command.getGender());
            user.setIntroduce(command.getIntroduce());

            String responseObject =  mapper.writeValueAsString(userRepository.save(user));
            return (JsonObject) new JsonParser().parse(responseObject);
        }

    }

    @RequestMapping(value = "/signin", method = RequestMethod.POST, produces = "application/json")
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
            throw new UnauthorizedException("Doesn't match password");
        }

    }

}
