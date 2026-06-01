package eshop.com.eshopinventoryservice.event;

import java.util.UUID;

public record OrderCreatedEvent(UUID orderId, String productId, int quantity) {
}
