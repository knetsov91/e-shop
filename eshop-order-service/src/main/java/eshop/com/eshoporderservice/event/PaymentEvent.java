package eshop.com.eshoporderservice.event;

import java.util.UUID;

public record PaymentEvent(UUID orderId, String status) {
}
