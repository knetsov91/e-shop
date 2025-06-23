package eshop.com.eshoporderservice.order.model;

import jakarta.persistence.*;
import java.util.UUID;

@Entity
public class OrderCommand {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column
    private String product;

    @Column
    private int quantity;

    @Column
    private String status;
}