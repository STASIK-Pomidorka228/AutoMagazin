package com.impact.AutoMagazin.config;

import com.impact.AutoMagazin.models.*;
import com.impact.AutoMagazin.repositories.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Component
@Profile("dev")
public class DataSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);

    private final UserRepository userRepository;
    private final CredentialsRepository credentialsRepository;
    private final UserPersonalDataRepository personalDataRepository;
    private final UserRoleRepository userRoleRepository;
    private final UserEmailRepository userEmailRepository;
    private final PasswordEncoder passwordEncoder;

    public DataSeeder(UserRepository userRepository,
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

    @Override
    @Transactional
    public void run(String... args) {
        if (userRepository.count() > 0) {
            log.info("Users already exist, skipping data seeding");
            return;
        }

        log.info("Seeding demo data...");

        createUser("admin", "admin123", "John", "Admin",
                "admin@automagazin.ru", LocalDate.of(1990, 1, 15), (short) 4);

        createUser("user", "user123", "Peter", "User",
                "user@automagazin.ru", LocalDate.of(1995, 6, 20), (short) 1);

        log.info("Demo data seeded successfully: admin/admin123, user/user123");
    }

    private void createUser(String username, String password, String firstName,
                            String lastName, String email, LocalDate birthDate, short roleId) {
        User user = new User();
        user.setUsername(username);
        user.setEnabled(true);
        user = userRepository.save(user);

        UserCredentials credentials = new UserCredentials();
        credentials.setUser(user);
        credentials.setUsername(username);
        credentials.setPasswordHash(passwordEncoder.encode(password));
        credentialsRepository.save(credentials);

        UserPersonalData personalData = new UserPersonalData();
        personalData.setUser(user);
        personalData.setFirstName(firstName);
        personalData.setLastName(lastName);
        personalData.setBirthDate(birthDate);
        personalDataRepository.save(personalData);

        UserRole role = new UserRole();
        role.setUserId(user.getId());
        role.setRoleId(roleId);
        userRoleRepository.save(role);

        UserEmail userEmail = new UserEmail();
        userEmail.setUser(user);
        userEmail.setEmail(email);
        userEmail.setIsPrimary(true);
        userEmailRepository.save(userEmail);
    }
}
