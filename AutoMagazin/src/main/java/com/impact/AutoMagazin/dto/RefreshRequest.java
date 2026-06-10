package com.impact.AutoMagazin.dto;

import lombok.Data;

@Data

public class RefreshRequest {
    private String accessToken;
    private String refreshToken;
}

