package com.finder.genie_ai.model.user.command;

import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class UserDeleteCommand {

    @NotNull
    private String passwd;

}
