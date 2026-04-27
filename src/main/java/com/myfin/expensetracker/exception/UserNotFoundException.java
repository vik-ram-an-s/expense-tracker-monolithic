package com.myfin.expensetracker.exception;

public class UserNotFoundException extends RuntimeException {

    public UserNotFoundException(String userNotFound) {
        super(userNotFound);
    }
}
