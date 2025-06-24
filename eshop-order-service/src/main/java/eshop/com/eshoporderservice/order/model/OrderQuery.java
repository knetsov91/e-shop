package eshop.com.eshoporderservice.order.model;

import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Document;

@AllArgsConstructor
@Document(collection = "orders")
public class OrderQuery {

    @Id
    private String orderId;

    private String product;

    private int quantity;

    private String status;
}
