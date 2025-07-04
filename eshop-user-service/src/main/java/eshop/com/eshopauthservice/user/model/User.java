package eshop.com.eshopauthservice.user.model;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name="users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String username;

    private String password;

    private String email;

    private LocalDate createdAd;

    private LocalDate updatedAd;
}