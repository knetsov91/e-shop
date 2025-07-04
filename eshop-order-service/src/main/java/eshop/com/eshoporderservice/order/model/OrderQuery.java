package eshop.com.eshoporderservice.order.model;

import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@AllArgsConstructor
@Document(collection = "orders")
public class OrderQuery {

    @Id
    private String orderId;

    private String product;

    private int quantity;

    private String status;
}
