package com.myfin.expensetracker.exception;

import lombok.Data;
import lombok.EqualsAndHashCode;


@EqualsAndHashCode(callSuper = true)
@Data
public class AppException extends RuntimeException {

    private int status;
    public AppException(int status, String message){
        super(message);
        this.status=status;
    }
    public AppException(String message) {
        super(message);
    }
}
