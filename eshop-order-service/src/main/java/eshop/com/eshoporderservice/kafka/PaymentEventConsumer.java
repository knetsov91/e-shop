package eshop.com.eshoporderservice.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import eshop.com.eshoporderservice.event.OrderCreatedEvent;
import eshop.com.eshoporderservice.event.PaymentEvent;
import eshop.com.eshoporderservice.order.model.OrderCommand;
import eshop.com.eshoporderservice.order.model.OrderStatus;
import eshop.com.eshoporderservice.order.repository.OrderCommandRepository;
import eshop.com.eshoporderservice.outbox.OutboxEvent;
import eshop.com.eshoporderservice.outbox.OutboxEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.RetryableTopic;
import org.springframework.retry.annotation.Backoff;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentEventConsumer {

    private final OrderCommandRepository orderCommandRepository;
    private final OutboxEventRepository outboxEventRepository;
    private final ObjectMapper objectMapper;

    @RetryableTopic(
            attempts = "3",
            backoff = @Backoff(delay = 500, multiplier = 2.0),
            exclude = JsonProcessingException.class
    )
    @KafkaListener(topics = "payment-events", groupId = "order-group")
    @Transactional
    public void consume(String message) throws JsonProcessingException {
        PaymentEvent event = objectMapper.readValue(message, PaymentEvent.class);

        orderCommandRepository.findById(event.orderId()).ifPresentOrElse(order -> {
            if ("SUCCEEDED".equals(event.status())) {
                order.setStatus(OrderStatus.AWAITING_INVENTORY);
                orderCommandRepository.save(order);
                publishOrderCreatedEvent(order);
            } else {
                order.setStatus(OrderStatus.PAYMENT_FAILED);
                orderCommandRepository.save(order);
            }
            log.info("Order {} status updated to {}", event.orderId(), order.getStatus());
        }, () -> log.warn("Order {} not found", event.orderId()));
    }

    private void publishOrderCreatedEvent(OrderCommand order) {
        try {
            OrderCreatedEvent event = new OrderCreatedEvent(order.getId(), order.getProduct(), order.getQuantity());

            OutboxEvent outboxEvent = new OutboxEvent();
            outboxEvent.setTopic("order-events");
            outboxEvent.setPayload(objectMapper.writeValueAsString(event));
            outboxEvent.setCreatedAt(LocalDateTime.now());
            outboxEventRepository.save(outboxEvent);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize order created event", e);
        }
    }
}
