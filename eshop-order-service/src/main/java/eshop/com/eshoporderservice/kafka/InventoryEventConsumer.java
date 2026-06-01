package eshop.com.eshoporderservice.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import eshop.com.eshoporderservice.event.InventoryEvent;
import eshop.com.eshoporderservice.order.repository.OrderCommandRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class InventoryEventConsumer {

    private final OrderCommandRepository orderCommandRepository;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "inventory-events", groupId = "order-group")
    public void consume(String message) {
        try {
            InventoryEvent event = objectMapper.readValue(message, InventoryEvent.class);

            orderCommandRepository.findById(event.orderId()).ifPresentOrElse(order -> {
                String status = "RESERVED".equals(event.status()) ? "CONFIRMED" : "FAILED";
                order.setStatus(status);
                orderCommandRepository.save(order);
                log.info("Order {} status updated to {}", event.orderId(), status);
            }, () -> log.warn("Order {} not found", event.orderId()));

        } catch (Exception e) {
            log.error("Failed to process inventory event: {}", message, e);
        }
    }
}
