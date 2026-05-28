package eshop.com.eshopinventoryservice.model;

import jakarta.persistence.*;
import lombok.Data;

import java.util.UUID;

@Data
@Entity
@Table(name = "stock")
public class Stock {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true)
    private String productId;

    @Column(nullable = false)
    private int quantity;

    @Version
    private Long version;
}
