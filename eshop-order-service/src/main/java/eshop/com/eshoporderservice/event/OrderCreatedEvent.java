package eshop.com.eshoporderservice.event;

import java.util.UUID;

public record OrderCreatedEvent(UUID orderId, String productId, int quantity) {
}
