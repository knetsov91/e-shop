package eshop.com.eshoporderservice.web.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class OrderCommandCreateRequest {

    @NotNull(message = "Product is required")
    private String product;

    @NotNull(message = "Quantity is required")
    @Positive
    private int quantity;

    @NotNull(message = "Amount is required")
    @Positive
    private BigDecimal amount;
}
