package eshop.com.eshoporderservice.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import eshop.com.eshoporderservice.event.InventoryEvent;
import eshop.com.eshoporderservice.order.repository.OrderCommandRepository;
import io.sentry.Sentry;
import io.sentry.SentryLevel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.DltHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.RetryableTopic;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.retry.annotation.Backoff;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class InventoryEventConsumer {

    private final OrderCommandRepository orderCommandRepository;
    private final ObjectMapper objectMapper;

    @RetryableTopic(
            attempts = "3",
            backoff = @Backoff(delay = 500, multiplier = 2.0),
            exclude = JsonProcessingException.class
    )
    @KafkaListener(topics = "inventory-events", groupId = "order-group")
    public void consume(String message) throws JsonProcessingException {
        InventoryEvent event = objectMapper.readValue(message, InventoryEvent.class);

        orderCommandRepository.findById(event.orderId()).ifPresentOrElse(order -> {
            String status = "RESERVED".equals(event.status()) ? "CONFIRMED" : "FAILED";
            order.setStatus(status);
            orderCommandRepository.save(order);
            log.info("Order {} status updated to {}", event.orderId(), status);
            Sentry.captureMessage("Order " + event.orderId() + " status updated to " + status, SentryLevel.INFO);
        }, () -> log.warn("Order {} not found", event.orderId()));
    }

    @DltHandler
    public void handleDlt(String message, @Header(KafkaHeaders.RECEIVED_TOPIC) String topic) {
        log.error("Message landed in DLT for topic {}: {}", topic, message);
        Sentry.captureMessage("DLT message on topic " + topic + ": " + message, SentryLevel.ERROR);
    }
}
