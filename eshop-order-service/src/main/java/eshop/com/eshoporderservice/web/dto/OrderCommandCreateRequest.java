package eshop.com.eshoporderservice.web.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class OrderCommandCreateRequest {

    @NotNull(message = "Product is required")
    private String product;

    @NotNull(message = "Quantity is required")
    @Positive
    private int quantity;
}
