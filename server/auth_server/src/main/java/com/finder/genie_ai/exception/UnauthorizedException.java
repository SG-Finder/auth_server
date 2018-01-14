package com.finder.genie_ai.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code = HttpStatus.UNAUTHORIZED)
public class UnauthorizedException extends BaseException {

    public static final String MESSAGE = "wrong password or format sessionToken in header";

    public UnauthorizedException(String message) {
        super(HttpStatus.UNAUTHORIZED.value(), MESSAGE);
    }

}
