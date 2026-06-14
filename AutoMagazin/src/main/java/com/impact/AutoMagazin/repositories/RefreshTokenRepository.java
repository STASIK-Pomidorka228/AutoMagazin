package com.impact.AutoMagazin.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RefreshTokenRepository extends JpaRepository<com.impact.AutoMagazin.models.RefreshToken, Long> {

    Optional<com.impact.AutoMagazin.models.RefreshToken> findByToken(String token);

    void deleteByUserId(Long userId);
}
