package com.impact.AutoMagazin.repositories;



import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

public class CredentialsRepository {
    public Optional<Object> findByUsername(String username) {
    }

    @Repository
    public interface CredentialsRepository<Long, UserCredentials> extends JpaRepository<UserCredentials, Long> {
        Optional<UserCredentials> findByUsername(String username);
}
