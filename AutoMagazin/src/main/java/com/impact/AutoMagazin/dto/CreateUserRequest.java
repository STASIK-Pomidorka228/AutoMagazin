package com.impact.AutoMagazin.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
@Getter
@Setter
public class CreateUserRequest {
    private String firstName;
    private String lastName;
    private LocalDate birthDate;

    private String username;
    private String password;
}


