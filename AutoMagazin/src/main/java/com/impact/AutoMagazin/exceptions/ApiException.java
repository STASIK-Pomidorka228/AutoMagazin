package com.impact.AutoMagazin.exceptions;

import com.impact.AutoMagazin.exceptions.ErrorCode;

public class ApiException extends RuntimeException {
    private final int errorCode;

    public ApiException(String message, int errorCode) {
        super(message);
        this.errorCode = errorCode;
    }
}