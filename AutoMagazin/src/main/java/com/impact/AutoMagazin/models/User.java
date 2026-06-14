package com.impact.AutoMagazin.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.Set;

@Entity
@Table(name = "users")
@Getter
@Setter
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String username;

    private boolean enabled;

    private String firstName;

    private String lastName;

    @Column(unique = true)
    private String email;

    private LocalDate birthDate;

    @OneToMany(mappedBy = "user")
    private Set<UserRole> roles;
}
