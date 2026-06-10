package com.impact.AutoMagazin.models;

@Entity
@Table(name = "user_credentials")
@Getter
@Setter

public class UserCredentials {

    @Id
    private Long userId;

    @OneToOne
    @MapsId
    @JoinColumn(name = "user_id")
    private User user;

    @Column(unique = true, nullable = false)
    private String username;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

}
