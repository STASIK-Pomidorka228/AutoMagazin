package com.impact.AutoMagazin.exceptions;

import lombok.Getter;

@Getter
public enum ErrorCode {
    AUTH_FAILED(401, "Authentication failed"),
    ACCESS_DENIED(403, "Insufficient permissions for this action"),
    USER_NOT_FOUND(404, "User does not exist"),
    TOKEN_EXPIRED(401, "Your session has expired"),
    INVALID_CREDENTIALS(401, "Invalid username or password"),
    USERNAME_EXISTS(409, "Username already exists"),
    CAR_NOT_FOUND(404, "Car not found"),
    NOT_AUTHENTICATED(401, "Not authenticated"),
    REFRESH_INVALID(401, "Invalid refresh token");

    private final int code;
    private final String message;

    ErrorCode(int code, String message) {
        this.code = code;
        this.message = message;
    }
}