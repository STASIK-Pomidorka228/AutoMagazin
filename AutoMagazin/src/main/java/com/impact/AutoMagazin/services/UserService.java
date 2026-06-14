package com.impact.AutoMagazin.services;

import com.impact.AutoMagazin.dto.*;
import com.impact.AutoMagazin.models.*;
import com.impact.AutoMagazin.repositories.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final CredentialsRepository credentialsRepository;
    private final UserPersonalDataRepository personalDataRepository;
    private final UserRoleRepository userRoleRepository;
    private final UserEmailRepository userEmailRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository,
                       CredentialsRepository credentialsRepository,
                       UserPersonalDataRepository personalDataRepository,
                       UserRoleRepository userRoleRepository,
                       UserEmailRepository userEmailRepository,
                       PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.credentialsRepository = credentialsRepository;
        this.personalDataRepository = personalDataRepository;
        this.userRoleRepository = userRoleRepository;
        this.userEmailRepository = userEmailRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public UserListResponse getAllUsers() {
        List<User> users = userRepository.findAll();
        List<UserResponse> responses = users.stream()
                .map(this::toUserResponse)
                .collect(Collectors.toList());
        return new UserListResponse(responses);
    }

    public UserResponse getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return toUserResponse(user);
    }

    @Transactional
    public void updatePassword(Long userId, UpdatePasswordRequest request) {
        UserCredentials credentials = credentialsRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        credentials.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        credentialsRepository.save(credentials);
    }

    @Transactional
    public void updatePersonalData(Long userId, UpdateUserPersonalData request) {
        UserPersonalData data = personalDataRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        if (request.getFirstName() != null) {
            data.setFirstName(request.getFirstName());
        }
        if (request.getLastName() != null) {
            data.setLastName(request.getLastName());
        }
        if (request.getBirthDate() != null) {
            data.setBirthDate(request.getBirthDate());
        }
        personalDataRepository.save(data);
    }

    @Transactional
    public void updateBirthDate(Long userId, UpdateBirthDateRequest request) {
        UserPersonalData data = personalDataRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        data.setBirthDate(request.getBirthdate());
        personalDataRepository.save(data);
    }

    @Transactional
    public void setEmail(SetEmailRequest request) {
        UserEmail email = new UserEmail();
        email.setUserId(request.getUserId());
        email.setEmail(request.getEmail());
        email.setIsPrimary(request.getIsPrimary());
        userEmailRepository.save(email);
    }

    @Transactional
    public void deleteUser(Long userId) {
        credentialsRepository.deleteById(userId);
        personalDataRepository.findByUserId(userId).ifPresent(personalDataRepository::delete);
        userRoleRepository.findByUserId(userId).ifPresent(userRoleRepository::delete);
        userEmailRepository.findByUserId(userId).ifPresent(userEmailRepository::delete);
        userRepository.deleteById(userId);
    }

    private UserResponse toUserResponse(User user) {
        UserResponse response = new UserResponse();
        response.setId(user.getId());
        response.setUsername(user.getUsername());
        personalDataRepository.findByUserId(user.getId()).ifPresent(data -> {
            response.setFirstName(data.getFirstName());
            response.setLastName(data.getLastName());
            response.setBirthDate(data.getBirthDate());
        });
        return response;
    }
}
