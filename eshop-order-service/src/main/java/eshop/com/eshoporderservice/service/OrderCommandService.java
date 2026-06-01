package eshop.com.eshoporderservice.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import eshop.com.eshoporderservice.event.OrderCreatedEvent;
import eshop.com.eshoporderservice.order.model.OrderCommand;
import eshop.com.eshoporderservice.order.repository.OrderCommandRepository;
import eshop.com.eshoporderservice.web.dto.OrderCommandCreateRequest;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class OrderCommandService {

    private final OrderCommandRepository orderCommandRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    public OrderCommandService(OrderCommandRepository orderCommandRepository,
                               KafkaTemplate<String, String> kafkaTemplate,
                               ObjectMapper objectMapper) {
        this.orderCommandRepository = orderCommandRepository;
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
    }

    public OrderCommand createOrder(OrderCommandCreateRequest request) {
        OrderCommand orderCommand = new OrderCommand();
        orderCommand.setProduct(request.getProduct());
        orderCommand.setQuantity(request.getQuantity());
        orderCommand.setStatus("PENDING");

        OrderCommand saved = orderCommandRepository.save(orderCommand);

        try {
            OrderCreatedEvent event = new OrderCreatedEvent(saved.getId(), saved.getProduct(), saved.getQuantity());
            kafkaTemplate.send("order-events", objectMapper.writeValueAsString(event));
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize OrderCreatedEvent", e);
        }

        return saved;
    }
}