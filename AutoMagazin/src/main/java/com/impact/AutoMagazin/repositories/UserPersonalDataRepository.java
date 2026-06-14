package com.impact.AutoMagazin.repositories;

import com.impact.AutoMagazin.models.UserPersonalData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserPersonalDataRepository extends JpaRepository<UserPersonalData, Long> {

    Optional<UserPersonalData> findByUserId(Long userId);
}
