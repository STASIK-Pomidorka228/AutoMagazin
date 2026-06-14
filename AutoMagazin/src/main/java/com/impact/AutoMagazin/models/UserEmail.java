package com.impact.AutoMagazin.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "user_emails")
@Getter
@Setter
public class UserEmail {

    @Id
    private Long userId;

    @OneToOne
    @MapsId
    @JoinColumn(name = "user_id")
    private User user;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(name = "is_primary")
    private Boolean isPrimary;
}
