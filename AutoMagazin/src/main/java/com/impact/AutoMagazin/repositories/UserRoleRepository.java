package com.impact.AutoMagazin.repositories;

import com.impact.AutoMagazin.models.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

public class UserRoleRepository {

    @Repository
    public interface UserRoleRepository<Optional> extends JpaRepository<UserRole, com.impact.lessons.entities.UserRoleId> {
        Optional<UserRole> findByUserId(long userId)
}
