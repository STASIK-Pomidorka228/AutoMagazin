package com.impact.AutoMagazin.controllers;

import com.impact.AutoMagazin.dto.*;
import com.impact.AutoMagazin.services.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public ResponseEntity<UserListResponse> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getUserById(@PathVariable Long id) {
        return ResponseEntity.ok(userService.getUserById(id));
    }

    @PutMapping("/{id}/password")
    public ResponseEntity<Void> updatePassword(@PathVariable Long id,
                                                @RequestBody UpdatePasswordRequest request) {
        userService.updatePassword(id, request);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{id}/personal-data")
    public ResponseEntity<Void> updatePersonalData(@PathVariable Long id,
                                                    @RequestBody UpdateUserPersonalData request) {
        userService.updatePersonalData(id, request);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{id}/birth-date")
    public ResponseEntity<Void> updateBirthDate(@PathVariable Long id,
                                                 @RequestBody UpdateBirthDateRequest request) {
        userService.updateBirthDate(id, request);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/email")
    public ResponseEntity<Void> setEmail(@RequestBody SetEmailRequest request) {
        userService.setEmail(request);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.ok().build();
    }
}
