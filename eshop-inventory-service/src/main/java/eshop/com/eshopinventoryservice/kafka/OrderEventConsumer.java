package eshop.com.eshopinventoryservice.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import eshop.com.eshopinventoryservice.event.InventoryEvent;
import eshop.com.eshopinventoryservice.event.OrderCreatedEvent;
import eshop.com.eshopinventoryservice.service.InventoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.DltHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.RetryableTopic;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.retry.annotation.Backoff;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderEventConsumer {

    private final InventoryService inventoryService;
    private final ObjectMapper objectMapper;
    private final KafkaTemplate<String, String> kafkaTemplate;

    @RetryableTopic(
            attempts = "3",
            backoff = @Backoff(delay = 500, multiplier = 2.0),
            exclude = JsonProcessingException.class
    )
    @KafkaListener(topics = "order-events", groupId = "inventory-group")
    public void consume(String message) throws Exception {
        OrderCreatedEvent event = objectMapper.readValue(message, OrderCreatedEvent.class);
        boolean reserved = inventoryService.reserveStock(event.productId(), event.quantity());

        String status = reserved ? "RESERVED" : "INSUFFICIENT";
        InventoryEvent result = new InventoryEvent(event.orderId(), event.productId(), status);
        kafkaTemplate.send("inventory-events", objectMapper.writeValueAsString(result));

        if (reserved) {
            log.info("Stock reserved for order {}, product {}", event.orderId(), event.productId());
        } else {
            log.warn("Insufficient stock for order {}, product {}", event.orderId(), event.productId());
        }
    }

    @DltHandler
    public void handleDlt(String message, @Header(KafkaHeaders.RECEIVED_TOPIC) String topic) {
        log.error("Message landed in DLT for topic {}: {}", topic, message);
    }
}
