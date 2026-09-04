package eshop.com.eshoporderservice.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import eshop.com.eshoporderservice.event.PaymentRequestedEvent;
import eshop.com.eshoporderservice.order.model.OrderCommand;
import eshop.com.eshoporderservice.order.model.OrderStatus;
import eshop.com.eshoporderservice.order.repository.OrderCommandRepository;
import eshop.com.eshoporderservice.outbox.OutboxEvent;
import eshop.com.eshoporderservice.outbox.OutboxEventRepository;
import eshop.com.eshoporderservice.web.dto.OrderCommandCreateRequest;
import io.sentry.Sentry;
import io.sentry.SentryLevel;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class OrderCommandService {

    private static final String CURRENCY = "USD";

    private final OrderCommandRepository orderCommandRepository;
    private final OutboxEventRepository outboxEventRepository;
    private final ObjectMapper objectMapper;

    public OrderCommandService(OrderCommandRepository orderCommandRepository,
                               OutboxEventRepository outboxEventRepository,
                               ObjectMapper objectMapper) {
        this.orderCommandRepository = orderCommandRepository;
        this.outboxEventRepository = outboxEventRepository;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public OrderCommand createOrder(OrderCommandCreateRequest request) {
        OrderCommand orderCommand = new OrderCommand();
        orderCommand.setProduct(request.getProduct());
        orderCommand.setQuantity(request.getQuantity());
        orderCommand.setAmount(request.getAmount());
        orderCommand.setStatus(OrderStatus.PENDING);

        OrderCommand saved = orderCommandRepository.save(orderCommand);

        Sentry.captureMessage("Order placed: " + saved.getId(), SentryLevel.INFO);

        try {
            PaymentRequestedEvent event = new PaymentRequestedEvent(saved.getId(), saved.getAmount(), CURRENCY);

            OutboxEvent outboxEvent = new OutboxEvent();
            outboxEvent.setTopic("payment-requests");
            outboxEvent.setPayload(objectMapper.writeValueAsString(event));
            outboxEvent.setCreatedAt(LocalDateTime.now());
            outboxEventRepository.save(outboxEvent);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize payment requested event", e);
        }

        return saved;
    }
}
