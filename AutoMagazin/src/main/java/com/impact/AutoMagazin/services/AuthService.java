package com.impact.AutoMagazin.services;

import com.impact.AutoMagazin.config.JwtService;
import com.impact.AutoMagazin.dto.CreateUserRequest;
import com.impact.AutoMagazin.dto.LoginRequest;
import com.impact.AutoMagazin.dto.RefreshRequest;
import com.impact.AutoMagazin.models.*;
import com.impact.AutoMagazin.repositories.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final CredentialsRepository credentialsRepository;
    private final UserPersonalDataRepository personalDataRepository;
    private final UserRoleRepository userRoleRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(UserRepository userRepository,
                       CredentialsRepository credentialsRepository,
                       UserPersonalDataRepository personalDataRepository,
                       UserRoleRepository userRoleRepository,
                       RefreshTokenRepository refreshTokenRepository,
                       PasswordEncoder passwordEncoder,
                       JwtService jwtService) {
        this.userRepository = userRepository;
        this.credentialsRepository = credentialsRepository;
        this.personalDataRepository = personalDataRepository;
        this.userRoleRepository = userRoleRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @Transactional
    public Map<String, String> register(CreateUserRequest request) {
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
        personalData.setFirstName(request.getFirstName());
        personalData.setLastName(request.getLastName());
        personalData.setBirthDate(request.getBirthDate());
        personalDataRepository.save(personalData);

        UserRole role = new UserRole();
        role.setUserId(user.getId());
        role.setRoleId((short) 1);
        userRoleRepository.save(role);

        String accessToken = jwtService.generateAccessToken(user.getUsername(), user.getId(), resolveRoleName(role.getRoleId()));
        String refreshToken = jwtService.generateRefreshToken(user.getUsername());

        saveRefreshToken(user.getId(), refreshToken);

        return Map.of(
            "accessToken", accessToken,
            "refreshToken", refreshToken
        );
    }

    public Map<String, String> login(LoginRequest request) {
        UserCredentials credentials = credentialsRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new RuntimeException("Invalid username or password"));

        if (!passwordEncoder.matches(request.getPassword(), credentials.getPasswordHash())) {
            throw new RuntimeException("Invalid username or password");
        }

        User user = userRepository.findById(credentials.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        UserRole userRole = userRoleRepository.findByUserId(user.getId())
                .orElse(null);
        String roleName = userRole != null ? resolveRoleName(userRole.getRoleId()) : "USER";

        String accessToken = jwtService.generateAccessToken(user.getUsername(), user.getId(), roleName);
        String refreshToken = jwtService.generateRefreshToken(user.getUsername());

        saveRefreshToken(user.getId(), refreshToken);

        return Map.of(
            "accessToken", accessToken,
            "refreshToken", refreshToken
        );
    }

    public Map<String, String> refresh(RefreshRequest request) {
        RefreshToken stored = refreshTokenRepository.findByToken(request.getRefreshToken())
                .orElseThrow(() -> new RuntimeException("Invalid refresh token"));

        if (stored.getExpiryDate().isBefore(Instant.now())) {
            refreshTokenRepository.delete(stored);
            throw new RuntimeException("Refresh token expired");
        }

        User user = userRepository.findById(stored.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        UserRole userRole = userRoleRepository.findByUserId(user.getId())
                .orElse(null);
        String roleName = userRole != null ? resolveRoleName(userRole.getRoleId()) : "USER";

        String newAccessToken = jwtService.generateAccessToken(user.getUsername(), user.getId(), roleName);
        String newRefreshToken = jwtService.generateRefreshToken(user.getUsername());

        refreshTokenRepository.delete(stored);
        saveRefreshToken(user.getId(), newRefreshToken);

        return Map.of(
            "accessToken", newAccessToken,
            "refreshToken", newRefreshToken
        );
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
        return switch (roleId) {
            case 1 -> "USER";
            case 2 -> "AUTHOR";
            case 3 -> "EDITOR";
            case 4 -> "ADMIN";
            default -> "USER";
        };
    }
}
