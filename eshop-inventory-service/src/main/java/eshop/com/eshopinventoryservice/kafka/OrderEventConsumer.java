package eshop.com.eshopinventoryservice.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import eshop.com.eshopinventoryservice.event.OrderCreatedEvent;
import eshop.com.eshopinventoryservice.service.InventoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderEventConsumer {

    private final InventoryService inventoryService;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "order-events", groupId = "inventory-group")
    public void consume(String message) {
        try {
            OrderCreatedEvent event = objectMapper.readValue(message, OrderCreatedEvent.class);
            boolean reserved = inventoryService.reserveStock(event.productId(), event.quantity());

            if (reserved) {
                log.info("Stock reserved for order {}, product {}", event.orderId(), event.productId());
            } else {
                log.warn("Insufficient stock for order {}, product {}", event.orderId(), event.productId());
            }
        } catch (Exception e) {
            log.error("Failed to process order event: {}", message, e);
        }
    }
}
