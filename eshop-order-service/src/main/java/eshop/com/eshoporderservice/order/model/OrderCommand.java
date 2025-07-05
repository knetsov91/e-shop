package eshop.com.eshoporderservice.order.model;

import jakarta.persistence.*;
import lombok.Data;
import java.util.UUID;

@Data
@Entity
@Table(name="orders")
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