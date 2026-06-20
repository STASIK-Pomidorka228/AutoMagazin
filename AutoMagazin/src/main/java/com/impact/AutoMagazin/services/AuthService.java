package com.impact.AutoMagazin.services;

import com.impact.AutoMagazin.config.JwtService;
import com.impact.AutoMagazin.dto.CreateUserRequest;
import com.impact.AutoMagazin.dto.LoginRequest;
import com.impact.AutoMagazin.dto.LoginResponse;
import com.impact.AutoMagazin.dto.RefreshRequest;
import com.impact.AutoMagazin.dto.UserResponse;
import com.impact.AutoMagazin.models.*;
import com.impact.AutoMagazin.repositories.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final CredentialsRepository credentialsRepository;
    private final UserPersonalDataRepository personalDataRepository;
    private final UserRoleRepository userRoleRepository;
    private final UserEmailRepository userEmailRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(UserRepository userRepository,
                       CredentialsRepository credentialsRepository,
                       UserPersonalDataRepository personalDataRepository,
                       UserRoleRepository userRoleRepository,
                       UserEmailRepository userEmailRepository,
                       RefreshTokenRepository refreshTokenRepository,
                       PasswordEncoder passwordEncoder,
                       JwtService jwtService) {
        this.userRepository = userRepository;
        this.credentialsRepository = credentialsRepository;
        this.personalDataRepository = personalDataRepository;
        this.userRoleRepository = userRoleRepository;
        this.userEmailRepository = userEmailRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @Transactional
    public LoginResponse register(CreateUserRequest request) {
        if (credentialsRepository.findByUsername(request.getUsername()).isPresent()) {
            throw new RuntimeException("Username already exists");
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setEnabled(true);
        user = userRepository.save(user);

        UserCredentials credentials = new UserCredentials();
        credentials.setUser(user);
        credentials.setUsername(request.getUsername());
        credentials.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        credentialsRepository.save(credentials);

        UserPersonalData personalData = new UserPersonalData();
        personalData.setUser(user);
        personalData.setLastName(request.getLastName());
        personalDataRepository.save(personalData);

        UserEmail userEmail = new UserEmail();
        userEmail.setUser(user);
        userEmail.setEmail(request.getEmail());
        userEmail.setIsPrimary(true);
        userEmailRepository.save(userEmail);

        UserRole role = new UserRole();
        role.setUserId(user.getId());
        role.setRoleId((short) 1);
        userRoleRepository.save(role);

        String roleName = resolveRoleName(role.getRoleId());
        String accessToken = jwtService.generateAccessToken(user.getUsername(), user.getId(), roleName);
        String refreshToken = jwtService.generateRefreshToken(user.getUsername());

        saveRefreshToken(user.getId(), refreshToken);

        return new LoginResponse(accessToken, refreshToken, buildUserResponse(user), "ВЫ ЗАРЕГИСТРИРОВАНЫ");
    }

    public LoginResponse login(LoginRequest request) {
        UserCredentials credentials = credentialsRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new RuntimeException("Invalid username or password"));

        if (!passwordEncoder.matches(request.getPassword(), credentials.getPasswordHash())) {
            throw new RuntimeException("Invalid username or password");
        }

        User user = userRepository.findById(credentials.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!user.isEnabled()) {
            throw new RuntimeException("Account disabled");
        }

        UserRole userRole = userRoleRepository.findByUserId(user.getId())
                .orElse(null);
        String roleName = userRole != null ? resolveRoleName(userRole.getRoleId()) : "USER";

        String accessToken = jwtService.generateAccessToken(user.getUsername(), user.getId(), roleName);
        String refreshToken = jwtService.generateRefreshToken(user.getUsername());

        saveRefreshToken(user.getId(), refreshToken);

        return new LoginResponse(accessToken, refreshToken, buildUserResponse(user), null);
    }

    public LoginResponse refresh(RefreshRequest request) {
        RefreshToken stored = refreshTokenRepository.findByToken(request.getRefreshToken())
                .orElseThrow(() -> new RuntimeException("Invalid refresh token"));

        if (stored.getExpiryDate().isBefore(Instant.now())) {
            refreshTokenRepository.delete(stored);
            throw new RuntimeException("Refresh token expired");
        }

        User user = userRepository.findById(stored.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!user.isEnabled()) {
            throw new RuntimeException("Account disabled");
        }

        UserRole userRole = userRoleRepository.findByUserId(user.getId())
                .orElse(null);
        String roleName = userRole != null ? resolveRoleName(userRole.getRoleId()) : "USER";

        String newAccessToken = jwtService.generateAccessToken(user.getUsername(), user.getId(), roleName);
        String newRefreshToken = jwtService.generateRefreshToken(user.getUsername());

        refreshTokenRepository.delete(stored);
        saveRefreshToken(user.getId(), newRefreshToken);

        return new LoginResponse(newAccessToken, newRefreshToken, buildUserResponse(user), null);
    }

    public UserResponse getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) {
            throw new RuntimeException("Not authenticated");
        }
        String username = (String) auth.getPrincipal();

        UserCredentials credentials = credentialsRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        User user = userRepository.findById(credentials.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        return buildUserResponse(user);
    }

    private UserResponse buildUserResponse(User user) {
        UserResponse response = new UserResponse();
        response.setId(user.getId());
        response.setUsername(user.getUsername());

        personalDataRepository.findByUserId(user.getId()).ifPresent(data -> {
            response.setFirstName(data.getFirstName());
            response.setLastName(data.getLastName());
            response.setBirthDate(data.getBirthDate());
        });

        userEmailRepository.findByUserId(user.getId()).ifPresent(email -> {
            response.setEmail(email.getEmail());
        });

        userRoleRepository.findByUserId(user.getId()).ifPresent(role -> {
            response.setRole(resolveRoleName(role.getRoleId()));
        });

        return response;
    }

    private void saveRefreshToken(Long userId, String token) {
        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setToken(token);
        refreshToken.setUserId(userId);
        refreshToken.setExpiryDate(Instant.now().plusSeconds(7 * 24 * 60 * 60));
        refreshTokenRepository.save(refreshToken);
    }

    private String resolveRoleName(Short roleId) {
        if (roleId == null) return "USER";
        switch (roleId) {
            case 4: return "ADMIN";
            default: return "USER";
        }
    }
}
