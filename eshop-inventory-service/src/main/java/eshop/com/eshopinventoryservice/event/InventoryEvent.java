package eshop.com.eshopinventoryservice.event;

import java.util.UUID;

public record InventoryEvent(UUID orderId, String productId, String status) {
}
