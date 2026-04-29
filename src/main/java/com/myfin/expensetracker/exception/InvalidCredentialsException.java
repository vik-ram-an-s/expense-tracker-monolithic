package com.myfin.expensetracker.exception;

import org.springframework.http.HttpStatus;

public class InvalidCredentialsException extends AppException {
    public InvalidCredentialsException(String invalidPassword) {
        super(HttpStatus.UNAUTHORIZED.value(),invalidPassword);
    }
}
