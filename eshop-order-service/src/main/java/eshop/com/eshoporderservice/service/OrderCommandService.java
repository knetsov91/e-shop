package eshop.com.eshoporderservice.service;

import eshop.com.eshoporderservice.order.model.OrderCommand;
import eshop.com.eshoporderservice.order.repository.OrderCommandRepository;
import eshop.com.eshoporderservice.web.dto.OrderCommandCreateRequest;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class OrderCommandService {

    private OrderCommandRepository orderCommandRepository;

    private KafkaTemplate<String, String> kafkaTemplate;

    public OrderCommandService(OrderCommandRepository orderCommandRepository, KafkaTemplate<String, String> kafkaTemplate) {
        this.orderCommandRepository = orderCommandRepository;
        this.kafkaTemplate = kafkaTemplate;
    }

    public OrderCommand createOrder(OrderCommandCreateRequest request) {
        OrderCommand orderCommand = new OrderCommand();
        orderCommand.setProduct(request.getProduct());
        orderCommand.setQuantity(request.getQuantity());
        orderCommand.setStatus("PENDING");

        OrderCommand saved = orderCommandRepository.save(orderCommand);
        kafkaTemplate.send("order-events", "OrderCreated:%s".formatted(saved.getId()));
        return saved;
    }
}