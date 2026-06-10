package com.impact.AutoMagazin.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter

public class SetEmailRequest {
    private Long userId;
    private String email;
    private Boolean isPrimary;
}
