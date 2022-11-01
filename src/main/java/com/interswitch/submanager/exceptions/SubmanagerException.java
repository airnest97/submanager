package com.interswitch.submanager.exceptions;

import lombok.Getter;

@Getter
public class SubmanagerException extends RuntimeException {
    private final int statusCode;
    public SubmanagerException(String message, int statusCode) {
        super(message);
        this.statusCode = statusCode;
    }
}
