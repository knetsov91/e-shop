package eshop.com.eshoporderservice.event;

import java.util.UUID;

public record InventoryEvent(UUID orderId, String productId, String status) {
}
