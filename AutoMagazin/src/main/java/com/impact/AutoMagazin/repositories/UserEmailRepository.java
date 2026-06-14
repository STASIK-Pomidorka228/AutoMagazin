package com.impact.AutoMagazin.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.impact.AutoMagazin.models.UserEmail;

import java.util.Optional;

@Repository
public interface UserEmailRepository extends JpaRepository<UserEmail, Long> {

    Optional<UserEmail> findByUserId(Long userId);

    Optional<UserEmail> findByEmail(String email);
}
