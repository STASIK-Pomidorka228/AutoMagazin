package com.impact.AutoMagazin.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RefreshRequest {

    @NotBlank
    private String accessToken;

    @NotBlank
    private String refreshToken;
}

