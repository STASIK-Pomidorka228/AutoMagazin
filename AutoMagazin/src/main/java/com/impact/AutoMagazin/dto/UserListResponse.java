package com.impact.AutoMagazin.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class UserListResponse {
    private List<UserResponse> users;
}
