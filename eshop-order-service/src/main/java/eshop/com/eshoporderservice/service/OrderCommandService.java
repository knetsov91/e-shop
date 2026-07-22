package eshop.com.eshoporderservice.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import eshop.com.eshoporderservice.event.OrderCreatedEvent;
import eshop.com.eshoporderservice.order.model.OrderCommand;
import eshop.com.eshoporderservice.order.repository.OrderCommandRepository;
import eshop.com.eshoporderservice.outbox.OutboxEvent;
import eshop.com.eshoporderservice.outbox.OutboxEventRepository;
import eshop.com.eshoporderservice.web.dto.OrderCommandCreateRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class OrderCommandService {

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
        orderCommand.setStatus("PENDING");

        OrderCommand saved = orderCommandRepository.save(orderCommand);

        try {
            OrderCreatedEvent event = new OrderCreatedEvent(saved.getId(), saved.getProduct(), saved.getQuantity());

            OutboxEvent outboxEvent = new OutboxEvent();
            outboxEvent.setTopic("order-events");
            outboxEvent.setPayload(objectMapper.writeValueAsString(event));
            outboxEvent.setCreatedAt(LocalDateTime.now());
            outboxEventRepository.save(outboxEvent);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize order event", e);
        }

        return saved;
    }
}
