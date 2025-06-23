package eshop.com.eshoporderservice.order.model;

import jakarta.persistence.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collation = "orders")
public class OrderQuery {

    @Id
    private String orderId;

    private String product;

    private int quantity;

    private String status;
}
